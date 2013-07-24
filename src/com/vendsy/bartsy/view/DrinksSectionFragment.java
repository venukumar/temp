/**
 * 
 */
package com.vendsy.bartsy.view;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragment;
import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.CustomDrinksActivity;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.VenueActivity;
import com.vendsy.bartsy.adapter.ExpandableListAdapter;
import com.vendsy.bartsy.dialog.DrinkDialogFragment;
import com.vendsy.bartsy.model.Item;
import com.vendsy.bartsy.model.Order;
import com.vendsy.bartsy.model.Venue;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;

/**
 * @author peterkellis
 * 
 */
public class DrinksSectionFragment extends SherlockFragment {
	private View mRootView = null;
	private ExpandableListView mDrinksListView = null;
	public BartsyApplication mApp = null;
	public VenueActivity mActivity = null;
	private Handler handler = new Handler();
	private Menu mMenu = null;
	String TAG = "DrinksSectionFragment";

	/*
	 * Menu class used to cache the menu to avoid delays in UI response time. For now we save this in the 
	 * fragment. Also consider saving this as part of the active venue structures in the main application
	 */
	
	public class Menu {
		String venueId;
		ArrayList<String> headings;
		ArrayList<ArrayList<Item>> items;
		
		Menu (String venueId, ArrayList<String> headings, ArrayList<ArrayList<Item>> items) {
			this.headings = headings;
			this.items = items;
			this.venueId = venueId;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.v(TAG, "onCreateView()");
		
		mRootView = inflater.inflate(R.layout.menu_tab, container, false);
		mDrinksListView = (ExpandableListView) mRootView.findViewById(R.id.view_drinks_for_me_list);
		
//		LinearLayout customList = (LinearLayout) mRootView.findViewById(R.id.view_custom_drinks);
//		customList.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// Start custom drink activity
//				startActivity(new Intent().setClass(mActivity, CustomDrinksActivity.class));
//			}
//		});
		
		// Make sure the fragment pointed to by the activity is accurate
		mApp = (BartsyApplication) getActivity().getApplication();
		((VenueActivity) getActivity()).mDrinksFragment = this;
		mActivity = (VenueActivity) getActivity();
		
		// Initialize the menu
		initMenu();

		// Update the view
		updateView();
		
		

		return mRootView;
	}
	
	private void initMenu() {
		String[] headerTitles = {"Recently ordered", "Top available favorites","Mixed drinks","Bartsy Cocktails"};
		
		// Initialize the default headers and items
		ArrayList<String> headings = new ArrayList<String>();
		ArrayList<ArrayList<Item>> items = new ArrayList<ArrayList<Item>>();

		// Add the default headers and null items for those headers. If there is null then we have to display loading view
		for (String title : headerTitles) {
			headings.add(title);
			items.add(new ArrayList<Item>());
		}
		
		mMenu = new Menu(mApp.mActiveVenue.getId(), headings, items);
	}



	/************
	 * 
	 * TODO - Menu loader. This function is called as soon as the fragment is created, to expedite the 
	 * loading of the menu so that it's available to the UI as quickly as possible.
	 * 
	 * Two loaders are called from the loadMenu() function depending on what is available in terms of cache 
	 * (memory, DB, or nothing): one loads the menu from the web, the other from the local database. Both 
	 * loaders will automatically display the menu at the end of loading, so there is no need to explicitly 
	 * call updateView()
	 * 
	 */
	
	/*
	 * Main menu loader. Decides which sub-loader to use to load the menu either from the DB (if it exists)
	 * or via a call to the web service
	 * 
 	 * Expects mActivity to be pointing to the right context as this function could be called without having 
 	 * initialized this fragment's view. The caller should set mActivity
	 */
	
	boolean mMenuLoading = false;
	
	public void loadMenu() {
		
		Log.v(TAG, "loadMenu()");
		
		// To avoid calling this function multiple times while it's still running, we have an indicator showing
		// if the menu is being loaded. We set this indicator when the function stars and clear it when it's done
		
		if (mMenuLoading) {
			// Another instance of this function is currently running, no need to call it again
			Log.d(TAG, "Another instance of loadMenu() is running. Return.");
			return;
		}
		
		// Indicate start of loading menu
		mMenuLoading = true;
		Log.d(TAG, "Indicate start of menu loading");
		
		// Check if menu has already been cached for this venue
		
		if (mMenu == null || !mMenu.venueId.equals(mApp.mActiveVenue.getId())) {
			Log.v(TAG, "Menu not available in memory");

			// Menu is not already in memory, call the appropriate loader

			new Thread() {

				@Override
				public void run() {

					// Attempt to download the menu from the web
					downloadAndDisplayMenu();
					
					if (mMenu == null) {
						// Both loaders failed - abort. 
						Log.d(TAG, "Loaders failed...");
						
						// Mark the end end of menu loading as we failed and we're returning.
						mMenuLoading = false;
						Log.d(TAG, "Indicate end of menu loading");

						return;
					}
					// Loading of the menu successful. Display it using a handler because Android 
					// doesn't allow manipulating views from separate threads
					handler.post(new Runnable() {
						// Use a handler because Android doesn't allow manipulating views from separate threads
						@Override public void run() {
							updateView();
						}
					});				

					// Mark the end of menu loading
					mMenuLoading = false;
					Log.d(TAG, "Indicate end of menu loading");
				}
			}.start();
						
		} else {
			// Menu already in memory. Nothing to load so just display it.
			Log.v(TAG, "Menu available in memory - displaying it...");
			updateView();
			
			// Mark the end of menu loading
			mMenuLoading = false;
			Log.d(TAG, "Indicate end of menu loading");			
		}
	}
	
	
	/******
	 * 
	 * TODO - Web service functions for menu loader
	 * 
	 * @return
	 */
	
	
	/*
	 * Web service loader. Downloads the menu from the server using a web service call. 
	 * This is called from a background thread.
	 */
	private String downloadAndDisplayMenu() {

		Log.v(TAG, "downloadAndDisplayMenu()");
		if(mApp.mActiveVenue==null){
			return null;
		}
		
		Venue venue = mApp.mActiveVenue;
		
		// Step 1 - get the web service response and display the results in the view
		String response = WebServices.getMenuList(mApp, venue.getId());
		if (response == null) {
			Log.d(TAG, "Webservice get menu call failed");
			return null;
		} else {
			Log.v(TAG, "Webservice menu response: " + response == null? "null" : response);
		}
		
		// parse the response into a menu in-memory structure
		addMenuFromResponse (venue, response);
		// To save menu in application class
		
		return response;
	}


	
	/*
	 * Helper function for downloadAndDisplayMenu. Processes the server response and builds a menu object
	 */
	
	private void addMenuFromResponse (Venue venue, String response) {
		
		ArrayList<String> headings = new ArrayList<String>();
		ArrayList<ArrayList<Item>> items = new ArrayList<ArrayList<Item>>();
	
		try {
		
			JSONObject result = new JSONObject(response);
			String errorCode = result.getString("errorCode");
			String errorMessage = result.getString("errorMessage");
			String menus = result.getString("menu");
	
			JSONArray sections = new JSONArray(menus);
			Log.v(TAG, "Menus length " + sections.length());
	
			// Parse sections 
			for (int i = 0; i < sections.length(); i++) {
	
				JSONObject section = sections.getJSONObject(i);
				if (section.has("section_name") && section.has("subsections")) {

					JSONArray subsections = section.getJSONArray("subsections");
					
					if (subsections != null && subsections.length() > 0) {

						for (int j = 0; j < subsections.length(); j++) {
							
							JSONObject subSection = subsections.getJSONObject(j);
							String subsection_name = subSection.getString("subsection_name");

							String section_name = section.getString("section_name");

							// If it's a top level item (no section or subsection name) use a generic section title
							if (section_name.trim().length() == 0)
								section_name = subsection_name;
							else if (subsection_name.trim().length() > 0)
								section_name += " - " + subsection_name;
							if (section_name.trim().length() == 0)
								section_name = "Various items";
							
							// Add the heading title to the headings list 
							headings.add(section_name);

							// Add the list of items under that heading to the items list
							JSONArray contents = subSection.getJSONArray("contents");
							ArrayList<Item> subsection_contents = new ArrayList<Item>();
							for (int k = 0; k < contents.length(); k++) {
								Item menuDrink = new Item(contents.getJSONObject(k));
								// Try to parse price to decimal. if it is succeeded then drink will be added to the subsection list otherwise it will not add the list 
								if (menuDrink.valid != null) {
									subsection_contents.add(menuDrink);
								}
							}
							
							// Add the contents of the subsection to the list of items
							items.add(subsection_contents);
						}
					}
				}
			}	
		} catch (JSONException e) {
			e.printStackTrace();
		}
		mMenu.headings.addAll(headings);
		mMenu.items.addAll(items);
	}

	
	
	
	/*****
	 * 
	 * TODO - Viewer functions
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
		
		if (mMenu == null || !mMenu.venueId.equals(mApp.mActiveVenue.getId())) {
			Log.d(TAG, "Menu not available for display");
			loadMenu();
			return;
		}

		// If the view is not yet initialized, don't display the menu. This also 
		if (mDrinksListView == null) {
			Log.d(TAG, "View is not available");
			return;
		}

		
		// Display menu from memory into the view
		
		ArrayList<String> headings = mMenu.headings;
		final ArrayList<ArrayList<Item>> items = mMenu.items;

		Log.v(TAG, "Menu is in cache. Displaying " + headings.size() + " headings");

		try {

			mDrinksListView.setAdapter(new ExpandableListAdapter(mActivity,headings, items));

			// Setup the dialog to be displayed when clicking on an item in the menu
			mDrinksListView.setOnChildClickListener(new OnChildClickListener() {
				@Override
				public boolean onChildClick(ExpandableListView parent, View v,
						int groupPosition, int childPosition, long id) {
					
					if(mApp.mActiveVenue == null){
						// for now don't post an error message, but this should be fixed ASAP
						return false;
					}
					
					Item menuDrink = items.get(groupPosition).get(childPosition);

					// Figure out if we are adding the item to the active order or creating a new order
					Order order;
					if (mApp.hasActiveOrder()) {
						order = mApp.getActiveOrder();
						order.addItem(menuDrink);
					} else {
						order = new Order(mApp.mProfile, mApp.mProfile, mApp.mActiveVenue.getTaxRate(), Constants.defaultTip, menuDrink);
					}
					
					// Create an instance of the dialog fragment and show it
					DrinkDialogFragment dialog = new DrinkDialogFragment(order);
					dialog.show(getActivity().getSupportFragmentManager(),"Order drink");

					return false;
				}
			});
			
		} catch (Exception e) {
			
			e.printStackTrace();
			return;
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