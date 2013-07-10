
package com.vendsy.bartsy;

import com.vendsy.bartsy.model.MessageData;
import com.vendsy.bartsy.utils.WebServices;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class MessagesActivity extends Activity{

	private Handler handler = new Handler();
	BartsyApplication mApp = null;

	Activity activity = this;
	private LinearLayout messagesLayout;
	
	private static final String TAG = "MessagesActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Set the view
		setContentView(R.layout.messages_main);

		// Set up the pointer to the main application
		mApp = (BartsyApplication) getApplication();

		messagesLayout = (LinearLayout) findViewById(R.id.messagesLayout);
		
		final EditText messageTextBox = (EditText) findViewById(R.id.messageTextBox);
		Button sendButton = (Button) findViewById(R.id.sendButton);
		
		sendButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendMessageSysCall(messageTextBox.getText().toString());
			}
		});
		
//		getIntent().getExtras().getString("recipientId");

		// Load venues in the background
//		loadVenuesFromServer();
	}
	
	/**
	 * 
	 * 
	 * @param message
	 */
	protected void sendMessageSysCall(String message) {
		
		MessageData messageData = new MessageData();
		messageData.setBody(message);
		messageData.setSenderId(mApp.mProfile.getBartsyId());
		messageData.setReceiverId(mApp.mProfile.getBartsyId());
		
		String response = WebServices.postRequest(WebServices.URL_SEND_MESSAGES, messageData.getJSONData(), (BartsyApplication)getApplication());
		
		new Thread(){
			@Override
			public void run() {
				// Post handler to access UI 
				handler.post(new Runnable() {
					@Override
					public void run() {
						
					}
				});
			}
		}.start();
	}


//	/**
//	 * To load messages from the server
//	 * 
//	 * @param venueList
//	 */
//	protected void loadVenuesFromServer(final String receiverId) {
//		new Thread() {
//			public void run() {
//				String response = WebServices.getMessages(activity, mApp.mProfile.getBartsyId(), receiverId);
//				if (response != null) {
//					venues = getVenueListResponse(response);
//				}
//			}
//		}.start();
//
//	}
	
		
}
