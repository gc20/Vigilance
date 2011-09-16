package com.level2.bhrastachar;

import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class ReportActivity extends Activity implements OnClickListener,
		Runnable {

	private static String TAG = ReportActivity.class.getSimpleName();
	private String url;

	// Views
	Button buttonSubmit;
	EditText titleEditText;
	EditText descriptionEditText;
	EditText bribeEditText;
	EditText offenderEditText;
	EditText departmentEditText;
	EditText emailEditText;
	EditText locationEditText;
	Button buttonSubmitFinal = null;

	// Location details
	Double latitude = 0.0;
	Double longitude = 0.0;
	String tempLocation;

	// Submission progress dialog
	ProgressDialog pd;
	
	// Refresh progress dialog
	ProgressDialog dialogRefresh;

	// Dialog for receiving email addresses
	Dialog dialog;

	// Thread for submission
	Thread thread;

	@SuppressWarnings("static-access")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.form);
		
		buttonSubmit = (Button) findViewById(R.id.buttonSubmit);
		buttonSubmit.setOnClickListener(this);
		
		titleEditText = (EditText) findViewById(R.id.titleEditText);
		descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
		bribeEditText = (EditText) findViewById(R.id.bribeEditText);
		offenderEditText = (EditText) findViewById(R.id.offenderEditText);
		departmentEditText = (EditText) findViewById(R.id.departmentEditText);
		locationEditText = (EditText) findViewById(R.id.locationEditText);

		url = ((MenuTabActivity) this.getParent()).url;

		// Submission thread
		thread = new Thread(this);

		// Submission progress dialog
		pd = new ProgressDialog(this);
		pd.setTitle("Submitting ...");
		pd.setMessage("Sending information ");
		pd.setIndeterminate(true);
		pd.setCancelable(false);

		// Setup email dialog
		dialog = new Dialog(this);
		dialog.setContentView(R.layout.email);
		dialog.setTitle("Enter Email Address (Optional)");
		dialog.setCancelable(true);
		buttonSubmitFinal = (Button) dialog.findViewById(R.id.buttonSubmitFinal);
		buttonSubmitFinal.setOnClickListener(this);
		emailEditText = (EditText) dialog.findViewById(R.id.emailEditText);
		
		// Show keyboard
		((InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE)).showSoftInput(titleEditText, 0); 
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "On Start Start");

		// Create alert message to display instructions
		String instructions = "Instructions:\n\nPlease populate the following corruption report thoroughly. The more information you provide, the more valuable will be your contribution.";

		AlertDialog.Builder builder = new AlertDialog.Builder(
				ReportActivity.this);
		builder.setMessage(instructions).setCancelable(false)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		AlertDialog alert = builder.create();
		alert.show();

		// Load pre-stored form values
		titleEditText
				.setText(((MenuTabActivity) this.getParent()).titleEditText);
		descriptionEditText
				.setText(((MenuTabActivity) this.getParent()).descriptionEditText);
		bribeEditText
				.setText(((MenuTabActivity) this.getParent()).bribeEditText);
		offenderEditText
				.setText(((MenuTabActivity) this.getParent()).offenderEditText);
		departmentEditText
				.setText(((MenuTabActivity) this.getParent()).departmentEditText);
		// emailEditText.setText(((MenuTabActivity)this.getParent()).emailEditText);
		locationEditText
					.setText(((MenuTabActivity) this.getParent()).locationEditText);
		Log.d(TAG, "On Start End");

		
		try {
			Thread locationThread = new Thread() {
				@Override
				public void run() {
					updateLocationDetails();
					Message tempMessage = new Message();
					tempMessage.arg1 = 2;
					handlerSubmit.sendMessage(tempMessage);
				}
			};
			locationThread.start();
		} catch (Exception e) {
			Log.d(TAG, "Location thread failed");
		}

	}

	@Override
	protected void onRestart() {

		super.onRestart();
		Log.d(TAG, "On Restart Start");
		updateUI();
		
		// Load pre-stored form values
		titleEditText
				.setText(((MenuTabActivity) this.getParent()).titleEditText);
		descriptionEditText
				.setText(((MenuTabActivity) this.getParent()).descriptionEditText);
		bribeEditText
				.setText(((MenuTabActivity) this.getParent()).bribeEditText);
		offenderEditText
				.setText(((MenuTabActivity) this.getParent()).offenderEditText);
		departmentEditText
				.setText(((MenuTabActivity) this.getParent()).departmentEditText);
		// emailEditText.setText(((MenuTabActivity)this.getParent()).emailEditText);

		// Get actual location if user's last entry was meaningless
		// updateLocationDetails();
		if (((MenuTabActivity) this.getParent()).locationEditText.equals(""))
			locationEditText
					.setText(((MenuTabActivity) this.getParent()).stringLocation);
		else
			locationEditText
					.setText(((MenuTabActivity) this.getParent()).locationEditText);

		Log.d(TAG, "On Restart End");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "On Restart Start");
		updateUI();
		
		// Load pre-stored form values
		titleEditText
				.setText(((MenuTabActivity) this.getParent()).titleEditText);
		descriptionEditText
				.setText(((MenuTabActivity) this.getParent()).descriptionEditText);
		bribeEditText
				.setText(((MenuTabActivity) this.getParent()).bribeEditText);
		offenderEditText
				.setText(((MenuTabActivity) this.getParent()).offenderEditText);
		departmentEditText
				.setText(((MenuTabActivity) this.getParent()).departmentEditText);
		// emailEditText.setText(((MenuTabActivity)this.getParent()).emailEditText);

		// Get actual location if user's last entry was meaningless
		// LocationDetails();
		if (((MenuTabActivity) this.getParent()).locationEditText.equals(""))
			locationEditText
					.setText(((MenuTabActivity) this.getParent()).stringLocation);
		else
			locationEditText
					.setText(((MenuTabActivity) this.getParent()).locationEditText);

		Log.d(TAG, "On Restart End");
	}

	@Override
	protected void onPause() {

		super.onPause();
		Log.d(TAG, "On Pause Start");

		// Store entries
		((MenuTabActivity) this.getParent()).titleEditText = titleEditText
				.getText().toString();
		((MenuTabActivity) this.getParent()).descriptionEditText = descriptionEditText
				.getText().toString();
		((MenuTabActivity) this.getParent()).bribeEditText = bribeEditText
				.getText().toString();
		((MenuTabActivity) this.getParent()).offenderEditText = offenderEditText
				.getText().toString();
		((MenuTabActivity) this.getParent()).departmentEditText = departmentEditText
				.getText().toString();
		// ((MenuTabActivity)this.getParent()).emailEditText =
		// emailEditText.getText().toString();
		((MenuTabActivity) this.getParent()).locationEditText = locationEditText
				.getText().toString();

		Log.d(TAG, "On Pause End");
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();

		// Store entries
		((MenuTabActivity) this.getParent()).titleEditText = titleEditText
				.getText().toString();
		((MenuTabActivity) this.getParent()).descriptionEditText = descriptionEditText
				.getText().toString();
		((MenuTabActivity) this.getParent()).bribeEditText = bribeEditText
				.getText().toString();
		((MenuTabActivity) this.getParent()).offenderEditText = offenderEditText
				.getText().toString();
		((MenuTabActivity) this.getParent()).departmentEditText = departmentEditText
				.getText().toString();
		// ((MenuTabActivity)this.getParent()).emailEditText =
		// emailEditText.getText().toString();
		((MenuTabActivity) this.getParent()).locationEditText = locationEditText
				.getText().toString();

		Log.d(TAG, "On Destroy");
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		// Set up email request
		case R.id.buttonSubmit:
			Log.d(TAG, "Button Submit");
			dialog.show();
			break;

		// Display progress dialog and submit information
		case R.id.buttonSubmitFinal:
			Log.d(TAG, "Button Submit Final");
			if (dialog != null && dialog.isShowing())
				dialog.dismiss();
			pd = ProgressDialog.show(this, "Submitting ...",
					"Sending information", true, false);
			Thread thread = new Thread(this);
			thread.start();
			break;

		default:
			break;
		}
	}
	
	/*
	 * private Handler handlerEmail = new Handler() {
	 * 
	 * @Override public void handleMessage(Message msg) {
	 * 
	 * Log.d(TAG, "Button Submit Final"); if (dialog!=null &&
	 * dialog.isShowing()) dialog.dismiss();
	 * 
	 * pd.show(); thread.start(); } };
	 */

	@Override
	public void run() {

		try {
			// Get latitude and longitude if needed
			try {
				if (!(locationEditText.getText().toString()
						.equals(((MenuTabActivity) this.getParent()).stringLocation))) {
					Log.d(TAG, "Trying to geocode");
					Geocoder geocoder = new Geocoder(this);
					List<Address> address = geocoder.getFromLocationName(
							locationEditText.getText().toString(), 1);
					latitude = address.get(0).getLatitude();
					longitude = address.get(0).getLongitude();
					Log.d(TAG, "Geocoded: " + latitude + ", " + longitude);
				}
				Log.d(TAG, "Did not need to geocode");
			} catch (Exception e) {
				Log.d(TAG, "Could not geocode");
			}

			// Create request
			String request = url
					+ "report.php?"
					+ "title="
					+ URLEncoder.encode(titleEditText.getText().toString(),
							"UTF-8")
					+ "&location="
					+ URLEncoder.encode(locationEditText.getText().toString(),
							"UTF-8")
					+ "&longitude="
					+ longitude
					+ "&latitude="
					+ latitude
					+ "&time="
					+ new Date().getTime()
					+ "&dept="
					+ URLEncoder.encode(
							departmentEditText.getText().toString(), "UTF-8")
					+ "&bribe="
					+ URLEncoder.encode(bribeEditText.getText().toString(),
							"UTF-8")
					+ "&message="
					+ URLEncoder.encode(descriptionEditText.getText()
							.toString(), "UTF-8")
					+ "&offender="
					+ URLEncoder.encode(offenderEditText.getText().toString(),
							"UTF-8") 
					+ "&email="
					+ URLEncoder.encode(emailEditText.getText().toString(), "UTF-8");
			Log.d(TAG, "Request: " + request);

			// Send request
			HttpClient hc = new DefaultHttpClient();
			HttpPost post;
			String response = "";
			Log.d(TAG, "Sending requset");
			post = new HttpPost(request);
			response = hc.execute(post).toString(); // xx check if valid
													// response was obtained
			Log.d(TAG, "Response: " + response);
			Log.d(TAG, "Sent Request.");

			// Clear pre-stored form values
			((MenuTabActivity) this.getParent()).titleEditText = "";
			((MenuTabActivity) this.getParent()).descriptionEditText = "";
			((MenuTabActivity) this.getParent()).bribeEditText = "";
			((MenuTabActivity) this.getParent()).offenderEditText = "";
			((MenuTabActivity) this.getParent()).departmentEditText = "";
			((MenuTabActivity)this.getParent()).emailEditText = "";
			((MenuTabActivity) this.getParent()).locationEditText = "";

			// Show confirmation
			if (pd != null && pd.isShowing())
				pd.dismiss();
			Message tempMessage = new Message();
			tempMessage.arg1 = 1;
			handlerSubmit.sendMessage(tempMessage);
		} catch (Exception e) {
			// Store entries
			((MenuTabActivity) this.getParent()).titleEditText = titleEditText
					.getText().toString();
			((MenuTabActivity) this.getParent()).descriptionEditText = descriptionEditText
					.getText().toString();
			((MenuTabActivity) this.getParent()).bribeEditText = bribeEditText
					.getText().toString();
			((MenuTabActivity) this.getParent()).offenderEditText = offenderEditText
					.getText().toString();
			((MenuTabActivity) this.getParent()).departmentEditText = departmentEditText
					.getText().toString();
			// ((MenuTabActivity)this.getParent()).emailEditText =
			// emailEditText.getText().toString();
			((MenuTabActivity) this.getParent()).locationEditText = locationEditText
					.getText().toString();

			// Dismiss confirmation
			if (pd != null && pd.isShowing())
				pd.dismiss();
			Message tempMessage = new Message();
			tempMessage.arg1 = 0;
			handlerSubmit.sendMessage(tempMessage);
			Log.d(TAG, "Error submitting form / sending its data to server");
		}
	}

	private Handler handlerSubmit = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			if (msg.arg1 == 2)
			{
				if (locationEditText.getText().toString().equals(""))
					locationEditText.setText(tempLocation);
			}
			
			// pd.dismiss();
			if (msg.arg1 == 1) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ReportActivity.this);
				builder.setMessage("Your form has been submitted. Thank you!")
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				AlertDialog alert = builder.create();
				alert.show();

				// Switch to map view
				// ((MenuTabActivity)
				// this.getParent()).tabHost.setCurrentTab(0);
			}
			if (msg.arg1 == 0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						ReportActivity.this);
				builder.setMessage(
						"We apologize. Your report could not be submitted due to network issues. Please try again later.")
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int id) {
										dialog.cancel();
									}
								});
				AlertDialog alert = builder.create();
				alert.show();
			}
		}
	};

	// Get latest location
	public void updateLocationDetails() {
		try {
			Log.d(TAG, "Entered updateLocationDetails");
			// Get coordinates
			LocationManager locationManager = ((MenuTabActivity) this
					.getParent()).locationManager;
			LocationListener locationListener = ((MenuTabActivity) this
					.getParent()).locationListener;
			locationManager.requestLocationUpdates(
					locationManager.getBestProvider(new Criteria(), true), 0,
					0, locationListener);
			Location userLoc = locationManager
					.getLastKnownLocation(locationManager.getBestProvider(
							new Criteria(), true));

			Log.d(TAG, "Before interpreting reverse geocoding");
			// Reverse geocode
			if (userLoc != null) {
				latitude = userLoc.getLatitude();
				longitude = userLoc.getLongitude();
				Log.d(TAG, "Lat: " + latitude + " Lon: " + longitude);
				Geocoder geocoder = new Geocoder(this);
				List<Address> address = geocoder.getFromLocation(
						userLoc.getLatitude(), userLoc.getLongitude(), 1);
				tempLocation = address.get(0).getAddressLine(0);
				if (address.get(0).getAdminArea()!=null)
					tempLocation += ", " + address.get(0).getAdminArea();
				if (address.get(0).getCountryName()!=null)
					tempLocation += ", " + address.get(0).getCountryName();
				if (address.get(0).getPostalCode()!=null)
					tempLocation += ", " + address.get(0).getPostalCode();
				((MenuTabActivity) this.getParent()).stringLocation = tempLocation;
				Log.d(TAG, "Location: " + tempLocation);
			} else {
				Log.d(TAG, "No location available");
			}

		} catch (Exception e) {
			Log.d(TAG, "Location could not be retrieved.");
		}
	}

	protected void updateUI (){
	}

	protected void displayprogressUI (){
		dialogRefresh = ProgressDialog.show(this, "", "Loading. Please wait...", false);
		dialogRefresh.show();
	}
	
	protected void dismissprogressUI (){
		dialogRefresh.dismiss();
	}
	
}