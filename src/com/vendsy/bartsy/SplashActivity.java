package com.vendsy.bartsy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vendsy.bartsy.model.Order;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.model.Venue;
import com.vendsy.bartsy.utils.WebServices;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Base activity launched at startup. Displays splash screen, get details from the host and decides what activity to 
 * launch based on the saved preferences.
 * 
 * @author PeterKellis
 *
 */

public class SplashActivity extends Activity {

	private final static String TAG = "SplashActivity";
	private Handler handler = new Handler();
	BartsyApplication mApp = null;
	SplashActivity mActivity = null;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		// Common init
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate()");

		setContentView(R.layout.splash_screen);

		// Setup application pointer
		mApp = (BartsyApplication) getApplication();
		mActivity = this;

		// If the user profile is not set, start the init activity
		if (mApp.mProfile == null) {
			Log.e(TAG, "No saved profile found - load init activity");
			
			Intent intent = new Intent().setClass(this, NDAActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			return;
		} 

		Log.e(TAG, "Previously saved profiled found, try to log in: " + mApp.mProfile);
		
		// We have saved profile information from preferences. Get latest profile info from host and also get user status
		new AsyncLoadXMLFeed().execute();
	}

	private class AsyncLoadXMLFeed extends AsyncTask<Void, Void, Void>{
		@Override
		protected void onPreExecute(){
			         // show your progress dialog
		}
		
		@Override
		protected Void doInBackground(Void... Voids) {
		    // load your xml feed asynchronously
		  
			
			UserProfile user = WebServices.getUserProfile(getApplicationContext(), mApp.mProfile);
			if (user == null) {
				// Could not get user details - erase our user locally and of course, don't check anybody in
				Log.v(TAG, "Could not load user profile");
				mApp.eraseUserProfile();
				return null;
			} else {
				// Got a valid profile - save it locally 
				Log.v(TAG, "Found profile: " + user);
				mApp.saveUserProfile(user);
			}
			
			// Sync user details
			
			Venue venue = WebServices.syncUserDetails(mApp, user);
		
			// If venue found - set it up as the active venue
			if (venue != null) {
				Log.v(TAG, "Active venue found: " + venue.getName());
				mApp.userCheckIn(venue);
				
			} else {
				// 
				Log.v(TAG, "Active venue not found");
				mApp.userCheckOut();
			}
			
			
			// Finally, load active orders
			loadUserOrders();

			return null;
		}
		
		@Override
		protected void onPostExecute(Void params){
			// dismiss your dialog
			// launch your News activity
			if (mApp.mProfile != null)
				Toast.makeText(mActivity, "Logged in as " + mApp.mProfile.getNickname(), Toast.LENGTH_SHORT).show();
			Intent intent = new Intent().setClass(SplashActivity.this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}
	}
	
	
	/**
	 * To get user orders from the server
	 */
	private void loadUserOrders() {
		// Service call for get menu list in background
		new Thread() {

			public void run() {
				String response = WebServices.getUserOrdersList(mApp);

				Log.v(TAG, "orders " + response);

				userOrdersResponseHandling(response);
			};

		}.start();
	}

	
	/**
	 * User orders web service Response handling
	 * 
	 * @param response
	 */

	private void userOrdersResponseHandling(String response) {
		if (response != null) {
			try {
				JSONObject orders = new JSONObject(response);
				JSONArray listOfOrders = orders.has("orders") ? orders
						.getJSONArray("orders") : null;
				if (listOfOrders != null) {

					// Start by clearning orders as there is a new loggin
					mApp.clearOrders();
					
					for (int i = 0; i < listOfOrders.length(); i++) {
						JSONObject orderJson = (JSONObject) listOfOrders.get(i);
						Order order = new Order(orderJson);
						mApp.addOrderNoUI(order);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

}
