package com.vendsy.bartsy.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.vendsy.bartsy.R;
import com.vendsy.bartsy.model.Venue;

public class VenueListViewAdapter extends ArrayAdapter<Venue> {

	private List<Venue> items;

	public VenueListViewAdapter(Context context, int resource,
			List<Venue> items) {

		super(context, resource, items);

		this.items = items;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;

		if (v == null) {

			LayoutInflater vi;
			vi = LayoutInflater.from(getContext());
			v = vi.inflate(R.layout.map_list_item, null);

		}

		Venue p = items.get(position);

		if (p != null) {

			TextView tt = (TextView) v.findViewById(R.id.tittle);
			TextView tt1 = (TextView) v.findViewById(R.id.address);
			// TextView tt3 = (TextView) v.findViewById(R.id.description);

			if (tt != null) {
				tt.setText(p.getName());
			}
			if (tt1 != null) {

				tt1.setText(p.getAddress());
			}

		}

		return v;

	}
}