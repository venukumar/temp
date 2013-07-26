package com.vendsy.bartsy.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.j256.ormlite.field.DatabaseField;
import com.vendsy.bartsy.R;

/**
 * @author Seenu
 * 
 *         A MenuDrink object we are creating.
 */
public class Item {
	
	public static final int TYPE_PAST_ORDER = 0;
	public static final int TYPE_FAVORITE = 1;
	public static final int TYPE_MIXED_DRINK = 2;
	public static final int TYPE_COCKTAIL = 3;
	public static final int TYPE_LOCU_ITEM = 4;
	
	public boolean isDummyLoadingItem; // It is require to add dummy item to display progress view in the Expandable list view

	public String valid = null;
	
	private String itemId;
	private String title;
	private String description;
	private float price;
	private String specialPrice;
	private String venueId;
	private ArrayList<OptionGroup> optionGroups=new ArrayList<Item.OptionGroup>();
	
	public class OptionGroup {
		protected String type;
		protected String text;
		protected ArrayList<String> options = new ArrayList<String>();
		
		public OptionGroup (JSONObject json) throws JSONException {
			if (json.has("type"))
				type = json.getString("type");
			if (json.has("text"))
				text = json.getString("text");
			
			if (json.has("options")) {
				JSONArray optionsJSON = json.getJSONArray("options");
				options = new ArrayList<String>();
				for (int i = 0 ; i < optionsJSON.length() ; i++) {
					options.add(optionsJSON.getString(i));
				}
			}
		}
		
		
		
		public ArrayList<String> getOptions() {
			return options;
		}



		public void setOptions(ArrayList<String> options) {
			this.options = options;
		}



//		public class Option {
//			
//			protected String name;
//			protected String price;
//			
//			public Option(String name){
//				this.name = name;
//			}
//			
//			public Option(JSONObject json) throws JSONException {
//				if (json.has("name"))
//					name = json.getString("name");
//				if (json.has("price"))
//					price = json.getString("price");
//			}
//
//			public String getName() {
//				return name;
//			}
//
//			public void setName(String name) {
//				this.name = name;
//			}
//
//			public String getPrice() {
//				return price;
//			}
//
//			public void setPrice(String price) {
//				this.price = price;
//			}
//			
//		}
	}
	
	public Item () {
	}
	
	public Item(String title, String description, float price) {
		this.title = title;
		this.description = description;
		this.price = price;
	}
	
	public Item(JSONObject object) {
		try {
			
			// Process items of type Locu MENU_ITEM only 
			//TODO commented for now
//			if (object.has("type")){
//				if (!object.getString("type").equals("ITEM"))
//					return;
//			}
			
			if (object.has("name"))
				this.title = object.getString("name");
			if (object.has("itemName"))
				this.title = object.getString("itemName");

			if (object.has("description"))
				this.description = object.getString("description");

			if (object.has("price"))
				this.price = Float.parseFloat(object.getString("price"));
			if (object.has("basePrice"))
				this.price = Float.parseFloat(object.getString("basePrice"));

			if (object.has("id"))
				this.itemId = object.getString("id");
			if (object.has("itemId"))
				this.itemId = object.getString("itemId");
			
			// Parse options
			if (object.has("options_groups")) {
				JSONArray optionGroupsJSON = object.getJSONArray("options_groups");
				optionGroups = new ArrayList<OptionGroup>();
				for (int i = 0 ; i < optionGroupsJSON.length() ; i++) {
					JSONObject optionGroupJSON = optionGroupsJSON.getJSONObject(i);
					optionGroups.add(new OptionGroup(optionGroupJSON));
				}
			}
			
			valid = "yes";

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}


	public String getItemId() {
		return itemId;
	}

	public void setItemId(String drinkId) {
		this.itemId = drinkId;
	}

	
	/**
	 * @return the venueId
	 */
	public String getVenueId() {
		return venueId;
	}

	/**
	 * @param venueId the venueId to set
	 */
	public void setVenueId(String venueId) {
		this.venueId = venueId;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return float the price
	 */
	public float getPrice() {
		return price;
	}

	/**
	 * @param price
	 *            the price to set
	 */
	public void setPrice(float price) {
		this.price = price;
	}

	/**
	 * @return the price_special
	 */
	public String getPrice_special() {
		return specialPrice;
	}

	/**
	 * @param price_special
	 *            the price_special to set
	 */
	public void setPrice_special(String price_special) {
		this.specialPrice = price_special;
	}

	public ArrayList<OptionGroup> getOptionGroups() {
		return optionGroups;
	}

	public void setOptionGroups(ArrayList<OptionGroup> optionGroups) {
		this.optionGroups = optionGroups;
	}


}
