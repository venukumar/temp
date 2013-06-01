package com.vendsy.bartsy;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.vendsy.bartsy.model.Venue;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.Utilities;
import com.vendsy.bartsy.utils.WebServices;

public class MainActivity extends FragmentActivity implements OnClickListener {

	private static final String TAG = "MainActivity";
	
	private Handler handler = new Handler();
	BartsyApplication mApp = null;
	static final int MY_SCAN_REQUEST_CODE = 23453; // used here only, just some
													// random unique number

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup application pointer
		mApp = (BartsyApplication) getApplication();
		
		
		// If the user profile is not set, start the init activity

		if (Utilities.loadPref(this, R.string.config_user_account_name, null) == null) {
			Intent intent = new Intent().setClass(this, InitActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return;
		} 
		
		
		setContentView(R.layout.main);


		if (mApp.mActiveVenue == null) {

			// No active venue - hide active menu UI
			findViewById(R.id.view_active_venue).setVisibility(View.GONE);
			
		} else {
			// Active venue exists - set up the active venue view. For now just show it

			findViewById(R.id.view_active_venue).setVisibility(View.VISIBLE);
			findViewById(R.id.check_out).setVisibility(View.VISIBLE);

			// Set up checkout button
			Button b = (Button) findViewById(R.id.button_active_venue);
			Button checkOut = (Button) findViewById(R.id.check_out);
			checkOut.setOnClickListener(this);

			// Setup text for the view
			if (mApp.mOrders.size() == 0) {
				b.setText("Checked in at: " + mApp.mActiveVenue.getName()
						+ "\nClick to order drinks and see who's here...");
			} else {
				b.setText("Checked in at: " + mApp.mActiveVenue.getName() + "\n"
						+ mApp.mOrders.size() + " open orders. Click for more...");
			}
		}

		// Set up button listeners
		
		((Button) findViewById(R.id.button_checkin)).setOnClickListener(this);
		((Button) findViewById(R.id.button_settings)).setOnClickListener(this);
		((View) findViewById(R.id.button_active_venue))
				.setOnClickListener(this);
		((View) findViewById(R.id.button_notifications))
				.setOnClickListener(this);
		((View) findViewById(R.id.button_payments)).setOnClickListener(this);
		((View) findViewById(R.id.button_profile)).setOnClickListener(this);
		((View) findViewById(R.id.button_payments_dismiss))
				.setOnClickListener(this);
		((View) findViewById(R.id.button_profile_dismiss))
				.setOnClickListener(this);
		((View) findViewById(R.id.button_my_venues)).setOnClickListener(this);

		// Hide action bar
		getActionBar().hide();
	}


	@Override
	public void onClick(View v) {
		Log.d("Bartsy", "Clicked on a button");

		Intent intent;

		switch (v.getId()) {

		case R.id.check_out:

			checkOutUser();

			break;

		case R.id.button_active_venue:

			intent = new Intent().setClass(this, VenueActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			break;
		case R.id.button_payments:
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
		case R.id.button_payments_dismiss:
			// For now simply modify the UI. This should open a dialog with
			// choices: remind again, don't remind again
			((View) v.getParent()).setVisibility(View.GONE);
			break;
		case R.id.button_profile:
			break;
		case R.id.button_profile_dismiss:
			// For now simply modify the UI. This should open a dialog with
			// choices: remind again, don't remind again
			((View) v.getParent()).setVisibility(View.GONE);
			break;
		case R.id.button_checkin:
			intent = new Intent().setClass(this, MapActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			break;
		case R.id.button_my_venues:
			break;
		case R.id.button_notifications:
			// For now don't do anything
			break;
		case R.id.button_settings:
			intent = new Intent().setClass(this, SettingsActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			break;
		}
	}

	/**
	 * To checkout user from the active venue
	 * 
	 */
	private void checkOutUser() {
		// For now it will ask confirmation dialog
		if (mApp.mActiveVenue != null && mApp.mOrders.size() > 0) {
			alertBox("You have open orders placed at "
					+ mApp.mActiveVenue.getName()
					+ ". If you checkout they will be cancelled and you will still be charged for it.Do you want to checkout from "
					+ mApp.mActiveVenue.getName() + "?");
		} else if (mApp.mActiveVenue != null) {

			alertBox("Do you want to checkout from "
					+ mApp.mActiveVenue.getName() + "?");

		}

	}


	/**
	 * To display alert box when the user check out from the active venue
	 */
	private void alertBox(String message) {

		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setCancelable(true);
		builder.setTitle("Please Confirm !");
		builder.setInverseBackgroundForced(true);
		builder.setMessage(message);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				// To check null condition (Error handling)
				if (mApp.mActiveVenue != null) {
					// Service call in the background
					new Thread() {
						public void run() {
							// Check out web service call
							String response = WebServices.userCheckInOrOut(
									MainActivity.this,
									mApp.mActiveVenue.getId(),
									Constants.URL_USER_CHECK_OUT);
							if (response != null) {
								System.out.println("response  ::: " + response);
								// To parse check out web service response
								try {
									JSONObject result = new JSONObject(response);
									String errorCode = result
											.getString("errorCode");

									// For now don't handle exceptions
									// locally...
									if (errorCode.equalsIgnoreCase("0")) {
										// No errors
									} else {
										// Errors
									}

									// Check out user locally regardless of
									// server status
									handler.post(new Runnable() {
										@Override
										public void run() {
											mApp.userCheckOut();
											findViewById(R.id.view_active_venue).setVisibility(View.GONE);
										}
									});
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						};
					}.start();
				}
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// To close the alert dialog
				dialog.dismiss();
			}
		});
		// To display alert dialog
		AlertDialog alert = builder.create();
		alert.show();

	}

	/* This method is used by Card.io to process the credit card numbers 
	 * (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
	 */
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == MY_SCAN_REQUEST_CODE) {
			String resultDisplayStr;
			if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
				CreditCard scanResult = data
						.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

				// Never log a raw card number. Avoid displaying it, but if
				// necessary use getFormattedCardNumber()
				resultDisplayStr = "Card Number: "
						+ scanResult.getRedactedCardNumber() + "\n";

				// Do something with the raw number, e.g.:
				// myService.setCardNumber( scanResult.cardNumber );

				if (scanResult.isExpiryValid()) {
					resultDisplayStr += "Expiration Date: "
							+ scanResult.expiryMonth + "/"
							+ scanResult.expiryYear + "\n";
				}

				if (scanResult.cvv != null) {
					// Never log or display a CVV
					resultDisplayStr += "CVV has " + scanResult.cvv.length()
							+ " digits.\n";
				}

				if (scanResult.zip != null) {
					resultDisplayStr += "Zip: " + scanResult.zip + "\n";
				}
			} else {
				resultDisplayStr = "Scan was canceled.";
			}
			// do something with resultDisplayStr, maybe display it in a
			// textView

			Toast.makeText(this, resultDisplayStr, Toast.LENGTH_SHORT).show();
		}
		// else handle other activity results
	}
}
