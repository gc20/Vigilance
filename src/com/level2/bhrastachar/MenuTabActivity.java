package com.level2.bhrastachar;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class MenuTabActivity extends TabActivity {

	// Debug Tag
	private static String TAG = MenuTabActivity.class.getSimpleName();
	
	// Stores statistic to be displayed
	public String statistic;
	
	// Stores incidents
	public ArrayList <Incident> IncidentList = new ArrayList <Incident> ();
	
	/*// Stores user location
	public double userLoc [] = new double [2];
	public boolean userLocationAvailable = false;*/  
	
	// Display options for initial lack of data
	AlertDialog alert = null;
	
	// Replace with your URL
	//String url = "http://www.yahoo.com/"; // xx
	String url = "http://www.corruptiontrak.com/";
	
	// Location sensor
	public LocationManager locationManager;
	public LocationListener locationListener;
	
	// Menu Tabs
	public TabHost tabHost;
	
	// Store form entries
	public String titleEditText = "";
	public String descriptionEditText = "";
	public String bribeEditText = "";
	public String offenderEditText = "";
	public String departmentEditText = "";
	public String emailEditText = "";
	public String locationEditText = "";
	
	// User location
	public String stringLocation = "";
	public Geocoder geoCoder;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		// General initialization
		Log.d(TAG, "On create");
	    super.onCreate(savedInstanceState);
	    
	    // Customized title bar
	    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
	    
	    // Get data from server
		boolean connectSuccess = getDataFromServer(); // If data is valid
		
		// Show alert citing network failure
		if (!connectSuccess)
		{
			// Create alert message for the situation in which network connectivity is absent
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
		
		// Locate user
		geoCoder = new Geocoder(this);
		getUserLocation ();
	    
	    // Inflate tab view
		setContentView(R.layout.tabhost);
	    
		// Retrieve tabhost view
		tabHost = (TabHost) findViewById(android.R.id.tabhost);
		
	    // Setup first tab
	    Intent intent = new Intent().setClass(this, ReportActivity.class);
	    setupTab(new TextView(this), "Submit Report", intent);
	    intent = new Intent().setClass(this, MapsActivity.class);
	    setupTab(new TextView(this), "View Map", intent);
	    intent = new Intent().setClass(this, ListingActivity.class);
	    setupTab(new TextView(this), "View List", intent);
	    
		// Begin with the first tab
	    tabHost.setCurrentTab(1);
	    
	    // Add custom title
	    getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.window_title);
	}
	
	
	// Sets up tabs and adds them to tabhost
	private void setupTab(final View view, final String tag, Intent intent) {
		View tabview = createTabView(tabHost.getContext(), tag);
	    TabSpec setContent = tabHost.newTabSpec(tag).setIndicator(tabview).setContent(intent);
		tabHost.addTab(setContent);
	}

	// Obtains view for each tab
	private static View createTabView(final Context context, final String text) {
		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		return view;
	}
	
	
	@Override
	protected void onStart() {
		super.onStart();
	}



	@Override
	protected void onDestroy() {
		super.onDestroy();
		locationManager.removeUpdates(locationListener);
	}

	private boolean getDataFromServer() {
		
		String result = null;
		InputStream is = null;
		StringBuilder sb=null;

		//HTTP post
		try
		{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url + "getReport.php");
			Log.d (TAG, url+"getReport.php ");
			//httppost.setEntity(new UrlEncodedFormEntity(IncidentList));
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();
		}
		catch(Exception e){
			Log.d(TAG, "Error in http connection"+e.toString());
			// Sample data xx
			IncidentList.add(new Incident("Paid Bribe to Police Office", "Irritated 1", "Kilpauk, Chennai", 13.08, 80.23, new Date ().getTime() - 10000, "Department1", 90, "Offender1", "email1@gmail.com"));
			IncidentList.add(new Incident("Police office did not file FIR", "Frustrated 2. This is going to be a very long message because I wish to test the length of the message. In other words, its elasticity.", "MG Road, Bangalore, Karnataka", 12.970214, 77.56029, new Date ().getTime() - 5000, "Department2", 95, "Offender2", "email2@gmail.com"));
			
		}

		//convert response to string
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
			sb = new StringBuilder();
			sb.append(reader.readLine() + "\n");
			String line="0";

			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}

			is.close();
			result=sb.toString();
			Log.d (TAG, "Result: " + result); // xx

		}
		catch(Exception e)
		{
			Log.d(TAG, "Error converting result "+e.toString());
		}

		//Parse data
		IncidentList.clear();
		try
		{
	      	JSONArray jArray = new JSONArray(result);
	      	JSONObject json_data=null;
	      	Incident tempIncident;
	      	IncidentList.clear();
	      	
	      	for(int i=(jArray.length()-1);i>=0;i--)
	      	{
	      		Log.d (TAG, "Before json_data");
					json_data = jArray.getJSONObject(i);
					tempIncident = new Incident(
							json_data.getString("title"), 
							json_data.getString("message") + 
							"This is a long message intended to reflect the " +
							"true length of ordinary messages that we expect.", 
							json_data.getString("location"),
							json_data.getDouble("latitude"),
							json_data.getDouble("longitude"),
							json_data.getLong("time"),
							json_data.getString("dept"),
							json_data.getInt("bribe"),
							json_data.getString("offender"),
							json_data.getString("email"));
					IncidentList.add(tempIncident);
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
			    	
			    	Log.d (TAG, "Location changed.");
			    	
			    	try
			    	{
				    	Log.d (TAG, "Obtained Location " + location.getLatitude() + " " + location.getLongitude());
				    	//List<Address> address = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
						//stringLocation = address.get(0).getAddressLine(0);
						Log.d (TAG, "Location: " + stringLocation);
			    	}
			    	catch (Exception e)
			    	{ Log.d (TAG, "Location management error.");}
					/*userLoc [0] = location.getLatitude();
			    	userLoc [1] = location.getLongitude();
			    	userLocationAvailable = true;*/
			    }
	
			    @Override
				public void onStatusChanged(String provider, int status, Bundle extras) {}
	
			    @Override
				public void onProviderEnabled(String provider) {}
	
			    @Override
				public void onProviderDisabled(String provider) {}
			  };
	
			// Register the listener with the Location Manager to receive location updates
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);		
			Log.d (TAG, "Requested location updates");
		}
		catch (Exception e)
		{ Log.d (TAG, "Error in registering location manager");}
	}
	
	// When "menu" is selected
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	

	// When menu options are selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		
			case R.id.refresh:
				
				// Display progress dialog
				String childActivity = getCurrentActivity().toString();
				Log.d (TAG, "Child: " + childActivity);
				if (childActivity.length() >= 35 && childActivity.substring(23, 35).equals("MapsActivity"))
					((MapsActivity) getCurrentActivity()).displayprogressUI();
				if (childActivity.length() >= 38 && childActivity.substring(23, 38).equals("ListingActivity"))
					((ListingActivity) getCurrentActivity()).displayprogressUI();
				if (childActivity.length() >= 37 && childActivity.substring(23, 37).equals("ReportActivity"))
					((ReportActivity) getCurrentActivity()).displayprogressUI();
				
			
				try {
					Thread refreshThread = new Thread() {
						@Override
						public void run() {
							// Get data from server
							boolean connectSuccess = getDataFromServer(); // If data is valid
							Message sendMessage = new Message();
							if (connectSuccess)
								sendMessage.arg1 = 1;
							else
								sendMessage.arg1 = 0;
							handlerRefresh.sendMessage(sendMessage);
						}
					};
					refreshThread.start();
				} catch (Exception e) {
					Log.d(TAG, "RefreshThread thread failed");
				}
			   
				break;
				
			case R.id.email:   
				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);  
		        String aEmailList[] = { "corruptiontrak@gmail.com" };  
			    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);  
			    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "User Enquiry");  
			    emailIntent.setType("plain/text");  
			    startActivity(emailIntent);  
				break;
		
			case R.id.info:
				//Set up dialog
                Dialog dialog = new Dialog(this);
                dialog.setContentView(R.layout.aboutus);
                dialog.setTitle("About Us");
                dialog.setCancelable(true);
                dialog.show();
                break;
		
			default:
				break;
		}

		return true;
	}
	


	private Handler handlerRefresh = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			// Get child activity
			String childActivity = getCurrentActivity().toString();
			
			// Show alert citing network failure
			if (msg.arg1 == 0)
			{
				// Create alert message for the situation in which network connectivity is absent
				AlertDialog.Builder builder = new AlertDialog.Builder(MenuTabActivity.this);
				builder.setMessage("Cannot connect to server presently: Please try again later.")
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
			if (msg.arg1 == 1)
			{
				// Update UI
				if (childActivity.length() >= 35 && childActivity.substring(23, 35).equals("MapsActivity"))
					((MapsActivity) getCurrentActivity()).updateUI();
				if (childActivity.length() >= 38 && childActivity.substring(23, 38).equals("ListingActivity"))
					((ListingActivity) getCurrentActivity()).updateUI();
				if (childActivity.length() >= 37 && childActivity.substring(23, 37).equals("ReportActivity"))
					((ReportActivity) getCurrentActivity()).updateUI();	
				
			}
			
			// Dismiss progress dialog
			if (childActivity.length() >= 35 && childActivity.substring(23, 35).equals("MapsActivity"))
				((MapsActivity) getCurrentActivity()).dismissprogressUI();
			if (childActivity.length() >= 38 && childActivity.substring(23, 38).equals("ListingActivity"))
				((ListingActivity) getCurrentActivity()).dismissprogressUI();
			if (childActivity.length() >= 37 && childActivity.substring(23, 37).equals("ReportActivity"))
				((ReportActivity) getCurrentActivity()).dismissprogressUI();
			
		}
	};

	
}