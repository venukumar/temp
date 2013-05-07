/**
 * 
 */
package com.kellislabs.bartsy;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * @author peterkellis
 *
 */
public class BartenderSectionFragment extends Fragment implements OnClickListener {

	private View mRootView = null;
	LinearLayout mNewOrdersView = null;
	LinearLayout mAcceptedOrdersView = null;
	LinearLayout mCompletedOrdersView = null;
	LayoutInflater mInflater = null;
	ViewGroup mContainer = null;
	BartsyApplication mApp = null;
	
//	private String mDBText = "";

	/*
	 * Creates a map view, which is for now a mock image. Listen for clicks on the image
	 * and toggle the bar details image
	 */ 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		Log.d("Bartsy", "OrdersSectionFragment.onCreateView()");

		mInflater = inflater;
		mContainer = container;
		mRootView = mInflater.inflate(R.layout.bartender_main, mContainer, false);
		mNewOrdersView = (LinearLayout) mRootView.findViewById(R.id.view_new_order_list);
		mAcceptedOrdersView = (LinearLayout) mRootView.findViewById(R.id.view_accepted_order_list);
		mCompletedOrdersView = (LinearLayout) mRootView.findViewById(R.id.view_completed_order_list);
		
		updateOrdersView();
		
		return mRootView;

	}

	/***
	 * Upddates the orders view on both the phone and the tablet
	 */
	
	public void updateOrdersView() {
		
		// Make sure the list view is empty
		mNewOrdersView.removeAllViews();
		

		// Add any existing orders in the layout, one by one
		Log.d("Bartsy", "About to add orders list to the View");
		Log.d("Bartsy", "mApp.mOrders list size = " + mApp.mOrders.size());

		for (Order order : mApp.mOrders) {
			Log.d("Bartsy", "Adding an item to the layout");
			order.view = (View) mInflater.inflate(R.layout.bartender_order, mContainer, false);
			order.updateView();
			order.view.findViewById(R.id.view_order_button_positive).setOnClickListener(this);
			order.view.findViewById(R.id.view_order_button_negative).setOnClickListener(this);

			
			switch (order.status) {
			case Order.ORDER_STATUS_NEW:
				// add order to the top of the accepted orders list view
				mNewOrdersView.addView(order.view);
				break;
			case Order.ORDER_STATUS_IN_PROGRESS:
				// add order to the top of the accepted orders list view
				mAcceptedOrdersView.addView(order.view, 0); 
				break;
			case Order.ORDER_STATUS_READY:
				// add order to the bottom of the completed orders list view 
				mCompletedOrdersView.addView(order.view);
				break;
			}
		}
	}
	
	
	@Override 
	public void onDestroyView() {
		super.onDestroyView();

		Log.d("Bartsy", "OrdersSectionFragment.onDestroyView()");
		
		mRootView = null;
		mNewOrdersView = null;
		mInflater = null;
		mContainer = null;
	}
	
	@Override 
	public void onDestroy() {
		super.onDestroy();

		Log.d("Bartsy", "OrdersSectionFragment.onDestroy()");
	}
		
		
	public void addOrder(Order order) {
//		String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
		
		Log.d("Bartsy", "Adding new order to orders list: " + order.title);
		
		if (mNewOrdersView == null) {
			
			Log.d("Bartsy", "The orders view in null. Adding to the orders list only");
			
			mApp.mOrders.add(order);
		} else {

			Log.d("Bartsy", "The orders view in not null. Adding to the orders list and the view");

			mApp.mOrders.add(order);
			order.view = (View) mInflater.inflate(R.layout.bartender_order, mContainer, false);
			order.updateView();

			order.view.findViewById(R.id.view_order_button_positive).setOnClickListener(this);
			order.view.findViewById(R.id.view_order_button_negative).setOnClickListener(this);
			mNewOrdersView.addView(order.view);
			
			Log.d("Bartsy", "Adding new order to orders list View");
		}
		Log.d("Bartsy", "mApp.mOrders list size = " + mApp.mOrders.size());
		
	}


	
	@Override
	public void onClick(View v) {

		Log.i("Bartsy", "Click event");

		switch (v.getId()) {
		case R.id.view_order_button_positive:
			Order order = (Order) v.getTag();
			Log.i("Bartsy", "Clicked on order positive button");

			// Update the order status locally 
			order.nextPositiveState();
			
			// Update the orders view accordingly
			switch (order.status) {
			case Order.ORDER_STATUS_IN_PROGRESS:
				// add order to the top of the accepted orders list view
				mNewOrdersView.removeView(order.view);
				mAcceptedOrdersView.addView(order.view, 0); 
				order.updateView();
				break;
			case Order.ORDER_STATUS_READY:
				// add order to the bottom of the completed orders list view 
				mAcceptedOrdersView.removeView(order.view);
				mCompletedOrdersView.addView(order.view);
				order.updateView();
				break;
			case Order.ORDER_STATUS_COMPLETE:	
				// Trash the order for now (later save it to log of past orders)
				mCompletedOrdersView.removeView(order.view);
				order.view = null;
				mApp.mOrders.remove(order);
				break;
			}
			
			// Send updated order status to the remote
			((VenueActivity) getActivity()).sendOrderStatusChanged(order);

			break;
		}
	}
}
