/**
 * 
 */
package com.vendsy.bartsy.view;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vendsy.bartsy.R;
import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.VenueActivity;
import com.vendsy.bartsy.model.Order;
import com.vendsy.bartsy.model.Profile;
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
public class OrdersSectionFragment extends Fragment implements OnClickListener {

	private View mRootView = null;
	public LinearLayout mOrderListView = null;
	LayoutInflater mInflater = null;
	ViewGroup mContainer = null;
	public BartsyApplication mApp = null;
	private VenueActivity mActivity = null;
	private Handler handler = new Handler();

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

		Log.i("Bartsy", "About to add orders list to the View");

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
				String response = WebServices.getUserOrdersList(mApp
						.getApplicationContext());

				Log.i(Constants.TAG, "oreders " + response);

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
							// Make sure the list view is empty
							mOrderListView.removeAllViews();

							// Add any existing orders in the layout, one by one
							Log.i("Bartsy", "mApp.mOrders list size = " + mApp.mOrders.size());

							for (Order barOrder : mApp.mOrders) {
								Log.d("Bartsy", "Adding an item to the layout");
								barOrder.view = (View) mInflater.inflate(R.layout.order_item,
										mContainer, false);
								barOrder.updateView();
								barOrder.view.findViewById(R.id.view_order_button_positive)
										.setOnClickListener(OrdersSectionFragment.this);
								barOrder.view.findViewById(R.id.view_order_button_negative)
										.setOnClickListener(OrdersSectionFragment.this);

								mOrderListView.addView(barOrder.view);
							}

						// Update people count in people tab
						mActivity.updateOrdersCount();
						}
					});
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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

	@Override
	public void onClick(View v) {

		Log.i("Bartsy", "Click event");

		switch (v.getId()) {
		case R.id.view_order_button_positive:
			Order order = (Order) v.getTag();
			Log.i("Bartsy", "Clicked on order positive button");

			// Update the order status locally and send the update to the remote
			order.nextPositiveState();
			((VenueActivity) getActivity()).sendOrderStatusChanged(order);

			if (order.status == Order.ORDER_STATUS_COMPLETE) {
				// Trash the order for now (later save it to log of past orders)
				removeOrder(order);
			} else {
				order.updateView();
			}
			break;
		}
	}

	public void removeOrder(Order order) {
		if (mOrderListView != null)
			mOrderListView.removeView(order.view);
		// ((ViewGroup) order.view.getParent()).removeView(order.view);
		order.view = null;
		mApp.mOrders.remove(order);
	}

}
