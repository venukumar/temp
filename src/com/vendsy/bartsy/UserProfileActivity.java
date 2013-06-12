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
import org.json.JSONException;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;

public class UserProfileActivity extends Activity implements OnClickListener {

	private static final String TAG = "UserProfileActivity";
	
	BartsyApplication mApp = null ; // pointer to the application used as an input/output buffer to this activity
	
	EditText locuId, paypal, wifiName, wifiPassword,orderTimeOut;
	
	// Progress dialog
	static final int MY_SCAN_REQUEST_CODE = 23453; // used here only, just some random unique number


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		Log.v(TAG, "onCreate()");
		
		// Set up pointer to activity used as an input/output buffer
		mApp = (BartsyApplication) getApplication();
		UserProfile person = mApp.mUserProfileActivityInput;

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
			if (person.hasDescription())
				((TextView) findViewById(R.id.view_profile_description)).setText(person.getDescription());
			if (person.hasEmail()) 
				((TextView) findViewById(R.id.view_profile_email)).setText(person.getEmail());
			if (person.hasImagePath()) 
				// User profile has an image - display it asynchronously and also set it up in the output buffer upon success
				new DownloadImageTask().execute((ImageView) findViewById(R.id.view_profile_user_image));
			if (person.hasGender()) {
				if (person.getGender().equalsIgnoreCase("male"))
					((RadioButton) findViewById(R.id.view_profile_gender_male)).setChecked(true);
				if (person.getGender().equalsIgnoreCase("female"))
					((RadioButton) findViewById(R.id.view_profile_gender_female)).setChecked(true);
			}
		}

		
		// Set up image controllers
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
				mApp.mUserProfileActivityOutput = profile;
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
	 * Invokes this method when the user clicks on the Submit Button
	 */
	public UserProfile validateProfileData() {
		
		Log.v(TAG, "validateProfileData()");
		
		UserProfile user = new UserProfile();
		
		// Make sure there is an email and password
		String email = ((TextView) findViewById(R.id.view_profile_email)).getText().toString();
		if (email.length() > 0) {
			user.setEmail(email);
		} else {
			Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
			return null;
		}
		String password =  ((TextView) findViewById(R.id.view_profile_password)).getText().toString();
		if (password.length() < 6) {
			Toast.makeText(this, "Please create a password that's at least 6 characters long", Toast.LENGTH_SHORT).show();
			return null;			
		}
		String password_confirm =  ((TextView) findViewById(R.id.view_profile_password_confirm)).getText().toString();
		if (password.compareTo(password_confirm) != 0) {
			Toast.makeText(this, "Password confirmation mismatch.", Toast.LENGTH_SHORT).show();
			return null;						
		}
		user.setPassword(password);

		// Set fields not present in the form such as username etc.

		if (mApp.mUserProfileActivityInput == null) {

			// The user input buffer is not set up, this mean we started with a blank profile
			user.setUsername(((TextView) findViewById(R.id.view_profile_email)).getText().toString());		// use email as the username
			user.setSocialNetworkId(((TextView) findViewById(R.id.view_profile_email)).getText().toString());		// use email as the username
			user.setType("bartsy");
		} 
		else if(mApp.mUserProfileActivityInput != null){

			// If we're not starting with a blank profile, use the input setup
			Log.v(TAG, "Setting profile up from social netowrk with username ID " + mApp.mUserProfileActivityInput.getUserId());
			user.setUsername(mApp.mUserProfileActivityInput.getUsername());
			user.setSocialNetworkId(mApp.mUserProfileActivityInput.getSocialNetworkId());
			user.setType(mApp.mUserProfileActivityInput.getType());
		} 

		// Make sure there's a nickname
		String nickname = ((TextView) findViewById(R.id.view_profile_nickname)).getText().toString();
		if (nickname.length() > 0) {
			user.setNickname(nickname);
		} else {
			Toast.makeText(this, "Nickname is required", Toast.LENGTH_SHORT).show();
			return null;			
		}
			
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
				return null;			
			}
			user.setBirthday(bd);
		}
		
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
		
		// Setup visibility preference
		if (((CheckBox) findViewById(R.id.view_profile_checkbox_details)).isChecked())
			user.setVisibility(UserProfile.VISIBLE);
		else
			user.setVisibility(UserProfile.HIDDEN);
		
		
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
		
		// Make sure we have a valid image and save it
		Bitmap image = (Bitmap) findViewById(R.id.view_profile_user_image).getTag();
		if (image == null) {
			// we only set the tag when we get a valid image, so null indicates that the image is invalid
			Toast.makeText(this, "User picture is required", Toast.LENGTH_SHORT).show();
			return null;	
		}
		user.setImage(image);
		
		// Extract description
		user.setDescription( ((TextView) findViewById(R.id.view_profile_description)).getText().toString() );
		
		
		
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

			if (mApp.mUserProfileActivityInput == null)
				return null;

			url = mApp.mUserProfileActivityInput.getImagePath();
			
			try {
				Log.d("Bartsy", "About to decode image for dialog from URL: " + url);

				bitmap = BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
			} catch (MalformedURLException e) {
				e.printStackTrace();
				Log.d("Bartsy", "Bad URL: " + mApp.mUserProfileActivityInput.getImagePath());
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
