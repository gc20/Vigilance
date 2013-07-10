/*
 * Copyright (C) 2009 Huan Erdao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hacknroll.crimewatch.map;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Point;
import android.graphics.Typeface;
import android.view.GestureDetector;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import com.hacknroll.crimewatch.map.GeoClusterer.GeoCluster;

/**
 * Overlay extended class to display Clustered Marker.
 * @author Huan Erdao
 */
public class ClusterMarker extends Overlay {

	/** cluster object */
	protected final GeoCluster cluster_;
	/** screen density for multi-resolution
	 *	get from contenxt.getResources().getDisplayMetrics().density;  */
	protected float screenDensity_ = 1.0f;
	
	protected final float TXTSIZE = 16.0f;
	
	/** Paint object for drawing icon */
	protected final Paint paint_;
	/** List of GeoItems within */
	protected final List<GeoItem> GeoItems_;
	/** center of the cluster */
	protected final GeoPoint center_;
	/** Bitmap objects for icons */
	protected final List<MarkerBitmap> markerIconBmps_;
	/** icon marker type */
	protected int markerTypes = 0;
	/** select state for cluster */
	protected boolean isSelected_ = false;
	/** selected item number in GeoItem List */
	protected int selItem_;
	/** Text Offset  */
	protected int txtHeightOffset_;
	/** grid size for clustering(dip). */
	protected int GRIDSIZE = 53;
    final int GridSizePx = (int) (GRIDSIZE * screenDensity_ + 0.5f);
	/** multiple alert dialog display check for the mapView. */
	public boolean alertDialogCheck [];
	/** semaphore to prevent multiple alert dialogs. */
	//public Semaphore sem;
	/** Context of the map view **/
	Context mapContext;
	/** Current map view including controller **/
	MapView mapView_;
	/* Gesture detection variable */
	GestureDetector gestureDetector;

	
	/**
	 * @param cluster a cluster to be rendered for this marker
	 * @param markerIconBmps icon set for marker
	 */
	public ClusterMarker(GeoCluster cluster, List<MarkerBitmap> markerIconBmps, float screenDensity, boolean alertDialogCheck [], Context mapContext, MapView mapView_) {
		cluster_ = cluster;
		markerIconBmps_ = markerIconBmps;
		center_ = cluster_.getLocation();
		GeoItems_ = cluster_.getItems();
		screenDensity_ = screenDensity;
		paint_ = new Paint();
		paint_.setStyle(Paint.Style.STROKE);
		paint_.setAntiAlias(true);
		paint_.setColor(Color.WHITE);
		paint_.setTextSize(TXTSIZE*screenDensity_);
		paint_.setTextAlign(Paint.Align.CENTER);
		paint_.setTypeface(Typeface.DEFAULT_BOLD);
		FontMetrics metrics = paint_.getFontMetrics();
		txtHeightOffset_ = (int)((metrics.bottom+metrics.ascent)/2.0f);
		/* check if we have selected item in cluster */
		selItem_ = 0;
		for(int i=0; i<GeoItems_.size(); i++) {
			if(GeoItems_.get(i).isSelected()) {
				selItem_ = i;
				isSelected_ = true;
			}
		}
		setMarkerBitmap();
		this.alertDialogCheck = alertDialogCheck;
		//this.sem = sem;
		this.mapContext = mapContext;
		this.mapView_ = mapView_;
		
		// Gesture detection
	    //gestureDetector = new GestureDetector(mapContext, new GestureListener());

	}
	
	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {
		
		Projection proj = mapView.getProjection();
		Point ptCenter = proj.toPixels(p,null);
		Point pos = proj.toPixels(center_, null);
		//Log.d ("TAG OnTap", p.getLatitudeE6()/1e6 + " " + p.getLongitudeE6()/1e6);
		//Log.d ("TAG onTap", pos.x + " " + pos.y);
		
		if(pos.x >= ptCenter.x - GridSizePx && pos.x <= ptCenter.x + GridSizePx &&
				  pos.y >= ptCenter.y - GridSizePx && pos.y <= ptCenter.y + GridSizePx) {
			
			//Log.d ("TAG", "TAPPPPPED " + p.getLatitudeE6()/1e6 + " " + p.getLongitudeE6()/1e6 + " " + GeoItems_.size() + " " + center_.getLatitudeE6()/1e6 + " " + center_.getLongitudeE6()/1e6);
			String message = "";
			AlertDialog.Builder builder = new AlertDialog.Builder(mapView.getContext());
			
			// If only one marker exists or if zoom level is maximum
			if (GeoItems_.size() == 1 || mapView_.getZoomLevel() == 19) {
				
				builder.setTitle("Incident Details");
				// Add pop-up snippet
				message = "Crime Name:\n" + GeoItems_.get(0).incidentDetails.Crime_Name;
				if (!GeoItems_.get(0).incidentDetails.Address.equals(""))
					message += "\n\nLocation:\n" + GeoItems_.get(0).incidentDetails.Address;
				if (!GeoItems_.get(0).incidentDetails.City.equals(""))
					message += "\n\nCity:\n " + GeoItems_.get(0).incidentDetails.City;
				if (!GeoItems_.get(0).incidentDetails.Description.equals(""))
					message += "\n\nDescription:\n" + GeoItems_.get(0).incidentDetails.Description;
			
				// Display pop-up
				builder.setMessage(message)
			    .setCancelable(false).setPositiveButton("Close", 
					new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   dialog.cancel();
			        	   alertDialogCheck[0] = false;
			           }
			       });
				try {
					//sem.acquire();
					AlertDialog alertDialog = builder.create();
					//Log.d ("TAG", "Alert Dialog Check: " + alertDialogCheck[0]);
					if (alertDialogCheck[0] == false) {
						alertDialogCheck[0] = true;
						alertDialog.show();
					}
					//sem.release();
				}
				catch (Exception e) {
					//Log.d ("TAG", "Dialog display exception");
				}
			}
			else if (GeoItems_.size() > 1) {
				
				message = GeoItems_.size() + " markers were selected. Please select individual marker to see specific incident data.";
				Toast.makeText(mapContext, message, Toast.LENGTH_SHORT).show();
				
				//mapView_.getController().zoomIn();
				//mapView_.getController().animateTo(p);
				
				// Zoom to appropriate position
				int minLat = GeoItems_.get(0).getLocation().getLatitudeE6();
				int minLon = GeoItems_.get(0).getLocation().getLongitudeE6();
				int maxLat = GeoItems_.get(0).getLocation().getLatitudeE6();
				int maxLon = GeoItems_.get(0).getLocation().getLongitudeE6();
				int geoRead;
				for (int i=1; i<GeoItems_.size(); i++) {
					
					// Latitude
					geoRead = GeoItems_.get(i).getLocation().getLatitudeE6();
					if (geoRead < minLat)
						minLat = geoRead;
					if (geoRead > maxLat)
						maxLat = geoRead;
					
					// Longitude
					geoRead = GeoItems_.get(i).getLocation().getLongitudeE6();
					if (geoRead < minLon)
						minLon = geoRead;
					if (geoRead > maxLon)
						maxLon = geoRead;
				}
			
				// Zoom to required position
				mapView_.getController().animateTo(new GeoPoint((minLat+maxLat)/2, (minLon+maxLon)/2));
				mapView_.getController().zoomToSpan((maxLat-minLat), (maxLon-minLon));
				mapView_.getController().zoomOut();
			}
		}
		return super.onTap(p, mapView);
	}
	
	/*
	// Handles touch events
    @Override
	public boolean onTouchEvent(MotionEvent e, MapView mapView) {
		return gestureDetector.onTouchEvent(e);
	}
	
    // Override class to detect double tap events
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

		// event when double tap occurs
	    @Override
	    public boolean onDoubleTap(MotionEvent e) {
	    	Log.d("Double Tap", "Tapped at: (" + e.getX() + "," + e.getY() + ")");
	        handleDoubleTap(e);
	        return true;
	    }
	}
	
    // Manages double tap events
    public void handleDoubleTap (MotionEvent e) {
    	
    	GeoPoint p = mapView_.getProjection().fromPixels((int) e.getX(),
                (int) e.getY());
    	Projection proj = mapView_.getProjection();
		Point ptCenter = proj.toPixels(p,null);
		Point pos = proj.toPixels(center_, null);
		Log.d ("handleDoubleTap", pos.x + " " + pos.y);
		
		if(pos.x >= ptCenter.x - GridSizePx && pos.x <= ptCenter.x + GridSizePx &&
				  pos.y >= ptCenter.y - GridSizePx && pos.y <= ptCenter.y + GridSizePx) {
			
			Log.d ("handleDoubleTap", "TAPPPPPED " + p.getLatitudeE6()/1e6 + " " + p.getLongitudeE6()/1e6 + " " + GeoItems_.size() + " " + center_.getLatitudeE6()/1e6 + " " + center_.getLongitudeE6()/1e6);
		}
    }*/
    
	/**
	 * change icon bitmaps according to the state.
	 */
	protected void setMarkerBitmap(){
		markerTypes = -1;
		for(int i = 0; i < markerIconBmps_.size(); i++ ){
			if( GeoItems_.size() < markerIconBmps_.get(i).getItemMax() ){
				markerTypes = i;
				paint_.setTextSize(markerIconBmps_.get(markerTypes).getTextSize()*screenDensity_);
				FontMetrics metrics = paint_.getFontMetrics();
				txtHeightOffset_ = (int)((metrics.bottom+metrics.ascent)/2.0f);
				break;
			}
		}
		if(markerTypes<0)
			markerTypes = markerIconBmps_.size()-1;
	}

	/**
	 * draw icon.
	 * @param canvas Canvas object.
	 * @param mapView MapView object.
	 * @param shadow shadow flag.
	 */
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		cluster_.onNotifyDrawFromMarker();
		Projection proj = mapView.getProjection();
		Point p = proj.toPixels(center_, null);
		if( p.x < 0 || p.x > mapView.getWidth() || p.y < 0 || p.y > mapView.getHeight() )
			return;
		MarkerBitmap mkrBmp = markerIconBmps_.get(markerTypes);
		Bitmap bmp = isSelected_ ? mkrBmp.getBitmapSelect() : mkrBmp.getBitmapNormal();
		Point grid = mkrBmp.getGrid();
		Point gridReal = new Point((int)(grid.x*screenDensity_+0.5f),(int)(grid.y*screenDensity_+0.5f));
		canvas.drawBitmap(bmp, p.x-gridReal.x, p.y-gridReal.y, paint_);
		String caption = String.valueOf(GeoItems_.size());
		int x = p.x;
		int y = p.y-txtHeightOffset_;
		canvas.drawText(caption,x,y,paint_);
	}
	
	/**
	 * check if the marker is selected.
	 * @return true if selected state.
	 */
	public boolean isSelected(){
		//Log.d ("TAG" , "is selected!");
		return isSelected_;
	}
	
	/**
	 * clears selected state.
	 */
	public void clearSelect(){
		isSelected_ = false;
		if(selItem_<GeoItems_.size()){
			GeoItems_.get(selItem_).setSelect(false);
		}
		setMarkerBitmap();
	}

	/**
	 * get center location of the marker.
	 * @return GeoPoint object of current marker center.
	 */
	public GeoPoint getLocation(){
		return center_;
	}

	/**
	 * get selected item's location. null if nothing is selected.
	 * @return GeoPoint object for selected item. null if nothing selected.
	 */
	public GeoPoint getSelectedItemLocation(){
		//Log.d ("TAG" , "getSelectedItemLocation");
		if(selItem_<GeoItems_.size()){
			return GeoItems_.get(selItem_).getLocation();
		}
		return null;
	}
	
}
