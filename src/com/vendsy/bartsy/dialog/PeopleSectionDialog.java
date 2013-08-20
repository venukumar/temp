/**
 * 
 */
package com.vendsy.bartsy.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;
import android.widget.LinearLayout;

import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.VenueActivity;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.view.PeopleListView;

/**
 * @author Seenu Malireddy
 * 
 */
public class PeopleSectionDialog extends Dialog{

	static final String TAG = "PeopleSectionFragmentDialog";
	

	private LinearLayout peopleLayout;

	private PeopleListView mPeopleListView;
	
	
	public PeopleSectionDialog(Activity context) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setCancelable(true);
		// Inflate and set the layout for the dialog
				// Pass null as the parent view because its going in the dialog layout
		setContentView(R.layout.people_tab);
				
				
		peopleLayout = (LinearLayout) findViewById(R.id.people_list);
				
		// Make sure the fragment pointed to by the activity is accurate
		BartsyApplication mApp = (BartsyApplication)context.getApplication();
		
		// Add People list view object to the layout
		mPeopleListView = new PeopleListView(context, mApp, getLayoutInflater(), ((VenueActivity) context).mImageCache){
			@Override
			protected void selectedUserProfile(UserProfile profile) {
				selectedProfile(profile);
			}
		};
				
		peopleLayout.addView(mPeopleListView);
		
	}
	
	protected void selectedProfile(UserProfile profile) {
		dismiss();
	}

}
