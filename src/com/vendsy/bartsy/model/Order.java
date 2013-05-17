package com.vendsy.bartsy.model;

import java.text.DateFormat;
import java.util.Currency;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import com.vendsy.bartsy.R;

import android.content.Context;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

public class Order  {

	// Each order has an ID that is unique within a session number
	public String clientID; // this is the client-side managed ID used to identify an order when status is received
	public String serverID; // this is the server-side manager ID used to identify the order on the server and tablet 
			// and also to let the user know what order the shoudl be asking for when they get to the bartender
	
	// Title and description are arbitrary strings
	public String title, description;
	public String itemId;
	
	// The total price is in the local denomination and is the sum of price * quantity, fee and tax
	public float price, fee, tax;
	public int quantity = 1;
	public int image_resource;
	public float tipAmount;
	public double total;
	public String updatedDate;
	
	
	// Each order contains the sender and the recipient (another single in the bar or a friend to pick the order up)
	public Profile orderSender;
	public Profile orderReceiver;
	
	// The view displaying this order or null. The view is the display of the order in a list. 
	// The list could be either on the client or the server and it looks different in both cases
	// but the code manages the differences. 
	public View view = null;
	
	// Each order has exactly one associated profile. The order is invalid without one.
//	Profile profile;
	
	// Order states
	// (received) -> NEW -> (accepted) -> IN_PROGRESS -> (completed) -> READY   -> (picked_up) -> COMPLETE
	//                      (rejected) -> REJECTED       (failed)    -> FAILED     (forgotten) -> INCOMPLETE  
	
	
	public int status;	
    public static final int ORDER_STATUS_NEW			= 0;
    public static final int ORDER_STATUS_REJECTED    	= 1;
    public static final int ORDER_STATUS_IN_PROGRESS  	= 2;
    public static final int ORDER_STATUS_READY 			= 3;
	public static final int ORDER_STATUS_FAILED	 		= 4;
	public static final int ORDER_STATUS_COMPLETE	 	= 5;
	public static final int ORDER_STATUS_COUNT			= 6;
	public Date[] state_transitions = new Date[ORDER_STATUS_COUNT];
	
	
	/* 
	 * When an order is initialized the state transition times are undefined except for the 
	 * first state, which is when the order is received
	 */
	public void initialize (String clientOrderID, String serverOrderID, String title, String description, 
			String price, String image_resource, Profile order_sender) {
		this.clientID = clientOrderID;
		this.serverID = serverOrderID;
		this.title = title;
		this.description = description;
		this.price = Float.parseFloat(price);
		this.image_resource = R.drawable.drinks; // for now always use the same picture for drinks 
//		this.image_resource = Integer.parseInt(image_resource); 
		this.orderSender = order_sender;

		// Orders starts in the "NEW" status
		this.status = ORDER_STATUS_NEW;
		this.state_transitions[this.status] = new Date();
		
		calculateTotalPrice();
	}
	
	public Order() {
	}
	
	public JSONObject getPlaceOrderJSON(){
		final JSONObject orderData = new JSONObject();
		try {
			orderData.put("basePrice", String.valueOf(price));
			orderData.put("itemId", itemId);
			orderData.put("itemName", title);
			orderData.put("tipPercentage", String.valueOf(tipAmount));
			orderData.put("totalPrice", String.valueOf(total));
			orderData.put("orderStatus", ORDER_STATUS_NEW);
			orderData.put("description", description);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return orderData;
	}
	
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
	}
	
	public void calculateTotalPrice() {
		float actualPrice = (price * quantity);
		float subTotal =  actualPrice * ((tipAmount + 8) / 100);
		
		total = actualPrice + subTotal;

	}
	
	public void updateView () {
		
		if (view == null) return;

		((TextView) view.findViewById(R.id.view_order_title)).setText(this.title);
		((TextView) view.findViewById(R.id.view_order_description)).setText(this.description);
		((TextView) view.findViewById(R.id.view_order_time)).setText(DateFormat.getTimeInstance().format(this.state_transitions[ORDER_STATUS_NEW]));
		((TextView) view.findViewById(R.id.view_order_date)).setText(DateFormat.getDateInstance().format(this.state_transitions[ORDER_STATUS_NEW]));
		((TextView) view.findViewById(R.id.view_order_price)).setText("" + (int) this.price); // use int for now

		String positive="", negative="";
		switch (this.status) {
		case ORDER_STATUS_NEW:
			positive = "ACCEPT";
			negative = "REJECT";
			((TextView) view.findViewById(R.id.view_order_number)).setText("Waiting for bartender to accept (" + this.clientID + ")");
			((View) view.findViewById(R.id.view_order_header)).setBackgroundResource(R.drawable.rounded_corner_red);
			break;
		case ORDER_STATUS_IN_PROGRESS:
			positive = "COMPLETED";
			negative = "FAILED";
			((TextView) view.findViewById(R.id.view_order_number)).setText("Accepted with number " + this.serverID);
			((View) view.findViewById(R.id.view_order_header)).setBackgroundResource(R.drawable.rounded_corner_orange);
			break;
		case ORDER_STATUS_READY:
			positive = "PICKED UP";
			negative = "NO SHOW";
			((TextView) view.findViewById(R.id.view_order_number)).setText("Ready for pickup with number " + this.serverID);
			((View) view.findViewById(R.id.view_order_header)).setBackgroundResource(R.drawable.rounded_corner_green);
			break;
		}
	
		((Button) view.findViewById(R.id.view_order_button_positive)).setText(positive);
		((Button) view.findViewById(R.id.view_order_button_positive)).setTag(this);
		((Button) view.findViewById(R.id.view_order_button_negative)).setText(negative);
		((Button) view.findViewById(R.id.view_order_button_negative)).setTag(this);
		view.setTag(this);
		
	}
	
}
