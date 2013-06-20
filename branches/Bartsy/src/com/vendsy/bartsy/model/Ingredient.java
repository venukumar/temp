package com.vendsy.bartsy.model;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * Ingredient model
 * 
 * @author Seenu Malireddy
 */

public class Ingredient {
	
	private long id;
	private String name;
	private int price;
	private boolean availability;
	
	public Ingredient(JSONObject json) {
		try {
			id = json.getInt("ingredientId");
			name = json.getString("name");
			price = json.getInt("price");
			
			json.put("name", name);
			json.put("price", String.valueOf(price));
			json.put("available", String.valueOf(availability));
		} catch (JSONException e) {	}
	}
	
	/**
	 * It will return in the JSON format which is used in the web service call
	 * 
	 * @return
	 */
	public JSONObject toJSON(){
		JSONObject json = new JSONObject();
		try {
			json.put("ingredientId", String.valueOf(id));
			json.put("name", name);
			json.put("price", String.valueOf(price));
			json.put("available", String.valueOf(availability));
		} catch (JSONException e) {	}
		
		return json;
	}
	
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the price
	 */
	public int getPrice() {
		return price;
	}
	/**
	 * @param price the price to set
	 */
	public void setPrice(int price) {
		this.price = price;
	}
	
	/**
	 * @return the availability
	 */
	public boolean isAvailability() {
		return availability;
	}
	/**
	 * @param availability the availability to set
	 */
	public void setAvailability(boolean availability) {
		this.availability = availability;
	}
	
	
}
