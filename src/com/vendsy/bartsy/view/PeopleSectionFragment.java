/**
 * 
 */
package com.vendsy.bartsy.view;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.VenueActivity;
import com.vendsy.bartsy.model.UserProfile;

/**
 * @author peterkellis
 * 
 */
public class PeopleSectionFragment extends SherlockFragment{

	static final String TAG = "PeopleSectionFragment";
	
	View mRootView = null;
	LayoutInflater mInflater = null;
	ViewGroup mContainer = null;
	public LinearLayout peopleLayout = null;
	public BartsyApplication mApp = null;
	private VenueActivity mActivity = null;
	private Handler handler = new Handler();

	private PeopleListView mPeopleListView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.v(TAG, "PeopleSectionFragment.onCreateView()");

		mInflater = inflater;
		mContainer = container;
		mRootView = inflater.inflate(R.layout.users_main, container, false);
		peopleLayout = (LinearLayout) mRootView.findViewById(R.id.view_singles);
		
		// Make sure the fragment pointed to by the activity is accurate
		mApp = (BartsyApplication) getActivity().getApplication();
		mActivity = (VenueActivity) getActivity();
		((VenueActivity) getActivity()).mPeopleFragment = this;
		
		// Add People list view object to the liner layout
		mPeopleListView = new PeopleListView(mActivity, mApp, inflater){
			@Override
			protected void selectedUserProfile(UserProfile profile) {
				// For now, there is no action for profile selection
			}
		};
		
		peopleLayout.addView(mPeopleListView);
		
		return mRootView;
	}
	
	/**
	 *  To get peoples from the server and update in the list
	 */
	public void updatePeopleView(){
		if(mPeopleListView!=null){
			mPeopleListView.loadPeopleList();
		}
	}
	
	
	@Override 
	public void onDestroy() {
		super.onDestroy();
		
		Log.v(TAG, "PeopleSectionFragment.onDestroy()");

		mRootView = null;
		peopleLayout = null;
		mInflater = null;
		mContainer = null;

		// Because the fragment may be destroyed while the activity persists, remove pointer from activity
		((VenueActivity) getActivity()).mPeopleFragment = null;
	}

}
