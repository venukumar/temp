package com.vendsy.bartsy.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Seenu Malireddy
 *
 */

public class Reward {
	
	private String venueId;
	private String venueName;
	private String address;
	private String venueImage;
	private String status;
	private String rewards;
	
	public Reward(JSONObject json) {
		try {
			venueId = json.getString("venueId");
			venueName = json.getString("venueName"); 
			venueImage = json.getString("venueImage"); 
			address = json.getString("address"); 
			rewards = json.getString("rewards"); 
			
		} catch (JSONException e) {
		}
	}
	
	public boolean hasVenueImage(){
		return venueImage!=null;
	}

	
	public String getVenueName() {
		return venueName;
	}

	public String getVenueImage() {
		return venueImage;
	}

	public String getVenueId() {
		return venueId;
	}

	public void setVenueId(String venueId) {
		this.venueId = venueId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRewards() {
		return rewards;
	}

	public void setRewards(String rewards) {
		this.rewards = rewards;
	}

	public void setVenueName(String venueName) {
		this.venueName = venueName;
	}

	public void setVenueImage(String venueImage) {
		this.venueImage = venueImage;
	}
	
}
