package com.vendsy.bartsy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

import com.vendsy.bartsy.utils.Constants;
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
		
		// Try to get buttons from the view.
		Button betaAgreementButton = (Button)findViewById(R.id.betaAgreement);
		Button endUserAgreementButton = (Button)findViewById(R.id.endUserAgreement);
		Button termsOfUseButton = (Button)findViewById(R.id.termsOfUse);
		Button privacyPolicyButton = (Button)findViewById(R.id.privacyPolicy);
		
		Button quitButton = (Button)findViewById(R.id.quitButton);
		final Button acceptButton = (Button)findViewById(R.id.acceptButton);
		
		// To set listeners for the buttons
		// Load BartsyBetaParticipantAgreement-2013-06-11.htm file in web view when user clicks on the beta agreement button
		betaAgreementButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadHTMLFile(Constants.ASSETS_PATH+"BartsyBetaParticipantAgreement-2013-06-11.htm");
			}
		});
		// Load BartsyEULA-2013-06-16.htm file in web view when user clicks on the end user agreement button
		endUserAgreementButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadHTMLFile(Constants.ASSETS_PATH+"BartsyEULA-2013-06-16.htm");
			}
		});
		// Load BartsyTermsofUse-2013-06-11.htm file in web view when user clicks on the termsOfUse button
		termsOfUseButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadHTMLFile(Constants.ASSETS_PATH+"BartsyTermsofUse-2013-06-11.htm");
			}
		});
		// Load BartsyPrivacyPolicy-2013-06-11.htm file in web view when user clicks on the end user agreement button
		privacyPolicyButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loadHTMLFile(Constants.ASSETS_PATH+"BartsyPrivacyPolicy-2013-06-11.htm");
			}
		});
		// Disable accept button by default
		acceptButton.setEnabled(false);
		
		final CheckBox agreementCheckBox = (CheckBox)findViewById(R.id.checkAgreement);
		agreementCheckBox.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				// Enable and disable accept button based on the check box
				if(agreementCheckBox.isChecked()){
					acceptButton.setEnabled(true);
				}else{
					acceptButton.setEnabled(false);
				}
				
			}
		});
		
		
		// Close the application when user selects on the quit button
		quitButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		// Allow the user to enter into app and user should select check box
		acceptButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent().setClass(NDAActivity.this, InitActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			}
		});
	}
	
	/**
	 * Load local HTML in web view activity based on the URL
	 *  
	 * @param url
	 */
	protected void loadHTMLFile(String url) {
		Intent intent = new Intent(NDAActivity.this, WebViewActivity.class);
		intent.putExtra("link", url);
		startActivity(intent);		
	}
	
}
