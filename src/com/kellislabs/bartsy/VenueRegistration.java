package com.kellislabs.bartsy;

import org.json.JSONException;
import org.json.JSONObject;

import com.kellislabs.bartsy.utils.Constants;
import com.kellislabs.bartsy.utils.WebServices;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class VenueRegistration extends Activity implements OnClickListener {

	private EditText locuId, bankname, bankAccountNo, wifiName, wifiPassword;
	private RadioGroup typeOfAuthentication, wifiPresent;
	
	private Handler handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.venue_registration);

		locuId = (EditText) findViewById(R.id.locuId);
		bankname = (EditText) findViewById(R.id.bankNameEdit);
		bankAccountNo = (EditText) findViewById(R.id.bankAccount);
		wifiName = (EditText) findViewById(R.id.wifiName);
		wifiPassword = (EditText) findViewById(R.id.wifiPassword);
		typeOfAuthentication = (RadioGroup) findViewById(R.id.authentication);
		wifiPresent = (RadioGroup) findViewById(R.id.wifiPresent);

		// Setup a listener for the submit button
		findViewById(R.id.button_venue_registration_submit).setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0) {
		Intent intent = new Intent(this, VenueActivity.class);

		// Perform registration - for now assume all will go well
		registration(arg0);
		
		Log.d("Bartsy", "Clicked on submit button");
	}
	
	public void registration(View v) {

		int selectedWifiPresent = wifiPresent.getCheckedRadioButtonId();

		// Gets a reference to our "selected" radio button
		RadioButton wifi = (RadioButton) findViewById(selectedWifiPresent);

		int selectedTypeOfAuthentication = typeOfAuthentication
				.getCheckedRadioButtonId();

		// Gets a reference to our "selected" radio button
		RadioButton typeOfAuthentication = (RadioButton) findViewById(selectedTypeOfAuthentication);
		SharedPreferences settings = getSharedPreferences(
				GCMIntentService.REG_ID, 0);
		String deviceToken = settings.getString("RegId", "");

		System.out.println("sumbit");
		final JSONObject postData = new JSONObject();
		try {
			postData.put("locuId", locuId.getText().toString());
			postData.put("deviceToken", deviceToken);
			postData.put("wifiName", wifiName.getText().toString());
			postData.put("wifiPassword", wifiPassword.getText().toString());
			postData.put("typeOfAuthentication", typeOfAuthentication == null ? "" :
				typeOfAuthentication.getText().toString());
			postData.put("bankName", bankname.getText().toString());
			postData.put("accountNumber", bankAccountNo.getText().toString());
			postData.put("deviceType", "0");

			if (wifi == null? false : wifi.getText().toString().equalsIgnoreCase("Yes"))
				postData.put("wifiPresent", "1");
			else
				postData.put("wifiPresent", "0");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		new Thread() {
			public void run() {

				try {
					String response = WebServices.postRequest(
							Constants.URL_SAVE_VENUEDETAILS, postData,
							VenueRegistration.this);

					Log.d("Bartsy", "response :: " + response);

					if (response != null) {
						final JSONObject json = new JSONObject(response);
						
						handler.post(new Runnable() {
							
							@Override
							public void run() {
								try {
									int errorCode = Integer.parseInt(json.getString("errorCode"));
									String errorMessage = json.getString("errorMessage");
									String venueName = null;
									Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
									BartsyApplication app;
									switch(errorCode) {
									case 2: 
										// venue already exists - still save the profile locally for now
										venueName = "Chaya Venice";
									case 0: 
										// Save the venue id in shared preferences
										String venueId = json.getString("venueId");
										venueName = venueName == null? json.getString("venueName") : venueName;
									    SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.config_shared_preferences_name), Context.MODE_PRIVATE);
										SharedPreferences.Editor editor = sharedPref.edit();
										editor.putString("RegisteredVenueId", venueId);
										editor.putString("RegisteredVenueName", venueName);
										app = (BartsyApplication)getApplication();
										app.venueProfileID = venueId;
										app.venueProfileName = venueName;
										
										editor.commit();

										Intent intent = new Intent(VenueRegistration.this,
												VenueActivity.class);
										intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
										startActivity(intent);
										finish();
									}
								} catch (NumberFormatException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (NotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
						});
					
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}.start();
	}
}
