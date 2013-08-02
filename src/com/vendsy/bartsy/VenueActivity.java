package com.vendsy.bartsy;

import org.json.JSONException;
import org.json.JSONObject;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.plus.model.people.Person;
import com.vendsy.bartsy.dialog.DrinkDialogFragment;
import com.vendsy.bartsy.dialog.ProfileDialogFragment;
import com.vendsy.bartsy.model.AppObservable;
import com.vendsy.bartsy.model.Item;
import com.vendsy.bartsy.model.MessageData;
import com.vendsy.bartsy.model.Order;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.model.Venue;
import com.vendsy.bartsy.utils.Utilities;
import com.vendsy.bartsy.utils.WebServices;
import com.vendsy.bartsy.view.AppObserver;
import com.vendsy.bartsy.view.MenuSectionFragment;
import com.vendsy.bartsy.view.OrdersSectionFragment;
import com.vendsy.bartsy.view.PeopleSectionFragment;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;


public class VenueActivity extends SherlockFragmentActivity implements ActionBar.TabListener, DrinkDialogFragment.OrderDialogListener, 
	AppObserver, ProfileDialogFragment.ProfileDialogListener {

	/****************
	 * 
	 * 
	 * TODO - global variablesORDERS_UPDATED
	 * 
	 */

	public static final String TAG = "VenueActivity";
	public MenuSectionFragment mDrinksFragment = null;
	public OrdersSectionFragment mOrdersFragment = null; 
	public PeopleSectionFragment mPeopleFragment = null; 


	public void appendStatus(String status) {
		Log.d(TAG, status);
	}

	// A pointer to the parent application. In the MVC model, the parent
	// application is the Model
	// that this observe changes and observes

	public BartsyApplication mApp = null;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;



	/**********************
	 * 
	 * 
	 * TODO - Activity lifecycle management
	 * 
	 * 
	 **********************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.v(TAG, "onCreate()");
		
		// Set base view for the activity
		setContentView(R.layout.activity_main);

		// Setup application pointer
		mApp = (BartsyApplication) getApplication();

		initializeFragments();

		// Log function call
		appendStatus(this.toString() + "onCreate()");

		// Set up the action bar custom view
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setIcon(R.drawable.tickmark);

		// Create the adapter that will return a fragment for each of the primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
				@Override
				public void onPageSelected(int position) {
					actionBar.setSelectedNavigationItem(position);
				}
			});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}

		/*
		 * Now that we're all ready to go, we are ready to accept notifications
		 * from other components.
		 */
		mApp.addObserver(this);
		
		// Load the active venue which we will save onDestroy. This is so we don't get into situations where we
		// are in the activity and the application is killed without setting up an active venue
		mApp.loadActiveVenue();
		
		try {
			String message = getIntent().getExtras().getString(Utilities.EXTRA_MESSAGE, "");
			
			Log.v(TAG, "gcm message ::: " + message);
			
			if(message!=null){
				processPushNotification(message);
			}
		} catch (Exception e) {
			Log.e(TAG, "Error in gcm message ::: " + e.getMessage());
		}

	}
	
	/**
	 * Process push notification when the user selects on the PN message
	 * 
	 * @param message
	 */
	public void processPushNotification(String message){
		// TODO if message is empty then we can think it as order PN for now
		if(message.length()==0){
			// Display order tab
			mViewPager.setCurrentItem(2);
			return;
		}
		try {
			JSONObject json = new JSONObject(message);
			if (json.has("messageType")){
				String type = json.getString("messageType");
				// If the PN type is message then launch Message Activity
				if(type.equals("message")) {
					MessageData messageData = new MessageData(json);
					mApp.selectedUserProfile = getOtherPeopleProfile(messageData);
					if(mApp.selectedUserProfile!=null){
						Intent intent = new Intent(mActivity, MessagesActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						mActivity.startActivity(intent);
					}
				}
				// Display order tab
				else{
					mViewPager.setCurrentItem(2);
				}
			}
			
		}catch (JSONException e) {
		}
	}
	
	private UserProfile getOtherPeopleProfile(MessageData message){
		String userId = message.getReceiverId();
		if(userId.equals(mApp.mProfile.getBartsyId())){
			userId = message.getSenderId();
		}
		// Fetch user profile based on the user id
		for(UserProfile profile: mApp.mPeople){
			if(profile.getBartsyId().equals(userId)){
				return profile;
			}
		}
		
		return null;
	}

	/**
	 * Initialize the fragments
	 */
	private void initializeFragments() {

		Log.v(TAG, "initializeFragments()");

		// Initialize orders fragment - the fragment may still exist even though the activity has restarted
		OrdersSectionFragment f = (OrdersSectionFragment) getSupportFragmentManager().findFragmentById(R.string.title_orders);
		if (f == null) {
			Log.v(TAG, "Orders fragment not found. Creating one.");
			mOrdersFragment = new OrdersSectionFragment();
		} else {
			Log.v(TAG, "Orders fragment found.");
			mOrdersFragment = f;
		}

//		// Initialize past orders fragment - the fragment may still exist even though the activity has restarted
//		PastOrdersSectionFragment po = (PastOrdersSectionFragment) getSupportFragmentManager().findFragmentById(R.string.title_past_orders);
//		if (f == null) {
//			Log.v(TAG, "Past orders fragment not found. Creating one.");
//			mPastOrdersFragment = new PastOrdersSectionFragment();
//		} else {
//			Log.v(TAG, "Past orders fragment found.");
//			mPastOrdersFragment = po;
//		}

		// Initialize people fragment - reuse the fragment if it's already in memory
		PeopleSectionFragment p = (PeopleSectionFragment) getSupportFragmentManager().findFragmentById(R.string.title_people);
		if (mPeopleFragment == null) {
			Log.v(TAG, "People fragment not found. Creating one.");
			mPeopleFragment = new PeopleSectionFragment();
		} else {
			Log.v(TAG, "People fragment found.");
			mPeopleFragment = p;
		}

		// Initialize people fragment - reuse the fragment if it's already in memory
		MenuSectionFragment d = (MenuSectionFragment) getSupportFragmentManager().findFragmentById(R.string.title_menu);
		if (mDrinksFragment == null) {
			Log.v(TAG, "Drinks fragment not found. Creating one.");
			mDrinksFragment = new MenuSectionFragment();
			
			// Already Start loading the menu in the background so by the time the OS creates the view of this fragment
			// we've done some work
			mDrinksFragment.loadMenus(this);
		} else {
			Log.v(TAG, "Drinks fragment found.");
			mDrinksFragment = d;
		}
	}


	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this); // Add this method.

		// Log function call
		appendStatus(this.toString() + "onCreate()");

		/*
		 * Keep a pointer to the Android Application class around. We use this
		 * as the Model for our MVC-based application. Whenever we are started
		 * we need to "check in" with the application so it can ensure that our
		 * required services are running.
		 */

		mApp.checkin();

		/*
		 * update the state of the action bar depending on our connection state.
		 */
		updateActionBarStatus();

	}

	@Override
	public void onStop() {
		super.onStop();
		Log.v(TAG, "onStop()");
		
		// Save active venue
		mApp.saveActiveVenue();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.v(TAG, "onDestroy()");

		// Only stop listening to messages from the application when we're killed (keep
		// listening while in the background with no active view)
		mApp.deleteObserver(this);

	}

	/******
	 * 
	 * 
	 * TODO - Setup sherlock menu
	 * 
	 */
	
	MenuItem checkOut = null;
	
	 @Override
	 public boolean onCreateOptionsMenu(Menu menu) {

        checkOut = menu.add("Check out...");
        checkOut.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		// Update the checkout/orders button
		if (checkOut != null) {
			if (mApp.hasActiveOrder()) {
				checkOut.setTitle(mApp.getActiveOrder().items.size() + " items");
				checkOut.setIcon(R.drawable.drink);
			} else {
				checkOut.setTitle("Check out...");
			}
		}
		
		// Calling super after populating the menu is necessary here to ensure that the action bar helpers have a chance to handle this event.
		boolean retValue = super.onCreateOptionsMenu(menu);

		return retValue;
	 }
	 
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (item == checkOut) {

			if (mApp.hasActiveOrder()) {
				
				// if we have an order, show the order dialog 
				new DrinkDialogFragment(mApp.getActiveOrder()).show(getSupportFragmentManager(),"Order drink");

			} else {
				
				// Check out from the venue
				checkOutFromVenue(mApp.mActiveVenue);
				return super.onOptionsItemSelected(item);
			}
		}
		
		switch (item.getItemId()) {
		
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, MapActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return super.onOptionsItemSelected(item);

		case R.id.action_settings:
			Intent settingsActivity = new Intent(getBaseContext(),
					SettingsActivity.class);
			startActivity(settingsActivity);
			break;

		case R.id.action_quit:
			mApp.quit();
			break;

		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	
	

	/**
	 * Invokes when the venue selected in the list view
	 * 
	 * @param venue
	 */

	// We're using this variable as a message buffer with the background service checking user in
	Venue mVenue = null;
	
	protected void checkOutFromVenue(Venue venue) {
		
		Log.v(TAG, "venueSelectedAction(" + venue.getId() + ")");

		// Initialize message buffer for alertBox() and userCheckinAction()
		mVenue = venue;

		// Check user into venue after confirmation
		
		if (mApp.mActiveVenue == null) {
			// Not checked in apparently. just end activity
			Intent intent = new Intent(this, MapActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
		} else if (mApp.getOrderCount() > 0) {
			// We already have a local active venue different than the one selected
			userCheckOutAlert("You have OPEN ORDERS at " + mApp.mActiveVenue.getName() +
					". If you checkout they will be cancelled and you will still be charged.\n\nAre you sure?", venue);
		} else if (mApp.mActiveVenue != null) {
			// Require to ask confirmation to check in to new venue
			userCheckOutAlert("Are you sure you want to check out from " + mApp.mActiveVenue.getName() + "?", venue);
		}
	}
	

	/**
	 * To display confirmation alert box when the user selects venue in the list
	 * view
	 * 
	 * @param message
	 * @param venue
	 */
	private void userCheckOutAlert(String message, final Venue venue) {

		AlertDialog.Builder builder = new AlertDialog.Builder(VenueActivity.this);
		builder.setCancelable(true);
		builder.setTitle("Check out?");
		builder.setInverseBackgroundForced(true);
		builder.setMessage(message);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
				invokeUserCheckOutSyscall();

			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();

	}

	/**
	 * Invokes this action when the user selects on the venue and calls check in
	 * web service
	 * 
	 * @param Uses a local variable "mVenue" as a parameter buffer
	 * 
	 */
	
	String errorMessage = null;
	Handler mHandler = new Handler();
	
	protected void invokeUserCheckOutSyscall() {
		new Thread() {
			public void run() {
				
				// Load the venue paramenter from the local parameter buffer
				Venue venue = mVenue;
				
				// Invoke the user checkin syscall
				String response = WebServices.userCheckInOrOut(mApp, mApp.loadBartsyId(), venue.getId(), WebServices.URL_USER_CHECK_OUT);

				String errorCode = "JSON error";
				if (response != null) {
					try {
						JSONObject json = new JSONObject(response);
						errorCode = json.getString("errorCode");
						errorMessage = json.has("errorMessage") ? json.getString("errorMessage") : "";
					} catch (JSONException e) {
						e.printStackTrace();
					}

				final String displayText;
				if (errorCode.equalsIgnoreCase("0")) {
					displayText = "Checked out from " + mVenue.getName();
				} else {
					displayText = "Error checking out. Please try again or restart application.";
				}
				
				// Check the user out locally too.
				mApp.userCheckOut();
				mHandler.post(new Runnable() {
					public void run() {
							
						Toast.makeText(VenueActivity.this, displayText, Toast.LENGTH_SHORT).show();
						// Start map activity and finish this activity
						Intent intent = new Intent(mActivity, MapActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						finish();
					}
				});

				}
			};
		}.start();
	}
	
	
	private void updateActionBarStatus() {

		Log.v(TAG, "updateActionBarStatus()");

		String name;
		
		if (mApp.mActiveVenue == null) {
			// Not checked in

			appendStatus("Channel iddle");

			// Set app title as not checked in for now. In the future this
			// should be an illegal state
			name = "Not checked in!";

		} else {
			// Checked-in

			name = mApp.mActiveVenue.getName();
		}

		getSupportActionBar().setTitle(name);

		// Update the tab titles
		updateOrdersCount();
		updatePeopleCount();
		
		// Update the checkout/orders button
		if (checkOut != null) {
			if (mApp.hasActiveOrder()) {
				checkOut.setTitle(mApp.getActiveOrder().items.size() + " items");
				checkOut.setIcon(R.drawable.drink);
			} else {
				checkOut.setTitle("Check out...");
				checkOut.setIcon(null);
			}
		}
	}
	
	/*
	 * Updates the action bar tab with the number of open orders
	 */

	public void updateOrdersCount() {
		for (int i= 0 ; i < mTabs.length ; i++) {
			if (mTabs[i] == R.string.title_orders) {
				// Found the right tab - update it
				getSupportActionBar().getTabAt(i).setText("Orders (" + mApp.getOrderCount() + ")");
				return;
			}
		}
	}
	

	/*
	 * Updates the action bar tab with the number of open orders
	 */

	public void updatePeopleCount() {
		for (int i= 0 ; i < mTabs.length ; i++) {
			if (mTabs[i] == R.string.title_people) {
				// Found the right tab - update it
				getSupportActionBar().getTabAt(i).setText("People (" +  mApp.mActiveVenue.getUserCount() + ")");
				return;
			}
		}
	}

	/***********
	 * 
	 * TODO - Views management
	 * 
	 */

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());

		// upon selecting the people tab we want to update the list of people
		// from the server
		if (mTabs[tab.getPosition()] == R.string.title_people) {
			mPeopleFragment.updatePeopleView();
		}
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	VenueActivity main_activity = this;

	private int mTabs[] = { R.string.title_people, R.string.title_menu, R.string.title_orders};

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (mTabs[position]) {
			case R.string.title_orders: // The order tab (for bar owners)
				return (mOrdersFragment);
//			case R.string.title_past_orders: // The order tab (for bar owners)
//				return (mPastOrdersFragment);
			case R.string.title_menu: // The drinks tab allows to order drinks
										// from previous orders, favorites, menu
										// items, drink guides or completely
										// custom.
				return (mDrinksFragment);
			case R.string.title_people: // The people tab shows who's local,
										// allows to send them a drink or a chat
										// request if they're available and
										// allows to leave comments for others
										// on the venue
				return (mPeopleFragment);
			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			// Show total pages.
			return mTabs.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {

			return getString(mTabs[position]);
		}
	}

	void createNotification(String title, String text) {

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title).setContentText(text);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, VenueActivity.class);

		// The stack builder object will contain an artificial back stack for
		// the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(VenueActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
				PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(0, mBuilder.build());

	}
	
	/**
	 * It will call when the user selects on the GCM message when it is in VenueActivity
	 */
	@Override 
	protected void onNewIntent(Intent intent) {

		super.onNewIntent(intent);

		try {
			String message = getIntent().getExtras().getString(Utilities.EXTRA_MESSAGE, "");
			
			Log.v(TAG, "gcm message ::: " + message);
			
			if(message!=null){
				processPushNotification(message);
			}
		} catch (Exception e) {
			Log.e(TAG, "Error in gcm message - onNewIntent() ::: " + e.getMessage());
		}

	}

	/*********************
	 * 
	 * 
	 * TODO - Bartsy protocol command handling and order management TODO - TODO
	 * - General command parsing/second TODO - Order command TODO - Order reply
	 * command TODO - Profile command TODO - User interaction commands.
	 * 
	 * 
	 */

	private static final int HANDLE_APPLICATION_QUIT_EVENT = 0;
	private static final int HANDLE_HISTORY_CHANGED_EVENT = 1;
	private static final int HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT = 2;
	private static final int HANDLE_ALLJOYN_ERROR_EVENT = 3;
	private static final int HANDLE_ORDERS_UPDATED_EVENT = 4;
	private static final int HANDLE_PEOPLE_UPDATED_EVENT = 5;
	private static final int HANDLE_MENUS_UPDATED_EVENT = 6;

	public synchronized void update(AppObservable o, Object arg) {
		Log.v(TAG, "update(" + arg + ")");
		String qualifier = (String) arg;

		if (qualifier.equals(BartsyApplication.APPLICATION_QUIT_EVENT)) {
			Message message = mApplicationHandler
					.obtainMessage(HANDLE_APPLICATION_QUIT_EVENT);
			mApplicationHandler.sendMessage(message);
		} else if (qualifier.equals(BartsyApplication.HISTORY_CHANGED_EVENT)) {
			Message message = mApplicationHandler
					.obtainMessage(HANDLE_HISTORY_CHANGED_EVENT);
			mApplicationHandler.sendMessage(message);
		} else if (qualifier
				.equals(BartsyApplication.USE_CHANNEL_STATE_CHANGED_EVENT)) {
			Message message = mApplicationHandler
					.obtainMessage(HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT);
			mApplicationHandler.sendMessage(message);
		} else if (qualifier.equals(BartsyApplication.ALLJOYN_ERROR_EVENT)) {
			Message message = mApplicationHandler
					.obtainMessage(HANDLE_ALLJOYN_ERROR_EVENT);
			mApplicationHandler.sendMessage(message);
		} else if (qualifier.equals(BartsyApplication.ORDERS_UPDATED)) {
			Message message = mApplicationHandler
					.obtainMessage(HANDLE_ORDERS_UPDATED_EVENT);
			mApplicationHandler.sendMessage(message);
		} else if (qualifier.equals(BartsyApplication.PEOPLE_UPDATED)) {
			Message message = mApplicationHandler
					.obtainMessage(HANDLE_PEOPLE_UPDATED_EVENT);
			mApplicationHandler.sendMessage(message);
		} else if (qualifier.equals(BartsyApplication.MENUS_UPDATED)) {
			Message message = mApplicationHandler
					.obtainMessage(HANDLE_MENUS_UPDATED_EVENT);
			mApplicationHandler.sendMessage(message);
		} else if (qualifier.equals(BartsyApplication.NEW_CHAT_MESSAGE_RECEIVED)) {
			Message message = mApplicationHandler
					.obtainMessage(HANDLE_PEOPLE_UPDATED_EVENT);
			mApplicationHandler.sendMessage(message);
		} 
	}

	private Handler mApplicationHandler= new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_APPLICATION_QUIT_EVENT:
				Log.v(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_APPLICATION_QUIT_EVENT");
				finish();
				break;
			case HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT:
				Log.v(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT");
				updateActionBarStatus();
				break;
			case HANDLE_HISTORY_CHANGED_EVENT: {
				Log.v(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_HISTORY_CHANGED_EVENT");

				String message = mApp.getLastMessage();
				break;
			}
			case HANDLE_ALLJOYN_ERROR_EVENT: {
				Log.v(TAG,
						"BartsyActivity.mhandler.handleMessage(): HANDLE_ALLJOYN_ERROR_EVENT");
				alljoynError();
			}
				break;
			case HANDLE_ORDERS_UPDATED_EVENT:
				Log.v(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_ORDERS_UPDATED_EVENT");
				if (mOrdersFragment != null) {
					Log.v(TAG, "Updating orders view and count...");
					mOrdersFragment.updateOrdersView();
					updateOrdersCount();
				}
				break;
			case HANDLE_PEOPLE_UPDATED_EVENT:
				
				Log.v(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_PEOPLE_UPDATED_EVENT");
				if (mPeopleFragment != null) {
					Log.v(TAG, "Updating people view and count...");
					mPeopleFragment.updatePeopleView();
					updatePeopleCount();
				}
				break;
			case HANDLE_MENUS_UPDATED_EVENT:
				
				Log.v(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_MENUS_UPDATED_EVENT");
				if (mDrinksFragment != null) {
					Log.v(TAG, "Updating menus...");
					mDrinksFragment.loadMenus(VenueActivity.this);
				}
				break;
			default:
				break;
			}
		}

		
	};
	

	private void alljoynError() {
		if (mApp.getErrorModule() == BartsyApplication.Module.GENERAL
				|| mApp.getErrorModule() == BartsyApplication.Module.USE) {
			appendStatus("AllJoyn ERROR!!!!!!");
			// showDialog(DIALOG_ALLJOYN_ERROR_ID);
		}
	}



	/******
	 * 
	 * 
	 * TODO - Send/receive drink order
	 * 
	 */

	@Override
	public void onOrderDialogPositiveClick(DrinkDialogFragment dialog) {

		if (mApp.mActiveVenue == null) {
			// No active venue. We need to terminate venue activity. We also notify the user.
			Toast.makeText(this, "You need to be logged in to place an order", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		// Update action bar
		updateActionBarStatus();
		
		// invokePaypalPayment(); // To enable paypal payment

		// Web service call - the response in handled asynchronously in processOrderDataHandler()
		if (WebServices.postOrderTOServer(mApp, dialog.order, mApp.mActiveVenue.getId(), processOrderDataHandler)) {
			// Failed to place syscall due to internal error
			Toast.makeText(mActivity, "Unable to place order. Please restart application.", Toast.LENGTH_SHORT).show();
		}
	}
	
	
	@Override
	public void onOrderDialogNegativeClick(DrinkDialogFragment dialog) {

		if (mApp.mActiveVenue == null) {
			// No active venue. We need to terminate venue activity. We also notify the user.
			Toast.makeText(this, "You need to be checked in to place an order", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		// Make this order the active order
		mApp.setActiveOrder(dialog.order);
		
		// Update action bar
		updateActionBarStatus();
	}
	
	
	
	/**
	 * 
	 * Response handler for asynchronous syscalls of processOrderData()
	 * 
	 */
	
	// Handler variables
	VenueActivity mActivity = this;
	
	// Response codes
	public static final int HANDLE_ORDER_RESPONSE_SUCCESS = 0;
	public static final int HANDLE_ORDER_RESPONSE_FAILURE = 1;
	public static final int HANDLE_ORDER_RESPONSE_FAILURE_WITH_CODE = 2;
	
	// The handler code
	public Handler processOrderDataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			Log.v(TAG, "VenueActivity.processOrderDataHandler.handleMessage(" + msg.arg1 + ", " + msg.arg2 + ", " + msg.obj + ")");
			
			switch (msg.what) {
			case HANDLE_ORDER_RESPONSE_SUCCESS:
				
				// If there is an active order, remove it
				if (mApp.hasActiveOrder())
					mApp.eraseActiveOrder();

				// Update action bar
				updateActionBarStatus();
				
				// Synchronize orders
				mApp.syncOrders();
				
				// The order was placed successfully 
				Toast.makeText(mActivity, "Your order was placed.", Toast.LENGTH_SHORT).show();

				break;
				
			case HANDLE_ORDER_RESPONSE_FAILURE:
				// The sys call was not placed
				Toast.makeText(mActivity, "Unable to place order.", Toast.LENGTH_SHORT).show();
				Toast.makeText(mActivity, "Check your internet connection, restart application, reset application or download new version.", Toast.LENGTH_SHORT).show();
				break;
				
			case HANDLE_ORDER_RESPONSE_FAILURE_WITH_CODE:
				// The sys call got an error code
				Toast.makeText(mActivity, "Unable to place order.", Toast.LENGTH_SHORT).show();
				Toast.makeText(mActivity, msg.obj.toString(), Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};



	/*
	 * 
	 * TODO - User interaction commands. These are the buttons of the user profile dialog that appears when clicking on a profile in the people list
	 */

	@Override
	public void onUserDialogPositiveClick(ProfileDialogFragment dialog) {

		Log.v(TAG, "Sending drink to: " + dialog.mUser.getNickname());

		// Start a blank order for the given user
		mApp.setActiveOrder(new Order(mApp.mProfile, dialog.mUser, mApp.mActiveVenue.getTaxRate()));
		updateActionBarStatus();
		
		// Let the user know that they can select the item to send and take them to the menu tab
		Toast.makeText(this, "Select a item or more for " + dialog.mUser.getNickname(), Toast.LENGTH_SHORT).show();
		for (int i = 0; i < mTabs.length ; i++) {		
			if (mTabs[i] == R.string.title_menu) {
				getSupportActionBar().setSelectedNavigationItem(i);
				return;
			}
		}
	}

	@Override
	public void onUserDialogNegativeClick(ProfileDialogFragment dialog) {

		Log.v(TAG, "Sending message to: " + dialog.mUser.getNickname());

		mApp.selectedUserProfile = dialog.mUser;
		Intent intent = new Intent(this, MessagesActivity.class);
		startActivity(intent);
	}

}
