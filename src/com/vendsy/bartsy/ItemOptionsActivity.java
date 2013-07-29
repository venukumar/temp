package com.vendsy.bartsy;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.vendsy.bartsy.dialog.DrinkDialogFragment;
import com.vendsy.bartsy.model.Item;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * @author Peter Kellis
 *
 */

public class ItemOptionsActivity extends SherlockActivity {
	
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
		mItem = mApp.selectedMenuItem;
		
		if (mItem == null) {
			Log.e(TAG, "Trying to order null item");
			finish();
			return;
		}
		
		setContentView(mItem.inflateOrder(getLayoutInflater()));
	}
	
	

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

}
