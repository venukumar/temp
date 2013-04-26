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
public class NotificationsActivity extends Activity
        implements
            OnSharedPreferenceChangeListener, OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.notifications_main);

    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		
	}

	@Override
	public void onClick(View v) {
		// These toggle buttons implement effectively a tab with the additional ability to hide all content
/*		
        mRootView.findViewById(R.id.button_singles).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                    	ToggleButton b = (ToggleButton) arg0;
                    	if (b.isChecked()) {
                    		mRootView.findViewById(R.id.singles).setVisibility(View.VISIBLE);
                    		mRootView.findViewById(R.id.friends).setVisibility(View.GONE);
                    		((ToggleButton) mRootView.findViewById(R.id.button_friends)).setChecked(false);

                    	} else {
                    		mRootView.findViewById(R.id.singles).setVisibility(View.GONE);                    		
                    	}
                    }});
		
        mRootView.findViewById(R.id.button_friends).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                    	ToggleButton b = (ToggleButton) arg0;
                    	if (b.isChecked()) {
                    		mRootView.findViewById(R.id.friends).setVisibility(View.VISIBLE);
                    		mRootView.findViewById(R.id.singles).setVisibility(View.GONE);  
                    		((ToggleButton) mRootView.findViewById(R.id.button_singles)).setChecked(false);
                    	} else {
                    		mRootView.findViewById(R.id.friends).setVisibility(View.GONE);                    		
                    	}
                    }});
		
*/

	}
	
}
