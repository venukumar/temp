package com.kellislabs.bartsy.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.kellislabs.bartsy.R;
import com.kellislabs.bartsy.db.DatabaseManager;
import com.kellislabs.bartsy.model.MenuDrink;
import com.kellislabs.bartsy.model.Section;

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

	public static void postOrderTOServer(Context context, MenuDrink drink) {
		Resources r = context.getResources();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		String bartsyId = prefs.getString(r.getString(R.string.bartsyUserId),
				"");
		String totalPrice = calculateTotalPrice();
		String tripPercentage = "";

		JSONObject orderData = new JSONObject();
		try {
			orderData.put("bartsyId", bartsyId);
			orderData.put("venueId", "100001");
			orderData.put("basePrice", drink.getPrice());
			orderData.put("itemId", drink.getDrinkId());
			orderData.put("itemName", drink.getTitle());
			orderData.put("tipPercentage", tripPercentage);
			orderData.put("totalPrice", totalPrice);
			orderData.put("orderStatus", "New");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static String calculateTotalPrice() {
		// TODO Auto-generated method stub

		return null;
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
