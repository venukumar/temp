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

package com.vendsy.bartsy;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.vendsy.bartsy.model.AppObservable;
import com.vendsy.bartsy.model.Category;
import com.vendsy.bartsy.model.Ingredient;
import com.vendsy.bartsy.model.Order;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.model.Venue;
import com.vendsy.bartsy.service.BackgroundService;
import com.vendsy.bartsy.service.ConnectivityService;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.Utilities;
import com.vendsy.bartsy.utils.WebServices;
import com.vendsy.bartsy.view.AppObserver;

/**
 * The ChatAppliation class serves as the Model (in the sense of the common user
 * interface design pattern known as Model-View-Controller) for the chat
 * application.
 * 
 * The ChatApplication inherits from the relatively little-known Android
 * application framework class Application. From the Android developers
 * reference on class Application:
 * 
 * Base class for those who need to maintain global application state. You can
 * provide your own implementation by specifying its name in your
 * AndroidManifest.xml's <application> tag, which will cause that class to be
 * instantiated for you when the process for your application/package is
 * created.
 * 
 * The important property of class Application is that its lifetime coincides
 * with the lifetime of the application, not its activities. Since we have
 * persistent state in our connections to the outside world via our AllJoyn
 * objects, and that state cannot be serialized, saved and restored; we need a
 * persistent object to ensure that state is held if transient objects like
 * Activities are destroyed and recreated by the Android application framework
 * during its normal operation.
 * 
 * This object holds the global state for our chat application, and starts the
 * Android Service that handles the background processing relating to our
 * AllJoyn connections.
 * 
 * Additionally, this class provides the Model for an MVC framework. It provides
 * a relatively abstract idea of what it is the application is doing. For
 * example, we provide methods oriented to conceptual actions (like our user has
 * typed a message) instead of methods oriented to the implementation (like,
 * create an AllJoyn bus object and register it). This allows the user interface
 * to be relatively independent of the channel implementation.
 * 
 * Android Activities can come and go in sometimes surprising ways during the
 * operation of an application. For example, when a phone is rotated from
 * portrait to landscape orientation, the displayed Activities are deleted and
 * recreated in the new orientation. This class holds the persistent state that
 * is required to correctly display Activities when they are recreated.
 */
public class BartsyApplication extends Application implements AppObservable {
	
	
	private static final String TAG = "BartsyApplication";
	public static String PACKAGE_NAME;
	
	/**
	 * When created, the application fires an intent to create the AllJoyn
	 * service. This acts as sort of a combined view/controller in the overall
	 * architecture.
	 */
	public void onCreate() {
		PACKAGE_NAME = getApplicationContext().getPackageName();
		Log.v(TAG, "onCreate()");

		
		// Start background ConnectionCheckingService
		Intent intent = new Intent(this, BackgroundService.class);
		mRunningService = startService(intent);
		if (mRunningService == null) {
			Log.e(TAG, "onCreate(): failed to startService()");
		}

		if (Constants.USE_ALLJOYN) {
			intent = new Intent(this, ConnectivityService.class);
			mRunningService = startService(intent);
			if (mRunningService == null) {
				Log.e(TAG, "onCreate(): failed to startService()");
			}
		}

		// load user profile if it exists. this is an application-wide variable.
		loadUserProfileBasics();

		// Load active venue from preferences
		loadActiveVenue();

		// GCM registration code

		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(this, WebServices.SENDER_ID);
		} else {
			Log.v(TAG, "Already registered");
		}
		Log.v(TAG, "the registration id is:::::" + regId);

		Log.v(TAG, "People list size: " + mPeople.size());
		Log.v(TAG, "Orders list size: " + mOrders.size());
		if (mActiveVenue == null)
			Log.v(TAG, "Not checked in");
		else
			Log.v(TAG, "Checked in at " + mActiveVenue.getName());

		// Setup Crittercism
//		Crittercism.init(getApplicationContext(), "51b1940e46b7c25a30000003");
	}
	

	
	/**
	 * Convenience functions to generate notifications and Toasts 
	 */

	public Handler mHandler = new Handler();

	public void makeText(final String toast, final int length) {
		mHandler.post(new Runnable() {
			public void run() {
				Log.v(TAG, toast);
				Toast.makeText(BartsyApplication.this, toast, length).show();
			}
		});
	}
	
	private void generateNotification(final String title, final String body, final int count) {
		mHandler.post(new Runnable() {
			public void run() {
				
				int icon = R.drawable.ic_launcher;
				long when = System.currentTimeMillis();
				NotificationManager notificationManager = (NotificationManager) BartsyApplication.this
						.getSystemService(Context.NOTIFICATION_SERVICE);
				Notification notification = new Notification(icon, body, when);
		
				Intent notificationIntent = new Intent(BartsyApplication.this, MainActivity.class);
				// set intent so it does not start a new activity
				notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				PendingIntent intent = PendingIntent.getActivity(BartsyApplication.this, 0,
						notificationIntent, 0);
				notification.setLatestEventInfo(BartsyApplication.this, title, body, intent);
				notification.flags |= Notification.FLAG_AUTO_CANCEL;
				notification.number = count;

				// // Play default notification sound
				notification.defaults = Notification.DEFAULT_SOUND;
				notificationManager.notify(0, notification);
			}
		});
	}
		
	/**
	 * 
	 * TODO - active venue
	 * 
	 * The active venue is the venue where the user is checked in or null if the
	 * user is not checked in
	 * 
	 */

	public Venue mActiveVenue = null;

	public boolean hasActiveVenue() {
		return mActiveVenue != null;
	}

	public boolean noActiveVenue() {
		return mActiveVenue == null;
	}
	
	public void updateActiveVenue(String venueId, String venueName, int userCount) {

		Log.v(TAG, "updateActiveVenue(" + venueId + ", " + venueName + ", " + userCount);
		
		if (mActiveVenue == null) {
			// Server thinks we're checked in but we have no active venue - check user in
			Log.e(TAG, "Active venue updated to " + venueName);
			userCheckIn(venueId, venueName, userCount);
			return;
		} 
		
		if (mActiveVenue.getUserCount() != userCount) {
			Log.e(TAG, "Updating user count from " + mActiveVenue.getUserCount() + " to " + userCount);
			mActiveVenue.setUserCount(userCount);
			notifyObservers(PEOPLE_UPDATED);
		}
		
		// Save the new venue to preferences
		saveActiveVenue();
	}
	

	public void userCheckOut() {

		Log.w(TAG, "userCheckOut()");

		eraseActiveVenue();
	}

	public void userCheckIn(String venueId, String venueName, int userCount) {
		
		Log.w(TAG, "userCheckIn(" + venueId + ", " + venueName + ")");
		
		mActiveVenue = new Venue();
		mActiveVenue.setId(venueId);
		mActiveVenue.setName(venueName);
		mActiveVenue.setUserCount(userCount);

		mOrders.clear();
		mPeople.clear();
		
		saveActiveVenue();
		
		notifyObservers(PEOPLE_UPDATED);
		notifyObservers(ORDERS_UPDATED);

	}
	
	public void userCheckIn(Venue venue) {
		
		Log.w(TAG, "userCheckIn(" + venue + ")" );
		
		mActiveVenue = venue;

		mOrders.clear();
		mPeople.clear();
		
		saveActiveVenue();
		
		notifyObservers(PEOPLE_UPDATED);
		notifyObservers(ORDERS_UPDATED); 
	}
	
	public Venue loadActiveVenue () {
		
		Log.w(TAG, "loadActiveVenue()");
		
		if (mActiveVenue != null) {
			Log.v(TAG, "Venue already loaded: " + mActiveVenue);
			saveActiveVenue(); // make sure the active venue is saved
			return mActiveVenue; 
		}
		
		mActiveVenue = null;
		mOrders.clear();
		mPeople.clear();
		
		String venueID = Utilities.loadPref(this, R.string.venueId, null);
		String venueName = Utilities.loadPref(this, R.string.venueName, null);
		int userCount = Utilities.loadPref(this, R.string.venueUserCount, 0);

		if (venueID == null || venueName == null) {
			Log.v(TAG, "No active venue found");
			return null;
		}
		
		mActiveVenue = new Venue();
		mActiveVenue.setId(venueID);
		mActiveVenue.setName(venueName);
		mActiveVenue.setUserCount(userCount);
		
		Log.w(TAG, "Active venue loaded: " + mActiveVenue);
		
		return mActiveVenue;
	}
	
	public void saveActiveVenue() {
		
		Log.w(TAG, "saveActiveVenue(" + mActiveVenue + ")");

		if (mActiveVenue == null) {
			Log.v(TAG, "Active venue doesn't exist");
			eraseActiveVenue();
		} else {
			Log.v(TAG, "Venue saved:  (" + mActiveVenue.getId() + ", " + mActiveVenue.getName() + ")");
			Utilities.savePref(this, R.string.venueId, mActiveVenue.getId());
			Utilities.savePref(this, R.string.venueName, mActiveVenue.getName());
			Utilities.savePref(this, R.string.venueUserCount, mActiveVenue.getUserCount());
		}
	}
	
	private void eraseActiveVenue() {

		Log.w(TAG, "eraseActiveVenue(" + mActiveVenue + ")");
		
		// Delete active venue in memory
		mActiveVenue = null;
		mOrders.clear();
		mPeople.clear();


		// Delete active venue in saved preferences
		
		SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.config_shared_preferences_name), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		Resources r = getResources();
		
		editor.remove(r.getString(R.string.venueId));
		editor.remove(r.getString(R.string.venueName));
		editor.remove(r.getString(R.string.venueUserCount));

		editor.commit();		

	}
	
	
	/***
	 * 
	 * TODO - User Profile
	 * 
	 * The user profile is saved in the application state. It's small enough
	 * that it shouldn't cause memory issues
	 * 
	 */

	public UserProfile mProfile;
	
	// These two fields are only used to pass information to and from the UserProfileActivity from InitActivity
	public UserProfile mUserProfileActivityInput = null; 


	void loadUserProfileBasics() {
		
		Log.w(TAG, "loadUserProfileBasics()");

		// If we already have a profile, don't load one
		if (mProfile != null) {
			Log.v(TAG, "Profile already exists: " + mProfile);
			return;
		}
		
		// Initialize the profile structure
		mProfile = null;

		// Make sure the user's account name has been saved or there is no local profile
		if (loadBartsyId() == null) {
			Log.v(TAG, "No saved user profile");
			return;
		}			

		// Load profile image
		Bitmap image = loadUserProfileImage();
		if (image == null) {
			Log.d(TAG, "Could not load profile image");
		}
		
		// Profile name and image were found. Create a user profile.
		mProfile = new UserProfile();
		
		// Bartsy login
		mProfile.setBartsyLogin(Utilities.loadPref(this, R.string.config_user_login, null));
		mProfile.setBartsyPassword(Utilities.loadPref(this, R.string.config_user_password, null));
		mProfile.setBartsyId(Utilities.loadPref(this, R.string.config_user_bartsyId, null)); 

		// Facebook Login
		mProfile.setFacebookUsername(Utilities.loadPref(this, R.string.config_facebook_username, null));
		mProfile.setFacebookId(Utilities.loadPref(this, R.string.config_facebook_id, null)); 
		
		// Google login
		mProfile.setGoogleUsername(Utilities.loadPref(this, R.string.config_google_username, null));
		mProfile.setGoogleId(Utilities.loadPref(this, R.string.config_google_id, null)); 

		// Other required params
		mProfile.setNickname(Utilities.loadPref(this, R.string.config_user_nickname, null));
		mProfile.setImage(image);

		Log.v(TAG, "Profile loaded: " +  mProfile);
	}
	
	void saveUserProfile(UserProfile profile) {

		// First remove any saved profile data to avoid saving bits of different profiles
		eraseUserProfile();

		Log.w(TAG, "saveUserProfile(" + profile + ")");
				
		// Save in memory
		mProfile = profile;
		
		// Save the username and the user picture along with any other detail that was fetched to the local profile
		SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.config_shared_preferences_name), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		Resources r = getResources();

		if (profile.hasBartsyLogin())
			editor.putString(r.getString(R.string.config_user_login), profile.getBartsyLogin());
		if (profile.hasPassword())
				editor.putString(r.getString(R.string.config_user_password), profile.getPassword());
		if (profile.hasBartsyId())
			editor.putString(r.getString(R.string.config_user_bartsyId), profile.getBartsyId());
		if (profile.hasFacebookUsername())
			editor.putString(r.getString(R.string.config_facebook_username), profile.getFacebookUsername());
		if (profile.hasFacebookId())
			editor.putString(r.getString(R.string.config_facebook_id), profile.getFacebookId());
		if (profile.hasGoogleUsername())
			editor.putString(r.getString(R.string.config_google_username), profile.getGoogleUsername());
		if (profile.hasGoogleId())
			editor.putString(r.getString(R.string.config_google_id), profile.getGoogleId());
		if (profile.hasNickname())
			editor.putString(r.getString(R.string.config_user_nickname), profile.getNickname());
		if (profile.hasImagePath())
			editor.putString(r.getString(R.string.config_user_image_path), profile.getImagePath());			
		editor.commit();
		if (profile.hasImage())
			saveUserProfileImage(profile.getImage());
			
	}

	void eraseUserProfile() {
		
		Log.w(TAG, "eraseUserProfile()");

		mProfile = null;
		
		SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.config_shared_preferences_name), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		Resources r = getResources();
		
		editor.remove(r.getString(R.string.config_user_login));
		editor.remove(r.getString(R.string.config_user_password));
		editor.remove(r.getString(R.string.config_user_bartsyId));

		editor.remove(r.getString(R.string.config_facebook_username));
		editor.remove(r.getString(R.string.config_facebook_id));
		
		editor.remove(r.getString(R.string.config_google_username));
		editor.remove(r.getString(R.string.config_google_id));

		editor.remove(r.getString(R.string.config_user_nickname));
		editor.remove(r.getString(R.string.config_user_image_path));
		editor.commit();		
		eraseUserProfileImage();
	}
	
	
	public String loadBartsyId() {


		SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.config_shared_preferences_name), Context.MODE_PRIVATE);
		Resources r = getResources();
		String id = sharedPref.getString(r.getString(R.string.config_user_bartsyId), null);

		Log.w(TAG, "loadBartsyId(" + id + ")");

		return id;
	}
	
	public void saveBartsyID(String bartsyUserId) {
		
		Log.w(TAG, "saveBartsyId(" + bartsyUserId + ")");

		// Save the unique bartsy ID in the user profile
		if (mProfile != null) 
			mProfile.setBartsyId(bartsyUserId);

		// Save in preferences
		SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.config_shared_preferences_name), Context.MODE_PRIVATE);
		Resources r = getResources();
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(r.getString(R.string.config_user_bartsyId), bartsyUserId);
		editor.commit();
	}
	
	public void saveUserProfileImage(Bitmap bitmap) {
		// Save bitmap to file
		String file = getFilesDir()  + File.separator + getResources().getString(R.string.config_user_profile_picture);
		Log.w(TAG, "Saving user profile image to " + file);

		try {
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Error saving profile image");
		}
	}
	
	Bitmap loadUserProfileImage() {
		String file = getFilesDir()  + File.separator + getResources().getString(R.string.config_user_profile_picture);
		Log.w(TAG, "Loading user profile from " + file);
		Bitmap image = null;
		try {
			image = BitmapFactory.decodeFile(file);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "Could not load profile image");
		}
		return image;
	}
	
	void eraseUserProfileImage() {
		
		Log.w(TAG, "Erase profile image");
		
		File file = new File(getFilesDir()  + File.separator + getResources().getString(R.string.config_user_profile_picture));
		file.delete();
	}
	
	
	
	/*****
	 * 
	 * TODO - People 
	 * 
	 * The list of people present (when checked in) is also saved here, in the
	 * global state. We have all handling code here because the application
	 * always runs in the background. If there is an activity that displays a
	 * view of that list listening, we will send an update message, but this
	 * code will always correctly change the model so that we never lose orders
	 * even if hte phone (or tablet) is in sleep mode, etc.
	 * 
	 */

	public ArrayList<UserProfile> mPeople = new ArrayList<UserProfile>();
	public static final String PEOPLE_UPDATED = "PEOPLE_UPDATED";

	/*
	 * Called when we have a new person check in a venue
	 */

	synchronized public void addPerson(UserProfile profile) {
		Log.v(TAG, "New user checked in: " + profile.getName() + " (" + profile.getBartsyId() + ")");

		// Go over the list of orders and update profiles based on the information received
		for (Order order : mOrders) {
			if (profile.getBartsyId().equals(order.senderId))
				order.orderSender = profile;
			if (profile.getBartsyId().equals(order.recipientId))
				order.orderRecipient = profile;
		}
		
		mPeople.add(profile);
		mActiveVenue.setUserCount(mPeople.size());
//		notifyObservers(PEOPLE_UPDATED);
	}

	
	
	/***
	 * 
	 * TODO - Orders
	 * 
	 * This is the local order id counter, incremented for each order and unique
	 * within the context of the application. The server will have a different
	 * number to describe the same order and there should be a mapping between
	 * the client and server numbers on the server. The server should then send
	 * the server side number along with the client side number to be able to
	 * match the order that's present on the phone with the one present at the
	 * server and the tablet.
	 * 
	 */
	
	public static final String ORDERS_UPDATED = "ORDERS_UPDATED";
	private ArrayList<Order> mOrders = new ArrayList<Order>();
	private Order mCurrentOrder = null;
	
	@SuppressWarnings("unchecked")
	public ArrayList<Order> getOrdersCopy() {
			return (ArrayList<Order>) mOrders.clone();
	}

	public void clearOrders() {
		mOrders.clear();
	}

	private synchronized boolean addOrder(Order order) {

		// Make sure we have a valid order
		if (order == null || order.serverId == null) {
			Log.e(TAG, "addOrder() encountered invalid order");
			return false;
		}

		// Make sure we have a valid venue
		if (mActiveVenue == null) {
			// For now hard crash - THIS NEEDS TO BE HANDLED BETTER (perhaps)
			Log.e(TAG, "addOrder() trying to add order with no active venue");
			return false;
		}

		// Set the order timeout based on the venue timeout value
		order.timeOut = mActiveVenue.getOrderTimeout();
		
		// Try to find the sender in the list of people to have full picture/username detail
		for (UserProfile profile : mPeople) {
			if (profile.getBartsyId().equals(order.senderId)) {
				order.orderSender = profile;
			} else if (profile.getBartsyId().equals(order.recipientId)) {
				order.orderRecipient = profile;
			}
		}
		
		// Make sure the order knows the bartsy ID of the profile on this phone to know who's the sender and who's the receiver
		order.bartsyId = mProfile.getBartsyId();
		
		// Add the order to the list of orders
		mOrders.add(order);

		return true;
	}

	public synchronized void removeOrder(Order order) {
		// Add the order to the list of orders
		mOrders.remove(order);
		notifyObservers(ORDERS_UPDATED);
	}

	public int getOrderCount() {
		return mOrders.size();
	}

	
	/*
	 * This updates the status of the order locally based on the status changed
	 * reported from the server
	 */
/*
	synchronized String updateOrder(String order_server_id, String remote_order_status) {

		Log.v(TAG, "Update for remote code " + order_server_id);

		int remote_status = Integer.parseInt(remote_order_status);
		String message = null;

		Order localOrder = null;
		for (Order order : mOrders) {
			if (order_server_id.equalsIgnoreCase(order.serverID)) {
				localOrder = order;
			}
		}

		// Make sure we have a local order
		if (localOrder == null)
			return syncAppWithServer("Received an update for a non existing order and recovered.");

		Log.e(TAG, "order " + order_server_id + " updated from status " + localOrder.status + " to status " + remote_order_status);

		// Update the status of the local order based on that of the remote order and return on error
		switch (remote_status) {
		case Order.ORDER_STATUS_IN_PROGRESS:
			// The order has been accepted remotely. Set the server_id on this order and update status and view
			if (localOrder.status != Order.ORDER_STATUS_NEW) 				
				return syncAppWithServer("Sycnronized Bartsy due to order missmatch.");
			message = "Order " + localOrder.serverID + " was accepted by the bartender.";
			localOrder.nextPositiveState();
			break;
		case Order.ORDER_STATUS_READY:
			// Remote order ready. Notify client with a notification and update status/view
			if (localOrder.status != Order.ORDER_STATUS_IN_PROGRESS)
				return syncAppWithServer("Sycnronized Bartsy due to order missmatch.");
			message = "Order " + localOrder.serverID + " is ready! Please please it up promptly to avoid charges.";
			localOrder.nextPositiveState();
			break;
		case Order.ORDER_STATUS_COMPLETE:
			// Order completed. Remove from the order list for now.
			if (localOrder.status != Order.ORDER_STATUS_READY) 
				return syncAppWithServer("Sycnronized Bartsy due to order missmatch.");
			Toast.makeText(this, "Your order was picked up. You can view a log of orders in the past orders tab", Toast.LENGTH_SHORT);
			localOrder.nextPositiveState();
			mOrders.remove(localOrder);
			message =  "Order " + localOrder.serverID + " was picked up. If you didn't pick it up please see your bartender.";
			break;
		case Order.ORDER_STATUS_CANCELLED:
			// Order cancelled. Notify the user and keep it in the list until acknowledged
			localOrder.setCancelledState();
			message = "Order " + localOrder.serverID + " is taking too long! Please check with your bartender.";
			break;
		case Order.ORDER_STATUS_REJECTED:
		case Order.ORDER_STATUS_FAILED:
		case Order.ORDER_STATUS_INCOMPLETE:
			localOrder.nextNegativeState("Your order was rejected");
			message = "Order " +localOrder.serverID + " rejected by the venue.";
			break;
		}

		// Update the orders tab view and title

		notifyObservers(ORDERS_UPDATED);
		return message;
	}
*/
	
	
	/**
	 * TODO - Synchronization
	 * 
	 * Functions that perform synchronization with the server. All these functions expect to be called from a thread other than 
	 * the main application thread and return an error code depending on success or failure:
	 * 
	 * @param message
	 */
	

	
	synchronized public String syncAppWithServer(final String message) {
		Log.w(TAG, ">>> Synchronize(" + message + ")");
		if (syncUserProfile() == SYNC_RESULT_OK) {
			syncActiveVenue();
			syncOrders();
		}
		return message;
	}
	
	/**
	 * This functions synchronizes the application with the server. It expects to be called from the background
	 * and returns an error code depending on success or failure:
	 */
	
	public static final int SYNC_RESULT_OK 					= 0;
	public static final int SYNC_RESULT_NO_LOCAL_PROFILE	= 1;
	public static final int SYNC_RESULT_NO_SERVER_PROFILE	= 2;
	public static final int SYNC_RESULT_JSON_ERROR			= 3;
	
	synchronized public int syncUserProfile() {

		Log.w(TAG, "syncUserProfile()");
		
		// Try to load user profile from preferences
		loadUserProfileBasics();

		// If not found, nothing more to be done that returning.
		if (mProfile == null)
			return SYNC_RESULT_NO_LOCAL_PROFILE; 
		
		UserProfile user = WebServices.getUserProfile(this, mProfile);
		if (user == null) {
			// Could not get user details - erase our user locally and of course, don't check anybody in
			Log.w(TAG, "Could not load user profile");
			return SYNC_RESULT_NO_SERVER_PROFILE;
		} 
		
		// Download user image
		Bitmap image = WebServices.fetchImage(user.getImagePath());
		if (image != null) {
			user.setImage(image);
			user.setImageDownloaded(true);
		}
		
		// Got a valid profile - save it locally 
		Log.w(TAG, "Found profile: " + user);
		saveUserProfile(user);

		return SYNC_RESULT_OK;	
	}
	

	synchronized public int syncActiveVenue() {
		
		Log.w(TAG, "syncActiveVenue()");

		/*
		 * Synchronize active venue
		 */
		
		if (mProfile == null)
			return SYNC_RESULT_NO_LOCAL_PROFILE;
		
		Venue venue = WebServices.getActiveVenue(BartsyApplication.this, mProfile);
	
		// If venue found - set it up as the active venue
		if (venue != null) {
			Log.w(TAG, "Active venue found: " + venue.getName());
			userCheckIn(venue);
			
		} else {
			// 
			Log.v(TAG, "Active venue not found");
			userCheckOut();
		}
		
		return SYNC_RESULT_OK;	
	}
	
	synchronized public int syncOpenOrders() {

		Log.w(TAG, "syncOpenOrders()");

		JSONObject orders = WebServices.getOpenOrders(BartsyApplication.this);
		if (orders != null) {
			try {
				JSONArray listOfOrders = orders.has("orders") ? orders.getJSONArray("orders") : null;
				if (listOfOrders != null) {

					// Get the server's view of the open orders - for now replace our list with that of the server
					mOrders.clear();
					for (int i = 0; i < listOfOrders.length(); i++) {
						JSONObject orderJson = (JSONObject) listOfOrders.get(i);
						
						if (!orderJson.has("orderTimeout"))
							orderJson.put("orderTimeout", mActiveVenue.getOrderTimeout());
						
						Order order = new Order(orderJson);
						addOrder(order);
					}
				} else {
					// We didn't get a response - keep our list
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return SYNC_RESULT_JSON_ERROR;
			}
		}
		
		notifyObservers(ORDERS_UPDATED);

		return SYNC_RESULT_OK;	
	}
	


	synchronized public void performHeartbeat() {
		
		Log.v(TAG, "performHeartbeat()");
		
		if (mActiveVenue == null) {
			Log.v(TAG, "No active venue - skipping heartbeat");
			return;
		}
		if (mProfile == null) {
			Log.v(TAG, "No active profile - skipping heartbeat");
			return;
		}
		
		JSONObject json = WebServices.postHeartbeatResponse(BartsyApplication.this, mProfile.getBartsyId(), mActiveVenue.getId());
		
		
		// Update venue, order and people counts
		if (json.has("venueId")) {
			try {
				updateActiveVenue(json.getString("venueId"), json.getString("venueName"), json.getInt("userCount"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			// We don't have an active venue - make sure we don't and delete local references
			userCheckOut();
		}
	}
	
	
	/**
	 * TODO - Synchronize orders
	 * 
	 * This is the main synchronization function with the server. It's called if a discrepancy is found in our state versus the server state.
	 * It performs all necessary synchronizations between our state and the server state.
	 * @param message
	 * @param background  - run in the background or not
	 */
	
	
	
	
	/**
	 * Accessors for the main update function. These decide if we should access in the current
	 * thread of spin up a new thread. They also can return a cloned version of the orders list
	 * instead of performing an update
	 */
	
	synchronized public ArrayList<Order> cloneOrders() {
		return accessOrders(ACCESS_ORDERS_VIEW);
	}
	
	synchronized public void syncOrders()  {
	
		if (Looper.myLooper() == Looper.getMainLooper()) {
			// We're in the main thread - execute the update in the background with a new asynchronous task
			Log.w(TAG, "Running updateOrders() in an async task");
//			mHandler.post(new Runnable() {
//				
//				@Override
//				public void run() {
//					new Thread () {
//						@Override 
//						public void run() {
//							accessOrders(BartsyApplication.ACCESS_ORDERS_UPDATE);				
//						};
//					}.start();
//				};
//			});

			new UpdateAsync().execute();
		} else {
			// We're not in the main thread - don't spin up a thread
			Log.w(TAG, "Running updateOrders()");
			accessOrders(ACCESS_ORDERS_UPDATE);
		}
		
		return;
	}
	
	private class UpdateAsync extends AsyncTask<Void, Void, Void>{
		@Override
		protected void onPreExecute(){
		}
		
		@Override
		protected Void doInBackground(Void... Voids) {
			accessOrders(ACCESS_ORDERS_UPDATE);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void params){
		}
	}
	
	/**
	 * 
	 * This is the main function of the bunch. It performs most of the work using helper functions. It looks at the server
	 * state and updates the current state by adding, removing and updated orders.
	 * 
	 */
	public static final int ACCESS_ORDERS_UPDATE	= 0;
	public static final int ACCESS_ORDERS_VIEW		= 1;
	
	@SuppressWarnings("unchecked")
	synchronized private ArrayList<Order> accessOrders(int options) {
		
		if (options == ACCESS_ORDERS_VIEW) {
			return (ArrayList<Order>) mOrders.clone();
		}

		
		// Print orders before update
		String ordersString = "\n";
		for (Order order : mOrders) {
			ordersString += order + "\n";
		}
		Log.w(TAG, ">>> Open orders before update:\n" + ordersString);

		
		boolean network = false;
		try {
			network = WebServices.isNetworkAvailable(BartsyApplication.this);
		} catch (Exception e) {
			e.printStackTrace();
		}	

		// Synchronize only when network is up and when there is an active venue
		
		if (network && mActiveVenue != null) {	
			
			JSONObject json = WebServices.getOpenOrders(BartsyApplication.this);
			if(json != null) {
	
				// Synchronize people 
				updatePeople(json);
				
				// Get remote orders list
				ArrayList<Order> remoteOrders = extractOrders(json);
		
				// Find new orders, existing orders and missing orders.
				ArrayList<Order> addedOrders = processAddedOrders(mOrders, remoteOrders);
				ArrayList<Order> removedOrders = processRemovedOrders(mOrders, remoteOrders);
				ArrayList<Order> updatedOrders = processExistingOrders(mOrders, remoteOrders);
		
				
				// Generate notifications
				if (addedOrders.size() > 0 || removedOrders.size() > 0 || updatedOrders.size() > 0) {
					String message = "";
					int count = 0;
					if (addedOrders.size() > 0) {
						for (Order order : addedOrders) {
							message += order.readableStatus() + "\n";
							count++;
						}
						message += "\n";
					}	
					if (updatedOrders.size() > 0) {
						for (Order order : updatedOrders) {
							message +=  order.readableStatus() + "\n";
							count++;
						}
						message += "\n";
					}	
					if (removedOrders.size() > 0) {
						for (Order order : removedOrders) {
							message += order.readableStatus() + "\n";
							count++;
						}
					}	
		
					// Print and generate modifications
					Log.w(TAG, message);
					generateNotification("Orders updated", message, count);
				}		
		
				// Print orders after update
				ordersString = "\n";
				for (Order order : mOrders) {
					ordersString += order + "\n";
				}
				Log.w(TAG, ">>> Open orders after update:\n" + ordersString);
			}
		}
		
		// Update timers and notify observers of status changes
		updateOrderTimers();
		notifyObservers(ORDERS_UPDATED);
		
		return null;
	}
	
	ArrayList<Order> extractOrders(JSONObject json) {
		
		ArrayList<Order> orders = new ArrayList<Order>();
		
		try {
			// To parse orders from JSON object
			if (json.has("orders")) {
				JSONArray ordersJson = json.getJSONArray("orders");
				
				for(int j=0; j<ordersJson.length();j++){
					
					JSONObject orderJSON = ordersJson.getJSONObject(j);
	
					// If the server is incorrectly sending the order timeout as a venue-wide variable, insert it in the order JSON
					if (!orderJSON.has("orderTimeout") && json.has("orderTimeout"))
						orderJSON.put("orderTimeout", json.getInt("orderTimeout"));
					
					Order order = new Order(orderJSON);
					orders.add(order);
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return orders;
	}
	
	Order findMatchingOrder(ArrayList<Order> orders, Order order) {
		for (Order found : orders) {
			if (found.serverId.equals(order.serverId))
				return found;
		}
		return null;
	}
	
	
	/**
	 * These are the processing helper functions of the main update function. They process orders that were in the server
	 * but not in the local state (new orders), orders that are in the local state but were not in the server state (removed 
	 * orders) and orders that are both in the server state and the local state (updated orders)
	 * 
	 */
	
	ArrayList<Order> processAddedOrders(ArrayList<Order> localOrders, ArrayList<Order> remoteOrders) {

		Log.w(TAG, "processAddedOrders()");

		// Find the orders to remove and store them in a separate list to avoid iterator issues
		ArrayList<Order> processedOrders = new ArrayList<Order>();
		for (Order order : remoteOrders) {
			if (findMatchingOrder(localOrders, order) == null) {

				switch(order.status) {
				
				// For order that have completed their lifecycle and we're just learning about it, remove them from host
				case Order.ORDER_STATUS_CANCELLED:
				case Order.ORDER_STATUS_COMPLETE:
				case Order.ORDER_STATUS_REJECTED:
				case Order.ORDER_STATUS_FAILED:
				case Order.ORDER_STATUS_INCOMPLETE:
				case Order.ORDER_STATUS_OFFER_REJECTED:
					order.updateStatus(order.status);
					order.updateStatus(Order.ORDER_STATUS_REMOVED);
					WebServices.orderStatusChanged(order, this);
					break;
					
				// These order we're learning about because we probably have lost our local orders cache. Add them to the cache.
				case Order.ORDER_STATUS_NEW:
				case Order.ORDER_STATUS_IN_PROGRESS:
				case Order.ORDER_STATUS_READY:
				case Order.ORDER_STATUS_OFFERED:
					processedOrders.add(order);
					break;
					
				// Illegal remote order state - print message and skip it
				case Order.ORDER_STATUS_REMOVED:
				default:
					Log.e(TAG, "Skipping illegal added order: " + order.serverId + " with status: " + order.status);
					break;
				}
			}
		}
		
		// Add the orders found
		ArrayList<Order> addedOrders = new ArrayList<Order>();
		for (Order order : processedOrders) {
			if (addOrder(order)) {
				Log.e(TAG, "Adding order: " + order.serverId + " with status: " + order.status);
				addedOrders.add(order);
			} else {
				Log.e(TAG, "Could not add order: " + order.serverId + " with status: " + order.status);
			}
		}
		
		return addedOrders;
	}
	
	ArrayList<Order> processRemovedOrders(ArrayList<Order> localOrders, ArrayList<Order> remoteOrders) {
		
		Log.w(TAG, "processRemovedOrders()");
		
		// Find the orders to remove and store them in a separate list to avoid iterator issues
		ArrayList<Order> removedOrders = new ArrayList<Order>();
		for (Order order : localOrders) {
			Order remoteOrder = findMatchingOrder(remoteOrders, order);
			if ( remoteOrder == null) {

				switch(order.status) {

				// These orders have finished their lifecycle and the host doesn't know about them. Remove them
				case Order.ORDER_STATUS_COMPLETE:
				case Order.ORDER_STATUS_REJECTED:
				case Order.ORDER_STATUS_FAILED:
				case Order.ORDER_STATUS_INCOMPLETE:
				case Order.ORDER_STATUS_OFFER_REJECTED:
					order.updateStatus(Order.ORDER_STATUS_REMOVED);
					break;
					
				// For these statuses we already expect user action, so keep them around
				case Order.ORDER_STATUS_TIMEOUT:
				case Order.ORDER_STATUS_REMOVED:
				case Order.ORDER_STATUS_CANCELLED:
					break;
					
				// We have orders that are still in progress and haven't timed out locally. Set them to timeout.
				case Order.ORDER_STATUS_NEW:
				case Order.ORDER_STATUS_IN_PROGRESS:
				case Order.ORDER_STATUS_READY:
				case Order.ORDER_STATUS_OFFERED:
					Log.e(TAG, "Timing out removed order: " + order.serverId + " with status: " + order.status);
					order.setTimeoutState();
					break;
				}
			}
		}
		
		// Remove orders found
		for (Order order : removedOrders) {
			localOrders.remove(order);
		}
		
		return removedOrders;
	}		
	
	ArrayList<Order> processExistingOrders(ArrayList<Order> localOrders, ArrayList<Order> remoteOrders) {

		Log.w(TAG, "processExistingOrders()");
		
		ArrayList<Order> updatedOrders = new ArrayList<Order>();

		for (Order remoteOrder : remoteOrders) {
			
			Order localOrder = findMatchingOrder(localOrders, remoteOrder);

			// Handle the case where the timeout of the bartender has changed while we have open orders 
			if (localOrder != null && localOrder.timeOut != remoteOrder.timeOut) {
				Log.v(TAG, "Adjusting order timeout for order " + localOrder.serverId + " from " + localOrder.timeOut + " to " + remoteOrder.timeOut);
				localOrder.timeOut = remoteOrder.timeOut;
			}
				
			if ( localOrder != null && localOrder.status != remoteOrder.status) {
				
				// Ignore the remote status in some cases
				boolean ignoreRemote = false;
				
				switch (localOrder.status) {
				case Order.ORDER_STATUS_OFFER_REJECTED:
					// We shouldn't be in this state unless the host didn't hear our state change. Remind the host.
					if (localOrder.senderId.equals(localOrder.bartsyId)) {
						// We are the sender - acknowledge the change of status and remove the order
						localOrder.updateStatus(remoteOrder.status);
						localOrder.updateStatus(Order.ORDER_STATUS_REMOVED);
						WebServices.orderStatusChanged(localOrder, BartsyApplication.this);
						updatedOrders.add(localOrder);
						ignoreRemote = true;
					} else {
						// We are the recipient - this is not a legal state for the server to be updating us on
						Log.e(TAG, "Skipping illegal existing order: " + localOrder.serverId + " with status: " + localOrder.status);
						localOrder.updateStatus(Order.ORDER_STATUS_OFFER_REJECTED);
						ignoreRemote = true;
					}
					break;
				}
				
				// Update the status of the local order based on that of the remote order and return on error
				
				if (!ignoreRemote) {
					
					switch (remoteOrder.status) {
					
					// These orders have finished their lifecycle and the host knows about them. Let the host know to remove them.
					case Order.ORDER_STATUS_REJECTED:
					case Order.ORDER_STATUS_FAILED:
					case Order.ORDER_STATUS_COMPLETE:
					case Order.ORDER_STATUS_INCOMPLETE:
					case Order.ORDER_STATUS_CANCELLED:
						// Change the state and leave it in the order list until user acknowledges the time out ****
						localOrder.updateStatus(remoteOrder.status);
						localOrder.updateStatus(Order.ORDER_STATUS_REMOVED);
						WebServices.orderStatusChanged(localOrder, BartsyApplication.this);
						updatedOrders.add(localOrder);
						break;
					
					// The host should never be sending us removed orders. Log the illegal state and let the order expire locally.
					case Order.ORDER_STATUS_REMOVED:
						Log.e(TAG, "Skipping illegal existing order: " + localOrder.serverId + " with status: " + localOrder.status);
						localOrder.updateStatus(Order.ORDER_STATUS_REMOVED);
						break;
	
					// Handling offered drinks is tricky because it depends on who the sender/recipient are
					case Order.ORDER_STATUS_OFFER_REJECTED:
						if (localOrder.senderId.equals(localOrder.bartsyId)) {
							// We are the sender - acknowledge the change of status and remove the order
							localOrder.updateStatus(remoteOrder.status);
							localOrder.updateStatus(Order.ORDER_STATUS_REMOVED);
							WebServices.orderStatusChanged(localOrder, BartsyApplication.this);
							updatedOrders.add(localOrder);
						} else {
							// We are the recipient - this is not a legal state for the server to be updating us on (we should be updating the host on this)
							Log.e(TAG, "Skipping illegal existing order: " + localOrder.serverId + " with status: " + localOrder.status);
							localOrder.updateStatus(Order.ORDER_STATUS_OFFER_REJECTED);
						}
						break;
						
					// These orders have legitimately changed status to match that of the host. Update them locally.
					case Order.ORDER_STATUS_NEW:
					case Order.ORDER_STATUS_IN_PROGRESS:
					case Order.ORDER_STATUS_READY:
						localOrder.updateStatus(remoteOrder.status);
						updatedOrders.add(localOrder);
						break;
						
					// These orders are somehow in a remote state that shouldn't be possible. Match host state!
					case Order.ORDER_STATUS_TIMEOUT:
					case Order.ORDER_STATUS_OFFERED:
					default:
						Log.e(TAG, "Skipping illegal existing order: " + localOrder.serverId + " with status: " + localOrder.status);
//						localOrder.updateStatus(remoteOrder.status);
						break;
					}
				}
			}
		}
		return updatedOrders;
	}		
	
	

	/**
	 * 
	 * This functions updates the timers of the various orders and moves them to the expired state in case of a local timeout
	 * 
	 */
	
	private synchronized void updateOrderTimers() {

		Log.v(TAG, "updateOrderTimers()");
		
		for (Order order : mOrders) {

			// The additional timeout when we check for local timeouts gives the server the opportunity to always time out an order first. This 
			long duration  = Constants.timoutDelay + order.timeOut - ((System.currentTimeMillis() - (order.state_transitions[order.status]).getTime()))/60000;
			
			if (duration <= 0) {

				Log.v(TAG, "Order " + order.serverId + " timed out. Status " + order.status + " (" + order.state_transitions[order.status] + 
						"), last_status: " + order.last_status + " (" + order.state_transitions[order.last_status] + 
						"), placed (" + order.state_transitions[Order.ORDER_STATUS_NEW] + ")");

				// Order time out - set it to that state (this won't have an effect if already in that state as the called function guarantees that)
				order.setTimeoutState();
			}
		}
	}
	

	/**
	 * 
	 * TODO - Synchronize people
	 * 
	 * 
	 * 
	 */
	
	synchronized private void updatePeople(JSONObject json) {
		// Verify checked in users match server list
		try {
			
			if(json.has("checkedInUsers")){
				
				JSONArray users;
					users = json.getJSONArray("checkedInUsers");
				
				// Check sizes match
				if (users.length() != mPeople.size()) {
					syncPeople(users);
					return;
				}
				
				// Check Id's match
				for(int i=0; i<users.length() ; i++){
					JSONObject userJson = users.getJSONObject(i);
					
					boolean found = false;
					for (UserProfile person : mPeople) {
						if (person.getBartsyId().equalsIgnoreCase(userJson.getString("bartsyId"))) {
							found = true;
						}
					}
	
					if (!found) {
						syncPeople(users);
						return;
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	synchronized private void syncPeople(JSONArray users) {
		Log.w(TAG, "syncPeople()");
		
		try {
			mPeople.clear();
			for(int i=0; i<users.length() ; i++) {
					mPeople.add(new UserProfile(users.getJSONObject(i)));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		notifyObservers(PEOPLE_UPDATED);

	}
	
	
	
	
	
	/**
	 * TODO - Custom drinks
	 * 
	 * The spirit list is saved in the global application state. This is done to
	 * avoid losing any spirits while the other activities are swapped in and out
	 * as the user navigates in different screens.
	 * 
	 */
	public ArrayList<Category> spirits = new ArrayList<Category>();
	public Ingredient selectedSpirit;
	

	/**
	 * The mixers list is saved in the global application state. This is done to
	 * avoid losing any mixers while the other activities are swapped in and out
	 * as the user navigates in different screens.
	 * 
	 */
	public ArrayList<Category> mixers = new ArrayList<Category>();
	

	/************************************************************************
	 * 
	 * 
	 * 
	 * TODO - This is the AllJoyn code
	 * 
	 * 
	 * 
	 */

	ComponentName mRunningService = null;

	/**
	 * Since our application is "rooted" in this class derived from Application
	 * and we have a long-running service, we can't just call finish in one of
	 * the Activities. We have to orchestrate it from here. We send an event
	 * notification out to all of our observers which tells them to exit.
	 * 
	 * Note that as a result of the notification, all of the observers will stop
	 * -- as they should. One of the things that will stop is the AllJoyn
	 * Service. Notice that it is started in the onCreate() method of the
	 * Application. As noted in the Android documentation, the Application class
	 * never gets torn down, nor does it provide a way to tear itself down.
	 * Thus, if the Chat application is ever run again, we need to have a way of
	 * detecting the case where it is "re-run" and then "re-start" the service.
	 */
	public void quit() {
		notifyObservers(APPLICATION_QUIT_EVENT);
		mRunningService = null;
	}
	
	/**
	 * Application components call this method to indicate that they are alive
	 * and may have need of the AllJoyn Service. This is required because the
	 * Android Application class doesn't have an end to its lifecycle other than
	 * through "kill -9". See quit().
	 */
	public void checkin() {
		Log.v(TAG, "checkin()");
		if (Constants.USE_ALLJOYN && mRunningService == null) {
			Log.v(TAG, "checkin():  Starting the AllJoynService");
			Intent intent = new Intent(this, ConnectivityService.class);
			mRunningService = startService(intent);
			if (mRunningService == null) {
				Log.v(TAG, "checkin(): failed to startService()");
			}
		}
	}

	public static final String APPLICATION_QUIT_EVENT = "APPLICATION_QUIT_EVENT";

	/**
	 * This is the method that AllJoyn Service calls to tell us that an error
	 * has happened. We are provided a module, which corresponds to the high-
	 * level "hunk" of code where the error happened, and a descriptive string
	 * that we do not interpret.
	 * 
	 * We expect the user interface code to sort out the best activity to tell
	 * the user about the error (by calling getErrorModule) and then to call in
	 * to get the string.
	 */
	public synchronized void alljoynError(Module m, String s) {
		mModule = m;
		mErrorString = s;
		notifyObservers(ALLJOYN_ERROR_EVENT);
	}

	/**
	 * Return the high-level module that caught the last AllJoyn error.
	 */
	public Module getErrorModule() {
		return mModule;
	}

	/**
	 * The high-level module that caught the last AllJoyn error.
	 */
	private Module mModule = Module.NONE;

	/**
	 * Enumeration of the high-level moudules in the system. There is one value
	 * per module.
	 */
	public static enum Module {
		NONE, GENERAL, USE, HOST
	}

	/**
	 * Return the error string stored when the last AllJoyn error happened.
	 */
	public String getErrorString() {
		return mErrorString;
	}

	/**
	 * The string representing the last AllJoyn error that happened in the
	 * AllJoyn Service.
	 */
	private String mErrorString = "ER_OK";

	/**
	 * The object we use in notifications to indicate that an AllJoyn error has
	 * happened.
	 */
	public static final String ALLJOYN_ERROR_EVENT = "ALLJOYN_ERROR_EVENT";

	/**
	 * Called from the AllJoyn Service when it gets a FoundAdvertisedName. We
	 * know by construction that the advertised name will correspond to a chat
	 * channel. Note that the channel here is the complete well-known name of
	 * the bus attachment advertising the channel. In most other places it is
	 * simply the channel name, which is the final segment of the well-known
	 * name.
	 */
	public synchronized void addFoundChannel(String channel) {
		Log.v(TAG, "addFoundChannel(" + channel + ")");
		removeFoundChannel(channel);
		mChannels.add(channel);
		Log.v(TAG, "addFoundChannel(): added " + channel);
		notifyObservers(NEW_CHANNEL_FOUND_EVENT);

	}

	/**
	 * The object we use in notifications to indicate that a channel has been
	 * found. By default Bartsy joins new channels automatically unless it's
	 * already connected to a channel. If it's already connected it adds the new
	 * channel to the list of channels in the main action bar UI and notifies
	 * the user with a notification that there are other services available
	 * nearby
	 */
	public static final String NEW_CHANNEL_FOUND_EVENT = "NEW_CHANNEL_FOUND_EVENT";

	/**
	 * Called from the AllJoyn Service when it gets a LostAdvertisedName. We
	 * know by construction that the advertised name will correspond to an chat
	 * channel.
	 */
	public synchronized void removeFoundChannel(String channel) {
		Log.v(TAG, "removeFoundChannel(" + channel + ")");

		for (Iterator<String> i = mChannels.iterator(); i.hasNext();) {
			String string = i.next();
			if (string.equals(channel)) {
				Log.v(TAG, "removeFoundChannel(): removed " + channel);
				i.remove();
			}
		}
	}

	/**
	 * Whenever the user is asked for a channel to join, it needs the list of
	 * channels found via FoundAdvertisedName. This method provides that list.
	 * Since we have no idea how or when the caller is going to access or change
	 * the list, and we are deeply paranoid, we provide a deep copy.
	 */
	public synchronized List<String> getFoundChannels() {
		Log.v(TAG, "getFoundChannels()");
		List<String> clone = new ArrayList<String>(mChannels.size());
		for (String string : mChannels) {
			Log.v(TAG, "getFoundChannels(): added " + string);
			clone.add(new String(string));
		}
		return clone;
	}

	/**
	 * The channels list is the list of all well-known names that correspond to
	 * channels we might conceivably be interested in. We expect that the "use"
	 * GUID will allow the local user to have this list displayed in a
	 * "join channel" dialog, whereupon she will choose one. This will
	 * eventually result in a joinSession call out from the AllJoyn Service
	 */
	private List<String> mChannels = new ArrayList<String>();

	/**
	 * The application has three ideas about the state of its channels. This is
	 * very detailed for a real application, but since this is an AllJoyn
	 * sample, we think it is important to convey the detailed state back to our
	 * user, whom we assume knows what it all means.
	 * 
	 * We have a basic bus attachment state, which reflects the fact that we
	 * can't do anything without a bus attachment. When the service comes up it
	 * automatically connects and starts discovering other instances of the
	 * application, so this isn't terribly interesting.
	 */
	public ConnectivityService.BusAttachmentState mBusAttachmentState = ConnectivityService.BusAttachmentState.DISCONNECTED;

	/**
	 * Set the status of the "host" channel. The AllJoyn Service part of the
	 * Application is expected to make this call to set the status to reflect
	 * the status of the underlying AllJoyn session.
	 */
	public synchronized void hostSetChannelState(
			ConnectivityService.HostChannelState state) {
		mHostChannelState = state;
		notifyObservers(HOST_CHANNEL_STATE_CHANGED_EVENT);
	}

	/**
	 * Get the state of the "use" channel.
	 */
	public synchronized ConnectivityService.HostChannelState hostGetChannelState() {
		return mHostChannelState;
	}

	/**
	 * The "host" state which reflects the state of the part of the system
	 * related to hosting an chat channel. In a "real" application this kind of
	 * detail probably isn't appropriate, but we want to do so for this sample.
	 */
	private ConnectivityService.HostChannelState mHostChannelState = ConnectivityService.HostChannelState.IDLE;

	/**
	 * Set the name part of the "host" channel. Since we are going to "use" a
	 * channel that is implemented remotely and discovered through an AllJoyn
	 * FoundAdvertisedName, this must come from a list of advertised names.
	 * These names are our channels, and so we expect the GUI to choose from
	 * among the list of channels it retrieves from getFoundChannels().
	 * 
	 * Since we are talking about user-level interactions here, we are talking
	 * about the final segment of a well-known name representing a channel at
	 * this point.
	 */
	public synchronized void hostSetChannelName(String name) {
		mHostChannelName = name;
		notifyObservers(HOST_CHANNEL_STATE_CHANGED_EVENT);
	}

	/**
	 * Get the name part of the "use" channel.
	 */
	public synchronized String hostGetChannelName() {
		return mHostChannelName;
	}

	/**
	 * The name of the "host" channel which the user has selected.
	 */
	private String mHostChannelName;

	/**
	 * The object we use in notifications to indicate that the state of the
	 * "host" channel or its name has changed.
	 */
	public static final String HOST_CHANNEL_STATE_CHANGED_EVENT = "HOST_CHANNEL_STATE_CHANGED_EVENT";

	/**
	 * Set the status of the "use" channel. The AllJoyn Service part of the
	 * appliciation is expected to make this call to set the status to reflect
	 * the status of the underlying AllJoyn session.
	 */
	public synchronized void useSetChannelState(
			ConnectivityService.UseChannelState state) {
		mUseChannelState = state;
		notifyObservers(USE_CHANNEL_STATE_CHANGED_EVENT);
	}

	/**
	 * Get the state of the "use" channel.
	 */
	public synchronized ConnectivityService.UseChannelState useGetChannelState() {
		return mUseChannelState;
	}

	/**
	 * The "use" state which reflects the state of the part of the system
	 * related to using a remotely hosted chat channel. In a "real" application
	 * this kind of detail probably isn't appropriate, but we want to do so for
	 * this sample.
	 */
	private ConnectivityService.UseChannelState mUseChannelState = ConnectivityService.UseChannelState.IDLE;

	/**
	 * The name of the "use" channel which the user has selected.
	 */
	private String mUseChannelName = null;

	/**
	 * Set the name part of the "use" channel. Since we are going to "use" a
	 * channel that is implemented remotely and discovered through an AllJoyn
	 * FoundAdvertisedName, this must come from a list of advertised names.
	 * These names are our channels, and so we expect the GUI to choose from
	 * among the list of channels it retrieves from getFoundChannels().
	 * 
	 * Since we are talking about user-level interactions here, we are talking
	 * about the final segment of a well-known name representing a channel at
	 * this point.
	 */
	public synchronized void useSetChannelName(String name) {
		mUseChannelName = name;
		notifyObservers(USE_CHANNEL_STATE_CHANGED_EVENT);
	}

	/**
	 * Get the name part of the "use" channel.
	 */
	public synchronized String useGetChannelName() {
		return mUseChannelName;
	}

	/**
	 * The object we use in notifications to indicate that the state of the
	 * "use" channel or its name has changed.
	 */
	public static final String USE_CHANNEL_STATE_CHANGED_EVENT = "USE_CHANNEL_STATE_CHANGED_EVENT";

	/**
	 * This is the method that the "use" tab user interface calls when the user
	 * indicates that she wants to join a channel. The channel name must have
	 * been previously set with a call to setUseChannelName(). The "use" channel
	 * is the channel that we talk about in the "Use" tab. Since it's a remote
	 * channel in a remote bus attachment, we need to tell the AllJoyn Service
	 * to go join the corresponding session.
	 */
	public synchronized void useJoinChannel() {
		clearHistory();
		notifyObservers(USE_CHANNEL_STATE_CHANGED_EVENT);
		notifyObservers(USE_JOIN_CHANNEL_EVENT);
	}

	/**
	 * The object we use in notifications to indicate that user has requested
	 * that we join a channel in the "use" tab.
	 */
	public static final String USE_JOIN_CHANNEL_EVENT = "USE_JOIN_CHANNEL_EVENT";

	/**
	 * This is the method that the "use" tab user interface calls when the user
	 * indicates that she wants to leave a channel. Since we're talking about a
	 * remote channel corresponding to a session with a remote bus attachment,
	 * we needto tell the AllJoyn Service to leave the corresponding session.
	 */
	public synchronized void useLeaveChannel() {
		notifyObservers(USE_CHANNEL_STATE_CHANGED_EVENT);
		notifyObservers(USE_LEAVE_CHANNEL_EVENT);
	}

	/**
	 * The object we use in notifications to indicate that user has requested
	 * that we leave a channel in the "use" tab.
	 */
	public static final String USE_LEAVE_CHANNEL_EVENT = "USE_LEAVE_CHANNEL_EVENT";

	/**
	 * This is the method that the "host" tab user interface calls when the user
	 * has completed providing her preferences for hosting a channel.
	 */
	public synchronized void hostInitChannel() {
		notifyObservers(HOST_CHANNEL_STATE_CHANGED_EVENT);
		notifyObservers(HOST_INIT_CHANNEL_EVENT);
	}

	/**
	 * The object we use in notifications to indicate that user has requested
	 * that we initialize the host channel parameters in the "use" tab.
	 */
	public static final String HOST_INIT_CHANNEL_EVENT = "HOST_INIT_CHANNEL_EVENT";

	/**
	 * This is the method that the "host" tab user interface calls when the user
	 * indicates that she wants to start hosting a channel.
	 */
	public synchronized void hostStartChannel() {
		notifyObservers(HOST_CHANNEL_STATE_CHANGED_EVENT);
		notifyObservers(HOST_START_CHANNEL_EVENT);
	}

	/**
	 * The object we use in notifications to indicate that user has requested
	 * that we initialize the host channel parameters in the "use" tab.
	 */
	public static final String HOST_START_CHANNEL_EVENT = "HOST_START_CHANNEL_EVENT";

	/**
	 * This is the method that the "host" tab user interface calls when the user
	 * indicates that she wants to stop hosting a channel.
	 */
	public synchronized void hostStopChannel() {
		notifyObservers(HOST_CHANNEL_STATE_CHANGED_EVENT);
		notifyObservers(HOST_STOP_CHANNEL_EVENT);
	}

	/**
	 * The object we use in notifications to indicate that user has requested
	 * that we initialize the host channel parameters in the "use" tab.
	 */
	public static final String HOST_STOP_CHANNEL_EVENT = "HOST_STOP_CHANNEL_EVENT";

	/**
	 * Whenever our local user types a message, we need to send it out on the
	 * channel, which we do by calling addOutboundItem. This will eventually
	 * result in an AllJoyn Bus Signal being sent to the other participants on
	 * the channel. Since the sessions that implement the channel don't "echo"
	 * back to the source, we need to echo the message into our history.
	 */
	public synchronized void newLocalUserMessage(String message) {
		addInboundItem("Me", message);
		if (useGetChannelState() == ConnectivityService.UseChannelState.JOINED) {
			addOutboundItem(message);
		}
	}

	/**
	 * Whenever a user types a message into the channel, we expect the AllJoyn
	 * Service local to that user to send the message to everyone participating
	 * on the channel. At each participant, the messages arrive in the AllJoyn
	 * Service as a Bus Signal. The Service handles the signals and passes the
	 * associated messages on to us here. We expect the nickname to be the
	 * unique ID of the sending bus attachment. This is not very user friendly,
	 * but is convenient and guaranteed to be unique.
	 */
	public synchronized void newRemoteUserMessage(String nickname,
			String message) {
		addInboundItem(nickname, message);
	}

	final int OUTBOUND_MAX = 5;

	/**
	 * The object we use in notifications to indicate that the the user has
	 * entered a message and it is queued to be sent to the outside world.
	 */
	public static final String OUTBOUND_CHANGED_EVENT = "OUTBOUND_CHANGED_EVENT";

	/**
	 * The outbound list is the list of all messages that have been originated
	 * by our local user and are designed for the outside world.
	 */
	private List<String> mOutbound = new ArrayList<String>();

	/**
	 * Whenever the local user types a message for distribution to the channel
	 * it calls newLocalMessage. We are called to queue up the message and send
	 * a notification to all of our observers indicating that the we have
	 * something ready to go out. We expect that the AllJoyn Service will
	 * eventually respond by calling back in here to get items off of the queue
	 * and send them down the session corresponding to the channel.
	 */
	private void addOutboundItem(String message) {
		if (mOutbound.size() == OUTBOUND_MAX) {
			mOutbound.remove(0);
		}
		mOutbound.add(message);
		notifyObservers(OUTBOUND_CHANGED_EVENT);
	}

	/**
	 * Whenever the local user types a message for distribution to the channel
	 * it is queued to a list of outbound messages. The AllJoyn Service is
	 * notified and calls in here to get the outbound messages that need to be
	 * sent.
	 */
	public synchronized String getOutboundItem() {
		if (mOutbound.isEmpty()) {
			return null;
		} else {
			return mOutbound.remove(0);
		}
	}

	/**
	 * The object we use in notifications to indicate that the history state of
	 * the model has changed and observers need to synchronize with it.
	 */
	public static final String HISTORY_CHANGED_EVENT = "HISTORY_CHANGED_EVENT";

	/**
	 * Whenever a message comes in from the AllJoyn Service over its channel
	 * session, it calls in here. We just add the message item to the history
	 * list, with the "nickname" provided by Service. This is currently expected
	 * to be the unique name of the bus attachment originating the message. Once
	 * the message is saved in the history, a change notification will be sent
	 * to all observers indicating that the history has changed. The user
	 * interface part of the application is then expected to wake up and
	 * syncrhonize itself to the new history.
	 */
	private void addInboundItem(String nickname, String message) {
		addHistoryItem(nickname, message);
	}

	/**
	 * Don't keep an infinite amount of history. Although we don't want to admit
	 * it, this is a toy application, so we just keep a little history.
	 */
	final int HISTORY_MAX = 20;

	/**
	 * The history list is the list of all messages that have been originated or
	 * recieved by the "use" channel.
	 */
	private List<String> mHistory = new ArrayList<String>();

	/**
	 * Whenever a user in the channel types a message, it needs to result in the
	 * history being updated with the nickname of the user originating the
	 * message and the message itself. We keep a history list of a given maximum
	 * size just for general principles. This history list contains the local
	 * time at which the message was recived, the nickname of the user who
	 * originated the message and the message itself. We send a change
	 * notification to all observers indicating that the history has changed
	 * when we modify it.
	 */
	private void addHistoryItem(String nickname, String message) {
		if (mHistory.size() == HISTORY_MAX) {
			mHistory.remove(0);
		}

		DateFormat dateFormat = new SimpleDateFormat("HH:mm");
		Date date = new Date();
		// mHistory.add("[" + dateFormat.format(date) + "] (" + nickname + ") "
		// + message);

		// Don't add local history messages for now - TODO
		if (nickname.equalsIgnoreCase("me"))
			return;

		mHistory.add(message);
		notifyObservers(HISTORY_CHANGED_EVENT);
	}

	/**
	 * Clear the history list. Whenever a user joins a new channel, we want to
	 * get rid of any existing history to avoid confusion.
	 */
	private void clearHistory() {
		mHistory.clear();
		notifyObservers(HISTORY_CHANGED_EVENT);
	}

	/**
	 * Whenever a new message is added to the history list, an update
	 * notification is sent to all of the observers registered to this object
	 * that indicates that the history list has changed. When the observer hears
	 * that the list has changed, it calls in here to get the new contents.
	 * Since we have no idea how or when the caller is going to access or change
	 * the list, and we are deeply paranoid, we provide a deep copy.
	 */
	public synchronized List<String> getHistory() {
		List<String> clone = new ArrayList<String>(mHistory.size());
		for (String string : mHistory) {
			clone.add(new String(string));
		}
		return clone;
	}

	public synchronized String getLastMessage() {
		if (mHistory.size() == 0)
			return null;
		else
			return mHistory.get(mHistory.size() - 1);
	}

	/**
	 * This object is really the model of a model-view-controller architecture.
	 * The observer/observed design pattern is used to notify view-controller
	 * objects when the model has changed. The observed object is this object,
	 * the model. Observers correspond to the view-controllers which in this
	 * case are the Android Activities (corresponding to the use tab and the
	 * hsot tab) and the Android Service that does all of the AllJoyn work. When
	 * an observer wants to register for change notifications, it calls here.
	 */
	public synchronized void addObserver(AppObserver obs) {
		Log.v(TAG, "addObserver(" + obs + ")");
		if (mObservers.indexOf(obs) < 0) {
			mObservers.add(obs);
		}
	}

	/**
	 * When an observer wants to unregister to stop receiving change
	 * notifications, it calls here.
	 */
	public synchronized void deleteObserver(AppObserver obs) {
		Log.v(TAG, "deleteObserver(" + obs + ")");
		mObservers.remove(obs);
	}

	/**
	 * This object is really the model of a model-view-controller architecture.
	 * The observer/observed design pattern is used to notify view-controller
	 * objects when the model has changed. The observed object is this object,
	 * the model. Observers correspond to the view-controllers which in this
	 * case are the Android Activities (corresponding to the use tab and the
	 * Host tab) and the Android Service that does all of the AllJoyn work. When
	 * the model (this object) wants to notify its observers that some
	 * interesting event has happened, it calls here and provides an object that
	 * identifies what has happened. To keep things obvious, we pass a
	 * descriptive string which is then sent to all observers. They can decide
	 * to act or not based on the content of the string.
	 */
	public void notifyObservers(Object arg) {
		Log.v(TAG, "notifyObservers(" + arg + ")");
		for (AppObserver obs : mObservers) {
			Log.v(TAG, "notify observer = " + obs);
			obs.update(this, arg);
		}
	}

	/**
	 * The observers list is the list of all objects that have registered with
	 * us as observers in order to get notifications of interesting events.
	 */
	private List<AppObserver> mObservers = new ArrayList<AppObserver>();
}
