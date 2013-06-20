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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vendsy.bartsy.R;
import com.vendsy.bartsy.utils.Constants;

public class Order {

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
	public String updatedDate;
	public String orderDate;

	
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
	// (received) -> NEW -> (accepted) -> IN_PROGRESS -> (completed) -> READY ->
	// (picked_up) -> COMPLETE -> (timed out, error, etc) -> CANCELLED
	// (rejected) -> REJECTED (failed) -> FAILED (forgotten) -> INCOMPLETE

	public static final int ORDER_STATUS_NEW = 0;
	public static final int ORDER_STATUS_REJECTED = 1;
	public static final int ORDER_STATUS_IN_PROGRESS = 2;
	public static final int ORDER_STATUS_READY = 3;
	public static final int ORDER_STATUS_FAILED = 4;
	public static final int ORDER_STATUS_COMPLETE = 5;
	public static final int ORDER_STATUS_INCOMPLETE = 6;
	public static final int ORDER_STATUS_CANCELLED = 7;
	public static final int ORDER_STATUS_COUNT = 8;
	
	public String type = "Custom";

	// The states are implemented in a status variable and each state transition
	// has an associated time
	public int status;
	public Date[] state_transitions = new Date[ORDER_STATUS_COUNT];

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

		
		this.baseAmount = Float.valueOf(baseAmount);
		this.tipAmount = Float.valueOf(tipAmount);
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

		try {
			status = Integer.valueOf(json.getString("orderStatus"));
			state_transitions[status] = new Date(); // For now just use current date. the date should come in the syscall 
			title = json.getString("itemName");
			orderDate = json.getString("orderTime");
			baseAmount = Float.valueOf(json.getString("basePrice"));
			serverID = json.getString("orderId");
			tipAmount = Float.valueOf(json.getString("tipPercentage"));
			totalAmount = Float.valueOf(json.getString("totalPrice"));
			taxAmount = baseAmount * Constants.taxRate;
			description = json.getString("description");
			itemId = json.getString("itemId");
			updatedDate = json.getString("updateTime");
			
			
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
			this.status = ORDER_STATUS_IN_PROGRESS;
			break;
		case ORDER_STATUS_IN_PROGRESS:
			this.status = ORDER_STATUS_READY;
			break;
		case ORDER_STATUS_READY:
			this.status = ORDER_STATUS_COMPLETE;
			break;
		}

		// Mark the time of the state transition in the timetable
		state_transitions[status] = new Date();
	}



	public View updateView(LayoutInflater inflater, ViewGroup container) {

		view = (View) inflater.inflate(R.layout.order_item, container, false);
		
		((TextView) view.findViewById(R.id.view_order_title)).setText(this.title);
		((TextView) view.findViewById(R.id.view_order_description)).setText(this.description);
		((TextView) view.findViewById(R.id.view_order_time)).setText(DateFormat.getTimeInstance().format(this.state_transitions[status]));
		((TextView) view.findViewById(R.id.view_order_date)).setText(DateFormat.getDateInstance().format(this.state_transitions[status]));

		((TextView) view.findViewById(R.id.view_order_price)).setText(df.format(baseAmount));

		((TextView) view.findViewById(R.id.view_order_tip_amount)).setText(df.format(tipAmount));
		((TextView) view.findViewById(R.id.view_order_tax_amount)).setText(df.format(taxAmount));
		((TextView) view.findViewById(R.id.view_order_total_amount)).setText(df.format(totalAmount));

		((TextView) view.findViewById(R.id.view_order_item_number)).setText("" + serverID);

		if (orderReceiver != null) {
			((TextView) view.findViewById(R.id.view_order_state_description)).setText(orderReceiver.getBartsyId());
			if (orderReceiver.hasImage())
				((ImageView) view.findViewById(R.id.view_order_profile_picture)).setImageBitmap(orderReceiver.getImage());
		} else if (orderSender != null){
			((TextView) view.findViewById(R.id.view_order_state_description)).setText(orderSender.getBartsyId());
			if (orderSender.hasImage())
				((ImageView) view.findViewById(R.id.view_order_profile_picture)).setImageBitmap(orderSender.getImage());
			
		}
		
		
		switch (this.status) {
		case ORDER_STATUS_NEW:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText("Waiting for bartender to accept");
			((View) view.findViewById(R.id.view_order_header)).setBackgroundResource(R.drawable.rounded_corner_red);
			break;
		case ORDER_STATUS_IN_PROGRESS:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText("Bartender accepted the order");
			((View) view.findViewById(R.id.view_order_header)).setBackgroundResource(R.drawable.rounded_corner_orange);
			break;
		case ORDER_STATUS_READY:
			((TextView) view.findViewById(R.id.view_order_state_description)).setText("Your order is ready for pickup!");
			((View) view.findViewById(R.id.view_order_header)).setBackgroundResource(R.drawable.rounded_corner_green);
			break;
		}
		
		return view;

	}
	
	public View getMiniView(LayoutInflater inflater, ViewGroup container ) {
		
		LinearLayout view = (LinearLayout) inflater.inflate(R.layout.order_item_mini, container, false);
		
		((TextView) view.findViewById(R.id.view_order_title)).setText(this.title);
		((TextView) view.findViewById(R.id.view_order_description)).setText(this.description);
		((TextView) view.findViewById(R.id.view_order_mini_price)).setText(df.format(baseAmount)); // use int for now
		
		return view;
	}

}
