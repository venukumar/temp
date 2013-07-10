package com.vendsy.bartsy;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;
import com.vendsy.bartsy.model.Notification;
import com.vendsy.bartsy.utils.Utilities;
import com.vendsy.bartsy.utils.WebServices;
/**
 * 
 * @author Seenu Malireddy
 *
 */
public class NotificationsActivity extends SherlockActivity {
	
	// Progress dialog
	private ProgressDialog progressDialog;
	private BartsyApplication mApp;

	private ArrayList<Notification> notifications = new ArrayList<Notification>();
	private LinearLayout notificationsListView;
	private HashMap<String, Bitmap> savedImages = new HashMap<String, Bitmap>();
	
	private Handler handler = new Handler();
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notifications_main);
		
		mApp = (BartsyApplication) getApplication();
		
		// Set up the action bar custom view
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		
		LinearLayout notificationsLayout = (LinearLayout)findViewById(R.id.people_notifications);
		
		ScrollView scrollView = new ScrollView(this);
		notificationsLayout.addView(scrollView);
		
		notificationsListView = new LinearLayout(this);
		// Set parameters for linear layout
		LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		notificationsListView.setLayoutParams(params);
		notificationsListView.setOrientation(LinearLayout.VERTICAL);
				
		// Set dividers
		notificationsListView.setDividerDrawable(getResources().getDrawable(R.drawable.div_light_grey));
		notificationsListView.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE | LinearLayout.SHOW_DIVIDER_BEGINNING | LinearLayout.SHOW_DIVIDER_END);
		
		scrollView.addView(notificationsListView);
//		notificationsListView.
		
		loadNotifications();
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		
		case android.R.id.home:
			// app icon in action bar clicked; go home
//			Intent intent = new Intent(this, MainActivity.class);
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(intent);
			finish();
			return super.onOptionsItemSelected(item);
		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
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
//			if(notification.getOrderType() != null && (notification.getOrderType().equals("offer") || notification.getOrderType().equals("placeOrder"))){
			if(Notification.TYPE_PLACE_ORDER.equals(notification.getType())){
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
		ImageView profileImage = ((ImageView)view.findViewById(R.id.profileImage));
		((TextView)view.findViewById(R.id.messageText)).setText(notification.getMessage());
		
		((TextView)view.findViewById(R.id.dateText)).setText(Utilities.getFriendlyDate(notification.getCreatedTime(), "d MMM yyyy HH:mm:ss 'GMT'"));
		
		// Set profile image based on the notification type
		((TextView)view.findViewById(R.id.userNameText)).setText(notification.getVenueName());
		profileImage.setTag(WebServices.DOMAIN_NAME+notification.getVenueImage());
		WebServices.downloadImage(profileImage, savedImages);		
		
//		ImageView imageView = ((ImageView)view.findViewById(R.id.messageTypeImage));
		// If the notification is related to checkout then replace messagetype image with checkout image 
//		if(Notification.TYPE_CHECKOUT.equals(notification.getType())){
//			imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.checkout));
//		}else if(Notification.TYPE_UPDATE_ORDER.equals(notification.getType())){
//			imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.drink));
//		}
		
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
		View view = getLayoutInflater().inflate(R.layout.notifications_item, null);
		
		// Set all notification information to the view
		((ImageView)view.findViewById(R.id.profileImage)).setImageBitmap(mApp.mProfile.getImage());
		((TextView)view.findViewById(R.id.messageText)).setText(notification.getMessage());
		
		((TextView)view.findViewById(R.id.dateText)).setText(Utilities.getFriendlyDate(notification.getCreatedTime(), "d MMM yyyy HH:mm:ss 'GMT'"));
		
		ImageView otherProfileImage = ((ImageView)view.findViewById(R.id.profileImage));
		
		if(Notification.TYPE_PLACE_ORDER.equals(notification.getType())){
//			ImageView imageView = ((ImageView)view.findViewById(R.id.messageTypeImage));
//			imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.drink));
			
			// Set venue profile image
			((TextView)view.findViewById(R.id.userNameText)).setText(notification.getVenueName());
			if(notification.hasVenueImage()){
				otherProfileImage.setTag(WebServices.DOMAIN_NAME+notification.getVenueImage());
			}
			
		}else if(notification.getOrder()!=null){
			// Set other person profile image
			((TextView)view.findViewById(R.id.userNameText)).setText(notification.getOrder().recipientNickname);
			otherProfileImage.setTag(WebServices.DOMAIN_NAME+notification.getOrder().recipientImagePath);
		}
		WebServices.downloadImage(otherProfileImage, savedImages);
		
		// Add view to the notification list view
		notificationsListView.addView(view);
	}
	
	
}
