package com.vendsy.bartsy.model;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

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
//import com.jwetherell.quick_response_code.data.Contents;
//import com.jwetherell.quick_response_code.qrcode.QRCodeEncoder;
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
	public float baseAmount	= 0;
	public float feeAmount	= 0;
	public float taxAmount	= 0;
	public float tipAmount	= 0;
	public float totalAmount= 0;
	public float taxRate	= 0;

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
	 * 
	 * TODO - CONSTRUCTORS
	 * 
	 * When an order is initialized the state transition times are undefined except for the first state, 
	 * which is when the order is received
	 */
	
	
	public Order(float taxRate) {
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);
		this.taxRate = taxRate;
	}

	public Order (UserProfile order_sender, UserProfile order_receiver, float taxRate, float tipAmount, Item item) {
		
		this.items.add(item);
		
		this.baseAmount = item.getPrice();
		this.tipAmount = tipAmount;
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

	
	/** 
	 * Constructor for new empty order destined for a given recipient
	 * @param profile
	 */
	public Order(UserProfile sender, UserProfile recipient, float taxRate) {
		
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

	/**
	 * It will returns JSON format to place order
	 */
	public JSONObject getPlaceOrderJSON() {
		final JSONObject orderData = new JSONObject();
		try {

			if (items.size() > 1) {
				JSONArray jsonItems = new JSONArray();
				for (Item item : items) {
					JSONObject jsonItem = new JSONObject();
					jsonItem.put("itemId", item.getItemId());
					jsonItem.put("itemName", item.getTitle());
					jsonItem.put("description", item.getDescription());
					jsonItem.put("basePrice", Float.toString(item.getPrice()));
					jsonItems.put(jsonItem);
				}
				orderData.put("itemsList", jsonItems);
			} else if (items.size() == 1) {
				orderData.put("itemId", items.get(0).getItemId());
				orderData.put("itemName", items.get(0).getTitle());
				orderData.put("description", items.get(0).getDescription());
			}
		
			orderData.put("basePrice", String.valueOf(baseAmount));
			orderData.put("tipPercentage", String.valueOf(tipAmount));
			orderData.put("totalPrice", String.valueOf(totalAmount));
			orderData.put("orderStatus", Integer.toString(status));
			
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
	public JSONObject getStatusChangedJSON(){
		final JSONObject orderData = new JSONObject();
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
		return getPlaceOrderJSON() + orderData.toString();
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
	 * Constructor to parse all the information from the JSON
	 * 
	 * @param json
	 */
	public Order(JSONObject json) {

		try {

			// Parse old format item
			if (json.has("title")  || json.has("description") || json.has("itemId")) {
				Item item = new Item();
				if (json.has("itemName"))
					item.setTitle(json.getString("itemName"));
				if (json.has("description"))
					item.setDescription(json.getString("description"));
				if (json.has("basePrice"))
					item.setPrice(Float.valueOf(json.getString("basePrice")));
				if (json.has("itemId"))
					item.setItemId(json.getString("itemId"));
				items.add(item);
			} 
	
			// Parse new format item list
			if (json.has("itemsList")) {
				JSONArray itemsJSON = json.getJSONArray("itemsList");
				
				for (int i=0 ; i < itemsJSON.length() ; i++) {
					items.add(new Item(itemsJSON.getJSONObject(i)));
				}
			}
			
			if (json.has("basePrice"))
				baseAmount = Float.valueOf(json.getString("basePrice"));

			if (json.has("tipPercentage"))
				tipAmount = Float.valueOf(json.getString("tipPercentage"));
			
			orderId = json.getString("orderId");
			
			totalAmount = Float.valueOf(json.getString("totalPrice"));
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

	
	Date parseWeirdDate(String date) {
		Date d = Utilities.getLocalDateFromGMTString(createdDate, "dd MMM yyyy HH:mm:ss 'GMT'");
		if (d == null)
			d = Utilities.getLocalDateFromGMTString(createdDate, "d MMM yyyy HH:mm:ss 'GMT'");
		return d;
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


	public View updateView(LayoutInflater inflater, ViewGroup container) {

		Log.v(TAG, "updateView()");
		Log.v(TAG, "Order sender   :" + orderSender);
		Log.v(TAG, "Order receiver :" + orderRecipient);
		
		view = (View) inflater.inflate(R.layout.open_orders_item, container, false);

		// Update header
		((TextView) view.findViewById(R.id.view_order_item_number)).setText(orderId);
		((TextView) view.findViewById(R.id.view_order_pickup_code)).setText(userSessionCode);
		

		// Add the item list
		addItemsView((LinearLayout) view.findViewById(R.id.view_order_mini), inflater, container);
		
		// Update the order's tip, tax and total
		updateTipTaxTotalView(tipAmount, taxAmount, totalAmount);
		
		// To display order receiver profile information in orders view
		updateProfileView();

		// If the order has been removed already (and we're showing for UI reasons), use the last status for showing the state
		int orderStatus = status;
		if (orderStatus == ORDER_STATUS_REMOVED)
			orderStatus = last_status;

		
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
			((TextView) view.findViewById(R.id.view_order_state_description)).setText("Your order was placed. You'll be notified when accepted.");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.darker_gray);
			break;
		case ORDER_STATUS_REJECTED:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText("Your order was rejected. Please check with the venue. You werent' charged.");
			((View) view.findViewById(R.id.view_order_background)).setBackgroundResource(android.R.color.holo_red_dark);
			view.findViewById(R.id.view_order_notification_button).setVisibility(View.VISIBLE);
			view.findViewById(R.id.view_order_notification_button).setTag(this);
			break;
		case ORDER_STATUS_FAILED:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText("Your order failed. Please check with the venue. You werent' charged.");
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
			
/*			//Encode with a QR Code image
			QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(userSessionCode, null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), 250);
			try {
				Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
				
				((ImageView) view.findViewById(R.id.view_order_recipient_image)).setImageBitmap(bitmap);
				
			} catch (WriterException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
*/			
			break;
		case ORDER_STATUS_INCOMPLETE:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText("Your order was not picked up. Check with the venue. You werent' charged.");
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
		if (orderRecipient != null) {

			// Display or download image if available
			if (orderRecipient.hasImage()) {
				((ImageView)view.findViewById(R.id.view_order_recipient_image)).setImageBitmap(orderRecipient.getImage());
			} else if (orderRecipient.hasImagePath()) {
				WebServices.downloadImage(orderRecipient, ((ImageView)view.findViewById(R.id.view_order_recipient_image)));
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
	
	public void addItemsView(LinearLayout itemsView, LayoutInflater inflater, ViewGroup container ) {
		
		for (Item item : items) {
			LinearLayout view = (LinearLayout) inflater.inflate(R.layout.open_orders_menu_item, container, false);
			((TextView) view.findViewById(R.id.view_order_mini_price)).setText(df.format(item.getPrice()));
			((TextView) view.findViewById(R.id.view_order_title)).setText(item.getTitle());
			if (item.getDescription() == null || item.getDescription().equalsIgnoreCase(""))
				((TextView) view.findViewById(R.id.view_order_description)).setVisibility(View.GONE);
			else
				((TextView) view.findViewById(R.id.view_order_description)).setText(item.getDescription());
			
			itemsView.addView(view);
		}
	}

	public void addItem(Item item) {

		Log.v(TAG, "Adding item "  + item.getTitle() + " to order " + orderId );
		
		// Add item to the order
		items.add(item);
		
		// Update totals - notice that we compute the tip here using the default of the order dialog, that's where it will be changed if needed
		baseAmount	+= item.getPrice();
		taxAmount	=  taxRate * baseAmount;
		tipAmount = Constants.defaultTip * baseAmount;
		totalAmount	=  tipAmount + taxAmount + baseAmount;
	}

}