/**
 * 
 */
package com.vendsy.bartsy.view;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

import com.vendsy.bartsy.R;
import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.VenueActivity;
import com.vendsy.bartsy.adapter.ExpandableListAdapter;
import com.vendsy.bartsy.db.DatabaseManager;
import com.vendsy.bartsy.dialog.DrinkDialogFragment;
import com.vendsy.bartsy.model.MenuDrink;
import com.vendsy.bartsy.model.Section;
import com.vendsy.bartsy.utils.WebServices;

/**
 * @author peterkellis
 * 
 */
public class DrinksSectionFragment extends Fragment {
	private View mRootView = null;
	private ExpandableListView mDrinksListView = null;
	public BartsyApplication mApp = null;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		Log.i("Bartsy", "DrinksSectionFragment.onCreateView()");
		
		mRootView = inflater.inflate(R.layout.drinks_main, container, false);
		mDrinksListView = (ExpandableListView) mRootView.findViewById(R.id.view_drinks_for_me_list);

		
		// Make sure the fragment pointed to by the activity is accurate
		mApp = (BartsyApplication) getActivity().getApplication();
		((VenueActivity) getActivity()).mDrinksFragment = this;

		// Update the view with the list of items from the DB
		final List<Section> sectionsList = DatabaseManager
				.getInstance().getMenuSections(mApp.activeVenue.getId());
		updateListView(sectionsList);
		
		return mRootView;
	}

	/**
	 * To update listview from the db
	 * 
	 * @param sectionsList
	 */
	public void updateListView(List<Section> sectionsList) {
		
		Log.i("Bartsy", "DrinksSectionFragment.updateListView()");
		
		if(sectionsList==null){
			return;
		}
		ArrayList<String> groupNames = new ArrayList<String>();
		// Default group name for individual drinks
		List<MenuDrink> defaultList = DatabaseManager.getInstance()
				.getMenuDrinks(mApp.activeVenue.getId());
		if (defaultList != null && defaultList.size() > 0) {
			groupNames.add("Various items");
		}

		for (int i = 0; i < sectionsList.size(); i++) {
			groupNames.add(sectionsList.get(i).getName());
		}

		final ArrayList<ArrayList<MenuDrink>> menuDrinks = new ArrayList<ArrayList<MenuDrink>>();
		if (defaultList != null && defaultList.size() > 0) {
			ArrayList<MenuDrink> defaultmenu = new ArrayList<MenuDrink>(
					defaultList);
			menuDrinks.add(defaultmenu);
		}
		for (int j = 0; j < sectionsList.size(); j++) {
			List<MenuDrink> list = DatabaseManager.getInstance().getMenuDrinks(
					sectionsList.get(j), mApp.activeVenue.getId());
			ArrayList<MenuDrink> menu = new ArrayList<MenuDrink>(list);
			menuDrinks.add(menu);
		}
		
		try {
			final BartsyApplication app = (BartsyApplication) getActivity().getApplication();
			
			mDrinksListView.setAdapter(new ExpandableListAdapter(getActivity(),
					groupNames, menuDrinks));
			mDrinksListView.setOnChildClickListener(new OnChildClickListener() {

				@Override
				public boolean onChildClick(ExpandableListView parent, View v,
						int groupPosition, int childPosition, long id) {
					
					if(app.activeVenue == null){
						// for now don't post an error message, but this should be fixed ASAP
						return false;
					}
					
					MenuDrink menuDrink = menuDrinks.get(groupPosition).get(
							childPosition);

					// Create an instance of the dialog fragment and show it
					DrinkDialogFragment dialog = new DrinkDialogFragment();
					dialog.drink = menuDrink;
					dialog.show(getActivity().getSupportFragmentManager(),
							"Order drink");

					return false;
				}
			});
		} catch (Exception e) {
			
			e.printStackTrace();
			return;
		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		Log.i("Bartsy", "DrinksSectionFragment.onDestroy()");
		
		// Because the fragment may be destroyed while the activity persists, remove pointer from activity
		((VenueActivity) getActivity()).mDrinksFragment = null;
	}

}