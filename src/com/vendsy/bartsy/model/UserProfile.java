package com.vendsy.bartsy.model;

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.plus.model.people.Person;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.Utilities;
import com.vendsy.bartsy.utils.WebServices;

public class UserProfile {

	static final String TAG = "Profile";
	

	// User login information
	private String bartsyLogin  = null; 	// required and doubles as email for the user
	private String bartsyId ;				// Unique ID enforced by Bartsy server
	private boolean localUser = false;		// It this the profile of the local user?
	private String bartsyPassword = null;	// required
			
	// Optional Google login parameters
	private String googleUsername = null;	
	private String googleId = null; 		// Google user id

	// Optional Facebook login parameters
	private String facebookUsername = null;	// user's Facebook username
	private String facebookId = null;
	
	// Required user information
	private Bitmap image = null;			// user's main profile image
	private String imagePath = null;
	private String nickname = null;
	private String email = null;

	// Optional user information
	public String location = null;			// use string for now
	public String info = null;				// info string
	public String description = null;
	private String name = null;
	private String gender  = null;
	private String creditCardNumberEncrypted = null;
	private String redactedCardNumber = null;// Show instead of credit card number 
	private String expMonth = null;
	private String expYear = null;

	// Advanced fields for "dating" profiles
	private String visibility = null;	
	private String firstName = null;
	private String lastName = null;
	private String birthday  = null;		// MM/DD/YYYY
	private String age  = null;
	private String status = null; 			// relationship status ("single", "attached", 
	private String orientation = null;  	// sexual orientation

	// The view of a particular user in the people list (expects a layout type of user_item.xml)
	public View view = null; 	

	public static final String VISIBLE = "ON"; // show user profile and see other profiles
	public static final String HIDDEN = "OFF"; // don't show user profile and don't see other profiles
	
	public static final String TYPE_MESSAGE_NEW = "New";
	public static final String TYPE_MESSAGE_OLD = "Old";
	public static final String TYPE_MESSAGE_NONE = "None";
	
	private String messagesStatus;
	
	ArrayList<UserProfile> likes  = null;
	ArrayList<UserProfile> favorites  = null;

	private boolean imageDownloaded;
	
	/**
	 * Default constructor
	 */
	public UserProfile() {

	}

	@Override
	public String toString() {
		
		
		return  "{" +
				(!hasBartsyLogin() ? "" : "bartsyLogin: " + bartsyLogin) + 
				(!hasBartsyId() ? "" : ", bartsyId: " + bartsyId) +
				(!hasPassword() ? "" : ", bartsyPassword: " + bartsyPassword) +
				(!hasGoogleUsername() ? "" : ", googleUsername: " + googleUsername) +
				(!hasGoogleId() ? "" : ", googleId: " + googleId) +
				(!hasFacebookUsername() ? "" : ", facebookUsername: " + facebookUsername) +
				(!hasFacebookId() ? "" : ", facebookId: " + facebookId) +
				(!hasImage() ? "Image: <**NOT AVAILABE**>" : ", Image: <available>") +
				(!hasImagePath() ? "" : ", imagePath: " + imagePath) +
				(!hasNickname() ? "" : ", nickname: " + nickname) +
				(!hasEmail() ? "" : ", email: " + email) +
				(!hasDescription() ? "" : ", description: " + description) +
				(!hasName() ? "" : ", name: " + name) +
				(!hasGender() ? "" : ", gender: " + gender) +
				(!hasCreditCardNumberEncrypted() ? "" : ", creditCardNumber: ****" + creditCardNumberEncrypted.substring(12)) +
				(!hasExpMonth() ? "" : ", expMonth: **") +
				(!hasExpYear() ? "" : ", expYear: **") +
				(!hasVisibility() ? "" : ", visibility: " + visibility) +
				(!hasFirstName() ? "" : ", firstName: " + firstName) +
				(!hasLastName() ? "" : ", lastName: " + lastName) +
				(!hasBirthday() ? "" : ", birthday : " + birthday) +
				(!hasStatus() ? "" : ", status: " + status) +
				(!hasOrientation() ? "" : ", orientation: " + orientation) +
				"}";
	}
	
	/**
	 * 
	 * TODO - Constructors
	 * 
	 */
	
	
	/*
	 * Constructor using Google+ profile as a base.  
	 * 
	 * @param person
	 * @return
	 */
	public UserProfile (Person person, String accountName) {
		
		// Setup Google login parameters
		googleUsername = accountName;
		googleId = person.getId();

		name = person.getDisplayName();
		email = accountName;
		
		if (person.hasGender()) {
			switch (person.getGender()) {
			case Person.Gender.MALE:
				gender = "male";
				break;
			case Person.Gender.FEMALE:
				gender = "female";
				break;
			case Person.Gender.OTHER:
				gender = "other";
				break;
			}
		}
				
		if (person.getAboutMe() != null)
			description = person.getAboutMe();
		if (person.getBirthday() != null) {
			// Convert from yyyy-mm-dd to mm/dd/yyyy
			String bd = person.getBirthday();
			birthday = bd.substring(5,7) + "/" + bd.substring(8, 10) + "/" + bd.substring(0, 4);
			Log.v(TAG, "Dateofbirth: " + bd + " -> " + birthday);
		}
		
		if(person.hasName() && person.getName().hasGivenName())
			firstName = person.getName().getGivenName();
		if(person.hasName() && person.getName().hasFamilyName())
			lastName = person.getName().getFamilyName();
		if (person.hasNickname()) 
			nickname = person.getNickname();
		else {
			if (Utilities.has(firstName))
				nickname = firstName;
			if (Utilities.has(lastName)) {
				if (Utilities.has(nickname))
					nickname += " " + lastName.substring(0, 1) + ".";
				else
					nickname = lastName.substring(0, 1) + ".";
			}
		}

		
		if (person.hasImage()) {
			String path = person.getImage().getUrl();

			// Get a bigger image if there's a size default
			int index = path.indexOf("?sz=");
			if (index != -1) {
				path = path.substring(0, index) + "?sz=200";
			}
			
			setImagePath(path);
		}
			
	}
	
	public boolean hasUnreadMessages(){
		return TYPE_MESSAGE_NEW.equals(messagesStatus);
	}
	
	public boolean hasReadMessages(){
		return TYPE_MESSAGE_OLD.equals(messagesStatus);
	}
	
	public boolean hasMessages(){
		return !TYPE_MESSAGE_NONE.equals(messagesStatus);
	}
	
	/**
	 * Constructor using Facebook profile as a base.  
	 * 
	 * @param user
	 * @return
	 */


	/*
	 * Constructor using server-sent JSON as a base.  
	 * 
	 * @param person
	 * @return
	 */
	public UserProfile (JSONObject json) throws JSONException {

		if (json.has("nickName"))
			nickname = json.getString("nickName");
		if (json.has("gender"))
			gender = json.getString("gender");
		if (json.has("bartsyId"))
			bartsyId = json.getString("bartsyId");
		if (json.has("userImagePath")) 
			imagePath = WebServices.DOMAIN_NAME + json.getString("userImagePath");
		if (json.has("hasMessages"))
			messagesStatus = json.getString("hasMessages");

		
		if (json.has("email"))
			setLastName(json.getString("email"));
		
		if (json.has("bio"))
			setDescription(json.getString("bio"));
		if (json.has("email"))
			setBartsyLogin(json.getString("email"));
		if (json.has("id")) { 
			String id;
			id = json.getString("id");
			this.setImagePath(Constants.FB_PICTURE+id+"/picture");
		}
		if(json.has("hasMessages"))
			messagesStatus = json.getString("hasMessages");
		
		updatePublicInfo(json);
	}
	
	/*
	 * Parse the json format and to set the values for the public information in the user profile
	 */
	public void updatePublicInfo(JSONObject json) throws JSONException{
		if (json.has("gender"))
			gender=json.getString("gender");
		if (json.has("age"))
			age=json.getString("age");
		if (json.has("orientation"))
			orientation=json.getString("orientation");
		if (json.has("showProfile"))
			visibility=json.getString("showProfile");
		if (json.has("status"))
			status=json.getString("status");
		if (json.has("description"))
			description=json.getString("description");

		// This is not useful
		if (json.has("currentTime"))
			json.getString("currentTime");
	}
	

	/**
	 * 
	 * TODO - Views
	 */

	public View listView(LayoutInflater inflater, OnClickListener listener, HashMap<String, Bitmap> cache) {
		
		View view = updateView(inflater.inflate(R.layout.user_item, null), cache);
		view.setOnClickListener(listener);
		view.setTag(this);
		return view;
	}
	
	public View dialogView(LayoutInflater inflater, HashMap<String, Bitmap> cache) {
		
		// Inflate and set the layout for the dialog. Pass null as the parent view because its going in the dialog layout
		View view = inflater.inflate(R.layout.user_profile_dialog, null);
		
		view = updateView(view, cache);
		
		// Each dialog knows the user its displaying
		view.findViewById(R.id.user_profile_name).setTag(this);

		return view;
	}

	public View updateView(View view, HashMap<String, Bitmap> cache) {

		// Show name
		((TextView) view.findViewById(R.id.user_profile_name)).setText(getNickname());
		

		// Snow user image
		ImageView profileImageView = (ImageView) view.findViewById(R.id.user_profile_image);
		if (image == null) {
			WebServices.downloadImage(this, profileImageView, cache);
		} else {
			profileImageView.setImageBitmap(image);
		}
		
		// Show messages
		if (localUser || !hasMessages()) {
			// User can not send message to self
			view.findViewById(R.id.user_profile_chat_field).setVisibility(View.GONE);
		} else {
			// Show message icon (pink for new messages) and show text
			if(hasReadMessages()) {
				((ImageView) view.findViewById(R.id.user_mail_icon)).setImageResource(R.drawable.mail_read);
				((TextView) view.findViewById(R.id.user_chat_text)).setText("You've chatted");
			} else {
				((ImageView) view.findViewById(R.id.user_mail_icon)).setImageResource(R.drawable.mail);
				((TextView) view.findViewById(R.id.user_chat_text)).setText("You have new chats!");
			}
		}

		// Show user info string
		String info = getAge() + " / " + getGender() + " / " + getOrientation();
		info.replace("null", "-");
		((TextView) view.findViewById(R.id.view_user_dialog_info)).setText(info);

		// Show user preference
		info = getStatus() + "";
		info.replace("null", "-");
		((TextView) view.findViewById(R.id.user_profile_preference)).setText(info);
		
		// Show description
		String description= getDescription();
		if(Utilities.has(description))
			((TextView)view.findViewById(R.id.view_user_dialog_description)).setText(description);

		// Return the view
		return view;
	}

	
	/**
	 * 
	 * TODO - Setters/Getters
	 * 
	 * @return
	 */
	
	public boolean hasBartsyId() {
		if (bartsyId == null || bartsyId.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}

	public String getBartsyId() {
		return bartsyId;
	}

	public void setBartsyId(String bartsyID) {
		this.bartsyId = bartsyID;
	}

	public boolean hasBartsyLogin() {
		if (bartsyLogin == null || bartsyLogin.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}

	public String getBartsyLogin() {
		return bartsyLogin;
	}

	public void setBartsyLogin(String login) {
		this.bartsyLogin = login;
	}

	public boolean hasFacebookId() {
		if (facebookId == null || facebookId.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}
	
	public String getFacebookId() {
		return facebookId;
	}

	public void setFacebookId(String facebookId) {
		this.facebookId = facebookId;
	}

	public boolean hasGoogleId() {
		if (googleId == null || googleId.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}
	
	public String getGoogleId() {
		return googleId;
	}

	public void setGoogleId(String googleId) {
		this.googleId = googleId;
	}

	public boolean hasGoogleUsername() {
		if (googleUsername == null || googleUsername.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}
	
	public String getGoogleUsername() {
		return googleUsername;
	}

	public void setGoogleUsername(String googleUsername) {
		this.googleUsername = googleUsername;
	}

	public boolean hasName() {
		if (name == null || name.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public boolean hasVisibility() {
		if (visibility == null || visibility.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}
	
	public String getVisibility() {
		return visibility;
	}
	
	public void setVisibility(String name) {
		this.visibility = name;
	}
	
	public boolean hasFirstName() {
		if (firstName == null || firstName.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String name) {
		this.firstName = name;
	}

	public String getLastName() {
		return lastName;
	}
	
	public boolean hasLastName() {
		if (lastName == null || lastName.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}
	
	public void setLastName(String name) {
		this.lastName = name;
	}

	public boolean hasNickname() {
		if (nickname == null || nickname.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public void setNickname(String name) {
		this.nickname = name;
	}

	public boolean hasEmail() {
		if (email == null || email.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String name) {
		this.email = name;
	}

	public boolean hasGender() {
		if (gender == null || gender.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}
	
	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public boolean hasCreditCardNumberEncrypted() {
		if (creditCardNumberEncrypted == null || creditCardNumberEncrypted.equalsIgnoreCase("") || creditCardNumberEncrypted.length()<13)
			return false;
		else 
			return true;
	}
	
	public String getCreditCardNumberEncrypted() {
		return creditCardNumberEncrypted;
	}

	public void setCreditCardNumberEncrypted(String creditCardNumber) {
		this.creditCardNumberEncrypted = creditCardNumber;
	}

	public boolean hasExpMonth() {
		if (expMonth == null || expMonth.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}
	
	public String getExpMonth() {
		return expMonth;
	}

	public void setExpMonth(String expMonth) {
		this.expMonth = expMonth;
	}

	public boolean hasExpYear() {
		if (expYear == null || expYear.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}
	
	public String getExpYear() {
		return expYear;
	}

	public void setExpYear(String expYear) {
		this.expYear = expYear;
	}

	public boolean hasPassword() {
		if (bartsyPassword == null || bartsyPassword.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}
		
	public String getPassword() {
		return bartsyPassword;
	}

	public void setBartsyPassword(String email) {
		this.bartsyPassword = email;
	}

	public boolean hasFacebookUsername () {
		return facebookUsername != null;
	}

	public String getFacebookUsername() {
		return facebookUsername;
	}

	public void setFacebookUsername(String username) {
		this.facebookUsername = username;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public boolean hasDescription() {
		if (description == null || description.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean hasBirthday() {
		if (birthday == null || birthday.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}
	
	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public boolean hasStatus() {
		if (status == null || status.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String birthday) {
		this.status = birthday;
	}


	public boolean hasOrientation() {
		if (orientation == null || orientation.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}
	
	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public boolean hasImagePath() {
		if (imagePath == null || imagePath.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public boolean hasImage() {
		if (image == null && !imageDownloaded)
			return false;
		else 
			return true;
	}
	
	public Bitmap getImage() {
		return image;
	}

	public void setImage(Bitmap image) {
		this.image = image;
	}

	public boolean hasRedactedCardNumber() {
		return !(redactedCardNumber == null || redactedCardNumber.equals(""));
	}

	public String getRedactedCardNumber() {
		return redactedCardNumber;
	}

	public void setRedactedCardNumber(String creditCardDisplay) {
		this.redactedCardNumber = creditCardDisplay;
	}
	
	public String getMessagesStatus() {
		return messagesStatus;
	}

	public void setMessagesStatus(String messagesStatus) {
		this.messagesStatus = messagesStatus;
	}
	
	public boolean isImageDownloaded() {
		return imageDownloaded;
	}

	public void setImageDownloaded(boolean imageDownloaded) {
		this.imageDownloaded = imageDownloaded;
	}

}
