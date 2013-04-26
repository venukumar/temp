package com.kellislabs.bartsy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParserException;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.ViewPager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.ActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.plus.model.people.Person;
import com.kellislabs.bartsy.CommandParser.BartsyCommand;


public class BartsyActivity extends FragmentActivity implements
		ActionBar.TabListener, DrinkDialogFragment.NoticeDialogListener, PeopleDialogFragment.UserDialogListener,
		AllJoynObserver {

 
	/****************
	 * 
	 * 
	 * TODO - Global variables  
	 * 
	 *  
	 */
       
	public boolean mIsServer = false;						// the app switches personalities for now depending on this (tablet = bar, phone = user)
	public boolean mDebug = false;							// set up for extra debugging tabs
    public static final String TAG = "Bartsy";
    private DebugSectionFragment mDebugFragment = null;
    private OrdersSectionFragment mOrdersFragment = null;
    private DrinksSectionFragment mDrinksFragment = null;
    private PeopleSectionFragment mPeopleFragment = null;

    public void appendStatus(String status) {
    	Log.d(TAG, status);
    	if (mDebug) mDebugFragment.appendLine(status);   	
    }

    // A pointer to the parent application. In the MVC model, the parent application is the Model
    // that this observe changes and observes
    
    private BartsyApplication mBartsyApplication = null;    
    
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

    private static final int HANDLE_APPLICATION_QUIT_EVENT = 0;
    private static final int HANDLE_HISTORY_CHANGED_EVENT = 1;
    private static final int HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT = 2;
    private static final int HANDLE_NEW_CHANNEL_FOUND_EVENT = 3;
    private static final int HANDLE_ALLJOYN_ERROR_EVENT = 4;
    private static final int HANDLE_HOST_CHANNEL_STATE_CHANGED_EVENT = 5;

    
    
    
    /**************************************
     * 
     * 
     * TODO - Save/restore state
     * 
     * 
     */
    /*
    static final String STATE_SCORE = "playerScore";
    static final String STATE_LEVEL = "playerLevel";
    ...

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putInt(STATE_SCORE, mCurrentScore);
        savedInstanceState.putInt(STATE_LEVEL, mCurrentLevel);
        savedInstanceState.
        
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }
    
    
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
       
        // Restore state members from saved instance
        mCurrentScore = savedInstanceState.getInt(STATE_SCORE);
        mCurrentLevel = savedInstanceState.getInt(STATE_LEVEL);
    }
    
*/    
    
    
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
		
		/*
		 * Init, debug and views setup
		 */

		// Make sure global settings get set first
		mIsServer = getResources().getBoolean(R.bool.isTablet);

		// Set base view for the activity
		setContentView(R.layout.activity_main);

		
		// Initialize debug view for logging purposes
		if (mDebugFragment == null) {
			mDebugFragment = new DebugSectionFragment();
		}
		
		// Initialize orders view
		if (mOrdersFragment == null) {
			mOrdersFragment = new OrdersSectionFragment();
		}
		
		// Initialize people view 
		if (mPeopleFragment == null) {
			mPeopleFragment = new PeopleSectionFragment();
		}
		
		// Log function call
		appendStatus(this.toString() + "onCreate()");


		// Set up the action bar custom view
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	    actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//	    getActionBar().setCustomView(View.inflate(getApplicationContext(), R.layout.actionbar_indeterminate_progress, null));		
		actionBar.setDisplayShowHomeEnabled(true);
		View homeIcon = findViewById(android.R.id.home);
		((View) homeIcon.getParent()).setVisibility(View.GONE);
		
		// Create the adapter that will return a fragment for each of the 
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
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
		
		// Load user profile information if it exists
		loadUserProfile();
		
		// Update channel state. If we're connected to an existing channel, use that channels name. 
		// If not, set our channel to 

	}

    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this); // Add this method.

		// Log function call
		appendStatus(this.toString() + "onCreate()");

      /*
       * Keep a pointer to the Android Application class around.  We use this
       * as the Model for our MVC-based application.  Whenever we are started
       * we need to "check in" with the application so it can ensure that our
       * required services are running.
       */
      mBartsyApplication = (BartsyApplication)getApplication();
      mBartsyApplication.checkin();
      
      /*
       * Call down into the model to get its current state.  Since the model
       * outlives its Activities, this may actually be a lot of state and not
       * just empty.
       */
      updateChannelState();
      
      /*
       * Now that we're all ready to go, we are ready to accept notifications
       * from other components.
       */
      mBartsyApplication.addObserver(this);
      if (mIsServer) {
    	  // This initiates a series of events from the application, handled by the hander
    	  mBartsyApplication.hostInitChannel();
      }
    }

    
	public void onStop() {
        super.onStop();
        appendStatus("onStop()");
        mBartsyApplication = (BartsyApplication)getApplication();
        mBartsyApplication.deleteObserver(this);
 	}

	
	/******
	 * 
	 * 
	 * TODO - Action bar (menu) helper functions
	 * 
	 */
	
	
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		
        // Calling super after populating the menu is necessary here to ensure that the
        // action bar helpers have a chance to handle this event.
        boolean retValue = super.onCreateOptionsMenu(menu);
        
        
        /*
         * Set up Action buttons
         */
        
        MenuItem item ;
        View menuItem;
        LayoutInflater inflater = (LayoutInflater) 
        		getActionBar().getThemedContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        // Set requests action 
        item = menu.findItem(R.id.action_requests);
//    	((TextView )mConnectedView.findViewById(R.id.actionBarConnectedText)).setText("(1 customer)");
        item.setActionView(inflater.inflate(R.layout.actionbar_requests, null));
       
        
        // Set messages action 
        item = menu.findItem(R.id.action_messages);
//    	((TextView )mConnectedView.findViewById(R.id.actionBarConnectedText)).setText("(1 customer)");
        menuItem = inflater.inflate(R.layout.actionbar_messages, null);
		item.setActionView(menuItem);
        
        // Set notifications action 
        item = menu.findItem(R.id.action_notifications);
        menuItem = inflater.inflate(R.layout.actionbar_notifications, null);
//    	((TextView )mConnectedView.findViewById(R.id.actionBarConnectedText)).setText("(1 customer)");
        item.setActionView(menuItem);
        menuItem = item.getActionView();
        menuItem.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 Intent activity = new Intent(getBaseContext(), NotificationsActivity.class);
				 startActivity(activity);	 
			}});
        item.expandActionView();
        
        return retValue;
 	}

 

    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	            // app icon in action bar clicked; go home
	            Intent intent = new Intent(this, BartsyActivity.class);
	            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(intent);
	            return true;
	            
	        case R.id.action_messages:
	        	item.getActionView().findViewById(R.id.view_action_bar_messages).setBackgroundColor(0xaaaaee);
	        	break;
/*
	            <item android:id="@+id/menu_refresh"
	                    android:title="@string/menu_refresh"
	                    android:icon="@android:drawable/ic_popup_sync"
	                    android:showAsAction="always" />

	            //            case R.id.menu_refresh:
                Toast.makeText(this, "Restarting P2P...", Toast.LENGTH_SHORT).show();

                // Restart WiFi Direct discovery
                restartP2P();
                break;
  */ 
	        case R.id.action_settings:
	            Intent settingsActivity = new Intent(getBaseContext(), SettingsActivity.class);
	            startActivity(settingsActivity);
                break;

	        case R.id.action_quit:
                mBartsyApplication.quit();
	        	break;
                
	         default:
	        	break;
	    }
        return super.onOptionsItemSelected(item);
	}
	
	
    private void updateChannelState() {
    	
        Log.i(TAG, "updateChannelState()");

        AllJoynService.UseChannelState channelState = mBartsyApplication.useGetChannelState();
    	String name = mBartsyApplication.useGetChannelName();
    	if (name == null) {
    		name = "Not set";
    	}
        
		View view = null;
        
        switch (channelState) {
        case IDLE:
    	    view = View.inflate(getApplicationContext(), R.layout.actionbar_indeterminate_progress, null);
            appendStatus("Channel iddle");
            break;
        case JOINED: // There are only two states so this switch statement is complete
            // Set action bar item to the connected machine's name
    	    view = View.inflate(getApplicationContext(), R.layout.actionbar_connected, null);
    	    ((TextView )view.findViewById(R.id.actionBarConnectedText)).setText(name);
            appendStatus("Joined a channel");
            
            
            // For now, send the profile here. It should be actually stored in the application and sent 
            // by the Alljoyn service when a channel is joined

            appendStatus("About to send profile to host");
            sendProfileCommand();
            
            break;	
        }
    
        // Set up the map button with a click listener that starts the map activity
    	view.findViewById(R.id.view_action_bar_map).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent mapActivity = new Intent(getBaseContext(), MapActivity.class);
                startActivity(mapActivity);	 
			}
    	});
    	
    	getActionBar().setCustomView(view);		
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
	BartsyActivity main_activity = this;

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		// Define tabs here
//		private final int tabsDebugTablet[] = {R.string.title_debug, R.string.title_drink_orders, R.string.title_customers,
//				   R.string.title_drinks, R.string.title_people, R.string.title_map, R.string.title_notifications};
//		private final int tabsDebugPhone[] = {R.string.title_debug, R.string.title_notifications, R.string.title_drinks, 
//				R.string.title_people, R.string.title_map, R.string.title_drink_orders, R.string.title_customers};
		private final int tabsDebugTablet[] = {R.string.title_debug, R.string.title_drink_orders, R.string.title_people, R.string.title_inventory};
		private final int tabsDebugPhone[] = {R.string.title_debug, R.string.title_drinks, 
				R.string.title_people, R.string.title_drink_orders};
		private final int tabsTablet[] = {R.string.title_drink_orders, R.string.title_people, R.string.title_inventory};
		private final int tabsPhone[] = {R.string.title_drinks, R.string.title_people, 
				R.string.title_drink_orders};
		
		private int mTabs[];

		
		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			
			if (mDebug && mIsServer) {
				mTabs = tabsDebugTablet;
			} else if (mDebug && !mIsServer) {
				mTabs = tabsDebugPhone;
			} else if (mIsServer) {
				mTabs = tabsTablet;
			} else {
				mTabs = tabsPhone;
			}
		}
		
		
		@Override
		public Fragment getItem(int position) {
			switch (mTabs[position]) {
			case R.string.title_debug: // Debug window
				return(mDebugFragment);	
			case R.string.title_drink_orders: // The order tab (for bar owners)
				return (mOrdersFragment);
			case R.string.title_inventory: // The customers tab (for bar owners)
				return (new CustomersSectionFragment());
			case R.string.title_drinks: // The drinks  tab allows to order drinks from previous orders, favorites, menu items, drink guides or completely custom.
				mDrinksFragment = new DrinksSectionFragment();
				return( mDrinksFragment);
			case R.string.title_people: // The people tab shows who's local, allows to send them a drink or a chat request if they're available and allows to leave comments for others on the venue
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
			
			return getString(mTabs[position]).toUpperCase(l);
		}
	}
	
	
	void createNotification (String title, String text) {
    	
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
		        .setContentTitle(title)
		        .setContentText(text);
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, BartsyActivity.class);

		
		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(BartsyActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mNotificationManager.notify(0, mBuilder.build());

	}

    /*********************
     * 
     * 
     * TODO - Bartsy protocol command handling and order management
     * TODO -
     * TODO - General command parsing/second
     * TODO - Order command
     * TODO - Order reply command
     * TODO - Profile command
     * TODO - User interaction commands.
     * 
     * 
     */


    public synchronized void update(AllJoynObservable o, Object arg) {
        Log.i(TAG, "update(" + arg + ")");
        String qualifier = (String)arg;
        
        if (qualifier.equals(BartsyApplication.APPLICATION_QUIT_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_APPLICATION_QUIT_EVENT);
            mHandler.sendMessage(message);
        } else if (qualifier.equals(BartsyApplication.HISTORY_CHANGED_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_HISTORY_CHANGED_EVENT);
            mHandler.sendMessage(message);
        } else if (qualifier.equals(BartsyApplication.USE_CHANNEL_STATE_CHANGED_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT);
            mHandler.sendMessage(message);
        } else if (qualifier.equals(BartsyApplication.HOST_CHANNEL_STATE_CHANGED_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_HOST_CHANNEL_STATE_CHANGED_EVENT);
            mHandler.sendMessage(message);
        } else if (qualifier.equals(BartsyApplication.NEW_CHANNEL_FOUND_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_NEW_CHANNEL_FOUND_EVENT);
            mHandler.sendMessage(message);
        } else if (qualifier.equals(BartsyApplication.ALLJOYN_ERROR_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_ALLJOYN_ERROR_EVENT);
            mHandler.sendMessage(message);
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case HANDLE_APPLICATION_QUIT_EVENT: {
	            Log.i(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_APPLICATION_QUIT_EVENT");
	            finish();
	            break; 
            }
            case HANDLE_NEW_CHANNEL_FOUND_EVENT: 
                Log.i(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_NEW_CHANNEL_FOUND_EVENT");
             
                if (mBartsyApplication.useGetChannelState() == AllJoynService.UseChannelState.IDLE) {
                	// We haven't yet connected to any channels, connect to the first one found
	                List<String> channels = mBartsyApplication.getFoundChannels(); 
	                String channel = null;
	                for (String c : channels) {
	                	int lastDot = c.lastIndexOf('.');
	                	if (lastDot < 0) {
	                		continue;
	                	}
	                	channel = c.substring(lastDot + 1);
	                }

	                if (channel == null) {
	                	appendStatus("ERROR - NEW CHANNEL EVENT WITH EMPTY CHANNEL LIST");
	                	break; 
	                }
	                
	                Log.i(TAG, "BartsyActivity.mhandler.handleMessage(): setting use channel name to: " + channels.get(0));
	                mBartsyApplication.useSetChannelName(channel);
	                mBartsyApplication.useJoinChannel();
                }
            	String name = mBartsyApplication.useGetChannelName();
            	if (name == null) {
            		name = "Not set";
            	}
	            break;
            case HANDLE_HOST_CHANNEL_STATE_CHANGED_EVENT: 
                Log.i(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_HOST_CHANNEL_STATE_CHANGED_EVENT");
                // Host channel started, join the channel as a user in order to be able to start receiving messages
                
                switch ( mBartsyApplication.hostGetChannelState()) {
                case IDLE:
                	// wait for the channel to be named
                    Log.i(TAG, "Host channel state is idle. Naming it: " + getResources().getString(R.string.config_venue_channel_name));
              	  	mBartsyApplication.hostSetChannelName(getResources().getString(R.string.config_venue_channel_name));
	                mBartsyApplication.hostStartChannel();
                	break; 
                case ADVERTISED:
                    Log.i(TAG, "Host channel state is advertised.");
                case CONNECTED:
                    Log.i(TAG, "Host channel state is connected.");
                	// as soon as the channel is advertised, join it
                	if (mBartsyApplication.useGetChannelState() == AllJoynService.UseChannelState.IDLE) {
                		// Join the service as a user 
                        Log.i(TAG, "Use channel state is IDLE.");
                		
                		String host_name = mBartsyApplication.hostGetChannelName();
                		
	                	int lastDot = host_name.lastIndexOf('.');
	                	if (lastDot < 0) {
	                		appendStatus("Found channel with wrong name: " + host_name);
	                		break;
	                	}
                		String channel = host_name.substring(lastDot + 1);
                        Log.i(TAG, "Joining use channel: " + channel);

		                mBartsyApplication.useSetChannelName(channel);
		                mBartsyApplication.useJoinChannel();
                	}
                	break;
                }
                break;
            case HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT: 
                Log.i(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_USE_CHANNEL_STATE_CHANGED_EVENT");
                updateChannelState();
                break;
            case HANDLE_HISTORY_CHANGED_EVENT: {
                Log.i(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_HISTORY_CHANGED_EVENT");

                String message = mBartsyApplication.getLastMessage();
                
                // The history could be empty because this event is sent even on a channel init
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
            case HANDLE_ALLJOYN_ERROR_EVENT:
            {
                Log.i(TAG, "BartsyActivity.mhandler.handleMessage(): HANDLE_ALLJOYN_ERROR_EVENT");
                alljoynError();
            }
            break;                
            default:
                break;
            }
        }
    };

    private void alljoynError() {
    	if (mBartsyApplication.getErrorModule() == BartsyApplication.Module.GENERAL ||
    		mBartsyApplication.getErrorModule() == BartsyApplication.Module.USE) {
    		appendStatus("AllJoyn ERROR!!!!!!");
    		//showDialog(DIALOG_ALLJOYN_ERROR_ID);
    	}
    }
      
        
    public BartsyCommand parseMessage(String readMessage) {
    	
	    appendStatus("Message received: " + readMessage);
	
	    // parse the command
	    BartsyCommand command = null;
	    ByteArrayInputStream stream = new ByteArrayInputStream(readMessage.getBytes());
	    CommandParser commandParser = new CommandParser();
	
	    try {
	    		command = commandParser.parse(stream);
	    } catch (XmlPullParserException e) {
			// Auto-generated catch block
			e.printStackTrace();
	        appendStatus("Invalid command format received" );
	        return null;
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
	        appendStatus("Parser IO exception" );
	        return null;
		} finally {
			// Makes sure that the InputStream is closed after the app is finished using it.
			if (stream != null) {
	            try {
					stream.close();
				} catch (IOException e) {
					// Auto-generated catch block
					e.printStackTrace();
	                appendStatus("Stream close IO exception" );
	                return null;
				}
	        }
	    }
            
        // check to make sure there was a 
        if (command == null) {
            appendStatus("Parser succeeded but command is null" );
        	return null;
        }

        // Return successfully processed command
        return command;
    }

    
    void processCommand (BartsyCommand command) {
        if(command.opcode.equalsIgnoreCase("order")) {
            appendStatus("Opcode: " + command.opcode + "" );
        	processCommandOrder(command);
        } else if (command.opcode.equalsIgnoreCase("order_status_changed")) {
            if (processRemoteOrderStatusChanged(command))
            	// An error occurred - for now log it
            	appendStatus("ERROR PROCESSING ORDER STATUS CHANGED COMMAND");
        } else if (command.opcode.equalsIgnoreCase("profile")) {
            processProfile(command);
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
    	
    	Drink drink = ((DrinkDialogFragment)dialog).drink;
    	
    	
    	appendStatus("Placing order for: " + drink.title);
    	
    	// Send user profile to make sure we're on the list of people present
    	sendProfileCommand();
    	
    	// Send order to server
        mBartsyApplication.newLocalUserMessage(
        			"<command><opcode>order</opcode>" +
        			"<argument>" + mOrderIDs + "</argument>"+					// client order ID
        			"<argument>" + mOrderIDs + "</argument>"+					// server order ID        			
        			"<argument>" + drink.title + "</argument>" +
            		"<argument>" + drink.description + "</argument>" +
            		"<argument>" + drink.price + "</argument>" +
            		"<argument>" + drink.image_resource + "</argument>" +
        			"<argument>" + mProfile.userID+ "</argument>" +				// Each order contains the profile of the sender (and later the profile of the person that should pick it up)
        			"</command>"
            		);
    	appendStatus("Placed drink order");
    	
    	BarOrder barOrder = new BarOrder();
    	barOrder.initialize(mOrderIDs,							// arg(0) - Client order ID
    					mOrderIDs,								// arg(1) - Server order ID - use client ID for now
    					drink.title,							// arg(2) - Title
    					drink.description,						// arg(3) - Description
    					drink.price,							// arg(4) - Price
    					Integer.toString(drink.image_resource),	// arg(5) - Image resource for the order
    					mProfile);								// arg(6) - Each order contains the profile of the sender (and later the profile of the person that should pick it up)
    	mOrdersFragment.addOrder(barOrder);
    	
    	// Increment the local order count
    	mOrderIDs++;
    }

    
    int mOrderIDs = 0 ;
    int mSessionID = 0;
    
    void processCommandOrder(BartsyCommand command) { 
    	BarOrder barOrder = new BarOrder();

    	appendStatus("Processing command for order:" + command.arguments.get(0));

    	// Find the person who placed the order in the list of people in this bar. If not found, don't accept the order
    	Profile person = null;
    	for (Profile p : mPeopleFragment.mPeople) {
    		if (p.userID.equalsIgnoreCase(command.arguments.get(6))) {
    			// User found
    			person = p;
    			break;
    		} 
    	}
    	if (person == null) {
        	appendStatus("Error processing command. Profile placing order is missing from the list");
    		return;
    	}
    	
    	barOrder.initialize(Integer.parseInt(command.arguments.get(0)),		// client order ID
    					mSessionID++,										// server order ID
    					command.arguments.get(2),							// Title
    					command.arguments.get(3),							// Description
    					command.arguments.get(4),							// Price
    					command.arguments.get(5),							// Image resource
    					person);											// Order sender ID
    	mOrdersFragment.addOrder(barOrder);
    }
    
    
    
    /* 
     * 
     * TODO - Send/receive order status changed command
     * 
     */
    
    
    public void sendOrderStatusChanged(BarOrder order) {
    	// Expects the order status and the server ID to be already set on this end
    	appendStatus("Sending order response for order: " + order.serverID);
    	
        mBartsyApplication.newLocalUserMessage(
        			"<command><opcode>order_status_changed</opcode>" +
        			"<argument>" + order.status + "</argument>" +		// arg(0) - status is already updated on this end
            		"<argument>" + order.serverID + "</argument>" +	// arg(1)
        			"<argument>" + order.clientID + "</argument>" +	// arg(2)
        			"</command>"
            		);
    }

    
    public Boolean processRemoteOrderStatusChanged(BartsyCommand command) {
    	// Return false if everything went well, true if we need to perform recovery
    	
    	appendStatus("Received new remote order status: " + command.arguments.get(1));

    	int remote_status = Integer.parseInt(command.arguments.get(0));
		int server_id = Integer.parseInt(command.arguments.get(1));
		int client_id = Integer.parseInt(command.arguments.get(2));

    	// Make sure the order exists only once on this side and some other conditions are met. 
    	BarOrder localOrder = null;
    	int order_index = -1, i =0;
    	for (BarOrder order : mOrdersFragment.mOrders) {
    		appendStatus("Looking at order " + order.clientID + " in position " + i);
    		if (order.clientID == client_id) {
            	appendStatus("ORDER FOUND at position " + i);
    			localOrder = mOrdersFragment.mOrders.get(i);
    			order_index = i;
    		}
    		i++;
    	};
    	
    	if ( order_index == -1) {
        	appendStatus("ERROR - ORDER MISMATCH");
    		return true;
    	}
    	if (remote_status != BarOrder.ORDER_STATUS_IN_PROGRESS && localOrder.serverID != server_id)
    	{
        	appendStatus("ERROR - ORDER ID MISMATCH");
    		return true;
    	}
    	
    	// Update the status of the local order based on that of the remote order and return on error
    	switch (remote_status) {
    	case BarOrder.ORDER_STATUS_IN_PROGRESS:
    		// The order has been accepted remotely. Set the server_id on this order and update status and view
    		if (localOrder.status != BarOrder.ORDER_STATUS_NEW)
    			return true;
    		localOrder.serverID = server_id;
    		localOrder.nextPositiveState();
    		localOrder.updateView();
    		break;
    	case BarOrder.ORDER_STATUS_READY:
    		// Remote order ready. Notify client with a notification and update status/view
    		if (localOrder.status != BarOrder.ORDER_STATUS_IN_PROGRESS)
    			return true;
    		localOrder.nextPositiveState();
    		localOrder.updateView();
    		this.createNotification("Your " + localOrder.title + " order is ready", 
    				"Please go to the Bartsy Point to pick it up");
    		break;
    	case BarOrder.ORDER_STATUS_COMPLETE:
    		// Remote order ready. Notify client with a notification and update status/view
    		if (localOrder.status != BarOrder.ORDER_STATUS_READY)
    			return true;
    		localOrder.nextPositiveState();
    		
			// Trash the order for now (later save it to log of past orders)
			mOrdersFragment.removeOrder(localOrder);
			break;
     	}
    	return false;
    }
     

    
    /* 
     * 
     * TODO - User interaction commands
     * 
     */
    

    @Override
    public void onUserDialogPositiveClick(DialogFragment dialog) {
        // User touched the dialog's positive button
    	
    	Person user = ((PeopleDialogFragment)dialog).mUser;
    	
    	
    	appendStatus("Sending drink to: " + user.getNickname());
    	
        mBartsyApplication.newLocalUserMessage(
        			"<command><opcode>message</opcode>" +
        			"<argument>" + user.getNickname() + "</argument>" +
            		"<argument>" + "hi buddy" + "</argument>" +
        			"</command>"
            		);
    	appendStatus("Placed drink order");
    }

     
    @Override
    public void onUserDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's positive button
    	
    	Person user = ((PeopleDialogFragment)dialog).mUser;
    	
    	
    	appendStatus("Sending message to: " + user.getNickname());
    	
        mBartsyApplication.newLocalUserMessage(
        			"<command><opcode>message</opcode>" +
        			"<argument>" + user.getNickname() + "</argument>" +
            		"<argument>" + "hi buddy" + "</argument>" +
        			"</command>"
            		);
    	appendStatus("Sent message");
    }

     
    /* 
     * 
     * TODO - Profile commands
     * 
     */
    
    Profile mProfile;
//    ArrayList<User> mPeople = new ArrayList<User>();
    
    String mAccountName;
    String mUserDescription;
    
    void loadUserProfile () {
	    SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.config_shared_preferences_name), Context.MODE_PRIVATE);
		mProfile = null;

	    appendStatus("Loading user profile from " + getFilesDir()+File.separator + getResources().getString(R.string.config_user_profile_picture));
	    
	    Bitmap image = null;
		try {
			image = BitmapFactory.decodeFile(getFilesDir()+File.separator + getResources().getString(R.string.config_user_profile_picture));
		} catch (Exception e) {
			e.printStackTrace();
			appendStatus("Could not load profile image");
			return;
		}
	    
		appendStatus("Profile image found, creating profile...");

		
	    mProfile = new Profile(
	    		sharedPref.getString(getResources().getString(R.string.config_user_account_name), ""), 
	    		sharedPref.getString(getResources().getString(R.string.config_user_name), ""),
	    	    sharedPref.getString(getResources().getString(R.string.config_user_location), ""), 
	    		sharedPref.getString(getResources().getString(R.string.config_user_info), ""),
	    		sharedPref.getString(getResources().getString(R.string.config_user_description), ""),
	    		image);
    }    
   	
    void sendProfileCommand() {
    	
		appendStatus("Sending profile command...");

    	
    	// Skip if there is no profile
    	if (mProfile == null || mProfile.image == null) {
    		appendStatus("Profile not found, skipping commmand...");
    		return;
    	}
    	
    	// Encode image into a compressed JPEG stream
    	Bitmap bm = mProfile.image;
    	ByteArrayOutputStream baos = new ByteArrayOutputStream();  
    	bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object   
    	byte[] b = baos.toByteArray();
        //String encodedImage = Base64.encode(b, Base64.DEFAULT);
        String image_string = Base64.encodeToString(b, Base64.DEFAULT);
    	
    	
    	/* We could be just converting the file into a string instead... 
    	String image_string = "";
		try {
			image_string = readFileAsString(getResources().getString(R.string.config_user_profile_picture));
		} catch (NotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	*/
    	
    	appendStatus("Sending user profile for: " + mProfile.username);
        mBartsyApplication.newLocalUserMessage(
    			"<command><opcode>profile</opcode>" +
    			"<argument>" + mProfile.userID + "</argument>" +		// arg(0) - userID
    			"<argument>" + mProfile.username + "</argument>" +		// arg(1) - username
    			"<argument>" + mProfile.location + "</argument>" +		// arg(2) - location
    			"<argument>" + mProfile.info + "</argument>" +			// arg(3) - info
    			"<argument>" + mProfile.description + "</argument>" +	// arg(4) - description
    			"<argument>" + image_string + "</argument>" +			// arg(5) - image
    			"</command>"
        		);

    }
 
    void processProfile(BartsyCommand command) {
    	
    	appendStatus("Process command: " + command.opcode);
    	
    	// Decode the user image and create a new incoming profile
    	byte[] decodedString = Base64.decode(command.arguments.get(5), Base64.DEFAULT);
    	Bitmap image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); 
    	Profile profile = new Profile( 
				command.arguments.get(0),					// userid
				command.arguments.get(1),					// username
				command.arguments.get(2),					// location
				command.arguments.get(3),					// info
				command.arguments.get(4),					// description
				image										// image
    			);
    	
    	// Add the person to the list of people in the bar (this method doesn't add duplicates)
    	mPeopleFragment.addPerson(profile);
    }
    

	public static String readFileAsString(String filePath) throws java.io.IOException
	{
	    BufferedReader reader = new BufferedReader(new FileReader(filePath));
	    String line, results = "";
	    while( ( line = reader.readLine() ) != null)
	        results += line;
	    reader.close();
	    return results;
	}

    
}
