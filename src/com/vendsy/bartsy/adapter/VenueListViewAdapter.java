package com.vendsy.bartsy.adapter;
/**
 * @author Seenu malireddy
 */
import java.text.DecimalFormat;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.model.Venue;

public class VenueListViewAdapter extends ArrayAdapter<Venue> {

	private List<Venue> items;
	LocationManager lm = null;
	private Venue activeVenue = null;

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
			
		// Show people present if venue is open
		if (!venue.getStatus().equalsIgnoreCase("CLOSED")) {
			view = vi.inflate(R.layout.map_list_item, null);
			((TextView) view.findViewById(R.id.view_map_venue_people_count)).setText(Integer.toString(venue.getUserCount()));
			if (venue.getUserCount() == 1) 
				((TextView) view.findViewById(R.id.view_map_people_text)).setText(" person checked in");
			else
				((TextView) view.findViewById(R.id.view_map_people_text)).setText(" people checked in");
		} else {
			view = vi.inflate(R.layout.map_list_item_closed, null);
		}

		// Display basic venue details
		((TextView) view.findViewById(R.id.view_map_venue_name)).setText(venue.getName());
		((TextView) view.findViewById(R.id.view_map_venue_address)).setText(venue.getAddress());
		if (venue.getWifiName() == null)
			view.findViewById(R.id.view_map_wifi).setVisibility(View.GONE);

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
			df.setMaximumFractionDigits(2);
			df.setMinimumFractionDigits(2);
	
			// Display distance
			if(distance==-1){
				((TextView) view.findViewById(R.id.view_map_distance)).setText("-");
			}else{
				((TextView) view.findViewById(R.id.view_map_distance)).setText(df.format(distance));
			}
		} else {
			((TextView) view.findViewById(R.id.view_map_distance)).setText("-");
		}
		
		// If we're checked in display check-mark
		if (activeVenue != null && activeVenue.getId().equals(venue.getId())) {
			view.findViewById(R.id.view_map_venue_checked_in).setVisibility(View.VISIBLE);
		} else {
			view.findViewById(R.id.view_map_venue_checked_in).setVisibility(View.GONE);
		}
		
		return view;
	}
}