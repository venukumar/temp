/**
 * 
 */
package com.vendsy.bartsy.view;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.CustomDrinksActivity;
import com.vendsy.bartsy.CustomizeActivity;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.VenueActivity;
import com.vendsy.bartsy.adapter.ExpandableListAdapter;
import com.vendsy.bartsy.dialog.DrinkDialogFragment;
import com.vendsy.bartsy.model.Item;
import com.vendsy.bartsy.model.Menu;
import com.vendsy.bartsy.model.OptionGroup;
import com.vendsy.bartsy.model.Order;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;

/**
 * @author peterkellis
 * 
 */
public class MenuSectionFragment extends SherlockFragment {
	
	String TAG = "DrinksSectionFragment";

	public BartsyApplication mApp = null;
	public VenueActivity mActivity = null;
	private Handler handler = new Handler();

	private View mRootView = null;
	private ExpandableListView mDrinksListView = null;
	ExpandableListAdapter mAdapter = null;
	
	private MenuList mMenu = null;
	private String mVenueId = null;
	
	private class MenuList {

		Menu recent = null;
		Menu favorites = null;
		Menu mixedDrinks = null;
		Menu cocktails = null;
		Menu locuMenu = null;
		
		synchronized ArrayList<String> getHeadings() {
			ArrayList<String> headings = new ArrayList<String>() ;
			if (recent != null)
				headings.addAll(recent.headings);
			if (favorites != null)
				headings.addAll(favorites.headings);
			if (mixedDrinks != null)
				headings.addAll(mixedDrinks.headings);
			if (cocktails != null)
				headings.addAll(cocktails.headings);
			if (locuMenu != null)
				headings.addAll(locuMenu.headings);
			return headings;
		}
		
		synchronized ArrayList<ArrayList<Item>> getItems () {
			ArrayList<ArrayList<Item>> items = new ArrayList<ArrayList<Item>>();
			
			if (recent != null)
				items.addAll(recent.items);
			if (favorites != null)
				items.addAll(favorites.items);
			if (mixedDrinks != null)
				items.addAll(mixedDrinks.items);
			if (cocktails != null)
				items.addAll(cocktails.items);
			if (locuMenu != null)
				items.addAll(locuMenu.items);
			
			return items;
		}
	}
	
	private static final int REQUEST_CODE_CUSTOM_DRINK = 9301;

	// We use this to store the json of a "compressed" option and replace compressed options on the fly
	private HashMap<String, JSONObject> savedSelections = new HashMap<String, JSONObject>();

	@Override
	public void onAttach(Activity activity) {
		
		super.onAttach(activity);
		
		Log.v(TAG, "DrinksSectionFragment()");

		// Make sure the fragment pointed to by the activity is accurate
		mActivity = (VenueActivity) activity;
		mApp = (BartsyApplication) mActivity.getApplication();
		mActivity.mDrinksFragment = this;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		Log.v(TAG, "onCreateView()");
		
		mRootView = inflater.inflate(R.layout.menu_tab, container, false);
		mDrinksListView = (ExpandableListView) mRootView.findViewById(R.id.view_drinks_for_me_list);
		
		// Update the view
		updateView();
		
		return mRootView;
	}

	/**
	 * Menu loader
	 */
	synchronized public void loadMenus(VenueActivity activity) {
		
		Log.v(TAG, "loadMenus()");
		
		// Make sure the fragment pointed to by the activity is accurate
		mActivity = (VenueActivity) activity;
		mApp = (BartsyApplication) mActivity.getApplication();
		mActivity.mDrinksFragment = this;
		
		// Check if menus were already cached for this venue
		
		if (mMenu == null || !mVenueId.equals(mApp.mActiveVenue.getId())) {
			
			if (mMenu == null)
				Log.v(TAG, "Menu not available in memory - downloading it from the server");
			else
				Log.v(TAG, "Wrong menu - downloading this venue's menu from the server");
			mMenu = new MenuList();

			// Set the venue we're checked in
			mVenueId = mApp.mActiveVenue.getId();
			if(mVenueId == null){
				Log.e(TAG, "Trying to load menu for a null venue");
				return ;
			}
			
			// First add a test menu to test options
/*			try {
				mMenu = createTestMenu();
			} catch (JSONException e) {
				e.printStackTrace();
				mMenu = new Menu();
			}
*/			
			// Get the menus for that venue
			new Thread() {
				@Override
				public void run() {
					mMenu.recent = getMenu(WebServices.URL_GET_RECENT_ORDERS_MENU, false, true);
					showMenus();
					mMenu.favorites = getMenu(WebServices.URL_GET_FAVORITES_MENU, false, true);
					showMenus();
					mMenu.mixedDrinks = getMenu(WebServices.URL_GET_MIXED_DRINKS_MENU, true, true);
					showMenus();
					mMenu.cocktails = getMenu(WebServices.URL_GET_COCKTAILS_MENU, true, true);
					showMenus();
					mMenu.locuMenu = getMenu(WebServices.URL_GET_BAR_LIST, true, false); // Hide the menu name itself for Locu menus
					showMenus();
				}
			}.start();
						
		} else {
			// Menu already in memory. Nothing to load so just display it.
			Log.v(TAG, "Menus available in memory - displaying them...");
			updateView();
		}
	}
	
	private Menu createTestMenu() throws JSONException {
		
		String testMenu = mActivity.getResources().getString(R.string.test_menu);
		
		Log.i(TAG, "Test Menu created:\n" + testMenu);
		
		JSONObject json = new JSONObject(testMenu);
		return new Menu(json.getJSONArray("menus"), savedSelections, true);
	}
	
	private void showMenus() {
		// Show the newly downloaded menu. Use a handler because Android doesn't allow manipulating views from separate threads
		handler.post(new Runnable() {
			@Override 
			public void run() {
				updateView();
			}
		});	
	}
	
	/*
	 * Web service loader. Downloads the menu from the server using a web service call. 
	 * This is called from a background thread based on the position url array.
	 * @param url
	 * @param showErrors	- show errors if menu unavailable?
	 * @param showMenuName	- display the menu name?
	 */
	private Menu getMenu(String url, boolean showErrors, boolean showMenuName) {

		Log.v(TAG, "addMenu(" + url + ")");
		
		Menu menu = null;
		
		// Get the data from the server
		String response = WebServices.getMenuList(mApp,url, mVenueId);
		if (response == null) {
			Log.d(TAG, "Webservice failed: " + url);
			mApp.makeText("Error downloading menu", Toast.LENGTH_SHORT);
			return null;
		} 
		
		// Extract the menu from the web services response and add the headings and the items to the view's list
		String error = null;
		try {
			JSONObject json = new JSONObject(response);
			
			if (json.has("errorCode") && json.getString("errorCode").equals("0") &&
					json.has("menus")) {
				
				// Success condition
				
				JSONArray menusArryObj = json.getJSONArray("menus");
				menu = new Menu(menusArryObj, savedSelections, showMenuName);
				
				return menu;
				
			} 
			
			if (json.has("errorMessage")) {
				error = json.getString("errorMessage");
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		if (showErrors) {
			// Error condition
			mApp.makeText("Error downloading menu", Toast.LENGTH_SHORT);
			Log.e(TAG, "Error downloading menu");
			
			if (error != null) {
				mApp.makeText(error, Toast.LENGTH_SHORT);
				Log.e(TAG, error);
			}
		}
		return null;
	}

	public void deleteMenu() {
		this.mMenu = null;
	}


	/*****
	 * 
	 * TODO - Views
	 * 
	 */
	

	/*
	 * Displays a menu in the view's expandable list adapter. If the menu cache is not loaded yet,
	 * it calls the appropriate loader that will in turn call this function again once they have 
	 * loaded the menu.
	 * 
	 * Expects mActivity to be pointing to the right context
	 */
	
	void updateView(){
		
		Log.v(TAG, "updateView()");
		
		if (mMenu == null ) {
			Log.d(TAG, "Menu not available for display");
			loadMenus(mActivity);
			return;
		}

		// If the view is not yet initialized, don't display the menu. This also 
		if (mDrinksListView == null) {
			Log.d(TAG, "View is not available");
			return;
		}

		
		// Display menu from memory into the view
		
		ArrayList<String> headings = mMenu.getHeadings();
		final ArrayList<ArrayList<Item>> items = mMenu.getItems();

		Log.v(TAG, "Menu is in cache. Displaying " + headings.size() + " headings");

		try {

			mAdapter = new ExpandableListAdapter(mActivity,headings, items);
			mDrinksListView.setAdapter(mAdapter);

			// Setup the dialog to be displayed when clicking on an item in the menu
			mDrinksListView.setOnChildClickListener(new OnChildClickListener() {
				@Override
				public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
					
					Item item = items.get(groupPosition).get(childPosition);
					
					if (item.has(item.getOptionGroups())) {

						// We have options - show customization activity
						CustomizeActivity.setInput(mApp, item);
						Intent intent = new Intent(mActivity, CustomizeActivity.class);
						startActivityForResult(intent, REQUEST_CODE_CUSTOM_DRINK);
					} else {

						// No options - simply add item to the order
						order(item);
					}
					return false;
				}
			});
			
			mDrinksListView.postInvalidate();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			return;
		}

	}

	private void order(Item item) {
		
		Order order;
		
		if (mApp.hasActiveOrder()) {
			order = mApp.getActiveOrder();
			order.addItem(item);
		} else {
			order = new Order(mApp.loadBartsyId(), mApp.mProfile, mApp.mProfile, mApp.mActiveVenue.getTaxRate(), Constants.defaultTip, item);
		}
		
		// Create an instance of the dialog fragment and show it
		DrinkDialogFragment dialog = new DrinkDialogFragment(order);				
		dialog.show(getActivity().getSupportFragmentManager(),"Order drink");

	}

	@Override
	public void onActivityResult(int requestCode, int responseCode, Intent data) {
		
		super.onActivityResult(requestCode, responseCode, data);


		Log.v(TAG, "Activity result for request: " + requestCode + " with response: " + responseCode);

		switch (requestCode) {
		case REQUEST_CODE_CUSTOM_DRINK:
			
			switch (responseCode) {
			case SherlockActivity.RESULT_OK:

				// Figure out if we are adding the item to the active order or creating a new order
				Item item;
				item = CustomizeActivity.getOutput(mApp);
				order(item);
				break;
			}
			break;
		}
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.v(TAG, "onDestroy()");
		
		// Because the fragment may be destroyed while the activity persists, remove pointer from activity
		((VenueActivity) getActivity()).mDrinksFragment = null;
	}

}