package com.vendsy.bartsy;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.vendsy.bartsy.dialog.DrinkDialogFragment;
import com.vendsy.bartsy.model.Item;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.utils.Utilities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

/**
 * 
 * @author Peter Kellis
 *
 */

public class CustomizeActivity extends SherlockActivity implements OnClickListener {
	
	private final static String TAG = "ItemOptionsActivity";
	
	private BartsyApplication mApp;
	private Item mItem;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		// Set up the action bar custom view
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		
		mApp = (BartsyApplication) getApplication();
		
		try {
			mItem = loadInput(mApp);
		} catch (Exception e) {
			// Invalid input
			e.printStackTrace();
			Log.e(TAG, "Invalid input");
			finish();
			return;
		}
		
		// Set the main view
		setContentView(mItem.inflateOrder(getLayoutInflater()));
		
		// Set up listeners
		findViewById(R.id.view_order_item_submit).setOnClickListener(this);
	}
	
	/** 
	 * TODO - Activity input/output 
	 */
	
	// Used to check the validity of this activity's input
	private static final int ACTIVITY_INPUT_VALID	= 0;
	private static final int ACTIVITY_INPUT_INVALID = 1;
	
	@Override
	public void onPause() {
		super.onPause();
		
		Log.v(TAG, "onPause()");
		
		// Invalidate the input of this activity when exiting to avoid reentering it with invalid data
		Utilities.savePref(this, R.string.ItemOptionsActivity_input_status, ACTIVITY_INPUT_INVALID);
	}

	/*
	 * Sets the input of this activity and makes it valid
	 */
	public static final void setInput(BartsyApplication context, Item item) {
		Utilities.savePref(context, R.string.ItemOptionsActivity_input_status, ACTIVITY_INPUT_VALID);
		context.selectedMenuItem = item;
	}
	
	private void finishWithResult(BartsyApplication context, Item item) {
		context.selectedMenuItem = item;
		setResult(UserProfileActivity.RESULT_OK);
		finish();
	}
	
	public static final Item getOutput(BartsyApplication context) {
		return context.selectedMenuItem;
	}
	
	private Item loadInput(BartsyApplication context) throws Exception {

		// Make sure the input is valid
		if (Utilities.loadPref(this, R.string.ItemOptionsActivity_input_status, ACTIVITY_INPUT_VALID) != ACTIVITY_INPUT_VALID) {		
			Log.e(TAG, "Invalid activity input - exiting...");
			Utilities.removePref(this, R.string.ItemOptionsActivity_input_status);
			throw new Exception();
		}
		
		return context.selectedMenuItem;
	}
	
	/**
	 * TODO - Click listeners
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View arg0) {

		switch (arg0.getId()) {
		
		case R.id.view_order_item_submit:
			
			mItem.updateOptions();
			finishWithResult(mApp, mItem);
			break;
		}
	}

}
