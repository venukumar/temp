package com.vendsy.bartsy.model;

import io.card.payment.CreditCard;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.Person.Emails;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;

public class UserProfile {

	static final String TAG = "Profile";
	

	// User login information
	private String login  = null; 			// required and doubles as email for the user
	private String bartsyId ;				// Unique ID enforced by Bartsy server
	private String password = null;			// required
			
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
	ArrayList<UserProfile> likes  = null;
	ArrayList<UserProfile> favorites  = null;

	// Advanced fields for "dating" profiles
	private String visibility = null;	
	private String firstName = null;
	private String lastName = null;
	private String birthday  = null;		// MM/DD/YYYY
	private String status = null; 			// relationship status ("single", "attached", 
	private String orientation = null;  	// sexual orientation

	// The view of a particular user in the people list (expects a layout type of user_item.xml)
	public View view = null; 	

	public static final String VISIBLE = "ON"; // show user profile and see other profiles
	public static final String HIDDEN = "OFF"; // don't show user profile and don't see other profiles
	
	private CreditCard cc = null;

	
	/**
	 * Default constructor
	 */
	public UserProfile() {

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
	public UserProfile (Person person, String email) {
		
		// Setup Google login parameters
		googleUsername = email;
		googleId = person.getId();

		name = person.getDisplayName();
		this.email = email;
		
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
		
		if(person.getName()!=null && person.getName().hasGivenName())
			firstName = person.getName().getGivenName();

		if (person.getNickname() != null) 
			nickname = person.getNickname();
		else
			nickname = firstName;

		if(person.getName() != null && person.getName().hasFamilyName())
			lastName = person.getName().getFamilyName();
		
		if (person.hasImage())
			setImagePath(person.getImage().getUrl());
			
	}
	
	
	/*
	 * Constructor using Facebook profile JSON as a base.  
	 * 
	 * @param person
	 * @return
	 */
	public UserProfile (JSONObject person) {

		try {
			setFacebookUsername(person.getString("username"));
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		try {
			setFacebookId(person.getString("id"));
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		
		if (person.has("first_name"))
			try {
				setFirstName(person.getString("first_name"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		if (person.has("last_name"))
			try {
				setLastName(person.getString("last_name"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		if (person.has("email"))
			try {
				setLastName(person.getString("email"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		
		if (person.has("bio"))
			try {
				setDescription(person.getString("bio"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		if (person.has("email"))
			try {
				setLogin(person.getString("email"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		if (person.has("id")){ 
			String id;
			try {
				id = person.getString("id");
				this.setImagePath(Constants.FB_PICTURE+id+"/picture");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
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

	public boolean hasLogin() {
		if (login == null || login.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
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

	public boolean hasPassword() {
		if (password == null || password.equalsIgnoreCase(""))
			return false;
		else 
			return true;
	}
		
	public String getPassword() {
		return password;
	}

	public void setPassword(String email) {
		this.password = email;
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
		if (image == null)
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

	public boolean hasCreditCard() {
		if (cc == null)
			return false;
		else 
			return true;
	}
	
	public CreditCard getCreditCard() {
		return cc;
	}

	public void setCreditCard(CreditCard cc) {
		this.cc = cc;
	}

	public void updateView(OnClickListener listener) {

		((ImageView) view.findViewById(R.id.view_user_list_image_resource)).setImageBitmap(getImage());
		((TextView) view.findViewById(R.id.view_user_list_name)).setText(getNickname());

		ImageView profileImageView = (ImageView) view
				.findViewById(R.id.ImageView16);

		if (image == null) {
			WebServices.downloadImage(Constants.DOMAIN_NAME + imagePath, this,
					profileImageView);
		} else {
			profileImageView.setImageBitmap(image);
		}

		view.setOnClickListener(listener);

		view.setTag(this);

	}
}
