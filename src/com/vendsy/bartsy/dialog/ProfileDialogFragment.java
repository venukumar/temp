/**
 * 
 */
package com.vendsy.bartsy.dialog;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.utils.WebServices;

/**
 * @author peterkellis
 * 
 */
public class ProfileDialogFragment extends SherlockDialogFragment implements OnClickListener {

	public UserProfile mUser = null;
	private ProfileDialogListener mListener;
	private Handler handler = new Handler();
	private static final String TAG = "ProfileDialogFragment";

	/*
	 * The activity that creates an instance of this dialog fragment must
	 * implement this interface in order to receive event callbacks. Each method
	 * passes the DialogFragment in case the host needs to query it.
	 */
	public interface ProfileDialogListener {
		public void onUserDialogPositiveClick(ProfileDialogFragment dialog);
		public void onUserDialogNegativeClick(ProfileDialogFragment dialog);
	}

	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
		
		super.onAttach(activity);
		
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (ProfileDialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString() + " must implement ProfileDialogListener");
		}
	}

	DialogInterface dialog = null;
	private View view;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Inflate and set the layout for the dialog. Pass null as the parent view because its going in the dialog layout
		if (mUser == null)
			view = inflater.inflate(R.layout.user_profile_dialog, null);
		else
			view = mUser.dialogView(inflater, null);
			
		// Set view and add click listeners by calling the listeners in the calling activity
		builder.setView(view);
	    builder.setTitle("User Profile");
	    builder.setPositiveButton("Send Drink", this);
	    builder.setNegativeButton("Send Message", this);
	    
	    // Update details from server
	    getUserPublicDetailsSysCall(mUser);
	    
		return builder.create();
	}
	

	
	//Web service call to fetch user public details to display User Profile Information.
	private void getUserPublicDetailsSysCall(final UserProfile user) {
		
		final BartsyApplication app=(BartsyApplication) getActivity().getApplication();
		
		// Background thread
				new Thread(){
					@Override
					public void run() {
						final String response = WebServices.userPublicDetails(app,user.getBartsyId());
						Log.d(TAG, "getUserPublicDetailsSysCall() Response: "+response);
						// Post handler to access UI 
						handler.post(new Runnable() {
							@Override
							public void run() {
								try {
									JSONObject json = new JSONObject(response);
									// Success response
									if(json.has("errorCode") && json.getInt("errorCode") ==0){
										user.updatePublicInfo(json);
										//to update user profile information from the sys call								
										user.updateView(view, null);
									}// Error response
									else if(json.has("errorMessage")){
										Toast.makeText(getActivity(), json.getString("errorMessage"), Toast.LENGTH_SHORT).show();
									}
								}catch (JSONException e) {
									// Handle exception
									e.printStackTrace();
									Toast.makeText(getActivity(), "Unable to get profile details", Toast.LENGTH_SHORT).show();
								}
							}
						});
					}
				}.start();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_POSITIVE:
			mListener.onUserDialogPositiveClick(this);
			break;
		case DialogInterface.BUTTON_NEGATIVE:
			mListener.onUserDialogNegativeClick(this);
			break;
		}
	}

}
