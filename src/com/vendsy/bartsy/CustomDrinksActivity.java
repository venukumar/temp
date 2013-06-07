package com.vendsy.bartsy;

import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.vendsy.bartsy.utils.Utilities;
import com.vendsy.bartsy.utils.WebServices;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.JsonReader;
import android.util.Log;
/**
 * 
 * @author Seenu Malireddy
 *
 */
public class CustomDrinksActivity extends FragmentActivity implements ActionBar.TabListener{

	public static final String TAG = "CustomDrinksActivity";
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	
	// Progress dialog
	private ProgressDialog progressDialog;
	private ActionBar actionBar;
	private Handler handler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.v(TAG, "onCreate()");

		// Set base view for the activity
		setContentView(R.layout.activity_custom_drinks);
		// Set up the action bar custom view

		actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		// To display progress dialog
		progressDialog = Utilities.progressDialog(this, "Loading..");
		progressDialog.show();
		
		final BartsyApplication app = (BartsyApplication)getApplication();
		// Error Handling
		if(app.mActiveVenue==null){
			return;
		}
		// To get ingredients from the server in background
		new Thread(){
			public void run() {
				
				String response = WebServices.getIngredients(CustomDrinksActivity.this, app.mActiveVenue.getId());
				parseIngredientsResponse(response);
				
				// Thread can't access UI related components directly. We have to post by using handler
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						
						setupTabs();
					}
				});
			}
		}.start();
	}
	/**
	 * To parse ingredients from the response
	 * 
	 * @param response
	 */
	protected void parseIngredientsResponse(String response) {
		try {
			JSONObject json = new JSONObject(response);
			
			if(json.has("ingredients") && json.has("errorCode") && json.getString("errorCode").equals("0")){
				JSONArray array = json.getJSONArray("ingredients");
				for(int i=0; i<array.length();i++){
					JSONObject jsonObject = array.getJSONObject(i);
					
				}
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * To setup tabs in the action bar
	 */
	private void setupTabs() {
				// Create the adapter that will return a fragment for each of the
				// primary sections of the app.
				mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

				// Set up the ViewPager with the sections adapter.
				mViewPager = (ViewPager) findViewById(R.id.pager);
				mViewPager.setAdapter(mSectionsPagerAdapter);

				// When swiping between different sections, select the corresponding
				// tab. We can also use ActionBar.Tab#select() to do this if we have
				// a reference to the Tab.
				mViewPager
						.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
							@Override
							public void onPageSelected(int position) {
								actionBar.setSelectedNavigationItem(position);
							}
						});

				// For each of the sections in the app, add a tab to the action bar.
				for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
					// Create a tab with text corresponding to the page title defined by
					// the adapter. Also specify this Activity object, which implements
					// the TabListener interface, as the callback (listener) for when
					// this tab is selected.
					actionBar.addTab(actionBar.newTab()
							.setText(mSectionsPagerAdapter.getPageTitle(i))
							.setTabListener(this));
				}
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			
			return null;
		}

		@Override
		public int getCount() {
			return 0;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();

			return "";
		}
	}
	
	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());

		// upon selecting the people tab we want to update the list of people
		// from the server
//		if (mTabs[tab.getPosition()] == R.string.title_people) {
//			mPeopleFragment.updatePeopleView();
//		}
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		
	}

}
