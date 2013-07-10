package com.hacknroll.crimewatch;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class MenuTabActivity extends TabActivity {

	// Debug Tag
	private static String TAG = MenuTabActivity.class.getSimpleName();
	
	// Splash screen dialog
	protected Dialog mSplashDialog;
	protected int splashDisplayTime = 2000; // Minimum milliseconds for which splash screen is displayed
	
	// Stores incidents
	public ArrayList <Crimes> Crimes = new ArrayList <Crimes> ();
	
	// Display options for initial lack of data
	AlertDialog alert = null;
	
	// Success of connectivity at the outset
	boolean connectSuccessInitial = true;
	
	// Check if initial splash screen has loaded
	boolean initialSplashScreen = true;
	
	// The value checks if the location listener should call server refresh (not required after first iteration)
	public boolean locationStartupLoad = true;
	
	// Location sensor
	public LocationManager locationManager;
	public LocationListener locationListener;
	
	// Menu Tabs
	public TabHost tabHost;
	
	// Resources
	public Resources resourses;
	
	// User location
	public Geocoder geoCoder;
	public double currentLatitude = 37.775002;
	public double currentLongitude = -122.418335;
	public String currentLocation = "";
	
	// Stores mapView
	public EnhancedMapView mapView = null;
	
	// Map Parameters
	double circleRadius = 100;
	double centerLatitude = 37.775002;
	double centerLongitude = 122.418325;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    
		// Show splash screen
	    mSplashDialog = new Dialog(getWindow().getContext(), R.style.CustomTheme);
	    
	    // Get user location
		getUserLocation();	    
	    
	    // Display customized title bar
	    setTheme(R.style.CustomTheme);
	    
		// Inflate tab view
		setContentView(R.layout.tabhost);
		
	    // Run super function
	    super.onCreate(savedInstanceState);
	    
	    // Initialize geocoder
		geoCoder = new Geocoder(this);
	    
		// Get resources variable
		resourses = getResources();
		
		// Retrieve tabhost view
		tabHost = (TabHost) findViewById(android.R.id.tabhost);
		
	    // Setup tabs
		tabHost.getTabWidget().setDividerDrawable(R.drawable.divider_vertical_bright);
		Intent intent = new Intent().setClass(this, MapsActivity.class);
	    setupTab(new TextView(this), getString(R.string.tabMap), intent, R.drawable.ic_tab_mapmode);
	    intent = new Intent().setClass(this, ListingActivity.class);
	    setupTab(new TextView(this), getString(R.string.tabList), intent, R.drawable.ic_tab_friendslist);
		
		// Begin with the first tab
		tabHost.setCurrentTab(0);
		
		// Hide keyboard when switching tabs
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId)
            {
            	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            	imm.hideSoftInputFromWindow(tabHost.getApplicationWindowToken(), 0);
            }
        });

	   // Wait for 1 second
	    try {
		    //initialConnectThread.run();

	    	Thread.sleep(500);
	    }
	    catch (Exception e)
	    {}
	    
	 
	}
	
	
	// Sets up tabs and adds them to tabhost
	private void setupTab(final View view, final String tag, Intent intent, int drawableID) {
		View tabview = createTabView(tabHost.getContext(), tag, resourses.getDrawable(drawableID));
	    TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview).setContent(intent);
		/*TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tag,
                getResources().getDrawable(drawableID)).setContent(intent);*/
		tabHost.addTab(setContent);
	}

	// Obtains view for each tab
	private static View createTabView(final Context context, final String text, Drawable drawableResource) {
		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
		ImageView iv = (ImageView) view.findViewById(R.id.imageViewTabs);
		iv.setImageDrawable(drawableResource);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}
	
	
	@Override
	protected void onStart() {
		super.onStart();
		
		// Indicate that initial splash screen has loaded
		initialSplashScreen = false;
		//Log.d (TAG, "On start");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(locationListener);
	}
	
	//Thread for obtaining data at the outset
    Runnable initialConnectThread = new Runnable () {
		@Override
		public void run() {

			// Get user location
			getUserLocation();
		
			// Get map data from server initially
			connectSuccessInitial = getDataFromServer();
			
			// Show alert citing network failure
			if (!connectSuccessInitial)
			{
				serverFailureMessage();
				connectSuccessInitial = true;
			}
		}
    };
    
	// Remove splash screen
	protected void removeSplashScreen() {
	    
		if (mSplashDialog != null) {
	        mSplashDialog.dismiss();
	        mSplashDialog = null;
	    }
	}
	 

	public boolean getDataFromServer() {
		
		String result = null;
		InputStream is = null;

		//HTTP Get
		try
		{
			// Prepare for potential timeouts
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			int timeoutConnection = 3000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout for waiting for data.
			int timeoutSocket = 5000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			
			// Setup HTTP variables
			HttpClient httpclient = new DefaultHttpClient(httpParameters);
			HttpGet httpGet;
			String requestString = "";
			
			requestString = "http://dev.semantics3.com:8080/query?latitude=" +
				currentLatitude + "&longitude=" + currentLongitude + 
				"&radius=" + 10;

			Log.d(TAG, requestString);
				
			// Make request
			URLConnection yc = new URL (requestString).openConnection();
	        BufferedReader in = new BufferedReader(
	                                new InputStreamReader(
	                                yc.getInputStream()));
	        Log.d(TAG, "After post");
			StringBuilder sb = new StringBuilder();
			sb.append(in.readLine() + "\n");
			String line="0";
			while ((line = in.readLine()) != null) {
				sb.append(line + "\n");
			}
			Log.d (TAG, "SB: " + sb); // xx
			result=sb.toString();
			Log.d (TAG, "Result: " + result); // xx

		}
		catch(Exception e)
		{
			Log.d(TAG, "Error converting result "+e.toString());
		}

		//Parse data
		try
		{
			JSONObject myjson = new JSONObject(result.trim());
			myjson = myjson.getJSONObject("Results");
	      	//Log.d(TAG, "After post 6");
	      	JSONArray jArray = myjson.getJSONArray("CrimeListDetail");
	      	JSONObject jsonReceived = null;
	      	Crimes tempCrime;
	      	if (jArray.length() != 0)
	      	{
	      		Crimes.clear();
	      	}

	      	//Log.d(TAG, "Before for");
	      	// Date formatter
		    //SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	      	for(int i=(jArray.length()-1);i>=0;i--)
	      	{
	      		//Log.d (TAG, Integer.toString(i));
	      		jsonReceived = jArray.getJSONObject(i);

	      		//Log.d (TAG, Integer.toString(i));
				//jsonReceived = new JSONArray ("[" + jsonReceived.getString("incident") + "]").getJSONObject(0);

	      		//Log.d (TAG, Integer.toString(i));
	      		tempCrime = new Crimes(
						jsonReceived.getString("id"), 
						jsonReceived.getString("description"),
						jsonReceived.getString("calc_date"),
						jsonReceived.getString("calc_time"),
						jsonReceived.getString("address"), 
						jsonReceived.getString("city"),
						jsonReceived.getString("crime_name"),
						jsonReceived.getString("crime_type"),
							jsonReceived.getDouble("latitude"),
							jsonReceived.getDouble("longitude"));
		      		
		      	//Log.d (TAG, "Crimes: " + jsonReceived.getString("id"));
		      	Crimes.add(tempCrime);
	      	}
	 
		}
		catch(Exception e1){
			return (false);
		}
		
		return (true);
	}

	
	private void getUserLocation () {
		try
		{
			// Acquire a reference to the system Location Manager
			locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	
			// Define a listener that responds to location updates
			locationListener = new LocationListener() {
				
			    @Override
				public void onLocationChanged(Location location) {
			    	
			    	//Log.d (TAG, "Location changed.");
			    	
			    	try
			    	{
				    	currentLatitude = location.getLatitude();
						currentLongitude = location.getLongitude();
						currentLatitude = 37.775002;
						currentLongitude = -122.418335;
						circleRadius = 100;
					}
			    	catch (Exception e)
			    	{ //Log.d (TAG, "Location management error.");
			    	}
			    }
	
			    @Override
				public void onStatusChanged(String provider, int status, Bundle extras) {}
	
			    @Override
				public void onProviderEnabled(String provider) {}
	
			    @Override
				public void onProviderDisabled(String provider) {}
			  };
	
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 300*1000, 1000, locationListener);		
			//Log.d (TAG, "Requested location updates");
		}
		catch (Exception e)
		{ //Log.d (TAG, "Error in registering location manager");
		}
	}

	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//Log.d (TAG, "on Touch event");
		if (initialSplashScreen)
			return false;
		else
			return super.onTouchEvent(event);
	}

	// When menu options are selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {


		switch (item.getItemId()) {
		
			case R.id.policeCall:
				call();
				break;
				
			default:
				break;
		}

		return true;
	}
	
	private void call() {
		try {
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:65656565"));
			startActivity(callIntent);
		} catch (Exception e) {
		Log.e("Calling police", "Call failed", e);
		}
	}
	
	public void serverFailureMessage () {
		
		// Create alert message for the situation in which network connectivity is absent
		if (alert!=null && alert.isShowing())
			alert.cancel();
		AlertDialog.Builder builder = new AlertDialog.Builder(MenuTabActivity.this);
		builder.setMessage("Cannot connect to server presently: Please try later.")
		.setCancelable(false).setPositiveButton("OK", 
				new DialogInterface.OnClickListener() {
		           @Override
				public void onClick(DialogInterface dialog, int id) {
		        	   dialog.cancel();
		           }
		       });
		alert = builder.create();
	    alert.show();

	}
	
}