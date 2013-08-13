/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vendsy.bartsy.utils;

import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Cipher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;

import com.actionbarsherlock.app.SherlockActivity;
import com.vendsy.bartsy.MainActivity;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.dialog.WifiNetworksDialog;
import com.vendsy.bartsy.model.Venue;

/**
 * Helper class providing methods and constants common to other classes in the
 * app.
 */
public final class Utilities {

	/**
	 * Base URL of the Demo Server (such as http://my_host:8080/gcm-demo)
	 */
	static final String SERVER_URL = "";


	/**
	 * Tag used on log messages.
	 */

	static final String TAG = "GCMDemo";

	/**
	 * Intent used to display a message in the screen.
	 */
	public static final String DISPLAY_MESSAGE_ACTION = "com.vendsy.bartsy.DISPLAY_MESSAGE";

	/**
	 * Intent's extra that contains the message to be displayed.
	 */
	public static final String EXTRA_MESSAGE = "message";

	/**
	 * Notifies UI to display a message.
	 * <p>
	 * This method is defined in the common helper because it's used both by the
	 * UI and the background service.
	 * 
	 * @param context
	 *            application's context.
	 * @param message
	 *            message to be displayed.
	 */
	public static void displayMessage(Context context, String message) {
		Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
		intent.putExtra(EXTRA_MESSAGE, message);
		context.sendBroadcast(intent);
	}
	
	/**
	 * To prepare progress dialog
	 * 
	 * @param context
	 * @return
	 */
	public static ProgressDialog progressDialog(Context context, String message){
		ProgressDialog mProgressDialog = new ProgressDialog(context);
		
		// To configure the loading dialog
        mProgressDialog.setMessage(message);
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setCancelable(true);
        
        return mProgressDialog;
	}

	/**
	 * Some shortcuts for saving and retrieving string preferences
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */

	public static void savePref(Context context, String key, String value) {

		SharedPreferences sharedPref = context.getSharedPreferences(
				context.getResources().getString(R.string.config_shared_preferences_name), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public static void savePref(Context context, int key, String value) {

		SharedPreferences sharedPref = context.getSharedPreferences(
				context.getResources().getString(R.string.config_shared_preferences_name), Context.MODE_PRIVATE);
		Resources r = context.getResources();
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(r.getString(key), value);
		editor.commit();
	}
	
	public static void savePref(Context context, int key, int value) {

		SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.config_shared_preferences_name), Context.MODE_PRIVATE);
		Resources r = context.getResources();
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putInt(r.getString(key), value);
		editor.commit();
	}
	
	public static String loadPref(Context context, String key, String defaultValue) {
		SharedPreferences sharedPref = context.getSharedPreferences(context.getResources()
				.getString(R.string.config_shared_preferences_name), Context.MODE_PRIVATE);
		return sharedPref.getString(key, defaultValue);
	}
	
	public static String loadPref(Context context, int key, String defaultValue) {
		SharedPreferences sharedPref = context.getSharedPreferences(context.getResources()
				.getString(R.string.config_shared_preferences_name), Context.MODE_PRIVATE);
		Resources r = context.getResources();
		return sharedPref.getString(r.getString(key), defaultValue);
	}
	
	public static int loadPref(Context context, int key, int defaultValue) {
		SharedPreferences sharedPref = context.getSharedPreferences(context.getResources()
				.getString(R.string.config_shared_preferences_name), Context.MODE_PRIVATE);
		Resources r = context.getResources();
		return sharedPref.getInt(r.getString(key), defaultValue);
	}

	public static void removePref(Context context, int key) {
		SharedPreferences sharedPref = context.getSharedPreferences(context.getResources()
				.getString(R.string.config_shared_preferences_name), Context.MODE_PRIVATE);
		Resources r = context.getResources();
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.remove(r.getString(key));
		editor.commit();	
	}
	
	/**
	 * Returns a Date with the GMT string provided as input in the local time zone. The 
	 * @param date
	 * @param format
	 * @return
	 */
	public static Date getLocalDateFromGMTString(String input, String format) {
		
        SimpleDateFormat inputFormat = new SimpleDateFormat(format, Locale.getDefault());
        inputFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm");
        Date output = null;
        String time = "";
        try {
			output = inputFormat.parse(input);
			time = outputFormat.format(output);
		} catch (ParseException e) {
			// Bad date format - leave time blank
			e.printStackTrace();
			Log.e(TAG, "Bad date format in getPastOrders syscall");
			return null;
		}
		return output; 
	}
	
	/**
	 * To parse JSON format to list of venues
	 * 
	 * @param response
	 * @return
	 */
	public static ArrayList<Venue> getVenueListResponse(String response) {

		ArrayList<Venue> list = new ArrayList<Venue>();
		
		JSONArray array = null;
		try {
			JSONObject json = new JSONObject(response);
			if(json.has("venues")){
				array = json.getJSONArray("venues");
			}
		} catch (JSONException e) {
			return null;
		}
		// Make sure that array should not be null
		if(array==null){
			return list;
		}
		
		for (int i = 0; i < array.length(); i++) {

			try {

				JSONObject json = array.getJSONObject(i);

				Venue venue = new Venue(json);

				list.add(venue);
				

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		
		
		return list;
	}
	
	/**
	 * Returns the date in string in "time ago format"
	 * 
	 * @param input
	 * @param format
	 * @return
	 */
	public static String getFriendlyDate(String input, String format){
		long time = System.currentTimeMillis();
		if(input!=null){
	
			// Parse date using the provided format
			Date date = getLocalDateFromGMTString(input, format);
	
			// Make sure the date is valid, if not simply return the input string
			if(date==null){
				return input;
			}
			time = date.getTime();
		}
		return (String) DateUtils.getRelativeTimeSpanString(time, System.currentTimeMillis(),DateUtils.SECOND_IN_MILLIS,DateUtils.FORMAT_ABBREV_RELATIVE);
	}
	
	/**
	 * 
	 * TODO Getters and setters
	 * 
	 */

	public static boolean has(String field) {
		return !(field == null || field.equals(""));
	}
	
	public boolean has(double field) {
		return field != 0;
	}

	public boolean has(boolean field) {
		return field;
	}
	
	public boolean has(Object field) {
		return field != null;
	}



	/**
	 * Check the wifi enabled or not and scan the nearest wifi names.
	 */
	public static void checkNetworkAvailability(final Context activity, final Handler handler) {
		
		final WifiManager wifiManager = (WifiManager)activity.getSystemService(Context.WIFI_SERVICE);

		if(wifiManager==null) return;

			ArrayList<Venue> networks = null;
				
			boolean networkAvailable =false;
			try {
					networkAvailable = WebServices.isNetworkAvailable(activity);
			} catch (Exception e) {}
				
			// if the network is not available then try to turn on wifi
			if(!networkAvailable){
				WifiConfigManager.enableWifi(wifiManager);
			}
			// Check the network is available or not
			try {
				networkAvailable = WebServices.isNetworkAvailable(activity);
			} catch (Exception e) {}
			// If the network is not available then scan nearest wifi names
			if(!networkAvailable){
				networks = searchAvailableWifiNetworks(activity);
			}
			if(networks!=null && networks.size()>0){
				WifiNetworksDialog dialog = new WifiNetworksDialog(activity, networks);
				dialog.show();
			}
	}
	
	/***
	 * 
	 * Scans List of Available Wifi Networks 
	 * 
	 **/
	public static ArrayList<Venue> searchAvailableWifiNetworks(final Context context) {
		// Try to get wifi service
		final WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		// Get list of wifi networks
		List<ScanResult> mScanResults = wifiManager.getScanResults();
		// Nearest networks which are exist in the venues
		ArrayList<Venue> venueNetworks = new ArrayList<Venue>();
		
		if(mScanResults != null && mScanResults.size()>0){
			// Load venue JSON format from shared preference. 
			String response = Utilities.loadPref(context, Venue.Shared_Pref_KEY,"");
			// We should not proceed if the response is empty
			if(response.equals("")){
				return null;
			}
			// Parse venue JSON format and get it in the array list
			ArrayList<Venue> venues = Utilities.getVenueListResponse(response);
			
			for(Venue venue:venues){
				// if the venue does not have wifi then we should not add to the list
				if(!venue.hasWifi()) continue;
				// Make sure that venue network is available in the nearest wifi networks. If it is available then it is require to add to the array list.
				for(ScanResult results : mScanResults){
					Log.d("Available Networks", results.SSID);
					
					if((results.SSID.equals(venue.getWifiName())) ){
						venueNetworks.add(venue);
					}
				}
			}
			
		}
		return venueNetworks;
	}
	
}
