package com.vendsy.bartsy;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnAccessRevokedListener;
import com.google.android.gms.plus.PlusClient.OnPersonLoadedListener;
import com.google.android.gms.plus.model.people.Person;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.dialog.PeopleDialogFragment.UserDialogListener;
import com.vendsy.bartsy.dialog.ProfileDialogFragment.ProfileDialogListener;
import com.vendsy.bartsy.model.Venue;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;
import com.vendsy.bartsy.wifi.AllJoynDialogBuilder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements OnClickListener {

	private Handler handler = new Handler();
	BartsyApplication mApp = null;
	static final int MY_SCAN_REQUEST_CODE = 23453; // used here only, just some
													// random unique number

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		// If the user profile is not set, start the init activity
	    SharedPreferences sharedPref = getSharedPreferences(getResources().getString(R.string.config_shared_preferences_name), Context.MODE_PRIVATE);
		if (sharedPref.getString(getResources().getString(R.string.config_user_account_name), "")
				.equalsIgnoreCase("")) {
			Intent intent = new Intent().setClass(this, InitActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return;
		}
		
		
		
		setContentView(R.layout.main);

		mApp = (BartsyApplication) getApplication();

		Venue venue = ((BartsyApplication) getApplication()).activeVenue;

		if (venue == null) {
			// No active venue - hide active menu UI
			findViewById(R.id.view_active_venue).setVisibility(View.GONE);

		} else {
			// Active venue exists - set up the active venue view
			// For now just show it
			findViewById(R.id.view_active_venue).setVisibility(View.VISIBLE);
			findViewById(R.id.check_out).setVisibility(View.VISIBLE);
			// Set up button
			Button b = (Button) findViewById(R.id.button_active_venue);
			Button checkOut = (Button) findViewById(R.id.check_out);

			checkOut.setOnClickListener(this);

			if (mApp.mOrders.size() == 0) {
				b.setText("Checked in at: " + venue.getName()
						+ "\nClick to order drinks and see who's here...");
			} else {
				b.setText("Checked in at: " + venue.getName() + "\n"
						+ mApp.mOrders.size()
						+ " open orders. Click for more...");
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
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	// @Override
	// protected Dialog onCreateDialog(int id) {
	// Log.i("Dailog ", "onCreateDialog()");
	// Dialog result = null;
	// System.out.println("dialogg");
	// switch (id) {
	// case 0: {
	// System.out.println("case o");
	// VenueListDialog dialog = new VenueListDialog(MainActivity.this);
	// }
	// break;
	//
	// }
	// return result;
	// }

	@Override
	public void onClick(View v) {
		Log.d("Bartsy", "Clicked on a button");

		Intent intent;

		switch (v.getId()) {

		case R.id.check_out:

			checkOutUser();

			break;

		case R.id.button_active_venue:

			// VenueListDialog dialog = new VenueListDialog(MainActivity.this) {
			// @Override
			// protected void venueSelected(final VenueItem venueItem) {
			// // TODO Auto-generated method stub
			// super.venueSelected(venueItem);
			//
			// new Thread() {
			//
			// @Override
			// public void run() {
			// // TODO Auto-generated method stub
			// final String response = WebServices.userCheckIn(
			// MainActivity.this, venueItem.getId());
			// handler.post(new Runnable() {
			//
			// @Override
			// public void run() {
			// updateCheckInView(response,
			// venueItem.getName());
			// }
			//
			// });
			// }
			// }.start();
			//
			// }
			// };
			// dialog.show();

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
			scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default:
																			// true
			scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, false); // default:
																			// false
			scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_ZIP, false); // default:
																			// false

			// MY_SCAN_REQUEST_CODE is arbitrary and is only used within this
			// activity.
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

	private void checkOutUser() {
		// TODO Auto-generated method stub

		if (mApp.activeVenue != null && mApp.mOrders.size() > 0) {
			alertBox("You have open orders placed at "
					+ mApp.activeVenue.getName()
					+ ". If you checkout they will be cancelled and you will still be charged for it.Do you want to checkout from "
					+ mApp.activeVenue.getName() + "?");
		} else if (mApp.activeVenue != null) {

			alertBox("Do you want to checkout from "
					+ mApp.activeVenue.getName() + "?");

		}

	}

	//
	// private void updateCheckInView(String response, String checkInName) {
	// // TODO Auto-generated method stub
	// if (response != null) {
	// try {
	// JSONObject checkInObject = new JSONObject(response);
	// if (checkInObject.has("errorCode")) {
	// String errorCode = checkInObject.getString("errorCode");
	// System.out.println("error code " + errorCode);
	// if (Integer.valueOf(errorCode) == 1) {
	//
	// mApp.useSetChannelName(checkInName);
	//
	// Intent intent = new Intent().setClass(this,
	// VenueActivity.class);
	// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	// this.startActivity(intent);
	// finish();
	// }
	// }
	//
	// } catch (JSONException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// } else {
	//
	// }
	//
	// }

	private void alertBox(String message) {
		// TODO Auto-generated method stub

		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setCancelable(true);
		builder.setTitle("Please Confirm !");
		builder.setInverseBackgroundForced(true);
		builder.setMessage(message);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				if (mApp.activeVenue != null) {
					new Thread() {
						public void run() {

							String response = WebServices.userCheckInOrOut(
									MainActivity.this,
									mApp.activeVenue.getId(),
									Constants.URL_USER_CHECK_OUT);
							if (response != null) {
								System.out.println("response  ::: " + response);
								try {
									JSONObject result = new JSONObject(response);
									String errorCode = result
											.getString("errorCode");
									if (errorCode.equalsIgnoreCase("0")) {

										handler.post(new Runnable() {

											@Override
											public void run() {
												// TODO Auto-generated method
												// stub

												findViewById(
														R.id.view_active_venue)
														.setVisibility(
																View.GONE);
												mApp.activeVenue = null;
											}
										});

									}
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
				dialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();

	}

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
