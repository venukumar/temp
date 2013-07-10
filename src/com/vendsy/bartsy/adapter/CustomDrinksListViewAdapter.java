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

public class CustomDrinksListViewAdapter extends ArrayAdapter<Ingredient> {

	private List<Ingredient> items;
	private LayoutInflater inflater;

	public CustomDrinksListViewAdapter(Context context, int resource,
			List<Ingredient> items) {

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
        	view = inflater.inflate(R.layout.drink_item, parent, false);
			Ingredient ingredient = items.get(position);
		 
			TextView textView = (TextView)view.findViewById( R.id.view_drink_title );
			// Try to set ingredient name
			if( textView != null )
				textView.setText( ingredient.getName() );
			
			// Try to set ingredient price
			TextView rgb = (TextView)view.findViewById( R.id.view_drink_price );
			if( rgb != null )
				rgb.setText( "$"+ingredient.getPrice() );
			
		return view;
	}
	
	
}