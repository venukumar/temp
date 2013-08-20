package com.vendsy.bartsy.model;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;
import com.vendsy.bartsy.utils.Utilities;

public class Order {

	private static final String TAG = "Order";
	
	// this is the server-side manager ID used to identify the order on the server and tablet 
	public String orderId; 

	// List of items in this order
	public ArrayList<Item> items = new ArrayList<Item>();
	
	// Dates
	public String updatedDate;
	public String orderDate;
	public String createdDate;

	
	// total = base + tax + fee + tip
    DecimalFormat df = new DecimalFormat();
	public double baseAmount	= 0;
	public double feeAmount	= 0;
	public double taxAmount	= 0;
	public double tipAmount	= 0;
	public double totalAmount= 0;
	public double taxRate	= 0;

	// Each order contains the sender and the recipient (another single in the bar or a friend to pick the order up)
	public UserProfile orderSender;
	public UserProfile orderRecipient;
	public String bartsyId = null; // our id
	public String senderId = null;
	public String senderNickname = null;
	public String senderImagePath = null;
	public String recipientId = null;
	public String recipientNickname = null;
	public String recipientImagePath = null;
	public String userSessionCode = null;
	private String orderInstructions = null;

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
	public static final int ORDER_STATUS_TIMEOUT 		= 11;  // this is a local status used on the phone for orders expired locally
	public static final int ORDER_STATUS_COUNT 			= 12;
	
	public String type = "Custom";

	public int status;
	public int last_status;	// the previous status of this order (needed for timeouts in particular)
	public int timeOut;			// time in minutes this order has before it times out (from the last updated state)
	public Date[] state_transitions = new Date[ORDER_STATUS_COUNT];
	private String errorReason = ""; // used to send an error reason for negative order states


	/**
	 * 
	 * TODO - Constructors/parsers
	 * 
	 * When an order is initialized the state transition times are undefined except for the first state, 
	 * which is when the order is received
	 */
	
	
	public Order(double taxRate) {
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);
		this.taxRate = taxRate;
	}

	public Order (String ourBartsyId, UserProfile order_sender, UserProfile order_receiver, double taxRate, double tipRate, Item item) {
		
		this.items.add(item);
		this.bartsyId = ourBartsyId;
		
		this.baseAmount = item.getOrderPrice();
		this.tipAmount = tipRate * this.baseAmount;
		this.taxAmount = baseAmount * taxRate;
		this.totalAmount = this.taxAmount + this.tipAmount + this.baseAmount;
		this.taxRate = taxRate;
		
		this.orderSender = order_sender;
		this.senderId = order_sender.getBartsyId();
		this.orderRecipient = order_receiver;
		this.recipientId = order_receiver.getBartsyId();

		// Orders starts in the "NEW" status
		this.status = ORDER_STATUS_NEW;
		this.last_status = this.status;
		this.state_transitions[this.status] = new Date();

		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);
	}

	public Order(String ourBartsyId, UserProfile sender, UserProfile recipient, double taxRate) {
		
		this.bartsyId = ourBartsyId;

		this.baseAmount = 0;
		this.tipAmount = 0;
		this.taxAmount = 0;
		this.totalAmount = 0;
		this.taxRate = taxRate;
		
		this.orderSender = sender;
		this.senderId = sender.getBartsyId();
		
		this.orderRecipient = recipient;
		this.recipientId = recipient.getBartsyId();
		
		this.status = ORDER_STATUS_NEW;
		
		// Orders starts in the "NEW" status
		this.status = ORDER_STATUS_NEW;
		this.last_status = this.status;
		this.state_transitions[this.status] = new Date();

		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

	}

	public Order(String ourBartsyId, JSONObject json) {

		this.bartsyId = ourBartsyId;

		try {

			if (json.has("itemsList")) {
				JSONArray itemsJSON = json.getJSONArray("itemsList");
				
				for (int i=0 ; i < itemsJSON.length() ; i++) {
					items.add(new Item(itemsJSON.getJSONObject(i), null));
				}
			}
			
			if (json.has("basePrice"))
				baseAmount = Double.valueOf(json.getString("basePrice"));

			if (json.has("tipPercentage"))
				tipAmount = Double.valueOf(json.getString("tipPercentage"));
			
			orderId = json.getString("orderId");
			
			totalAmount = Double.valueOf(json.getString("totalPrice"));
			taxAmount = totalAmount - tipAmount - baseAmount;


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
			if(json.has("recieverName")){
				recipientNickname = json.getString("recieverName");
			}
			if (json.has("recipientImagePath"))
				recipientImagePath = json.getString("recipientImagePath");
			if (json.has("recieverImage")){
				recipientImagePath = json.getString("recieverImage");
			}
			
			// Setup the order status if it exists or set it to NEW_ORDER if it doesn't
			if (json.has("orderStatus"))
				status = Integer.parseInt(json.getString("orderStatus"));
			else 
				status = ORDER_STATUS_NEW;

			// Used only by the getPastOrders syscall
			if (json.has("dateCreated")) 
				createdDate = json.getString("dateCreated");

			// Setup created date (time the order was placed)
			if (json.has("orderTime")) {
				// User server provided creation date in the following format: 27 Jun 2013 12:03:04 GMT
				createdDate = json.getString("orderTime");
				if (json.has("currentTime")) {
					state_transitions[ORDER_STATUS_NEW] = adjustDate(json.getString("currentTime"), createdDate);
				} else {
					state_transitions[ORDER_STATUS_NEW] = Utilities.getLocalDateFromGMTString(createdDate, "dd MMM yyyy HH:mm:ss 'GMT'");
				}
			} else {
				// If no created date use current date for the creation state
				state_transitions[ORDER_STATUS_NEW] = new Date();
			}

			// Setup last updated date (time the order was updated last)  *** MAKE SURE to have updated status before getting here ***
			if (json.has("updateTime")) {
				// User server provided creation date in the following format: 27 Jun 2013 12:03:04 GMT
				updatedDate = json.getString("updateTime");
				if (json.has("currentTime")) {
					state_transitions[status] = adjustDate(json.getString("currentTime"), updatedDate);
				} else {
					state_transitions[status] = Utilities.getLocalDateFromGMTString(updatedDate, "dd MMM yyyy HH:mm:ss 'GMT'");
				}
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
			
			// Update the last status of the order (this is sent for past orders)
			if (json.has("lastState"))
				last_status = Integer.parseInt(json.getString("lastState"));
			
			// Set up user session code
			if (json.has("userSessionCode")) 
				userSessionCode = json.getString("userSessionCode");
			
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
	 * TODO - Serializers
	 */

	public JSONObject toJson() throws JSONException {
		
		JSONObject json = new JSONObject();

		JSONArray jsonItems = new JSONArray();
		for (Item item : items)
			jsonItems.put(item.toJson());
		json.put("itemsList", jsonItems);
	
		json.put("basePrice", String.valueOf(baseAmount));
		json.put("tipPercentage", String.valueOf(tipAmount));
		json.put("totalPrice", String.valueOf(totalAmount));
		json.put("orderStatus", Integer.toString(status));
		
		if (has(orderInstructions))
			json.put("orderInstructions", orderInstructions);

		return json;
	}

	/**
	 * It will returns JSON format to update order status
	 */
	public JSONObject getStatusChangedJSON(){
		JSONObject orderData = new JSONObject();
		try {
			orderData.put("orderId", orderId);
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
			
			// Additional fields not already in the place order JSON
			orderData.put("orderTimeout", timeOut);
			orderData.put("serverId", orderId);
			orderData.put("senderId", senderId);
			orderData.put("receiverId", recipientId);
			orderData.put("orderStatus", status);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			return toJson() + orderData.toString();
		} catch (JSONException e) {
			e.printStackTrace();
			return orderData.toString();
		}
	}
	
	public String readableStatus() {
		String message = "";
		
		int orderStatus = status;
		
		if (status == ORDER_STATUS_REMOVED)
			orderStatus = last_status;
		
		switch(orderStatus) {
		case ORDER_STATUS_NEW:
			message = "New order placed.\nWe will let you know when it's in progress.";
			break;
		case ORDER_STATUS_REJECTED:
			message = "Order rejected.\nUnfortunately the venue could not prepare it.";
			break;
		case ORDER_STATUS_IN_PROGRESS:
			message = "Order in progress.\nIt should be ready in less than a minute.";
			break;
		case ORDER_STATUS_READY:
			message = "Order ready.\nYou have " + timeOut + " minutes to pick it up, then you will be charged!";
			break;
		case ORDER_STATUS_FAILED:
			message = "Order failed.\nPlease check with the venue.";
			break;
		case ORDER_STATUS_COMPLETE:
			message = "Order picked up.\nPlease make sure you have it.";
			break;
		case ORDER_STATUS_INCOMPLETE:
			message = "Order not picked up.\nYou were charged for it because you didn't pick it up on time. Check with the venue for all refunds.";
			break;
		case ORDER_STATUS_CANCELLED:
			message = "Order timed out.\nYour were not charged. Please check with the venue.";
			break;
		case ORDER_STATUS_OFFERED:
			message = "Order offered.\nAccept/reject?";
			break;
		case ORDER_STATUS_OFFER_REJECTED:
			message = "Order rejected.\nYour drink offer was not accepted";
			break;
		case ORDER_STATUS_TIMEOUT:
			message = "Order timed out due to internet issues.\nPlease check on your order with the venue.";
			break;
		default:
			message = "Order is in an illegal status.";
		}
		return message;
	}
	
	
	
	
	/**
	 * TODO - States
	 */

	public void updateStatus(int status) {
		
		if (this.status == ORDER_STATUS_REMOVED) {
			Log.i(TAG, "Skipping status update of order " + orderId + " from " + this.status + " to " + status + " with last status " + last_status);
			return;
		}
		
		last_status = this.status;
		this.status = status;
		state_transitions[status] = new Date();

		// Log the state change and update the order with an error reason
		Log.i(TAG, "Order " + orderId + " changed status from " + last_status + " to " + status + " for reason: "  + errorReason);
//		this.errorReason = errorReason;
	}
	

	public void setTimeoutState() {
		
		Log.v(TAG, "setTimeoutState()");

		if (status == ORDER_STATUS_NEW || status == ORDER_STATUS_READY || status == ORDER_STATUS_IN_PROGRESS || status == ORDER_STATUS_OFFERED) {
			// Change status of orders 
			last_status = status;
			status = ORDER_STATUS_TIMEOUT;
			state_transitions[status] = new Date();
			errorReason = "Server unreachable. Check your internet connection and notify Bartsy customer support.";

			Log.v(TAG, "Order " + this.orderId + " moved from state " + last_status + " to timeout state " + status);
		} else {
			Log.v(TAG, "Order " + this.orderId + "with last status " + last_status + " not changed to timeout status because the status was " + status + " with reason " + errorReason);
			return;
		}
	}

	
	/**
	 * TODO - Views
	 */

	public View updateView(LayoutInflater inflater, ViewGroup container, HashMap<String, Bitmap> cache) {

		Log.v(TAG, "updateView()");
		Log.v(TAG, "Order sender   :" + orderSender);
		Log.v(TAG, "Order receiver :" + orderRecipient);
		
		view = (View) inflater.inflate(R.layout.open_orders_item, container, false);

		// Update header
		((TextView) view.findViewById(R.id.view_order_item_number)).setText(orderId);
		
		// Add the item list
		LinearLayout itemsView = (LinearLayout) view.findViewById(R.id.view_order_mini);
		for (Item item : items) itemsView.addView(item.orderView(inflater));
		
		// Update the order's tip, tax and total
		updateTipTaxTotalView(tipAmount, taxAmount, totalAmount);
		
		// To display order receiver profile information in orders view
		updateProfileView(cache);

		// If the order has been removed already (and we're showing for UI reasons), use the last status for showing the state
		int orderStatus = status;
		if (orderStatus == ORDER_STATUS_REMOVED)
			orderStatus = last_status;

		// Set the status related fields of the view
		switch (orderStatus) {
		case ORDER_STATUS_OFFERED:
			if (senderId.equals(bartsyId)) {
				// We are the sender 
				((TextView) view.findViewById(R.id.view_order_state_description)).setText("Waiting for recipient to accept/reject your offer");
				((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.darker_gray);				
			} else {
				// We are the recipient
				((TextView) view.findViewById(R.id.view_order_state_description)).setText("You were offered a drink!");
				((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.darker_gray);
				view.findViewById(R.id.view_order_footer).setVisibility(View.VISIBLE);
				view.findViewById(R.id.view_order_footer_reject).setTag(this);
				view.findViewById(R.id.view_order_footer_accept).setTag(this);
			}
			break;
		case ORDER_STATUS_OFFER_REJECTED:
			if (recipientId.equals(bartsyId)) {
				// We are the recipient 
				((TextView) view.findViewById(R.id.view_order_state_description)).setText("You rejected this offer.");
				((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.darker_gray);				
				view.findViewById(R.id.view_order_notification_button).setVisibility(View.VISIBLE);
				view.findViewById(R.id.view_order_notification_button).setTag(this);
			} else {
				// We are the sender - 
				((TextView) view.findViewById(R.id.view_order_state_description)).setText("Your offer was rejected by its recipient");
				((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.darker_gray);
				view.findViewById(R.id.view_order_notification_button).setVisibility(View.VISIBLE);
				view.findViewById(R.id.view_order_notification_button).setTag(this);
			}
			break;
		case ORDER_STATUS_NEW:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText("Order placed. You'll be notified when accepted.");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.darker_gray);
			break;
		case ORDER_STATUS_REJECTED:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText("Order rejected. Please check with the venue. You werent' charged.");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.holo_red_dark);
			view.findViewById(R.id.view_order_notification_button).setVisibility(View.VISIBLE);
			view.findViewById(R.id.view_order_notification_button).setTag(this);
			break;
		case ORDER_STATUS_FAILED:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText("Order failed. Please check with the venue. You werent' charged.");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.holo_red_dark);
			view.findViewById(R.id.view_order_notification_button).setVisibility(View.VISIBLE);
			view.findViewById(R.id.view_order_notification_button).setTag(this);
			break;
		case ORDER_STATUS_IN_PROGRESS:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText("Order accepted. You'll be notified when it's ready.");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.holo_orange_dark);
			break;
		case ORDER_STATUS_READY:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText("Order ready. Please pick it up before it expires.");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.holo_green_dark);
			break;
		case ORDER_STATUS_INCOMPLETE:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText("Order not picked up. Check with the venue. You weren't charged.");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.holo_red_dark);
			view.findViewById(R.id.view_order_notification_button).setVisibility(View.VISIBLE);
			view.findViewById(R.id.view_order_notification_button).setTag(this);
			break;
		case ORDER_STATUS_CANCELLED:
			switch (last_status) { 
			case ORDER_STATUS_OFFERED:
				if (recipientId.equals(bartsyId)) {
					// We are the recipient 
					((TextView) view.findViewById(R.id.view_order_state_description)).setText("The offer timed out. Please accept your offered drinks within " + timeOut + " minutes.");
				} else {
					// We are the sender - 
					((TextView) view.findViewById(R.id.view_order_state_description)).setText("Your offer timed out as the recipient didn't accept or reject it in time. You weren't charged, so feel free to make another offer.");
				}
				break;
			default:
				((TextView) view.findViewById(R.id.view_order_state_description)).setText("Your order was taking too long and it timed out. You weren't charged.");
				break;
			}
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.holo_red_dark);
			view.findViewById(R.id.view_order_notification_button).setVisibility(View.VISIBLE);
			view.findViewById(R.id.view_order_notification_button).setTag(this);
			break;
		case ORDER_STATUS_COMPLETE:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText("Your order was picked up.");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.holo_green_dark);
			view.findViewById(R.id.view_order_notification_button).setVisibility(View.VISIBLE);
			view.findViewById(R.id.view_order_notification_button).setTag(this);
			break;
		case ORDER_STATUS_TIMEOUT:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText("Order  timed out due to connectivity issues. Please check with the venue immediately.");
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
	
	/*
	 * Update the view of the order only with the given tip, tax and total amounts 
	 */
	public void updateTipTaxTotalView(double tipAmount,double taxAmount,double totalAmount){
		if(view!=null){
			((TextView) view.findViewById(R.id.view_order_tip_amount)).setText(df.format(tipAmount));
			((TextView) view.findViewById(R.id.view_order_tax_amount)).setText(df.format(taxAmount));
			((TextView) view.findViewById(R.id.view_order_total_amount)).setText(df.format(totalAmount));
		}
	}

	/*
	 * Update profile information in orders view
	 */
	private void updateProfileView(HashMap<String, Bitmap> cache) {

		if (senderId != null && recipientId != null && senderId.equals(recipientId)) {
			view.findViewById(R.id.view_order_images).setVisibility(View.GONE);
			view.findViewById(R.id.view_order_sender_recipient_text).setVisibility(View.GONE);
			return;
		}
		
		// Show sender details
		if (orderSender != null) {

			// Display or download image if available
			if (orderSender.hasImage()) {
				((ImageView)view.findViewById(R.id.view_order_sender_image)).setImageBitmap(orderSender.getImage());
			} else if (orderSender.hasImagePath()) {
				WebServices.downloadImage(orderSender, ((ImageView)view.findViewById(R.id.view_order_sender_image)), cache);
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
		if (orderRecipient != null) {

			// Display or download image if available
			if (orderRecipient.hasImage()) {
				((ImageView)view.findViewById(R.id.view_order_recipient_image)).setImageBitmap(orderRecipient.getImage());
			} else if (orderRecipient.hasImagePath()) {
				WebServices.downloadImage(orderRecipient, ((ImageView)view.findViewById(R.id.view_order_recipient_image)), cache);
			} else {
				view.findViewById(R.id.view_recipient).setVisibility(View.GONE);
				view.findViewById(R.id.view_order_for).setVisibility(View.GONE);
			}

			// Show nick name if available
			if (orderRecipient.hasNickname())  {
				((TextView) view.findViewById(R.id.view_order_recipient)).setText(orderRecipient.getNickname());	
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
			if (orderRecipient != null) 
				recipient = "\n(for: " + orderRecipient.getNickname() + ")";
			else 
				recipient = "\n(for another user)";
		} else if (!senderId.equals(bartsyId)){
			if (orderSender != null)
				recipient = "\n(from: " + orderSender.getNickname() + ")";
			else 
				recipient = "\n(from another user)";
		}
		return recipient;
	}
	
	
	public View pastView(LayoutInflater inflater) {

		final View itemView = inflater.inflate(R.layout.order_past, null);
		
		// Extract time from UTC field
		String inputText = createdDate.replace("T", " ").replace("Z", ""); // example: 2013-06-27 10:20:15
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
		time = Utilities.getFriendlyDate(createdDate.replace("T", " ").replace("Z", ""), "yyyy-MM-dd HH:mm:ss");
		((TextView) itemView.findViewById(R.id.dateCreated)).setText(time);
		((TextView) itemView.findViewById(R.id.orderId)).setText(orderId);
		
		// Set status - either price if successful or the actual status otherwise
		String status = "";
		switch(last_status) {
		case Order.ORDER_STATUS_CANCELLED:
			status = "Cancelled";
			break;
		case Order.ORDER_STATUS_NEW:
		case Order.ORDER_STATUS_READY:
		case Order.ORDER_STATUS_IN_PROGRESS:
		case Order.ORDER_STATUS_COMPLETE:
			// Set total
		    DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(2);
			df.setMinimumFractionDigits(2);
			status = String.valueOf("$" + df.format(totalAmount));
			break;
		case Order.ORDER_STATUS_FAILED:
			status = "Failed";
			break;
		case Order.ORDER_STATUS_INCOMPLETE:
			status = "Incomplete";
			break;
		case Order.ORDER_STATUS_REJECTED:
			status = "Rejected";
			break;
		default:
			status = "<status " + last_status + ">";
			break;
		}
		((TextView) itemView.findViewById(R.id.order_past_status)).setText(String.valueOf(status));

		// Show sender/recipient string
		String us = bartsyId;
		String text = "";
		if (us.equals(senderId) && !us.equals(recipientId))
			text = "To: " + recipientNickname;
		else if (!us.equals(senderId) && us.equals(recipientId))
			text = "From: " + senderNickname;
		else
			itemView.findViewById(R.id.sender_recipient).setVisibility(View.GONE);
		((TextView) itemView.findViewById(R.id.sender_recipient)).setText(text);

		// Show items
		LinearLayout itemsLayout = (LinearLayout) itemView.findViewById(R.id.order_past_items);
		for (Item item : items) 
			itemsLayout.addView(item.orderView(inflater));

		return itemView;
	}
	

	/**
	 * TODO - Utilities
	 */
	
	public void addItem(Item item) {

		Log.v(TAG, "Adding item "  + item.getTitle() + " to order " + orderId );
		
		// Add item to the order
		items.add(item);
		
		// Update totals - notice that we compute the tip here using the default of the order dialog, that's where it will be changed if needed
		baseAmount	+= item.getOrderPrice();
		taxAmount	=  taxRate * baseAmount;
		tipAmount = Constants.defaultTip * baseAmount;
		totalAmount	=  tipAmount + taxAmount + baseAmount;
		
		// Add an options description if there isn't one
		if (!item.has(item.getOptionGroups()))
			item.setOptionsDescription("Ordered 'as-is.'");
	}
	
	Date adjustDate(String serverDate, String orderDate) {
		Date server = Utilities.getLocalDateFromGMTString(serverDate, "dd MMM yyyy HH:mm:ss 'GMT'");
		Date order = Utilities.getLocalDateFromGMTString(orderDate, "dd MMM yyyy HH:mm:ss 'GMT'");
		order.setTime(order.getTime() + (new Date().getTime() - server.getTime()));
		return order;
	}
	
	public String getRecipientName(ArrayList<UserProfile> people) {
 
		if (recipientNickname != null)
			return recipientNickname;
		
		if (orderRecipient != null)
			return orderRecipient.getName();
		
		if (recipientId == null)
			return "<unkown>";
					
		for (UserProfile p : people) {
			if (p.getBartsyId().equals(recipientId)) {
				// User found
				orderRecipient = p;
				return p.getName();
			}
		}
		return "<unkown>";
	}

	
	/**
	 * 
	 * TODO Getters and setters
	 * 
	 */

	public boolean has(String field) {
		return !(field == null || field.equals(""));
	}
	
	public boolean has(double field) {
		return field != 0;
	}

	public boolean has(boolean field) {
		return field;
	}
	
	public boolean has(Object field) {
		return field != null;
	}

}