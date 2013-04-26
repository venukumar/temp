/**
 * 
 */
package com.kellislabs.bartsy;

import java.util.ArrayList;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * @author peterkellis
 *
 */
public class DrinksSectionFragment extends Fragment implements OnClickListener
{
	private View mRootView = null;
	private LinearLayout mDrinksListView = null;
	ArrayList<Drink> mDrinks = new ArrayList<Drink>();
	LayoutInflater mInflater = null;
	ViewGroup mContainer = null;

	ImageButton btnSambuca, btnAbsinth;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
   
		mRootView = inflater.inflate(R.layout.drinks_main, container, false);
		
		
		if (mDrinksListView == null) {
			mRootView = inflater.inflate(R.layout.drinks_main, container, false);
			mDrinksListView = (LinearLayout) mRootView.findViewById(R.id.view_drinks_for_me_list);

			mInflater = inflater;
			mContainer = container; 
			
			
			loadDrinks();
			
			// Add any existing orders in the layout, one by one
			Log.d("Bartsy", "About to add drink list to the View");
			Log.d("Bartsy", "mOrders list size = " + mDrinks.size());

			for (Drink barOrder : mDrinks) {
				Log.d("Bartsy", "Adding an item to the layout");
				barOrder.view = (View) mInflater.inflate(R.layout.drink_item, mContainer, false);
				barOrder.updateView(this); // sets up view specifics and sets listener to this
				mDrinksListView.addView(barOrder.view);
//				((Bartsy)getActivity()).appendStatus("Added new view");
			}
		}
		
		return mRootView;
	}

	@Override 
	public void onDestroyView() {
		super.onDestroyView();
		mRootView = null;
		mDrinksListView = null;
		mInflater = null;
		mContainer = null;
	}

	/*
	 * This should be loaded from a DB. For now they are hardcoded.
	 */
	
	private class drink {

		String title, description, price;
		
		drink(String title, String description, String Price) {
			this.title = title;
			this.description = description;
			this.price = price;
		}
	}
	
	private void loadDrinks() {
		mDrinks.add(new Drink(R.drawable.sambuca, "Sambuca rocks","1 shot Sambuca\n3 coffee beans for good luck (optional)\nIce", "12"));
		mDrinks.add(new Drink(R.drawable.absinthe, "Absinth drip","1 shot Sambuca\n3 coffee beans for good luck (optional)\nIce", "14"));
		mDrinks.add(new Drink(R.drawable.moscowmule, "Moscow Mule","1 shot Sambuca\n3 coffee beans for good luck (optional)\nIce", "10"));
		mDrinks.add(new Drink(R.drawable.martini, "Martini vodka dirty","1 shot Sambuca\n3 coffee beans for good luck (optional)\nIce", "10"));
		mDrinks.add(new Drink(R.drawable.margarita, "Margarita","1 shot Sambuca\n3 coffee beans for good luck (optional)\nIce", "8"));
		mDrinks.add(new Drink(R.drawable.whiskey, "Whiskey neat","1 shot Sambuca\n3 coffee beans for good luck (optional)\nIce", "8"));
	}
	

	@Override
	public void onClick(View v) {
        // Create an instance of the dialog fragment and show it
        DrinkDialogFragment dialog = new DrinkDialogFragment();
        dialog.drink = (Drink)v.getTag();
        dialog.show(getActivity().getSupportFragmentManager(), "Order drink");
	}


}
