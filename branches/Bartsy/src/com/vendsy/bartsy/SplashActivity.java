package com.vendsy.bartsy;

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
			
			Intent intent = new Intent().setClass(this, InitActivity.class);
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
}
