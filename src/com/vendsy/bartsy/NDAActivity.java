package com.vendsy.bartsy;

import android.app.Activity;
import android.os.Bundle;
/**
 * 
 * @author Seenu Malireddy
 *
 */
public class NDAActivity extends Activity{
	

	private BartsyApplication mApp;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.agreement_main);
		
		mApp = (BartsyApplication)getApplication();
		
		
		
		
	}
	
}
