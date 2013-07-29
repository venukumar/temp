
package com.vendsy.bartsy;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Color;
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
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.vendsy.bartsy.model.AppObservable;
import com.vendsy.bartsy.model.MessageData;
import com.vendsy.bartsy.utils.Utilities;
import com.vendsy.bartsy.utils.WebServices;
import com.vendsy.bartsy.view.AppObserver;

public class MessagesActivity extends SherlockActivity implements AppObserver {

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
	private ResponsiveScrollView scrollView;
	
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
				if(!messageTextBox.getText().toString().trim().equals("")){
					sendMessageSysCall(messageTextBox.getText().toString());
				}
				
			}
		});
		
		scrollView = (ResponsiveScrollView)findViewById(R.id.scrollView);
		
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
		// Add message to messages list view
		addMessageInList(messageData);
		// Add progress message
		final TextView errorMessageView = getErrorMessageView("Sending..");
		messagesList.addView(errorMessageView);
		
		// Clear text box
		messageTextBox.setText("");
		// Require to scroll down after adding new message
		scrollView.post(new Runnable() {
				    @Override
				    public void run() {
				        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
				    }
		});
		
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
								messagesList.removeView(errorMessageView);
								
							}// Error response
							else if(json.has("errorMessage")){
								Toast.makeText(getApplicationContext(), json.getString("errorMessage"), Toast.LENGTH_LONG).show();
								errorMessageView.setText("Message failed");
							}
						}catch (JSONException e) {
							errorMessageView.setText("Message failed");
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
					Collections.sort(messages,message);
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
		// Make sure that list is empty and reset to default values
		messagesLayout.removeAllViews();
		messagesList = null;
		isSelfProfile = false;
		
		for(MessageData message: messages){
			addMessageInList(message);
		}
		// Require to scroll down after adding new message
		scrollView.post(new Runnable() {
			@Override
			public void run() {
			     scrollView.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
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
				((TextView)view.findViewById(R.id.messages_list_date)).setText(Utilities.getFriendlyDate(message.getCreatedDate(), "d MMM yyyy HH:mm:ss 'GMT'"));
				
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
				((TextView)view.findViewById(R.id.messages_list_date)).setText(Utilities.getFriendlyDate(message.getCreatedDate(), "d MMM yyyy HH:mm:ss 'GMT'"));
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
		
		// Require to scroll down after adding new message
		scrollView.post(new Runnable() {
		    @Override
		    public void run() {
		        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
		    }
		});
		
	}
	
	private TextView getErrorMessageView(String message) {
		
		TextView messageTextView = new TextView(this);
		messageTextView.setTextColor(Color.RED);
		// Set layout params to the text view
		messageTextView.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		messageTextView.setText(message);
		
		messageTextView.setGravity(Gravity.RIGHT);
		
		return messageTextView;
		
	}
	
	public synchronized void update(AppObservable o, Object arg) {
		Log.v(TAG, "update(" + arg + ")");
		
		final String qualifier = (String) arg;
					
		if (qualifier.equals(BartsyApplication.NEW_CHAT_MESSAGE_RECEIVED)) {
			// Load all the messages from the server and update the messages view
			loadMessagesFromServer();
			mApp.receivedMessage=null;
		}
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.v(TAG, "onDestroy()");

		// Only stop listening to messages from the application when we're killed (keep
		// listening while in the background with no active view)
		mApp.deleteObserver(this);

	}

		
}
