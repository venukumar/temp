/**
 * 
 */
package com.vendsy.bartsy.view;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;
import com.actionbarsherlock.app.SherlockFragment;
import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.VenueActivity;
import com.vendsy.bartsy.utils.WebServices;
import com.vendsy.bartsy.model.Order;

/**
 * @author peterkellis
 * 
 */
public class PastOrdersSectionFragment extends SherlockFragment {
	
	String TAG = "PastOrdersSectionFragment";

	private BartsyApplication mApp = null;
	private VenueActivity mActivity = null;
	LayoutInflater mInflater = null;
	ViewGroup mContainer = null;


	private View mRootView = null;
	private LinearLayout ordersTableLayout = null;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Log.v(TAG, "onCreateView()");
		
		// Setup application pointer
		mActivity = (VenueActivity) getActivity();
		mApp = (BartsyApplication) mActivity.getApplication();

		// Set up views
		mInflater = inflater;
		mContainer = container;
		mRootView = inflater.inflate(R.layout.orders_past_main, container, false);
		ordersTableLayout = (LinearLayout) mRootView.findViewById(R.id.pastordersLayout);
		
		// Set up layout in the background
		new PastOrders().execute("params");

		return mRootView;
	}

	
	/**
	 * 
	 * Past orders sys call using async task instead of thread
	 */
	private class PastOrders extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			
			return WebServices.getPastOrders(mActivity, mApp.mProfile.getBartsyId(), mApp.mActiveVenue.getId());
		}

		@Override
		protected void onPostExecute(String result) {

			if (result != null) {
				updateOrdersView(result);
			}

		}

		@Override
		protected void onPreExecute() {

		}

	}	


	/**
	 * To update the orders view
	 */
	private void updateOrdersView(String response) {
		
		// Make sure that layout should be empty to add new views
		if (ordersTableLayout.getChildCount() > 0)
			ordersTableLayout.removeAllViews();
		
		try {
			JSONObject object = new JSONObject(response);
			JSONArray array = object.getJSONArray("pastOrders");
			if (array != null) {
				
				// Add rows one by one
				for (int i = 0; i < array.length(); i++) {
					JSONObject json =  array.getJSONObject(i);
					Order order = new Order(json);
					addNewOrderRow(order);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * To add new order to the table view
	 * 
	 * @param order
	 */
	private void addNewOrderRow(Order order) {
		
		final View itemView = mInflater.inflate(R.layout.orders_past_row, null);

		((TextView) itemView.findViewById(R.id.dateCreated)).setText(order.createdDate.substring(11, 16));
		((TextView) itemView.findViewById(R.id.orderId)).setText(order.serverID);
		
		String status = "?";
		switch(order.status) {
		case Order.ORDER_STATUS_CANCELLED:
			status = "Failed";
			break;
		case Order.ORDER_STATUS_COMPLETE:
			status = "OK";
			break;
		case Order.ORDER_STATUS_READY:
			status = "Open";
			break;
		case Order.ORDER_STATUS_FAILED:
			status = "Failed";
			break;
		case Order.ORDER_STATUS_IN_PROGRESS:
			status = "Open";
			break;
		case Order.ORDER_STATUS_INCOMPLETE:
			status = "Failed";
			break;
		case Order.ORDER_STATUS_NEW:
			status = "Open";
			break;
		case Order.ORDER_STATUS_REJECTED:
			status = "Failed";
			break;
		}
		
		((TextView) itemView.findViewById(R.id.orderStatus)).setText(String.valueOf(status));
		((TextView) itemView.findViewById(R.id.itemName)).setText(order.title);
		((TextView) itemView.findViewById(R.id.totalPrice)).setText(String.valueOf("$" + order.totalAmount));

		ordersTableLayout.addView(itemView, 0);
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.v(TAG, "onDestroy()");
		
		// Because the fragment may be destroyed while the activity persists, remove pointer from activity
		((VenueActivity) getActivity()).mDrinksFragment = null;
	}

}