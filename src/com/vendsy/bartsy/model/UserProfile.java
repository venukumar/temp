package com.vendsy.bartsy.model;

import java.util.ArrayList;
import java.util.List;

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
	
	public int bartsyID;		// Unique ID enforced by Bartsy server
	public String userID; 		// Google user id
	public Bitmap image;		// user's main profile image
	public String username;		// user's first name / last name
	public String location;		// use string for now
	public String info;			// info string
	public String description="";
	private String name;
	private String email = "noreply@bartsy.com"; 
	private String gender = "Male";
	private String type;		// oneof {"Google", "Facebook", "Bartsy"}
	private String socialNetworkId;
	private String imagePath;
	ArrayList<UserProfile> likes = new ArrayList<UserProfile>();
	ArrayList<UserProfile> favorites = new ArrayList<UserProfile>();

	// Advanced fields for "dating" profiles
	// NULL is not acceptable in JSON format. So, we can use empty instead of null 
	public String firstName="";
	public String lastName="";
	public String dateofbirth="00/00/0000";
	public String nickname="";
	public String status="Single"; 		// relationship status
	public String orientation="Straight";  // sexual orientation

	// The view of a particular user in the people list (expects a layout type of user_item.xml)
	public View view = null; 	

	
	/**
	 * Default constructor
	 */
	public UserProfile() {

	}

	/**
	 * Constructor to set all profile information
	 * 
	 * @param userid
	 * @param username
	 * @param location
	 * @param info
	 * @param description
	 * @param image
	 * @param imagePath
	 */
	public UserProfile(int bartsyID, String userid, String username, String location,
			String info, String description, Bitmap image, String imagePath) {
		this.bartsyID = bartsyID;
		this.image = image;
		this.userID = userid;
		this.username = username;
		this.location = location;
		this.info = info;
		this.description = description;
		this.image = image;
		this.imagePath = imagePath;
	}
	
	/**
	 * Constructor using Google+ profile as a base.  
	 * 
	 * @param person
	 * @return
	 */
	public UserProfile (Person person, String email) {
		
		username = person.getId();
		name = person.getDisplayName();
		type = "google";
		socialNetworkId = person.getId();
//		gender = String.valueOf(person.getGender());
		
		if (person.getAboutMe() != null)
			description = person.getAboutMe();
		if (person.getBirthday() != null) {
			// Convert from yyyy-mm-dd to mm/dd/yyyy
			String bd = person.getBirthday();
			dateofbirth = bd.substring(5,7) + "/" + bd.substring(8, 10) + "/" + bd.substring(0, 4);
			Log.v(TAG, "Dateofbirth: " + bd + " -> " + dateofbirth);
		}
		
		if(person.getName()!=null && person.getName().hasGivenName())
			firstName = person.getName().getGivenName();

		if (person.getNickname() != null) 
			nickname = person.getNickname();
		else
			nickname = firstName;

		if(person.getName() != null && person.getName().hasFamilyName())
			lastName = person.getName().getFamilyName();
		this.email = email;
	}


	public String getSocialNetworkId() {
		return socialNetworkId;
	}

	public void setSocialNetworkId(String socialNetworkId) {
		this.socialNetworkId = socialNetworkId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFirstName(String name) {
		this.firstName = name;
	}

	public void setLastName(String name) {
		this.lastName = name;
	}

	public void setNickname(String name) {
		this.nickname = name;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public Bitmap getImage() {
		return image;
	}

	public void setImage(Bitmap image) {
		this.image = image;
	}

	public void updateView(OnClickListener listener) {

		((ImageView) view.findViewById(R.id.view_user_list_image_resource))
				.setImageBitmap(this.image);
		((TextView) view.findViewById(R.id.view_user_list_name))
				.setText(this.username);

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
