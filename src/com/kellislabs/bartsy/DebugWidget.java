/*
 * Copyright 2011, Qualcomm Innovation Center, Inc.
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.kellislabs.bartsy;

import org.json.JSONException;
import org.json.JSONObject;

import wifi.AllJoynHostActivity;
import wifi.AllJoynUseActivity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.kellislabs.bartsy.utils.Utilities;

public class DebugWidget extends TabActivity {
	private static final String TAG = "Bartsy";
	
//	Intent intent;

	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("in broadcast receiver:::::::");
			String newMessage = intent.getExtras().getString(
					Utilities.EXTRA_MESSAGE);
			System.out.println("the message is ::::" + newMessage);
			
			processPushNotification(newMessage);
			
			// mDisplay.append(newMessage + "\n");

		}

	};
	
	private void processPushNotification(String message) {
		try {
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
			JSONObject json = new JSONObject(message);
			if(json.has("messageType")){
				if(json.getString("messageType").equals("placeOrder")){
//					PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
					
//					Order order = new Order(json);
//					mBartenderFragment.addOrder(order);
//
//					updateOrdersCount();
				}
			}
			json.getString("");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alljoyn_service_main);

		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		

		Intent intent = new Intent().setClass(this, AllJoynUseActivity.class);
		spec = tabHost
				.newTabSpec("Command client")
				.setIndicator("Command Client",
						res.getDrawable(R.drawable.ic_tab_use))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, AllJoynHostActivity.class);
		spec = tabHost
				.newTabSpec("Command host")
				.setIndicator("Command Host",
						res.getDrawable(R.drawable.ic_tab_host))
				.setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);

		// GCM registration code
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(this, Utilities.SENDER_ID);
		} else {
			Log.v(TAG, "Already registered");
		}
		System.out.println("the registration id is:::::" + regId);

		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				Utilities.DISPLAY_MESSAGE_ACTION));

		startProperActivity();
	}

	private void startProperActivity() {
		SharedPreferences sharedPref = getSharedPreferences(getResources()
				.getString(R.string.config_shared_preferences_name),
				Context.MODE_PRIVATE);

		Intent intent;
		// Start the right activity depending on whether we're a tablet or a
		// phone
		if (getResources().getBoolean(R.bool.isTablet)) {

			String venueId = sharedPref.getString("RegisteredVenueId", null);

			if (venueId == null)
				intent = new Intent().setClass(this, VenueRegistration.class);
			else
				intent = new Intent().setClass(this, VenueActivity.class);
		} else {
			// If the user profile has no been set, start the init, if it has,
			// start Bartsy
			if (sharedPref
					.getString(
							getResources().getString(
									R.string.config_user_account_name), "")
					.equalsIgnoreCase("")) {
				// Profile not set
				intent = new Intent().setClass(this, InitActivity.class);
			} else {
				// Start Bartsy - for now we start it here so that we can go
				// back and see
				// what is happening using the Alljoyn stub tab host activity
				// which logs messages
				intent = new Intent().setClass(this, MainActivity.class);
			}
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		this.startActivity(intent);
		
	}
}