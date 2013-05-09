/**
 * 
 */
package com.kellislabs.bartsy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.kellislabs.bartsy.model.MenuDrink;
import com.zooz.android.lib.CheckoutActivity;

/**
 * @author peterkellis
 * 
 */
public class DrinkDialogFragment extends DialogFragment {

	public MenuDrink drink;
	public String tipPercentageValue;

	// Identifier for the ZooZ CheckoutActivity
	private static final String TAG = DrinkDialogFragment.class.getSimpleName();
	private static final int ZooZ_Activity_ID = 1;

	/*
	 * The activity that creates an instance of this dialog fragment must
	 * implement this interface in order to receive event callbacks. Each method
	 * passes the DialogFragment in case the host needs to query it.
	 */
	public interface NoticeDialogListener {
		public void onDialogPositiveClick(DialogFragment dialog);
	}

	// Use this instance of the interface to deliver action events
	NoticeDialogListener mListener;

	// Override the Fragment.onAttach() method to instantiate the
	// NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (NoticeDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener");
		}
	}

	DialogInterface dialog = null;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Create dialog and set animation styles
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());// ,
																				// R.style.DrinkDialog);

		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		View view = inflater.inflate(R.layout.dialog_drink_order, null);

		// Customize dialog for this drink
		((TextView) view.findViewById(R.id.view_dialog_drink_title))
				.setText(drink.getTitle());
		((TextView) view.findViewById(R.id.view_dialog_drink_description))
				.setText(drink.getDescription());
		((TextView) view.findViewById(R.id.view_dialog_drink_price)).setText(""
				+ drink.getPrice());
		// ((ImageView)view.findViewById(R.id.view_dialog_drink_image_resource)).setImageResource(drink.image_resource);
		// // don't show image for now
		view.findViewById(R.id.view_dialog_drink_title).setTag(this.drink);
		final RadioGroup tipPercentage = (RadioGroup) view
				.findViewById(R.id.tipPercentage);
		final EditText percentage = (EditText) view
				.findViewById(R.id.editTextPercentage);

		builder.setView(view)
		// Add action buttons
				.setPositiveButton("Order",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) {
								
								
								System.out.println("order button is clicked");
								

								Intent intent = new Intent(getActivity(),
										CheckoutActivity.class);
								System.out.println("drink price:::"
										+ drink.getPrice());

								// send merchant credential, app_key as given in
								// the registration
								intent.putExtra(CheckoutActivity.ZOOZ_APP_KEY,
										"06717d2d-095e-4849-9d93-ab29beba3b7d");
								intent.putExtra(CheckoutActivity.ZOOZ_AMOUNT,
										0.99);
								intent.putExtra(
										CheckoutActivity.ZOOZ_CURRENCY_CODE,
										"USD");
								intent.putExtra(
										CheckoutActivity.ZOOZ_IS_SANDBOX, true);

								// start ZooZCheckoutActivity and wait to the
								// activity result.
								startActivityForResult(intent, ZooZ_Activity_ID);
								
							}

						});

		// Create dialog and set up animation
		return builder.create();
	}

	/**
	 * Parses the result returning from the ZooZ CheckoutActivity
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == ZooZ_Activity_ID) {

			switch (resultCode) {
			case Activity.RESULT_OK:
				Log.i(TAG,
						"Successfully paid. Your transaction id is: "
								+ data.getStringExtra(CheckoutActivity.ZOOZ_TRANSACTION_ID)
								+ "\nDisplay ID: "
								+ data.getStringExtra(CheckoutActivity.ZOOZ_TRANSACTION_DISPLAY_ID));

				// Send the positive button event back to the host activity
				 mListener.onDialogPositiveClick(DrinkDialogFragment.this);
				
				break;
			case Activity.RESULT_CANCELED:

				if (data != null)
					Log.e(TAG,
							"Error, cannot complete payment with ZooZ. "
									+ "Error code: "
									+ data.getIntExtra(
											CheckoutActivity.ZOOZ_ERROR_CODE, 0)
									+ "; Error Message: "
									+ data.getStringExtra(CheckoutActivity.ZOOZ_ERROR_MSG));
				break;

			default:
				break;
			}
		}
	}
}
