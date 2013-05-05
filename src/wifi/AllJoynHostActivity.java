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


import com.kellislabs.bartsy.AppObservable;
import com.kellislabs.bartsy.AppObserver;
import com.kellislabs.bartsy.ConnectivityService;
import com.kellislabs.bartsy.BartsyApplication;
import com.kellislabs.bartsy.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.app.Activity;
import android.app.Dialog;

import android.view.View;

import android.widget.Button;
import android.widget.TextView;

import android.util.Log;
import com.kellislabs.bartsy.*;
import com.kellislabs.bartsy.ConnectivityService.HostChannelState;
import com.kellislabs.bartsy.BartsyApplication.Module;
import com.kellislabs.bartsy.R.id;
import com.kellislabs.bartsy.R.layout;

public class AllJoynHostActivity extends Activity implements AppObserver {
    private static final String TAG = "Bartsy";
     
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.host);
              
        mChannelName = (TextView)findViewById(R.id.hostChannelName);
        mChannelName.setText("");
        
        mChannelStatus = (TextView)findViewById(R.id.hostChannelStatus);
        mChannelStatus.setText("Idle");
        
        mSetNameButton = (Button)findViewById(R.id.hostSetName);
        mSetNameButton.setEnabled(true);
        mSetNameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_SET_NAME_ID);
        	}
        });

        mStartButton = (Button)findViewById(R.id.hostStart);
        mStartButton.setEnabled(false);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_START_ID);
            }
        });
        
        mStopButton = (Button)findViewById(R.id.hostStop);
        mStopButton.setEnabled(false);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_STOP_ID);
            }
        });
        
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
        
        
        mQuitButton = (Button)findViewById(R.id.hostQuit);
        mQuitButton.setEnabled(true);
        mQuitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mBartsyApplication.quit();
            }
        });
    }
    
	public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        mBartsyApplication = (BartsyApplication)getApplication();
        mBartsyApplication.deleteObserver(this);
        super.onDestroy();
 	}
	
    private BartsyApplication mBartsyApplication = null;
    
    static final int DIALOG_SET_NAME_ID = 0;
    static final int DIALOG_START_ID = 1;
    static final int DIALOG_STOP_ID = 2;
    public static final int DIALOG_ALLJOYN_ERROR_ID = 3;

    protected Dialog onCreateDialog(int id) {
        Log.i(TAG, "onCreateDialog()");
        Dialog result = null;
        switch(id) {
        case DIALOG_SET_NAME_ID:
	        { 
	        	AllJoynDialogBuilder builder = new AllJoynDialogBuilder();
	        	result = builder.createHostNameDialog(this, mBartsyApplication);
	        }  
        	break;
        case DIALOG_START_ID:
	        { 
	        	AllJoynDialogBuilder builder = new AllJoynDialogBuilder();
	        	result = builder.createHostStartDialog(this, mBartsyApplication);
	        } 
            break;
        case DIALOG_STOP_ID:
	        { 
	        	AllJoynDialogBuilder builder = new AllJoynDialogBuilder();
	        	result = builder.createHostStopDialog(this, mBartsyApplication);
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
    
    public synchronized void update(AppObservable o, Object arg) {
        Log.i(TAG, "update(" + arg + ")");
        String qualifier = (String)arg;
        
        if (qualifier.equals(BartsyApplication.APPLICATION_QUIT_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_APPLICATION_QUIT_EVENT);
            mHandler.sendMessage(message);
        }
        
        if (qualifier.equals(BartsyApplication.HOST_CHANNEL_STATE_CHANGED_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_CHANNEL_STATE_CHANGED_EVENT);
            mHandler.sendMessage(message);
        }
        
        if (qualifier.equals(BartsyApplication.ALLJOYN_ERROR_EVENT)) {
            Message message = mHandler.obtainMessage(HANDLE_ALLJOYN_ERROR_EVENT);
            mHandler.sendMessage(message);
        }
    }
    
    private void updateChannelState() {
    	ConnectivityService.HostChannelState channelState = mBartsyApplication.hostGetChannelState();
    	String name = mBartsyApplication.hostGetChannelName();
    	boolean haveName = true;
    	if (name == null) {
    		haveName = false;
    		name = "Not set";
    	}
        mChannelName.setText(name);
        switch (channelState) {
        case IDLE:
            mChannelStatus.setText("Idle");
            break;
        case NAMED:
            mChannelStatus.setText("Named");
            break;
        case BOUND:
            mChannelStatus.setText("Bound");
            break;
        case ADVERTISED:
            mChannelStatus.setText("Advertised");
            break;
        case CONNECTED:
            mChannelStatus.setText("Connected");
            break;
        default:
            mChannelStatus.setText("Unknown");
            break;
        }
        
        if (channelState == ConnectivityService.HostChannelState.IDLE) {
            mSetNameButton.setEnabled(true);
            if (haveName) {
            	mStartButton.setEnabled(true);
            } else {
                mStartButton.setEnabled(false);
            }
            mStopButton.setEnabled(false);
        } else {
            mSetNameButton.setEnabled(false);
            mStartButton.setEnabled(false);
            mStopButton.setEnabled(true);
        }
    }
    
    private TextView mChannelName;
    private TextView mChannelStatus;
    private Button mSetNameButton;
    private Button mStartButton;
    private Button mStopButton;
    private Button mQuitButton;
    
    private void alljoynError() {
    	if (mBartsyApplication.getErrorModule() == BartsyApplication.Module.GENERAL ||
    		mBartsyApplication.getErrorModule() == BartsyApplication.Module.USE) {
    		showDialog(DIALOG_ALLJOYN_ERROR_ID);
    	}
    }
    
    private static final int HANDLE_APPLICATION_QUIT_EVENT = 0;
    private static final int HANDLE_CHANNEL_STATE_CHANGED_EVENT = 1;
    private static final int HANDLE_ALLJOYN_ERROR_EVENT = 2;
    
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
	            case HANDLE_APPLICATION_QUIT_EVENT:
	            {
	                Log.i(TAG, "mHandler.handleMessage(): HANDLE_APPLICATION_QUIT_EVENT");
	                finish();
	            }
	            break; 
            case HANDLE_CHANNEL_STATE_CHANGED_EVENT:
	            {
	                Log.i(TAG, "mHandler.handleMessage(): HANDLE_CHANNEL_STATE_CHANGED_EVENT");
	                updateChannelState();
	            }
                break;
            case HANDLE_ALLJOYN_ERROR_EVENT:
            {
                Log.i(TAG, "mHandler.handleMessage(): HANDLE_ALLJOYN_ERROR_EVENT");
                alljoynError();
            }
            break;                
            default:
                break;
            }
        }
    };
    
}