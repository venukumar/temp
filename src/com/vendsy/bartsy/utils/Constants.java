package com.vendsy.bartsy.utils;


public class Constants {
	
	//public static final String DOMAIN_NAME = "http://192.168.0.109:8080/";

	public static final String DOMAIN_NAME = "http://54.235.76.180:8080/";
	
	public static final String PROJECT_NAME ="Bartsy/";

	public static final boolean USE_ALLJOYN = false;

	// This is the url for getting the bars list from server
	public static final String URL_GET_BAR_LIST = DOMAIN_NAME + PROJECT_NAME
			+ "venue/getMenu";
	// This is the url for posting the Profiles Data to server
	public static final String URL_POST_PROFILE_DATA = DOMAIN_NAME + PROJECT_NAME
			+ "user/saveUserProfile";
	// This is the url for place the order
	public static final String URL_PLACE_ORDER = DOMAIN_NAME + PROJECT_NAME
			+ "order/placeOrder";
	// This is the url for getting the venu list from server
	public static final String URL_GET_VENU_LIST = DOMAIN_NAME + PROJECT_NAME
			+ "venue/getVenueList";
	// This is the url for User Check In
	public static final String URL_USER_CHECK_IN = DOMAIN_NAME + PROJECT_NAME
			+ "user/userCheckIn";

	// This is the url for User Check Out
	public static final String URL_USER_CHECK_OUT = DOMAIN_NAME + PROJECT_NAME
			+ "user/userCheckOut";

	// Android Device Type
	public static final int DEVICE_Type = 0;
	
	// Paypal Key
	public static final String PAYPAL_KEY = "APP-80W284485P519543T";
	
	// This is the url for download the facebook picture
	public static final String FB_PICTURE="https://graph.facebook.com/";
}
