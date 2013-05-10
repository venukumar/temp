package com.kellislabs.bartsy.utils;

import java.security.PublicKey;

public class Constants {
	//public static final String DOMAIN_NAME = "http://192.168.0.109:8080";

	 public static final String DOMAIN_NAME = "http://54.235.76.180:8080";

	public static final boolean USE_ALLJOYN = false;

	// This is the url for getting the bars list from server
	public static final String URL_GET_BAR_LIST = DOMAIN_NAME
			+ "/Bartsy/venue/getMenu";
	// This is the url for posting the Profiles Data to server
	public static final String URL_POST_PROFILE_DATA = DOMAIN_NAME
			+ "/Bartsy/user/saveUserProfile";
	// This is the url for place the order
	public static final String URL_PLACE_ORDER = DOMAIN_NAME
			+ "/Bartsy/order/placeOrder";
	// This is the url for getting the venu list from server
	public static final String URL_GET_VENU_LIST = DOMAIN_NAME
			+ "/Bartsy/venue/getVenueList";
	// This is the url for User Check In
	public static final String URL_USER_CHECK_IN = DOMAIN_NAME
			+ "/Bartsy/user/userCheckIn";

	// This is the url for User Check Out
	public static final String URL_USER_CHECK_OUT = DOMAIN_NAME
			+ "/Bartsy/user/userCheckOut";

	// This is the url for saveVenueDetails for bartender
	public static final String URL_SAVE_VENUEDETAILS = DOMAIN_NAME
			+ "/Bartsy/venue/saveVenueDetails";
	// Android Device Type
	public static final int DEVICE_Type = 0;
	// This is the url for download the facebook picture
	public static final String FB_PICTURE="https://graph.facebook.com/";
}
