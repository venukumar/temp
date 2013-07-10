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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.format.DateUtils;
import android.util.Log;

import com.vendsy.bartsy.R;

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
	 * Returns the date in string in "time ago format"
	 * 
	 * @param input
	 * @param format
	 * @return
	 */
	public static String getFriendlyDate(String input, String format){

		// Parse date using the provided format
		Date date = getLocalDateFromGMTString(input, format);

		// Make sure the date is valid, if not simply return the input string
		if(date==null){
			return input;
		}
		return (String) DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(),DateUtils.SECOND_IN_MILLIS,DateUtils.FORMAT_ABBREV_RELATIVE);
	}
}
