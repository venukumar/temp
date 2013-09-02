/**
 * 
 */
package com.vendsy.bartsy;

import java.text.DecimalFormat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.adapter.ItemAdapter;
import com.vendsy.bartsy.dialog.PeopleSectionFragmentDialog;
import com.vendsy.bartsy.model.Item;
import com.vendsy.bartsy.model.Order;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.utils.WebServices;

/**
 * @author peterkellis
 * 
 */
public class OrderListActivity extends SherlockFragmentActivity implements OnClickListener, OnTouchListener {

	private static final int REQUEST_CODE_CUSTOM_DRINK = 2340;

	private static final String TAG = "DrinkDialogFragment";

	BartsyApplication mApp;
	ItemAdapter mItemAdapter;
	
	// Inputs/outputs
	public Order mOrder;
	
	private DecimalFormat df = new DecimalFormat();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		setContentView(R.layout.order_dialog);
		

		// Set up the action bar custom view
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		setTitle("Review your order");
		
		mApp = (BartsyApplication) getApplication();
		
		// Set active order as an input
		mOrder = mApp.getActiveOrder();

		// Show the total, tax and tip amounts
		((EditText) findViewById(R.id.view_dialog_drink_tip_amount)).setText(df.format(mOrder.tipAmount));
		((TextView) findViewById(R.id.view_dialog_drink_tax_amount)).setText(df.format(mOrder.taxAmount));
		((TextView) findViewById(R.id.view_dialog_drink_total_amount)).setText(df.format(mOrder.totalAmount));
		
		// If we already have an open order add its items to the layout
		ListView itemList = (ListView) findViewById(R.id.item_list);
		mItemAdapter = new ItemAdapter(this, R.layout.item_order, mOrder.items);
		itemList.setAdapter(mItemAdapter);
		itemList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Item item = mOrder.items.get(arg2);
				item.setOrder(mOrder);	// This item is part of an order. this will tell customize activity to show different text for the buttons
				CustomizeActivity.setInput(mApp, item);
				Intent intent = new Intent(OrderListActivity.this, CustomizeActivity.class);
				startActivityForResult(intent, REQUEST_CODE_CUSTOM_DRINK);
			}
		});
		
		// Show profile information by default
		if (mOrder.orderRecipient != null) updateProfileView(mOrder.orderRecipient);
		
		if (mOrder.items.isEmpty()) {
			// No items in this open order. Don't allow to place the order
		} else {
			Button placeOrderButton = (Button)findViewById(R.id.placeOrderButton);
			placeOrderButton.setOnClickListener(this);
		}
		Button addMoreButton = (Button)findViewById(R.id.addMoreButton);
		addMoreButton.setOnClickListener(this);
		
		// Set radio button listeners
		findViewById(R.id.view_dialog_order_tip_10).setOnClickListener(this);
		findViewById(R.id.view_dialog_order_tip_15).setOnClickListener(this);
		findViewById(R.id.view_dialog_order_tip_20).setOnClickListener(this);

		// Set the  edit text listener which unselects radio buttons when the tip is entered manually
		((EditText) findViewById(R.id.view_dialog_drink_tip_amount)).setOnTouchListener(this);
		
		// Set listeners 
		((Button)findViewById(R.id.placeOrderButton)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finishWithResult(SherlockActivity.RESULT_OK);
			}
		});
		
		((Button)findViewById(R.id.addMoreButton)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finishWithResult(SherlockActivity.RESULT_FIRST_USER);
			}
		});
		
	}

	@Override
	public void onActivityResult(int requestCode, int responseCode, Intent data) {
		
		super.onActivityResult(requestCode, responseCode, data);


		Log.v(TAG, "Activity result for request: " + requestCode + " with response: " + responseCode);

		switch (requestCode) {
		case REQUEST_CODE_CUSTOM_DRINK:
			
			Item item = CustomizeActivity.getOutput(mApp);
			Order order = item.getOrder();
			item.setOrder(null);


			switch (responseCode) {
			case CustomizeActivity.RESULT_OK:

				// Update the item list
				updateTotals();
				mItemAdapter.notifyDataSetChanged();
				Toast.makeText(mApp, "Item updated", Toast.LENGTH_SHORT).show();
				
				break;
				
			case CustomizeActivity.RESULT_FIRST_USER:

				// Figure out if we are adding the item to the active order or creating a new order
				order.items.remove(item);
				
				// Close activity if there are no more items
				if (order.items.size() == 0) {
					
					Toast.makeText(mApp, "Order cancelled", Toast.LENGTH_SHORT).show();
					mApp.setActiveOrder(null);
					finishWithResult(SherlockActivity.RESULT_CANCELED);
				} else {
					mItemAdapter.notifyDataSetChanged();
					updateTotals();
					Toast.makeText(mApp, "Item removed", Toast.LENGTH_SHORT).show();
				}
				break;
			}
			break;
		}
		
	}
	
	private void updateProfileView(UserProfile profile) {
		ImageView profileImageView = ((ImageView)findViewById(R.id.view_user_dialog_image_resource));
		
		if (!profile.hasImage()) {
			WebServices.downloadImage(profile, profileImageView);
		} else {
			profileImageView.setImageBitmap(profile.getImage());
		}
	
		// Show the username of the recipient
		((TextView) findViewById(R.id.view_user_dialog_info)).setText(profile.getNickname());	
		
		// To pick more user profiles when user pressed on the image view
		profileImageView.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.view_user_dialog_image_resource:
			PeopleSectionFragmentDialog dialog = new PeopleSectionFragmentDialog(){
				@Override
				protected void selectedProfile(UserProfile userProfile) {
					// Update profile with new selected profile
					mOrder.orderRecipient = userProfile;
					mOrder.recipientId = userProfile.getBartsyId();
					updateProfileView(userProfile);
					super.selectedProfile(userProfile);
				}
			};
			dialog.show(getSupportFragmentManager(),"PeopleSectionDialog");
			break;
			
		case R.id.view_dialog_order_tip_10:
		case R.id.view_dialog_order_tip_15:
		case R.id.view_dialog_order_tip_20:
			
			double percent = 0;
			
			if (v.getId() == R.id.view_dialog_order_tip_10)
				percent = (double) 0.10;
			else if (v.getId() == R.id.view_dialog_order_tip_15)
				percent = (double) 0.15;
			else if (v.getId() == R.id.view_dialog_order_tip_20)
				percent = (double) 0.20;
			
			// Set the tip and total amount based on the radio button selected
			mOrder.tipAmount = mOrder.baseAmount * percent;
			mOrder.totalAmount = mOrder.tipAmount + mOrder.taxAmount + mOrder.baseAmount;
			((EditText) findViewById(R.id.view_dialog_drink_tip_amount)).setText(df.format(mOrder.tipAmount));
			((TextView) findViewById(R.id.view_dialog_drink_total_amount)).setText(df.format(mOrder.totalAmount));

			break;
		}
	}

	private void finishWithResult(int result) {
		updateTotals();
		setResult(result);
		finish();
	}
	
	/* 
	 * Update and show price fields
	 */
	void updateTotals() {

		// Update order base amount
		mOrder.baseAmount = 0;
		for (Item item : mOrder.items)
			mOrder.baseAmount += item.getOrderPrice();
		
		// Update tip
		RadioGroup tipPercentage = (RadioGroup) findViewById(R.id.view_dialog_drink_tip);
		EditText percentage = (EditText) findViewById(R.id.view_dialog_drink_tip_amount);
		int selected = tipPercentage.getCheckedRadioButtonId();
		RadioButton b = (RadioButton) tipPercentage.findViewById(selected);
		if (b == null || b.getText().toString().trim().length() == 0) {
			String tip="0";
			if (percentage.getText()!= null)
				tip = percentage.getText().toString();
			mOrder.tipAmount = Double.parseDouble(tip) ;
		} else {
			mOrder.tipAmount = Double.parseDouble(b.getText().toString().replace("%", "")) / (double) 100 * mOrder.baseAmount;
		}
		
		// Update total
		mOrder.totalAmount = mOrder.tipAmount + mOrder.taxAmount + mOrder.baseAmount;
		
		// Show the total, tax and tip amounts
		((EditText) findViewById(R.id.view_dialog_drink_tip_amount)).setText(df.format(mOrder.tipAmount));
		((TextView) findViewById(R.id.view_dialog_drink_tax_amount)).setText(df.format(mOrder.taxAmount));
		((TextView) findViewById(R.id.view_dialog_drink_total_amount)).setText(df.format(mOrder.totalAmount));
	}
	
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		switch (arg0.getId()) {
		case R.id.view_dialog_drink_tip_amount:
			((RadioGroup) findViewById(R.id.view_dialog_drink_tip)).clearCheck();
			break;
		}
		return false;
	}

}
