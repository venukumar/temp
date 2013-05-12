/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kellislabs.bartsy.service;

import static com.kellislabs.bartsy.utils.Utilities.SENDER_ID;
import static com.kellislabs.bartsy.utils.Utilities.displayMessage;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.kellislabs.bartsy.MainActivity;
import com.kellislabs.bartsy.R;
import com.kellislabs.bartsy.R.drawable;
import com.kellislabs.bartsy.R.string;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {
	public static int count = 0;
	public static final String REG_ID = "RegId";

    @SuppressWarnings("hiding")
    
    private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
    	
        super(SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
    	System.out.println("senderid :::"+SENDER_ID);
    	System.out.println("in on registered method");
        Log.i(TAG, "Device registered: regId = " + registrationId);
       
        SharedPreferences settings = getSharedPreferences(REG_ID, 0);
//        String uname = settings.getString("user", "").toString();
        SharedPreferences.Editor editor = settings
				.edit();
		editor.putString("RegId", registrationId);
		
		editor.commit();

        
    //    DukesDonorUtil.getInstance().setDeviceId(registrationId);
        displayMessage(context, getString(R.string.gcm_registered));
//        ServerUtilities.register(context, registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        displayMessage(context, getString(R.string.gcm_unregistered));
        if (GCMRegistrar.isRegisteredOnServer( getApplicationContext())) {
//            ServerUtilities.unregister(context, registrationId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            Log.i(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
//        String message = getString(R.string.gcm_message);
        String message = (String) intent.getExtras().get("message");
//        String count=(String) intent.getExtras().get("badgeCount");
        Uri notification = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		Ringtone ringtone = RingtoneManager.getRingtone(
				context, notification);
		if (ringtone != null) {
			ringtone.play();
		}
        
        if(message==null){
        	message = "";
        }
        
        displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }

//    @Override
//    protected void onDeletedMessages(Context context, int total) {
//        Log.i(TAG, "Received deleted messages notification");
//        String message = getString(R.string.gcm_deleted, total);
//        displayMessage(context, message);
//        // notifies user
//        generateNotification(context, message);
//    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
        displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        displayMessage(context, getString(R.string.gcm_recoverable_error,
                errorId));
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     * @param count 
     */
    private static void generateNotification(Context context, String message) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        String title = context.getString(R.string.app_name);
        count++;
        if(count!=0){
        	title+="("+count+")";
        }
       
        Intent notificationIntent = new Intent(context, MainActivity.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.number = count;
        notificationManager.notify(0, notification);
        
        notification.defaults = Notification.DEFAULT_ALL;
//        int count1 = Integer.parseInt(count);
		
        
//        // Play default notification sound
//        notification.defaults |= Notification.DEFAULT_SOUND;
//        
//        // Vibrate if vibrate is enabled
//        notification.defaults |= Notification.DEFAULT_VIBRATE;
//        notificationManager.notify(0, notification);   
    }

}
