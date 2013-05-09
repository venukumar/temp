package com.kellislabs.bartsy;

import org.json.JSONException;
import org.json.JSONObject;

import com.kellislabs.bartsy.utils.Constants;
import com.kellislabs.bartsy.utils.WebServices;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class VenueRegistration extends Activity {

	private EditText locuId, bankname, bankAccountNo, wifiName, wifiPassword;
	private RadioGroup typeOfAuthentication, wifiPresent;

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
			postData.put("typeOfAuthentication", typeOfAuthentication.getText()
					.toString());
			postData.put("bankName", bankname.getText().toString());
			postData.put("accountNumber", bankAccountNo.getText().toString());
			postData.put("deviceType", "0");

			if (wifi.getText().toString().equalsIgnoreCase("Yes"))

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

					System.out.println("response :: " + response);

					if (response != null) {
						JSONObject json = new JSONObject(response);
						String errorCode = json.getString("errorCode");
						String errorMessage = json.getString("errorMessage");
						if (errorCode.equals("0")) {
							String venueId = json.getString("venueId");
							((BartsyApplication) getApplication()).selectedVenueId = Integer
									.valueOf(venueId);

							Intent intent = new Intent(VenueRegistration.this,
									VenueActivity.class);
							startActivity(intent);
							finish();
						}
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}.start();

	}
}
