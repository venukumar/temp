package com.vendsy.bartsy.adapter;
/**
 * @author Seenu malireddy
 */
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.vendsy.bartsy.R;
import com.vendsy.bartsy.model.Ingredient;
import com.vendsy.bartsy.model.Item;
import com.vendsy.bartsy.model.Venue;

public class CustomDrinksListViewAdapter extends ArrayAdapter<Item> {

	private List<Item> items;
	private LayoutInflater inflater;

	public CustomDrinksListViewAdapter(Context context, int resource, List<Item> items) {
		super(context, resource, items);
		this.items = items;
		inflater = LayoutInflater.from( context );
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = null;
		// try to get view from convert view if it is not exist then it is require to inflate the list item view
        if( convertView != null )
            view = convertView;
        else
        	view = inflater.inflate(R.layout.menu_item, parent, false);
		Item ingredient = items.get(position);
		ingredient.updateView(view);
		return view;
	}
	
	
}