/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vendsy.bartsy;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.adapter.VenueListViewAdapter;
import com.vendsy.bartsy.model.Venue;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.Utilities;
import com.vendsy.bartsy.utils.WebServices;

/**
 * This preference activity has in its manifest declaration an intent filter for
 * the ACTION_MANAGE_NETWORK_USAGE action. This activity provides a settings UI
 * for users to specify network settings to control data usage.
 */
public class MapActivity extends Activity implements LocationListener,
		OnClickListener {
	private List<Venue> venues;
	private GoogleMap mMap = null;
	private LocationManager locationManager;
	private static final long MIN_TIME = 400;
	private static final float MIN_DISTANCE = 1000;
	private Handler handler = new Handler();
	BartsyApplication mApp = null;

	Activity activity = this;
	private static final String TAG = "MapActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.maps_main);

		// Set the action bar to enable back navigation
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Set up the pointer to the main application
		mApp = (BartsyApplication) getApplication();

		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.

			mMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();

			// Make sure we got a map, if not exit
			if (mMap == null) {
				Toast.makeText(this, "Could not connect to Google maps",
						Toast.LENGTH_SHORT).show();
				finish();
				return;
			}
		}

		mMap.setMyLocationEnabled(true);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

		// To obtain list view from the SupportMapFragment.
		final ListView venueList = (ListView) findViewById(R.id.checkInListView);
		venueList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// It will invoke when the venue list item selected
				Venue venue = venues.get(arg2);
				venueSelectedAction(venue);
			}
		});

		// To call web service in background
		new Thread() {

			public void run() {
				loadVenuesFromServer(venueList);
			}
		}.start();

	}

	/**
	 * To load venues information from the server
	 * 
	 * @param venueList
	 */
	protected void loadVenuesFromServer(final ListView venueList) {

		String response = WebServices.getVenueList(MapActivity.this);
		if (response != null) {
			venues = getVenueListResponse(response);
			// Handler for UI thread
			handler.post(new Runnable() {

				@Override
				public void run() {
					updateListView(venueList);
				}
			});
		}
	}
	
	/**
	 * To parse JSON format to list of venues
	 * 
	 * @param response
	 * @return
	 */
	private List<Venue> getVenueListResponse(String response) {
		List<Venue> list = new ArrayList<Venue>();
		try {
			JSONArray array = new JSONArray(response);
			for (int i = 0; i < array.length(); i++) {
				JSONObject venueObject = array.getJSONObject(i);
				String venueName = venueObject.has("venueName") ? venueObject
						.getString("venueName") : "";
				String venueId = venueObject.getString("venueId");
				String latitude = venueObject.getString("latitude");
				String longitude = venueObject.getString("longitude");
				String address = venueObject.getString("address");

				// To set all information to the venue object
				Venue venueProfile = new Venue();
				venueProfile.setId(venueId);
				venueProfile.setName(venueName);
				venueProfile.setLatitude(latitude);
				venueProfile.setLongitude(longitude);
				venueProfile.setAddress(address);
				list.add(venueProfile);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}


	/**
	 * To update list view with venue information and update markers in the Map
	 * view
	 * 
	 * @param venueList
	 */
	protected void updateListView(ListView venueList) {

		if (venueList == null || venues == null) {
			return;
		}
		// To set the adapter to the list view
		VenueListViewAdapter customAdapter = new VenueListViewAdapter(
				MapActivity.this, R.layout.map_list_item, venues);

		venueList.setAdapter(customAdapter);

		// To add markers to the map view
		for (int i = 0; i < venues.size(); i++) {
			Venue venue = venues.get(i);

			LatLng coord = new LatLng(Float.valueOf(venue.getLatitude()),
					Float.valueOf(venue.getLongitude()));
			mMap.addMarker(new MarkerOptions().position(coord)
					.title(venue.getName()).snippet("People checked in: " + i));
		}
	}

	/**
	 * Invokes when the venue selected in the list view
	 * 
	 * @param venue
	 */

	// We're using this variable as a message buffer with the background service checking user in
	Venue mVenue = null;
	
	protected void venueSelectedAction(Venue venue) {
		
		Log.v(TAG, "venueSelectedAction(" + venue.getId() + ")");

		// Initialize message buffer for alertBox() and userCheckinAction()
		mVenue = venue;

		// Check user into venue after confirmation
		
		if (mApp.mActiveVenue == null) {
			// We're not locally checked in, we don't need to display alerts so we 
			// directly go to check the user in
			invokeUserCheckInSyscall();
		} else if (venue.getId().trim().equalsIgnoreCase(mApp.mActiveVenue.getId().trim())) {
			// Selected venue was already active, no need to do anything more
			Intent intent = new Intent(activity, VenueActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		} else if (mApp.mOrders.size() > 0) {
			// We already have a local active venue different than the one selected
			userCheckOutAlert("You are already checked-in an have open orders placed at" + mApp.mActiveVenue.getName() +
					". If you checkout they will be cancelled and you will still be charged. Are you sure you want to check out?", venue);
		} else if (mApp.mActiveVenue != null) {
			// Require to ask confirmation to check in to new venue
			userCheckOutAlert("You are already checked in " + mApp.mActiveVenue.getName()
					+ ".Do you want to check out and check in " + venue.getName() + " instead?", venue);
		}
	}
	


	/**
	 * To display confirmation alert box when the user selects venue in the list
	 * view
	 * 
	 * @param message
	 * @param venue
	 */
	private void userCheckOutAlert(String message, final Venue venue) {

		AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
		builder.setCancelable(true);
		builder.setTitle("Please Confirm!");
		builder.setInverseBackgroundForced(true);
		builder.setMessage(message);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
				invokeUserCheckInSyscall();

			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();

	}

	/**
	 * Invokes this action when the user selects on the venue and calls check in
	 * web service
	 * 
	 * @param Uses a local variable "mVenue" as a parameter buffer
	 * 
	 */
	
	String errorMessage = null;
	
	protected void invokeUserCheckInSyscall() {
		new Thread() {
			public void run() {
				
				// Load the venue paramenter from the local parameter buffer
				final Venue venue = mVenue;
				
				// Invoke the user checkin syscall
				String response = WebServices.userCheckInOrOut(MapActivity.this, venue.getId(), Constants.URL_USER_CHECK_IN);

				if (response != null) {
					try {
						JSONObject json = new JSONObject(response);
						String errorCode = json.getString("errorCode");
						errorMessage = json.has("errorMessage") ? json.getString("errorMessage") : "";

						if (errorCode.equalsIgnoreCase("0")) {
							
							// Host checked user in successfully. Check the user in locally too.

							handler.post(new Runnable() {
								public void run() {
									
									// Check into the venue locally
									mApp.userCheckIn(venue);

									// Start venue activity and finish this activity
									Intent intent = new Intent(activity, VenueActivity.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(intent);
									finish();
								}
							});
						} else {
							
							// An error has occurred and the user was not checked in - Toast it

							handler.post(new Runnable() {
								public void run() {
									Toast.makeText(MapActivity.this, "Error checking in (" + errorMessage + 
											"). Please try again or restart application.", Toast.LENGTH_SHORT).show();
								}
							});
						}

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			};
		}.start();
	}

	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		LatLng latLng = new LatLng(location.getLatitude(),
				location.getLongitude());
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,
				15);
		mMap.animateCamera(cameraUpdate);
		locationManager.removeUpdates(this);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onClick(View arg0) {
		// Just start the bartsy activity regardless of where the use clicks for
		// now
		this.startActivity(new Intent().setClass(this, VenueActivity.class));
	}


	
}
