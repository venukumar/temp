package com.vendsy.bartsy.adapter;
/**
 * @author Seenu malireddy
 */
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.model.Venue;
import com.vendsy.bartsy.utils.WebServices;

public class VenueListViewAdapter extends ArrayAdapter<Venue> {
	
	private final static String TAG = "VenueListAdapter";

	private List<Venue> items;
	LocationManager lm = null;
	private Venue activeVenue = null;
	private HashMap<String, Bitmap> venueImages = new HashMap<String, Bitmap>();

	public VenueListViewAdapter(Context context, int resource, List<Venue> items, LocationManager lm, Venue activeVenue) {

		super(context, resource, items);

		this.items = items;
		this.lm = lm;
		this.activeVenue = activeVenue;

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
			if (venue.getUserCount() == 1) 
				((TextView) view.findViewById(R.id.view_map_people_text)).setText(" checkin");
			else
				((TextView) view.findViewById(R.id.view_map_people_text)).setText(" checkins");
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
		
		// Compute distance to user
		if (venue.hasLatLong()) {
			String locationProvider = LocationManager.NETWORK_PROVIDER;
			Location currentLocation = lm.getLastKnownLocation(locationProvider);
			Location venueLocation = new Location(LocationManager.NETWORK_PROVIDER);
			venueLocation.setLatitude(Double.parseDouble(venue.getLatitude()));
			venueLocation.setLongitude(Double.parseDouble(venue.getLongitude()));
			float distance=-1;
			if(currentLocation!=null){
				distance = venueLocation.distanceTo(currentLocation) * (float) 0.00062137;
			}
	
			// Format distance
			DecimalFormat df = new DecimalFormat();
	
			// Display distance
			if (distance==-1) {
				((TextView) view.findViewById(R.id.view_map_distance)).setText("-");
			} else if (distance < 10 ){
				df.setMaximumFractionDigits(2);
				df.setMinimumFractionDigits(2);
				((TextView) view.findViewById(R.id.view_map_distance)).setText(df.format(distance));
			} else {
				df.setMaximumFractionDigits(0);
				df.setMinimumFractionDigits(0);
				((TextView) view.findViewById(R.id.view_map_distance)).setText(df.format(distance));
			}
		} else {
			((TextView) view.findViewById(R.id.view_map_distance)).setText("-");
		}
		
		// If we're checked in display check-mark
		if (activeVenue != null && activeVenue.getId().equals(venue.getId())) {
			view.findViewById(R.id.view_map_venue_checked_in).setVisibility(View.VISIBLE);
			view.findViewById(R.id.view_map_distance_option).setVisibility(View.GONE);
		} else {
			view.findViewById(R.id.view_map_venue_checked_in).setVisibility(View.GONE);
			view.findViewById(R.id.view_map_distance_option).setVisibility(View.VISIBLE);
		}
		
		return view;
	}
}