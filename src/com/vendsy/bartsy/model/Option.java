package com.vendsy.bartsy.model;

import java.text.DecimalFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vendsy.bartsy.R;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Option {
		
	protected String name;		// name of the option
	protected double price; 	// addition price of the option
	protected String specials;	// specials for this option
	protected boolean selected = false; // is this option selected?
	
	private View mView = null;
	
	/**
	 * TODO - Constructors / parsers
	 */

	Option (JSONObject json, String type) throws JSONException, NumberFormatException {
		if (json.has("name"))
			name = json.getString("name");
		if (json.has("specials"))
			specials = json.getString("specials");
		if (json.has("price"))
			price = Double.parseDouble(json.getString("price"));
		if (json.has("selected"))
			selected = json.getBoolean("selected");
	}

	/**
	 * TODO - Serializers
	 */
	
	public JSONObject toJson() throws JSONException {

		JSONObject json = new JSONObject();
		
		if (has(name))
			json.put("name", name);
		if (has(price))
			json.put("price", price);
		if (has(specials))
			json.put("specials", specials);
		if (has(selected))
			json.put("selected", selected);
		
		return json;
	}
	
	
	/**
	 * TODO - Views
	 */
	
	public View customizeView(LayoutInflater inflater) {

		mView = inflater.inflate(R.layout.order_option, null);
		
	    DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(0);
		df.setMinimumFractionDigits(0);

		mView.setTag(this);
		CheckBox optionName = (CheckBox) mView.findViewById(R.id.view_order_option_name);
		
		if (name != null) {
			String viewName = name;
			if (price != 0)
				viewName = name + " (add $" + df.format(price) + ")";
			optionName.setText(viewName);
		}
		
		if (price == 0)
			mView.findViewById(R.id.view_order_option_price).setVisibility(View.GONE);
		else
			((TextView) mView.findViewById(R.id.view_order_option_base_amount)).setText(df.format(price));

		setChecked(mView, selected);
		optionName.setTag(this);
		
		// The listener here selects/deselects the option depending on the type
		optionName.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
				LinearLayout options = (LinearLayout) mView.getParent();
				OptionGroup optionGroup = (OptionGroup) options.getTag();
				
				if (OptionGroup.OPTION_CHOOSE.equals(optionGroup.type)) {

					// If we have a choose option, clear all options first

					if (!selected) {
						for (int i = 0 ; i < options.getChildCount() ; i++) {
							Option option = (Option) options.getChildAt(i).getTag();
							option.setChecked(option.mView, false);
						}
					}
					setChecked(mView, true);
				} else if (OptionGroup.OPTION_ADD.equals(optionGroup.type)) {

					// Reverse the selection for add groups
					setChecked(mView, !selected);
				}
			}
		});
		
		return mView;
	}
	
	private void setChecked(View view, boolean selected) {
		
		this.selected = selected;
		
		// Set check box tick mark
		((CheckBox) mView.findViewById(R.id.view_order_option_name)).setChecked(selected);
		
		// Show price if selected
		if (selected && price != 0)
			view.findViewById(R.id.view_order_option_price).setVisibility(View.VISIBLE);
		else
			view.findViewById(R.id.view_order_option_price).setVisibility(View.GONE);
	}
	
	/**
	 * 
	 * TODO Getters and setters
	 * 
	 */

	public boolean has(String field) {
		return !(field == null || field.equals(""));
	}
	
	public boolean has(double field) {
		return field != 0;
	}

	public boolean has(boolean field) {
		return field;
	}
	
	public boolean has(Object field) {
		return field != null;
	}

}
