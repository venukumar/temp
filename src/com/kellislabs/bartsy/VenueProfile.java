package com.kellislabs.bartsy;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class VenueProfile  {

	String id;			// Unique ID enforced by Bartsy service
	Bitmap image;			// user's main profile image
	String name; 		// user's first name / last name
	String location;		// use string for now
	String info;			// info string
	String description;
	
	
	ArrayList<VenueProfile> likes = new ArrayList<VenueProfile>();
	ArrayList<VenueProfile> favorites = new ArrayList<VenueProfile>();
//	ArrayList<Message> messages = new ArrayList<Message>();
	
	View view = null;		// the view of a particular user in a list, expect a layout type of user_item.xml
	
	
	public VenueProfile (String id, String name, String location, 
			String info, String description, Bitmap image) {
		this.image = image;
		this.id = id;
		this.name = name;
		this.location = location;
		this.info = info;
		this.description = description;
	}

	void updateView (OnClickListener listener) {
		((ImageView) view.findViewById(R.id.view_user_list_image_resource)).setImageBitmap(this.image);
		((TextView) view.findViewById(R.id.view_user_list_name)).setText(this.name);

		view.setOnClickListener(listener);
		
		view.setTag(this);
		
	}
	
}
