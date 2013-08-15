package com.vendsy.bartsy.adapter;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.vendsy.bartsy.R;
import com.vendsy.bartsy.model.Item;

public class ItemAdapter extends ArrayAdapter<Item> {

		private ArrayList<Item> items;

		public ItemAdapter(Context context, int resource, ArrayList<Item> items) {
		    super(context, resource, items);
		    this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

		    if (convertView == null) 
		        convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_order, null);

		    Item item = items.get(position);
		    item.updateView(convertView);

		    return convertView;
		}
}
