/**
 * 
 */
package com.vendsy.bartsy.dialog;

import java.text.DecimalFormat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.model.Item;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.utils.WebServices;

/**
 * @author peterkellis
 * 
 */
public class DrinkDialogFragment extends SherlockDialogFragment implements DialogInterface.OnClickListener, OnClickListener, OnTouchListener {

	public Item drink;
	public UserProfile profile;
	public float tipAmount;
	private View view;
	private BartsyApplication mApp;
    DecimalFormat df = new DecimalFormat();


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
			throw new ClassCastException(activity.toString() + " must implement NoticeDialogListener");
		}
		
		mApp = (BartsyApplication) getActivity().getApplication();
		
	}


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		df.setMaximumFractionDigits(2);
		df.setMinimumFractionDigits(2);

		// Create dialog and set animation styles
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		view = inflater.inflate(R.layout.dialog_drink_order, null);

		// Customize dialog for this drink
		((TextView) view.findViewById(R.id.view_dialog_drink_title)).setText(drink.getTitle());
		if (drink.getDescription() != null && !drink.getDescription().equalsIgnoreCase(""))
			((TextView) view.findViewById(R.id.view_dialog_drink_description)).setText(drink.getDescription());
		else
			((TextView) view.findViewById(R.id.view_dialog_drink_description)).setVisibility(View.GONE);
		((TextView) view.findViewById(R.id.view_dialog_drink_price)).setText(df.format(drink.getPrice()));

		// Show profile information by default
		if (profile != null) updateProfileView(profile);
		
		// ((ImageView)view.findViewById(R.id.view_dialog_drink_image_resource)).setImageResource(drink.image_resource);
		// // don't show image for now
		view.findViewById(R.id.view_dialog_drink_title).setTag(this.drink);

		// Setup up title and buttons
		builder.setView(view).setPositiveButton("Place order", this)
			.setNegativeButton("Cancel", this);
		builder.setTitle("Place your order");
		
		// Set radio button listeners
		view.findViewById(R.id.view_dialog_order_tip_10).setOnClickListener(this);
		view.findViewById(R.id.view_dialog_order_tip_15).setOnClickListener(this);
		view.findViewById(R.id.view_dialog_order_tip_20).setOnClickListener(this);

		// Set the tip amount based on the radio button selected (default is 20%)
		((EditText) view.findViewById(R.id.view_dialog_drink_tip_amount)).setText(df.format(drink.getPrice() * (float) 20 / (float) 100));

		// Set the  edit text listener which unselects radio buttons when the tip is entered manually
		((EditText) view.findViewById(R.id.view_dialog_drink_tip_amount)).setOnTouchListener(this);
		
		
		// Create dialog and set up animation
		return builder.create();
	}

	private void updateProfileView(UserProfile profile) {
		ImageView profileImageView = ((ImageView)view.findViewById(R.id.view_user_dialog_image_resource));
		
		if (!profile.hasImage()) {
			WebServices.downloadImage(profile, profileImageView);
		} else {
			profileImageView.setImageBitmap(profile.getImage());
		}
	
		// Show the username of the recipient
		if (profile.getBartsyId() == mApp.mProfile.getBartsyId())
			((TextView) view.findViewById(R.id.view_user_dialog_info)).setText("You");	
		else
			((TextView) view.findViewById(R.id.view_user_dialog_info)).setText(profile.getNickname());	
		
		// To pick more user profiles when user pressed on the image view
		profileImageView.setOnClickListener(this);
		
	}


	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.view_user_dialog_image_resource:
			PeopleSectionFragmentDialog dialog = new PeopleSectionFragmentDialog(){
				@Override
				protected void selectedProfile(UserProfile userProfile) {
					// Update profile with new selected profile
					DrinkDialogFragment.this.profile = userProfile;
					updateProfileView(userProfile);
					super.selectedProfile(userProfile);
				}
			};
			dialog.show(getActivity().getSupportFragmentManager(),"PeopleSectionDialog");
			break;
			
		case R.id.view_dialog_order_tip_10:
		case R.id.view_dialog_order_tip_15:
		case R.id.view_dialog_order_tip_20:
			
			float percent = 0;
			
			if (v.getId() == R.id.view_dialog_order_tip_10)
				percent = (float) 0.10;
			else if (v.getId() == R.id.view_dialog_order_tip_15)
				percent = (float) 0.15;
			else if (v.getId() == R.id.view_dialog_order_tip_20)
				percent = (float) 0.20;
			
			// Set the tip amount based on the radio button selected
			float price = drink.getPrice();
			((EditText) view.findViewById(R.id.view_dialog_drink_tip_amount)).setText(df.format(price * percent));

			break;
		}
	}


	@Override
	public void onClick(DialogInterface dialog, int id) {
		
		RadioGroup tipPercentage = (RadioGroup) view.findViewById(R.id.view_dialog_drink_tip);
		EditText percentage = (EditText) view.findViewById(R.id.view_dialog_drink_tip_amount);
		
		switch (id) {
		
		case DialogInterface.BUTTON_POSITIVE:

				// Send the positive button event back to the host activity

				int selected = tipPercentage.getCheckedRadioButtonId();

				// Gets a reference to our "selected" radio button
				RadioButton b = (RadioButton) tipPercentage.findViewById(selected);
				if (b == null || b.getText().toString().trim().length() == 0) {
					String tip="0";
					if (percentage.getText()!= null)
						tip = percentage.getText().toString();
					tipAmount = Float.parseFloat(tip) ;
				} else 
					tipAmount = Float.parseFloat(b.getText().toString().replace("%", "")) / (float) 100 * drink.getPrice();

				mListener.onDialogPositiveClick(DrinkDialogFragment.this);
				break;

		case DialogInterface.BUTTON_NEGATIVE:
			break;
		}
	}

	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		switch (arg0.getId()) {
		case R.id.view_dialog_drink_tip_amount:
			((RadioGroup) view.findViewById(R.id.view_dialog_drink_tip)).clearCheck();
			break;
		}
		return false;
	}

}
