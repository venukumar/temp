/**
 * 
 */
package com.vendsy.bartsy.view;

import java.util.ArrayList;

import android.app.Activity;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

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
public class OpenOrdersSectionView extends LinearLayout{

	private View mRootView = null;
	public LinearLayout mOrderListView = null;
	LayoutInflater mInflater = null;
	ViewGroup mContainer = null;
	public BartsyApplication mApp = null;
	private VenueActivity mActivity = null;
	
	static final String TAG = "OrdersSectionView";

	// private String mDBText = "";

	public OpenOrdersSectionView(Activity activity) {
		super(activity);
		
		Log.v(TAG, "OpenOrdersSectionView() - Constructor");
		
		// Setup application pointer
		mActivity = (VenueActivity) activity;
		mApp = (BartsyApplication) mActivity.getApplication();
		
		// Set parameters for linear layout
		LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		setLayoutParams(params);
		setOrientation(LinearLayout.VERTICAL);

		mInflater = activity.getLayoutInflater();
		mRootView = mInflater.inflate(R.layout.orders_open_main, mContainer, false);
		mOrderListView = (LinearLayout) mRootView.findViewById(R.id.order_list);

		updateOrdersView();

		addView(mRootView);
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
		ArrayList<Order> orders = mApp.cloneOrders();
		
		// Update people count in people tab
		mActivity.updateOrdersCount();

		// Make sure the list view is empty
		mOrderListView.removeAllViews();
		
		ArrayList<String> recipients = new ArrayList<String>();
		
		for (Order order : orders) {
			if (!recipients.contains(order.recipientId))
				recipients.add(order.recipientId);
		}

		// For all different recipients of drinks (for now order by recipient isntead of order number)

		for (String recipientId : recipients) {
		
			// We bundle orders together according to their status, starting from the top of the list
			boolean[] statusDisplayed = new boolean[Order.ORDER_STATUS_COUNT];
	
			// Add any existing orders in the layout, one by one
	
			for (int i = 0 ; i < orders.size(); i++) {
				
				Order order= orders.get(i);

				// Process the orders of the current recipient only
				
				if (order.recipientId.equals(recipientId)) {
					
					Log.v(TAG, "Processing order " + order.serverID + " with status " + order.status + " and last status " + order.last_status);
		
					if (!statusDisplayed[order.status]) {
						
						Log.v(TAG, "Showing master order " + order.serverID);
		
						statusDisplayed[order.status] = true;
						
						// Display header based on the current order
						
						View view = order.updateView(mInflater, mContainer);
						view.findViewById(R.id.view_order_notification_button).setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								mApp.removeOrder((Order) v.getTag());
							}
						});
						
						view.findViewById(R.id.view_order_footer_accept).setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Order order = (Order) v.getTag();
								((Button) order.view.findViewById(R.id.view_order_footer_reject)).setEnabled(false);
								((Button) order.view.findViewById(R.id.view_order_footer_accept)).setEnabled(false);
								WebServices.updateOfferedDrinkStatus(mApp, order, true);
							}
						});
						
						view.findViewById(R.id.view_order_footer_reject).setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Order order = (Order) v.getTag();
								((Button) order.view.findViewById(R.id.view_order_footer_reject)).setEnabled(false);
								((Button) order.view.findViewById(R.id.view_order_footer_accept)).setEnabled(false);
								WebServices.updateOfferedDrinkStatus(mApp, order, false);
							}
						});
						
						mOrderListView.addView(view);
		
						// If there are any more orders of the same type and recipient, display them as mini views
		
						float taxAmt = order.taxAmount;
						float tipAmt = order.tipAmount;
						float totalAmt = order.totalAmount;
						
						for (int j = i+1 ; j < orders.size(); j++)
						{
							Order mini = orders.get(j);
							if (mini.status == order.status && order.recipientId.equals(mini.recipientId))  
							{
								Log.v(TAG, "Adding mini order " + mini.serverID + " to order " + order.serverID);
								((LinearLayout)order.view.findViewById(R.id.view_order_mini)).addView(mini.getMiniView(mInflater, mContainer));
		
								// Collect prices from mini views						
								taxAmt+=mini.taxAmount;
								tipAmt+=mini.tipAmount;
								totalAmt+=mini.totalAmount;
							}
						}
		
						// Update price of the parent order view with any additional money from mini-views added
						order.updateTipTaxTotalView(tipAmt,taxAmt,totalAmt);
					}
				}
			}
		}
	}

}
