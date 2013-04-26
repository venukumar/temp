/**
 * 
 */
package com.kellislabs.bartsy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ToggleButton;

/**
 * @author peterkellis
 *
 */
public class NotificationsSectionFragment extends Fragment {

	View mRootView = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
 
		mRootView = inflater.inflate(R.layout.notifications_main, container, false);
		
		// These toggle buttons implement effectively a tab with the additional ability to hide all content
/*		
        mRootView.findViewById(R.id.button_singles).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                    	ToggleButton b = (ToggleButton) arg0;
                    	if (b.isChecked()) {
                    		mRootView.findViewById(R.id.singles).setVisibility(View.VISIBLE);
                    		mRootView.findViewById(R.id.friends).setVisibility(View.GONE);
                    		((ToggleButton) mRootView.findViewById(R.id.button_friends)).setChecked(false);

                    	} else {
                    		mRootView.findViewById(R.id.singles).setVisibility(View.GONE);                    		
                    	}
                    }});
		
        mRootView.findViewById(R.id.button_friends).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                    	ToggleButton b = (ToggleButton) arg0;
                    	if (b.isChecked()) {
                    		mRootView.findViewById(R.id.friends).setVisibility(View.VISIBLE);
                    		mRootView.findViewById(R.id.singles).setVisibility(View.GONE);  
                    		((ToggleButton) mRootView.findViewById(R.id.button_singles)).setChecked(false);
                    	} else {
                    		mRootView.findViewById(R.id.friends).setVisibility(View.GONE);                    		
                    	}
                    }});
		
*/
        
        return mRootView;
	}
	
	
}
