/**
 * 
 */
package com.kellislabs.bartsy;

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

import com.kellislabs.bartsy.model.MenuDrink;

/**
 * @author peterkellis
 *
 */
public class DrinkDialogFragment extends DialogFragment  {

	public MenuDrink drink;
	
	
    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
    }
    
    // Use this instance of the interface to deliver action events
    NoticeDialogListener mListener;
    
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
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
	    
	    
	    
	    View view = inflater.inflate(R.layout.dialog_drink_order, null);
	    
	    // Customize dialog for this drink
	    ((TextView)view.findViewById(R.id.view_dialog_drink_title)).setText(drink.getTitle());
	    ((TextView)view.findViewById(R.id.view_dialog_drink_description)).setText(drink.getDescription());
	    ((TextView)view.findViewById(R.id.view_dialog_drink_price)).setText(drink.getPrice());
//	    ((ImageView)view.findViewById(R.id.view_dialog_drink_image_resource)).setImageResource(drink.image_resource);
	    view.findViewById(R.id.view_dialog_drink_title).setTag(this.drink);
	    
	    builder.setView(view)
	    // Add action buttons
	        .setPositiveButton("Order", new DialogInterface.OnClickListener() {
	        	
	            @Override
	            public void onClick(DialogInterface dialog, int id) {
                    // Send the positive button event back to the host activity
                    mListener.onDialogPositiveClick(DrinkDialogFragment.this);
	            }
        });      
	    return builder.create();
	}
}
