package com.vendsy.bartsy.model;

import org.json.JSONObject;

/**
 * 
 * @author Seenu Malireddy
 *
 */
public class MessageData{
	
	private int id;
	private String body;
	private String senderId;
	private String receiverId;
	private String venueId;
	
	
	public JSONObject getJSONData(){
		JSONObject json = new JSONObject();
		try {
			json.put("venueId", getVenueId());
			json.put("senderId", getSenderId());
			json.put("receiverId", getReceiverId());
			json.put("message", getBody());
		} catch (Exception e1) {
			e1.printStackTrace();
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
	
}
