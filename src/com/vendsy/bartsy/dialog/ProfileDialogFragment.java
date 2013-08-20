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
		view = inflater.inflate(R.layout.user_profile_dialog, null);
		
		if (mUser != null){
			// Customize dialog for this user
			((TextView) view.findViewById(R.id.view_user_dialog_name)).setText(mUser.getNickname());
			
	
			// Set up user image 
			((ImageView) view.findViewById(R.id.view_user_dialog_image_resource)).setImageBitmap(mUser.getImage());
//	    new DownloadImageTask().execute((ImageView)view.findViewById(R.id.view_user_dialog_image_resource));	  
	      
			updateMoreUserInformation();
			
	
			// Each dialog knows the user its displaying
			view.findViewById(R.id.view_user_dialog_name).setTag(this.mUser);
		}

		// Set view and add click listeners by calling the listeners in the calling activity
		builder.setView(view);
		
		
	    builder.setTitle("User Profile");
	    
	    builder.setPositiveButton("Send Drink", this);
	    builder.setNegativeButton("Send Message", this);

	    
	    getUserPublicDetailsSysCall();
	    
		return builder.create();
	}
	
    /**
     * method to update user profile information in the dialog view
     */
	private void updateMoreUserInformation() {
		// Set up user info string
		String info = mUser.getAge() + " / " + mUser.getGender() + " / " + mUser.getStatus() +" / "+mUser.getOrientation();
		info.replace("null", " - ");
		((TextView) view.findViewById(R.id.view_user_dialog_info)).setText(info);
		String description=mUser.getDescription();
		if(description!=null){
			((TextView)view.findViewById(R.id.view_user_dialog_description)).setText(description);
		}
	}
	
	//Web service call to fetch user public details to display User Profile Information.
	private void getUserPublicDetailsSysCall() {
		
		final BartsyApplication app=(BartsyApplication) getActivity().getApplication();
		
		// Background thread
				new Thread(){
					@Override
					public void run() {
						final String response = WebServices.userPublicDetails(app,mUser.getBartsyId());
						Log.d(TAG, "getUserPublicDetailsSysCall() Response: "+response);
						// Post handler to access UI 
						handler.post(new Runnable() {
							@Override
							public void run() {
								try {
									JSONObject json = new JSONObject(response);
									// Success response
									if(json.has("errorCode") && json.getInt("errorCode") ==0){
										mUser.parsePublicInfo(json);
                                    //to update user profile information from the sys call								
										updateMoreUserInformation();
									}// Error response
									else if(json.has("errorMessage")){
										Toast.makeText(getActivity(), json.getString("errorMessage"), Toast.LENGTH_SHORT).show();
									}
								}catch (JSONException e) {
									
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
