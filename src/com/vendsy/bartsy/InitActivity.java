package com.vendsy.bartsy;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnPersonLoadedListener;
import com.google.android.gms.plus.model.people.Person;
import com.vendsy.bartsy.dialog.LoginDialogFragment;
import com.vendsy.bartsy.dialog.LoginDialogFragment.LoginDialogListener;
import com.vendsy.bartsy.dialog.ProfileDialogFragment;
import com.vendsy.bartsy.dialog.ProfileDialogFragment.ProfileDialogListener;
import com.vendsy.bartsy.facebook.AndroidFacebookConnectActivity;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;

public class InitActivity extends SherlockFragmentActivity implements
		ConnectionCallbacks, OnConnectionFailedListener, OnPersonLoadedListener, ProfileDialogListener, OnClickListener, LoginDialogListener {

	private static final String TAG = "InitActivity";

	private ViewPager pager;
	private static int NUM_VIEWS = 2;
	private InitAdapter adapter;
	private PlusClient mPlusClient;
	// private ProgressDialog mConnectionProgressDialog;
	final Context context = this;
	private ConnectionResult mConnectionResult = null;
	private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
	private static final int REQUEST_CODE_USER_PROFILE = 9001;
	private static final int REQUEST_CODE_USER_FB = 9002;
	public static final String REQUEST_CODE_USER_FB_RESULT = "AndroidFacebookConnectActivity.result";
	static final String[] SCOPES = new String[] { Scopes.PLUS_LOGIN };
	public ProgressDialog mConnectionProgressDialog;

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
		setTheme(R.style.Theme_Sherlock_Light_DarkActionBar);
		setContentView(R.layout.init_main);

		// Setup pointers
		mApp = (BartsyApplication) getApplication();
		
		// Hide action bar
		getSupportActionBar().hide();

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
		Log.d(TAG, "Clicked on a button");

		switch (v.getId()) {
		case R.id.sign_in_button:
			if (!mPlusClient.isConnected()) {
				mPlusClient.connect();
				if (mConnectionResult == null) {
					mConnectionProgressDialog.show();
				} else {
					try {
						mConnectionResult.startResolutionForResult(mActivity, REQUEST_CODE_RESOLVE_ERR);
					} catch (SendIntentException e) {
						// Try connecting again.
						mConnectionResult = null;
						mPlusClient.connect();
					}
				}
				Log.d(TAG, "Connecting App to Google...");
			} else {
				// Disconnect and connect again per user request...
				mPlusClient.clearDefaultAccount();
				mPlusClient.disconnect();
				mPlusClient.connect();
			}
			break;
		case R.id.button_disconnect:

			mConnectionProgressDialog.show();

			// Start Face book connection
			Intent fbIntent = new Intent(InitActivity.this, AndroidFacebookConnectActivity.class);
			startActivityForResult(fbIntent, REQUEST_CODE_USER_FB);
			
			break;
		case R.id.view_init_create_account:
			
			mApp.mUserProfileActivityInput = null;
			Intent intent = new Intent(getBaseContext(), UserProfileActivity.class);
			this.startActivityForResult(intent, REQUEST_CODE_USER_PROFILE);

			break;
			
		case R.id.view_init_sign_in:

			new LoginDialogFragment().show(getSupportFragmentManager(),"Please log in to Bartsy");
			
			break;
		}
	}

	
	
	@Override
	public void onDialogPositiveClick(LoginDialogFragment dialog) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Login with username " + dialog.username, Toast.LENGTH_SHORT).show();
		
		// Create a new thread to handle getting a response from the host
		
		final UserProfile user = new UserProfile();
		user.setLogin(dialog.username);
		user.setPassword(dialog.password);
		
		new Thread() {
			public void run() {
				UserProfile profile = WebServices.getUserProfile(mApp.getApplicationContext(), user);

				// If there was an error, Toast it and do nothing more.
				if (profile == null) {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(mActivity, "Could not log in. Please try again.", Toast.LENGTH_SHORT).show();
						}
					});
					return;
				}

				// We got a new user. Start profile edit activity using this user and the input
				mApp.mUserProfileActivityInput = profile;
				Intent intent = new Intent(getBaseContext(), UserProfileActivity.class);
				mActivity.startActivityForResult(intent, REQUEST_CODE_USER_PROFILE);
			};
		}.start();
	}

	@Override
	public void onDialogNegativeClick(LoginDialogFragment dialog) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Cancel login", Toast.LENGTH_SHORT).show();
		
	}

	
	
	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		
		Log.v(TAG, "Activity result for request: " + requestCode + " with response: " + responseCode);

		switch (requestCode) {
		case REQUEST_CODE_RESOLVE_ERR:
			switch (responseCode) {
			case RESULT_OK:
				Log.v(TAG, "Result is ok, trying to reconnect");
				mConnectionResult = null;
				mPlusClient.connect();
				break;
			default:
				Log.e(TAG, "Connection cancelled");
				if (mConnectionProgressDialog.isShowing())
					mConnectionProgressDialog.dismiss();
				Toast.makeText(this, "Connection cancelled", Toast.LENGTH_SHORT).show();
				break;
			}
			break;
			
		case REQUEST_CODE_USER_FB:
			switch (responseCode) {
			case RESULT_OK:
				Log.v(TAG, "Received Facebook information");
				String response  = intent.getStringExtra(InitActivity.REQUEST_CODE_USER_FB_RESULT);
				
				// Reset the user profile activity input buffer
				mApp.mUserProfileActivityInput = null;
				
				try {
					JSONObject fbProfileData = new JSONObject(response);
					UserProfile p = new UserProfile(fbProfileData);

					// If the Facebook response was parsed correctly, start the profile activity with a FB user
					mApp.mUserProfileActivityInput = p;
				} catch (JSONException e) {
					e.printStackTrace();
					if (mConnectionProgressDialog.isShowing())
						mConnectionProgressDialog.dismiss();
					Toast.makeText(this, "Could not download Facebook information", Toast.LENGTH_SHORT).show();
					return;
				}
				 
				Intent userProfileintent = new Intent(getBaseContext(), UserProfileActivity.class);
				this.startActivityForResult(userProfileintent, REQUEST_CODE_USER_PROFILE);		
				break;
			default:
				Log.v(TAG, "Failed to get FACEBOOK information");
				Toast.makeText(this, "Could not download Facebook information", Toast.LENGTH_SHORT).show();
				if (mConnectionProgressDialog.isShowing())
					mConnectionProgressDialog.dismiss();
				break;
			}
			break;
		case REQUEST_CODE_USER_PROFILE:
			switch (responseCode) {
			case RESULT_OK:
				// We got a response from the user profile activity. Process the user profile and start
				// the right activity if successful
				Log.v(TAG, "Profile saved - process results");
				processUserProfile(mApp.mUserProfileActivityOutput);
				break;
			default:
				// No profile was created - dismiss the dialog
				if (mConnectionProgressDialog != null && mConnectionProgressDialog.isShowing()) 
					mConnectionProgressDialog.dismiss();
				Log.v(TAG, "Profile not saved");
				break;
			}

			// Reset parameters passed as inputs using the application object 
			Log.d(TAG, "Resetting application user input/output buffers");
			mApp.mUserProfileActivityInput = null;
			mApp.mUserProfileActivityOutput = null;
			
			break;
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
		mAccountName = mPlusClient.getAccountName();
//		Toast.makeText(this, "Connected as " + mAccountName, Toast.LENGTH_LONG).show();
		mPlusClient.loadPerson(this, "me");
	}
	

	@Override
	public void onPersonLoaded(ConnectionResult status, Person mPerson) {

		Log.v(TAG, "onPersonLoaded()");

		
		if (status.getErrorCode() == ConnectionResult.SUCCESS) {

			Log.v(TAG, "Person Loaded successfully");

			// Download and save profile picture in the background
			if (mPerson.hasImage() && mPerson.hasName() && mPerson.hasBirthday() && !mPerson.getBirthday().substring(0,4).equalsIgnoreCase("0000")) {
				new DownloadAndSaveUserProfileImageTask().execute(mPerson.getImage().getUrl(), getResources().getString(R.string.config_user_profile_picture));

				// Show dialog and on exit start Bartsy (there should be an option to change the profile)
				ProfileDialogFragment dialog = new ProfileDialogFragment();
				dialog.mUser = mPerson;
				dialog.show(getSupportFragmentManager(), "Your profile");

				Toast.makeText(mActivity, "Downloaded your Google profile, please verify it...", Toast.LENGTH_SHORT).show();
				
				Log.d(TAG, mPerson.getAgeRange().toString());
				return;
				
			} else {
				// Incomplete profile
				Log.d(TAG, "Incomplete profile - starting blank user edit activity");
//				Toast.makeText(mActivity, "Your Google profile could use some adding too...", Toast.LENGTH_SHORT).show();
			}
		} else {
			Log.d(TAG, "Error loading person - starting blank user edit activity");
			Toast.makeText(mActivity, "Could not download profile, please create one...", Toast.LENGTH_SHORT).show();
		}
		
		// If the profile is incomplete or couldn't be downloaded, start the profile edit activity
		mApp.mUserProfileActivityInput = new UserProfile(mPerson, mAccountName); // use the application as a buffer to pass the message to the new activity
		Intent intent = new Intent(getBaseContext(), UserProfileActivity.class);
		this.startActivityForResult(intent, REQUEST_CODE_USER_PROFILE);		
		
	}

	

	@Override
	public void onUserDialogPositiveClick(final DialogFragment dialog) {
		// User accepted the profile, launch main activity
		
		Log.v(TAG, "onUserDialogPositiveClick()");
		
		// Create a profile for the person
		Person person = ((ProfileDialogFragment) dialog).mUser;
		UserProfile profile = new UserProfile(person, this.mAccountName);
		profile.setImage(mApp.loadUserProfileImage());
		processUserProfile(profile);
	}
	
	
	@Override 
	public void onUserDialogNegativeClick(final DialogFragment dialog) {
		// Start user profile activity and pass it a pointer to the user object saved in the global application structure for convenience
		Person person = ((ProfileDialogFragment) dialog).mUser;
		mApp.mUserProfileActivityInput = new UserProfile(person, mAccountName); // use the application as a buffer to pass the message to the new activity
		Intent intent = new Intent(getBaseContext(), UserProfileActivity.class);
		this.startActivityForResult(intent, REQUEST_CODE_USER_PROFILE);
		finish();
	}
	
	
	/**
	 * THis function is called when the user has accepted the profile in the profile activity. It saves the user
	 * details locally. If the user is checked in according to the server, the function also checks the user in
	 * locally. This function will either terminate the activity and start a new one or leave the user in this 
	 * activity with a Toast asking them to retry their login.
	 * 
	 * The function uses the mUser parameters passed through the application as assumes they are set up
	 */
	
	public void processUserProfile(final UserProfile userProfile) {
		
		Log.i(TAG, "processUserProfileData()");
		
		SharedPreferences settings = getSharedPreferences(GCMIntentService.REG_ID, 0);
		String deviceToken = settings.getString("RegId", "");
		if (deviceToken.trim().length() > 0) {
			
			// Send profile data to server in background

			new Thread() {
				public void run() {
					String bartsyUserId = null;
					
					try {
						// Service call for post profile data to server
						JSONObject resultJson = WebServices.postProfile(userProfile, Constants.URL_POST_PROFILE_DATA, getApplicationContext());

						// Process web service response
						
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
									bartsyUserId = resultJson.getString("bartsyId");

									Log.v(TAG, "bartsyUserId " + bartsyUserId + "");
								} else {
									Log.e(TAG, "BartsyID " + "bartsyUserId not found");
								}
								
								final String bartsyId = bartsyUserId;
								// To check whether user is checkedIn or not. If user already checkedIn then it 
								// should navigate to VenueActivity, otherwise it should navigate to MainActivity
								
								mHandler.post(new Runnable() {
									public void run() {
										// Save profile in the global application structure and in preferences
										userProfile.setBartsyId(bartsyId);
										
										mApp.saveUserProfile(userProfile);
										
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
									if (mConnectionProgressDialog != null && mConnectionProgressDialog.isShowing())
										mConnectionProgressDialog.dismiss();
								}
							});
							return;
						}
					} catch (JSONException e) {
						e.printStackTrace();

						// Error creating user. Ask parent to post Toast.
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(mActivity, "Please try again....", Toast.LENGTH_LONG).show();
								if (mConnectionProgressDialog != null && mConnectionProgressDialog.isShowing())
									mConnectionProgressDialog.dismiss();
							}
						});
						return;
					
					}
				}
			}.start();

		} else {
			Toast.makeText(this, "Please try again....", Toast.LENGTH_LONG).show();
			if (mConnectionProgressDialog != null && mConnectionProgressDialog.isShowing())
				mConnectionProgressDialog.dismiss();
			return;
		}
	}


	@Override
	public void onDisconnected() {
		Log.d(TAG, "disconnected");
		Toast.makeText(this, "Logged out from Google", Toast.LENGTH_LONG).show();
	}

	
	private class DownloadAndSaveUserProfileImageTask extends AsyncTask<String, Integer, Bitmap> {
		// Do the long-running work in here

		protected Bitmap doInBackground(String... params) {
			// Kind of inefficient way to download an image. Need to just save
			// the file as it comes...

			String url = params[0];
			Bitmap bitmap; // the temporary bitmap used to transfer the image from the web to a file

			Log.d(TAG, "Fetching user image profile image from: " + url);

			// Fetch image from URL into a bitmap
			try {
				bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
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
			mApp.saveUserProfileImage(bitmap);

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

				// Set up create account button
				Button bt = (Button) v.findViewById(R.id.view_init_create_account);
				bt.setOnClickListener(mActivity);
				
				// Set up create account button
				bt = (Button) v.findViewById(R.id.view_init_sign_in);
				bt.setOnClickListener(mActivity);
				
				
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
