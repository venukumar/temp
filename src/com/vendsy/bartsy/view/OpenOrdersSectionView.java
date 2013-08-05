/**
 * 
 */
package com.vendsy.bartsy.view;

import java.util.ArrayList;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.VenueActivity;
import com.vendsy.bartsy.model.Item;
import com.vendsy.bartsy.model.Order;
import com.jwetherell.quick_response_code.data.Contents;
import com.jwetherell.quick_response_code.qrcode.QRCodeEncoder;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.Utilities;
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
		mRootView = mInflater.inflate(R.layout.open_orders, mContainer, false);
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
		
		if (Constants.bundleOrders)
			displayBundledOrders();
		else
			displayOrders();
	}

	
	
	private void displayOrders() {
		
		// Use a shallow copy of the global structure to be able to remove orders from the global structure in the iterator
		ArrayList<Order> orders = mApp.cloneOrders();
		String pickupCode = null;

		// Update people count in people tab
		mActivity.updateOrdersCount();

		// Make sure the list view is empty
		mOrderListView.removeAllViews();
		
		for (int i = 0 ; i < orders.size(); i++) {
			
			Order order= orders.get(i);
			Log.v(TAG, "Processing order " + order.orderId + " with status " + order.status + " and last status " + order.last_status);

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
					order.updateStatus(Order.ORDER_STATUS_NEW);
					WebServices.updateOfferedDrinkStatus(mApp, order);
				}
			});
			
			view.findViewById(R.id.view_order_footer_reject).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Order order = (Order) v.getTag();
					((Button) order.view.findViewById(R.id.view_order_footer_reject)).setEnabled(false);
					((Button) order.view.findViewById(R.id.view_order_footer_accept)).setEnabled(false);
					order.updateStatus(Order.ORDER_STATUS_OFFER_REJECTED);
					WebServices.updateOfferedDrinkStatus(mApp, order);
				}
			});
			
			// Find orders for us
			if (mApp.mProfile.getBartsyId().equals(order.recipientId))
				pickupCode = order.userSessionCode;
			
			mOrderListView.addView(view);
		}
		
		// Update the header with the pickup code if an order exists
		if (Utilities.has(pickupCode)) {
			((TextView) mRootView.findViewById(R.id.open_orders_pickup_code)).setText(pickupCode);
			mRootView.findViewById(R.id.open_orders_pickup_field).setVisibility(View.VISIBLE);
			
			//Encode with a QR Code image
			QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(pickupCode, null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), 250);
			try {
				Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
				((ImageView) mRootView.findViewById(R.id.open_orders_qr_code)).setImageBitmap(bitmap);
			} catch (WriterException e) {
				e.printStackTrace();
				mRootView.findViewById(R.id.open_orders_qr_code).setVisibility(View.GONE);
			}
		} else {
			mRootView.findViewById(R.id.open_orders_pickup_field).setVisibility(View.GONE);
		}
		
	}
	

	/*
	 * Displays the orders from the order list into the view, bundled by state
	 */
	
	private void displayBundledOrders() {

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

		// For all different recipients of drinks (for now order by recipient instead of order number)

		for (String recipientId : recipients) {
		
			// We bundle orders together according to their status, starting from the top of the list
			boolean[] statusDisplayed = new boolean[Order.ORDER_STATUS_COUNT];
	
			// Add any existing orders in the layout, one by one
	
			for (int i = 0 ; i < orders.size(); i++) {
				
				Order order= orders.get(i);

				// Process the orders of the current recipient only
				
				if (order.recipientId.equals(recipientId)) {
					
					Log.v(TAG, "Processing order " + order.orderId + " with status " + order.status + " and last status " + order.last_status);
		
					// Use last status if the order status is now removed
					int status = order.status;
					if (order.status == Order.ORDER_STATUS_REMOVED)
						status = order.last_status;
					
					if (!statusDisplayed[status]) {
						
						Log.v(TAG, "Showing master order " + order.orderId);
		
						statusDisplayed[status] = true;
						
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
								order.updateStatus(Order.ORDER_STATUS_NEW);
								WebServices.updateOfferedDrinkStatus(mApp, order);
							}
						});
						
						view.findViewById(R.id.view_order_footer_reject).setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Order order = (Order) v.getTag();
								((Button) order.view.findViewById(R.id.view_order_footer_reject)).setEnabled(false);
								((Button) order.view.findViewById(R.id.view_order_footer_accept)).setEnabled(false);
								order.updateStatus(Order.ORDER_STATUS_OFFER_REJECTED);
								WebServices.updateOfferedDrinkStatus(mApp, order);
							}
						});
						
						mOrderListView.addView(view);
		 
						// If there are any more orders of the same type and recipient, display them as mini views
		
						double taxAmt = order.taxAmount;
						double tipAmt = order.tipAmount;
						double totalAmt = order.totalAmount;
						
						for (int j = i+1 ; j < orders.size(); j++)
						{
							Order mini = orders.get(j);
							
							// Use last status if the mini order status is now removed
							int miniStatus = mini.status;
							if (mini.status == Order.ORDER_STATUS_REMOVED)
								miniStatus = order.last_status;
							
							if (miniStatus == status && order.recipientId.equals(mini.recipientId))  
							{
								Log.v(TAG, "Adding mini order " + mini.orderId + " to order " + order.orderId);
								LinearLayout itemsView = (LinearLayout) order.view.findViewById(R.id.view_order_mini);
								for (Item item : mini.items) itemsView.addView(item.orderView(mInflater));

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
