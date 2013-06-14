/**
 * 
 */
package com.vendsy.bartsy.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.vendsy.bartsy.R;
import com.vendsy.bartsy.model.MenuDrink;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;

/**
 * @author peterkellis
 * 
 */
public class DrinkDialogFragment extends DialogFragment {

	public MenuDrink drink;
	public UserProfile profile;
	public String tipPercentageValue;
	private View view;

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


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Create dialog and set animation styles
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());// ,
																				// R.style.DrinkDialog);

		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		view = inflater.inflate(R.layout.dialog_drink_order, null);

		// Customize dialog for this drink
		((TextView) view.findViewById(R.id.view_dialog_drink_title))
				.setText(drink.getTitle());
		((TextView) view.findViewById(R.id.view_dialog_drink_description))
				.setText(drink.getDescription());
		((TextView) view.findViewById(R.id.view_dialog_drink_price)).setText(""
				+ drink.getPrice());
		// To set self profile information by default
		if(profile!=null){
			updateProfileView(profile);
		}
		
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

								// Send the positive button event back to the
								// host activity

								// Returns an integer which represents the
								// selected radio button's ID
								int selected = tipPercentage
										.getCheckedRadioButtonId();

								// Gets a reference to our "selected" radio
								// button
								RadioButton b = (RadioButton) tipPercentage
										.findViewById(selected);
								if (b.getText().toString().trim().length() == 0) {
									tipPercentageValue = percentage.getText()
											.toString();
								} else {
									tipPercentageValue = b.getText().toString();
								}

								mListener
										.onDialogPositiveClick(DrinkDialogFragment.this);

							}
						});

		// Create dialog and set up animation
		return builder.create();
	}

	private void updateProfileView(UserProfile profile) {
		ImageView profileImageView = ((ImageView)view.findViewById(R.id.view_user_dialog_image_resource));
		
		if (!profile.hasImage()) {
			WebServices.downloadImage(Constants.DOMAIN_NAME + profile.getImagePath(), profile,
					profileImageView);
		} else {
			profileImageView.setImageBitmap(profile.getImage());
		}
		((TextView) view.findViewById(R.id.view_user_dialog_info))
		.setText(profile.getNickname());	
		
		
		
		// To pick more user profiles when user pressed on the image view
		profileImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
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
			}
		});
		
		
	}
}
