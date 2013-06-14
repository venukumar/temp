/**
 * 
 */
package com.vendsy.bartsy.view;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.plus.model.people.Person;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.VenueActivity;
import com.vendsy.bartsy.dialog.PeopleDialogFragment;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

/**
 * @author peterkellis
 * 
 */
public class PeopleSectionFragment extends Fragment implements OnClickListener {

	static final String TAG = "PeopleSectionFragment";
	
	View mRootView = null;
	LayoutInflater mInflater = null;
	ViewGroup mContainer = null;
	public LinearLayout mPeopleListView = null;
	public BartsyApplication mApp = null;
	private VenueActivity mActivity = null;
	private Handler handler = new Handler();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.v(TAG, "PeopleSectionFragment.onCreateView()");

		mInflater = inflater;
		mContainer = container;
		mRootView = inflater.inflate(R.layout.users_main, container, false);
		mPeopleListView = (LinearLayout) mRootView.findViewById(R.id.view_singles);
		
		// Make sure the fragment pointed to by the activity is accurate
		mApp = (BartsyApplication) getActivity().getApplication();
		mActivity = (VenueActivity) getActivity();
		((VenueActivity) getActivity()).mPeopleFragment = this;
		
		updatePeopleView();
		

		return mRootView;
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
					String name = null, gender = null, imagepath = null;
					String bartsyID = null;
					JSONObject json = array.getJSONObject(i);
					if (json.has("nickName"))
						name = json.getString("nickName");
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
						profile.setNickname(name);
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
							profile.view = mInflater.inflate(R.layout.user_item, mContainer, false);
							profile.updateView(mActivity.mPeopleFragment); // sets up view specifics and sets listener to this
							mPeopleListView.addView(profile.view);
						};

						// Update people count in people tab
						mActivity.updatePeopleCount();
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
	
	
	@Override 
	public void onDestroy() {
		super.onDestroy();
		
		Log.v(TAG, "PeopleSectionFragment.onDestroy()");

		mRootView = null;
		mPeopleListView = null;
		mInflater = null;
		mContainer = null;

		// Because the fragment may be destroyed while the activity persists, remove pointer from activity
		((VenueActivity) getActivity()).mPeopleFragment = null;
	}

	@Override
	public void onClick(View v) {
		// Create an instance of the dialog fragment and show it
		// PeopleDialogFragment dialog = new PeopleDialogFragment();
		// dialog.mUser = (Person) v.getTag();
		// dialog.show(getActivity().getSupportFragmentManager(),
		// "User profile");
	}

}
