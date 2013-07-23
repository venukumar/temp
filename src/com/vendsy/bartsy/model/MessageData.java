package com.vendsy.bartsy.model;

import java.util.Comparator;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Seenu Malireddy
 *
 */
public class MessageData implements Comparator<MessageData>{
	
	private int id;
	private String body;
	private String senderId;
	private String receiverId;
	private String venueId;
	private String createdDate;
	
	/**
	 * Default constructor
	 */
	public MessageData() {
	}
	
	/**
	 * Constructor to extract values from JSON Object
	 * 
	 * @param json
	 */
	public MessageData(JSONObject json) {
		try {
			if(json.has("id")){
				id = json.getInt("id");
			}
			body = json.getString("message");
			senderId = json.getString("senderId");
			receiverId = json.getString("receiverId");
			
			createdDate = json.getString("date");
		} catch (JSONException e) {
		}
	}
	/**
	 * Get Message in JSON format to post in Sys call
	 * 
	 * @return
	 */
	public JSONObject getJSONData(){
		JSONObject json = new JSONObject();
		try {
			json.put("venueId", getVenueId());
			json.put("senderId", getSenderId());
			json.put("receiverId", getReceiverId());
			json.put("message", getBody());
		} catch (Exception e) {
		}
		return json;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public String getSenderId() {
		return senderId;
	}
	
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
	
	public String getBody() {
		return body;
	}
	
	public void setBody(String body) {
		this.body = body;
	}
	
	public String getReceiverId() {
		return receiverId;
	}
	
	public void setReceiverId(String receiverId) {
		this.receiverId = receiverId;
	}
	
	public String getVenueId() {
		return venueId;
	}
	
	public void setVenueId(String venueId) {
		this.venueId = venueId;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	@Override
	public int compare(MessageData lhs, MessageData rhs) {
		if (lhs.getId() > rhs.getId())
			return 1;
		else
			return -1;
	}
	
}
