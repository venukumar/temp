package com.vendsy.bartsy.view;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.MessagesActivity;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.VenueActivity;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.utils.Utilities;
import com.vendsy.bartsy.utils.WebServices;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
/**
 * 
 * @author Seenu Malireddy
 *
 */
public class PeopleListView extends LinearLayout implements OnClickListener {
	
	private Activity activity;
	private BartsyApplication mApp;
	private Handler handler = new Handler();
	private LayoutInflater mInflater;
	
	static final String TAG = "PeopleListView";
	
	public PeopleListView(Activity activity, BartsyApplication mApp, LayoutInflater inflater, HashMap<String, Bitmap> cache) {
		super(activity);
		this.activity = activity;
		this.mApp = mApp;
		this.mInflater = inflater;
		
		// Set parameters for linear layout
		LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		setLayoutParams(params);
		setOrientation(LinearLayout.VERTICAL);
		
		// Set dividers
		setDividerDrawable(getResources().getDrawable(R.drawable.div_light_grey));
		setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE | LinearLayout.SHOW_DIVIDER_BEGINNING | LinearLayout.SHOW_DIVIDER_END);
		
//		setPadding(5, 5, 5, 5);
		
		loadPeopleList(cache);
		
	}
		

	/**
	 * To get CheckedIn People from the server
	 */
	public void loadPeopleList(final HashMap<String, Bitmap> cache) {

		Log.v(TAG, "PeopleSectionFragment.loadPeopleList()");
		
		if (mApp.mActiveVenue == null || mApp.mProfile == null) {
			// This is when Internet is lost, user tried to load their profile and couldn't and now is trying to see if they can check their orders. They can.
			Toast.makeText(mApp, "Please check your internet connection", Toast.LENGTH_SHORT).show();
			return;
		}
		
		try {

			new Thread() {

				public void run() {

					String response = null;
					if (mApp.mActiveVenue == null) {
						return;
					}
					// Post data for to get the checkedIn people
					JSONObject postData = new JSONObject();
					try {
						postData.put("venueId", mApp.mActiveVenue.getId());
						postData.put("bartsyId", mApp.mProfile.getBartsyId());
					} catch (JSONException e) {
						e.printStackTrace();
					}
					// Webservice call for to get the checkedIn people
					try {
						response = WebServices.postRequest(WebServices.URL_LIST_OF_CHECKED_IN_USERS, postData, mApp);
					} catch (Exception e) {
						e.printStackTrace();
					}
					// CheckedIn people web service Response handling
					if (response != null)
						processCheckedInUsersResponse(response, cache);

				};
			}.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * CheckedIn people web service Response handling
	 * 
	 * @param response
	 */
	private synchronized void processCheckedInUsersResponse(String response, final HashMap<String, Bitmap> cache) {
		// Save the list of people and use it as an image cache, resetting the global structure
		ArrayList<UserProfile> knownPeople = mApp.mPeople;

		mApp.mPeople = new ArrayList<UserProfile>();

		try {
			JSONObject peopleData = new JSONObject(response);

			if (peopleData.has("checkedInUsers")) {
				
				// Get list of people from API call. If a person is known, copy known version as an optimization
				JSONArray array = peopleData.getJSONArray("checkedInUsers");
				for (int i = 0; i < array.length(); i++) {

					// Construct user profile from json
					JSONObject json = array.getJSONObject(i);
					UserProfile profile = new UserProfile(json);

					// Add profile to the people list
					mApp.addPerson(profile);
				}

				// Call UI thread and display checkedIn people list
				handler.post(new Runnable() {

					@Override
					public void run() {
						
														
						// Make sure the list view is empty
						removeAllViews();
						
						// Add any existing people in the layout, one by one
						
						Log.v(TAG, "mApp.mPeople list size = " + mApp.mPeople.size());
						
						for (UserProfile profile : mApp.mPeople) {
							Log.v(TAG, "Adding a user item to the layout");
							View view = profile.listView(mInflater, PeopleListView.this, cache);
							checkFacebookFriends(view);
							addView(view);
						};

						// Update people count in people tab
						if(activity!=null && activity instanceof VenueActivity){
							((VenueActivity)activity).updatePeopleCount();
						}
					}
				});

			} else {

				Log.v(TAG, "checked in users not found !!!! ");
			}

		} catch (JSONException e) {
			e.printStackTrace();
			// Reset as previous
			mApp.mPeople = knownPeople;
		}
	}

	/**
	 * Fetch the facebook user's friend list from shared preference. Parse the
	 * retrieved friend list Check whether friends on facebook
	 * 
	 * @param view
	 */
	protected void checkFacebookFriends(View view) {
		String result = Utilities.loadPref(activity,
				R.string.prefs_facebook_friends, "");
		JSONObject responseObject = null;
		JSONArray friendListArray = null;
		// try parse the string to a JSON object
		try {
			responseObject = new JSONObject(result);
			friendListArray = responseObject.getJSONArray("data");
			Log.i("RESULT", "JSON ARRAY" + friendListArray.toString());
		} catch (JSONException e) {
			Log.e("ERROR", "Error parsing data " + e.toString());
			return;
		}

		UserProfile profile = (UserProfile) view.getTag();
		profile.updateFacebookFriends(friendListArray, view);

	}

	@Override
	public void onClick(View v) {
		// When user selects on the profile item in the list
		UserProfile profile = (UserProfile)v.getTag();
		selectedUserProfile(profile);
	}
	
	/**
	 * There is no implementation for this method and implementation is provided by the Activity or dialog which is used
	 * 
	 * @param profile
	 */
	protected void selectedUserProfile(UserProfile profile) {
		
	}
	
}
