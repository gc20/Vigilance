package com.level2.bhrastachar;

import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class ListingActivity extends ListActivity {

	// Debug TAG
	private static String TAG = ReportActivity.class.getSimpleName();
	
	// Stores incidents
	public ArrayList <Incident> IncidentList = new ArrayList <Incident> ();
	
	// List adapter
	ListingListAdapter dAdapter;
	
	// Loading progress dialog
	ProgressDialog dialogRefresh;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	    
		setContentView(R.layout.listing);
	    Log.d(TAG, "On create");
		
		// Let tab activity handle updating of array list
		IncidentList = ((MenuTabActivity) (this.getParent())).IncidentList;
		
		dAdapter= new ListingListAdapter (this, R.layout.listinglist, ((MenuTabActivity) (this.getParent())).IncidentList, 
				((MenuTabActivity) (this.getParent())).locationManager, ((MenuTabActivity) (this.getParent())).locationListener);
				//((MenuTabActivity) (this.getParent())).userLocationAvailable, ((MenuTabActivity) (this.getParent())).userLoc);
		setListAdapter(dAdapter);
	}


	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		updateUI();
	}

	
	@Override
	protected void onResume() {
		super.onRestart();
		updateUI();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		Incident tempIncident = IncidentList.get(position);
		
		/*// Get category
		String tempCategory;
		switch (tempIncident.getCategory()){ // xx Populate with category
			case 1: tempCategory = "Education"; break;
			case 2: tempCategory = "Traffic"; break;
			case 3: tempCategory = "Public Utilities"; break;
			case 4: tempCategory = "Unavailable"; break;
		}*/
		
		String incidentDetails = "";
		if (tempIncident.getTitle().equals(""))
			incidentDetails += "Title: \nUntitled\n\n"; 
		else
			incidentDetails += "Title: \n" + tempIncident.getTitle() + "\n\n";
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
		
		AlertDialog.Builder builder = new AlertDialog.Builder(ListingActivity.this);
		builder.setMessage(incidentDetails)
		.setCancelable(false).setPositiveButton("Close", 
				new DialogInterface.OnClickListener() {
		           @Override
				public void onClick(DialogInterface dialog, int id) {
		        	   dialog.cancel();
		           }
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	protected void updateUI (){
		Log.d (TAG, "Notify data set changed start.");
		dAdapter.notifyDataSetChanged();
		Log.d (TAG, "Notify data set changed stop.");
	}

	protected void displayprogressUI (){
		dialogRefresh = ProgressDialog.show(this, "", "Loading. Please wait...", false);
		dialogRefresh.show();
	}
	
	protected void dismissprogressUI (){
		dialogRefresh.dismiss();
	}
	
}