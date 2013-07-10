package com.hacknroll.crimewatch;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

public class ListingActivity extends ListActivity {

	// Debug TAG
	private static String TAG = ListingActivity.class.getSimpleName();
	
	// Stores incidents
	public ArrayList <Crimes> Crimes = new ArrayList <Crimes> ();
	
	// List adapter
	ListingListAdapter dAdapter;
	
	// Loading progress dialog
	ProgressDialog dialogRefresh;
	
	// Manage obtaining data at first
	Runnable firstDataThread;
    Handler mHandler = new Handler();
    
    // Alert message to show progress
    AlertDialog progressAlert;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	    
		setContentView(R.layout.listing);
	    //Log.d(TAG, "On create");
	    
		// Let tab activity handle updating of array list
		Crimes = ((MenuTabActivity) (this.getParent())).Crimes;
		
		dAdapter= new ListingListAdapter (this, R.layout.listinglist, ((MenuTabActivity) (this.getParent())).Crimes, 
				((MenuTabActivity) (this.getParent())).locationManager, ((MenuTabActivity) (this.getParent())).locationListener);
				//((MenuTabActivity) (this.getParent())).userLocationAvailable, ((MenuTabActivity) (this.getParent())).userLoc);
		setListAdapter(dAdapter);
	    
	    // Create progress alert dialog
	    /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Retrieving activities ...")
				.setTitle("Please Wait")
				.setCancelable(false);
		progressAlert = builder.create();*/
		
	    // Get data based on location, the first time
	    //getFirstData();
	}


	@Override
	protected void onStart() {
		super.onStart();
	}
	
	@Override
	protected void onRestart() {
		//Log.d (TAG, "Restart");
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
		
		Crimes tempIncident = Crimes.get(position);
		
		String incidentDetails = "";
		if (tempIncident.Crime_Name.equals(""))
			incidentDetails += "Crime Name: \nUnavailable\n\n"; 
		else
			incidentDetails += "Crime Name: \n" + tempIncident.Crime_Name + "\n\n";
		if (!tempIncident.Address.equals(""))
			incidentDetails += "Location: \n" + tempIncident.Address + "\n\n";;
		if (!tempIncident.City.equals(""))
			incidentDetails += "City: \n" + tempIncident.City + "\n\n";
		if (!tempIncident.Description.equals(""))
			incidentDetails += "Description: \n" + tempIncident.Description + "\n\n";
		incidentDetails = incidentDetails.trim();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(ListingActivity.this);
		builder.setMessage(incidentDetails).setTitle("Incident Details")
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
	
	// When "menu" is selected
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mainmenu, menu);
		return true;
	}
	
	protected void updateUI (){
		//Log.d (TAG, "Notify data set changed start.");
		dAdapter.notifyDataSetChanged();
		//Log.d (TAG, "Notify data set changed stop.");
	}

	protected void displayprogressUI (){
		dialogRefresh = ProgressDialog.show(this, "", "Loading. Please wait...", false);
		dialogRefresh.show();
	}
	
	protected void dismissprogressUI (){
		//Log.d (TAG, "In dismiss");
		if (dialogRefresh!=null && dialogRefresh.isShowing())
			dialogRefresh.dismiss();
		//Log.d (TAG, "In dismiss");
		if (progressAlert!=null && progressAlert.isShowing())
			progressAlert.dismiss();
		//Log.d (TAG, "In dismiss");
	}
	
	public void getFirstData () {
		
		try{
			if (((MenuTabActivity)this.getParent()).locationStartupLoad)
			{
				//Log.d (TAG, "IF firstData");
				// Display progress dialog
				progressAlert.show();
				
				firstDataThread = new Runnable() {
					@Override
					public void run() {
						//Log.d (TAG, "In firstDataThread");
						Message tempMessage = new Message();
						tempMessage.arg1 = 0;
						if (listGetDataFromServer(1)) {
							tempMessage.arg1 = 1;
						}
					}
				};
				//Log.d (TAG, "Start firstDataThread");
				mHandler.removeCallbacks(firstDataThread);
		        mHandler.postDelayed(firstDataThread, 100);
				//Log.d (TAG, "Exiting getFirstData");
			}
		}
		catch (Exception e) {
			//Log.d (TAG, "Location data refresh failed");
		}
	}

	public boolean listGetDataFromServer (int i) {
		return (((MenuTabActivity)this.getParent()).getDataFromServer());
	}
	
}