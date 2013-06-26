package com.vendsy.bartsy.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.GCMIntentService;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.VenueActivity;
import com.vendsy.bartsy.model.Order;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.model.Venue;

public class WebServices {

	private static final String TAG = "WebServices";

	/**
	 * To check internet connection
	 * 
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static boolean isNetworkAvailable(Context context) throws Exception {

		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isFailover())
			return false;
		else if (cm.getActiveNetworkInfo() != null

		&& cm.getActiveNetworkInfo().isAvailable()
				&& cm.getActiveNetworkInfo().isConnected()) {

			return true;

		}

		else {

			return false;
		}

	}

	/**
	 * Create a new HttpClient and Post data
	 * 
	 * @param url
	 * @param postData
	 * @param context
	 * @return
	 * @throws Exception
	 */
	public static String postRequest(String url, JSONObject postData, Context context) throws Exception {

		String response = null;
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		
		// added apiVersion
		postData.put("apiVersion", Constants.API_VERSION);
		String data = postData.toString();
		
		Log.i(TAG,"===> postRequest("+ url  + ", " + data + ")");

		try {
			boolean status = isNetworkAvailable(context);
			if (status == true) {
				try {
					httppost.setEntity(new StringEntity(data));

					// Execute HTTP Post Request

					httppost.setHeader("Accept", "application/json");
					httppost.setHeader("Content-type", "application/json");

					HttpResponse httpResponse = httpclient.execute(httppost);

					String responseofmain = EntityUtils.toString(httpResponse.getEntity());
					response = responseofmain.toString();
					
					Log.i(TAG,"<=== postRequest response:"+ response);
					
				} catch (Exception e) {
					Log.e(TAG, "Error in http connection" + e.toString());
					Log.v(TAG, "Exception found ::: " + e.getMessage());

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Error in http connection" + e.toString());
			Log.e(TAG, "Error in http connection" + e.toString());
		}
		
		return response;
	}

	
	/**
	 * Service call for user check in and check out
	 * 
	 * @param url
	 * @param context
	 * @return
	 */
	public static String userCheckInOrOut (final Context context, String bartsyID, String venueId, String url) {
		String response = null;
		SharedPreferences sharedPref = context.getSharedPreferences(
				context.getResources().getString(
						R.string.config_shared_preferences_name),
				Context.MODE_PRIVATE);
		Resources r = context.getResources();

		Log.v(TAG, "bartsyId ::: " + bartsyID);
		final JSONObject json = new JSONObject();
		try {
			json.put("bartsyId", bartsyID);
			json.put("venueId", venueId);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {

			response = postRequest(url, json, context);
			Log.v(TAG, "CheckIn or Check Out response :: " + response);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	
	/**
	 * 
	 * Post the response to a heartbeat PN. The response simply indicates to the server
	 * the app is alive, connected to the internet, able to process PN's and able to send
	 * responses back. If the server does not receive the response for some period of time
	 * (currently 15 min), the server will check the user out of the venue.
	 * 
	 */
	
	public static void postHeartbeatResponse (final Context context, 
			String bartsyId, String venueId) {
		
		Log.v(TAG, "WebService.postHeartbeatResponse()");
		final JSONObject json = new JSONObject();

		// Prepare syscall
		try {
			json.put("bartsyId", bartsyId);
			json.put("venueId", venueId);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// Invoke syscall
		try {
			// We are not interested in the response to the syscall as the server is what's
			// checking to see if everything is working property. If the server doesn't see
			// the heartbeat it will check the user out. 
			postRequest(Constants.URL_HEARTBEAT_RESPONSE, json, context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Service call for post order. 
	 * 
	 * @param context
	 * @param order
	 * @param venueID
	 * @param handler - this handler handles error events. It needs to be in the calling activity
	 * @return	true	- failed to send syscall
	 * 			false	- successfully sent syscall
	 */
	public static boolean postOrderTOServer(final BartsyApplication app, final Order order, 
			String venueID, final Handler processOrderDataHandler) {
		final Context context = app.getApplicationContext();
		final JSONObject orderData = order.getPlaceOrderJSON();
		
		String bartsyId = app.loadBartsyId();

		// Prepare syscall 
		try {
			orderData.put("bartsyId", bartsyId);
			orderData.put("venueId", venueID);
			if(order.orderReceiver!=null){
				orderData.put("recieverBartsyId", order.orderReceiver.getBartsyId());
			}else{
				orderData.put("recieverBartsyId", bartsyId);
			}
			orderData.put("specialInstructions", "");

		} catch (JSONException e) {
			e.printStackTrace();
			return true;
		}

		// Place order webservice call in background
		new Thread() {
			@Override
			public void run() {
				
				Message msg = processOrderDataHandler.obtainMessage(VenueActivity.HANDLE_ORDER_RESPONSE_FAILURE);
				
				try {
					String response = postRequest(Constants.URL_PLACE_ORDER, orderData, context);
					Log.v(TAG, "Post order to server response :: " + response);
					
												
					JSONObject json = new JSONObject(response);
					String errorCode = json.getString("errorCode");
					
					if (errorCode.equalsIgnoreCase("0")) {
						// Error code 0 means the order was placed successfullly. Set the serverID of the order from the syscall.
						response = "success";
						order.serverID = json.getString("orderId");
						msg = processOrderDataHandler.obtainMessage(VenueActivity.HANDLE_ORDER_RESPONSE_SUCCESS);
						
						// Add order to the list and update views. This is a synchronized operation in case multiple threads are stepping on each other
						app.addOrder(order);

						// Increment the local order count
						app.mOrderIDs++;
						
					} else if (errorCode.equalsIgnoreCase("1")) {
						// Error code 1 means the venue doesn't accept orders.
						response = json.getString("errorMessage");
						msg = processOrderDataHandler.obtainMessage(VenueActivity.HANDLE_ORDER_RESPONSE_FAILURE_WITH_CODE, response);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				// Send the response back to the caller's response handler
				processOrderDataHandler.sendMessage(msg);
			}
		}.start();

		return false;
	}
	

	/**
	 * @methodName: postProfile
	 * 
	 *              Service call for profile information
	 * 
	 * @param user
	 * @param profileImage
	 * @param path
	 * @param context
	 * @return
	 */
	public static JSONObject postProfile(UserProfile user, String path, Context context) {

		String url = path;
		byte[] dataFirst = null;

		// Setup connection parameters
		int TIMEOUT_MILLISEC = 10000; // = 10 seconds
		HttpParams my_httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(my_httpParams, TIMEOUT_MILLISEC); 
		HttpConnectionParams.setSoTimeout(my_httpParams, TIMEOUT_MILLISEC); 

		// get registration id from shared preferences
		SharedPreferences settings = context.getSharedPreferences(
				GCMIntentService.REG_ID, 0);

		// Created a json object for posting data to server
		JSONObject json = new JSONObject();
		try {
			
			// Required system parameter
			json.put("deviceType", String.valueOf(Constants.DEVICE_Type));
			json.put("deviceToken", settings.getString("RegId", ""));
			// added apiVersion
			json.put("apiVersion", Constants.API_VERSION);
			
/*
 			// Make sure the profile has login information and add it
			if (user.hasLogin() && user.hasPassword()) {
				json.put("bartsyLogin", user.getLogin());
				json.put("bartsyPassword", user.getPassword());
				return null;
			} else if (user.hasFacebookUsername() && user.hasFacebookId()) {
				json.put("facebookUserName", user.getFacebookUsername());
				json.put("facebookId", user.getFacebookId());
			} else if (user.hasGoogleUsername() && user.hasGoogleId()) {
				json.put("googleUserName", user.getGoogleUsername());
				json.put("googleId", user.getGoogleId());
			} else {
				Log.e(TAG, "Missing all login information");
				return null;				
			}
*/

			// Required parameters (user)
			if (!user.hasNickname()) {
				Log.e(TAG, "Missing nickname");
				return null;				
			} else {
				json.put("nickname", user.getNickname());
			}

			// Set up social network connections
			if (user.hasBartsyLogin()) 
				json.put("bartsyLogin", user.getBartsyLogin());
			if (user.hasPassword()) 
				json.put("bartsyPassword", user.getPassword());
			if (user.hasBartsyId()) 
				json.put("bartsyId", user.getBartsyId());
			if (user.hasFacebookUsername() && user.hasFacebookId()) {
				json.put("facebookUserName", user.getFacebookUsername());
				json.put("facebookId", user.getFacebookId());
			}
			if (user.hasGoogleUsername() && user.hasGoogleId()) {
				json.put("googleUserName", user.getGoogleUsername());
				json.put("googleId", user.getGoogleId());
			}
			
			// Optional parameters (user)
			if (user.hasEmail())
				json.put("email", user.getEmail());
			if (user.hasVisibility())
				json.put("showProfile", user.getVisibility());
			if (user.hasName())
				json.put("name", user.getName());
			if (user.hasFirstName())
				json.put("firstname", user.getFirstName());
			if (user.hasLastName())
				json.put("lastname", user.getLastName());
			if (user.hasBirthday())
				json.put("dateofbirth", user.getBirthday());
			if (user.hasStatus())
				json.put("status", user.getStatus()); 
			if (user.hasOrientation())
				json.put("orientation", user.getOrientation());
			if (user.hasDescription())
				json.put("description", user.getDescription());
			if (user.hasGender()) 
				json.put("gender", user.getGender());
			if (user.hasCreditCardNumber()) 
				json.put("creditCardNumber", user.getCreditCardNumber());
			if (user.hasExpMonth())
				json.put("expMonth", user.getExpMonth());
			if (user.hasExpYear())	
				json.put("expYear", user.getExpYear());
			
		} catch (JSONException e1) {
			e1.printStackTrace();
			return null;
		}

		try {

			// Converting profile bitmap image into byte array
			if (user.hasImage()) {
				// Image found - converting it to a byte array and adding to syscall

				Log.i(TAG,"===> postRequestMultipart("+ url  + ", " + json + ")");

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				user.getImage().compress(Bitmap.CompressFormat.JPEG, 100, baos);
				dataFirst = baos.toByteArray();


			} else {
				// Could not find image
				Log.i(TAG,"===> postRequest("+ url  + ", " + json + ")");
			}

			// String details = URLEncoder.encode(json.toString(), "UTF-8");
			// url = url + details;

			// Execute HTTP Post Request

			HttpPost postRequest = new HttpPost(url);
			HttpClient client = new DefaultHttpClient();
			ByteArrayBody babFirst = null;

			if (dataFirst != null)
				babFirst = new ByteArrayBody(dataFirst, "userImage" + ".jpg");

			MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			

			// added profile image into MultipartEntity

			if (babFirst != null)
				reqEntity.addPart("userImage", babFirst);
			if (json != null)
				reqEntity.addPart("details", new StringBody(json.toString(), Charset.forName("UTF-8")));
			postRequest.setEntity(reqEntity);
			HttpResponse responses = client.execute(postRequest);

			// Check response 

			if (responses != null){
				
				String responseofmain = EntityUtils.toString(responses.getEntity());
				Log.v(TAG, "postProfileResponseChecking " + responseofmain);
				JSONObject resultJson = new JSONObject(responseofmain);
				
				Log.i(TAG,"<=== postRequest response: " + resultJson);

				
				return resultJson;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}


	/**
	 * @methodName : getVenueList
	 * 
	 *             To get venue list from server
	 * 
	 * @param context
	 * @return list of venues
	 */
	public static String getVenueList(final Context context, String string) {
		String response = null;
		try {
			JSONObject json = new JSONObject();
			json.put("bartsyId", string);
			response = WebServices.postRequest(Constants.URL_GET_VENU_LIST, json, context);
		} catch (Exception e) {
			Log.v(TAG, "Error venu list " + e.getMessage());
		}
		Log.v(TAG, "response venu list " + response);
		return response;
	}
	
	
	public static String getIngredients(final Context context, String venueID){
		String response = null;
		JSONObject json = new JSONObject();
		try {
			json.put("venueId", venueID);
			response = postRequest(Constants.URL_GET_INGREDIENTS, json, context);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		Log.v(TAG, "response Ingredient list " + response);
		return response;
	}
	

	/**
	 * methodName : getUserOrdersList
	 * 
	 * @return ordersList
	 */

	public static String getUserOrdersList(BartsyApplication app) {

		String response = null;
		try {

			JSONObject postData = new JSONObject();
			postData.put("bartsyId", app.loadBartsyId());
			response = WebServices.postRequest(Constants.URL_LIST_OF_USER_ORDERS, postData, app.getApplicationContext());

		} catch (Exception e) {
			Log.v(TAG, "getUserOdersList Exception found " + e.getMessage());
			return null;
		}
		return response;
	}

	/**
	 * To get list of menu list
	 * 
	 * @param context
	 * @param venueID
	 */
	public static String getMenuList(Context context, String venueID) {

		Log.v(TAG, "getting menu for venue: " + venueID);

		String response = null;
		JSONObject json = new JSONObject();
		try {
			json.put("venueId", venueID);
			response = postRequest(Constants.URL_GET_BAR_LIST, json, context);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return response;
	}
	
	/**
	 * To get list of menu list
	 * 
	 * @param context
	 * @param venueID
	 */
	public static String updateOfferDrinkStatus(Context context, String venueID, Order order, int orderStatus, String barstyId) {

		Log.v(TAG, "To update offer drink: " + venueID);

		String response = null;
		JSONObject json = new JSONObject();
		try {
			json.put("venueId", venueID);
			json.put("orderId", order.serverID);
			json.put("bartsyId", barstyId);
			json.put("orderStatus", String.valueOf(orderStatus));
			response = postRequest(Constants.URL_UPDATE_OFFERED_DRINK, json, context);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return response;
	}

	
	/**
	 * Get user profile using username/password or one of the social network ID's as the key. If present, 
	 * username password take priority. 
	 * 
	 * @param context 
	 * @param venueID
	 * @param userProfile: this is used for username/password or one of the social network ID's 
	 * 
	 * Return either a new profile with the parameters returned from the host or null if an error occurred or the
	 * profile doesn't exist
	 */
	public static UserProfile getUserProfile(Context context, UserProfile login) {

		Log.v(TAG, "userLogin()");

		try {

			JSONObject json = new JSONObject();
			
			// Setup call parameters
			if (login.hasBartsyId())
				json.put("bartsyId", login.getBartsyId());
			if (login.hasBartsyLogin() && login.hasPassword()) {
				// Login with username/password
				json.put("bartsyLogin", login.getBartsyLogin());
				json.put("bartsyPassword", login.getPassword());
			} 
			if (login.hasFacebookUsername() && login.hasFacebookId()) {
				json.put("facebookUserName", login.getFacebookUsername());
				json.put("facebookId", login.getFacebookId());
			}
			if (login.hasGoogleUsername() && login.hasGoogleId()) {
				json.put("googleUserName", login.getGoogleUsername());
				json.put("googleId", login.getGoogleId());
			} 

			// Place API call and check for errors
			JSONObject result = new JSONObject(postRequest(Constants.URL_GET_USER_PROFILE, json, context));
			String errorCode = result.getString("errorCode");

			// Parse response if no error
			if (errorCode.equalsIgnoreCase("0")) {
				UserProfile user = new UserProfile();

				
				if (result.has("bartsyLogin"))
					user.setBartsyLogin(result.getString("bartsyLogin"));
				if (result.has("bartsyPassword"))
					user.setBartsyPassword(result.getString("bartsyPassword"));
				if (result.has("bartsyId"))
					user.setBartsyId(result.getString("bartsyId"));

				if (result.has("googleUserName"))
					user.setGoogleUsername(result.getString("googleUserName"));
				if (result.has("googleId"))
					user.setGoogleId(result.getString("googleId"));

				if (result.has("facebookUserName"))
					user.setFacebookUsername(result.getString("googleUserName"));
				if (result.has("facebookId"))
					user.setFacebookId(result.getString("facebookId"));

				
				if (result.has("name"))
					user.setName(result.getString("name"));
				if (result.has("firstname"))
					user.setFirstName(result.getString("firstname"));
				if (result.has("lastname"))
					user.setLastName(result.getString("lastname"));
				if (result.has("dateofbirth"))
					user.setBirthday(result.getString("dateofbirth"));
				if (result.has("dateofbirth"))
					user.setBirthday(result.getString("dateofbirth"));
				if (result.has("description"))
					user.setDescription(result.getString("description"));
				if (result.has("gender"))
					user.setGender(result.getString("gender"));
				if (result.has("nickname"))
					user.setNickname(result.getString("nickname"));
				if (result.has("orientation"))
					user.setOrientation(result.getString("orientation"));
				if (result.has("showProfile"))
					user.setVisibility(result.getString("showProfile"));
				if (result.has("status"))
					user.setStatus(result.getString("status"));
				if (result.has("userImage"))
					user.setImagePath(result.getString("userImage"));
				if (result.has("creditCardNumber"))
					user.setCreditCardNumber(result.getString("creditCardNumber"));
				if (result.has("expMonth"))
					user.setExpMonth(result.getString("expMonth"));
				if (result.has("expYear"))
					user.setExpYear(result.getString("expYear"));
					
				return user;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return null;
	}

	

	/**
	 * Get user profile using username/password or one of the social network ID's as the key. If present, 
	 * username password take priority. 
	 * 
	 * @param context 
	 * @param venueID
	 * @param userProfile: this is used for username/password or one of the social network ID's 
	 * 
	 * Return either a new profile with the parameters returned from the host or null if an error occurred or the
	 * profile doesn't exist
	 */
	public static Venue syncUserDetails(BartsyApplication context, UserProfile user) {

		Log.v(TAG, "userLogin()");

		// For now only load the venue from preference as this syscall is BROKEN!!!
//		return context.loadActiveVenue();
		
		
		try {

			JSONObject json = new JSONObject();
			
			// Required system parameter
			SharedPreferences settings = context.getSharedPreferences(GCMIntentService.REG_ID, 0);
			json.put("deviceType", String.valueOf(Constants.DEVICE_Type));
			json.put("deviceToken", settings.getString("RegId", ""));
			
			
			// Setup call parameters
			if (user.hasBartsyId())
				json.put("bartsyId", user.getBartsyId());
			if (user.hasBartsyLogin() && user.hasPassword()) {
				json.put("userName", user.getBartsyLogin());
			} else if (user.hasFacebookUsername() && user.hasFacebookId()) {
				json.put("userName", user.getFacebookUsername());
			} else if (user.hasGoogleUsername() && user.hasGoogleId()) {
				json.put("userName", user.getGoogleUsername());
			} 
			
			// Set the syscall type
			json.put("type", "login");
			
			// Place API call and check for errors
			JSONObject result = new JSONObject(postRequest(Constants.URL_SYNC_USER_DETAILS, json, context));
			String errorCode = result.getString("errorCode");

			// Parse response if no error
			if (errorCode.equalsIgnoreCase("0")) {

				if (result.has("venueId") && result.has("venueName")) {
					// We are checked in - return the venue details
					
					Venue venue = new Venue();
					venue.setId(result.getString("venueId"));
					venue.setName(result.getString("venueName"));
					
					if (result.has("userCount"))
						venue.setUserCount(result.getInt("userCount"));

					return venue;
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		return null;
		
		
	}

	
	/**
	 * To get past orders based on venue profile
	 * 
	 * @param context
	 * @param venueID
	 * @return
	 */
	public static String getPastOrders(Context context, String bartsyId, String venueID){

		JSONObject postData = new JSONObject();
		String response = null;
		try {
			postData.put("bartsyId", bartsyId);
			postData.put("venueId", venueID);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		try {
			response = WebServices.postRequest(
					Constants.URL_GET_PAST_ORDERS, postData,
					context);
		} catch (Exception e) {
		}
		
		return response;
	}
	
	
	/**
	 * @methodName : downloadImage
	 * 
	 *             To download the image from server and set image bitmap to
	 *             imageView
	 * 
	 * @param fileUrl
	 * @param model
	 * @param imageView
	 */
	public static void downloadImage(final String fileUrl, final Object model, final ImageView imageView) {

		new AsyncTask<String, Void, Bitmap>() {
			Bitmap bmImg;

			protected void onPreExecute() {
				super.onPreExecute();

			}

			protected Bitmap doInBackground(String... params) {

				Log.v("file Url: ", fileUrl);
				URL myFileUrl = null;
				try {
					myFileUrl = new URL(fileUrl);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				// Webservice call to get the image bitmap from the image url
				try {

					HttpURLConnection conn = (HttpURLConnection) myFileUrl
							.openConnection();
					conn.setDoInput(true);
					conn.connect();
					InputStream is = conn.getInputStream();
					bmImg = BitmapFactory.decodeStream(is);

				} catch (IOException e) {
					e.printStackTrace();
					bmImg = null;
				}

				return bmImg;
			}

			protected void onPostExecute(Bitmap result) {
				
				// Make sure we got an image
				if (result != null) {

					if (model!=null && model instanceof UserProfile) {
						UserProfile profile = (UserProfile) model;
						profile.setImage(result);
					}
					
					imageView.setImageBitmap(result);
					// Set bitmap image to profile image view
					imageView.setTag(result);
				}
			}

		}.execute();

	}

	
	// alert box
	public static void alertbox(final String message, final Activity a) {

		new AlertDialog.Builder(a).setMessage(message).setCancelable(false)
				.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {

						return;
					}
				}).show();
	}

}
