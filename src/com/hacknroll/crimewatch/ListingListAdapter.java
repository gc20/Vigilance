package com.hacknroll.crimewatch;

import java.util.ArrayList;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListingListAdapter extends ArrayAdapter<Crimes> {
	
	// Debug Tag
	private static String TAG = ListingListAdapter.class.getSimpleName();
	
	private ArrayList <Crimes> IncidentList;
	/*private boolean userLocationAvailable;
	private double userLoc [];*/
	LocationManager locationManager;
	LocationListener locationListener;
	
    public ListingListAdapter(Context context, int textViewResourceId, ArrayList<Crimes> IncidentList, LocationManager locationManager, LocationListener locationListener) {
    	super(context, textViewResourceId, IncidentList);
        this.IncidentList = IncidentList;
        this.locationManager = locationManager;
        this.locationListener = locationListener;
        //this.userLocationAvailable = userLocationAvailable;
        //this.userLoc = userLoc;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
    	View v = convertView;
        if (v == null) {
        	LayoutInflater vi = (LayoutInflater) (getContext()).getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.listinglist, null);
        }
        
        Crimes in = IncidentList.get(position);
        //int categoryNum = 0; // Test
        if (in != null) {
			
        	TextView in1 = (TextView) v.findViewById(R.id.listingList1); 
			TextView in2 = (TextView) v.findViewById(R.id.listingList2);
			TextView in3 = (TextView) v.findViewById(R.id.listingList3);
			TextView in4 = (TextView) v.findViewById(R.id.listingList4);
			
			// Incident category is displayed
			if (in1 != null) {
				in1.setText(in.Crime_Name);
			}
			
			// Incident description
			if (in3 != null) {
				in3.setText (in.Description);
			}
						
			// Location and distance from incident
			if (in4 != null) {
				try
				{
					in4.setVisibility(0);
					//locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);		
					Location userLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					////Log.d (TAG, "Checking userloc");
					if (userLoc != null)
					{
						////Log.d (TAG, "userloc not null ");
						if (!(in.Latitude==0.0 && in.Longitude==0.0))
						{
							////Log.d (TAG, "lat lon not 0");
							double distance = haversianDistance(userLoc.getLatitude(), userLoc.getLongitude(), in.Latitude, in.Longitude);
							String distanceString = "";
							if (distance < 1)
								distanceString = "" + (int)(distance*1000) + " m away";
							else
								distanceString = "" + (int)(distance) + " km away";
							if (in.Address.trim().equals("") == false)
								distanceString += " - " + in.Address;
							////Log.d (TAG, "No exception " + distanceString);
							in4.setText (distanceString);
							////Log.d (TAG, "No exception " + distanceString);
						}
						else
						{
							in4.setVisibility (8); // Remove textview
							in3.setPadding(8, 0, 8, 6);
						}
					}
					else
					{
						in4.setVisibility (8); // Remove textview
						in3.setPadding(8, 0, 8, 6);
					}
				}
				catch (Exception e)
				{ }//in4.setVisibility (8);
			}
        }
        return v;
   }
    
    private double haversianDistance (double lat1, double lon1, double lat2, double lon2) {
		double piConst = Math.PI / 180;
		double R = 6371; // metres
		double dLat = (lat2-lat1) * piConst;
		double dLon = (lon2-lon1) * piConst; 
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		        Math.cos(lat1  * piConst) * Math.cos(lat2 * piConst) * 
		        Math.sin(dLon/2) * Math.sin(dLon/2); 
		a = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		return (Math.abs(R * a)); // Km
		
	}
}
