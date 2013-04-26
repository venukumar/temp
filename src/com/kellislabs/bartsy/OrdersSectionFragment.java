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
	private LinearLayout mOrderListView = null;
	ArrayList<BarOrder> mOrders = new ArrayList<BarOrder>();
	LayoutInflater mInflater = null;
	ViewGroup mContainer = null;

//	private String mDBText = "";

	/*
	 * Creates a map view, which is for now a mock image. Listen for clicks on the image
	 * and toggle the bar details image
	 */ 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		Log.d("Bartsy", "OrdersSectionFragment.onCreateView()");

		if (mOrderListView == null) {
			mRootView = inflater.inflate(R.layout.orders_main, container, false);
			mOrderListView = (LinearLayout) mRootView.findViewById(R.id.order_list);

			if (mOrderListView == null)
				Log.d("Bartsy", "COULD NOT CREATE ORDERS LIST VIEW!!");
			else
				Log.d("Bartsy", "Orders list view created");

			
			mInflater = inflater;
			mContainer = container;
			
			// Add any existing orders in the layout, one by one
			Log.d("Bartsy", "About to add orders list to the View");
			Log.d("Bartsy", "mOrders list size = " + mOrders.size());

			for (BarOrder barOrder : mOrders) {
				Log.d("Bartsy", "Adding an item to the layout");
				barOrder.view = (View) mInflater.inflate(R.layout.order_item, mContainer, false);
				barOrder.updateView();
				mOrderListView.addView(barOrder.view);
//				((Bartsy)getActivity()).appendStatus("Added new view");
			}
			
			((ToggleButton) mRootView.findViewById(R.id.button_orders_new)).setText("NEW (" + mOrders.size() + ")");
			((ToggleButton) mRootView.findViewById(R.id.button_orders_new)).setTextOn("NEW (" + mOrders.size() + ")");
			((ToggleButton) mRootView.findViewById(R.id.button_orders_new)).setTextOff("NEW (" + mOrders.size() + ")");
			
		}
		return mRootView;

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
		
		
	public void addOrder(BarOrder barOrder) {
//		String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
		
		Log.d("Bartsy", "Adding new order to orders list: " + barOrder.title);
		
		if (mOrderListView == null) {
			
			Log.d("Bartsy", "The orders view in null. Adding to the orders list only");
			
			mOrders.add(barOrder);
		} else {

			Log.d("Bartsy", "The orders view in not null. Adding to the orders list and the view");

			mOrders.add(barOrder);
			barOrder.view = (View) mInflater.inflate(R.layout.order_item, mContainer, false);
			barOrder.updateView();

			barOrder.view.findViewById(R.id.view_order_button_positive).setOnClickListener(this);
			barOrder.view.findViewById(R.id.view_order_button_negative).setOnClickListener(this);
			mOrderListView.addView(barOrder.view);
			
			// Update header buttons
			((ToggleButton) mRootView.findViewById(R.id.button_orders_new)).setText("NEW (" + mOrders.size() + ")");
			((ToggleButton) mRootView.findViewById(R.id.button_orders_new)).setTextOn("NEW (" + mOrders.size() + ")");
			((ToggleButton) mRootView.findViewById(R.id.button_orders_new)).setTextOff("NEW (" + mOrders.size() + ")");

			Log.d("Bartsy", "Adding new order to orders list View");
//			((Bartsy)getActivity()).appendStatus("Added new order to order list view");
		}
		Log.d("Bartsy", "mOrders list size = " + mOrders.size());
	}


	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.view_order_button_positive:
			BarOrder order = (BarOrder) v.getTag();

			// Update the order status locally and send the update to the remote
			order.nextPositiveState();
			((BartsyActivity) getActivity()).sendOrderStatusChanged(order);
			
			if (order.status == BarOrder.ORDER_STATUS_COMPLETE) {
				// Trash the order for now (later save it to log of past orders)
				removeOrder(order);
			} else {
				order.updateView();
			}
			break;
		}
	}
	
	public void removeOrder(BarOrder order) {
		if (mOrderListView != null)
			mOrderListView.removeView(order.view);
//		((ViewGroup) order.view.getParent()).removeView(order.view);
		order.view = null;
		mOrders.remove(order);
	}

	
}
