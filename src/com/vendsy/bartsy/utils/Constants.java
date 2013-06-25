package com.vendsy.bartsy.utils;

public class Constants {

//	public static final String DOMAIN_NAME = "http://192.168.0.109:8080/";  // local machine
//	public static final String DOMAIN_NAME = "http://54.235.76.180:8080/";	// dev
	public static final String DOMAIN_NAME = "http://bartsy.techvedika.com/"; // prod

	
	public static final String PROJECT_NAME = "Bartsy/";

	public static final boolean USE_ALLJOYN = false;

	// This is the url for getting the bars list from server
	public static final String URL_GET_BAR_LIST = DOMAIN_NAME + PROJECT_NAME
			+ "venue/getMenu";
	// This is the url for getting the venu list from server
	public static final String URL_GET_VENU_LIST = DOMAIN_NAME + PROJECT_NAME
			+ "venue/getVenueList";
	
	// This is the url for to get the list of checked in users
	public static final String URL_LIST_OF_CHECKED_IN_USERS = DOMAIN_NAME
			+ PROJECT_NAME + "data/checkedInUsersList";
	// This is the url for to sync data with customer app
	public static final String URL_LIST_OF_USER_ORDERS = DOMAIN_NAME
			+ PROJECT_NAME + "data/getUserOrders";
	
	// Get user profile from login information
	public static final String URL_GET_USER_PROFILE = DOMAIN_NAME + PROJECT_NAME
			+ "user/getUserProfile";
	// Get user profile from login information
	public static final String URL_SYNC_USER_DETAILS = DOMAIN_NAME + PROJECT_NAME
			+ "user/syncUserDetails";
	// This is the url for posting the Profiles Data to server
	public static final String URL_POST_PROFILE_DATA = DOMAIN_NAME
			+ PROJECT_NAME + "user/saveUserProfile";
	// This is the url for User Check In
	public static final String URL_USER_CHECK_IN = DOMAIN_NAME + PROJECT_NAME
			+ "user/userCheckIn";
	// This is the url for User Check Out
	public static final String URL_USER_CHECK_OUT = DOMAIN_NAME + PROJECT_NAME
			+ "user/userCheckOut";
	// Response to heartbeat PN 
	public static final String URL_HEARTBEAT_RESPONSE = DOMAIN_NAME 
			+ PROJECT_NAME + "user/heartBeat";

	// This is the url for place the order
	public static final String URL_PLACE_ORDER = DOMAIN_NAME + PROJECT_NAME
			+ "order/placeOrder";
	
	// This is the url for place the order
	public static final String URL_GET_INGREDIENTS = DOMAIN_NAME + PROJECT_NAME
				+ "inventory/getIngredients";
	
	// To update offered drink status
	public static final String URL_UPDATE_OFFERED_DRINK = DOMAIN_NAME + PROJECT_NAME
			+ "order/updateOfferedDrinkStatus";

	// For setVenueStatus
	public static final String URL_GET_PAST_ORDERS = DOMAIN_NAME
				+ PROJECT_NAME + "order/getPastOrders";

	// Android Device Type
	public static final int DEVICE_Type = 0;

	// Paypal Key
	public static final String PAYPAL_KEY = "APP-80W284485P519543T";

	// This is the url for download the facebook picture
	public static final String FB_PICTURE = "https://graph.facebook.com/";
	
	// Current ApiVersion number
	public static final String 	API_VERSION="1";
	
	public static final String ASSETS_PATH = "file:///android_asset/";
	
	// For now use card-coded value of tax
	public static final float taxRate = (float) (9.0) / (float) (100.0);
}
