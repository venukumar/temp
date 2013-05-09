/**
 * 
 */
package com.kellislabs.bartsy;

import java.util.ArrayList;

import com.google.android.gms.plus.model.people.Person;
import com.kellislabs.bartsy.model.Profile;

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
	 LinearLayout mPeopleListView = null;
	public BartsyApplication mApp = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
 
		if (mRootView == null) {
			mRootView = inflater.inflate(R.layout.users_main, container, false);
			mPeopleListView = (LinearLayout) mRootView.findViewById(R.id.view_singles);
			
			mInflater = inflater;
			mContainer = container; 
		
//			loadUsers();
//			ArrayList<User> mUsers = new ArrayList<User>();
			
			// Add any existing people in the layout, one by one
			Log.d("Bartsy", "About to add people list to the View");
			Log.d("Bartsy", "mUsers list size = " + mApp.mPeople.size());

			for (Profile profile : mApp.mPeople) {
				Log.d("Bartsy", "Adding a user item to the layout");
				profile.view = (View) mInflater.inflate(R.layout.user_item, mContainer, false);
				profile.updateView(this); // sets up view specifics and sets listener to this
				mPeopleListView.addView(profile.view);
//				((Bartsy)getActivity()).appendStatus("Added new view");
			}
			
			// These toggle buttons implement effectively a tab with the additional ability to hide all content
			
	        mRootView.findViewById(R.id.button_singles).setOnClickListener(
	                new View.OnClickListener() {
	                    @Override
	                    public void onClick(View arg0) {
	                    	ToggleButton b = (ToggleButton) arg0;
	                    	if (b.isChecked()) {
	                    		mRootView.findViewById(R.id.view_singles).setVisibility(View.VISIBLE);
	//                    		mRootView.findViewById(R.id.friends).setVisibility(View.GONE);
	//                    		((ToggleButton) mRootView.findViewById(R.id.button_friends)).setChecked(false);
	                    	} else {
	                    		mRootView.findViewById(R.id.view_singles).setVisibility(View.GONE);                    		
	                    	}
	                    }});
			
	        mRootView.findViewById(R.id.button_friends).setOnClickListener(
	                new View.OnClickListener() {
	                    @Override
	                    public void onClick(View arg0) {
	                    	ToggleButton b = (ToggleButton) arg0;
	                    	if (b.isChecked()) {
	                    		mRootView.findViewById(R.id.friends).setVisibility(View.VISIBLE);
	//                    		mRootView.findViewById(R.id.singles).setVisibility(View.GONE);  
	//                    		((ToggleButton) mRootView.findViewById(R.id.button_singles)).setChecked(false);
	                    	} else {
	                    		mRootView.findViewById(R.id.friends).setVisibility(View.GONE);                    		
	                    	}
	                    }});
		
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

	
	/* 
	// Hardcode for now
	private void loadUsers() {
		mUsers.add(new User(R.drawable.emily, 1, "Emily","Santa Monica, CA", "20's / F / Straight / Single", "I'm hot, I know it, buy me a drink and show it."));
		mUsers.add(new User(R.drawable.tracy, 2, "Tracy","Santa Monica, CA", "30's / F / Straight / Single", "If ignorance were bliss I'd be so blissfully aware that it wasn't."));
		mUsers.add(new User(R.drawable.alexandra, 3, "Alexandra","Santa Monica, CA", "20's / F / Straight / Single", "Got chef?"));
	}
	*/
	
	
	public void addPerson(Profile person) {
//		String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
		
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
			
			// Update header buttons
//			((ToggleButton) mRootView.findViewById(R.id.button_orders_new)).setText("NEW (" + mOrders.size() + ")");
//			((ToggleButton) mRootView.findViewById(R.id.button_orders_new)).setTextOn("NEW (" + mOrders.size() + ")");
//			((ToggleButton) mRootView.findViewById(R.id.button_orders_new)).setTextOff("NEW (" + mOrders.size() + ")");

			Log.d("Bartsy", "Added new person to people list View");
//			((Bartsy)getActivity()).appendStatus("Added new order to order list view");
		}
		Log.d("Bartsy", "mPeople list size = " + mApp.mPeople.size());
	}

	
	
    @Override
    public void onClick(View v) {
    	
        // Create an instance of the dialog fragment and show it
//        DialogFragment dialog = new UserProfileDialog();
//        dialog.show(getActivity().getSupportFragmentManager(), "User profile");
        
        // Create an instance of the dialog fragment and show it
        PeopleDialogFragment dialog = new PeopleDialogFragment();
        dialog.mUser = (Person) v.getTag();
        dialog.show(getActivity().getSupportFragmentManager(), "User profile");
    }
	
}
