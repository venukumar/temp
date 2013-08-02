package com.vendsy.bartsy;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.model.Venue;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.Utilities;
import com.vendsy.bartsy.utils.WebServices;
import com.vendsy.bartsy.utils.WifiConfigManager;

public class MainActivity extends SherlockFragmentActivity implements OnClickListener {

	private static final String TAG = "MainActivity";
	
	private Handler handler = new Handler();
	BartsyApplication mApp = null;
	MainActivity mActivity = null;
	private static final int REQUEST_CODE_USER_PROFILE = 9001;
	private ProgressDialog mProgressDialog;

	private WifiManager wifiManager;



	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup application pointer
		mApp = (BartsyApplication) getApplication();
		mActivity = this;
		
		wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
		
		
		setContentView(R.layout.main);
		

		if (mApp.loadActiveVenue() == null) {
			
			// No active venue - hide active menu UI
			findViewById(R.id.button_active_venue).setVisibility(View.GONE);
			
		} else {
			// Active venue exists - set up the active venue view. For now just show it

			findViewById(R.id.button_active_venue).setVisibility(View.VISIBLE);

			// Set up checkout button
			Button b = (Button) findViewById(R.id.button_active_venue);

			// Setup text for the view
			b.setText(mApp.mActiveVenue.getName() + "\n" +
					  mApp.mActiveVenue.getUserCount() + (mApp.mActiveVenue.getUserCount() == 1 ? " person, " : " people, ") + 
					  mApp.getOrderCount() + " orders");
		}

		// Set up button listeners
		
		((Button) findViewById(R.id.button_checkin)).setOnClickListener(this);
		((Button) findViewById(R.id.button_my_profile)).setOnClickListener(this);
		((View) findViewById(R.id.button_active_venue)).setOnClickListener(this);
		((View) findViewById(R.id.button_notifications)).setOnClickListener(this);
		((View) findViewById(R.id.rewardsButton)).setOnClickListener(this);
		((View) findViewById(R.id.button_logout)).setOnClickListener(this);
		
		// Hide action bar
		getSupportActionBar().hide();
		
		
		// Check and set development environment display
		if (WebServices.DOMAIN_NAME.equalsIgnoreCase("http://54.235.76.180:8080/") && 
				WebServices.SENDER_ID.equalsIgnoreCase("605229245886")) 
			((TextView) findViewById(R.id.view_main_deployment_environment)).setText("Server: DEV");
		else if (WebServices.DOMAIN_NAME.equalsIgnoreCase("http://app.bartsy.vendsy.com/") && 
				WebServices.SENDER_ID.equalsIgnoreCase("560663323691")) 
			((TextView) findViewById(R.id.view_main_deployment_environment)).setText("Server: PROD");
		else 
			((TextView) findViewById(R.id.view_main_deployment_environment)).setText("** INCONSISTENT DEPLOYMENT **");
		
		
		checkNetworkAvailability();
	}

	/**
	 * Check the wifi enabled or not and scan the nearest wifi names.
	 */
	private void checkNetworkAvailability() {
		
		if(wifiManager==null) return;
		
					
		final ProgressDialog progressDialog = Utilities.progressDialog(this, "Fixing the wifi..");
		progressDialog.show();

		new Thread(){
			public void run() {
				
				boolean networkAvailable =false;
				try {
					networkAvailable = WebServices.isNetworkAvailable(MainActivity.this);
				} catch (Exception e) {}
				
				// if the network is not available then try to turn on wifi
				if(!networkAvailable){
					WifiConfigManager.enableWifi(wifiManager);
				}
				// Check the network is available or not
				try {
					networkAvailable = WebServices.isNetworkAvailable(MainActivity.this);
				} catch (Exception e) {}
				// If the network is not available then scan nearest wifi names
				if(!networkAvailable){
					searchAvailableWifi();
				}
				
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						progressDialog.dismiss();
					}
				});
			}
		}.start();
		
	}
	
	/***
	 * 
	 * Scans List of Available Wifi Networks 
	 * 
	 **/
	public void searchAvailableWifi() {
		List<ScanResult> mScanResults = wifiManager.getScanResults();
		ScanResult bestResult = null;
		Venue bestVenue = null;
		
		if(mScanResults != null && mScanResults.size()>0){
			// Get the venues from the shared preference
			String response = Utilities.loadPref(mActivity, Venue.Shared_Pref_KEY,"");
			
			if(response.equals("")){
				return;
			}
			ArrayList<Venue> venues = Utilities.getVenueListResponse(response);
			
			// Try to find the best wifi network based on Signal Level which is available in the venues
			for(Venue venue:venues){
				
				if(!venue.hasWifi()) continue;
				
				for(ScanResult results : mScanResults){
					Log.d("Available Networks", results.SSID);
					
					if((bestResult == null && results.SSID.equals(venue.getWifiName())) || WifiManager.compareSignalLevel(bestResult.level, results.level) < 0){
						bestResult = results;
						bestVenue = venue;
					}
				}
			}
			// Enable the wifi network of the best venue
			if(bestVenue != null){
				String networkType = bestVenue.getWifiNetworkType();
				if(bestVenue.getWifiPassword()==null || bestVenue.getWifiPassword().equals("")){
					networkType = "nopass";
				}
				WifiConfigManager.configure(wifiManager, bestVenue.getWifiName(), bestVenue.getWifiPassword(), networkType);
			}
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		Log.v(TAG, "onStop()");
		
		// Save active venue
		mApp.saveActiveVenue();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.v(TAG, "onDestroy()");

	}
	
	
	@Override
	public void onClick(View v) {
		Log.d("Bartsy", "Clicked on a button");

		Intent intent;

		switch (v.getId()) {


		case R.id.button_active_venue:

			intent = new Intent().setClass(this, VenueActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			break;
		case R.id.button_logout:
			mApp.eraseUserProfile();
			finish();
			intent = new Intent().setClass(this, InitActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			break;
		case R.id.button_checkin:
			intent = new Intent().setClass(this, MapActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			break;
		case R.id.button_notifications:
			intent = new Intent().setClass(this, NotificationsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			break;
		case R.id.rewardsButton:
			intent = new Intent().setClass(this, RewardsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			break;
		case R.id.button_my_profile:
			
			Log.v(TAG, "User profile button");

			if (mProgressDialog != null && mProgressDialog.isShowing())
				return;
						
			mProgressDialog = Utilities.progressDialog(this, "Loading..");
			mProgressDialog.show();

			
			if (mApp.mProfile != null) {

				// We have saved profile information from preferences. Get latest profile info from host and also get user status
				new downloadUserProfile().execute();
				return;
			} 
			
			UserProfileActivity.setInput(mApp, null);
			intent = new Intent(getBaseContext(), UserProfileActivity.class);
			this.startActivityForResult(intent, REQUEST_CODE_USER_PROFILE);
			break;
		}
		

	}

	
	
	private class downloadUserProfile extends AsyncTask<Void, Void, Void>{
		@Override
		protected void onPreExecute(){
			         // show your progress dialog
		}
		
		@Override
		protected Void doInBackground(Void... Voids) {
		    // load your xml feed asynchronously
		  
			
			UserProfile user = WebServices.getUserProfile(mApp, mApp.mProfile);
			if (user == null) {
				// Could not get user details 
//				mApp.eraseUserProfile();
			} else {
				// Got a valid profile - save it locally 
				mApp.saveUserProfile(user);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void params){
			if (mApp.mProfile == null) {
				Toast.makeText(mActivity, "Could not load profile. Check your internet connection.", Toast.LENGTH_SHORT).show();	
				mProgressDialog.dismiss();
			} else {
				Toast.makeText(mActivity, "Loaded profile", Toast.LENGTH_SHORT).show();
				UserProfileActivity.setInput(mApp, mApp.mProfile);
				Intent intent = new Intent(getBaseContext(), UserProfileActivity.class);
				startActivityForResult(intent, REQUEST_CODE_USER_PROFILE);
			}
		}
	}

	/**
	 * To checkout user from the active venue
	 */
	private void checkOutUser() {
		// For now it will ask confirmation dialog
		if (mApp.mActiveVenue != null && mApp.getOrderCount() > 0) {
			alertBox("You have open orders placed at "
					+ mApp.mActiveVenue.getName()
					+ ". If you checkout they will be cancelled and you will still be charged for it.Do you want to checkout from "
					+ mApp.mActiveVenue.getName() + "?");
		} else if (mApp.mActiveVenue != null) {

			alertBox("Do you want to checkout from "
					+ mApp.mActiveVenue.getName() + "?");

		}

	}


	/**
	 * To display alert box when the user check out from the active venue
	 */
	private void alertBox(String message) {

		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setCancelable(true);
		builder.setTitle("Please Confirm !");
		builder.setInverseBackgroundForced(true);
		builder.setMessage(message);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// To check null condition (Error handling)
				if (mApp.mActiveVenue != null) {
					// Service call in the background
					new Thread() {
						public void run() {
							// Check out web service call
							String response = WebServices.userCheckInOrOut(
									mApp,
									mApp.loadBartsyId(),
									mApp.mActiveVenue.getId(),
									WebServices.URL_USER_CHECK_OUT);
							if (response != null) {
								System.out.println("response  ::: " + response);
								// To parse check out web service response
								try {
									JSONObject result = new JSONObject(response);
									String errorCode = result
											.getString("errorCode");

									// For now don't handle exceptions
									// locally...
									if (errorCode.equalsIgnoreCase("0")) {
										// No errors
									} else {
										// Errors
									}

									// Check out user locally regardless of
									// server status
									handler.post(new Runnable() {
										@Override
										public void run() {
											mApp.userCheckOut();
											findViewById(R.id.button_active_venue).setVisibility(View.GONE);
										}
									});
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						};
					}.start();
				}
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// To close the alert dialog
				dialog.dismiss();
			}
		});
		// To display alert dialog
		AlertDialog alert = builder.create();
		alert.show();

	}

	
	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent data) {
		
		super.onActivityResult(requestCode, responseCode, data);


		Log.v(TAG, "Activity result for request: " + requestCode + " with response: " + responseCode);

		switch (requestCode) {
		case REQUEST_CODE_USER_PROFILE:
			
			// Stop showing the dialog that we launched before calling the user profile activity
			if (mProgressDialog != null && mProgressDialog.isShowing())
				mProgressDialog.dismiss();

			switch (responseCode) {
			case RESULT_OK:
				// We got a response from the user profile activity. Process the user profile and start the right activity if successful
				Log.v(TAG, "Profile saved - process results");
				Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
				break;
			default:
				// No profile was created 
				Log.v(TAG, "Profile not saved");
				Toast.makeText(this, "Profile could not be saved to the server. Please try again", Toast.LENGTH_LONG).show();
				break;
			}

			break;
		}
		
	}
}
