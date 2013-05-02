package com.kellislabs.bartsy.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;

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

	public static void getMenuList(Context context) {

		System.out.println("get menu list");
		String response = null;
		response = getRequest(Constants.URL_GET_BAR_LIST, context);
		if (response == null) {

		} else {
			try {
				JSONArray jsonArray = new JSONArray(response);
				System.out.println("json arrya " + jsonArray.length());
				
				for (int section = 0; section < jsonArray.length(); section++) {

					JSONObject jsonObject = jsonArray.getJSONObject(section);
					Section menuSection = null;
					if (jsonObject.has("section_name") && jsonObject.has("subsections")) {
						
						String name = jsonObject.getString("section_name");
						
						JSONArray subsections = jsonObject.getJSONArray("subsections");
						if (subsections != null && subsections.length() > 0) {
							if (name.trim().length()>0 && subsections.length()==1) {
								menuSection = new Section();
								menuSection.setName(name);
								DatabaseManager.getInstance().saveSection(menuSection);
							}
						
							for (int i = 0; i < subsections.length(); i++) {
									JSONObject subSection = subsections.getJSONObject(i);
									String subName = subSection.getString("subsection_name");
									
									if(subName.trim().length()>0){
										String newName = name+" - "+subName;
										menuSection = new Section();
										menuSection.setName(newName);
										DatabaseManager.getInstance().saveSection(menuSection);
									}
									
									JSONArray contents = subSection.getJSONArray("contents");
									for (int k = 0; k < contents.length(); k++)
									{
										MenuDrink menuDrink = new MenuDrink(
												contents.getJSONObject(k));
										menuDrink.setSection(menuSection);
										DatabaseManager.getInstance().saveDrink(menuDrink);
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
