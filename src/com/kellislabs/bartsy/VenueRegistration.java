package com.kellislabs.bartsy;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
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
		RadioButton typeOfAuthentication = (RadioButton) findViewById(selectedWifiPresent);

		System.out.println("sumbit");
		JSONObject postData = new JSONObject();
		try {
			postData.put("locuId", locuId.getText().toString());
			//postData.put("deviceToken", value);
			postData.put("wifiName", wifiName.getText().toString());
			postData.put("wifiPassword", wifiPassword.getText().toString());
			postData.put("typeOfAuthentication", typeOfAuthentication.getText()
					.toString());
			postData.put("bankName", bankname.getText().toString());
			postData.put("accountNumber", bankAccountNo.getText().toString());
			postData.put("deviceType", "0");
			postData.put("wifiPresent", wifi.getText().toString());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
