package com.vendsy.bartsy.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.j256.ormlite.field.DatabaseField;
import com.vendsy.bartsy.R;

/**
 * @author Seenu
 * 
 *         A MenuDrink object we are creating.
 */
public class Item {
	
	private static final String TAG = "Item";

	// Menu types
	private String type = null;
	public static final String MENU_ITEM 	 = "ITEM";
	public static final String ITEM_SELECT 	 = "ITEM_SELECT";
	public static final String SECTION_TEXT  = "SECTION_TEXT";
	public static final String BARTSY_ITEM 	 = "BARTSY_ITEM";
	public static final String LOADING		 = "LOADING";
	
	// For MENU_ITEM type
	private String itemId = null;
	private String name = null;
	private String description = null;
	private double price;
	private double basePrice;
	private double optionsPrice;
	private double specialPrice;
	private String venueId = null;
	private ArrayList<OptionGroup> optionGroups = null;
	
	// In addition to above for BARTSY_ITEM type
	private String glass = null;
	private String ingredients = null;
	private String instructions = null;
	private String category = null;
	
	// For SECTION_TEXT type
	private String text = null;
	
	
	/**
	 * TODO - Constructors / parsers
	 */
	
	public Item () {
		this.type = LOADING;
	}
	
	public Item(String title, String description, double price) {
		this.type = MENU_ITEM;
		this.name = title;
		this.description = description;
		this.price = price;
	}
	
	public Item(JSONObject json, HashMap<String, JSONObject> savedSelections) throws JSONException, NumberFormatException {
			
		// Find the type and parse the json accordingly
		if (json.has("type"))
			type = json.getString("type");
		else 
			// Default as in orders we don't yet get full item details
			type = MENU_ITEM;
		
		if (type.equals(MENU_ITEM) || type.equals(BARTSY_ITEM) || type.equals(ITEM_SELECT)) {
			
			// Parse MENU_ITEM type
			
			if (json.has("name"))
				this.name = json.getString("name");
			if (json.has("itemName"))
				this.name = json.getString("itemName");
	
			if (json.has("description"))
				this.description = json.getString("description");
	
			if (json.has("price"))
				this.basePrice = Double.parseDouble(json.getString("price"));
			if (json.has("basePrice"))
				this.basePrice = Double.parseDouble(json.getString("basePrice"));
			
			if (json.has("id"))
				this.itemId = json.getString("id");
			if (json.has("itemId"))
				this.itemId = json.getString("itemId");
			
			if (json.has("glass"))
				glass = json.getString("glass");
			if (json.has("ingredients"))
				ingredients = json.getString("ingredients");
			if (json.has("instructions"))
				instructions = json.getString("instructions");
			if (json.has("category"))
				category = json.getString("category");
			
			// Parse options
			JSONArray optionGroupsJSON = null;
			if (json.has("option_groups"))
				optionGroupsJSON = json.getJSONArray("option_groups");
			if (json.has("options_groups"))
				optionGroupsJSON = json.getJSONArray("options_groups");
			if (optionGroupsJSON != null) {
				
				optionGroups = new ArrayList<OptionGroup>();
				for (int i = 0 ; i < optionGroupsJSON.length() ; i++) {
					
					JSONObject optionGroupJSON = optionGroupsJSON.getJSONObject(i);

					// If we're saving a selection with ITEM_SELECT, save the first option
					if (type.equals(ITEM_SELECT) && i == 0 && savedSelections != null) {
						Log.v(TAG, "Saving selection " + name + " for: " + optionGroupJSON);
						savedSelections.put(name, optionGroupJSON);
					}
					
					if (optionGroupJSON.getString("type").equals(OptionGroup.OPTION_SELECT)
							&& savedSelections != null) {
						
						// If we're dealing with an OPTION_SELECT group, try to uncompress it or ignore it
						JSONArray optionsJSON = optionGroupJSON.getJSONArray("options");
						for (int j = 0 ; j < optionsJSON.length() ; j++) {
							String selectionName = optionsJSON.getString(j);
							OptionGroup option = new OptionGroup(savedSelections.get(selectionName));
							if (option != null) {
								Log.v(TAG, "Loading selection " + selectionName + " for: " + name + ", " + optionGroupJSON);
								optionGroups.add(option);
							} else {
								Log.e(TAG, "Could not load selection: " + selectionName);
							}
						}
					} else {
						// If it's not a compressed group just add it to the option groups
						optionGroups.add(new OptionGroup(optionGroupJSON));
					}
				}
			}
			
			// Calculate the drink price based on selected options, if any.
			calculatePrice();
			
		} else if (type.equals(SECTION_TEXT)) {
			
			// Parse SECTION_TEXT type
			text = json.getString("text");
		} else {
			throw new JSONException("Invalid menu item type");
		}
	}

	
	/**
	 * 
	 * TODO - Views
	 * 
	 */
	
	public View inflateOrder(LayoutInflater inflater) {
		
		View view = inflater.inflate(R.layout.order_item, null);
		
		// Set title and description
		if (hasTitle()) {
			((TextView) view.findViewById(R.id.view_order_item_name)).setText(getTitle());
			if (hasDescription())
				((TextView) view.findViewById(R.id.view_order_item_description)).setText(getDescription());
			else 
				((TextView) view.findViewById(R.id.view_order_item_description)).setVisibility(View.GONE);
		} else {
			view.findViewById(R.id.view_order_item_header).setVisibility(View.GONE);
		}
		
		// Set options views
		LinearLayout options = (LinearLayout) view.findViewById(R.id.view_order_item_options);
		options.setTag(this);

		if (getOptionGroups() != null) {
			for (OptionGroup optionGroup : getOptionGroups()) {
				options.addView(optionGroup.inflateOrder(inflater));
			}
		}
		
		return view;
	}
	
	
	
	/**
	 * 
	 * TODO Getters and setters
	 * 
	 */

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String drinkId) {
		this.itemId = drinkId;
	}

	
	public String getVenueId() {
		return venueId;
	}

	public void setVenueId(String venueId) {
		this.venueId = venueId;
	}

	public boolean hasTitle() {
		return ! (name == null || name.equals(""));
	}
	
	public String getTitle() {
		return name;
	}

	public void setTitle(String title) {
		this.name = title;
	}

	public boolean hasDescription() {
		return ! (description == null || description.equals(""));
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getPrice_special() {
		return specialPrice;
	}

	public void setPrice_special(double price_special) {
		this.specialPrice = price_special;
	}

	public ArrayList<OptionGroup> getOptionGroups() {
		return optionGroups;
	}

	public void setOptionGroups(ArrayList<OptionGroup> optionGroups) {
		this.optionGroups = optionGroups;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getSpecialPrice() {
		return specialPrice;
	}

	public void setSpecialPrice(double specialPrice) {
		this.specialPrice = specialPrice;
	}

	public String getGlass() {
		return glass;
	}

	public void setGlass(String glass) {
		this.glass = glass;
	}

	public String getIngredients() {
		return ingredients;
	}

	public void setIngredients(String ingredients) {
		this.ingredients = ingredients;
	}

	public String getInstructions() {
		return instructions;
	}

	public void setInstructions(String instructions) {
		this.instructions = instructions;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	
	/**
	 * TODO - Utilities
	 */
	
	public void calculatePrice() {

		optionsPrice = 0;
		
		if (optionGroups == null)
			return;
		
		for (OptionGroup options : optionGroups) {
			for (Option option : options.options) {
				if (option.selected) {
					optionsPrice += option.price;
				}
			}
		}
		
		price = basePrice + optionsPrice;
	}
	
}
