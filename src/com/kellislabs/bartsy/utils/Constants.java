package com.kellislabs.bartsy.utils;

public class Constants {

	// public static final String DOMAIN_NAME = "http://192.168.0.109:8080";

	public static final String DOMAIN_NAME = "http://54.235.76.180:8080";

	// This is the url for getting the bars list from server
	public static final String URL_GET_BAR_LIST = DOMAIN_NAME
			+ "/Bartsy/venue/getMenu";
	// This is the url for posting the Profiles Data to server
	public static final String URL_POST_PROFILE_DATA = DOMAIN_NAME
			+ "/Bartsy/user/saveUserProfile";
	//This is the url for place the order
	public static final String URL_PLACE_ORDER=DOMAIN_NAME+"/Bartsy/order/placeOrder";

	// Android Device Type
	public static final int DEVICE_Type = 0;
}
