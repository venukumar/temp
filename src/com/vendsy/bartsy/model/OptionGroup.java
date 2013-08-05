package com.vendsy.bartsy.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vendsy.bartsy.R;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OptionGroup {

	// Option group types
	protected String type = null;
	protected static final String OPTION_ADD = "OPTION_ADD"; 
	protected static final String OPTION_CHOOSE = "OPTION_CHOOSE"; 
	protected static final String OPTION_SELECT = "OPTION_SELECT";  // Bartsy defined type specifying a category selection from the ingredients menu 
	
	// Specific to OPTION_ADD and OPTION_CHOOSE types
	protected String text = null;
	protected ArrayList<Option> options = null;

	// Specific to OPTION_SELECT
	protected ArrayList<String> selections = null;
	
	/**
	 * TODO - Constructors / parsers
	 */

	public OptionGroup (JSONObject json) throws JSONException {

		type = json.getString("type");

		if (json.has("text"))
			text = json.getString("text");
		
		if (json.has("options")) {
			JSONArray optionsJSON = json.getJSONArray("options");
			
			if (type.equals(OPTION_ADD) || type.equals(OPTION_CHOOSE)) {
				options = new ArrayList<Option>();
				for (int i = 0 ; i < optionsJSON.length() ; i++) {
					options.add(new Option(optionsJSON.getJSONObject(i),type));
				}
			} else if (type.equals(OPTION_SELECT)) {
				selections = new ArrayList<String>();
				for (int i = 0 ; i < optionsJSON.length() ; i++) {
					selections.add(optionsJSON.getString(i));
				}
			}
		}
	}
	
	
	/**
	 * TODO - Serializers
	 */
	
	public JSONObject toJson() throws JSONException {
		JSONObject json = new JSONObject();
		
		if (has(type))
			json.put("type", type);
		if (has(text))
			json.put("text", text);
		if (has(options)) {
			JSONArray optionsJson = new JSONArray();
			for (Option option : options)
				optionsJson.put(option.toJson());
			json.put("options", optionsJson);
		}
		
		return json;
	}
	
	/**
	 * TODO - Views
	 */
	
	public View customizeView(LayoutInflater inflater) {
		
		View view = inflater.inflate(R.layout.order_option_group, null);
		
		if (text != null)
			((TextView) view.findViewById(R.id.view_order_option_group_name)).setText(text);
		if (OPTION_ADD.equals(type))
			((TextView) view.findViewById(R.id.view_order_option_group_type)).setText("(choose any)");
		if (OPTION_CHOOSE.equals(type))
			((TextView) view.findViewById(R.id.view_order_option_group_type)).setText("(choose one)");
//		if (OPTION_SELECT.equals(type))
//			((TextView) view.findViewById(R.id.view_order_option_group_type)).setText("(choose one)");
			
		LinearLayout optionsView = (LinearLayout) view.findViewById(R.id.view_order_options);
		optionsView.setTag(this);
		for (Option option : options) {
			optionsView.addView(option.customizeView(inflater));
		}
		
		return view;
	}
	
	/**
	 * 
	 * TODO Getters and setters
	 * 
	 */

	public boolean has(String field) {
		return !(field == null || field.equals(""));
	}
	
	public boolean has(Object field) {
		return field != null;
	}
	
}
