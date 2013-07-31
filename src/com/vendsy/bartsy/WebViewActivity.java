package com.vendsy.bartsy;

import com.actionbarsherlock.app.SherlockActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.webkit.WebView;
/**
 * 
 * @author Seenu Malireddy
 *
 */
@SuppressLint("SetJavaScriptEnabled")
public class WebViewActivity extends SherlockActivity {
	WebView webView;

	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.agreement_details_main);
		
		webView = (WebView) findViewById(R.id.webView);
		
		// Try to get link frim intent
		String link = getIntent().getExtras().getString("link");

		if (link != null) {
			webView.loadUrl(link);
			webView.getSettings().setBuiltInZoomControls(true);
			webView.getSettings().setSupportZoom(true);
			webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
			webView.getSettings().setJavaScriptEnabled(true);
			
		}
		
	}

}
