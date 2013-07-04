package com.vendsy.bartsy;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.vendsy.bartsy.model.Order;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.model.Venue;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;

public class UserProfileActivity extends Activity implements OnClickListener {

	private static final String TAG = "UserProfileActivity";
	
	BartsyApplication mApp = null ; // pointer to the application used as an input/output buffer to this activity
	UserProfileActivity mActivity = this;
	Handler mHandler = new Handler();
	
	EditText locuId, paypal, wifiName, wifiPassword,orderTimeOut;
	
	// Progress dialog
	static final int MY_SCAN_REQUEST_CODE = 23453; // used here only, just some random unique number

	
	/*
	 * TODO - Create view
	 * 
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		
		// Set up pointer to activity used as an input/output buffer
		mApp = (BartsyApplication) getApplication();
		UserProfile person = mApp.mUserProfileActivityInput;

		Log.v(TAG, "onCreate(" + person + ")");
		
		// Set the base view then pre-populate it with any existing values found in the application input buffer (mUser)
		setContentView(R.layout.user_profile);

		
		// Pre-populate fields if there is a user object already 
		if (person != null) {
			// Set first name, last name, nickname, description, email, user image
			if(person.hasFirstName())
				((TextView) findViewById(R.id.view_profile_first_name)).setText(person.getFirstName());
			if(person.hasLastName())
				((TextView) findViewById(R.id.view_profile_last_name)).setText(person.getLastName());
			if (person.getNickname() != null) 
				((TextView) findViewById(R.id.view_profile_nickname)).setText(person.getNickname());
			else
				// If no Nickname, use the peron's first name
				((TextView) findViewById(R.id.view_profile_nickname)).setText(
						((TextView) findViewById(R.id.view_profile_first_name)).getText());

			if (person.hasImage())
				((ImageView) findViewById(R.id.view_profile_user_image)).setImageBitmap(person.getImage());
			else if (person.hasImagePath()) {
				// User profile has an image - display it asynchronously and also set it up in the output buffer upon success
//				new DownloadImageTask().execute((ImageView) findViewById(R.id.view_profile_user_image));
//				String imagePath;
//				
//				if(person.getImagePath().contains("http://") || person.getImagePath().contains("https://")){
//					imagePath = person.getImagePath();
//				}else{
//					imagePath = Constants.DOMAIN_NAME + person.getImagePath();
//				}
				
				WebServices.downloadImage(person, (ImageView) findViewById(R.id.view_profile_user_image));
			}
			
			if (person.hasEmail()) 
				((TextView) findViewById(R.id.view_profile_email)).setText(person.getEmail());	

			if (person.hasBartsyLogin()) 
				((TextView) findViewById(R.id.view_profile_email)).setText(person.getBartsyLogin());	
			
			if (person.hasPassword()) {
				((EditText) findViewById(R.id.view_profile_password)).setText(person.getPassword());
				((EditText) findViewById(R.id.view_profile_password_confirm)).setText(person.getPassword());
			}

			// If we have FB or G+ login, don't show password field, otherwise don't show password checkbox
			if ((person.hasFacebookUsername() || person.hasGoogleUsername()) && 
					!person.hasPassword()) {
				findViewById(R.id.view_profile_password_view).setVisibility(View.GONE);
				findViewById(R.id.view_profile_account_view).setVisibility(View.VISIBLE);			
			} else {
				findViewById(R.id.view_profile_password_view).setVisibility(View.VISIBLE);
				findViewById(R.id.view_profile_account_view).setVisibility(View.GONE);			
			}

			// Setup credit card info if available
			if (person.hasCreditCardNumber() && person.hasExpMonth() && person.hasExpYear()) {
				((TextView) findViewById(R.id.view_profile_cc_type)).setText(GetCreditCardType(person.getCreditCardNumber()));
				((TextView) findViewById(R.id.view_profile_cc_number_redacted)).setText("****" + person.getCreditCardNumber().substring(12));
				((TextView) findViewById(R.id.view_profile_cc_number_redacted)).setTag(person.getCreditCardNumber());
				((TextView) findViewById(R.id.view_profile_cc_month)).setText("**");
				((TextView) findViewById(R.id.view_profile_cc_month)).setTag(person.getExpMonth());
				((TextView) findViewById(R.id.view_profile_cc_year)).setText("**");
				((TextView) findViewById(R.id.view_profile_cc_year)).setTag(person.getExpYear());
				findViewById(R.id.view_profile_has_cc).setVisibility(View.VISIBLE);
				findViewById(R.id.view_profile_no_cc).setVisibility(View.GONE);
			}
			
			

			// Setup visibility preference
			if (person.hasVisibility() && person.getVisibility().equalsIgnoreCase(UserProfile.VISIBLE)) {

				((CheckBox) findViewById(R.id.view_profile_checkbox_details)).setChecked(true);
				findViewById(R.id.view_profile_details).setVisibility(View.VISIBLE);
				
				// Set up description
				if (person.hasDescription())
					((TextView) findViewById(R.id.view_profile_description)).setText(person.getDescription());

				// Set up gender
				if (person.hasGender()) {
					if (person.getGender().equalsIgnoreCase("Male"))
						((RadioButton) findViewById(R.id.view_profile_gender_male)).setChecked(true);
					if (person.getGender().equalsIgnoreCase("Female"))
						((RadioButton) findViewById(R.id.view_profile_gender_female)).setChecked(true);
				}

				// Display birthday
				if (person.hasBirthday())
					((TextView) findViewById(R.id.view_profile_birthday)).setText(person.getBirthday());
				
				// Setup status based on checkboxes
				if (person.hasStatus()) {
					if (person.getStatus().equalsIgnoreCase("Singles/Friends")) {
					((CheckBox) findViewById(R.id.view_profile_status_singles)).setChecked(true);
					((CheckBox) findViewById(R.id.view_profile_status_friends)).setChecked(true);
				} else if (person.getStatus().equalsIgnoreCase("Singles")) 
					((CheckBox) findViewById(R.id.view_profile_status_singles)).setChecked(true);
				else if (person.getStatus().equalsIgnoreCase("Friends")) 
					((CheckBox) findViewById(R.id.view_profile_status_friends)).setChecked(true);
				}
				
				// Setup orientation
				if (person.hasOrientation()) {
					if (person.getOrientation().equalsIgnoreCase("Straight"))
						((RadioButton) findViewById(R.id.view_profile_orientation_straight)).setChecked(true);
					if (person.getOrientation().equalsIgnoreCase("Gay"))
						((RadioButton) findViewById(R.id.view_profile_orientation_gay)).setChecked(true);
					if (person.getOrientation().equalsIgnoreCase("Bisexual"))
						((RadioButton) findViewById(R.id.view_profile_orientation_bisexual)).setChecked(true);
				}
				
			} else {
				
				((CheckBox) findViewById(R.id.view_profile_checkbox_details)).setChecked(false);
				findViewById(R.id.view_profile_details).setVisibility(View.GONE);

			}
		} else {
			// No current user - don't show option not to add password or credit card
			findViewById(R.id.view_profile_account_view).setVisibility(View.GONE);
			findViewById(R.id.view_profile_has_cc).setVisibility(View.GONE);
			findViewById(R.id.view_profile_no_cc).setVisibility(View.VISIBLE);

		}
		

		
		// Set up image controllers
		findViewById(R.id.view_profile_account_checkbox).setOnClickListener(this);
		findViewById(R.id.view_profile_checkbox_details).setOnClickListener(this);
		findViewById(R.id.view_profile_user_image).setOnClickListener(this);
		findViewById(R.id.view_profile_button_cancel).setOnClickListener(this);
		findViewById(R.id.view_profile_button_submit).setOnClickListener(this);
		findViewById(R.id.view_profile_button_cc_add).setOnClickListener(this);
		findViewById(R.id.view_profile_button_cc_replace).setOnClickListener(this);
		findViewById(R.id.view_profile_button_cc_delete).setOnClickListener(this);
		
		
	}

	
	private static final int SELECT_PHOTO = 100;
	
	@Override
	public void onClick(View arg0) {

		
		Log.v(TAG, "onClick()");

		switch (arg0.getId()) {
		
		case R.id.view_profile_account_checkbox:
			Log.v(TAG, "Account checkbox");
			if (((CheckBox) findViewById(R.id.view_profile_account_checkbox)).isChecked())
				findViewById(R.id.view_profile_password_view).setVisibility(View.VISIBLE);
			else
				findViewById(R.id.view_profile_password_view).setVisibility(View.GONE);
			break;
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
		case R.id.view_profile_button_cc_replace:

			// Get credit card number using card.io
			Intent scanIntent = new Intent(this, CardIOActivity.class);

			// required for authentication with card.io
			scanIntent.putExtra(CardIOActivity.EXTRA_APP_TOKEN, getResources().getString(R.string.config_cardio_token));

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
			processProfileData();
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
					
					// Display credit card info
					((TextView) findViewById(R.id.view_profile_cc_type)).setText(GetCreditCardType(scanResult.cardNumber));
					((TextView) findViewById(R.id.view_profile_cc_number_redacted)).setText(scanResult.getRedactedCardNumber());
					((TextView) findViewById(R.id.view_profile_cc_number_redacted)).setTag(scanResult.cardNumber);
					((TextView) findViewById(R.id.view_profile_cc_month)).setText(Integer.toString(scanResult.expiryMonth));
					((TextView) findViewById(R.id.view_profile_cc_month)).setTag(Integer.toString(scanResult.expiryMonth));
					((TextView) findViewById(R.id.view_profile_cc_year)).setText(Integer.toString(scanResult.expiryYear));
					((TextView) findViewById(R.id.view_profile_cc_year)).setTag(Integer.toString(scanResult.expiryYear));
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
	 * TODO - Invokes this method when the user clicks on the Submit Button
	 */
	private void processProfileData() {
		
		Log.v(TAG, "validateProfileData(" + (mApp.mUserProfileActivityInput == null ? "null" : mApp.mUserProfileActivityInput.toString()) + ")");
		
		UserProfile user = new UserProfile();
		
		String email = ((TextView) findViewById(R.id.view_profile_email)).getText().toString();
		
		if ( mApp.mUserProfileActivityInput == null || ((CheckBox) findViewById(R.id.view_profile_account_checkbox)).isChecked() ||
				(mApp.mUserProfileActivityInput != null && mApp.mUserProfileActivityInput.hasBartsyLogin())) {

			// Make sure there is a login/email and password if logging in with Bartsy
			if (email.length() > 0) {
				user.setBartsyLogin(email);
			} else {
				Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
				return;
			}
			
			// Verify password matches and exists
			String password =  ((TextView) findViewById(R.id.view_profile_password)).getText().toString();
			if (password.length() < 6) {
				Toast.makeText(this, "Please create a password that's at least 6 characters long", Toast.LENGTH_SHORT).show();
				return;			
			}
			String password_confirm =  ((TextView) findViewById(R.id.view_profile_password_confirm)).getText().toString();
			if (password.compareTo(password_confirm) != 0) {
				Toast.makeText(this, "Password confirmation mismatch.", Toast.LENGTH_SHORT).show();
				return;						
			}
			user.setBartsyPassword(password);

			// Also set email to be the same as the login info
			user.setEmail(email);
			
		} else {

			// If we've logged in with a social network, email is not required, so don't fail if it's not present
			if (email.length() > 0)
				user.setEmail(email);
		}

		// Set social network connectivity fields if present
		if( mApp.mUserProfileActivityInput != null){
			user.setBartsyId(mApp.mUserProfileActivityInput.getBartsyId());
			user.setFacebookUsername(mApp.mUserProfileActivityInput.getFacebookUsername());
			user.setFacebookId(mApp.mUserProfileActivityInput.getFacebookId());
			user.setGoogleUsername(mApp.mUserProfileActivityInput.getGoogleUsername());
			user.setGoogleId(mApp.mUserProfileActivityInput.getGoogleId());
		} 

		// Make sure there's a nickname
		String nickname = ((TextView) findViewById(R.id.view_profile_nickname)).getText().toString();
		if (nickname.length() > 0) {
			user.setNickname(nickname);
		} else {
			Toast.makeText(this, "Nickname is required", Toast.LENGTH_SHORT).show();
			return;			
		}
			

		// Setup credit card information
		if (findViewById(R.id.view_profile_has_cc).getVisibility() == View.VISIBLE) {
			
			// Extract first and last name
			String first_name = ((TextView) findViewById(R.id.view_profile_first_name)).getText().toString();
			String last_name = ((TextView) findViewById(R.id.view_profile_last_name)).getText().toString();
			if (first_name.length() > 0 && last_name.length() > 0)
				user.setName(first_name + " " + last_name);
			else if (first_name.length() > 0)
				user.setName(first_name);
			else if (last_name.length() > 0)
				user.setName(last_name);
			if (first_name.length() > 0)
				user.setFirstName(first_name);
			if (last_name.length() > 0)
					user.setLastName(last_name);
			
			//Extract card details 	
			user.setCreditCardNumber((String) ((TextView) findViewById(R.id.view_profile_cc_number_redacted)).getTag());
			user.setExpMonth((String) ((TextView) findViewById(R.id.view_profile_cc_month)).getTag());
			user.setExpYear((String) ((TextView) findViewById(R.id.view_profile_cc_year)).getTag());
			
			String ccType = GetCreditCardType(user.getCreditCardNumber());
			if (!ccType.equalsIgnoreCase("VISA") && !ccType.equalsIgnoreCase("MasterCard")) {
				Toast.makeText(this, "Please use VISA or MasterCard.", Toast.LENGTH_SHORT).show();
				return;			
			}
		}
		

		// Make sure we have a valid image and save it
		Bitmap image = (Bitmap) findViewById(R.id.view_profile_user_image).getTag();
		if (image == null) {
			// we only set the tag when we get a valid image, so null indicates that the image is invalid
			Toast.makeText(this, "User picture is required", Toast.LENGTH_SHORT).show();
			return;	
		}
		user.setImage(image);
		
		
		// Setup visibility preference
		if (((CheckBox) findViewById(R.id.view_profile_checkbox_details)).isChecked()) {
			user.setVisibility(UserProfile.VISIBLE);
			user.setDescription(((TextView) findViewById(R.id.view_profile_description)).getText().toString());
			if (((RadioButton) findViewById(R.id.view_profile_gender_male)).isChecked())
				user.setGender("Male");
			else if (((RadioButton) findViewById(R.id.view_profile_gender_female)).isChecked())
				user.setGender("Female");
			
			// Validate birthday format
			String bd = ((TextView) findViewById(R.id.view_profile_birthday)).getText().toString();
			if (bd.length() > 0) {
				try {
					DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
					Date date = df.parse(bd);
					SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
					bd = sdf.format(date);
				} catch (Exception e) {
					Toast.makeText(this, "Birthday format is invalid", Toast.LENGTH_SHORT).show();
					return;			
				}
				user.setBirthday(bd);
			}

			// Extract description
			user.setDescription( ((TextView) findViewById(R.id.view_profile_description)).getText().toString() );
			
			// Setup status based on checkboxes
			Boolean singles = ((CheckBox) findViewById(R.id.view_profile_status_singles)).isChecked();
			Boolean friends = ((CheckBox) findViewById(R.id.view_profile_status_friends)).isChecked();
			if (friends && singles) 
				user.setStatus("Singles/Friends");
			else if (friends)
				user.setStatus("Friends");
			else if (singles)
				user.setStatus("Singles");
			
			// Setup orientation
			if (((RadioButton) findViewById(R.id.view_profile_orientation_straight)).isChecked())
				user.setOrientation("Straight");
			if (((RadioButton) findViewById(R.id.view_profile_orientation_gay)).isChecked())
				user.setOrientation("Gay");
			if (((RadioButton) findViewById(R.id.view_profile_orientation_bisexual)).isChecked())
				user.setOrientation("Bisexual");

			
		} else {
			user.setVisibility(UserProfile.HIDDEN);
			user.setDescription(null);
			user.setBirthday(null);
			user.setStatus(null);
			user.setOrientation(null);
		}
		
		
		
		
		
		Log.v(TAG, "New user created: " + user);
		
		processUserProfile(user);
	
	}
	


	
	/**
	 * TODO 
	 * THis function is called when the user has accepted the profile in the profile activity. It saves the user
	 * details locally. If the user is checked in according to the server, the function also checks the user in
	 * locally. This function will either terminate the activity and start a new one or leave the user in this 
	 * activity with a Toast asking them to retry their login.
	 * 
	 * The function uses the mUser parameters passed through the application as assumes they are set up
	 */
	
	public void processUserProfile(final UserProfile userProfile) {
		
		Log.i(TAG, "processUserProfileData()");
		
		SharedPreferences settings = getSharedPreferences(GCMIntentService.REG_ID, 0);
		String deviceToken = settings.getString("RegId", "");
		if (deviceToken.trim().length() > 0) {
			
			// Send profile data to server in background

			new Thread() {
				public void run() {
					String bartsyUserId = null;
					
					try {
						// Service call for post profile data to server
						JSONObject resultJson = WebServices.saveUserProfile(userProfile, WebServices.URL_POST_PROFILE_DATA, getApplicationContext());

						// Make sure we got a successful response
						if (!(resultJson!=null && resultJson.has("errorCode") && resultJson.getString("errorCode").equalsIgnoreCase("0"))) {

							// Error creating user. Ask parent to post Toast.
							mHandler.post(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(mActivity, "Could not save profile, check your connection and try again....", Toast.LENGTH_SHORT).show();
									finish();
								}
							});
							return;
						}

						// Syscall successful - process response 
						if (resultJson.has("bartsyId")) {
							bartsyUserId = resultJson.getString("bartsyId");
							Log.v(TAG, "bartsyUserId " + bartsyUserId + "");
						} else {
							// Bad server response
							Log.e(TAG, "bartsyId " + bartsyUserId + " not found");
							mHandler.post(new Runnable() {
								public void run() {
									Toast.makeText(mActivity, "Server rejected login request. Try a different login.", Toast.LENGTH_LONG).show();
									finish();
								}
							});
							return;
						}

						// Save profile in the global application structure and in preferences
						userProfile.setBartsyId(bartsyUserId);
						mApp.saveUserProfile(userProfile);

						// Sync active venue
						mApp.syncActiveVenue();

						// Finally, load the open orders this user has to be stored in the application object that will stick around 
						mApp.syncOpenOrders();
						
						// AFter processing profile return back to calling activity with success code
						mHandler.post(new Runnable() {
							public void run() {
								// Return to calling activity with success code 
								mActivity.setResult(InitActivity.RESULT_OK);
								finish();
							}
						});
					} catch (JSONException e) {
						e.printStackTrace();

						// Error creating user. Ask parent to post Toast.
						mHandler.post(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(mActivity, "Please try again....", Toast.LENGTH_LONG).show();
								finish();
							}
						});
						return;
					}
				}
			}.start();

		} else {
			Toast.makeText(this, "Please try again....", Toast.LENGTH_LONG).show();
			finish();
		}
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

			if (mApp.mUserProfileActivityInput == null)
				return null;

			url = mApp.mUserProfileActivityInput.getImagePath();
			
			try {
				Log.v(TAG, "About to decode image from URL: " + url);

				bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
			} catch (MalformedURLException e) {
				e.printStackTrace();
				Log.d(TAG, "Bad URL: " + mApp.mUserProfileActivityInput.getImagePath());
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				Log.d(TAG, "Could not download image from URL: " + url);
				return null;
			}

			Log.v(TAG, "Image decompress successfully: " + url);

			return bitmap;
		}

		// This is called each time you call publishProgress()
		protected void onProgressUpdate(Integer... progress) {
			// setProgressPercent(progress[0]);
		}

		// This is called when doInBackground() is finished
		protected void onPostExecute(Bitmap result) {
			Log.v(TAG, "onPostExecute()");
//			mProfileImage = result;
			if (view != null) {
				Log.v(TAG, "Saving and displaying image...");
				view.setImageBitmap(result);
				view.setTag(result);
			}
		}
	}
	
}
