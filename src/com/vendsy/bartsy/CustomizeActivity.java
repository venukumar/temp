package com.vendsy.bartsy;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.LayoutParams;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.vendsy.bartsy.dialog.DrinkDialogFragment;
import com.vendsy.bartsy.model.Item;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.utils.Utilities;
import com.vendsy.bartsy.utils.WebServices;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author Peter Kellis
 *
 */

public class CustomizeActivity extends SherlockActivity implements OnClickListener {
	
	private final static String TAG = "ItemOptionsActivity";
	
	private BartsyApplication mApp;
	private Item mItem;
	private Handler mHandler = new Handler();
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
	private boolean pendingPublishReauthorization = false;
	private UiLifecycleHelper uiHelper;
	private ImageView like;
	
	// Callback for Facebook session status
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
			pendingPublishReauthorization = savedInstanceState.getBoolean(
					PENDING_PUBLISH_KEY, false);
		}
		mApp = (BartsyApplication) getApplication();
		
		// Make sure the input is still valid (in case we're lost the application object when this activity started)
		try {
			mItem = loadInput(mApp);
		} catch (Exception e) {
			// Invalid input
			e.printStackTrace();
			Log.e(TAG, "Invalid input");
			finish();
			return;
		}
	
		// Set up the action bar custom view
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setIcon(R.drawable.circle_pink);

		// Inflate the view and set as custom view for action bar
		LayoutInflater inflator = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View customView = inflator.inflate(R.layout.header_order_customize,
				null);
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
				| ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_USE_LOGO);
		LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, Gravity.RIGHT
						| Gravity.CENTER_VERTICAL);
		actionBar.setCustomView(customView, lp);
		//Set the title
		if (mItem.hasTitle())
			actionBar.setTitle(mItem.getName());
		
		// Share the item 
		ImageView shareButton=(ImageView)customView.findViewById(R.id.view_order_share);
		shareButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SEND);
			    intent.setType("text/plain");
			    intent.putExtra(Intent.EXTRA_SUBJECT,"Liked "+mItem.getName()+" on "+mApp.mActiveVenue.getName()+" with Bartsy");
			    intent.putExtra(Intent.EXTRA_TEXT, "Download Bartsy Application from Google Play for more details");  
			    startActivity(Intent.createChooser(intent, "Share with"));
				
			}
		});
		like = (ImageView) customView.findViewById(R.id.view_order_like);
		like.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				likeAlert("Are you sure you want to like " + mItem.getName()
						+ " on Facebook");
			}
		});
		
		// Set the visibility of the like button to true only if the user logins through Facebook
		if (mApp.mProfile.hasFacebookId()) {
			like.setVisibility(View.VISIBLE);
		} else {
			like.setVisibility(View.GONE);
		}
		
		// Set the main view
		setContentView(mItem.customizeView(getLayoutInflater()));
		
		// Set up the favorites checkbox
		if (mItem.has(mItem.getFavoriteId())) {
			((CheckBox) findViewById(R.id.view_order_item_favorite)).setChecked(true);
		} else {
			((CheckBox) findViewById(R.id.view_order_item_favorite)).setChecked(false);
		}
		
		// Set up listeners
		findViewById(R.id.item_customize_positive).setOnClickListener(this);
		findViewById(R.id.item_customize_negative).setOnClickListener(this);
		findViewById(R.id.view_order_item_favorite).setOnClickListener(this);
	}
	
	/**
	 * Display confirmation alert box when the user selects like button
	 * 
	 * @param message
	 */
	private void likeAlert(String message) {

		AlertDialog.Builder builder = new AlertDialog.Builder(
				CustomizeActivity.this);
		builder.setCancelable(true);
		builder.setTitle("Like us");
		builder.setInverseBackgroundForced(true);
		builder.setMessage(message);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
				like.setImageResource(R.drawable.like_blue);
				publishItem();

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
	 * Publish the feed on Facebook wall
	 */
	protected void publishItem() {
		Session session = Session.getActiveSession();
		if (session != null) {
			if (session.isOpened()) {
				// Check for publish permissions
				if (pendingPublishReauthorization == false) {
					postParams(session);
				}

			} else {
				// Open the session if the session is closed or inactive for publishing the feed
				Session.openActiveSession(this, true,
						new Session.StatusCallback() {

							@Override
							public void call(final Session session,
									SessionState state, Exception exception) {

								if (session.isOpened()) {
									Request.executeMeRequestAsync(session,
											new Request.GraphUserCallback() {
												@Override
												public void onCompleted(
														GraphUser user,
														Response response) {
													if (user != null
															&& pendingPublishReauthorization == false) {
														postParams(session);
													}
												}
											});
								} else {
									Log.d("SESSION NOT OPENED",
											"SESSION NOT OPENED");
								}
							}
						});
			}
		}

	}
	
	/**
	 * Post the params to be published on the Facebook feed
	 * @param session
	 */
	private void postParams(Session session) {
		Bundle postParams = new Bundle();
		postParams.putString("name", "Bartsy");
		postParams.putString("caption", "Liked " + mItem.getName() + " on"
				+ mApp.mActiveVenue.getName());
		postParams.putString("description", mItem.getDescription());
		postParams.putString("link", "");
		postParams.putString("picture", mApp.mActiveVenue.getImagePath());

		Request.Callback callback = new Request.Callback() {
			public void onCompleted(Response response) {
				if (response != null) {
					Log.i(TAG, response.toString());
					 JSONObject graphResponse;
					 GraphObject graphObject = response.getGraphObject();
					 if(graphObject!=null){
					 graphResponse = response.getGraphObject()
							.getInnerJSONObject();
					 }else{
						 return;
					 }
					String postId = null;
					try {
						// PostId obtained after publishing the feed
						postId = graphResponse.getString("id");
					} catch (JSONException e) {
						Log.i(TAG, "JSON error " + e.getMessage());
					}
					FacebookRequestError error = response.getError();
					try{
					if (error != null) {
						Toast.makeText(CustomizeActivity.this,
								error.getErrorMessage(), Toast.LENGTH_SHORT)
								.show();
					} else {
						Toast.makeText(CustomizeActivity.this,

						postId, Toast.LENGTH_LONG).show();
					}
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};

		Request request = new Request(session, "me/feed", postParams,
				HttpMethod.POST, callback);

		RequestAsyncTask task = new RequestAsyncTask(request);
		task.execute();

	}
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
		 if (pendingPublishReauthorization && 
			        state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
			    pendingPublishReauthorization = false;
			    publishItem();
			}
	 }





	/** 
	 * TODO - Activity input/output 
	 */
	
	// Used to check the validity of this activity's input
	private static final int ACTIVITY_INPUT_VALID	= 0;
	private static final int ACTIVITY_INPUT_INVALID = 1;
	
	@Override
	public void onPause() {
		super.onPause();
		
		Log.v(TAG, "onPause()");
		
		// Invalidate the input of this activity when exiting to avoid reentering it with invalid data
		Utilities.savePref(this, R.string.ItemOptionsActivity_input_status, ACTIVITY_INPUT_INVALID);
	}

	/*
	 * Sets the input of this activity and makes it valid
	 */
	public static final void setInput(BartsyApplication context, Item item) {
		Utilities.savePref(context, R.string.ItemOptionsActivity_input_status, ACTIVITY_INPUT_VALID);
		context.selectedMenuItem = item;
	}
	
	private void finishWithResult(BartsyApplication context, Item item, int result) {
		context.selectedMenuItem = item;
		setResult(result);
		finish();
	}
	
	public static final Item getOutput(BartsyApplication context) {
		return context.selectedMenuItem;
	}
	
	private Item loadInput(BartsyApplication context) throws Exception {

		// Make sure the input is valid
		if (Utilities.loadPref(this, R.string.ItemOptionsActivity_input_status, ACTIVITY_INPUT_VALID) != ACTIVITY_INPUT_VALID) {		
			Log.e(TAG, "Invalid activity input - exiting...");
			Utilities.removePref(this, R.string.ItemOptionsActivity_input_status);
			throw new Exception();
		}
		
		return context.selectedMenuItem;
	}
	
	/**
	 * TODO - Click listeners
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View arg0) {

		switch (arg0.getId()) {
		
		case R.id.item_customize_positive:
			
			// Update price and description based on the selections
			mItem.updateOptionsDescription();
			mItem.updateOrderPrice();
			
			String specialInstructions = ((EditText) findViewById(R.id.view_order_item_special_instructions)).getText().toString();
				
			if (Utilities.has(specialInstructions))
				mItem.setSpecialInstructions(specialInstructions);
			
			if (mItem.getOrderPrice() == 0 && mItem.getOptionsPrice(false) != 0 ) {
				mApp.makeText("Please select at least one option", Toast.LENGTH_SHORT);
				return;
			}
			
			finishWithResult(mApp, mItem, UserProfileActivity.RESULT_OK);
			
			break;

		case R.id.item_customize_negative:
		
			// Discard changes
			finishWithResult(mApp, mItem, UserProfileActivity.RESULT_FIRST_USER);
			break;
			
		case R.id.view_order_item_favorite:
			CheckBox favorite = (CheckBox) arg0;
			mItem.updateOptionsDescription();
			
			addOrRemovefavorite(favorite);
			
			break;
		}
	}

	private void addOrRemovefavorite(final CheckBox favoriteCheckBox) {
		
		new Thread(){
			public void run() {
				// Add to favorites
				if (favoriteCheckBox.isChecked()){ 
					
					// Update price and description based on the selections
					mItem.updateOptionsDescription();
					mItem.updateOrderPrice();
					
					// Update special instructions
					String specialInstructions = ((EditText) findViewById(R.id.view_order_item_special_instructions)).getText().toString();
					if (Utilities.has(specialInstructions))
						mItem.setSpecialInstructions(specialInstructions);
					
					// Save the favorite
					String response = WebServices.saveFavorites(mItem, mApp.mActiveVenue.getId(), mApp.mProfile.getBartsyId(), mApp);
					try {
						JSONObject json = new JSONObject(response);
						mItem.setFavoriteId(json.getString("favoriteDrinkId"));
						updateCheckbox(favoriteCheckBox, true);
					} catch (Exception e) {
						updateCheckbox(favoriteCheckBox, false);
					}
				}
				// Remove from favorites
				else if(mItem.getFavoriteId()!=null){
					WebServices.deleteFavorite(mItem.getFavoriteId(), mApp.mActiveVenue.getId(), mApp.mProfile.getBartsyId(), mApp);
					updateCheckbox(favoriteCheckBox, false);
				}
				
				// Update menus to show change in favorites
				mApp.notifyObservers(BartsyApplication.MENU_UPDATED);
			}
			
			private void updateCheckbox(final CheckBox favoriteCheckBox, final boolean checked) {

				mHandler.post(new Runnable() {

					@Override public void run() {
						if (checked) {
							favoriteCheckBox.setChecked(true);
							Toast.makeText(CustomizeActivity.this, "Saved to favorites", Toast.LENGTH_SHORT).show();
						} else {
							favoriteCheckBox.setChecked(false);
							mApp.makeText("Removed from favorites", Toast.LENGTH_SHORT);
						}
					}
				});
			}
			
		}.start();
		
	}

}
