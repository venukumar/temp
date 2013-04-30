/**
 * 
 */
package com.kellislabs.bartsy;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.Person.Image;
import com.google.android.gms.plus.model.people.Person.Name;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author peterkellis
 *
 */
public class ProfileDialogFragment extends DialogFragment  {

	public Person mUser = null;
	
	
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface ProfileDialogListener {
        public void onUserDialogPositiveClick(DialogFragment dialog);
        public void onUserDialogNegativeClick(DialogFragment dialog);
    }
    
    // Use this instance of the interface to deliver action events
    ProfileDialogListener mListener;
    
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (ProfileDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ProfileDialogListener");
        }
    }	
	
	DialogInterface dialog = null;
    
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
   
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	    // Get the layout inflater
	    LayoutInflater inflater = getActivity().getLayoutInflater();
	
	    // Inflate and set the layout for the dialog
	    // Pass null as the parent view because its going in the dialog layout
	    
	    View view = inflater.inflate(R.layout.dialog_user_profile, null);
	    
	    // Customize dialog for this user
	    ((TextView)view.findViewById(R.id.view_user_dialog_name)).setText(mUser.getDisplayName());
	    ((TextView)view.findViewById(R.id.view_user_dialog_description)).setText(mUser.getAboutMe());
	
	    // Set up user image asynchronously (it will be displayed when downloaded)
	    new DownloadImageTask().execute((ImageView)view.findViewById(R.id.view_user_dialog_image_resource));	  
	    
	    // Set up user info string
	    String info = mUser.getBirthday() + " / " + mUser.getGender() + " / " + mUser.getRelationshipStatus();
	    ((TextView)view.findViewById(R.id.view_user_dialog_info)).setText(info);
	    
	    // Each dialog knows the user its displaying
	    view.findViewById(R.id.view_user_dialog_name).setTag(this.mUser);
	    
	    builder.setView(view)
	    // Add action buttons
	        .setPositiveButton("Accept profile", new DialogInterface.OnClickListener() {
	        	
	            @Override
	            public void onClick(DialogInterface dialog, int id) {
                    // Send the positive button event back to the host activity
                    mListener.onUserDialogPositiveClick(ProfileDialogFragment.this);
	            }})
	        .setNegativeButton("Edit profile", new DialogInterface.OnClickListener() {
	        	
	            @Override
	            public void onClick(DialogInterface dialog, int id) {
                    // Send the positive button event back to the host activity
                    mListener.onUserDialogNegativeClick(ProfileDialogFragment.this);
	            }});
	    return builder.create();
	}
	
	
	private class DownloadImageTask extends AsyncTask<ImageView, Integer, Bitmap> {
        // Do the long-running work in here

    	ImageView view = null;

        protected Bitmap doInBackground(ImageView... params) {
        	view = params[0];
        	Bitmap bitmap;
        	
        	if (mUser == null)
        		return null;
        	
    	    try {
    			Log.d("Bartsy", "About to decode image for dialog from URL: " + mUser.getImage().getUrl());

    		    bitmap = BitmapFactory.decodeStream((InputStream)new URL(mUser.getImage().getUrl()).getContent());
        	} catch (MalformedURLException e) {
    			e.printStackTrace();
    			Log.d("Bartsy", "Bad URL: " + mUser.getImage().getUrl());
    			return null;
        	} catch (IOException e) {
        		e.printStackTrace();
    			Log.d("Bartsy", "Could not download image from URL: " + mUser.getImage().getUrl());
    			return null;
        	}
    	    
    	    
			Log.d("Bartsy", "Image decompress successfully for dialog: " + mUser.getImage().getUrl());

    	    
			return bitmap;
        }

        // This is called each time you call publishProgress()
        protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
        }

        // This is called when doInBackground() is finished
        protected void onPostExecute(Bitmap result) {
//            showNotification("Downloaded " + result + " bytes");
        	if (view != null)
        		view.setImageBitmap(result);
        }
    }
}
