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
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.model.UserProfile;

/**
 * @author peterkellis
 * 
 */
public class ProfileDialogFragment extends SherlockDialogFragment {

	public UserProfile mUser = null;

	/*
	 * The activity that creates an instance of this dialog fragment must
	 * implement this interface in order to receive event callbacks. Each method
	 * passes the DialogFragment in case the host needs to query it.
	 */
	public interface ProfileDialogListener {
		public void onUserDialogPositiveClick(DialogFragment dialog);
		public void onUserDialogNegativeClick(DialogFragment dialog);
	}

	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
	}

	DialogInterface dialog = null;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Inflate and set the layout for the dialog. Pass null as the parent view because its going in the dialog layout
		View view = inflater.inflate(R.layout.dialog_user_profile, null);
		
		if(mUser!=null){
			// Customize dialog for this user
			((TextView) view.findViewById(R.id.view_user_dialog_name)).setText(mUser.getNickname());
			((TextView) view.findViewById(R.id.view_user_dialog_description)).setText(mUser.getDescription());
	
			// Set up user image 
			((ImageView) view.findViewById(R.id.view_user_dialog_image_resource)).setImageBitmap(mUser.getImage());
	
			// Set up user info string
			String info = mUser.getBirthday() + " / " + mUser.getGender() + " / " + mUser.getStatus();
			((TextView) view.findViewById(R.id.view_user_dialog_info)).setText(info);
	
			// Each dialog knows the user its displaying
			view.findViewById(R.id.view_user_dialog_name).setTag(this.mUser);
		}

		// Set view and add click listeners by calling the listeners in the calling activity
		builder.setView(view);
		
		return builder.create();
	}

}
