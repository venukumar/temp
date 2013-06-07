package com.vendsy.bartsy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
/**
 * 
 * @author Seenu Malireddy
 *
 */
public class OrderCustomDrinkActivity extends Activity{
	
	// Progress dialog
	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order_item);
		
	}
	
	
}
