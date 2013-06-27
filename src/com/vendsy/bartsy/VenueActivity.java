package com.vendsy.bartsy;

import static com.vendsy.bartsy.utils.Utilities.SENDER_ID;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;
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
import com.vendsy.bartsy.dialog.OfferDrinkDialogFragment;
import com.vendsy.bartsy.dialog.PeopleDialogFragment;
import com.vendsy.bartsy.model.AppObservable;
import com.vendsy.bartsy.model.MenuDrink;
import com.vendsy.bartsy.model.Order;
import com.vendsy.bartsy.model.Venue;
import com.vendsy.bartsy.utils.CommandParser;
import com.vendsy.bartsy.utils.CommandParser.BartsyCommand;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.Utilities;
import com.vendsy.bartsy.utils.WebServices;
import com.vendsy.bartsy.view.AppObserver;
import com.vendsy.bartsy.view.DrinksSectionFragment;
import com.vendsy.bartsy.view.OpenOrdersSectionView;
import com.vendsy.bartsy.view.OrdersSectionFragment;
import com.vendsy.bartsy.view.PastOrdersSectionView;
import com.vendsy.bartsy.view.PeopleSectionFragment;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;


public class VenueActivity extends SherlockFragmentActivity implements
		ActionBar.TabListener, DrinkDialogFragment.NoticeDialogListener,
		PeopleDialogFragment.UserDialogListener, AppObserver {

	/****************
	 * 
	 * 
	 * TODO - global variables
	 * 
	 */

	public static final String TAG = "VenueActivity";
	public DrinksSectionFragment mDrinksFragment = null;
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
		try {
			JSONObject json = new JSONObject(message);
			if (json.has("messageType") && json.getString("messageType").equals("DrinkOffered")) {
				mApp.displayOfferDrink(new Order(json),json.getString("senderBartsyId"));
			}
		}catch (JSONException e) {
		}

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
		DrinksSectionFragment d = (DrinksSectionFragment) getSupportFragmentManager().findFragmentById(R.string.title_menu);
		if (mDrinksFragment == null) {
			Log.v(TAG, "Drinks fragment not found. Creating one.");
			mDrinksFragment = new DrinksSectionFragment();
			mDrinksFragment.mActivity = this;
			mDrinksFragment.mApp = (BartsyApplication) getApplication();
			
			// Already Start loading the menu in the background so by the time the OS creates the view of this fragment
			// we've done some work
			mDrinksFragment.loadMenu();
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

        

		// Calling super after populating the menu is necessary here to ensure that the
		// action bar helpers have a chance to handle this event.
		boolean retValue = super.onCreateOptionsMenu(menu);


		return retValue;
	 }
	 
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		
		if (item == checkOut) {
			// Check out from the venue
			checkOutFromVenue(mApp.mActiveVenue);
			return super.onOptionsItemSelected(item);
		}
		
		
		switch (item.getItemId()) {
		
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, MainActivity.class);
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
			Intent intent = new Intent(this, MainActivity.class);
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
				String response = WebServices.userCheckInOrOut(VenueActivity.this, mApp.loadBartsyId(), venue.getId(), Constants.URL_USER_CHECK_OUT);

				if (response != null) {
					try {
						JSONObject json = new JSONObject(response);
						String errorCode = json.getString("errorCode");
						errorMessage = json.has("errorMessage") ? json.getString("errorMessage") : "";

						if (errorCode.equalsIgnoreCase("0")) {
							
							// Host checked user out successfully. Check the user out locally too.


							// Check into the venue locally
							mApp.userCheckOut();

							
							final Venue venuefinal = venue;
							
							mHandler.post(new Runnable() {
								public void run() {
									
									Toast.makeText(VenueActivity.this, "Checked out from " + mVenue.getName(), Toast.LENGTH_SHORT).show();


									// Start venue activity and finish this activity
									Intent intent = new Intent(mActivity, MainActivity.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									startActivity(intent);
									finish();
								}
							});
						} else {
							
							// An error has occurred and the user was not checked in - Toast it
							
							mApp.userCheckOut();

							mHandler.post(new Runnable() {
								public void run() {
									Toast.makeText(VenueActivity.this, "Error checking out. Please try again or restart application.", Toast.LENGTH_SHORT).show();
								}
							});
						}

					} catch (JSONException e) {
						e.printStackTrace();
					}
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
			Locale l = Locale.getDefault();

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
	private static final int HANDLE_DRINK_OFFERED_EVENT = 6;

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
		} else if (qualifier.equals(BartsyApplication.DRINK_OFFERED)) {
			Message message = mApplicationHandler
					.obtainMessage(HANDLE_DRINK_OFFERED_EVENT);
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

				// The history could be empty because this event is sent even on
				// a channel init
				if (message == null)
					break;

				BartsyCommand command = parseMessage(message);
				if (command != null) {
					processCommand(command);
				} else {
					Log.d(TAG, "Invalid command received");
				}
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
			case HANDLE_DRINK_OFFERED_EVENT:
				Log.v(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_DRINK_OFFERED_EVENT");
				if (mPeopleFragment != null) {
					Log.v(TAG, "DRINK_OFFERED dialog..");
					displayOfferDrinkDialog();
				}
				break;
			default:
				break;
			}
		}

		
	};
	
	private void displayOfferDrinkDialog() {
		OfferDrinkDialogFragment dialog = new OfferDrinkDialogFragment();
		dialog.show(getSupportFragmentManager(),"displayOfferDrink");
	}
	

	private void alljoynError() {
		if (mApp.getErrorModule() == BartsyApplication.Module.GENERAL
				|| mApp.getErrorModule() == BartsyApplication.Module.USE) {
			appendStatus("AllJoyn ERROR!!!!!!");
			// showDialog(DIALOG_ALLJOYN_ERROR_ID);
		}
	}

	public BartsyCommand parseMessage(String readMessage) {

		appendStatus("Message received: " + readMessage);

		// parse the command
		BartsyCommand command = null;
		ByteArrayInputStream stream = new ByteArrayInputStream(
				readMessage.getBytes());
		CommandParser commandParser = new CommandParser();

		try {
			command = commandParser.parse(stream);
		} catch (XmlPullParserException e) {
			// Auto-generated catch block
			e.printStackTrace();
			appendStatus("Invalid command format received");
			return null;
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
			appendStatus("Parser IO exception");
			return null;
		} finally {
			// Makes sure that the InputStream is closed after the app is
			// finished using it.
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					// Auto-generated catch block
					e.printStackTrace();
					appendStatus("Stream close IO exception");
					return null;
				}
			}
		}

		// check to make sure there was a
		if (command == null) {
			appendStatus("Parser succeeded but command is null");
			return null;
		}

		// Return successfully processed command
		return command;
	}

	void processCommand(BartsyCommand command) {
		if (command.opcode.equalsIgnoreCase("order_status_changed")) {
			if (processRemoteOrderStatusChanged(command))
				// An error occurred - for now log it
				appendStatus("ERROR PROCESSING ORDER STATUS CHANGED COMMAND");
		} else
			appendStatus("Unknown command: " + command.opcode);
	}

	/******
	 * 
	 * 
	 * TODO - Send/receive drink order
	 * 
	 */

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		// User touched the dialog's positive button
		MenuDrink drink = ((DrinkDialogFragment) dialog).drink;

		appendStatus("Placing order for: " + drink.getTitle());
		
		if (mApp.mActiveVenue == null) {
			// No active venue. We need to termiate venue activity. We also notify the user.
			Toast.makeText(this, "You need to be logged in to place an order", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		Order order = new Order();

		Float tipAmount = ((DrinkDialogFragment) dialog).tipAmount;

		order.initialize(Long.toString(mApp.mOrderIDs), // arg(0) - Client order  ID
				null, 									// arg(1) - This order still doesn't have a server-assigned ID
				drink.getTitle(), 						// arg(2) - Title
				drink.getDescription(), 				// arg(3) - Description
				Float.valueOf(drink.getPrice()), 						// arg(4) - Price
				tipAmount,
				Integer.toString(R.drawable.drinks), 	// arg(5) - Image resource for the order. for now always use the same picture for the drink drink.getImage(),
				mApp.mProfile);
		

		
		order.orderReceiver = ((DrinkDialogFragment) dialog).profile;
		// arg(6) - Each order contains the profile of the sender (and later the profile of the person that should pick it up)
		order.itemId = drink.getDrinkId();

		// invokePaypalPayment(); // To enable paypal payment

		processOrderData(order); // bypass PayPal for now for testing

	}
		
	private void processOrderData(Order order) {

		// Web service call - the response in handled asynchronously in processOrderDataHandler()
		if (WebServices.postOrderTOServer(mApp, order, mApp.mActiveVenue.getId(), processOrderDataHandler))
			// Failed to place syscall due to internal error
			Toast.makeText(mActivity, "Unable to place order. Please restart application.", Toast.LENGTH_SHORT).show();
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
				// The order was placed successfully 
				
				break;
				
			case HANDLE_ORDER_RESPONSE_FAILURE:
				// The syscall was not placed
				Toast.makeText(mActivity, "Unable to place order. Check your internet connection, restart application, reset application or download new version.", Toast.LENGTH_SHORT).show();
				break;
				
			case HANDLE_ORDER_RESPONSE_FAILURE_WITH_CODE:
				// The syscall got an error code
				Toast.makeText(mActivity, "Unable to place order: " + msg.obj, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	
	
	
	

	/*
	 * 
	 * TODO - Send/receive order status changed command
	 */

	public void sendOrderStatusChanged(Order order) {
		// Expects the order status and the server ID to be already set on this
		// end
		appendStatus("Sending order response for order: " + order.serverID);

		mApp.newLocalUserMessage("<command><opcode>order_status_changed</opcode>"
				+ "<argument>" + order.status + "</argument>" + // arg(0) -
																// status is
																// already
																// updated on
																// this end
				"<argument>" + order.serverID + "</argument>" + // arg(1)
				"<argument>" + order.clientID + "</argument>" + // arg(2)
				"<argument>" + order.orderSender.getBartsyId() + "</argument>" + // arg(3)
				"</command>");

		// Update tab title with the number of open orders
		updateOrdersCount();

	}

	public Boolean processRemoteOrderStatusChanged(BartsyCommand command) {
		// Return false if everything went well, true if we need to perform
		// recovery

		appendStatus("Received new remote order status: "
				+ command.arguments.get(1));

		String orderSenderID = command.arguments.get(3);

		// Because with Alljoyn every connected client gets a command, we make
		// sure this command is for us
		if (!orderSenderID.equalsIgnoreCase("" + mApp.mProfile.getBartsyId()))
			return true;

		mApp.updateOrder(command.arguments.get(1), command.arguments.get(0));

		return false;
	}

	/*
	 * 
	 * TODO - User interaction commands
	 */

	@Override
	public void onUserDialogPositiveClick(DialogFragment dialog) {
		// User touched the dialog's positive button

		Person user = ((PeopleDialogFragment) dialog).mUser;

		// DukesDonorUtil.getInstance().setDeviceId(registrationId);
		// displayMessage(context, getString(R.string.gcm_registered));
		// ServerUtilities.register(context, registrationId);

		appendStatus("Sending drink to: " + user.getNickname());

		mApp.newLocalUserMessage("<command><opcode>message</opcode>"
				+ "<argument>" + user.getNickname() + "</argument>"
				+ "<argument>" + "hi buddy" + "</argument>" + "</command>");
		appendStatus("Placed drink order");
	}

	@Override
	public void onUserDialogNegativeClick(DialogFragment dialog) {
		// User touched the dialog's positive button

		Person user = ((PeopleDialogFragment) dialog).mUser;

		appendStatus("Sending message to: " + user.getNickname());

		mApp.newLocalUserMessage("<command><opcode>message</opcode>"
				+ "<argument>" + user.getNickname() + "</argument>"
				+ "<argument>" + "hi buddy" + "</argument>" + "</command>");
		appendStatus("Sent message");
	}

}
