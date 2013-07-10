package com.hacknroll.crimewatch;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.hacknroll.crimewatch.map.GeoClusterer;
import com.hacknroll.crimewatch.map.GeoItem;
import com.hacknroll.crimewatch.map.MarkerBitmap;

public class MapsActivity extends MapActivity implements Runnable {

	private static String TAG = MapsActivity.class.getSimpleName();
	
	// Map view controls
	private EnhancedMapView mapView;
	public MapController mapController;
	
	// Stores incidents
	public ArrayList <Crimes> Crimes = new ArrayList <Crimes> ();
	
	// Refresh progress dialog
	ProgressDialog dialogRefresh;

	// Marker Icons
	private List<MarkerBitmap> markerIconBmps = new ArrayList<MarkerBitmap>();
	
	// Screen density
	private float screenDensity;

	// Old zoom level of map
	int oldZoomLevel=-1;
	
	// Is user planning
	boolean userIsPanning = true;

	// Map Center
	GeoPoint mapCenter;
	
	// Update thread
	Thread updateThread;
	
	// Check to ensure that only one onTap alert dialog is displayed
	public static boolean alertDialogCheck [] = {false};
	
	@Override
	protected void onCreate(Bundle arg0) {

		//Log.d(TAG, "On create");
		super.onCreate(arg0);
	
		this.mapView = ((MenuTabActivity)this.getParent()).mapView;
		if (mapView == null)
			mapView = new EnhancedMapView (this, this.getString(R.string.mapKey));
		setContentView(mapView);
		mapController = mapView.getController();
		mapView.setClickable (true);
		mapView.setBuiltInZoomControls(true);
		
		zoomLocation();
		
		// Setup updateThread after first showing a loading screen
		displayprogressUI();
		Log.d (TAG, "Updating thread");
		updateThread = new Thread (this);

		// Get incidents
		Crimes = ((MenuTabActivity)this.getParent()).Crimes;
		
		// Prepare for marker icons
		// Small icon for maximum 10 items
		//Log.d (TAG, "Bitmap 1");
		markerIconBmps.add(
			new MarkerBitmap(
					BitmapFactory.decodeResource(getResources(), R.drawable.m1),
					BitmapFactory.decodeResource(getResources(), R.drawable.m1),
					new Point(20,20),
					14,
					10)
			);
		
		// Large icons. 100 will be ignored.
		//Log.d (TAG, "Bitmap 2");
		markerIconBmps.add(
				new MarkerBitmap(
						BitmapFactory.decodeResource(getResources(), R.drawable.m3),
						BitmapFactory.decodeResource(getResources(), R.drawable.m3),
						new Point(28,28),
						16,
						100)
				);

		// Add overlays
		//Log.d (TAG, "Updating UI");
		updateUI();
		
		// Set listeners
		mapView.setOnZoomChangeListener(new EnhancedMapView.OnZoomChangeListener() {
	        @Override
	        public void onZoomChange(MapView view, int newZoom, int oldZoom) {
	            //Log.d("test", "Zoom changed from " + oldZoom + " to " + newZoom);
	            reorderData();
	        }
	    });
		mapView.setOnPanChangeListener(new EnhancedMapView.OnPanChangeListener() {
	        public void onPanChange(MapView view, GeoPoint newCenter, GeoPoint oldCenter) {
	            //Log.d("test", "Center changed from " + oldCenter.getLatitudeE6() + "," + oldCenter.getLongitudeE6() + " to " + newCenter.getLatitudeE6() + "," + newCenter.getLongitudeE6());
	            reorderData();
	        }
	    });
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	
	@Override
	protected void onRestart() {
		super.onRestart();
		updateUI();
	}	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		updateUI();
	}
	
	
	
	protected void zoomLocation () {

		// Zoom to relevant borders
		int maxLat = (int)(38*1e6); int minLat = (int)(36*1e6);
		int maxLon = (int)(-121*1e6); int minLon = (int)(-123*1e6);
	    mapController.zoomToSpan(((maxLat - minLat)), ((maxLon - minLon)));
	    mapController.animateTo(new GeoPoint((maxLat + minLat)/2, (maxLon + minLon)/2));
	    mapView.invalidate();
	}
	

	// Add map overlays
	public synchronized void updateUI () {
		
		//Log.d (TAG, "UpdateUI");
		// Initialize handler and threads which perform registration after 10 milliseconds
		screenDensity = this.getResources().getDisplayMetrics().density;
		Runnable mUpdateUI = new Runnable() {
        	   public void run() {
        		   try
        		   {

        				//Log.d (TAG, "Runnable UpdateUI");
        				// Create Clusterer instance
        				GeoClusterer clusterer = new GeoClusterer(mapView,markerIconBmps,screenDensity, alertDialogCheck, MapsActivity.this);
        				
        				// Add Geoitems for clustering
        				int i;
        				for(i=0; i<Crimes.size(); i++) {
        					clusterer.addItem(new GeoItem(i,(int)(Crimes.get(i).Latitude*1E6),
        							(int)(Crimes.get(i).Longitude*1E6), Crimes.get(i)));
        				}
        				/*for (i=0; i<mapView.getOverlays().size(); i++)
    					Overlay overlay = mapView.getOverlays().get(i);*/
        				
        				// Clear previous overlays
        				mapView.getOverlays().clear();
        				
        				// Now redraw the cluster to create markers
        				clusterer.redraw();
        				mapView.invalidate();
        		   }
        		   catch (Exception e)
        		   {//Log.d (TAG, "Exception in mUpdateUI");
        			 
        		   }
        		   
        	   }
        };
        Handler registerHandler = new Handler();
        registerHandler.removeCallbacks(mUpdateUI);
        registerHandler.postDelayed(mUpdateUI, 10); // 10ms to register

	}

	protected void displayprogressUI (){
		dialogRefresh = ProgressDialog.show(this, "", "Loading. Please wait...", false);
		dialogRefresh.show();
	}
	
	protected void dismissprogressUI (){
		dialogRefresh.dismiss();
	}

	public void reorderData () {

		//Log.d (TAG, Double.toString(((MenuTabActivity)this.getParent()).centerLatitude));
		//Log.d (TAG, Double.toString(((MenuTabActivity)this.getParent()).centerLongitude));
		//Log.d (TAG, Double.toString(((MenuTabActivity)this.getParent()).circleRadius));
		
		if (updateThread.isAlive())
			return;
		
		// Store map zoom levels
		//Log.d (TAG, "onZoom");
		double horizontalDiameter = haversianDistance
			(mapView.getMapCenter().getLatitudeE6()/1e6, 
			(mapView.getMapCenter().getLongitudeE6() - mapView.getLongitudeSpan()/2)/1e6, 
			mapView.getMapCenter().getLatitudeE6()/1e6,
			(mapView.getMapCenter().getLongitudeE6() + mapView.getLongitudeSpan()/2)/1e6);
		double verticalDiameter = haversianDistance
			((mapView.getMapCenter().getLatitudeE6() - mapView.getLatitudeSpan()/2)/1e6, 
			mapView.getMapCenter().getLongitudeE6()/1e6, 
			(mapView.getMapCenter().getLatitudeE6() + mapView.getLatitudeSpan()/2)/1e6,
			mapView.getMapCenter().getLongitudeE6()/1e6);
			
		if (verticalDiameter > horizontalDiameter)
			((MenuTabActivity)this.getParent()).circleRadius = verticalDiameter;
		else
			((MenuTabActivity)this.getParent()).circleRadius = horizontalDiameter;
		((MenuTabActivity)this.getParent()).centerLatitude = mapView.getMapCenter().getLatitudeE6()/1e6;
		((MenuTabActivity)this.getParent()).centerLongitude = mapView.getMapCenter().getLongitudeE6()/1e6;
		
		// Update data
		if (!updateThread.isAlive())
			updateThread.run();
	}
	
	@Override
	public void run() {
		((MenuTabActivity)this.getParent()).getDataFromServer();
		handlerUpdate.sendMessage(new Message());
	}

	private Handler handlerUpdate = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			updateUI();
			dismissprogressUI();
		}
	};
	
	// Calculates distance between consecutive GPS coordinates
	private double haversianDistance (double lat1, double lon1, double lat2, double lon2) {
		
		//Log.d (TAG, lat1 + " " + lon1 + " " + lat2 + " " + lon2);
		double piConst = Math.PI / 180;
		double R = 6371; // metres
		double dLat = (lat2-lat1) * piConst;
		double dLon = (lon2-lon1) * piConst; 
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		        Math.cos(lat1  * piConst) * Math.cos(lat2 * piConst) * 
		        Math.sin(dLon/2) * Math.sin(dLon/2); 
		a = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		return (Math.abs(R * a));
		
	}

	
}
