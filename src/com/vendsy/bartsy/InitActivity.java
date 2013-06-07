package com.vendsy.bartsy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnAccessRevokedListener;
import com.google.android.gms.plus.PlusClient.OnPersonLoadedListener;
import com.google.android.gms.plus.model.people.Person;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.dialog.ProfileDialogFragment;
import com.vendsy.bartsy.dialog.ProfileDialogFragment.ProfileDialogListener;
import com.vendsy.bartsy.facebook.AndroidFacebookConnectActivity;
import com.vendsy.bartsy.model.Profile;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.Utilities;
import com.vendsy.bartsy.utils.WebServices;

public class InitActivity extends FragmentActivity implements
		ConnectionCallbacks, OnConnectionFailedListener, OnPersonLoadedListener, ProfileDialogListener, OnClickListener {

	private static final String TAG = "InitActivity";

	private ViewPager pager;
	private static int NUM_VIEWS = 2;
	private InitAdapter adapter;
	private PlusClient mPlusClient;
	// private ProgressDialog mConnectionProgressDialog;
	final Context context = this;
	private ConnectionResult mConnectionResult = null;
	private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
	static final String[] SCOPES = new String[] { Scopes.PLUS_LOGIN };
	private ProgressDialog mConnectionProgressDialog;

	BartsyApplication mApp = null;
	InitActivity mActivity = this;
	String mAccountName = null;
	Handler mHandler = new Handler();

	
	/** 
	 * Called when the activity is first created. 
	 * */
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.init_main);

		// Setup pointers
		mApp = (BartsyApplication) getApplication();
		
		// Hide action bar
		getActionBar().hide();

		// Initialize the startup screen tabs
		adapter = new InitAdapter();
		pager = (ViewPager) findViewById(R.id.awesomepager);
		pager.setAdapter(adapter);

		// Initialize Google sign in framework
		mPlusClient = new PlusClient.Builder(this, this, this)
				.setScopes(SCOPES)
				.setVisibleActivities("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity").build();

		// Progress bar to be displayed if the connection failure is not resolved.
		mConnectionProgressDialog = new ProgressDialog(this);
		mConnectionProgressDialog.setMessage("Connecting");

	}

	@Override
	protected void onStart() {
		super.onStart();
		// mPlusClient.connect();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mPlusClient.disconnect();
	}

	@Override
	public void onClick(View v) {
		Log.d(TAG, "Connecting to Google+...");

		switch (v.getId()) {
		case R.id.sign_in_button:
			if (!mPlusClient.isConnected()) {
				mPlusClient.connect();
				if (mConnectionResult == null) {
					mConnectionProgressDialog.show();
				} else {
					try {
						mConnectionResult.startResolutionForResult(mActivity,
								REQUEST_CODE_RESOLVE_ERR);
					} catch (SendIntentException e) {
						// Try connecting again.
						mConnectionResult = null;
						mPlusClient.connect();
					}
				}
				Log.d(TAG, "Connecting App to Google...");
			} else
				Toast.makeText(this, "Already logged in to Google...",
						Toast.LENGTH_SHORT).show();
			break;
		case R.id.button_disconnect:
			if (mPlusClient.isConnected()) {
				mPlusClient.clearDefaultAccount();
				mPlusClient.disconnect();
				// mPlusClient.connect();
				Toast.makeText(this, "Logged out from Google",
						Toast.LENGTH_SHORT).show();
			} else
				Toast.makeText(this, "Already logged out from Google",
						Toast.LENGTH_SHORT).show();
			break;
		case R.id.button_revoke:
			if (!mPlusClient.isConnected()) {
				// Need to be connected in order to revoke access
				mPlusClient.connect();
				
				Toast.makeText(this, "Need to be logged in to disconnect App",
						Toast.LENGTH_SHORT).show();
				break;
			}
			mPlusClient
					.revokeAccessAndDisconnect(new OnAccessRevokedListener() {
						@Override
						public void onAccessRevoked(ConnectionResult status) {
							// mPlusClient is now disconnected and access has been revoked.
							// Trigger app logic to comply with the developer policies
							Toast.makeText(mActivity, "Disconnected App from Google", Toast.LENGTH_SHORT).show();
							mApp.eraseUserProfile();
						}
					});

			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int responseCode,
			Intent intent) {
		Log.d(TAG, "Activity result for request: " + requestCode
				+ " with response: " + responseCode);

		String error = "";

		switch (requestCode) {
		case REQUEST_CODE_RESOLVE_ERR:
			switch (responseCode) {
			case RESULT_OK:
				Log.d(TAG, "Result is ok, trying to reconnect");
				mConnectionResult = null;
				mPlusClient.connect();
				break;
			default:
				if (mConnectionProgressDialog.isShowing())
					mConnectionProgressDialog.dismiss();
				Toast.makeText(this, "Connection cancelled", Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.d(TAG, "Connection failed with result: " + result.toString());
		if (result.hasResolution()) {
			Log.d(TAG, "Trying to resolve error");
			try {
				result.startResolutionForResult(this, REQUEST_CODE_RESOLVE_ERR);
			} catch (SendIntentException e) {
				mPlusClient.connect();
				Log.d(TAG, "Trying to resolve error by reconnecting");
			}
		}
		// Save the result and resolve the connection failure upon a user click.
		mConnectionResult = result;
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		mConnectionProgressDialog.dismiss();
		mAccountName = mPlusClient.getAccountName();
		Toast.makeText(this, "Connected as " + mAccountName, Toast.LENGTH_LONG).show();
		mPlusClient.loadPerson(this, "me");
	}
	

	@Override
	public void onPersonLoaded(ConnectionResult status, Person mPerson) {
		if (status.getErrorCode() == ConnectionResult.SUCCESS) {

			// Save profile picture in the background
			new DownloadImageTask().execute(mPerson.getImage().getUrl(), getResources().getString(R.string.config_user_profile_picture));


			// Show dialog and on exit start Bartsy (there should be an option to change the profile)
			ProfileDialogFragment dialog = new ProfileDialogFragment();
			dialog.mUser = mPerson;
			dialog.show(getSupportFragmentManager(), "Your profile");

		} else {
			Log.d(TAG, "Error loading person");
		}
	}

	@Override
	public void onUserDialogPositiveClick(final DialogFragment dialog) {
		// User accepted the profile, launch main activity
		
		Log.i(TAG, "InitActivity.onUserDialogPositiveClick()");
		
		SharedPreferences settings = getSharedPreferences(GCMIntentService.REG_ID, 0);
		String deviceToken = settings.getString("RegId", "");
		if (deviceToken.trim().length() > 0) {
			// To send profile data to server in background
			new Thread() {
				public void run() {
					int bartsyUserId=0;
					// Create a profile for the person
					Person person = ((ProfileDialogFragment) dialog).mUser;
					
					final Profile profile = getNewProfile(person);
					
					try {
						// Service call for post profile data to server
						JSONObject resultJson = WebServices.postProfile(profile, ((ProfileDialogFragment) dialog).mProfileImage, Constants.URL_POST_PROFILE_DATA, getApplicationContext());
						if (resultJson!=null && resultJson.has("errorCode") && resultJson.getString("errorCode").equalsIgnoreCase("0")) {

							final String userCheckedInOrNot = resultJson.getString("userCheckedIn");
							// Error handling
								// if user checkedIn is true
								if ( userCheckedInOrNot!=null && userCheckedInOrNot.equalsIgnoreCase("0") && resultJson.has("venueId") && resultJson.has("venueName"))
								{
									// Check the user in locally 
									mApp.userCheckIn(resultJson.getString("venueId"), resultJson.getString("venueName"));
								}
								
								if (resultJson.has("bartsyId")) {
									bartsyUserId = resultJson.getInt("bartsyId");

									Log.v(TAG, "bartsyUserId " + bartsyUserId + "");
								} else {
									Log.e(TAG, "BartsyID " + "bartsyUserIdnot found");
								}
								
								final int bartsyId = bartsyUserId;
								// To check whether user is checkedIn or not. If user already checkedIn then it 
								// should navigate to VenueActivity, otherwise it should navigate to MainActivity
								
								mHandler.post(new Runnable() {
									public void run() {
										// Save profile in the global application structure and in preferences
										profile.bartsyID = bartsyId;
										
										mApp.saveUserProfile(profile);
										
										if (userCheckedInOrNot.equalsIgnoreCase("0")) {
											// User profile saved successfully and user checked in
											startActivity(new Intent().setClass(mActivity, VenueActivity.class));
										} else {
											// User profile saved successfully and user in not checked in
											startActivity(new Intent().setClass(mActivity, MainActivity.class));
										}
										
										// Stop the parent activity of this thread (initactivity) as we just started a new one
										mActivity.finish();
									}
								});
								
						}else{
									// Error creating user. Ask parent to post Toast.
									mHandler.post(new Runnable() {
										@Override
										public void run() {
											Toast.makeText(mActivity, "Please try again....", Toast.LENGTH_LONG).show();
										}
									});
									return;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}.start();

		} else {
			Toast.makeText(this, "Please try again....", Toast.LENGTH_LONG).show();
			return;
		}
	}
	
	/**
	 * To initiate new profile from the google+ person object 
	 * 
	 * @param person
	 * @return
	 */
	protected Profile getNewProfile(Person person) {
		
		Profile profile = new Profile();
		profile.setUsername(person.getId());
		profile.setName(person.getDisplayName());
		profile.setType("google");
		profile.setSocialNetworkId(person.getId());
		profile.setGender(String.valueOf(person.getGender()));
		
		
		if(person.getAboutMe()!=null){
			profile.setDescription(person.getAboutMe());
		}
		if(person.getBirthday()!=null){
			profile.dateofbirth = person.getBirthday();
		}
		
		// Error handling - null should not get in JSON format
		if(person.getName()!=null && person.getName().hasGivenName()){
			profile.firstName = person.getName().getGivenName();
		}else{
			profile.firstName = "";
		}
		
		// Error handling
		if(person.getName()!=null && person.getName().hasFamilyName()){
			profile.lastName = person.getName().getFamilyName();
		}else{
			profile.lastName = "";
		}
		
		// try to get nick name. if it is not exist then we can set first name
		if(person.getNickname()!=null){
			profile.nickname = person.getNickname();
		}else{
			profile.nickname = profile.firstName;
		}
		
		if(mPlusClient!=null && mPlusClient.getAccountName()!=null){
			profile.setEmail(mPlusClient.getAccountName());
		}
		
		return profile;
	}


	@Override
	public void onUserDialogNegativeClick(DialogFragment dialog) {
		// User wants to edit the profile. For now, do nothing - TODO

	}



	@Override
	public void onDisconnected() {
		Log.d(TAG, "disconnected");
		Toast.makeText(this, "Logged out from Google", Toast.LENGTH_LONG)
				.show();

	}

	private class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {
		// Do the long-running work in here

		protected Bitmap doInBackground(String... params) {
			// Kind of inefficient way to download an image. Need to just save
			// the file as it comes...

			String url = params[0], file = params[1];
			Bitmap bitmap; // the temporary bitmap used to transfer the image
							// from the web to a file

			Log.d(TAG, "Fetching user image profile image from: " + url);

			// Fetch image from URL into a bitmap
			try {
				bitmap = BitmapFactory.decodeStream((InputStream) new URL(url)
						.getContent());
			} catch (MalformedURLException e) {
				e.printStackTrace();
				Log.d(TAG, "Bad URL: " + url);
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				Log.d(TAG, "Could not download image from URL: " + url);
				return null;
			}

			if (bitmap == null) {
				Log.d(TAG, "Could not create bitmap " + url);
				return null;
			}

			// Save bitmap to file
			Log.d(TAG, "Saving user profile to " + getFilesDir()
					+ File.separator + file);

			try {
				FileOutputStream out = new FileOutputStream(getFilesDir()
						+ File.separator + file);

				bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG,
						"Could not save user profile to "
								+ getResources().getString(
										R.string.config_user_profile_picture));

				return null;
			}

			return bitmap;
		}

	}

	/****************************
	 * 
	 * 
	 * 
	 * TODO - View handling
	 * 
	 * @author peterkellis
	 * 
	 */

	private class InitAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return NUM_VIEWS;
		}

		/**
		 * Create the page for the given position. The adapter is responsible
		 * for adding the view to the container given here, although it only
		 * must ensure this is done by the time it returns from
		 * {@link #finishUpdate(android.view.ViewGroup)}.
		 * 
		 * @param collection
		 *            The containing View in which the page will be shown.
		 * @param position
		 *            The page position to be instantiated.
		 * @return Returns an Object representing the new page. This does not
		 *         need to be a View, but can be some other container of the
		 *         page.
		 */
		@Override
		public Object instantiateItem(ViewGroup collection, int position) {

			View v = null;

			switch (position) {
			case 0:
				v = getLayoutInflater().inflate(R.layout.init_page_0, null);
				break;
			case 1:
				v = getLayoutInflater().inflate(R.layout.init_page_1, null);

				// Setup sign-in button to start Google sign in
				SignInButton button = (SignInButton) v
						.findViewById(R.id.sign_in_button);
				button.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_DARK);
				button.setOnClickListener(mActivity);

				// Set up Facebook button to disconnect (DEBUG)
				ImageButton b = (ImageButton) v
						.findViewById(R.id.button_disconnect);
				b.setOnClickListener(mActivity);

				// Set up Twitter button to revoke (DEBUG)
				b = (ImageButton) v.findViewById(R.id.button_revoke);
				b.setOnClickListener(mActivity);
				break;
			}
			collection.addView(v);
			return v;
		}

		/**
		 * Remove a page for the given position. The adapter is responsible for
		 * removing the view from its container, although it only must ensure
		 * this is done by the time it returns from
		 * {@link #finishUpdate(android.view.ViewGroup)}.
		 * 
		 * @param collection
		 *            The containing View from which the page will be removed.
		 * @param position
		 *            The page position to be removed.
		 * @param view
		 *            The same object that was returned by
		 *            {@link #instantiateItem(android.view.View, int)}.
		 */
		@Override
		public void destroyItem(ViewGroup collection, int position, Object view) {
			collection.removeView((View) view);
		}

		/**
		 * Determines whether a page View is associated with a specific key
		 * object as returned by instantiateItem(ViewGroup, int). This method is
		 * required for a PagerAdapter to function properly.
		 * 
		 * @param view
		 *            Page View to check for association with object
		 * @param object
		 *            Object to check for association with view
		 * @return
		 */
		@Override
		public boolean isViewFromObject(View view, Object object) {
			return (view == object);
		}

		/**
		 * Called when the a change in the shown pages has been completed. At
		 * this point you must ensure that all of the pages have actually been
		 * added or removed from the container as appropriate.
		 * 
		 * @param arg0
		 *            The containing View which is displaying this adapter's
		 *            page views.
		 */
		@Override
		public void finishUpdate(ViewGroup arg0) {
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}
	}
}
