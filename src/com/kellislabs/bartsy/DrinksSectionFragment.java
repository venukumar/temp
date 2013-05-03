/**
 * 
 */
package com.kellislabs.bartsy;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

import com.kellislabs.bartsy.adapters.ExpandableListAdapter;
import com.kellislabs.bartsy.db.DatabaseManager;
import com.kellislabs.bartsy.model.MenuDrink;
import com.kellislabs.bartsy.model.Section;
import com.kellislabs.bartsy.utils.WebServices;

/**
 * @author peterkellis
 * 
 */
public class DrinksSectionFragment extends Fragment{
	private View mRootView = null;
	private ExpandableListView mDrinksListView = null;
	LayoutInflater mInflater = null;
	ViewGroup mContainer = null;
	private Handler handler = new Handler();


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mRootView = inflater.inflate(R.layout.drinks_main, container, false);

		if (mDrinksListView == null) {

			mRootView = inflater
					.inflate(R.layout.drinks_main, container, false);
			mDrinksListView = (ExpandableListView) mRootView
					.findViewById(R.id.view_drinks_for_me_list);

			mInflater = inflater;
			mContainer = container;

			List<Section> sections = DatabaseManager.getInstance()
					.getMenuSections();

			if (sections != null && sections.size() > 0) {
				updateListView(sections);
			} else {
				loadMenuSections();
			}
		}

		return mRootView;
	}
	
	/**
	 * To update listview from the db
	 * 
	 * @param sectionsList
	 */
	public void updateListView(List<Section> sectionsList){
		
		ArrayList<String> groupNames = new ArrayList<String>();
		// Default group name for individual drinks
		List<MenuDrink> defaultList = DatabaseManager.getInstance().getMenuDrinks();
		if(defaultList!=null && defaultList.size()>0){
			groupNames.add("Drinks");
		}
		
		for (int i = 0; i < sectionsList.size(); i++) {
			groupNames.add(sectionsList.get(i).getName());
		}
		
		final ArrayList<ArrayList<MenuDrink>> menuDrinks = new ArrayList<ArrayList<MenuDrink>>();
		if(defaultList!=null && defaultList.size()>0){
			ArrayList<MenuDrink> defaultmenu = new ArrayList<MenuDrink>(defaultList);
			menuDrinks.add(defaultmenu);
		}
		for (int j = 0; j < sectionsList.size(); j++) {
			List<MenuDrink> list = DatabaseManager.getInstance()
					.getMenuDrinks(sectionsList.get(j));
			ArrayList<MenuDrink> menu = new ArrayList<MenuDrink>(list);
			menuDrinks.add(menu);
		}

		mDrinksListView.setAdapter(new ExpandableListAdapter(getActivity(),
				groupNames, menuDrinks));
		mDrinksListView.setOnChildClickListener(new OnChildClickListener() {
			
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				MenuDrink menuDrink= menuDrinks.get(groupPosition).get(childPosition);
				
				// Create an instance of the dialog fragment and show it
				DrinkDialogFragment dialog = new DrinkDialogFragment();
				dialog.drink = menuDrink;
				dialog.show(getActivity().getSupportFragmentManager(), "Order drink");
				
				return false;
			}
		});
		
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		mRootView = null;
		mDrinksListView = null;
		mInflater = null;
		mContainer = null;
	}

	/**
	 * To get Sections and Drinks from the server
	 */
	private void loadMenuSections() {

		new Thread(new Runnable() {

			@Override
			public void run() {
				WebServices.getMenuList(getActivity());
				final List<Section> sectionsList = DatabaseManager.getInstance()
						.getMenuSections();
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						updateListView(sectionsList);
					}
				});
				
			}
		}).start();
	}

	

}