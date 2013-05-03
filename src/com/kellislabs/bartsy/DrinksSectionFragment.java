/**
 * 
 */
package com.kellislabs.bartsy;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import com.kellislabs.bartsy.adapters.ExpandableListAdapter;
import com.kellislabs.bartsy.db.DatabaseManager;
import com.kellislabs.bartsy.model.MenuDrink;
import com.kellislabs.bartsy.model.Section;
import com.kellislabs.bartsy.utils.WebServices;

/**
 * @author peterkellis
 * 
 */
public class DrinksSectionFragment extends Fragment implements OnClickListener {
	private View mRootView = null;
	private ExpandableListView mDrinksListView = null;
	ArrayList<Drink> mDrinks = new ArrayList<Drink>();
	LayoutInflater mInflater = null;
	ViewGroup mContainer = null;

	ImageButton btnSambuca, btnAbsinth;

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
				// loadMenuSections();
			} else {
				loadMenuSections();
			}
			List<Section> sectionsList = DatabaseManager.getInstance()
					.getMenuSections();
			ArrayList<String> groupNames = new ArrayList<String>();
			for (int i = 0; i < sectionsList.size(); i++) {
				groupNames.add(sectionsList.get(i).getName());
			}
			ArrayList<ArrayList<MenuDrink>> menuDrinks = new ArrayList<ArrayList<MenuDrink>>();

			for (int j = 0; j < sectionsList.size(); j++) {
				List<MenuDrink> list = DatabaseManager.getInstance()
						.getMenuDrinks(sectionsList.get(j));
				ArrayList<MenuDrink> menu = new ArrayList<MenuDrink>(list);
				menuDrinks.add(menu);

			}

			/*
			 * for (int i = 0; i < sections.size(); i++) { Section section =
			 * sections.get(i); List<MenuDrink> menuDrinks =
			 * DatabaseManager.getInstance() .getMenuDrinks(section);
			 * section.setDrinks(menuDrinks); }
			 */

			mDrinksListView.setAdapter(new ExpandableListAdapter(getActivity(),
					groupNames, menuDrinks));
			//
			// for (Section menuSection : sections) {
			//
			// updateView(menuSection);
			// }

			// for (Drink barOrder : mDrinks) {
			// Log.d("Bartsy", "Adding an item to the layout");
			// barOrder.view = (View) mInflater.inflate(R.layout.drink_item,
			// mContainer, false);
			// barOrder.updateView(this); // sets up view specifics and sets
			// // listener to this
			// mDrinksListView.addView(barOrder.view);
			// // ((Bartsy)getActivity()).appendStatus("Added new view");
			// }
		}

		return mRootView;
	}

	private void updateView(final Section menuSection) {
		// TODO Auto-generated method stub

		// LayoutInflater linflater = (LayoutInflater) getActivity()
		// .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// View view = linflater.inflate(R.layout.menu_section, null);
		//
		// ((ImageView) view.findViewById(R.id.view_drink_image))
		// .setImageResource(R.drawable.happyhour);
		// ((TextView) view.findViewById(R.id.view_drink_title))
		// .setText(menuSection.getName());
		// // ((TextView)
		// // view.findViewById(R.id.view_drink_price)).setText("" +
		// // this.price);
		// view.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		//
		// List<MenuDrink> drinkMenu = DatabaseManager.getInstance()
		// .getMenuDrinks(menuSection);
		// for (MenuDrink menuDrink : drinkMenu) {
		//
		// //updateView1(menuDrink);
		// }
		//
		// }
		//
		// });
		//
		//

		// sets up view specifics and sets
		// listener to this
		// mDrinksListView.setAdapter(new ());

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
			}
		}).start();
	}

	@Override
	public void onClick(View v) {
		// Create an instance of the dialog fragment and show it
		DrinkDialogFragment dialog = new DrinkDialogFragment();
		dialog.drink = (Drink) v.getTag();
		dialog.show(getActivity().getSupportFragmentManager(), "Order drink");
	}

}