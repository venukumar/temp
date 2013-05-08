package com.kellislabs.bartsy;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kellislabs.bartsy.model.VenueItem;
import com.kellislabs.bartsy.utils.WebServices;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class VenueListDialog extends Dialog {
	
	private Handler handler = new Handler();
	private List<VenueItem> venues;
	
	public VenueListDialog(final Context context) {
		super(context);
		
		requestWindowFeature(getWindow().FEATURE_NO_TITLE);
		setContentView(R.layout.display_venues);
		final ListView channelList = (ListView) findViewById(R.id.useJoinChannelList);
		TextView title = (TextView) findViewById(R.id.useJoinChannelTextview);
		title.setText("Select a Venue from the list below");

		final ArrayAdapter<String> channelListAdapter = new ArrayAdapter<String>(
				context, android.R.layout.test_list_item);
		// channelListAdapter.add(object);

		channelList.setAdapter(channelListAdapter);
		new Thread() {
			

			public void run() {
				// TODO Auto-generated method stub
				String response = WebServices.getVenueList(context);
				if (response != null) {
					venues = getVenueListResponse(response);
					handler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							updateVenueListView(venues, channelListAdapter);
						}
					});
				}

			}
		}.start();

		channelList.setOnItemClickListener(new ListView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(venues!=null){
					venueSelected(venues.get(position));
				}
				
				cancel();
			}
		});

		Button cancel = (Button) findViewById(R.id.useJoinCancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				cancel();
			}
		});
		
	}
	
	protected void venueSelected(VenueItem venueItem) {
		
	}

	private void updateVenueListView(List<VenueItem> venues,
			ArrayAdapter<String> channelListAdapter)

	{
		if (venues != null && venues.size() > 0) {

			for (int i = 0; i < venues.size(); i++) {

				VenueItem venue = venues.get(i);
				channelListAdapter.add(venue.getName());

			}

		}
		channelListAdapter.notifyDataSetChanged();
	}

	private List<VenueItem> getVenueListResponse(String response) {
		List<VenueItem> list = new ArrayList<VenueItem>();
		try {
			JSONArray array = new JSONArray(response);
			for (int i = 0; i < array.length(); i++) {
				JSONObject venueObject = array.getJSONObject(i);
				String venueName = venueObject.getString("venueName");
				String venueId = venueObject.getString("venueId");
				String latitude = venueObject.getString("latitude");
				String longitude = venueObject.getString("longitude");

				VenueItem venueProfile = new VenueItem();
				venueProfile.setId(venueId);
				venueProfile.setName(venueName);
				venueProfile.setLatitude(latitude);
				venueProfile.setLongitude(longitude);
				list.add(venueProfile);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}


}
