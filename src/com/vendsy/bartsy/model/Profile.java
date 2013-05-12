package com.vendsy.bartsy.model;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.vendsy.bartsy.R;

public class Profile  {

	public String userID;			// Unique ID enforced by Bartsy service
	public Bitmap image;			// user's main profile image
	public String username; 		// user's first name / last name
	public String location;		// use string for now
	public String info;			// info string
	public String description;
	private String name;
	private String email;
	private String gender;
	private String type;
	private String socialNetworkId;

	ArrayList<Profile> likes = new ArrayList<Profile>();
	ArrayList<Profile> favorites = new ArrayList<Profile>();
//	ArrayList<Message> messages = new ArrayList<Message>();
	
	public View view = null;		// the view of a particular user in a list, expect a layout type of user_item.xml
	
	public Profile (){
		
	}
	public Profile (String userid, String username, String location, 
			String info, String description, Bitmap image) {
		this.image = image;
		this.userID = userid;
		this.username = username;
		this.location = location;
		this.info = info;
		this.description = description;
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
	
	public void updateView (OnClickListener listener) {
		((ImageView) view.findViewById(R.id.view_user_list_image_resource)).setImageBitmap(this.image);
		((TextView) view.findViewById(R.id.view_user_list_name)).setText(this.username);

		view.setOnClickListener(listener);
		
		view.setTag(this);
		
	}
	
}
