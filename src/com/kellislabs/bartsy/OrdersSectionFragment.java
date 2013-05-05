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
public class OrdersSectionFragment extends Fragment implements OnClickListener {

	private View mRootView = null;
	LinearLayout mOrderListView = null;
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
		mRootView = mInflater.inflate(R.layout.orders_main, mContainer, false);
		mOrderListView = (LinearLayout) mRootView.findViewById(R.id.order_list);
		
		updateOrdersView();
		
		return mRootView;

	}

	public void updateOrdersView() {
		
		// Make sure the list view is empty
		mOrderListView.removeAllViews();
		

		// Add any existing orders in the layout, one by one
		Log.d("Bartsy", "About to add orders list to the View");
		Log.d("Bartsy", "mApp.mOrders list size = " + mApp.mOrders.size());

		for (Order order : mApp.mOrders) {
			Log.d("Bartsy", "Adding an item to the layout");
			order.view = (View) mInflater.inflate(R.layout.order_item, mContainer, false);
			order.updateView();
			order.view.findViewById(R.id.view_order_button_positive).setOnClickListener(this);
			order.view.findViewById(R.id.view_order_button_negative).setOnClickListener(this);

			mOrderListView.addView(order.view);
//			((Bartsy)getActivity()).appendStatus("Added new view");
		}
		
		((ToggleButton) mRootView.findViewById(R.id.button_orders_new)).setText("NEW (" + mApp.mOrders.size() + ")");
		((ToggleButton) mRootView.findViewById(R.id.button_orders_new)).setTextOn("NEW (" + mApp.mOrders.size() + ")");
		((ToggleButton) mRootView.findViewById(R.id.button_orders_new)).setTextOff("NEW (" + mApp.mOrders.size() + ")");
		
	}
	
	
	@Override 
	public void onDestroyView() {
		super.onDestroyView();

		Log.d("Bartsy", "OrdersSectionFragment.onDestroyView()");
		
		mRootView = null;
		mOrderListView = null;
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
		
		if (mOrderListView == null) {
			
			Log.d("Bartsy", "The orders view in null. Adding to the orders list only");
			
			mApp.mOrders.add(order);
		} else {

			Log.d("Bartsy", "The orders view in not null. Adding to the orders list and the view");

			mApp.mOrders.add(order);
			order.view = (View) mInflater.inflate(R.layout.order_item, mContainer, false);
			order.updateView();

			order.view.findViewById(R.id.view_order_button_positive).setOnClickListener(this);
			order.view.findViewById(R.id.view_order_button_negative).setOnClickListener(this);
			mOrderListView.addView(order.view);
			
			// Update header buttons
			((ToggleButton) mRootView.findViewById(R.id.button_orders_new)).setText("NEW (" + mApp.mOrders.size() + ")");
			((ToggleButton) mRootView.findViewById(R.id.button_orders_new)).setTextOn("NEW (" + mApp.mOrders.size() + ")");
			((ToggleButton) mRootView.findViewById(R.id.button_orders_new)).setTextOff("NEW (" + mApp.mOrders.size() + ")");

			Log.d("Bartsy", "Adding new order to orders list View");
//			((Bartsy)getActivity()).appendStatus("Added new order to order list view");
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

			// Update the order status locally and send the update to the remote
			order.nextPositiveState();
			
			if (order.status == Order.ORDER_STATUS_COMPLETE) {
				// Trash the order for now (later save it to log of past orders)
				removeOrder(order);
			} else {
				order.updateView();
			}

			// Send order update status to the remote
			((VenueActivity) getActivity()).sendOrderStatusChanged(order);

			
			break;
		}
	}
	
	public void removeOrder(Order order) {
		if (mOrderListView != null)
			mOrderListView.removeView(order.view);
//		((ViewGroup) order.view.getParent()).removeView(order.view);
		order.view = null;
		mApp.mOrders.remove(order);
	}

	
}
