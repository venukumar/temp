package com.vendsy.bartsy.adapter;
/**
 * @author Seenu malireddy
 */
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vendsy.bartsy.R;
import com.vendsy.bartsy.model.Venue;
import com.vendsy.bartsy.utils.WebServices;

public class VenueNetworksAdapter extends ArrayAdapter<Venue> {
	
	private final static String TAG = "VenueNetworksAdapter";

	private List<Venue> items;
	private HashMap<String, Bitmap> venueImages = new HashMap<String, Bitmap>();

	public VenueNetworksAdapter(Context context,  List<Venue> items) {

		super(context, R.layout.map_list_item, items);

		this.items = items;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = convertView;

		Venue venue = items.get(position);
		if (venue == null) 
			return view;

		// Setup view if needed
		LayoutInflater vi = LayoutInflater.from(getContext());
		view = vi.inflate(R.layout.map_list_item, null);
			
		// Show people present if venue is open
		if (!venue.getStatus().equalsIgnoreCase("CLOSED")) {

			view.findViewById(R.id.view_map_checkins).setVisibility(View.VISIBLE);
			view.findViewById(R.id.view_map_closed).setVisibility(View.GONE);
			
			((TextView) view.findViewById(R.id.view_map_venue_people_count)).setText(Integer.toString(venue.getUserCount()));
			
		} else {
			view.findViewById(R.id.view_map_checkins).setVisibility(View.GONE);
			view.findViewById(R.id.view_map_closed).setVisibility(View.VISIBLE);
			
			((TextView) view.findViewById(R.id.view_map_distance)).setTextColor(parent.getResources().getColor(android.R.color.darker_gray));
			((TextView) view.findViewById(R.id.view_map_venue_name)).setTextColor(parent.getResources().getColor(android.R.color.darker_gray));
		}
	
		// Display venue name
		((TextView) view.findViewById(R.id.view_map_venue_name)).setText(venue.getName());
		
		// Don't show country (why on earth is that there??) and zip
		String address = venue.getAddress();
		address = address.substring(0, address.indexOf(",United States,")).replace(",", ", ").replace(".",  "");
		((TextView) view.findViewById(R.id.view_map_venue_address)).setText(address);
		
		// Display wifi if available
		if (venue.getWifiName() == null)
			view.findViewById(R.id.view_map_wifi).setVisibility(View.GONE);

		// Download venue image
		if (venue.hasImagePath()) {
			Log.v(TAG, "Downloading " + venue.getName() + " image from " + venue.getImagePath());
			ImageView venueImage = ((ImageView)view.findViewById(R.id.view_map_venue_image));
			WebServices.downloadImage(venue.getImagePath(), venueImage, venueImages);
		} else {
			Log.v(TAG, venue.getName() + " has no image path");
		}
		
		return view;
	}
}