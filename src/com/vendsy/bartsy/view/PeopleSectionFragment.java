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
 
		if (mRootView == null) {
			mRootView = inflater.inflate(R.layout.users_main, container, false);
			mPeopleListView = (LinearLayout) mRootView.findViewById(R.id.view_singles);
			
			mInflater = inflater;
			mContainer = container; 

			// Add any existing people in the layout, one by one
			Log.d("Bartsy", "About to add people list to the View");
			Log.d("Bartsy", "mUsers list size = " + mApp.mPeople.size());

			for (Profile profile : mApp.mPeople) {
				Log.d("Bartsy", "Adding a user item to the layout");
				profile.view = (View) mInflater.inflate(R.layout.user_item, mContainer, false);
				profile.updateView(this); // sets up view specifics and sets listener to this
				mPeopleListView.addView(profile.view);
			}
		}
        return mRootView;
	}
	
	@Override 
	public void onDestroyView() {
		super.onDestroyView();
		mRootView = null;
		mPeopleListView = null;
		mInflater = null;
		mContainer = null;
	}
	
	public void addPerson(Profile person) {
		
		Log.d("Bartsy", "Adding new person to people list: " + person.userID);
		
		// Check to see if person is already "here" and don't add them if so
		for (Profile p : mApp.mPeople) {
			if (p.userID.equalsIgnoreCase(person.userID)) {
				Log.d("Bartsy", "Profile already exists in the list, skip adding it");
				return;
			}
		}
		
		if (mPeopleListView == null) {
			
			Log.d("Bartsy", "The people view in null. Adding to the people list only");
			
			mApp.mPeople.add(person);
		} else {

			Log.d("Bartsy", "The people view in not null. Adding to the people list and the view");

			mApp.mPeople.add(person);
			person.view = (View) mInflater.inflate(R.layout.user_item, mContainer, false);
			person.updateView(this);

			mPeopleListView.addView(person.view);
			
			Log.d("Bartsy", "Added new person to people list View");
		}
		Log.d("Bartsy", "mPeople list size = " + mApp.mPeople.size());
	}

	
    @Override
    public void onClick(View v) {
        // Create an instance of the dialog fragment and show it
        PeopleDialogFragment dialog = new PeopleDialogFragment();
        dialog.mUser = (Person) v.getTag();
        dialog.show(getActivity().getSupportFragmentManager(), "User profile");
    }
	
}
