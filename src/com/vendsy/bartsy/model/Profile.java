package com.vendsy.bartsy.model;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.vendsy.bartsy.R;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;

public class Profile {

	public String userID; // Unique ID enforced by Bartsy service
	public Bitmap image; // user's main profile image
	public String username; // user's first name / last name
	public String location; // use string for now
	public String info; // info string
	public String description;
	private String name;
	private String email;
	private String gender;
	private String type;
	private String socialNetworkId;
	private String imagePath;
	ArrayList<Profile> likes = new ArrayList<Profile>();
	ArrayList<Profile> favorites = new ArrayList<Profile>();

	// ArrayList<Message> messages = new ArrayList<Message>();

	public View view = null; // the view of a particular user in a list, expect
								// a layout type of user_item.xml

	/**
	 * Default constructor
	 */
	public Profile() {

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
	public Profile(String userid, String username, String location,
			String info, String description, Bitmap image, String imagePath) {
		this.image = image;
		this.userID = userid;
		this.username = username;
		this.location = location;
		this.info = info;
		this.description = description;
		this.image = image;
		this.imagePath = imagePath;
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
