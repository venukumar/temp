package com.vendsy.bartsy;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Profile;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.plus.model.people.Person;
import com.vendsy.bartsy.dialog.ProfileDialogFragment;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.Utilities;
import com.vendsy.bartsy.utils.WebServices;

public class UserProfileActivity extends Activity implements OnClickListener {

	private static final String TAG = "UserProfileActivity";
	
	BartsyApplication mApp = null ; // pointer to the application used as an input/output buffer to this activity
	
	EditText locuId, paypal, wifiName, wifiPassword,orderTimeOut;
	private RadioGroup typeOfAuthentication, wifiPresent;

	private LinearLayout wifiNameLinear, wifiTypeLinear, wifiPasswordLinear;
	
	// Progress dialog
	private ProgressDialog progressDialog;
	
	static final int MY_SCAN_REQUEST_CODE = 23453; // used here only, just some random unique number


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		Log.v(TAG, "onCreate()");
		
		// Set up pointer to activity used as an input/output buffer
		mApp = (BartsyApplication) getApplication();
		Person person = mApp.mUser;

		// Set the base view then pre-populate it with any existing values found in the application input buffer (mUser)
		setContentView(R.layout.user_profile);

		
		// Pre-populate fields if there is a user object already
		if (person != null) {
			// Set first name, last name, nickname, description, email, user image
			if(person.getName()!=null && person.getName().hasGivenName())
				((TextView) findViewById(R.id.view_profile_first_name)).setText(person.getName().getGivenName());
			if(person.getName()!=null && person.getName().hasFamilyName())
				((TextView) findViewById(R.id.view_profile_last_name)).setText(person.getName().getFamilyName());
			if (person.getNickname() != null) 
				((TextView) findViewById(R.id.view_profile_nickname)).setText(person.getNickname());
			else
				// If no Nickname, use the peron's first name
				((TextView) findViewById(R.id.view_profile_nickname)).setText(
						((TextView) findViewById(R.id.view_profile_first_name)).getText());
			if (person.hasAboutMe())
				((TextView) findViewById(R.id.view_profile_description)).setText(person.getAboutMe());
			if (mApp.mUserEmail != null) 
				((TextView) findViewById(R.id.view_profile_email)).setText(mApp.mUserEmail);
			if (mApp.mUser.getImage() != null) 
				// User profile has an image - display it asynchronously and also set it up in the output buffer upon success
				new DownloadImageTask().execute((ImageView) findViewById(R.id.view_profile_user_image));
		}
		
		// Set up image controllers
		findViewById(R.id.view_profile_checkbox_details).setOnClickListener(this);
		findViewById(R.id.view_profile_user_image).setOnClickListener(this);
		findViewById(R.id.view_profile_button_cancel).setOnClickListener(this);
		findViewById(R.id.view_profile_button_submit).setOnClickListener(this);
		findViewById(R.id.view_profile_button_cc_add).setOnClickListener(this);
		findViewById(R.id.view_profile_button_cc_replace).setOnClickListener(this);
		findViewById(R.id.view_profile_button_cc_delete).setOnClickListener(this);
		

		
		// Try to get all form elements from the XML
/*		locuId = (EditText) findViewById(R.id.locuId);
		paypal = (EditText) findViewById(R.id.paypalEdit);
		wifiPassword = (EditText) findViewById(R.id.wifiPassword);
		typeOfAuthentication = (RadioGroup) findViewById(R.id.authentication);
		wifiPresent = (RadioGroup) findViewById(R.id.wifiPresent);
		orderTimeOut = (EditText) findViewById(R.id.orderTimeOut);

		wifiNameLinear = (LinearLayout) findViewById(R.id.wifiNameLinear);
		wifiTypeLinear = (LinearLayout) findViewById(R.id.wifiTypeLinear);
		wifiPasswordLinear = (LinearLayout) findViewById(R.id.wifiPasswordLinear);

		// Setup a listener for the submit button
		findViewById(R.id.button_venue_registration_submit).setOnClickListener(
				this);
		// Setup on check listener for the check box
		wifiPresent.setOnCheckedChangeListener(new OnCheckedChangeListener() {

		
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// Invoke this method when the check box selected or unselected
				RadioButton selectedButton = (RadioButton) findViewById(checkedId);
				String name = selectedButton.getText().toString();
				// To hide the wifi information when user selects "no"
				if (name.equalsIgnoreCase("No")) {
					wifiNameLinear.setVisibility(View.GONE);
					wifiTypeLinear.setVisibility(View.GONE);
					wifiPasswordLinear.setVisibility(View.GONE);
				} 
				// Set the wifi information visibility true when user selects "Yes"
				else
				{
					wifiNameLinear.setVisibility(View.VISIBLE);
					wifiTypeLinear.setVisibility(View.VISIBLE);
					wifiPasswordLinear.setVisibility(View.VISIBLE);
				}

			}
		});
		
		*/
	}

	
	private static final int SELECT_PHOTO = 100;
	
	@Override
	public void onClick(View arg0) {

		
		Log.v(TAG, "onClick()");

		switch (arg0.getId()) {
		
		case R.id.view_profile_user_image:
			Log.v(TAG, "Clicked on image");
			Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
			photoPickerIntent.setType("image/*");
			startActivityForResult(photoPickerIntent, SELECT_PHOTO); 
			return;
		case R.id.view_profile_checkbox_details:
			Log.v(TAG, "Clicked on details checkbox");
			if (((CheckBox) arg0).isChecked()) {
				findViewById(R.id.view_profile_details).setVisibility(View.VISIBLE);
			} else {
				findViewById(R.id.view_profile_details).setVisibility(View.GONE);				
			}
			break;
		case R.id.view_profile_button_cc_add:
			// For now directly call card.io. This should be separate activity
			// that allows to edit credit cards
			Intent scanIntent = new Intent(this, CardIOActivity.class);

			// required for authentication with card.io
			scanIntent.putExtra(CardIOActivity.EXTRA_APP_TOKEN, getResources()
					.getString(R.string.config_cardio_token));

			// customize these values to suit your needs.
			scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true);
			scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false);
			scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_ZIP, false); 

			// MY_SCAN_REQUEST_CODE is arbitrary and is only used within this activity.
			startActivityForResult(scanIntent, MY_SCAN_REQUEST_CODE);
			break;
		case R.id.view_profile_button_cancel:
			// On cancel, just quit the activity. The underlying object should be unchanged
			this.setResult(InitActivity.RESULT_CANCELED);
			finish();
			return;
		case R.id.view_profile_button_cc_delete:
			
			findViewById(R.id.view_profile_has_cc).setVisibility(View.GONE);
			findViewById(R.id.view_profile_no_cc).setVisibility(View.VISIBLE);

			break;
		case R.id.view_profile_button_submit:
			// Check and save modifications. If all goes well, finish activity, if not stay.
			UserProfile profile = validateProfileData();
	
			if (profile != null) {
				// The new form contains valid data - accept it and return with result OK
				Log.v(TAG, "Form valid - save profile data");
				mApp.mUserProfile = profile;
				this.setResult(InitActivity.RESULT_OK);
				finish();
			}
			else {
				// Something is invalide and the user has been warned - don't go anywhere
				Log.v(TAG, "Error in profile form");
			}			
			return;
		}
		
        return;
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
	    super.onActivityResult(requestCode, resultCode, data); 

	    switch(requestCode) { 
	    case SELECT_PHOTO:
	        if(resultCode == RESULT_OK){  
	            Uri selectedImage = data.getData();
				
				// Down-sample selected image to make sure we don't get exceptions
				Bitmap bitmap = null;
				try {
	            	bitmap = decodeUri(selectedImage);
				} catch (FileNotFoundException e) {
					// Failure - don't change the user image
					e.printStackTrace();
					Log.e(TAG, "Failed to downsample image");
					return;
				}

				// Display the image and set the tag to the bitmap, indicating it's a valid profile picture.
	            ((ImageView) findViewById(R.id.view_profile_user_image)).setImageBitmap(bitmap);
	            ((ImageView) findViewById(R.id.view_profile_user_image)).setTag(bitmap);
	        }
	        break;

	    case MY_SCAN_REQUEST_CODE:
				String resultDisplayStr;
				if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
					CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

					// Never log a raw card number. Avoid displaying it, but if necessary use getFormattedCardNumber()
					resultDisplayStr = "Card Number: " + scanResult.getRedactedCardNumber() + "\n";

					// Do something with the raw number, e.g.: myService.setCardNumber( scanResult.cardNumber );
					if (scanResult.isExpiryValid()) {
						resultDisplayStr += "Expiration Date: " + scanResult.expiryMonth + "/" + scanResult.expiryYear + "\n";
					} else {
						Toast.makeText(this, "Credit card expiration date invalid", Toast.LENGTH_SHORT).show();
						return;
					}

					if (scanResult.cvv != null) {
						// Never log or display a CVV
						resultDisplayStr += "CVV has " + scanResult.cvv.length() + " digits.\n";
//					} else {
//						Toast.makeText(this, "Credit card CVV code missing", Toast.LENGTH_SHORT).show();
//						return;
					}

					if (scanResult.zip != null) {
						resultDisplayStr += "Zip: " + scanResult.zip + "\n";
					}
					
					// TODO - save credit card info
					
					// Display credit card info
					((TextView) findViewById(R.id.view_profile_cc_type)).setText(GetCreditCardType(scanResult.getFormattedCardNumber()));
					((TextView) findViewById(R.id.view_profile_cc_number_redacted)).setText(scanResult.getRedactedCardNumber());
					findViewById(R.id.view_profile_has_cc).setVisibility(View.VISIBLE);
					findViewById(R.id.view_profile_no_cc).setVisibility(View.GONE);
					
				} else {
					Toast.makeText(this, "Credit card scan was cancelled", Toast.LENGTH_SHORT).show();
					return;
				}
			break;
	    
	    }
	}
	
	
	 public String GetCreditCardType(String CreditCardNumber)
	    {
	        String regVisa = "^4[0-9]{12}(?:[0-9]{3})?$";
	        String regMaster = "^5[1-5][0-9]{14}$";
	        String regExpress = "^3[47][0-9]{13}$";
	        String regDiners = "^3(?:0[0-5]|[68][0-9])[0-9]{11}$";
	        String regDiscover = "^6(?:011|5[0-9]{2})[0-9]{12}$";
	        String regJSB = "^(?:2131|1800|35\\d{3})\\d{11}$";


	        if(CreditCardNumber.matches(regVisa))
	            return "VISA";
	        if (CreditCardNumber.matches(regMaster))
	            return "MasterCard";
	        if (CreditCardNumber.matches(regExpress))
	            return "AMEX";
	        if (CreditCardNumber.matches(regDiners))
	            return "DinersS";
	        if (CreditCardNumber.matches(regDiscover))
	            return "Discover";
	        if (CreditCardNumber.matches(regJSB))
	            return "JSB";
	        return "Credit card:";
	    }
	
	private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {

        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 140;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
               || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);

    }
	
	/**
	 * Invokes this method when the user clicks on the Register Button
	 */
	public UserProfile validateProfileData() {
		
		UserProfile user = new UserProfile();
		
		// Set fields not present in the form such as username etc.
		if (mApp.mUser == null) {
			// The user input buffer is not set up, this mean we started with a blank profile
			user.setUsername(((TextView) findViewById(R.id.view_profile_email)).getText().toString());		// use email as the username
			user.setSocialNetworkId(((TextView) findViewById(R.id.view_profile_email)).getText().toString());		// use email as the username
			user.setType("bartsy");
		} else {
			// For now we haven't wired the Facebook button, so it's G+ or nothing...
			user.setUsername(mApp.mUser.getId());
			user.setSocialNetworkId(mApp.mUser.getId());
			user.setType("google");
		}		
		
		// Validate and save first name, last name and email
		String first_name = ((TextView) findViewById(R.id.view_profile_first_name)).getText().toString();
		String last_name = ((TextView) findViewById(R.id.view_profile_last_name)).getText().toString();
		String email = ((TextView) findViewById(R.id.view_profile_email)).getText().toString();
		if (first_name.length() > 0 && last_name.length() > 0 && email.length() > 0) {
			user.setName(first_name + " " + last_name);
			user.setFirstName(first_name);
			user.setLastName(last_name);
			user.setEmail(email);
		} else {
			Toast.makeText(this, "Name, last name and email are required", Toast.LENGTH_SHORT).show();
			return null;
		}
		
		// Make sure we have a valid image and save it
		Bitmap image = (Bitmap) findViewById(R.id.view_profile_user_image).getTag();
		if (image == null) {
			// we only set the tag when we get a valid image, so null indicates that the image is invalid
			Toast.makeText(this, "User picture is required", Toast.LENGTH_SHORT).show();
			return null;	
		}
		user.setImage(image);
		
		// Save non-required fields: nickname, description
		user.setDescription( ((TextView) findViewById(R.id.view_profile_description)).getText().toString() );
		user.setNickname( ((TextView) findViewById(R.id.view_profile_nickname)).getText().toString() );
		
		

		
/*

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
		
		// To check GCM token received or not
		if (deviceToken.trim().length() > 0) {

			final JSONObject postData = new JSONObject();
			try {
				// Prepare registration information in JSON format to the web service
				postData.put("locuId", locuId.getText().toString());
				postData.put("deviceToken", deviceToken);
				postData.put("wifiName", wifiName.getText().toString());
				postData.put("wifiPassword", wifiPassword.getText().toString());
				postData.put("typeOfAuthentication",
						typeOfAuthentication == null ? ""
								: typeOfAuthentication.getText().toString());
				postData.put("paypalId", paypal.getText().toString());
				postData.put("deviceType", "0");
				postData.put("cancelOrderTime",orderTimeOut.getText().toString());

				if (wifi == null ? false : wifi.getText().toString()
						.equalsIgnoreCase("Yes"))
					postData.put("wifiPresent", "1");
				else
					postData.put("wifiPresent", "0");

			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
			
			// Start progress dialog from here
			progressDialog = Utilities.progressDialog(this, "Loading..");
			progressDialog.show();
			
		} else {
		// To stop sending details to server if the GCM device token is failed
			WebServices.alertbox("Please try again....", UserProfileActivity.this);
			return null;
		}
		
		*/
		
		return user;
	}
	

	/* 
	 * Downloads an image for the mUser structure and displays it in a given view when done.
	 */
	
	private class DownloadImageTask extends AsyncTask<ImageView, Integer, Bitmap> {
		// Do the long-running work in here

		ImageView view = null;

		protected Bitmap doInBackground(ImageView... params) {
			view = params[0];
			Bitmap bitmap;
			String url = null;

			if (mApp.mUser == null)
				return null;

			url = mApp.mUser.getImage().getUrl();
			
			try {
				Log.d("Bartsy", "About to decode image for dialog from URL: " + url);

				bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
			} catch (MalformedURLException e) {
				e.printStackTrace();
				Log.d("Bartsy", "Bad URL: " + mApp.mUser.getImage().getUrl());
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				Log.d("Bartsy", "Could not download image from URL: " + url);
				return null;
			}

			Log.d("Bartsy", "Image decompress successfully for dialog: " + url);

			return bitmap;
		}

		// This is called each time you call publishProgress()
		protected void onProgressUpdate(Integer... progress) {
			// setProgressPercent(progress[0]);
		}

		// This is called when doInBackground() is finished
		protected void onPostExecute(Bitmap result) {
			// showNotification("Downloaded " + result + " bytes");
//			mProfileImage = result;
			if (view != null) {
				view.setImageBitmap(result);
				view.setTag(result);
			}
		}
	}
	
}
