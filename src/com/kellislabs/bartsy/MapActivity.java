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
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.kellislabs.bartsy.R;

/**
 * This preference activity has in its manifest declaration an intent filter for
 * the ACTION_MANAGE_NETWORK_USAGE action. This activity provides a settings UI
 * for users to specify network settings to control data usage.
 */
public class MapActivity extends Activity
        implements
            OnSharedPreferenceChangeListener, OnClickListener{

	ImageView bars_map, bar_detail;
	Boolean toggle = true;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.bars_main);

		bars_map = (ImageView) findViewById(R.id.bars_map);
		bars_map.setOnClickListener(this);
		bar_detail = (ImageView) findViewById(R.id.bar_detail);
		bar_detail.setOnClickListener(this);
    }

	/*
	 * On click, switch between the map image and the bar detail image
	 */

	@Override
	public void onClick(View v) {

		if (toggle) {
			bars_map.setVisibility(View.INVISIBLE);
			bar_detail.setVisibility(View.VISIBLE);
		} else {
			bars_map.setVisibility(View.VISIBLE);
			bar_detail.setVisibility(View.INVISIBLE);
		}
		toggle = !toggle;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		
	}
	
}
