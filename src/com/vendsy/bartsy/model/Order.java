package com.vendsy.bartsy.model;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.zip.Inflater;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.service.BackgroundService;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;

public class Order {

	private static final String TAG = "Order";
	
	// Each order has an ID that is unique within a session number
	public String clientID; // this is the client-side managed ID used to
							// identify an order when status is received
	public String serverID; // this is the server-side manager ID used to
							// identify the order on the server and tablet
	// and also to let the user know what order the shoudl be asking for when
	// they get to the bartender

	// Title and description are arbitrary strings
	public String title, description;
	public String itemId;

	// The total price is in the local denomination and is the sum of price *
	// quantity, fee and tax

	public int image_resource;
	
	// Dates
	public String updatedDate;
	public String orderDate;
	public String createdDate;

	
	// Fees: total = base + tax + fee + tip
	public float baseAmount;
	public float feeAmount;
	public float taxAmount;
	public float tipAmount;
	public float totalAmount;
	
    DecimalFormat df = new DecimalFormat();


	// Each order contains the sender and the recipient (another single in the
	// bar or a friend to pick the order up)
	public UserProfile orderSender;
	public UserProfile orderReceiver;

	// The view displaying this order or null. The view is the display of the
	// order in a list.
	// The list could be either on the client or the server and it looks
	// different in both cases
	// but the code manages the differences.
	public View view = null;

	// Order states
	// (received) -> NEW -> (accepted) -> IN_PROGRESS -> (completed) -> READY -> (picked_up) -> COMPLETE 
	//			  -> (timed out, error, etc) -> CANCELLED
	// 						(rejected) -> REJECTED (failed) -> FAILED (forgotten) -> INCOMPLETE

	public static final int ORDER_STATUS_NEW = 0;
	public static final int ORDER_STATUS_REJECTED = 1;
	public static final int ORDER_STATUS_IN_PROGRESS = 2;
	public static final int ORDER_STATUS_READY = 3;
	public static final int ORDER_STATUS_FAILED = 4;
	public static final int ORDER_STATUS_COMPLETE = 5;
	public static final int ORDER_STATUS_INCOMPLETE = 6;
	public static final int ORDER_STATUS_CANCELLED = 7;
	public static final int ORDER_STATUS_TIMEOUT = 8;
	public static final int ORDER_STATUS_COUNT = 9;
	
	public String type = "Custom";

	// The states are implemented in a status variable and each state transition
	// has an associated time
	public int status;
	public int last_status;	// the previous status of this order (needed for timeouts in particular)
	public int timeOut;			// time in minutes this order has before it times out (from the last updated state)
	public Date[] state_transitions = new Date[ORDER_STATUS_COUNT];
	private String errorReason = ""; // used to send an error reason for negative order states


	/**
	 * When an order is initialized the state transition times are undefined
	 * except for the first state, which is when the order is received
	 */
	public void initialize(String clientOrderID, String serverOrderID,
			String title, String description, float baseAmount, float tipAmount,
			String image_resource, UserProfile order_sender) {
		this.clientID = clientOrderID;
		this.serverID = serverOrderID;
		this.title = title;
		this.description = description;

		
		this.baseAmount = baseAmount;
		this.tipAmount = tipAmount;
		this.taxAmount = baseAmount * Constants.taxRate;
		this.totalAmount = this.taxAmount + this.tipAmount + this.baseAmount;
		
		this.image_resource = R.drawable.drinks; // for now always use the same
													// picture for drinks
		// this.image_resource = Integer.parseInt(image_resource);
		this.orderSender = order_sender;

		// Orders starts in the "NEW" status
		this.status = ORDER_STATUS_NEW;
		this.state_transitions[this.status] = new Date();

		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

	}

	public Order() {
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);
	}

	/**
	 * It will returns JSON format to place order
	 */
	public JSONObject getPlaceOrderJSON() {
		final JSONObject orderData = new JSONObject();
		try {
			orderData.put("itemId", itemId);
			orderData.put("itemName", title);
			orderData.put("basePrice", String.valueOf(baseAmount));
			orderData.put("tipPercentage", String.valueOf(tipAmount));
			orderData.put("totalPrice", String.valueOf(totalAmount));
			orderData.put("orderStatus", ORDER_STATUS_NEW);
			orderData.put("description", description);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

		return orderData;
	}

	/**
	 * Constructor to parse all the information from the JSON
	 * 
	 * @param json
	 */
	public Order(JSONObject json) {

		String sender = null;
		String receiver = null;
		
		try {
			status = Integer.valueOf(json.getString("orderStatus"));
			state_transitions[status] = new Date(); // For now just use current date. the date should come in the syscall 
			title = json.getString("itemName");
			
			baseAmount = Float.valueOf(json.getString("basePrice"));
			serverID = json.getString("orderId");
			tipAmount = Float.valueOf(json.getString("tipPercentage"));
			totalAmount = Float.valueOf(json.getString("totalPrice"));
			taxAmount = baseAmount * Constants.taxRate;
			description = json.getString("description");

			if (json.has("itemId"))
				itemId = json.getString("itemId");

			if (json.has("senderBartsyId"))
				sender = json.getString("senderBartsyId");
			receiver = json.getString("recieverBartsyId");
			
			if (json.has("description"))
				description = json.getString("description");
			
			if (json.has("orderTime"))
				updatedDate = json.getString("orderTime");
			if (json.has("dateCreated"))
			  createdDate = json.getString("dateCreated");
			
			if (json.has("orderStatus"))
				status = Integer.parseInt(json.getString("orderStatus"));

			if (json.has("dateCreated"))
				  createdDate = json.getString("dateCreated");
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		this.taxAmount = totalAmount - tipAmount - baseAmount;

		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

	}

	/**
	 * To process next positive state for the 0rder
	 */
	public void nextPositiveState() {
		switch (this.status) {
		case ORDER_STATUS_NEW:
			last_status = status;
			this.status = ORDER_STATUS_IN_PROGRESS;
			break;
		case ORDER_STATUS_IN_PROGRESS:
			last_status = status;
			this.status = ORDER_STATUS_READY;
			break;
		case ORDER_STATUS_READY:
			last_status = status;
			this.status = ORDER_STATUS_COMPLETE;
			break;
		}

		// Mark the time of the state transition in the timetable
		state_transitions[status] = new Date();
	}

	public void setCancelledState() {
		
		last_status = status;
		status = ORDER_STATUS_CANCELLED;
		state_transitions[status] = new Date();
	}

	public void setTimeoutState() {
		
		// Don't change orders that have already this status because their last_status would get lost
		if (status == ORDER_STATUS_TIMEOUT ||
				status == ORDER_STATUS_REJECTED ||
				status == ORDER_STATUS_FAILED ||
				status == ORDER_STATUS_INCOMPLETE) 
			return;
		
		last_status = status;
		status = ORDER_STATUS_CANCELLED;
		state_transitions[status] = new Date();
	}

	/**
	 * To process next negative state for the order
	 */
	
	public void nextNegativeState(String errorReason) {
		
		
		int oldStatus = status;
		
		switch (status) {
		case ORDER_STATUS_NEW:
			last_status = status;
			status = ORDER_STATUS_REJECTED;
			break;
		case ORDER_STATUS_IN_PROGRESS:
			last_status = status;
			status = ORDER_STATUS_FAILED;
			break;
		case ORDER_STATUS_READY:
			last_status = status;
			status = ORDER_STATUS_INCOMPLETE;
			break;
		}
		
		// Log the state change and update the order with an error reason
		Log.i(TAG, "Order " + serverID + " changed status from " + oldStatus + " to " + status + " for reason: "  + errorReason);
		this.errorReason = errorReason;
		
		// Mark the time of the state transition in the timetable
		state_transitions[status] = new Date();
	}
	
	
	public View updateView(LayoutInflater inflater, ViewGroup container) {

		Log.v(TAG, "updateView()");
		Log.v(TAG, "Order sender   :" + orderSender);
		Log.v(TAG, "Order receiver :" + orderReceiver);
		
		view = (View) inflater.inflate(R.layout.orders_open_item_list, container, false);
		
		((TextView) view.findViewById(R.id.view_order_title)).setText(this.title);
		if (description != null || description.equalsIgnoreCase(""))
			((TextView) view.findViewById(R.id.view_order_description)).setVisibility(View.GONE);
		else
			((TextView) view.findViewById(R.id.view_order_description)).setText(this.description);

		//		((TextView) view.findViewById(R.id.view_order_time)).setText(DateFormat.getTimeInstance().format(this.state_transitions[status]));
//		((TextView) view.findViewById(R.id.view_order_date)).setText(DateFormat.getDateInstance().format(this.state_transitions[status]));

		((TextView) view.findViewById(R.id.view_order_mini_price)).setText(df.format(baseAmount));

		((TextView) view.findViewById(R.id.view_order_tip_amount)).setText(df.format(tipAmount));
		((TextView) view.findViewById(R.id.view_order_tax_amount)).setText(df.format(taxAmount));
		((TextView) view.findViewById(R.id.view_order_total_amount)).setText(df.format(totalAmount));

		((TextView) view.findViewById(R.id.view_order_item_number)).setText("" + serverID);
		
		// To display order receiver profile information in orders view
		if(orderReceiver!=null){
			updateProfileView(orderReceiver);
		}

		switch (this.status) {
		case ORDER_STATUS_NEW:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText("Your order is waiting to be accepted.");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.darker_gray);
			break;
		case ORDER_STATUS_REJECTED:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText(
					"Your order was rejected. Please check with the venue. You werent' charged.");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.holo_red_dark);
			view.findViewById(R.id.view_order_notification_button).setVisibility(View.VISIBLE);
			view.findViewById(R.id.view_order_notification_button).setTag(this);
			break;
		case ORDER_STATUS_FAILED:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText(
					"Your order failed. Please check with the venue. You werent' charged.");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.holo_red_dark);
			view.findViewById(R.id.view_order_notification_button).setVisibility(View.VISIBLE);
			view.findViewById(R.id.view_order_notification_button).setTag(this);
			break;
		case ORDER_STATUS_IN_PROGRESS:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText("Your order was accepted and is being worked on.");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.holo_orange_dark);
			break;
		case ORDER_STATUS_READY:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText("Your order is ready for pickup!");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.holo_green_dark);
			break;
		case ORDER_STATUS_INCOMPLETE:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText(
					"Your order was not picked up. Check with the venue. You werent' charged.");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.holo_red_dark);
			view.findViewById(R.id.view_order_notification_button).setVisibility(View.VISIBLE);
			view.findViewById(R.id.view_order_notification_button).setTag(this);
			break;
		case ORDER_STATUS_CANCELLED:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText(
					"Your order was taking too long and it expired. You weren't charged.");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.holo_red_dark);
			view.findViewById(R.id.view_order_notification_button).setVisibility(View.VISIBLE);
			view.findViewById(R.id.view_order_notification_button).setTag(this);
			break;
		case ORDER_STATUS_TIMEOUT:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText(
					"Connectivity issues. Check with the venue immediately.");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.holo_red_dark);
			view.findViewById(R.id.view_order_notification_button).setVisibility(View.VISIBLE);
			view.findViewById(R.id.view_order_notification_button).setTag(this);
			break;
		}
		
		
		/*		

		// Compute timers
		long timeout_ms	 = timeOut * 60000;
		long elapsed_ms;
		if (state_transitions[last_status] != null)
			elapsed_ms = System.currentTimeMillis() - (state_transitions[last_status]).getTime();
		else
			// For now don't bother resetting timers in the case we just uninstalled and reinstalled the app
			elapsed_ms = 0;
		long left_ms     = timeout_ms - elapsed_ms;
		long elapsed_min = elapsed_ms / 60000;
		long left_min    = left_ms / 60000;
		
		// Set the background color of the order depending on how much time has elapsed as a percent of the timeout green->orange->red
		if (elapsed_ms <= timeout_ms / 3.0)
			view.findViewById(R.id.view_order_header).setBackgroundResource(android.R.color.holo_green_dark);
		else if (elapsed_ms <=  timeout_ms * 2.0 / 3.0)
			view.findViewById(R.id.view_order_header).setBackgroundResource(android.R.color.holo_orange_dark);
		else
			view.findViewById(R.id.view_order_header).setBackgroundResource(android.R.color.holo_red_dark);


		// Update timer since last state
		((TextView) view.findViewById(R.id.view_order_timer)).setText(String.valueOf(elapsed_min)+" min");

		// Update timout counter		
		if (left_min > 0) 
			((TextView) view.findViewById(R.id.view_order_timeout)).setText(String.valueOf(left_min)+" min");
		else
			((TextView) view.findViewById(R.id.view_order_timeout)).setText("0 min");

		((Button) view.findViewById(R.id.view_order_button_positive)).setText(positive);
		((Button) view.findViewById(R.id.view_order_button_positive)).setTag(this);
		((Button) view.findViewById(R.id.view_order_button_negative)).setText(negative);
		((Button) view.findViewById(R.id.view_order_button_negative)).setTag(this);
		((Button) view.findViewById(R.id.view_order_button_remove)).setTag(this);
	
*/		
		
		return view;

	}
	
	/**
	 * To update latest calculated price amounts(tip, tax and totals) in the view
	 */
	public void updatePrice(float tipAmount,float taxAmount,float totalAmount){
		if(view!=null){
			((TextView) view.findViewById(R.id.view_order_tip_amount)).setText(df.format(tipAmount));
			((TextView) view.findViewById(R.id.view_order_tax_amount)).setText(df.format(taxAmount));
			((TextView) view.findViewById(R.id.view_order_total_amount)).setText(df.format(totalAmount));
		}
	}
	
	/**
	 * To update profile information in orders view
	 * 
	 * @param profile
	 */
	private void updateProfileView(UserProfile profile) {
		
		ImageView profileImageView = ((ImageView)view.findViewById(R.id.view_order_profile_picture));
		// Set profile image
		if (!profile.hasImage() && profileImageView!=null) {
			WebServices.downloadImage(Constants.DOMAIN_NAME + profile.getImagePath(), profile, profileImageView);
		} else {
			profileImageView.setImageBitmap(profile.getImage());
		}
		
	
		// Show the username of the recipient
		((TextView) view.findViewById(R.id.view_order_profile_name)).setText(profile.getNickname());	
	}

	
	public View getMiniView(LayoutInflater inflater, ViewGroup container ) {
		
		LinearLayout view = (LinearLayout) inflater.inflate(R.layout.orders_open_item, container, false);
		
		((TextView) view.findViewById(R.id.view_order_title)).setText(this.title);
		((TextView) view.findViewById(R.id.view_order_description)).setText(this.description);
		((TextView) view.findViewById(R.id.view_order_mini_price)).setText(df.format(baseAmount)); // use int for now
		
		return view;
	}


}
