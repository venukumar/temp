package com.vendsy.bartsy.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.util.Log;

import com.vendsy.bartsy.GCMIntentService;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.db.DatabaseManager;
import com.vendsy.bartsy.model.MenuDrink;
import com.vendsy.bartsy.model.Order;
import com.vendsy.bartsy.model.Profile;
import com.vendsy.bartsy.model.Section;

public class WebServices {

	// checking internet connection
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
	public static String postRequest(String url, JSONObject postData,
			Context context) throws Exception {

		String response = null;
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);

		String data = postData.toString();
		System.out.println("*** " + data);
		try {
			boolean status = isNetworkAvailable(context);
			if (status == true) {
				try {
					httppost.setEntity(new StringEntity(data));

					// Execute HTTP Post Request

					httppost.setHeader("Accept", "application/json");
					httppost.setHeader("Content-type", "application/json");

					HttpResponse httpResponse = httpclient.execute(httppost);

					String responseofmain = EntityUtils.toString(httpResponse
							.getEntity());
					response = responseofmain.toString();
				} catch (Exception e) {
					Log.e("log_tag", "Error in http connection" + e.toString());
					System.out.println("::: " + e.getMessage());

				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		return response;

	}

	public static String userCheckInOrOut(final Context context,
			String venueId, String url) {
		String response = null;
		SharedPreferences sharedPref = context.getSharedPreferences(
				context.getResources().getString(
						R.string.config_shared_preferences_name),
				Context.MODE_PRIVATE);
		Resources r = context.getResources();
		int bartsyId = sharedPref.getInt(r.getString(R.string.bartsyUserId), 0);

		System.out.println("bartsyId ::: " + bartsyId);
		final JSONObject json = new JSONObject();
		try {
			json.put("bartsyId", bartsyId);
			json.put("venueId", venueId);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {

			response = postRequest(url, json, context);
			System.out.println("response :: " + response);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	/*
	 * This Method i am using for each and every request which is going through
	 * get() method.
	 */
	public static String getRequest(String url, Context context) {
		System.out.println("web service calling ");
		BufferedReader bufferReader = null;
		StringBuffer stringBuffer = new StringBuffer("");
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpRequest = new HttpGet();
		String result = "";

		try {
			boolean status = isNetworkAvailable(context);
			if (status == true) {

				httpRequest.setURI(new URI(url));

				HttpResponse response = httpClient.execute(httpRequest);

				bufferReader = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				String line = "";
				String NL = System.getProperty("line.separator");
				while ((line = bufferReader.readLine()) != null) {
					stringBuffer.append(line + NL);
				}
				bufferReader.close();

			}
			result = stringBuffer.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public static void postOrderTOServer(final Context context, Order order,
			String venueID) {
		final JSONObject orderData = order.getPlaceOrderJSON();
		Resources r = context.getResources();
		SharedPreferences sharedPref = context.getSharedPreferences(
				context.getResources().getString(
						R.string.config_shared_preferences_name),
				Context.MODE_PRIVATE);
		int bartsyId = sharedPref.getInt(r.getString(R.string.bartsyUserId),
				100002);

		try {
			orderData.put("bartsyId", bartsyId);
			orderData.put("venueId", venueID);
//			orderData.put("clientOrderId", order.clientID);   - USE THIS LINE WHEN THE SERVER CODE IS READY
		} catch (JSONException e) {
			e.printStackTrace();
		}

		new Thread() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					String response;
					response = postRequest(Constants.URL_PLACE_ORDER,
							orderData, context);
					System.out.println("response :: " + response);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}

	// public static void saveProfileData(Profile bartsyProfile, Context
	// context) {
	// try {
	// // To get GCM reg ID from the Shared Preference
	// SharedPreferences settings = context.getSharedPreferences(
	// GCMIntentService.REG_ID, 0);
	// String deviceToken = settings.getString("RegId", "");
	//
	// int deviceType = Constants.DEVICE_Type;
	//
	// JSONObject json = new JSONObject();
	// json.put("userName", bartsyProfile.getUsername());
	// json.put("name", bartsyProfile.getName());
	// json.put("loginId", bartsyProfile.getSocialNetworkId());
	// json.put("loginType", bartsyProfile.getType());
	// json.put("gender", bartsyProfile.getGender());
	// json.put("deviceType", deviceType);
	// json.put("deviceToken", deviceToken);
	//
	// try {
	// String responses = WebServices.postRequest(
	// Constants.URL_POST_PROFILE_DATA, json,
	// context.getApplicationContext());
	// System.out.println("responses   " + responses);
	// if (responses != null) {
	// int bartsyUserId = 0;
	// JSONObject resultJson = new JSONObject(responses);
	// String errorCode = resultJson.getString("errorCode");
	// String errorMessage = resultJson.getString("errorMessage");
	//
	// System.out.println("error message " + errorMessage);
	// System.out.println("errorCode " + errorCode);
	//
	// if (resultJson.has("bartsyUserId")) {
	// bartsyUserId = resultJson.getInt("bartsyUserId");
	//
	// System.out.println("bartsyUserId " + bartsyUserId);
	// }
	// else
	// {
	// System.out.println("bartsyUserIdnot found");
	// }
	// if (bartsyUserId > 0) {
	// SharedPreferences sharedPref = context
	// .getSharedPreferences(
	// context.getResources()
	// .getString(
	// R.string.config_shared_preferences_name),
	// Context.MODE_PRIVATE);
	// Resources r = context.getResources();
	//
	// SharedPreferences.Editor editor = sharedPref.edit();
	// editor.putInt(r.getString(R.string.bartsyUserId),
	// bartsyUserId);
	// editor.commit();
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	//
	// }

	public static String postProfile(Profile bartsyProfile,
			Bitmap profileImage, String path, Context context) {

		String url = path;

		byte[] dataFirst = null;

		String status = null;

		int TIMEOUT_MILLISEC = 180000; // =180sec
		HttpParams my_httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(my_httpParams,
				TIMEOUT_MILLISEC); // set conn time out
		HttpConnectionParams.setSoTimeout(my_httpParams, TIMEOUT_MILLISEC); // set
		// socket
		// time
		// out

		SharedPreferences settings = context.getSharedPreferences(
				GCMIntentService.REG_ID, 0);
		String deviceToken = settings.getString("RegId", "");

		int deviceType = Constants.DEVICE_Type;

		JSONObject json = new JSONObject();
		try {
			json.put("userName", bartsyProfile.getUsername());
			json.put("name", bartsyProfile.getName());
			json.put("loginId", bartsyProfile.getSocialNetworkId());
			json.put("loginType", bartsyProfile.getType());
			json.put("gender", bartsyProfile.getGender());
			json.put("deviceType", deviceType);
			json.put("deviceToken", deviceToken);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {

			if (profileImage != null) {

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				profileImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				dataFirst = baos.toByteArray();

			}

			try {
				// String details = URLEncoder.encode(json.toString(), "UTF-8");
				// url = url + details;
				// System.out.println("details:"+details);
				HttpPost postRequest = new HttpPost(url);

				HttpClient client = new DefaultHttpClient();

				ByteArrayBody babFirst = null;

				if (dataFirst != null)
					babFirst = new ByteArrayBody(dataFirst, "userImage"
							+ ".jpg");

				MultipartEntity reqEntity = new MultipartEntity(
						HttpMultipartMode.BROWSER_COMPATIBLE);

				if (babFirst != null)
					reqEntity.addPart("userImage", babFirst);

				System.out.println("json::" + json.toString());
				if (json != null)
					reqEntity.addPart("details", new StringBody(
							json.toString(), Charset.forName("UTF-8")));

				postRequest.setEntity(reqEntity);
				HttpResponse responses = client.execute(postRequest);

				/* Checking response */

				System.out.println("********** RESPONSE **********");
				System.out.println("********** RESPONSE **********");
				System.out.println("********** RESPONSE **********");

				System.out.println(responses);

				if (responses != null) {
					String responseofmain = EntityUtils.toString(responses
							.getEntity());

					int bartsyUserId = 0;
					JSONObject resultJson = new JSONObject(responseofmain);
					String errorCode = resultJson.getString("errorCode");
					String errorMessage = resultJson.getString("errorMessage");

					System.out.println("error message " + errorMessage);
					System.out.println("errorCode " + errorCode);

					if (resultJson.has("bartsyUserId")) {
						bartsyUserId = resultJson.getInt("bartsyUserId");

						System.out.println("bartsyUserId " + bartsyUserId);
					} else {
						System.out.println("bartsyUserIdnot found");
					}
					if (bartsyUserId > 0) {
						SharedPreferences sharedPref = context
								.getSharedPreferences(
										context.getResources()
												.getString(
														R.string.config_shared_preferences_name),
										Context.MODE_PRIVATE);
						Resources r = context.getResources();

						SharedPreferences.Editor editor = sharedPref.edit();
						editor.putInt(r.getString(R.string.bartsyUserId),
								bartsyUserId);
						editor.commit();
					}
				}

				// if (responses != null) {
				// System.out.println("response not null");
				// String responseofmain = EntityUtils.toString(responses
				// .getEntity());
				//
				// System.out.println(responseofmain + " response :::: ");
				//
				// System.out.println(responseofmain);
				//
				// JSONObject jsonResponse = new JSONObject(responseofmain);
				//
				// if (jsonResponse.has("Result"))
				// status = jsonResponse.getString("Result");
				// else
				// System.out.println("not has result");
				// System.out.println("status :: " + status);
				//
				// }

			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;

	}

	public static String getVenueList(final Context context) {
		String response = null;

		response = WebServices.getRequest(Constants.URL_GET_VENU_LIST, context);
		System.out.println("response venu " + response);
		return response;
	}

	public static void getMenuList(Context context) {

		System.out.println("get menu list");
		String response = null;
		JSONObject json = new JSONObject();
		try {
			json.put("venueId", "100001");
			response = postRequest(Constants.URL_GET_BAR_LIST, json, context);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		if (response == null) {

		} else {
			try {

				JSONObject result = new JSONObject(response);
				String errorCode = result.getString("errorCode");
				String errorMessage = result.getString("errorMessage");
				String menus = result.getString("menu");

				JSONArray jsonArray = new JSONArray(menus);
				System.out.println("json arrya " + jsonArray.length());

				for (int section = 0; section < jsonArray.length(); section++) {

					JSONObject jsonObject = jsonArray.getJSONObject(section);
					Section menuSection = null;
					if (jsonObject.has("section_name")
							&& jsonObject.has("subsections")) {

						String name = jsonObject.getString("section_name");

						JSONArray subsections = jsonObject
								.getJSONArray("subsections");
						if (subsections != null && subsections.length() > 0) {
							if (name.trim().length() > 0
									&& subsections.length() == 1) {
								menuSection = new Section();
								if (name.length() > 0)
									menuSection.setName(name);
								DatabaseManager.getInstance().saveSection(
										menuSection);
							}

							for (int i = 0; i < subsections.length(); i++) {
								JSONObject subSection = subsections
										.getJSONObject(i);
								String subName = subSection
										.getString("subsection_name");

								if (subName.trim().length() > 0) {
									String newName = name + " - " + subName;
									menuSection = new Section();
									menuSection.setName(newName);
									DatabaseManager.getInstance().saveSection(
											menuSection);
								}

								JSONArray contents = subSection
										.getJSONArray("contents");
								for (int k = 0; k < contents.length(); k++) {
									MenuDrink menuDrink = new MenuDrink(
											contents.getJSONObject(k));
									menuDrink.setSection(menuSection);
									DatabaseManager.getInstance().saveDrink(
											menuDrink);
								}
							}
						}
					}

				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}
}
