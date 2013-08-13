/**
 * 
 */
package com.vendsy.bartsy.dialog;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.vendsy.bartsy.R;
import com.vendsy.bartsy.adapter.VenueNetworksAdapter;
import com.vendsy.bartsy.model.Venue;
import com.vendsy.bartsy.utils.WifiConfigManager;

/**
 * @author Seenu Malireddy
 * 
 */
public class WifiNetworksDialog extends Dialog{

	static final String TAG = "WifiNetworksDialog";

	private ListView networksListView;

	private WifiManager wifiManager;

	public WifiNetworksDialog(Context context, final ArrayList<Venue> networks) {
		super(context);
		
		setTitle("Select any wifi Network");
		setCancelable(true);
		
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		// Set the layout for the dialog
		setContentView(R.layout.wifi_networks_list);
		
		// Try to get the list view 
		networksListView = (ListView) findViewById(R.id.networksView);
		// Set adapter for the listview to display list of networks
		VenueNetworksAdapter adapter = new VenueNetworksAdapter(context,networks);
		networksListView.setAdapter(adapter);
		
		// Set listener for the list items
		networksListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				 enableWifiNetwork(networks.get(arg2));
				
			}
			
		});
	}
	/**
	 * Enable wifi network which information is available in the venue
	 * 
	 * @param venue
	 */
	private void enableWifiNetwork(Venue venue) {
		
		if(venue != null){
			String networkType = venue.getWifiNetworkType();
			if(venue.getWifiPassword()==null || venue.getWifiPassword().equals("")){
				networkType = "nopass";
			}
			WifiConfigManager.configure(wifiManager, venue.getWifiName(), venue.getWifiPassword(), networkType);
		}
	}
	
}
