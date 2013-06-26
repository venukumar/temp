/**
 * 
 */
package com.vendsy.bartsy.view;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.VenueActivity;
import com.vendsy.bartsy.model.Order;
import com.vendsy.bartsy.utils.WebServices;

/**
 * @author peterkellis
 * 
 */
public class CopyOfOpenOrdersSectionFragment_old extends SherlockFragment implements OnClickListener {

	private View mRootView = null;
	public LinearLayout mOrderListView = null;
	LayoutInflater mInflater = null;
	ViewGroup mContainer = null;
	public BartsyApplication mApp = null;
	private VenueActivity mActivity = null;
	private Handler handler = new Handler();
	
	static final String TAG = "OpenOrdersSectionFragment";

	// private String mDBText = "";

	/*
	 * Creates a map view, which is for now a mock image. Listen for clicks on
	 * the image and toggle the bar details image
	 */

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.d(TAG, "onCreateView()");

		mInflater = inflater;
		mContainer = container;
		mRootView = mInflater.inflate(R.layout.orders_open_main, mContainer, false);
		mOrderListView = (LinearLayout) mRootView.findViewById(R.id.order_list);

		// Make sure the fragment pointed to by the activity is accurate
		mApp = (BartsyApplication) getActivity().getApplication();
		mActivity = (VenueActivity) getActivity();
//		((VenueActivity) getActivity()).mOpenOrdersFragment = this;
		updateOrdersView();

		return mRootView;

	}

	public void updateOrdersView() {

		Log.v(TAG, "About to add orders list to the View");

		// Make sure the list view exists and is empty
		if (mOrderListView == null)
			return;
		
		// For now remove list of orders
//		mApp.clearOrders();
			
		// Load the orders currently present in the Bartsy server
//		loadUserOrders();
		
		displayOrders();

	}


	/*
	 * Displays the orders from the order list into the view, bundled by state
	 */
	
	private void displayOrders() {

		Log.v(TAG, "mApp.mOrders list size = " + mApp.getOrderCount());

		// Use a swallow copy of the global structure to be able to remove orders from the global structure in the iterator
		ArrayList<Order> orders = mApp.getOrdersCopy();
		
		// Update people count in people tab
		mActivity.updateOrdersCount();

		// Make sure the list view is empty
		mOrderListView.removeAllViews();

		// We bundle orders together according to their status, starting from the top of the list
		boolean newOrdersDisplayed = false;
		boolean acceptedOrdersDisplayed = false;
		boolean readyOrdersDisplayed = false;

		// Add any existing orders in the layout, one by one

		for (int i = 0 ; i < orders.size(); i++) {
			
			Order order= orders.get(i);

			Log.v(TAG, "Processing order " + order.serverID + " with status " + order.status);

			int next = Order.ORDER_STATUS_COUNT;
			
			switch (order.status) {
			case Order.ORDER_STATUS_NEW:
				if (!newOrdersDisplayed) {
					Log.v(TAG, "Order " + order.serverID + " is the first order with status " + order.status);
					next = Order.ORDER_STATUS_NEW;
					newOrdersDisplayed = true;
				} else {
					Log.v(TAG, "All orders of status " + order.status + " have been displayed. Skipping order " + order.serverID);					
				}
				break;
			case Order.ORDER_STATUS_IN_PROGRESS:
				if (!acceptedOrdersDisplayed) {
					Log.v(TAG, "Order " + order.serverID + " is the first order with status " + order.status);
					next = Order.ORDER_STATUS_IN_PROGRESS;
					acceptedOrdersDisplayed = true;
				} else {
					Log.v(TAG, "All orders of status " + order.status + " have been displayed. Skipping order " + order.serverID);					
				}
				break;
			case Order.ORDER_STATUS_READY:
				if (!readyOrdersDisplayed) {
					Log.v(TAG, "Order " + order.serverID + " is the first order with status " + order.status);
					next = Order.ORDER_STATUS_READY;
					readyOrdersDisplayed = true;
				} else {
					Log.v(TAG, "All orders of status " + order.status + " have been displayed. Skipping order " + order.serverID);					
				}
				break;
			case Order.ORDER_STATUS_CANCELLED:
				// Always display cancelled orders individually
				Log.v(TAG, "Display timed-out order " + order.serverID);
				next = Order.ORDER_STATUS_CANCELLED;
				break;
			default:
				Log.d(TAG, "Unexpected order status");
				break;
			}

			// If we have found a new order to display that hasn't been displayed before, display it and any related mini-view
			
			if (next != Order.ORDER_STATUS_COUNT) {
				Log.v(TAG, "Showing master order " + order.serverID);
				
				// Display header view with current order
				View view = order.updateView(mInflater, mContainer);
				view.findViewById(R.id.view_order_notification_button).setOnClickListener(this);
				mOrderListView.addView(view);

				
				// If there are any more orders of the same type display them as mini views
				
				for (int j = i+1 ; j < orders.size(); j++)
				{
					Order mini = orders.get(j);
					if (mini.status == next && next 
							!= Order.ORDER_STATUS_CANCELLED)  // Never bundle cancelled orders...
					{
						Log.v(TAG, "Adding mini order " + mini.serverID + " to order " + order.serverID);
						((LinearLayout)order.view.findViewById(R.id.view_order_mini))
							.addView(mini.getMiniView(mInflater, mContainer));
					}
				}
			}
		}
	}
	

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.view_order_notification_button:
			mApp.removeOrder((Order) arg0.getTag());
			break;
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.d("Bartsy", "OrdersSectionFragment.onDestroy()");

		mRootView = null;
		mOrderListView = null;
		mInflater = null;
		mContainer = null;

		// Because the fragment may be destroyed while the activity persists,
		// remove pointer from activity
//		((VenueActivity) getActivity()).mOpenOrdersFragment = null;
	}


	public void removeOrders(Order order) {
		if (mOrderListView != null)
			mOrderListView.removeView(order.view);
		// ((ViewGroup) order.view.getParent()).removeView(order.view);
		order.view = null;
		mApp.removeOrder(order);
	}

}
