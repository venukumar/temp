package com.kellislabs.bartsy;

import java.text.DateFormat;
import java.util.Currency;
import java.util.Date;

import android.content.Context;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Drink  {

	String title, description;
	String price;
	String price_special;
	View view = null;
	int image_resource;
	
	
	public Drink (int image_resource, String title, String description, String price) {
		this.title = title;
		this.description = description;
		this.price = price;
		this.image_resource = image_resource;
	}

	void updateView (OnClickListener listener) {
		((ImageView) view.findViewById(R.id.view_drink_image)).setImageResource(this.image_resource);
		((TextView) view.findViewById(R.id.view_drink_title)).setText(this.title);
		((TextView) view.findViewById(R.id.view_drink_price)).setText("" + this.price);

		view.setOnClickListener(listener);
		
		view.setTag(this);
		
	}
	
}
