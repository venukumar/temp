/**
 * 
 */
package com.vendsy.bartsy.view;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.VenueActivity;

/**
 * @author peterkellis
 * 
 */
public class OrdersSectionFragment extends SherlockFragment{

	private View mRootView = null;
	LayoutInflater mInflater = null;
	ViewGroup mContainer = null;
	public BartsyApplication mApp = null;
	private VenueActivity mActivity = null;
	
	private LinearLayout mOpenOrderView;
	private LinearLayout mPastOrderView;
	private OpenOrdersSectionView mOpenOrdersListView;
	
	static final String TAG = "OrdersSectionFragment";

	// private String mDBText = "";

	/*
	 * Creates a map view, which is for now a mock image. Listen for clicks on
	 * the image and toggle the bar details image
	 */

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.d("Bartsy", "OrdersSectionFragment.onCreateView()");

		mInflater = inflater;
		mContainer = container;
		mRootView = mInflater.inflate(R.layout.open_closed_orders_main, mContainer, false);
		
		// Make sure the fragment pointed to by the activity is accurate
		mApp = (BartsyApplication) getActivity().getApplication();
		mActivity = (VenueActivity) getActivity();
		((VenueActivity) getActivity()).mOrdersFragment = this;
		
		// Try to get content layouts
		mOpenOrderView = (LinearLayout) mRootView.findViewById(R.id.view_orders_open);
		mPastOrderView = (LinearLayout) mRootView.findViewById(R.id.view_orders_past);
		
		// Add Open orders and past orders to the layouts
		mOpenOrdersListView = new OpenOrdersSectionView(mActivity);
		mOpenOrderView.addView(mOpenOrdersListView);
		mPastOrderView.addView(new PastOrdersSectionView(mActivity));
		
		// Try to get buttons from the layout
		Button openOrdersButton = (Button) mRootView.findViewById(R.id.view_orders_button_open);
		Button pastOrdersButton = (Button) mRootView.findViewById(R.id.view_orders_button_past);
		
		openOrdersButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mOpenOrderView.setVisibility(View.VISIBLE);
				mPastOrderView.setVisibility(View.GONE);
				
				((ToggleButton) mRootView.findViewById(R.id.view_orders_button_open)).setChecked(true);
				((ToggleButton) mRootView.findViewById(R.id.view_orders_button_past)).setChecked(false);
			}
		});
		
		pastOrdersButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPastOrderView.setVisibility(View.VISIBLE);
				mOpenOrderView.setVisibility(View.GONE);

				((ToggleButton) mRootView.findViewById(R.id.view_orders_button_open)).setChecked(false);
				((ToggleButton) mRootView.findViewById(R.id.view_orders_button_past)).setChecked(true);
			}
		});
		
		
		
		// Hide past orders by default
		mPastOrderView.setVisibility(View.GONE);
		
		updateOrdersView();

		return mRootView;

	}

	public void updateOrdersView() {

		Log.v(TAG, "About to add orders list to the View");
		if(mOpenOrdersListView!=null){
			mOpenOrdersListView.updateOrdersView();
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d("Bartsy", "OrdersSectionFragment.onDestroy()");

		mRootView = null;
		mInflater = null;
		mContainer = null;

		// Because the fragment may be destroyed while the activity persists,
		// remove pointer from activity
		((VenueActivity) getActivity()).mOrdersFragment = null;
	}

}
