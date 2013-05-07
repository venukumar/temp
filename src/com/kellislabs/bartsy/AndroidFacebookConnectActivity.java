package com.kellislabs.bartsy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.kellislabs.bartsy.facebook.AsyncFacebookRunner;
import com.kellislabs.bartsy.facebook.AsyncFacebookRunner.RequestListener;
import com.kellislabs.bartsy.facebook.DialogError;
import com.kellislabs.bartsy.facebook.Facebook;
import com.kellislabs.bartsy.facebook.Facebook.DialogListener;
import com.kellislabs.bartsy.facebook.FacebookError;
import com.kellislabs.bartsy.utils.Constants;
import com.kellislabs.bartsy.utils.WebServices;

public class AndroidFacebookConnectActivity extends Activity {

	// Your Facebook APP ID
	private static String APP_ID = "596602043683290"; // Replace with your App
	// ID

	// Instance of Facebook Class
	private Facebook facebook = new Facebook(APP_ID);
	private AsyncFacebookRunner mAsyncRunner;
	String FILENAME = "AndroidSSO_data";
	private SharedPreferences mPrefs;

	// Buttons
	Button btnFbLogin;
	// Button btnFbGetProfile;
	Button btnPostToWall;
	Button btnShowAccessTokens;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facebook_layout);

		btnFbLogin = (Button) findViewById(R.id.btn_fblogin);
		// btnFbGetProfile = (Button) findViewById(R.id.btn_get_profile);
		btnPostToWall = (Button) findViewById(R.id.btn_fb_post_to_wall);
		btnShowAccessTokens = (Button) findViewById(R.id.btn_show_access_tokens);
		mAsyncRunner = new AsyncFacebookRunner(facebook);

		/**
		 * Login button Click event
		 * */
		btnFbLogin.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d("Image Button", "button Clicked");
				loginToFacebook();
			}
		});
		btnFbLogin.performClick();

		// /**
		// * Getting facebook Profile info
		// * */
		// btnFbGetProfile.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// getProfileInformation();
		// }
		// });

		/**
		 * Posting to Facebook Wall
		 * */
		btnPostToWall.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				postToWall();
			}
		});

		/**
		 * Showing Access Tokens
		 * */
		btnShowAccessTokens.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				showAccessTokens();
			}
		});

	}

	/**
	 * Function to login into facebook
	 * */
	public void loginToFacebook() {

		mPrefs = getPreferences(MODE_PRIVATE);
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);

		if (access_token != null) {
			System.out.println("access token not null");
			getProfileInformation();
			facebook.setAccessToken(access_token);

			btnFbLogin.setVisibility(View.INVISIBLE);

			// Making get profile button visible
			// btnFbGetProfile.setVisibility(View.VISIBLE);

			// Making post to wall visible
			btnPostToWall.setVisibility(View.GONE);

			// Making show access tokens button visible

			btnShowAccessTokens.setVisibility(View.GONE);

			Log.d("FB Sessions", "" + facebook.isSessionValid());
		}

		if (expires != 0) {
			facebook.setAccessExpires(expires);
		}

		if (!facebook.isSessionValid()) {
			System.out.println("!facebook.isSessionValid()");
			// getProfileInformation();
			facebook.authorize(this,
					new String[] { "email", "publish_stream" },
					new DialogListener() {

						@Override
						public void onCancel() {
							// Function to handle cancel event
						}

						@Override
						public void onComplete(Bundle values) {
							// Function to handle complete event
							// Edit Preferences and update facebook acess_token
							SharedPreferences.Editor editor = mPrefs.edit();
							editor.putString("access_token",
									facebook.getAccessToken());
							editor.putLong("access_expires",
									facebook.getAccessExpires());
							editor.commit();
							getProfileInformation();

							// Making Login button invisible
							btnFbLogin.setVisibility(View.INVISIBLE);

							// Making logout Button visible
							// btnFbGetProfile.setVisibility(View.VISIBLE);

							// Making post to wall visible
							btnPostToWall.setVisibility(View.GONE);

							// Making show access tokens button visible
							btnShowAccessTokens.setVisibility(View.GONE);

						}

						@Override
						public void onError(DialogError error) {
							// Function to handle error

						}

						@Override
						public void onFacebookError(FacebookError fberror) {
							// Function to handle Facebook errors

						}

					});
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		System.out.println("on activity result ****************************");
		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	/**
	 * Get Profile information by making request to Facebook Graph API
	 * */
	public void getProfileInformation() {
		System.out.println("in get profile info");
		mAsyncRunner.request("me", new RequestListener() {
			@Override
			public void onComplete(String response, Object state) {
				Log.d("Profile", response);
				System.out.println("the response from json is :::" + response);
				String json = response;
				toSaveFBData(json);
				try {
					// Facebook Profile JSON data
					JSONObject profile = new JSONObject(json);

					// getting name of the user
					final String name = profile.getString("name");

					// getting email of the user
					final String email = profile.getString("email");

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							Toast.makeText(getApplicationContext(),
									"Name: " + name + "\nEmail: " + email,
									Toast.LENGTH_LONG).show();
						}

					});
					finish();

				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onIOException(IOException e, Object state) {
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
			}

			@Override
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
			}
		});
	}

	/**
* 
* */
	public void toSaveFBData(String response) {

		try {
			JSONObject fbProfileData = new JSONObject(response);
			// getting name of the user
			String name = fbProfileData.getString("name");

			// getting email of the user
			String email = fbProfileData.getString("email");
			// getting accessToken of the user
			String fbProfileId = fbProfileData.getString("id");
			// getting username of the user
			String userName = fbProfileData.getString("username");
			// getting gender of the user
			String gender = fbProfileData.getString("gender");

			String deviceToken = "0000000000000000000";
			String loginType = "facebook";
			int deviceType = Constants.DEVICE_Type;
			JSONObject json = new JSONObject();
			json.put("userName", userName);
			json.put("name", name);
			json.put("loginId", fbProfileId);
			json.put("loginType", loginType);
			json.put("gender", gender);
			json.put("deviceType", deviceType);
			json.put("deviceToken", deviceToken);

			try {
				String responses = WebServices.postRequest(
						Constants.URL_Post_Profile_Data, json,
						getApplicationContext());
				System.out.println("responses   " + responses);
				if (response != null) {
					String bartsyUserId = null;
					JSONObject resultJson = new JSONObject(responses);
					String errorCode = resultJson.getString("errorCode");
					String errorMessage = resultJson.getString("errorMessage");
					if (resultJson.has("bartsyUserId"))
						bartsyUserId = (String) resultJson.get("bartsyUserId");
					if (bartsyUserId != null) {

						SharedPreferences sharedPref = getSharedPreferences(
								getResources()
										.getString(
												R.string.config_shared_preferences_name),
								Context.MODE_PRIVATE);
						Resources r = getResources();

						SharedPreferences.Editor editor = sharedPref.edit();
						editor.putString(r.getString(R.string.bartsyUserId),
								bartsyUserId);
						
					}

				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Function to post to facebook wall
	 * */
	public void postToWall() {
		// post on user's wall.
		facebook.dialog(this, "feed", new DialogListener() {

			@Override
			public void onFacebookError(FacebookError e) {
			}

			@Override
			public void onError(DialogError e) {
			}

			@Override
			public void onComplete(Bundle values) {
			}

			@Override
			public void onCancel() {
			}
		});

	}

	/**
	 * Function to show Access Tokens
	 * */
	public void showAccessTokens() {
		String access_token = facebook.getAccessToken();

		Toast.makeText(getApplicationContext(),
				"Access Token: " + access_token, Toast.LENGTH_LONG).show();
	}

	/**
	 * Function to Logout user from Facebook
	 * */
	public void logoutFromFacebook() {
		mAsyncRunner.logout(this, new RequestListener() {
			@Override
			public void onComplete(String response, Object state) {
				Log.d("Logout from Facebook", response);
				if (Boolean.parseBoolean(response) == true) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// make Login button visible
							btnFbLogin.setVisibility(View.VISIBLE);

							// making all remaining buttons invisible
							// btnFbGetProfile.setVisibility(View.INVISIBLE);
							btnPostToWall.setVisibility(View.INVISIBLE);
							btnShowAccessTokens.setVisibility(View.INVISIBLE);
						}

					});

				}
			}

			@Override
			public void onIOException(IOException e, Object state) {
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
			}

			@Override
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
			}
		});
	}

}