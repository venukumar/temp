/**
 * 
 */
package com.vendsy.bartsy.view;

import java.util.HashMap;

import android.graphics.Bitmap;
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
import com.vendsy.bartsy.dialog.ProfileDialogFragment;
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
	private PeopleListView mPeopleListView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.v(TAG, "PeopleSectionFragment.onCreateView()");

		mInflater = inflater;
		mContainer = container;
		mRootView = inflater.inflate(R.layout.people_tab, container, false);
		peopleLayout = (LinearLayout) mRootView.findViewById(R.id.people_list);
		
		// Make sure the fragment pointed to by the activity is accurate
		mApp = (BartsyApplication) getActivity().getApplication();
		mActivity = (VenueActivity) getActivity();
		((VenueActivity) getActivity()).mPeopleFragment = this;
		
		// Add People list view object to the liner layout
		mPeopleListView = new PeopleListView(mActivity, mApp, inflater, mActivity.mImageCache){
			@Override
			protected void selectedUserProfile(UserProfile profile) {
				// if this view has been used by VenueActivity then enable the PeopleProfileDialog
				ProfileDialogFragment dialog = new ProfileDialogFragment();
				dialog.mUser = profile;
				dialog.show((mActivity).getSupportFragmentManager(),"ProfileDialogFragment");
				
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
			mPeopleListView.loadPeopleList(mActivity.mImageCache);
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
