package com.vendsy.bartsy.dialog;

import com.vendsy.bartsy.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

public class LoginDialogFragment extends DialogFragment {
	
	// The parameters that the dialog sets up and passes to the listener below
	public String username;
	public String password;

   // Parameter passing variables
   LoginDialogListener mListener;		// point to the activity calling this dialog
   View view; 							// the view of this dialog

	   
   /* The activity that creates an instance of this dialog fragment must  implement this interface in order to 
    * receive event callbacks. Each method passes the DialogFragment in case the host needs to query it. */

	public interface LoginDialogListener {
       public void onDialogPositiveClick(LoginDialogFragment dialog);
       public void onDialogNegativeClick(LoginDialogFragment dialog);
   }
   
   
   /*
    *  Override the Fragment.onAttach() method to instantiate the NoticeDialogListener(non-Javadoc)
    * @see android.support.v4.app.DialogFragment#onAttach(android.app.Activity)
    */
	
   @Override
   public void onAttach(Activity activity) {
       super.onAttach(activity);
       // Verify that the host activity implements the callback interface
       try {
           // Instantiate the NoticeDialogListener so we can send events to the host
           mListener = (LoginDialogListener) activity;
       } catch (ClassCastException e) {
           // The activity doesn't implement the interface, throw exception
           throw new ClassCastException(activity.toString() + " must implement LoginDialogListener");
       }
   }
   
   
   /*
    * (non-Javadoc)
    * @see android.support.v4.app.DialogFragment#onCreateDialog(android.os.Bundle)
    */
   
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Create dialog and set animation styles
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		// Get the layout inflater
		LayoutInflater inflater = getActivity().getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		view =  inflater.inflate(R.layout.login_dialog, null);
		
		builder.setTitle("Login to Bartsy");
		
		// Add action buttons
		builder.setView(view)
			.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            	
	            	// Set return values 
	            	username = ((TextView) view.findViewById(R.id.dialog_login_username)).getText().toString();
	            	password = ((TextView) view.findViewById(R.id.dialog_login_password)).getText().toString();
	            	
	                // Send the positive button event back to the host activity
	                mListener.onDialogPositiveClick(LoginDialogFragment.this);
	            }
	        })
	        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	                // Send the negative button event back to the host activity
	                mListener.onDialogNegativeClick(LoginDialogFragment.this);
	            }
	        });
		
		// Create dialog and set up animation
		return builder.create();
	}

}