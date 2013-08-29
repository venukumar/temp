package com.vendsy.bartsy;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnPersonLoadedListener;
import com.google.android.gms.plus.model.people.Person;
import com.vendsy.bartsy.dialog.LoginDialogFragment;
import com.vendsy.bartsy.dialog.LoginDialogFragment.LoginDialogListener;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.utils.WebServices;

public class InitActivity extends SherlockFragmentActivity implements
		ConnectionCallbacks, OnConnectionFailedListener, OnPersonLoadedListener, OnClickListener, LoginDialogListener {

	private static final String TAG = "InitActivity";

	private PlusClient mPlusClient;
	final Context context = this;
	private ConnectionResult mConnectionResult = null;
	private static final int REQUEST_CODE_RESOLVE_ERR = 9000;
	private static final int REQUEST_CODE_USER_PROFILE = 9001;
	private static final int REQUEST_CODE_USER_FB = 64206;
	public static final String REQUEST_CODE_USER_FB_RESULT = "AndroidFacebookConnectActivity.result";
	static final String[] SCOPES = new String[] { Scopes.PLUS_LOGIN };
	public ProgressDialog mConnectionProgressDialog;

	BartsyApplication mApp = null;
	InitActivity mActivity = this;
	String mAccountName = null;
	Handler mHandler = new Handler();
	LoginButton loginButton;
	private UiLifecycleHelper uiHelper;
	private static final List<String> PERMISSIONS = Arrays.asList(
			 "read_friendlists", "publish_stream", "offline_access", "email", "read_stream", "user_location" );
	
	 //Callback for Facebook session status
		private Session.StatusCallback callback = new Session.StatusCallback() {
		        @Override
		        public void call(Session session, SessionState state, Exception exception) {
		            onSessionStateChange(session, state, exception);
		        }
		    };

	
	/** 
	 * Called when the activity is first created. 
	 * */
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		// Setup pointers
		mApp = (BartsyApplication) getApplication();
		mActivity = this;
		
		// Hide action bar
		getSupportActionBar().hide();

		// Initialize Google sign in framework
		mPlusClient = new PlusClient.Builder(this, this, this)
				.setScopes(SCOPES)
				.setVisibleActivities("http://schemas.google.com/AddActivity", "http://schemas.google.com/BuyActivity").build();

		// Progress bar to be displayed if the connection failure is not resolved.
		mConnectionProgressDialog = new ProgressDialog(this);
		mConnectionProgressDialog.setMessage("Connecting");

		setContentView(R.layout.init_main);
		
		findViewById(R.id.view_init_create_account).setOnClickListener(this);
		findViewById(R.id.view_init_toggle_sign_in).setOnClickListener(this);
		findViewById(R.id.view_init_google).setOnClickListener(this);
		findViewById(R.id.view_init_sign_in).setOnClickListener(this);
		findViewById(R.id.view_init_toggle_sign_up).setOnClickListener(this);
		loginButton=(LoginButton) findViewById(R.id.view_init_facebook);
		
		//Before opening the session, asking for force login, even If the user has open facebook session from facebook application
//	    loginButton.setLoginBehavior(SessionLoginBehavior.SUPPRESS_SSO);
		
	    //set publish permission to post from facebook
		 loginButton.setPublishPermissions(PERMISSIONS);
		
		if (mApp.loadBartsyId() == null) 
			signUpListeners();
		else
			signInListeners();

		// Check and set development environment display
		if (WebServices.DOMAIN_NAME.equalsIgnoreCase("http://54.235.76.180:8080/") && 
				WebServices.SENDER_ID.equalsIgnoreCase("605229245886")) 
			((TextView) findViewById(R.id.view_main_deployment_environment)).setText("Server: DEV");
		else if (WebServices.DOMAIN_NAME.equalsIgnoreCase("http://app.bartsy.vendsy.com/") && 
				WebServices.SENDER_ID.equalsIgnoreCase("560663323691")) 
			((TextView) findViewById(R.id.view_main_deployment_environment)).setText("Server: PROD");
		else 
			((TextView) findViewById(R.id.view_main_deployment_environment)).setText("** INCONSISTENT DEPLOYMENT **");
		
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {if (session != null && session.isOpened()) {
        if (!state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
             makeMeRequest(session);
        }
     }
   }
	
	/**
	 * Fetch Facebook graph user information
	 * @param session
	 */
	private void makeMeRequest(final Session session) {
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
            	Log.d(TAG, "Received response");
            }
        });
        request.executeAsync();
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

		if (mConnectionProgressDialog != null && mConnectionProgressDialog.isShowing())
			mConnectionProgressDialog.dismiss();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}
	
	@Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }
	
	void signUpListeners() {
		findViewById(R.id.view_init_create_account).setVisibility(View.VISIBLE);
		findViewById(R.id.view_init_toggle_sign_in).setVisibility(View.VISIBLE);

		findViewById(R.id.view_init_sign_in).setVisibility(View.GONE);
		findViewById(R.id.view_init_toggle_sign_up).setVisibility(View.GONE);
		
		((TextView) findViewById(R.id.view_init_google)).setText("Sign up with Google");
		((TextView) findViewById(R.id.view_init_facebook)).setText("Sign up with Facebook");
	}
	
	void signInListeners() {
		findViewById(R.id.view_init_create_account).setVisibility(View.GONE);
		findViewById(R.id.view_init_toggle_sign_in).setVisibility(View.GONE);

		findViewById(R.id.view_init_sign_in).setVisibility(View.VISIBLE);
		findViewById(R.id.view_init_toggle_sign_up).setVisibility(View.VISIBLE);

		((TextView) findViewById(R.id.view_init_google)).setText("Sign in with Google");
		((TextView) findViewById(R.id.view_init_facebook)).setText("Sign in with Facebook");
	}
	
	@Override
	public void onClick(View v) {
		Log.d(TAG, "Clicked on a button");

		switch (v.getId()) {
		case R.id.view_init_google:
			if (!mPlusClient.isConnected()) {
				mPlusClient.connect();
				if (mConnectionResult == null) {
					if (!mConnectionProgressDialog.isShowing())
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
		case R.id.view_init_create_account:
			
			UserProfileActivity.setInput(mApp, null);
			Intent intent = new Intent(getBaseContext(), UserProfileActivity.class);
			this.startActivityForResult(intent, REQUEST_CODE_USER_PROFILE);

			break;
			
		case R.id.view_init_sign_in:

			new LoginDialogFragment().show(getSupportFragmentManager(),"Please log in to Bartsy");
			
			break;
			
		case R.id.view_init_toggle_sign_in:
			signInListeners();
			break;
		case R.id.view_init_toggle_sign_up:
			signUpListeners();
			break;
		}
	}

	
	/**
	 * This function is called by the user login dialog. The dialog provides a username/password and this function 
	 * gets the rest of the information for the user profile or displays a Toast if the username/password are incorrect
	 */
	
	@Override
	public void onDialogPositiveClick(LoginDialogFragment dialog) {
		// TODO Auto-generated method stub
		
		// Create a new thread to handle getting a response from the host
		
		final UserProfile user = new UserProfile();
		user.setBartsyLogin(dialog.username);
		user.setBartsyPassword(dialog.password);

		new Thread() {
			public void run() {
				UserProfile profile = WebServices.getUserProfile(mApp, user);

				if (profile == null) {
	
					// Could not log in
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(mActivity, "Could not log in. Please try again.", Toast.LENGTH_SHORT).show();
						}
					});
					return;
				} 
	
				// Logged in successfully and obtained user information. Save it.
				mApp.saveUserProfile(profile);
				
				// Synch user check-in status
				mApp.syncActiveVenue();
				
				// Sync open orders
				mApp.syncOrders();
				
				
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						
						Toast.makeText(mActivity, "Logged in as " + user.getBartsyLogin(), Toast.LENGTH_SHORT).show();

						// We got an existing user. Start profile edit activity using this user and the input
						Intent intent = new Intent().setClass(InitActivity.this, MainActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						mActivity.finish();
					}
				});
			};
		}.start();
	}
	
	
	@Override
	public void onDialogNegativeClick(LoginDialogFragment dialog) {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Cancel login", Toast.LENGTH_SHORT).show();
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
		
		super.onActivityResult(requestCode, responseCode, intent);
		uiHelper.onActivityResult(requestCode, responseCode, intent);

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
				
				// Stop Progress dialog
				if (mConnectionProgressDialog.isShowing())
					mConnectionProgressDialog.dismiss();
				 uiHelper.onActivityResult(requestCode, responseCode, intent);
				
				 Session.openActiveSession(this, true, new Session.StatusCallback() {

				        @Override
				        public void call(final Session session, SessionState state, Exception exception) {

				            if (session.isOpened()) {
				                Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
				                    @Override
				                    public void onCompleted(GraphUser user, Response response) {
				                        if (user != null) {
				                        	Log.d(TAG, response.toString());
				                            Log.d("Access_token", session.getAccessToken());
				                            // Get facebook friends list in background thread
				                            new Thread(){
				            					@Override
				            					public void run() {
				            					WebServices.getFacebookFriendList(session.getAccessToken(),InitActivity.this);	
				            					}
				                            }.start();
				                        }
				                    }
				                });
				            } else {
				                Log.d("SESSION NOT OPENED", "SESSION NOT OPENED");
								 loginButton.setSessionStatusCallback(callback);
				            }
				        }
				    });

				 loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
			            @Override
			            public void onUserInfoFetched(GraphUser user) {
			            	if(user!=null){
			                try {
								UserProfileActivity.setInput(mApp,new UserProfile(user));
							} catch (JSONException e) {
								Toast.makeText(InitActivity.this,"Could not download user details", Toast.LENGTH_SHORT).show();
								e.printStackTrace();
							}
			            	Intent userProfileintent = new Intent(getBaseContext(), UserProfileActivity.class);
			                startActivityForResult(userProfileintent, REQUEST_CODE_USER_PROFILE);
			            	}
			            }
			        });	
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
				Intent action = new Intent().setClass(InitActivity.this, MainActivity.class);
				action.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(action);
				finish();
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
			
			// Try to log the user in

			final UserProfile user = new UserProfile(mPerson, mAccountName);;

			new Thread() {
				public void run() {
					
					// User log-in syscall
					UserProfile profile = WebServices.getUserProfile(mApp, user);

					if (profile == null) {
		
						// Could not log in. We need to create a new profile based on the information obtained.
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								UserProfileActivity.setInput(mApp, user); // use the application as a buffer to pass the message to the new activity
								Intent intent = new Intent(getBaseContext(), UserProfileActivity.class);
								mActivity.startActivityForResult(intent, REQUEST_CODE_USER_PROFILE);		
							}
						});
						return;
					}

					// Got profile - save it
					mApp.saveUserProfile(profile);

					// Sync active venue
					mApp.syncActiveVenue();
					
					// Sync open orders
					mApp.syncOrders();
					
					// Start main activity.
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							Toast.makeText(mActivity, "Logged in as " + user.getGoogleUsername(), Toast.LENGTH_SHORT).show();
							Intent intent = new Intent().setClass(InitActivity.this, MainActivity.class);
							intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
							finish();
						}
					});

				};
			}.start();

		} else {
			
			// Could not load a user profile. Start a blank profile
			UserProfileActivity.setInput(mApp, null);
			Intent intent = new Intent(getBaseContext(), UserProfileActivity.class);
			mActivity.startActivityForResult(intent, REQUEST_CODE_USER_PROFILE);
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
	
}
