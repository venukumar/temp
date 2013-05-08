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

import wifi.AllJoynHostActivity;
import wifi.AllJoynUseActivity;
import android.os.Bundle;
import android.app.TabActivity;
import android.widget.TabHost;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;
import com.kellislabs.bartsy.*;
import com.kellislabs.bartsy.R.bool;
import com.kellislabs.bartsy.R.drawable;
import com.kellislabs.bartsy.R.layout;
import com.kellislabs.bartsy.R.string;
import com.kellislabs.bartsy.db.DatabaseManager;
import com.kellislabs.bartsy.utils.Utilities;

public class DebugWidget extends TabActivity {
	private static final String TAG = "Bartsy";
	
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("in broadcast receiver:::::::");
			String newMessage = intent.getExtras().getString(
					Utilities.EXTRA_MESSAGE);
			System.out.println("the message is ::::" + newMessage);
			// mDisplay.append(newMessage + "\n");

		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alljoyn_service_main);

		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;

		intent = new Intent().setClass(this, AllJoynUseActivity.class);
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

		intent = new Intent().setClass(this, ContactsClient.class);
		spec = tabHost
				.newTabSpec("People client")
				.setIndicator("People Client",
						res.getDrawable(R.drawable.friend)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, ContactsService.class);
		spec = tabHost
				.newTabSpec("people receive")
				.setIndicator("People Host", res.getDrawable(R.drawable.friend))
				.setContent(intent);
		tabHost.addTab(spec);

		tabHost.setCurrentTab(0);
		
		//	GCM registration code
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


		// Start the right activity depending on whether we're a tablet or a
		// phone
		if (getResources().getBoolean(R.bool.isTablet)) {
			intent = new Intent().setClass(this, VenueActivity.class);
		} else {
			// If the user profile has no been set, start the init, if it has,
			// start Bartsy
			SharedPreferences sharedPref = getSharedPreferences(getResources()
					.getString(R.string.config_shared_preferences_name),
					Context.MODE_PRIVATE);
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
		this.startActivity(intent);
	}
}