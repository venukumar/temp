/**
 * 
 */
package com.vendsy.bartsy.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.VenueActivity;
import com.vendsy.bartsy.model.Order;
import com.vendsy.bartsy.utils.WebServices;

/**
 * @author peterkellis
 * @author Seenu Malireddy
 * 
 */
public class PastOrdersSectionView extends LinearLayout {
	
	String TAG = "PastOrdersSectionFragment";

	private BartsyApplication mApp = null;
	private VenueActivity mActivity = null;
	LayoutInflater mInflater = null;
	ViewGroup mContainer = null;


	private View mRootView = null;
	private LinearLayout ordersTableLayout = null;


	public PastOrdersSectionView(Activity activity) {
		super(activity);
		
		Log.v(TAG, "PastOrdersSectionView() - Constructor");
		
		// Setup application pointer
		mActivity = (VenueActivity) activity;
		mApp = (BartsyApplication) mActivity.getApplication();
		
		// Set parameters for linear layout
		LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		setLayoutParams(params);
		setOrientation(LinearLayout.VERTICAL);
		
		// Set up views
		mInflater = activity.getLayoutInflater();
		mRootView = mInflater.inflate(R.layout.orders_past_main, null);
		ordersTableLayout = (LinearLayout) mRootView.findViewById(R.id.pastordersLayout);
		
		addView(mRootView);
	}
	
	public void loadPastOrders(){
		// Set up layout in the background
		new PastOrders().execute("params");
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
		
		// Extract tiem from UTC field
		String inputText = order.createdDate.replace("T", " ").replace("Z", ""); // example: 2013-06-27 10:20:15
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        inputFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm");
        Date date;
        String time = "";
        try {
			date = inputFormat.parse(inputText);
			time = outputFormat.format(date);
		} catch (ParseException e) {
			// Bad date format - leave time blank
			e.printStackTrace();
			Log.e(TAG, "Bad date format in getPastOrders syscall");
		} 
		
		((TextView) itemView.findViewById(R.id.dateCreated)).setText(time);
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

		ordersTableLayout.addView(itemView);
	}

}