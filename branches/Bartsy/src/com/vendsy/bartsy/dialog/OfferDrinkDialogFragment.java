package com.vendsy.bartsy.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;
/**
 * 
 * @author Seenu Malireddy
 *
 */
public class OfferDrinkDialogFragment extends SherlockDialogFragment {

	private BartsyApplication mApp;
	private View rootView;
	
	public static final int OFFER_ACCEPTED = 0;
	public static final int OFFER_REJECTED = 8;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Create dialog and set animation styles
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());// ,
		
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		rootView = inflater.inflate(R.layout.offer_drink_dialog, null);
		
		mApp = (BartsyApplication) getActivity().getApplication();
		
		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout

		// Customize dialog for this drink
		((TextView) rootView.findViewById(R.id.view_dialog_drink_title))
						.setText(mApp.drinkOffered.title);
		((TextView) rootView.findViewById(R.id.view_dialog_drink_description))
						.setText(mApp.drinkOffered.description);
		((TextView) rootView.findViewById(R.id.view_dialog_drink_price)).setText(""
						+ mApp.drinkOffered.baseAmount);
		// To set self profile information by default
		if(mApp.drinkOffered.orderSender!=null){
			updateProfileView(mApp.drinkOffered.orderSender);
		}
		
		builder.setView(rootView)
		// Add action buttons
				.setPositiveButton("Accept",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) {

								updateOfferedDrinkStatusSysCall(OFFER_ACCEPTED);
							}
						})
				.setNegativeButton("Reject", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int id) {
								
								updateOfferedDrinkStatusSysCall(OFFER_REJECTED);
								
							}
				});

		// Create dialog and set up animation
		return builder.create();
		
	}
	/**
	 * Sys call for update Offered Drink Status
	 */
	protected void updateOfferedDrinkStatusSysCall(final int orderStatus) {
		new Thread(){
			@Override
			public void run() {
				String response = WebServices.updateOfferDrinkStatus(getActivity(), mApp.mActiveVenue.getId(), mApp.drinkOffered, orderStatus, mApp.mProfile.getBartsyId());
			}
		}.start();
		
		//TODO Require to handle response
	}
	/**
	 * To update profile view
	 * 
	 * @param profile
	 */
	private void updateProfileView(UserProfile profile) {
		ImageView profileImageView = ((ImageView)rootView.findViewById(R.id.view_user_dialog_image_resource));
		
		if (!profile.hasImage()) {
			WebServices.downloadImage(Constants.DOMAIN_NAME + profile.getImagePath(), profile,
					profileImageView);
		} else {
			profileImageView.setImageBitmap(profile.getImage());
		}
		((TextView) rootView.findViewById(R.id.view_user_dialog_info))
		.setText(profile.getNickname());	
	}
	
}