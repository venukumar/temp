/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.kellislabs.bartsy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kellislabs.bartsy.R;

/**
 * This preference activity has in its manifest declaration an intent filter for
 * the ACTION_MANAGE_NETWORK_USAGE action. This activity provides a settings UI
 * for users to specify network settings to control data usage.
 */
public class MapActivity extends Activity implements LocationListener, OnClickListener {

	private GoogleMap mMap = null;
	private LocationManager locationManager;
	private static final long MIN_TIME = 400;
	private static final float MIN_DISTANCE = 1000;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps_main);
        
        // Set the action bar to enable back navigation
        getActionBar().setDisplayHomeAsUpEnabled(true);

        
        
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.

    		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        
    		// Make sure we got a map, if not exit
    		if (mMap == null) {
                Toast.makeText(this, "Could not connect to Google maps", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }
        
        mMap.setMyLocationEnabled(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); 
        
        
        // Add some markers - in the working version a call is placed to the Bartsy server with the current location of the user and a radius.
        // The server returns Bartsy points within this radius along with number of people in each.
        LatLng AREAL = new LatLng(33.999786, -118.481364);
        mMap.addMarker(new MarkerOptions()
        .position(AREAL)
        .title("Areal")
        .snippet("People checked in: 6"));

        LatLng CHAYA = new LatLng(33.997031, -118.47932);
        mMap.addMarker(new MarkerOptions()
        .position(CHAYA)
        .title("Chaya Venice")
        .snippet("People checked in: 8"));
        
        LatLng BRICK = new LatLng(34.003544, -118.484955);
        mMap.addMarker(new MarkerOptions()
        .position(BRICK)
        .title("Brick & Mortar")
        .snippet("People checked in: 17"));
        
        LatLng CIRCLE = new LatLng(33.998872, -118.480602);
        mMap.addMarker(new MarkerOptions()
        .position(CIRCLE)
        .title("Circle Bar")
        .snippet("People checked in: 14"));
        
        LatLng THREEONETEN = new LatLng(33.999203, -118.48059);
        mMap.addMarker(new MarkerOptions()
        .position(THREEONETEN)
        .title("31Ten")
        .snippet("People checked in: 23"));
       
        LatLng BASESEMENT = new LatLng(33.999203, -118.48059);
        mMap.addMarker(new MarkerOptions()
        .position(BASESEMENT)
        .title("Basement Tavern")
        .snippet("People checked in: 18"));
       
        LatLng MAIN = new LatLng(33.99895, -118.48052);
        mMap.addMarker(new MarkerOptions()
        .position(MAIN)
        .title("Main on Main")
        .snippet("People checked in: 14"));
        
        
        findViewById(R.id.view_venues_list).setOnClickListener(this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	@Override
	public void onLocationChanged(Location location) {
	    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
	    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
	    mMap.animateCamera(cameraUpdate);
	    locationManager.removeUpdates(this);
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View arg0) {
		// Just start the bartsy activity regardless of where the use clicks for now
		this.startActivity(new Intent().setClass(this, BartsyActivity.class));
	}
	
}
