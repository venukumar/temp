
package com.vendsy.bartsy;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vendsy.bartsy.model.AppObservable;
import com.vendsy.bartsy.model.MessageData;
import com.vendsy.bartsy.model.Notification;
import com.vendsy.bartsy.utils.WebServices;
import com.vendsy.bartsy.view.AppObserver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class MessagesActivity extends Activity implements AppObserver {

	private Handler handler = new Handler();
	BartsyApplication mApp = null;
	// Progress dialog
//	private ProgressDialog progressDialog;

	Activity activity = this;
	private LinearLayout messagesLayout;
	private boolean isSelfProfile = false;
	
	private ArrayList<MessageData> messages = new ArrayList<MessageData>();
	private LinearLayout messagesList;
	private EditText messageTextBox;
	
	private static final String TAG = "MessagesActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set the view
		setContentView(R.layout.messages_main);

		// Set up the pointer to the main application
		mApp = (BartsyApplication) getApplication();
		
		/*
		 * Now that we're all ready to go, we are ready to accept notifications
		 * from other components.
		 */
		mApp.addObserver(this);

		messagesLayout = (LinearLayout) findViewById(R.id.messagesLayout);
		
		messageTextBox = (EditText) findViewById(R.id.messageTextBox);
		Button sendButton = (Button) findViewById(R.id.sendButton);
		// Set click listener to the send button
		sendButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendMessageSysCall(messageTextBox.getText().toString());
				
			}
		});

		// Load messages in the background
		loadMessagesFromServer();
	}

	/**
	 * Send message to other user
	 * 
	 * @param message
	 */
	protected void sendMessageSysCall(String message) {
		// Create message object and set required properties to post in sys call
		final MessageData messageData = new MessageData();
		messageData.setBody(message);
		messageData.setSenderId(mApp.mProfile.getBartsyId());
		messageData.setReceiverId(mApp.selectedUserProfile.getBartsyId());
		messageData.setVenueId(mApp.mActiveVenue.getId());
		// Background thread
		new Thread(){
			@Override
			public void run() {
				final String response = WebServices.postRequest(WebServices.URL_SEND_MESSAGE, messageData.getJSONData(), (BartsyApplication)getApplication());
				Log.d(TAG, "sendMessageSysCall() Responce: "+response);
				// Post handler to access UI 
				handler.post(new Runnable() {
					@Override
					public void run() {
						try {
							JSONObject json = new JSONObject(response);
							// Success response
							if(json.has("errorCode") && json.getInt("errorCode") ==0){
								addMessageInList(messageData);
								// Clear 
								messageTextBox.setText("");
							}// Error response
							else if(json.has("errorMessage")){
								Toast.makeText(getApplicationContext(), json.getString("errorMessage"), Toast.LENGTH_LONG).show();
							}
						}catch (JSONException e) {
						}
					}
				});
			}
		}.start();
	}
	
	/**
	 * Get list of messages from the server
	 * 
	 * @param message
	 */
	protected void loadMessagesFromServer() {
		
		final JSONObject json = new JSONObject();
		try {
			json.put("senderId", mApp.mProfile.getBartsyId());
			json.put("receiverId", mApp.selectedUserProfile.getBartsyId());
			json.put("venueId", mApp.mActiveVenue.getId());
			
			// Background thread
			new Thread(){
				@Override
				public void run() {
					final String response = WebServices.postRequest(WebServices.URL_GET_MESSAGES, json, (BartsyApplication)getApplication());
					Log.d(TAG, "getMessagesSysCall() Response: "+response);
					
					// Post handler to access UI 
					handler.post(new Runnable() {
						@Override
						public void run() {
							processMessagesResponse(response);
						}
					});
				}
			}.start();
		} catch (JSONException e) {
		}
	}
	
	/**
	 * Parse getMessages Sys call response and display in the UI
	 * 
	 * @param response
	 */
	private void processMessagesResponse(String response) {
		messages = new ArrayList<MessageData>();
		try {
			JSONObject json = new JSONObject(response);
			// Success response
			if(json.has("messages") && json.has("errorCode") && json.getInt("errorCode") ==0){
				JSONArray jsonArray = json.getJSONArray("messages");
				for (int i=0; i<jsonArray.length() ; i++) {
					JSONObject jsonObj = jsonArray.getJSONObject(i);
					MessageData message = new MessageData(jsonObj);
					messages.add(message);
				}
				
				// Update list view with messages data
				updateMessagesView();
			}
			// Error response
			else if(json.has("errorMessage")){
				Toast.makeText(getApplicationContext(), json.getString("errorMessage"), Toast.LENGTH_LONG).show();
			}
		} catch (JSONException e) {
		}
	}
	
	private void updateMessagesView() {
		// Make sure that list is empty
		messagesLayout.removeAllViews();
		
		for(MessageData message: messages){
			addMessageInList(message);
		}
	}

	private void addMessageInList(MessageData message) {
		View view = null;
		// Self Profile
		if(mApp.mProfile!=null && message.getSenderId().equals(mApp.mProfile.getBartsyId())){
			
			if(isSelfProfile && messagesList!=null){
				addMessageView(message);
			}else{
				isSelfProfile = true;
				// Set self profile image
				view = getLayoutInflater().inflate(R.layout.message_self_view, null);
				((ImageView)view.findViewById(R.id.view_user_list_image_resource)).setImageBitmap(mApp.mProfile.getImage());
				messagesList = ((LinearLayout)view.findViewById(R.id.messages_list));
				
				LayoutParams params = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				params.gravity = Gravity.RIGHT;
				view.setLayoutParams(params);
				
				addMessageView(message);
				
			}
		}
		// Recipient Profile
		else if(mApp.selectedUserProfile!=null){
			
			if((!isSelfProfile) && messagesList!=null){
				addMessageView(message);
			}else{
				isSelfProfile = false;
			
				// Set recipient profile image
				view = getLayoutInflater().inflate(R.layout.message_view, null);
				((ImageView)view.findViewById(R.id.view_user_list_image_resource)).setImageBitmap(mApp.selectedUserProfile.getImage());
				
				messagesList = ((LinearLayout)view.findViewById(R.id.messages_list));
				
				addMessageView(message);
			}
		}
		// Add message view with profile
		if(view!=null){
			messagesLayout.addView(view);
		}
		
	}

	private void addMessageView(MessageData message) {
		
		TextView messageTextView = new TextView(this);
		// Set layout params to the text view
		messageTextView.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		messageTextView.setText(message.getBody());
		
		// Set gravity right if it is self message
		if(isSelfProfile){
			messageTextView.setGravity(Gravity.RIGHT);
		}
		
		messagesList.addView(messageTextView);
		
	}
	
	public synchronized void update(AppObservable o, Object arg) {
		Log.v(TAG, "update(" + arg + ")");
		
		final String qualifier = (String) arg;
		handler.post(new Runnable() {
			@Override
			public void run() {
				
				if (qualifier.equals(BartsyApplication.NEW_CHAT_MESSAGE_RECEIVED)) {
					addMessageInList(mApp.receivedMessage);
					mApp.receivedMessage=null;
				}
			}
		});
		
	}

		
}
