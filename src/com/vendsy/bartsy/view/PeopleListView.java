package com.vendsy.bartsy.view;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.VenueActivity;
import com.vendsy.bartsy.dialog.DrinkDialogFragment;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
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
	
	public PeopleListView(Activity activity, BartsyApplication mApp, LayoutInflater inflater) {
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
		
		loadPeopleList();
		
	}
		

	/**
	 * To get CheckedIn People from the server
	 */
	public void loadPeopleList() {

		Log.v(TAG, "PeopleSectionFragment.loadPeopleList()");

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
					} catch (JSONException e) {
						e.printStackTrace();
					}
					// Webservice call for to get the checkedIn people
					try {
						response = WebServices.postRequest(
								Constants.URL_LIST_OF_CHECKED_IN_USERS,
								postData, mApp.getApplicationContext());
					} catch (Exception e) {
						e.printStackTrace();
					}
					// CheckedIn people web service Response handling
					if (response != null)
						processCheckedInUsersResponse(response);

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
	private synchronized void processCheckedInUsersResponse(String response) {
		// Save the list of people and use it as an image cache, resetting the global structure
				ArrayList<UserProfile> knownPeople = mApp.mPeople;

				mApp.mPeople = new ArrayList<UserProfile>();

				try {
					JSONObject peopleData = new JSONObject(response);

					if (peopleData.has("checkedInUsers")) {
						
						// Get list of people from API call. If a person is known, copy known version as an optimization
						JSONArray array = peopleData.getJSONArray("checkedInUsers");
						for (int i = 0; i < array.length(); i++) {
							String nickName = null, gender = null, imagepath = null;
							String bartsyID = null;
							JSONObject json = array.getJSONObject(i);
							if (json.has("nickName"))
								nickName = json.getString("nickName");
							if (json.has("gender"))
								gender = json.getString("gender");
							if (json.has("bartsyId"))
								bartsyID = json.getString("bartsyId");
							if (json.has("userImagePath")) {
								imagepath = json.getString("userImagePath");
							}
							
							// Go over the list of people in the global structure looking for images
							UserProfile profile = null;
							boolean found = false;
							for (UserProfile p : knownPeople) {
								if (p.getBartsyId().equalsIgnoreCase(bartsyID) && p.hasImage()) {
									// Found the profile and it has an image. Shamelessly reuse it
									Log.v(TAG, "Reusing image for profile " + bartsyID);
									profile = p;
									found = true;
									break;
								}
							}
							
							// If an existing profile was not found, create one
							if (!found) {
								// Create new instance for profile - this is for now incomplete!!

								profile = new UserProfile();
								profile.setBartsyId(bartsyID);
								profile.setNickname(nickName);
								profile.setImagePath(imagepath);
							}
							
							// Add profile (new or old) to the existing people list
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
									profile.view = mInflater.inflate(R.layout.user_item, null);
									profile.updateView(PeopleListView.this); // sets up view specifics and sets listener to this
									addView(profile.view);
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