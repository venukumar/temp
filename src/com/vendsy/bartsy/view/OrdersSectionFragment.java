/**
 * 
 */
package com.vendsy.bartsy.view;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vendsy.bartsy.R;
import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.VenueActivity;
import com.vendsy.bartsy.model.Order;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

/**
 * @author peterkellis
 * 
 */
public class OrdersSectionFragment extends Fragment {

	private View mRootView = null;
	public LinearLayout mOrderListView = null;
	LayoutInflater mInflater = null;
	ViewGroup mContainer = null;
	public BartsyApplication mApp = null;
	private VenueActivity mActivity = null;
	private Handler handler = new Handler();
	
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
		mRootView = mInflater.inflate(R.layout.orders_main, mContainer, false);
		mOrderListView = (LinearLayout) mRootView.findViewById(R.id.order_list);

		// Make sure the fragment pointed to by the activity is accurate
		mApp = (BartsyApplication) getActivity().getApplication();
		mActivity = (VenueActivity) getActivity();
		((VenueActivity) getActivity()).mOrdersFragment = this;
		updateOrdersView();

		return mRootView;

	}

	public void updateOrdersView() {

		Log.v(TAG, "About to add orders list to the View");

		// Make sure the list view exists and is empty
		if (mOrderListView == null)
			return;
		
		// For now remove list of orders
		mApp.mOrders.clear();
		// Load the orders currently present in the Bartsy user
		loadUserOrders();

	}

	/**
	 * To get user orders from the server
	 */
	private void loadUserOrders() {
		// Service call for get menu list in background
		new Thread() {

			public void run() {
				String response = WebServices.getUserOrdersList(mApp);

				Log.v(TAG, "oreders " + response);

				userOrdersResponseHandling(response);
			};

		}.start();
	}

	/**
	 * User orders web service Response handling
	 * 
	 * @param response
	 */

	private void userOrdersResponseHandling(String response) {
		if (response != null) {
			try {
				JSONObject orders = new JSONObject(response);
				JSONArray listOfOrders = orders.has("orders") ? orders
						.getJSONArray("orders") : null;
				if (listOfOrders != null) {

					for (int i = 0; i < listOfOrders.length(); i++) {
						JSONObject orderJson = (JSONObject) listOfOrders.get(i);
						Order order = new Order(orderJson);
						mApp.mOrders.add(order);
					}
					// To call UI thread and display checkedIn people list
					handler.post(new Runnable() {

						@Override
						public void run() {

							displayOrders();
						}
					});
				}
				else
				{
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							// Make sure the list view is empty
							mOrderListView.removeAllViews();
						}
					});
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

	}

	/*
	 * Displays the orders from the order list into the view, bundled by state
	 */
	
	private void displayOrders() {

		Log.v(TAG, "mApp.mOrders list size = " + mApp.mOrders.size());

		// Use a swallow copy of the global structure to be able to remove orders from the global structure in the iterator
		ArrayList<Order> orders = (ArrayList<Order>) mApp.mOrders.clone();

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
			default:
				Log.d(TAG, "Unexpected order status");
				break;
			}

			// If we have found a new order to display that hasn't been displayed before, display it and any related mini-view
			
			if (next != Order.ORDER_STATUS_COUNT) {
				Log.v(TAG, "Showing master order " + order.serverID);
				
				// Display header view with current order
				mOrderListView.addView(order.updateView(mInflater, mContainer));
				
				// If there are any more orders of the same type display them as mini views
				
				for (int j = i+1 ; j < orders.size(); j++)
				{
					Order mini = orders.get(j);
					if (mini.status == next) {
						Log.v(TAG, "Adding mini order " + mini.serverID + " to order " + order.serverID);
						((LinearLayout)order.view.findViewById(R.id.view_order_mini))
							.addView(mini.getMiniView(mInflater, mContainer));
					}
				}
			}
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
		((VenueActivity) getActivity()).mOrdersFragment = null;
	}


	public void removeOrder(Order order) {
		if (mOrderListView != null)
			mOrderListView.removeView(order.view);
		// ((ViewGroup) order.view.getParent()).removeView(order.view);
		order.view = null;
		mApp.mOrders.remove(order);
	}

}
