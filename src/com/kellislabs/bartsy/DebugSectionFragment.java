/**
 * 
 */
package com.kellislabs.bartsy;

import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.DateFormat;
import java.util.Date;

/**
 * @author peterkellis
 *
 */
public class DebugSectionFragment extends Fragment implements OnClickListener {

	private View mRootView = null;
	private TextView mDBTextView = null;
	private String mDBText = "";
	
	/*
	 * Creates a map view, which is for now a mock image. Listen for clicks on the image
	 * and toggle the bar details image
	 */ 
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if (mDBTextView == null) {
			mRootView = inflater.inflate(R.layout.debug_main, container, false);
			mDBTextView = (TextView) mRootView.findViewById(R.id.debug_text);
			mDBTextView.setText(mDBText);
			
	        ScrollView scrollView = (ScrollView) mRootView.findViewById(R.id.debugScrollView);
	        scrollView.fullScroll(android.view.View.FOCUS_DOWN );
		}
		return mRootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mRootView=null;
		mDBTextView=null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroyView();
		mDBText = "";
	}

	@Override 
	public void onClick(View v) {
		// TODO Auto-generated method stub
	
	}

	public void appendLine(String text) {
		
		String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
		
		if (mDBTextView == null) {
			mDBText = mDBText + "\n" + currentDateTimeString + ": " + text;
		} else {
			mDBText = mDBText + "\n" + currentDateTimeString + ": " + text;
	        mDBTextView.setText(mDBTextView.getText().toString() + "\n" +  currentDateTimeString + ": " + text);

	        ScrollView scrollView = (ScrollView) mRootView.findViewById(R.id.debugScrollView);
	        scrollView.fullScroll(android.view.View.FOCUS_DOWN );
		}
	}
	

	
}
