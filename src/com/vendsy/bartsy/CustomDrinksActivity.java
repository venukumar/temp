package com.vendsy.bartsy;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.vendsy.bartsy.model.Category;
import com.vendsy.bartsy.model.Ingredient;
import com.vendsy.bartsy.model.Item.OptionGroup;
import com.vendsy.bartsy.utils.Utilities;
import com.vendsy.bartsy.utils.WebServices;
import com.vendsy.bartsy.view.CustomDrinksSectionFragment;
/**
 * 
 * @author Seenu Malireddy
 *
 */
public class CustomDrinksActivity extends SherlockFragmentActivity implements ActionBar.TabListener{

	public static final String TAG = "CustomDrinksActivity";
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	
	public ArrayList<Category> spirits = new ArrayList<Category>();
	
	// Progress dialog
	private ProgressDialog progressDialog;
	private ActionBar actionBar;
	private Handler handler = new Handler();
	
	private BartsyApplication mApp;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate()");

		// Set base view for the activity
		setContentView(R.layout.activity_custom_drinks);
		// Set up the action bar custom view
		
		actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		mApp = (BartsyApplication)getApplication();
		
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
				
				String response = WebServices.getIngredients(app, app.mActiveVenue.getId());
				parseIngredientsResponse(response);
				
				// Thread can't access UI related components directly. We have to post by using handler
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						// To stop progress dialog
						progressDialog.dismiss();
						// To setup tabs for categories
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
			spirits.clear();
			mApp.mixers.clear();
			
			JSONObject json = new JSONObject(response);
			// verify the request is success or failed 
			if(json.has("ingredients") && json.has("errorCode") && json.getString("errorCode").equals("0")){
				JSONArray array = json.getJSONArray("ingredients");
				
				for(int i=0; i<array.length();i++){
					JSONObject jsonObject = array.getJSONObject(i);
					// To verify type Name exist or not
					if(jsonObject.has("typeName")){
						String type = jsonObject.getString("typeName");
						// To parse spirits categories
						if(type.equalsIgnoreCase(Category.SPIRITS_TYPE) && jsonObject.has("categories")){
							JSONArray categoriesArray = jsonObject.getJSONArray("categories");
							if(categoriesArray!=null && categoriesArray.length()>0){
								updateCategoriesAndIngredients(categoriesArray, spirits, false);
							}
						}
						// To parse mixers categories
						else if(type.equalsIgnoreCase(Category.MIXER_TYPE) && jsonObject.has("categories")){
							JSONArray categoriesArray = jsonObject.getJSONArray("categories");
							if(categoriesArray!=null && categoriesArray.length()>0){
								updateCategoriesAndIngredients(categoriesArray, mApp.mixers, true);
							}
						}
					}
				}
			}else{
				Log.e(TAG, "Error in IngredientsResponse: "+response);
			}
			
		} catch (JSONException e) {
			Log.e(TAG, "parseIngredientsResponse():  JSON error :: "+e.getMessage());
		}
	}
	/**
	 * To parse ingredients and categories and add to the given list
	 * 
	 * @param categoriesArray
	 */
	private void updateCategoriesAndIngredients(JSONArray categoriesArray, ArrayList<Category> list, boolean isMixers) {

		for(int i=0;i<categoriesArray.length();i++){
			try {
				JSONObject json = categoriesArray.getJSONObject(i);
				// To get category name and initialize category object and add to the spirits list
				
				String categoryName = json.getString("categoryName");
				// Verify whether option groups are exist in the ingredients categories or not
				if(mApp.selectedMenuItem.getOptionGroups().size()>0){
					OptionGroup group = mApp.selectedMenuItem.getOptionGroups().get(0);
					if(!group.getOptions().contains(categoryName)){
						continue;
					}
				}
				Category category = new Category();
				category.setName(categoryName);
				
				
				JSONArray ingredients = json.getJSONArray("ingredients");
				// To add ingredients to the category
				for(int j=0;j<ingredients.length();j++){
					Ingredient ingredient = new Ingredient(ingredients.getJSONObject(j));
					category.getIngredients().add(ingredient);
				}
				// Add categories which contains at least one ingredient 
				if(category.getIngredients().size()>0){
					list.add(category);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
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
			
			return new CustomDrinksSectionFragment(spirits.get(position));
		}

		@Override
		public int getCount() {
			return spirits.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();

			return spirits.get(position).getName();
		}
	}
	
	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		
	}

}
