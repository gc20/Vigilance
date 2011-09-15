package com.level2.bhrastachar;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapsOverlayDraw extends ItemizedOverlay<OverlayItem>{
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	Context context;
	
	// Constructor
	public MapsOverlayDraw(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		this.context = context;
	}

	
	// Add overlay
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	
	
	// Retrieve each overlay item
	@Override
	protected OverlayItem createItem(int i) {
		  return mOverlays.get(i);
	}
	
	// Size of overlay array
	@Override
	public int size() {
	  return mOverlays.size();
	}

	
	// When user taps on item
	@Override
	protected boolean onTap(int index) {
	
		if (mOverlays.size()==0)
			return false;
		
		  OverlayItem item = mOverlays.get(index);
		  AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		  dialog.setTitle(item.getTitle());
		  dialog.setMessage(item.getSnippet());
		  dialog.show();
		  return true;
	}


	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, false); // true/false depends on whether shadow is desired
	}
	
	
	
}
