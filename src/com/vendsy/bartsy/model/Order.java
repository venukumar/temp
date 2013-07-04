package com.vendsy.bartsy.model;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;
import com.vendsy.bartsy.utils.Utilities;

public class Order {

	private static final String TAG = "Order";
	
	// Each order has an ID that is unique within a session number
	public String clientID; 
	
	// this is the server-side manager ID used to identify the order on the server and tablet 
	public String serverID; 

	// Title and description are arbitrary strings
	public String title, description;
	public String itemId;
	
	// Dates
	public String updatedDate;
	public String orderDate;
	public String createdDate;

	
	// total = base + tax + fee + tip
    DecimalFormat df = new DecimalFormat();
	public float baseAmount;
	public float feeAmount;
	public float taxAmount;
	public float tipAmount;
	public float totalAmount;

	// Each order contains the sender and the recipient (another single in the bar or a friend to pick the order up)
	public UserProfile orderSender;
	public UserProfile orderReceiver;
	public String bartsyId = null; // our id
	public String senderId = null;
	public String senderNickname = null;
	public String senderImagePath = null;
	public String recipientId = null;
	public String recipientNickname = null;
	public String recipientImagePath = null;

	// The view displaying this order or null. The view is the display of the  order in a list.
	// The list could be either on the client or the server and it looks different in both cases but the code manages the differences.
	public View view = null;

	// Order states
	// (received) -> NEW -> (accepted) -> IN_PROGRESS -> (completed) -> READY -> (picked_up) -> COMPLETE 
	// 						(rejected) -> REJECTED (failed) -> FAILED (forgotten) -> INCOMPLETE
	//			  -> (timed out, error, etc) -> CANCELLED
	//			  -> (no server updates, timeout) -> TIMEDOUT
	// (offered)  -> OFFERED -> NEW...

	public static final int ORDER_STATUS_NEW 			= 0;
	public static final int ORDER_STATUS_REJECTED 		= 1;
	public static final int ORDER_STATUS_IN_PROGRESS 	= 2;
	public static final int ORDER_STATUS_READY 			= 3;
	public static final int ORDER_STATUS_FAILED 		= 4;
	public static final int ORDER_STATUS_COMPLETE 		= 5;
	public static final int ORDER_STATUS_INCOMPLETE 	= 6;
	public static final int ORDER_STATUS_CANCELLED 		= 7;
	public static final int ORDER_STATUS_OFFER_REJECTED = 8;
	public static final int ORDER_STATUS_OFFERED 		= 9;
	public static final int ORDER_STATUS_REMOVED		= 10;

	
	// These are local state (not sent to the  server used only for user notification purposes)
	public static final int ORDER_STATUS_TIMEOUT = 11;  // this is a local status used on the phone for orders expired locally
	public static final int ORDER_STATUS_COUNT = 12;
	
	public String type = "Custom";

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
			String image_resource, UserProfile order_sender, UserProfile order_receiver) {
		this.clientID = clientOrderID;
		this.serverID = serverOrderID;
		this.title = title;
		this.description = description;

		
		this.baseAmount = baseAmount;
		this.tipAmount = tipAmount;
		this.taxAmount = baseAmount * Constants.taxRate;
		this.totalAmount = this.taxAmount + this.tipAmount + this.baseAmount;
		
		// this.image_resource = Integer.parseInt(image_resource);
		this.orderSender = order_sender;
		this.senderId = order_sender.getBartsyId();
		this.orderReceiver = order_receiver;
		this.recipientId = order_receiver.getBartsyId();

		// Orders starts in the "NEW" status
		this.status = ORDER_STATUS_NEW;
		this.last_status = this.status;
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
	 * It will returns JSON format to update order status
	 */
	public JSONObject statusChangedJSON(){
		final JSONObject orderData = new JSONObject();
		try {
			orderData.put("orderId", serverID);
			orderData.put("orderStatus", status);
			orderData.put("orderRejectionReason", errorReason);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return orderData;
	}
	
	
	@Override
	public String toString() {
		
		JSONObject orderData = new JSONObject();
		
		try {
			orderData.put("itemId", itemId);
			orderData.put("itemName", title);
			orderData.put("basePrice", String.valueOf(baseAmount));
			orderData.put("tipPercentage", String.valueOf(tipAmount));
			orderData.put("totalPrice", String.valueOf(totalAmount));
			orderData.put("orderStatus", ORDER_STATUS_NEW);
			orderData.put("description", description);
			
			orderData.put("status", status);
			orderData.put("orderTimeout", timeOut);
			orderData.put("serverId", serverID);
			orderData.put("senderId", senderId);
			orderData.put("receiverId", recipientId);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return orderData.toString();
	}
	
	public String readableStatus() {
		String message = "";
		
		switch(status) {
		case ORDER_STATUS_NEW:
			message = title + " order placed.\nWe will let you know when it's in progress.";
			break;
		case ORDER_STATUS_REJECTED:
			message = title + " order rejected.\nUnfortunately the venue could not prepare it.";
			break;
		case ORDER_STATUS_IN_PROGRESS:
			message = title + " order in progress.\nIt should be ready in less than a minute.";
			break;
		case ORDER_STATUS_READY:
			message = title + " order ready.\nYou have " + timeOut + " minutes to pick it up, then you will be charged!";
			break;
		case ORDER_STATUS_FAILED:
			message = title + " order failed.\nPlease check with the venue.";
			break;
		case ORDER_STATUS_COMPLETE:
			message = title + " order picked up.\nPlease make sure you have it.";
			break;
		case ORDER_STATUS_INCOMPLETE:
			message = title + " not picked up.\nYou were charged for it because you didn't pick it up on time. Check with the venue for all refunds.";
			break;
		case ORDER_STATUS_CANCELLED:
			message = title + " timed out.\nIt was taking too long.";
			break;
		case ORDER_STATUS_OFFERED:
			message = title + " offered.\nAccept/reject?";
			break;
		case ORDER_STATUS_OFFER_REJECTED:
			message = title + " rejected.\nYour drink offer was not accepted";
			break;
		}
		return message;
	}
	
	
	/**
	 * Constructor to parse all the information from the JSON
	 * 
	 * @param json
	 */
	public Order(JSONObject json) {

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
				senderId = json.getString("senderBartsyId");
			if (json.has("senderNickname"))
				senderNickname = json.getString("senderNickname");
			if (json.has("senderImagePath"))
				senderImagePath = json.getString("senderImagePath");

			if (json.has("bartsyId"))
				recipientId = json.getString("bartsyId");
			if (json.has("recieverBartsyId"))
				recipientId = json.getString("recieverBartsyId");
			if (json.has("receiverBartsyId"))
				recipientId = json.getString("receiverBartsyId");			
			if (json.has("recipientBartsyId"))
				recipientId = json.getString("recipientBartsyId");
			if (json.has("recipientNickname"))
				recipientNickname = json.getString("recipientNickname");
			if (json.has("recipientImagePath"))
				recipientImagePath = json.getString("recipientImagePath");
			
			if (json.has("description"))
				description = json.getString("description");
			
			// Setup the order status if it exists or set it to NEW_ORDER if it doesn't
			if (json.has("orderStatus"))
				status = Integer.parseInt(json.getString("orderStatus"));
			else 
				status = ORDER_STATUS_NEW;

			// For now (hack) since the server is sending the wrong status for offered drinks, update it 
//			if (json.has("drinkOffered") && json.getBoolean("drinkOffered") && status == ORDER_STATUS_NEW) {
//				status = ORDER_STATUS_OFFERED;
//			}
			
			// Used only by the getPastOrders syscall
			if (json.has("dateCreated")) 
				createdDate = json.getString("dateCreated");

			// Setup created date (time the order was placed)
			if (json.has("orderTime")) {
				// User server provided creation date in the following format: 27 Jun 2013 12:03:04 GMT
				createdDate = json.getString("orderTime");
				state_transitions[ORDER_STATUS_NEW] = parseWeirdDate(createdDate);
			} else {
				// If no created date use current date for the creation state
				state_transitions[ORDER_STATUS_NEW] = new Date();
			}

			// Setup last updated date (time the order was updated last)  *** MAKE SURE to have updated status before getting here ***
			if (json.has("updateTime")) {
				// User server provided creation date in the following format: 27 Jun 2013 12:03:04 GMT
				updatedDate = json.getString("updateTime");
				state_transitions[status] = parseWeirdDate(json.getString("updateTime"));
			} else {
				// If no created date use current date for the update time
				state_transitions[status] = new Date();
			}

			if (json.has("orderTimeout"))
				timeOut = json.getInt("orderTimeout");
			
			// Set up last status based on current status
			switch (status) {
			case ORDER_STATUS_OFFERED:
				last_status = status;
				break;
			case ORDER_STATUS_NEW:
				last_status = status;
				break;
			case ORDER_STATUS_IN_PROGRESS:
				last_status = ORDER_STATUS_READY;
				break;
			case ORDER_STATUS_READY:
				last_status = ORDER_STATUS_IN_PROGRESS;
				break;
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		this.taxAmount = totalAmount - tipAmount - baseAmount;

		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

	}

	
	Date parseWeirdDate(String date) {
		Date d = Utilities.getLocalDateFromGTMString(createdDate, "dd MMM yyyy HH:mm:ss 'GMT'");
		if (d == null)
			d = Utilities.getLocalDateFromGTMString(createdDate, "d MMM yyyy HH:mm:ss 'GMT'");
		return d;
	}
	

	public String getRecipientName(ArrayList<UserProfile> people) {
 
		if (recipientNickname != null)
			return recipientNickname;
		
		if (orderReceiver != null)
			return orderReceiver.getName();
		
		if (recipientId == null)
			return "<unkown>";
					
		for (UserProfile p : people) {
			if (p.getBartsyId().equals(recipientId)) {
				// User found
				orderReceiver = p;
				return p.getName();
			}
		}
		return "<unkown>";
	}
	
	

	public void updateStatus(int status) {
		
		if (status == ORDER_STATUS_REMOVED) {
			Log.i(TAG, "Skipping status update of order " + serverID + " from " + this.status + " to " + status + " with last status " + last_status);
			return;
		}
		
		last_status = status;
		this.status = status;
		state_transitions[status] = new Date();

		// Log the state change and update the order with an error reason
		Log.i(TAG, "Order " + serverID + " changed status from " + last_status + " to " + status + " for reason: "  + errorReason);
//		this.errorReason = errorReason;
	}
	

	public void setTimeoutState() {
		
		Log.v(TAG, "setTimeoutState()");

		if (status == ORDER_STATUS_NEW || status == ORDER_STATUS_READY || status == ORDER_STATUS_IN_PROGRESS) {
			// Change status of orders 
			last_status = status;
			status = ORDER_STATUS_TIMEOUT;
			state_transitions[status] = new Date();
			errorReason = "Server unreachable. Check your internet connection and notify Bartsy customer support.";

			Log.v(TAG, "Order " + this.serverID + " moved from state " + last_status + " to timeout state " + status);
		} else {
			Log.v(TAG, "Order " + this.serverID + "with last status " + last_status + " not changed to timeout status because the status was " + status + " with reason " + errorReason);
			return;
		}
	}


	public View updateView(LayoutInflater inflater, ViewGroup container) {

		Log.v(TAG, "updateView()");
		Log.v(TAG, "Order sender   :" + orderSender);
		Log.v(TAG, "Order receiver :" + orderReceiver);
		
		view = (View) inflater.inflate(R.layout.orders_open_item_list, container, false);

		// Update header
		((TextView) view.findViewById(R.id.view_order_item_number)).setText("" + serverID);

		// Update item details
		((TextView) view.findViewById(R.id.view_order_mini_price)).setText(df.format(baseAmount));
		((TextView) view.findViewById(R.id.view_order_title)).setText(this.title + getTitleModifier());
		if (description != null || description.equalsIgnoreCase(""))
			((TextView) view.findViewById(R.id.view_order_description)).setVisibility(View.GONE);
		else
			((TextView) view.findViewById(R.id.view_order_description)).setText(this.description);

		// Update the order's tip, tax and total
		updateTipTaxTotalView(tipAmount, taxAmount, totalAmount);
		
		// To display order receiver profile information in orders view
		updateProfileView();

		int orderStatus = status;
		if (orderStatus == ORDER_STATUS_REMOVED)
			orderStatus = last_status;
		
		switch (orderStatus) {
		case ORDER_STATUS_OFFERED:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText(
					"You were offered a drink!");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.darker_gray);
			view.findViewById(R.id.view_order_footer).setVisibility(View.VISIBLE);
			view.findViewById(R.id.view_order_footer_reject).setTag(this);
			view.findViewById(R.id.view_order_footer_accept).setTag(this);
			break;
		case ORDER_STATUS_OFFER_REJECTED:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText(
					"Your drink offer was rejected by " + recipientId + ".");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.darker_gray);
			view.findViewById(R.id.view_order_notification_button).setVisibility(View.VISIBLE);
			view.findViewById(R.id.view_order_notification_button).setTag(this);
			break;
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
		case ORDER_STATUS_COMPLETE:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText(
					"Your order was picked up.");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.holo_green_dark);
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
		
		
		// Compute timers. Placed shows the time since the order was placed. Expires shows the time left in the current state until timeout.
		double current_ms	= System.currentTimeMillis() ;
		double timeout_ms	= timeOut * 60000;
		double elapsed_ms	= current_ms - state_transitions[ORDER_STATUS_NEW].getTime();
		double left_ms     	= timeout_ms - (current_ms - state_transitions[status].getTime());
		double elapsed_min 	= elapsed_ms / (double) 60000;
		double left_min    	= Math.ceil(left_ms / (double) 60000);
		

		// Update timer since the order was placed
		((TextView) view.findViewById(R.id.view_order_timer)).setText(String.valueOf((int)elapsed_min)+" min");

		// Handle timeout views
		if (orderStatus == ORDER_STATUS_CANCELLED || orderStatus == ORDER_STATUS_TIMEOUT) {

			// Update timeout counter to always be expired even if there is some left (due to clock inconsistencies between local and server)
			((TextView) view.findViewById(R.id.view_order_timeout_value)).setText("Expired");
			view.findViewById(R.id.view_order_expires_text).setVisibility(View.GONE);
			
		} else {

			// Update timeout counter		
			if (left_min > 0) 
				((TextView) view.findViewById(R.id.view_order_timeout_value)).setText("< " + String.valueOf((int)left_min)+" min");
			else {
				((TextView) view.findViewById(R.id.view_order_timeout_value)).setText("Expriring...");
				view.findViewById(R.id.view_order_expires_text).setVisibility(View.GONE);
			}
		}
	
		
		return view;

	}
	
	/**
	 * Update the view of the order only with the given tip, tax and total amounts 
	 */
	public void updateTipTaxTotalView(float tipAmount,float taxAmount,float totalAmount){
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
	private void updateProfileView() {
		
		// Show sender details
		if (orderSender != null && senderId != null && recipientId != null && !senderId.equals(recipientId)) {

			// Display or download image if available
			if (orderSender.hasImage()) {
				((ImageView)view.findViewById(R.id.view_order_sender_image)).setImageBitmap(orderSender.getImage());
			} else if (orderSender.hasImagePath()) {
				WebServices.downloadImage(orderSender, ((ImageView)view.findViewById(R.id.view_order_sender_image)));
			} else {
				view.findViewById(R.id.view_sender).setVisibility(View.GONE);
				view.findViewById(R.id.view_order_from).setVisibility(View.GONE);
			}

			// Show nick name if available
			if (orderSender.hasNickname())  {
				((TextView) view.findViewById(R.id.view_order_sender)).setText(orderSender.getNickname());	
			} else {
				view.findViewById(R.id.view_order_from).setVisibility(View.GONE);					
			}
		} else {
			view.findViewById(R.id.view_sender).setVisibility(View.GONE);
			view.findViewById(R.id.view_order_from).setVisibility(View.GONE);
		}
		
		// Show receiver details
		if (orderReceiver != null) {

			// Display or download image if available
			if (orderReceiver.hasImage()) {
				((ImageView)view.findViewById(R.id.view_order_recipient_image)).setImageBitmap(orderReceiver.getImage());
			} else if (orderReceiver.hasImagePath()) {
				WebServices.downloadImage(orderReceiver, ((ImageView)view.findViewById(R.id.view_order_recipient_image)));
			} else {
				view.findViewById(R.id.view_recipient).setVisibility(View.GONE);
				view.findViewById(R.id.view_order_for).setVisibility(View.GONE);
			}

			// Show nick name if available
			if (orderReceiver.hasNickname())  {
				((TextView) view.findViewById(R.id.view_order_recipient)).setText(orderReceiver.getNickname());	
			} else {
				view.findViewById(R.id.view_order_for).setVisibility(View.GONE);					
			}
		} else {
			view.findViewById(R.id.view_recipient).setVisibility(View.GONE);
			view.findViewById(R.id.view_order_for).setVisibility(View.GONE);
		}
		
		
	}

	private String getTitleModifier() {
		
		String recipient = "";
		if (!recipientId.equals(bartsyId)) {
			if (orderReceiver != null) 
				recipient = "\n(for: " + orderReceiver.getNickname() + ")";
			else 
				recipient = "\n(for another user)";
		} else if (!senderId.equals(bartsyId)){
			if (orderSender != null)
				recipient = "\n(from: " + orderReceiver.getNickname() + ")";
			else 
				recipient = "\n(from another user)";
		}
		return recipient;
	}
	
	public View getMiniView(LayoutInflater inflater, ViewGroup container ) {
		
		LinearLayout view = (LinearLayout) inflater.inflate(R.layout.orders_open_item, container, false);
		
		((TextView) view.findViewById(R.id.view_order_mini_price)).setText(df.format(baseAmount));

		
		((TextView) view.findViewById(R.id.view_order_title)).setText(this.title + getTitleModifier());
		if (description != null || description.equalsIgnoreCase(""))
			((TextView) view.findViewById(R.id.view_order_description)).setVisibility(View.GONE);
		else
			((TextView) view.findViewById(R.id.view_order_description)).setText(this.description);

		return view;
	}


}