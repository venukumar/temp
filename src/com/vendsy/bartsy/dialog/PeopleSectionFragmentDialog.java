/**
 * 
 */
package com.vendsy.bartsy.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.view.PeopleListView;

/**
 * @author Seenu Malireddy
 * 
 */
public class PeopleSectionFragmentDialog extends SherlockDialogFragment{

	static final String TAG = "PeopleSectionFragmentDialog";
	
	View mRootView = null;
	public BartsyApplication mApp = null;

	private LayoutInflater mInflater;

	private PeopleListView mPeopleListView;


	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		// Create dialog and set animation styles
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());// ,
																				// R.style.DrinkDialog);

		// Get the layout inflater
		mInflater = getActivity().getLayoutInflater();

		// Inflate and set the layout for the dialog
		// Pass null as the parent view because its going in the dialog layout
		mRootView = mInflater.inflate(R.layout.people_tab, null);
		
		
		LinearLayout peopleLayout = (LinearLayout) mRootView.findViewById(R.id.view_singles);
		
		// Make sure the fragment pointed to by the activity is accurate
		mApp = (BartsyApplication) getActivity().getApplication();
		
		// Add People list view object to the liner layout
		mPeopleListView = new PeopleListView(getActivity(), mApp, mInflater){
			@Override
			protected void selectedUserProfile(UserProfile profile) {
				selectedProfile(profile);
			}
		};
						
		peopleLayout.addView(mPeopleListView);
		
		builder.setView(mRootView);
		builder.setTitle("Select order recipient");
		
		return builder.create();

	}

	protected void selectedProfile(UserProfile profile) {
		dismiss();
	}

}
