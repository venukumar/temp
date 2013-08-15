/**
 * 
 */
package com.vendsy.bartsy.view;

import java.text.DecimalFormat;
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
import com.vendsy.bartsy.model.Item;
import com.vendsy.bartsy.model.Order;
import com.vendsy.bartsy.utils.Utilities;
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
		mRootView = mInflater.inflate(R.layout.past_orders, null);
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
			
			return WebServices.getPastOrders(mApp, mApp.mProfile.getBartsyId(), mApp.mActiveVenue.getId());
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
					Order order = new Order(mApp.loadBartsyId(), json);
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
		ordersTableLayout.addView(order.pastView(mInflater));
	}

}