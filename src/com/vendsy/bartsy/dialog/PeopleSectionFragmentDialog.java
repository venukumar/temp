/**
 * 
 */
package com.vendsy.bartsy.dialog;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;

/**
 * @author Seenu Malireddy
 * 
 */
public class PeopleSectionFragmentDialog extends SherlockDialogFragment{

	static final String TAG = "PeopleSectionFragmentDialog";
	
	View mRootView = null;
	public LinearLayout mPeopleListView = null;
	public BartsyApplication mApp = null;
	private Handler handler = new Handler();

	private LayoutInflater mInflater;
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Create dialog and set animation styles
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());// ,
																				// R.style.DrinkDialog);

		// Get the layout inflater
		mInflater = getActivity().getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		mRootView = mInflater.inflate(R.layout.users_main, null);
		
		
		mPeopleListView = (LinearLayout) mRootView.findViewById(R.id.view_singles);
		
		// Make sure the fragment pointed to by the activity is accurate
		mApp = (BartsyApplication) getActivity().getApplication();
		
		builder.setView(mRootView);
		
		updatePeopleView();
		
		return builder.create();

	}

	/**
	 * Updates the people view from scratch
	 */
	
	public void updatePeopleView () {
		
		Log.v(TAG, "About to update people list view");

		if (mPeopleListView == null)
			return;

		// Load the people currently present in the venue
		loadPeopleList();
	}

	
	/**
	 * To get CheckedIn People from the server
	 */
	private void loadPeopleList() {

		Log.v(TAG, "PeopleSectionFragmentDialog.loadPeopleList()");

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
	private void processCheckedInUsersResponse(String response) {

		// Save the list of people and use it as an image cache, resetting the global structure
		ArrayList<UserProfile> knownPeople = (ArrayList<UserProfile>) mApp.mPeople.clone();
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
					if (found) {
						// Profile found. Remove it from known people list
						knownPeople.remove(profile);
					} else {
						// Create new instance for profile - this is for now incomplete!!

						profile = new UserProfile();
						profile.setBartsyId(bartsyID);
						profile.setNickname(nickName);
						profile.setImagePath(imagepath);
					}
					
					// Add profile (new or old) to the existing people list
					mApp.mPeople.add(profile);
				}

				// Call UI thread and display checkedIn people list
				handler.post(new Runnable() {

					@Override
					public void run() {
						
						// Avoid null pointer exceptions...
						if (mPeopleListView == null) {
							Log.e(TAG, "Called processCheckedInUsers() with a null mPeopleListView");
							return;
						}
						
						// Make sure the list view is empty
						mPeopleListView.removeAllViews();

						// Add any existing people in the layout, one by one
						
						Log.v(TAG, "mApp.mPeople list size = " + mApp.mPeople.size());

						for (UserProfile profile : mApp.mPeople) {
							Log.v(TAG, "Adding a user item to the layout");
							profile.view = mInflater.inflate(R.layout.user_item, null);
							profile.updateView(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									selectedProfile((UserProfile)v.getTag());
								}
							}); 
							// sets up view specifics and sets listener to this
							mPeopleListView.addView(profile.view);
						};

					}
				});

			} else {

				Log.v(TAG, "checked in users not found !!!! ");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		// Cleanup
		knownPeople.clear();
	}
	
	

	
	/**
	 * There is no implementation for this method and implementation is provided by the DrinkDialogFragment
	 * 
	 * @see DrinkDialogFragment
	 * 
	 * @param profile
	 */
	protected void selectedProfile(UserProfile profile) {
		dismiss();
	}

}
