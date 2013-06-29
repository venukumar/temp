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
import java.util.Collections;
import java.util.Comparator;
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
import com.vendsy.bartsy.adapter.VenueListViewAdapter;
import com.vendsy.bartsy.model.Venue;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;

/**
 * This preference activity has in its manifest declaration an intent filter for
 * the ACTION_MANAGE_NETWORK_USAGE action. This activity provides a settings UI
 * for users to specify network settings to control data usage.
 */
public class MapActivity extends Activity implements LocationListener,
		OnClickListener {
	private ArrayList<Venue> venues = new ArrayList<Venue>();
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
		
		// Set the view
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
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this);
		
		
		// To obtain list view from the SupportMapFragment.
		final ListView venueList = (ListView) findViewById(R.id.checkInListView);
		venueList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				// It will invoke when the venue list item selected
				Venue venue = venues.get(arg2);
				venueSelectedAction(venue);
			}
		});

		// Load venues in the background
		loadVenuesFromServer(venueList);
	}


	/**
	 * To load venues information from the server
	 * 
	 * @param venueList
	 */
	protected void loadVenuesFromServer(final ListView venueList) {
		new Thread() {
			public void run() {
				String response = WebServices.getVenueList(MapActivity.this, mApp.loadBartsyId());
				if (response != null) {
					venues = getVenueListResponse(response);
					// Handler for UI thread
					handler.post(new Runnable() {
		
						@Override
						public void run() {
							updateListView(venueList);

							onLocationChanged(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));

						}
					});
				}
			}
		}.start();

	}
	
	
	/**
	 * To parse JSON format to list of venues
	 * 
	 * @param response
	 * @return
	 */
	private ArrayList<Venue> getVenueListResponse(String response) {

		ArrayList<Venue> list = new ArrayList<Venue>();
		
		JSONArray array;
		try {
			array = new JSONArray(response);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
			
		for (int i = 0; i < array.length(); i++) {

			try {

				JSONObject json = array.getJSONObject(i);

				Venue venue = new Venue();
				venue.setId(json.getString("venueId"));
				venue.setName(json.getString("venueName"));
				venue.setLatitude(json.getString("latitude"));
				venue.setLongitude(json.getString("longitude"));
				venue.setAddress(json.getString("address"));
				venue.setUserCount(json.getInt("checkedInUsers"));
				venue.setOrderTimeout(json.getInt("cancelOrderTime"));
				if (json.getInt("wifiPresent") != 0) {
					venue.setWifiName(json.getString("wifiName"));
					venue.setWifiPassword(json.getString("wifiPassword"));
					venue.SetWifiTypeOfAuthentication(json.getString("typeOfAuthentication"));
				}
				venue.setStatus(json.getString("venueStatus"));

				list.add(venue);

			} catch (JSONException e) {
				e.printStackTrace();
			}

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
				MapActivity.this, R.layout.map_list_item, venues, locationManager);

		venueList.setAdapter(customAdapter);

		// To add markers to the map view
		for (int i = 0; i < venues.size(); i++) {
			Venue venue = venues.get(i);

			LatLng coord = new LatLng(Float.valueOf(venue.getLatitude()), Float.valueOf(venue.getLongitude()));
			mMap.addMarker(new MarkerOptions().position(coord).title(venue.getName())
					.snippet(venue.getUserCount() == 1 ? "person" : "people" + 
							" checked in: " + venue.getUserCount()));
		}
	}

	/**
	 * Invokes when the venue selected in the list view
	 * 
	 * @param venue
	 */

	// We're using this variable as a message buffer with the background service checking user in
//	Venue mVenue = null;
	
	protected void venueSelectedAction(Venue venue) {
		
		Log.v(TAG, "venueSelectedAction(" + venue.getId() + ")");
		
		// Don't do anything if the venue is closed
		if (venue.getStatus().equalsIgnoreCase("CLOSED"))
			return;

		// Check user into venue after confirmation
		if (mApp.mActiveVenue == null) {
			// We're not locally checked in, we don't need to display alerts so we 
			// directly go to check the user in
			invokeUserCheckInSyscall(venue);
		} else if (venue.getId().trim().equalsIgnoreCase(mApp.mActiveVenue.getId().trim())) {
			// Selected venue was already active, no need to do anything more
			Intent intent = new Intent(activity, VenueActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		} else if (mApp.getOrderCount() > 0) {
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
				invokeUserCheckInSyscall(venue);

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
	
	protected void invokeUserCheckInSyscall(final Venue venue) {
		new Thread() {
			public void run() {
				
				// Invoke the user checkin syscall
				String response = WebServices.userCheckInOrOut(MapActivity.this, mApp.loadBartsyId(), venue.getId(), Constants.URL_USER_CHECK_IN);

				if (response != null) {
					try {
						JSONObject json = new JSONObject(response);
						String errorCode = json.getString("errorCode");
						errorMessage = json.has("errorMessage") ? json.getString("errorMessage") : "";

						if (errorCode.equalsIgnoreCase("0")) {
							
							// Host checked user in successfully. Check the user in locally too.

							// Set venue parameters
							if (json.has("userCount"))
								venue.setUserCount(json.getInt("userCount"));

							// Check into the venue locally
							mApp.userCheckIn(venue);

							handler.post(new Runnable() {
								public void run() {
									

									// Start venue activity and finish this activity
									Intent intent = new Intent(activity, VenueActivity.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(intent);
									finish();
								}
							});
						} else {
							
							// An error has occurred and the user was not checked in - Toast it
							
							mApp.userCheckOut();

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

		Log.v(TAG, "onLocationChanged()");
		
		try {
			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
			mMap.animateCamera(cameraUpdate);
			locationManager.removeUpdates(this);

			// Sort the venues by distance
			if (venues != null)
				Collections.sort(venues, new VenueSorter(location));
			
			// Invalidate the venue list view so that it get redrawn with the new location used to compute distances
			((ListView) findViewById(R.id.checkInListView)).invalidate();
		} catch (Exception e) {
			Log.e(TAG, "Could not update new location " + location);
		}
	}
	
	Location currentLocation;
	
	public class VenueSorter implements Comparator<Venue>
	{
		Location currentLocation;
		VenueSorter (Location currentLocation) {
			this.currentLocation = currentLocation;
		}
		
		@Override
		public int compare(Venue arg0, Venue arg1) {
			
			Location loc0 = new Location(LocationManager.NETWORK_PROVIDER);
			loc0.setLatitude(Double.parseDouble(arg0.getLatitude()));
			loc0.setLongitude(Double.parseDouble(arg0.getLongitude()));
			Float d0 = loc0.distanceTo(currentLocation);

			Location loc1 = new Location(LocationManager.NETWORK_PROVIDER);
			loc1.setLatitude(Double.parseDouble(arg1.getLatitude()));
			loc1.setLongitude(Double.parseDouble(arg1.getLongitude()));
			Float d1 = loc1.distanceTo(currentLocation);

			return d0.compareTo(d1);
		}
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
