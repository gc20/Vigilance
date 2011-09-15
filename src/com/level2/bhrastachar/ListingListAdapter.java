package com.level2.bhrastachar;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ListingListAdapter extends ArrayAdapter<Incident> {
	
	private ArrayList <Incident> IncidentList;
	/*private boolean userLocationAvailable;
	private double userLoc [];*/
	LocationManager locationManager;
	LocationListener locationListener;
	
    public ListingListAdapter(Context context, int textViewResourceId, ArrayList<Incident> IncidentList, LocationManager locationManager, LocationListener locationListener) {
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
        
        Incident in = IncidentList.get(position);
        //int categoryNum = 0; // Test
        if (in != null) {
			
        	TextView in1 = (TextView) v.findViewById(R.id.listingList1);
			TextView in2 = (TextView) v.findViewById(R.id.listingList2);
			TextView in3 = (TextView) v.findViewById(R.id.listingList3);
			TextView in4 = (TextView) v.findViewById(R.id.listingList4);
			
			// Incident title is displayed
			if (in1 != null) {
				String tempString = in.getTitle();
				if (tempString.length() > 37)
					tempString = tempString.substring(0, 38) + "...";
				if (tempString.equals(""))
					tempString = "Untitled";
				in1.setText(tempString);
			}
			/*if (in2 != null) {
				
				categoryNum = in.getCategory();
				switch (categoryNum){ // xx Populate with category
					case 1: in2.setText("Education"); break;
					case 2: in2.setText("Traffic"); break;
					case 3: in2.setText("Public Utilities"); break;
					default: in2.setText("Unavailable"); break;
				}
			}*/
			
			// Incident time difference from present time
			if  (in2 != null) {
				try {
					String timeText = "";
					long timeDiff = (new Date ().getTime() - in.getReportTime())/1000;
					if (timeDiff < 60) {
						timeText = timeDiff + " secs";
					}
					else {
						if (timeDiff < 3600) {
							timeText = (timeDiff/60) + " mins";
						}
						else {
							if (timeDiff < 86400) {
								timeText = (timeDiff/3600) + " hour";
								if (!(timeDiff/3600 == 1))
									timeText += "s";
							}
							else {
								timeText = (timeDiff/86400) + " day";
								if (!(timeDiff/86400 == 1))
									timeText += "s";
							}
						}
					}
					in2.setText(timeText);
				}
				catch (Exception e)
				{ in2.setText ("");}
			}
			
			// Incident description
			if (in3 != null) {
				in3.setText (in.getMessage());
			}
						
			// Location and distance from incident
			if (in4 != null) {
				try
				{
					locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);		
					Location userLoc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					if (userLoc != null)
					{
						if (!(in.getLatitude()==0.0 && in.getLongitude()==0.0))
						{
							double distance = haversianDistance(userLoc.getLatitude(), userLoc.getLongitude(), in.getLatitude(), in.getLongitude());
							String distanceString = "";
							if (distance < 1)
								distanceString = "" + (int)(distance*1000) + " m away";
							else
								distanceString = "" + (int)(distance) + " km away";
							if (in.getLocation().trim().equals("") == false)
								distanceString += " - " + in.getLocation();
							in4.setText (distanceString);
						}
						else
							in4.setVisibility (8); // Remove textview
					}
					else
						in4.setVisibility (8); // Remove textview
				}
				catch (Exception e)
				{ in4.setVisibility (8); }
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
