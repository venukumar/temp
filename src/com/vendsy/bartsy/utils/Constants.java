package com.vendsy.bartsy.utils;

public class Constants {

	public static final boolean USE_ALLJOYN = false;

	// Android Device Type
	public static final int DEVICE_Type = 0;

	// Paypal Key
	public static final String PAYPAL_KEY = "APP-80W284485P519543T";

	// This is the url for download the facebook picture
	public static final String FB_PICTURE = "https://graph.facebook.com/";
	
	public static final String ASSETS_PATH = "file:///android_asset/";
	
	// For now use hard-coded value of tax
	public static final float taxRate = (float) (9.5) / (float) (100.0);
	
	public static final long monitorFrequency = 60000 ; // frequency in which to run the background service, in ms
	public static final int timoutDelay = 2; // how many more minutes to delay a local timeout from the server timeout

	public static final boolean bundleOrders = false;
	
	public static final float defaultTip = (float) 20 / (float) 100;

}
