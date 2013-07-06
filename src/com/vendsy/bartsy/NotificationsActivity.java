package com.vendsy.bartsy;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vendsy.bartsy.model.Notification;
import com.vendsy.bartsy.utils.Utilities;
import com.vendsy.bartsy.utils.WebServices;
/**
 * 
 * @author Seenu Malireddy
 *
 */
public class NotificationsActivity extends Activity{
	
	// Progress dialog
	private ProgressDialog progressDialog;
	private BartsyApplication mApp;

	private ArrayList<Notification> notifications = new ArrayList<Notification>();
	private LinearLayout notificationsListView;
	
	private Handler handler = new Handler();
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notifications_main);
		
		mApp = (BartsyApplication) getApplication();
		
		notificationsListView = (LinearLayout)findViewById(R.id.people_notifications);
		
		loadNotifications();
	}
	
	/**
	 * Call getNotifications Sys call and get the notifications data from the server
	 */
	private void loadNotifications(){
			
		// To display progress dialog
		progressDialog = Utilities.progressDialog(this, "Loading..");
		progressDialog.show();
		
		// Background thread
		new Thread() {

			public void run() {
				
				final String response = WebServices.getNotifications(mApp, mApp.mProfile.getBartsyId());
				// Post handler to access UI 
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						progressDialog.dismiss();
						
						// Notifications web service response handling
						if (response != null){
							processNotificationResponse(response);
						}
					}
				});
				

			};
		}.start();

	}
	/**
	 * Parse getNotification Sys call response and display in the UI
	 * 
	 * @param response
	 */
	private void processNotificationResponse(String response) {
		
		notifications.clear();
		
		try {
			JSONObject json = new JSONObject(response);
			// Success response
			if(json.has("notifications") && json.has("errorCode") && json.getInt("errorCode") ==0){
				JSONArray jsonArray = json.getJSONArray("notifications");
				for (int i=0; i<jsonArray.length() ; i++) {
					JSONObject jsonObj = jsonArray.getJSONObject(i);
					Notification notification = new Notification(jsonObj);
					notifications.add(notification);
				}
				
				updateNotificationsView();
			}
			// Error response
			else if(json.has("errorMessage")){
				Toast.makeText(getApplicationContext(), json.getString("errorMessage"), Toast.LENGTH_LONG).show();
			}
		} catch (JSONException e) {
		}
	}

	/**
	 * Update notifications in the view. For every type of notification, there are different type of view and adding to the list view
	 *  
	 */
	public void updateNotificationsView(){
		// Make sure that list view is empty
		notificationsListView.removeAllViews();
		
		for(Notification notification: notifications){
			// Add offer drink view and set the notification data 
			if(Notification.TYPE_OFFER_DRINK.equals(notification.getType())){
				addOfferDrinkView(notification);
			}
			// Add checkin or checkout view and set the notification data 
			else {
				addGenericNotificationView(notification);
			}
		}
	}
	
	/**
	 * Add different type notification(checkin/checkout/place order/order update) view to the main listview and set the notification values
	 * 
	 * @param notification
	 */
	public void addGenericNotificationView(Notification notification){
		// Inflate checkin view
		View view = getLayoutInflater().inflate(R.layout.notifications_item, null);
		
		// Set all notification information to the view
		
		((ImageView)view.findViewById(R.id.profileImage)).setImageBitmap(mApp.mProfile.getImage());
		((TextView)view.findViewById(R.id.messageText)).setText(notification.getMessage());
		((TextView)view.findViewById(R.id.dateText)).setText(notification.getCreatedTime());
		
		// If the notification is related to checkout then replace messagetype image with checkout image 
		if(Notification.TYPE_CHECKOUT.equals(notification.getType())){
			ImageView imageView = ((ImageView)view.findViewById(R.id.messageTypeImage));
			imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.checkout));
		}
		
		// Add view to the notification list view
		notificationsListView.addView(view);
	}
	
	/**
	 * Add Offer Drink view to the main listview and set the notification values
	 * 
	 * @param notification
	 */
	public void addOfferDrinkView(Notification notification){
		// Inflate checkin view
		View view = getLayoutInflater().inflate(R.layout.offer_drink_notifications_item, null);
		
		// Set all notification information to the view
		((ImageView)view.findViewById(R.id.profileImage)).setImageBitmap(mApp.mProfile.getImage());
		((TextView)view.findViewById(R.id.messageText)).setText(notification.getMessage());
		((TextView)view.findViewById(R.id.dateText)).setText(notification.getCreatedTime());
		
		// Add view to the notification list view
		notificationsListView.addView(view);
	}
	
	
}
