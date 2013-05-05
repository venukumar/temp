/*
 * Copyright 2011, Qualcomm Innovation Center, Inc.
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package wifi;


import android.os.Handler;
import android.os.Message;
import android.os.Bundle;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import android.util.Log;

import java.util.List;

import com.kellislabs.bartsy.AppObservable;
import com.kellislabs.bartsy.AppObserver;
import com.kellislabs.bartsy.BartsyApplication;
import com.kellislabs.bartsy.ConnectivityService;
import com.kellislabs.bartsy.InitActivity;
import com.kellislabs.bartsy.R;
import com.kellislabs.bartsy.VenueActivity;
import com.kellislabs.bartsy.BartsyApplication.Module;
import com.kellislabs.bartsy.ConnectivityService.UseChannelState;
import com.kellislabs.bartsy.R.id;
import com.kellislabs.bartsy.R.layout;


public class AllJoynUseActivity extends Activity implements AppObserver {
    private static final String TAG = "Bartsy";
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.use);
                
        mHistoryList = new ArrayAdapter<String>(this, android.R.layout.test_list_item);
        ListView hlv = (ListView) findViewById(R.id.useHistoryList);
        hlv.setAdapter(mHistoryList);
        
        EditText messageBox = (EditText)findViewById(R.id.useMessage);
        messageBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
			public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                	String message = view.getText().toString();
                    Log.i(TAG, "useMessage.onEditorAction(): got message " + message + ")");
    	            mBartsyApplication.newLocalUserMessage(message);
    	            view.setText("");
                }
                return true;
            }
        });
                
        mJoinButton = (Button)findViewById(R.id.useJoin);
        mJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View v) {
                showDialog(DIALOG_JOIN_ID);
            }
        });

        mLeaveButton = (Button)findViewById(R.id.useLeave);
        mLeaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View v) {
                showDialog(DIALOG_LEAVE_ID);
            }
        });
        
        ((Button)findViewById(R.id.useBartsy))
        	.setOnClickListener(new View.OnClickListener() {
        		@Override
				public void onClick(View v) {
        	        Intent intent = new Intent(getApplication(), VenueActivity.class);
        	        startActivity(intent);
        		}
        });

        ((Button)findViewById(R.id.useInit))
    	.setOnClickListener(new View.OnClickListener() {
    		@Override
			public void onClick(View v) {
    	        Intent intent = new Intent(getApplication(), InitActivity.class);
    	        startActivity(intent);
    		}
    });

        
        mChannelName = (TextView)findViewById(R.id.useChannelName);
        mChannelStatus = (TextView)findViewById(R.id.useChannelStatus);
        
        /*
         * Keep a pointer to the Android Appliation class around.  We use this
         * as the Model for our MVC-based application.    Whenever we are started
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
        updateHistory();
        
        /*
         * Now that we're all ready to go, we are ready to accept notifications
         * from other components.
         */
        mBartsyApplication.addObserver(this);

    }
    
	@Override
	public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        mBartsyApplication = (BartsyApplication)getApplication();
        mBartsyApplication.deleteObserver(this);
    	super.onDestroy();
 	}
    
    public static final int DIALOG_JOIN_ID = 0;
    public static final int DIALOG_LEAVE_ID = 1;
    public static final int DIALOG_ALLJOYN_ERROR_ID = 2;

    @Override
	protected Dialog onCreateDialog(int id) {
    	Log.i(TAG, "onCreateDialog()");
        Dialog result = null;
        switch(id) {
        case DIALOG_JOIN_ID:
	        { 
	        	AllJoynDialogBuilder builder = new AllJoynDialogBuilder();
	        	result = builder.createUseJoinDialog(this, mBartsyApplication);
	        }        	
        	break;
        case DIALOG_LEAVE_ID:
	        { 
	        	AllJoynDialogBuilder builder = new AllJoynDialogBuilder();
	        	result = builder.createUseLeaveDialog(this, mBartsyApplication);
	        }
	        break;
        case DIALOG_ALLJOYN_ERROR_ID:
	        { 
	        	AllJoynDialogBuilder builder = new AllJoynDialogBuilder();
	        	result = builder.createAllJoynErrorDialog(this, mBartsyApplication);
	        }
	        break;	        
        }
        return result;
    }
    
    @Override
	public synchronized void update(AppObservable o, Object arg) {
        Log.i(TAG, "update(" + arg + ")");
        String qualifier = (String)arg;
        
        if (qualifier.equals(BartsyApplication.APPLICATION_QUIT_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_APPLICATION_QUIT_EVENT);
            mHandler.sendMessage(message);
        }
        
        if (qualifier.equals(BartsyApplication.HISTORY_CHANGED_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_HISTORY_CHANGED_EVENT);
            mHandler.sendMessage(message);
        }
        
        if (qualifier.equals(BartsyApplication.USE_CHANNEL_STATE_CHANGED_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_CHANNEL_STATE_CHANGED_EVENT);
            mHandler.sendMessage(message);
        }
        
        if (qualifier.equals(BartsyApplication.ALLJOYN_ERROR_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_ALLJOYN_ERROR_EVENT);
            mHandler.sendMessage(message);
        }
    }
    
    private void updateHistory() {
        Log.i(TAG, "updateHistory()");
	    mHistoryList.clear();
	    List<String> messages = mBartsyApplication.getHistory();
        for (String message : messages) {
            mHistoryList.add(message);
        }
	    mHistoryList.notifyDataSetChanged();
    }
    
    private void updateChannelState() {
        Log.i(TAG, "updateChannelState()");
    	ConnectivityService.UseChannelState channelState = mBartsyApplication.useGetChannelState();
    	String name = mBartsyApplication.useGetChannelName();
    	if (name == null) {
    		name = "Not set";
    	}
        mChannelName.setText(name);
        
        switch (channelState) {
        case IDLE:
            mChannelStatus.setText("Idle");
            mJoinButton.setEnabled(true);
            mLeaveButton.setEnabled(false);
            break;
        case JOINED:
            mChannelStatus.setText("Joined");
            mJoinButton.setEnabled(false);
            mLeaveButton.setEnabled(true);
            break;	
        }
    }
    
    /**
     * An AllJoyn error has happened.  Since this activity pops up first we
     * handle the general errors.  We also handle our own errors.
     */
    private void alljoynError() {
    	if (mBartsyApplication.getErrorModule() == BartsyApplication.Module.GENERAL ||
    		mBartsyApplication.getErrorModule() == BartsyApplication.Module.USE) {
    		showDialog(DIALOG_ALLJOYN_ERROR_ID);
    	}
    }
    
    private static final int HANDLE_APPLICATION_QUIT_EVENT = 0;
    private static final int HANDLE_HISTORY_CHANGED_EVENT = 1;
    private static final int HANDLE_CHANNEL_STATE_CHANGED_EVENT = 2;
    private static final int HANDLE_ALLJOYN_ERROR_EVENT = 3;
    
    private Handler mHandler = new Handler() {
        @Override
		public void handleMessage(Message msg) {
            switch (msg.what) {
            case HANDLE_APPLICATION_QUIT_EVENT:
	            {
	                Log.i(TAG, "mHandler.handleMessage(): HANDLE_APPLICATION_QUIT_EVENT");
	                finish();
	            }
	            break; 
            case HANDLE_HISTORY_CHANGED_EVENT:
                {
                    Log.i(TAG, "mHandler.handleMessage(): HANDLE_HISTORY_CHANGED_EVENT");
                    updateHistory();
                    break;
                }
            case HANDLE_CHANNEL_STATE_CHANGED_EVENT:
	            {
	                Log.i(TAG, "mHandler.handleMessage(): HANDLE_CHANNEL_STATE_CHANGED_EVENT");
	                updateChannelState();
	                break;
	            }
            case HANDLE_ALLJOYN_ERROR_EVENT:
	            {
	                Log.i(TAG, "mHandler.handleMessage(): HANDLE_ALLJOYN_ERROR_EVENT");
	                alljoynError();
	                break;
	            }
            default:
                break;
            }
        }
    };
    
    private BartsyApplication mBartsyApplication = null;
    
    private ArrayAdapter<String> mHistoryList;
    
    private Button mJoinButton;
    private Button mLeaveButton;
    
    private TextView mChannelName;
      
    private TextView mChannelStatus;
}
