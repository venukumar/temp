/**
 * 
 */
package com.vendsy.bartsy.view;

import java.util.ArrayList;

import com.google.android.gms.plus.model.people.Person;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.dialog.PeopleDialogFragment;
import com.vendsy.bartsy.model.Profile;

import android.os.Bundle;
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
 
		Log.i("Bartsy", "PeopleSectionFragment.onCreateView()");

		mInflater = inflater;
		mContainer = container; 
		mRootView = inflater.inflate(R.layout.users_main, container, false);
		mPeopleListView = (LinearLayout) mRootView.findViewById(R.id.view_singles);
		
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

		// Make sure the list view is empty
		mPeopleListView.removeAllViews();

		// Add any existing people in the layout, one by one
		
		Log.i("Bartsy", "mApp.mPeople list size = " + mApp.mPeople.size());

		for (Profile profile : mApp.mPeople) {
			Log.i("Bartsy", "Adding a user item to the layout");
			profile.view = mInflater.inflate(R.layout.user_item, mContainer, false);
			profile.updateView(this); // sets up view specifics and sets listener to this
			mPeopleListView.addView(profile.view);
		}
	}

	@Override 
	public void onDestroyView() {
		super.onDestroyView();
		mRootView = null;
		mPeopleListView = null;
		mInflater = null;
		mContainer = null;
	}
	
	
    @Override
    public void onClick(View v) {
        // Create an instance of the dialog fragment and show it
        PeopleDialogFragment dialog = new PeopleDialogFragment();
        dialog.mUser = (Person) v.getTag();
        dialog.show(getActivity().getSupportFragmentManager(), "User profile");
    }
	
}
