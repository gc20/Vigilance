package com.level2.bhrastachar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class MapsActivity extends MapActivity {

	private static String TAG = MapsActivity.class.getSimpleName();
	private MapView mapView;
	
	// Stores incidents
	public ArrayList <Incident> IncidentList = new ArrayList <Incident> ();
	
	// Overlay variables
	List<Overlay> mapOverlays;
	Drawable drawable;
	MapsOverlayDraw itemizedoverlay;
	GeoPoint point;
	OverlayItem overlayitem;
	
	// Refresh progress dialog
	ProgressDialog dialogRefresh;
	
	@Override
	protected void onCreate(Bundle arg0) {

		Log.d(TAG, "On create ");
		
		super.onCreate(arg0);
		Log.d (TAG, "After super create");
		setContentView(R.layout.mapview);

		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);

		IncidentList = ((MenuTabActivity)this.getParent()).IncidentList;
		
		// Obtain accident overlay marker
		drawable = this.getResources().getDrawable(R.drawable.ic_bullet_key_permission);
		
		// Add overlays
		updateUI();
		
		// Zoom to India
		zoomIndia();
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
	
	protected void zoomIndia () {

		// Zoom to India borders
		int maxLat = (int)(37.5*1e6); int minLat = (int)(6.73*1e6);
		int maxLon = (int)(97.42*1e6); int minLon = (int)(68.12*1e6);
	    mapView.getController().zoomToSpan(((maxLat - minLat)), ((maxLon - minLon)));
	    mapView.getController().animateTo(new GeoPoint((maxLat + minLat)/2, (maxLon + minLon)/2));
	}
	
	protected void updateUI (){
		
		// Initialize itemized overlay
		MapsOverlayDraw itemizedoverlay = new MapsOverlayDraw(drawable, this);
		
		// Obtain map overlays
		mapOverlays = mapView.getOverlays();
		
		// Remove existing overlays
		mapOverlays.clear();
		
		// Add alerts
		int size = IncidentList.size();
		Incident tempIncident;
		
		// Add each incident to map
		for (int i=0; i<size; i++)
		{
			tempIncident = IncidentList.get(i);
			point = new GeoPoint((int) (tempIncident.getLatitude() * 1e6), (int) (tempIncident.getLongitude() * 1e6));
			
			String incidentDetails = "";
			String incidentTitle = tempIncident.getTitle();
			if (incidentTitle.equals(""))
				incidentTitle = "Untitled";
			if (!tempIncident.getLocation().equals(""))
				incidentDetails += "Location: \n" + tempIncident.getLocation() + "\n\n";;
			if (!tempIncident.getDepartment().equals(""))
				incidentDetails += "Department: \n" + tempIncident.getDepartment() + "\n\n";
			if (!tempIncident.getOffender().equals(""))
				incidentDetails += "Offender: \n" + tempIncident.getOffender() + "\n\n";
			if (tempIncident.getBribe()!=0)
				incidentDetails += "Monetary Amount: \n" + tempIncident.getBribe() + "\n\n";
			if (!tempIncident.getMessage().equals(""))
				incidentDetails += "Description: \n" + tempIncident.getMessage() + "\n\n";
			if (tempIncident.getReportTime()!=0)
				incidentDetails += "Reported Time: \n" + new Date(tempIncident.getReportTime()).toLocaleString();
			incidentDetails = incidentDetails.trim();
			Log.d (TAG, tempIncident.getLatitude() + " " + tempIncident.getLongitude() + " ");
			Log.d (TAG, incidentDetails);
			
			overlayitem = new OverlayItem(point, incidentTitle, 
					incidentDetails);

			itemizedoverlay.addOverlay(overlayitem);
			//mapView.getController().animateTo(point);
		}

		// Add itemizedoverlap to map
		if (size!=0)
			mapOverlays.add(itemizedoverlay);
	}

	protected void displayprogressUI (){
		dialogRefresh = ProgressDialog.show(this, "", "Loading. Please wait...", false);
		dialogRefresh.show();
	}
	
	protected void dismissprogressUI (){
		dialogRefresh.dismiss();
	}

	
}
