
package com.vendsy.bartsy.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.CustomDrinksActivity;
import com.vendsy.bartsy.OrderCustomDrinkActivity;
import com.vendsy.bartsy.R;
import com.vendsy.bartsy.adapter.CustomDrinksListViewAdapter;
import com.vendsy.bartsy.model.Category;

/**
 * @author Seenu Malireddy
 * 
 */
public class CustomDrinksSectionFragment extends Fragment {
	private View mRootView = null;
	
	private Handler handler = new Handler();
	
	private String TAG = "CustomDrinksSectionFragment";

	private Category category;

	private BartsyApplication mApp;
	
	public CustomDrinksSectionFragment(Category category) {
		this.category = category;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.v(TAG, "onCreateView()");
		
		mApp = (BartsyApplication)getActivity().getApplication();
		
		mRootView = inflater.inflate(R.layout.custom_drinks_main, container, false);
		
		// To obtain list view from the SupportMapFragment.
		final ListView venueList = (ListView) mRootView.findViewById(R.id.drinksListView);
		venueList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
									long arg3) {
				// It will invoke when the custom drink list item selected
				
				// Error handling
				if(category==null || category.getIngredients().size()<=position){
					return;
				}
				// To use selected drink in next activity
				mApp.selectedSpirit = category.getIngredients().get(position);
				// Proceed to order screen
				startActivity(new Intent().setClass(getActivity(), OrderCustomDrinkActivity.class));
					
			}
		});
		// Try to set the adapter for list view
		if(category!=null){
			venueList.setAdapter(new CustomDrinksListViewAdapter(getActivity(), R.layout.map_list_item, category.getIngredients()));
		}
				
		return mRootView;
	}

	
	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.v(TAG, "onDestroy()");
		
	}

}