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
import com.vendsy.bartsy.model.Profile;
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

		Log.i("Bartsy", "PeopleSectionFragment.onCreateView()");

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
		
		Log.i("Bartsy", "About to update people list view");

		if (mPeopleListView == null)
			return;

		// For now remove list of people and do it from scratch. This is inefficient because it requires loading pictures for people we may already have in the list
		mApp.mPeople.clear();
		
		// Load the people currently present in the venue
		loadPeopleList();
	}

	
	/**
	 * To get CheckedIn People from the server
	 */
	private void loadPeopleList() {

		Log.i(Constants.TAG, "PeopleSectionFragment.loadPeopleList()");

		try {

			new Thread() {

				public void run() {

					String response = null;
					if (mApp.activeVenue == null) {
						return;
					}
					// Post data for to get the checkedIn people
					JSONObject postData = new JSONObject();
					try {
						postData.put("venueId", mApp.activeVenue.getId());
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

		try {
			JSONObject peopleData = new JSONObject(response);

			if (peopleData.has("checkedInUsers")) {
				// Parse json format
				JSONArray array = peopleData.getJSONArray("checkedInUsers");
				for (int i = 0; i < array.length(); i++) {
					String name = null, gender = null, imagepath = null;
					JSONObject json = array.getJSONObject(i);
					if (json.has("name"))
						name = json.getString("name");
					if (json.has("gender"))
						gender = json.getString("gender");

					if (json.has("userImage")) {
						imagepath = json.getString("userImage");
					}
					// Create new instance for profile
					Profile profile = new Profile(null, name, null, null, null,
							null, imagepath);
					// Add profile to the existing people list
					mApp.mPeople.add(profile);

				}
				// To call UI thread and display checkedIn people list
				handler.post(new Runnable() {

					@Override
					public void run() {
						// Make sure the list view is empty
						mPeopleListView.removeAllViews();

						// Add any existing people in the layout, one by one
						
						Log.i(Constants.TAG, "mApp.mPeople list size = " + mApp.mPeople.size());

						for (Profile profile : mApp.mPeople) {
							Log.i("Bartsy", "Adding a user item to the layout");
							profile.view = mInflater.inflate(R.layout.user_item, mContainer, false);
							profile.updateView(mActivity.mPeopleFragment); // sets up view specifics and sets listener to this
							mPeopleListView.addView(profile.view);
						};

						// Update people count in people tab
						mActivity.updatePeopleCount();
					}
				});

			} else {

				Log.i(Constants.TAG, "checked in users not found !!!! ");
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
	
	
	@Override 
	public void onDestroy() {
		super.onDestroy();
		
		Log.i(Constants.TAG, "PeopleSectionFragment.onDestroy()");

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
