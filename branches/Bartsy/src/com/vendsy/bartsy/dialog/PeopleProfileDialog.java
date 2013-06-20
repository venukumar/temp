/**
 * 
 */
package com.vendsy.bartsy.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.vendsy.bartsy.R;

/**
 * @author peterkellis
 *
 */
public class PeopleProfileDialog extends SherlockDialogFragment  {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
   
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();

	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    builder.setView(inflater.inflate(R.layout.dialog_user_profile, null));

	    return builder.create();
	}
}
