package com.vendsy.bartsy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.SherlockActivity;
import com.vendsy.bartsy.model.Order;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.model.Venue;
import com.vendsy.bartsy.utils.Utilities;
import com.vendsy.bartsy.utils.WebServices;

import android.app.Activity;
import android.app.ProgressDialog;
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

public class SplashActivity extends SherlockActivity {

	private final static String TAG = "SplashActivity";
	private Handler handler = new Handler();
	BartsyApplication mApp = null;
	SplashActivity mActivity = null;
	private ProgressDialog mProgressDialog;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		// Common init
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate()");

		setContentView(R.layout.splash_screen);

		// Setup application pointer
		mApp = (BartsyApplication) getApplication();
		mActivity = this;

		// Display progress dialog
		mProgressDialog = Utilities.progressDialog(this, "Loading..");
		mProgressDialog.show();

		// If the user profile is not set (it would have been set by the call to loadUserProfile() in the application object,
		// which is guaranteed to have loaded before any activity or service), then start the init activity
		if (mApp.mProfile == null) {
			Log.e(TAG, "No saved profile found - load init activity");
			new Thread(){
				public void run() {
					mApp.loadServerKey();
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							//Stop loading
							mProgressDialog.dismiss();
							// Display NDA activity
							Intent intent = new Intent().setClass(SplashActivity.this, NDAActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();							
						}
					});
				}
			}.start();
			
		}else{ 

			Log.e(TAG, "Previously saved profiled found, try to log in: " + mApp.mProfile);
			
			// We have saved profile information from preferences. Get latest profile info from host and also get user status
			new Synchronize().execute();
		}
	}

	private class Synchronize extends AsyncTask<Void, Void, Void>{
		@Override
		protected void onPreExecute(){
		}
		
		@Override
		protected Void doInBackground(Void... Voids) {
			
			// Synchronize with server
			mApp.syncAppWithServer("Synchonizing on startup...");
			return null;
		}
		
		@Override
		protected void onPostExecute(Void params){
			mProgressDialog.dismiss();
			if (mApp.mProfile != null)
				Toast.makeText(mActivity, "Logged in as " + mApp.mProfile.getNickname(), Toast.LENGTH_SHORT).show();
			Intent intent = new Intent().setClass(SplashActivity.this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		}
	}

}
