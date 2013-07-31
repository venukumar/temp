package com.vendsy.bartsy;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockActivity;
import com.vendsy.bartsy.adapter.CustomDrinksListViewAdapter;
import com.vendsy.bartsy.dialog.DrinkDialogFragment;
import com.vendsy.bartsy.dialog.PeopleSectionDialog;
import com.vendsy.bartsy.dialog.PeopleSectionFragmentDialog;
import com.vendsy.bartsy.model.Category;
import com.vendsy.bartsy.model.Ingredient;
import com.vendsy.bartsy.model.Item;
import com.vendsy.bartsy.model.Order;
import com.vendsy.bartsy.model.UserProfile;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
/**
 * 
 * @author Seenu Malireddy
 *
 */
public class OrderCustomDrinkActivity extends SherlockActivity {
	
	// Progress dialog
	private ProgressDialog progressDialog;
	private View ordersView;
	private Spinner categoriesSpinner;
	private BartsyApplication mApp;
	private ListView mixersListView;
	private LinearLayout mixerLayout;
	private Category selectedCategory;
	
	private ArrayList<Ingredient> selectedMixers= new ArrayList<Ingredient>();
	private Order order;
	
	private double baseAmount;
	private double tipAmount;
	private double totalAmount;
	private TextView priceText;
	public UserProfile profile;
	
	private View addOnsView;
	private View submitButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_drinks_order_main);
		
		addOnsView = findViewById(R.id.custom_order_add_ons);
		submitButton = findViewById(R.id.orderButton);
		
		mApp = (BartsyApplication)getApplication();
		
		profile = mApp.mProfile;
		
		// To setup order view
		ordersView = (LinearLayout) findViewById(R.id.ordersView);
		
		// Customize dialog for this drink
		((TextView) ordersView.findViewById(R.id.drinkTitle)).setText(mApp.selectedSpirit.getName());
		priceText = ((TextView) ordersView.findViewById(R.id.view_dialog_drink_price));
		priceText.setText("$ "+ mApp.selectedSpirit.getPrice());
		 
		// Set price for base spirit
		((TextView) ordersView.findViewById(R.id.view_custom_order_drink_base_price))
			.setText("$ " + mApp.selectedSpirit.getPrice());
		
		mixerLayout = (LinearLayout) ordersView.findViewById(R.id.mixersLayout);
		
		// Setup mixers list view
		mixersListView = (ListView)findViewById(R.id.mixersListView);
		
		// To setup spinner with mixer categories
		categoriesSpinner = (Spinner)findViewById(R.id.categoriesSpinner);
		// To setup adapter for spinner
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, getMixerCategories());
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		categoriesSpinner.setAdapter(adapter);
		// To setup order button
		final Button orderButton = (Button) ordersView.findViewById(R.id.orderButton);
		orderButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				orderButton.setEnabled(false);
				proceedOrder();
			}
		});
		
		// set up item selected listener
		categoriesSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// Get the selected item position
				int position = categoriesSpinner.getSelectedItemPosition();
				// Update list view
				updateMixerList(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
		
		updateMixerList(0);
		categoriesSpinner.requestFocus();
		updatePrice();
		
		if(profile!=null){
			updateProfileView(profile);
		}
	}
	
	private void updateProfileView(UserProfile profile) {
		ImageView profileImageView = ((ImageView)ordersView.findViewById(R.id.view_user_dialog_image_resource));
		
		if (!profile.hasImage()) {
			WebServices.downloadImage(profile, profileImageView);
		} else {
			profileImageView.setImageBitmap(profile.getImage());
		}
		((TextView) ordersView.findViewById(R.id.view_user_dialog_info))
		.setText(profile.getNickname());	
		
		
		
		// To pick more user profiles when user pressed on the image view
		profileImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				PeopleSectionDialog dialog = new PeopleSectionDialog(OrderCustomDrinkActivity.this){
					@Override
					protected void selectedProfile(UserProfile userProfile) {
						// Update profile with new selected profile
						OrderCustomDrinkActivity.this.profile = userProfile;
						updateProfileView(userProfile);
						super.selectedProfile(userProfile);
					}
				};
				dialog.show();
			}
		});
		
		
	}
	
	/**
	 * To add mixer layout to the drink
	 */
	private void addMixerLayout(final Ingredient ingredient){
		
		
		final View mixerView = getLayoutInflater().inflate(R.layout.custom_drink_modifier, null);
		// Set name values for text view
		TextView titleView = (TextView) mixerView.findViewById(R.id.view_order_title);
		titleView.setText(ingredient.getName());
		
		// Set price values for text view
		TextView priceView = (TextView) mixerView.findViewById(R.id.order_price);
		priceView.setText(String.valueOf(ingredient.getPrice()));
				
		
		// Delete button to remove mixer from the list
		Button deleteButton = (Button) mixerView.findViewById(R.id.view_order_button_remove);
		deleteButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mixerLayout.removeView(mixerView);
				selectedMixers.remove(ingredient);
				updatePrice();
				
				addOnsView.setVisibility(View.VISIBLE);
			}
		});
		
		// Add ingredient to mixers array list
		selectedMixers.add(ingredient);
		mixerLayout.addView(mixerView);
		updatePrice();
		
	}
	/**
	 * Add all mixers price and drink price
	 */
	private void updatePrice(){
		totalAmount = mApp.selectedSpirit.getPrice();
		for(Ingredient ingredient: selectedMixers){
			totalAmount+=ingredient.getPrice();
		}
		// TO update price in order view
		priceText.setText("$"+ totalAmount);
	}
	/**
	 * It will trigger when order button is pressed
	 */
	private void proceedOrder(){

		
		final RadioGroup tipPercentage = (RadioGroup) ordersView.findViewById(R.id.view_dialog_drink_tip);
		int selected = tipPercentage.getCheckedRadioButtonId();
//		final EditText percentage = (EditText) ordersView.findViewById(R.id.view_dialog_drink_tip_amount);
		
		// Gets a reference to our "selected" radio button
		RadioButton b = (RadioButton) tipPercentage.findViewById(selected);
		String tipPercentageValue;
//		if (b.getText().toString().trim().length() == 0) {
//			tipPercentageValue = percentage.getText().toString();
//		} else {
			tipPercentageValue = b.getText().toString();
//		}
		
		tipPercentageValue = tipPercentageValue.replace("%", "");
		double tipAmount = 0;
		
		try {
			tipAmount = Double.valueOf(tipPercentageValue) / 100 * totalAmount;
		} catch (NumberFormatException e) {
		}

		String mixers = "";
		for (Ingredient i : selectedMixers) {
			mixers += i.getName() + ", ";
		}
		
		Order order = new Order(mApp.mProfile, profile,  mApp.mActiveVenue.getTaxRate(), tipAmount, 
				new Item(mApp.selectedSpirit.getName(), mixers, totalAmount));
		
		// invokePaypalPayment(); // To enable paypal payment

		// Web service call - the response in handled asynchronously in processOrderDataHandler()
		if (WebServices.postOrderTOServer(mApp, order, mApp.mActiveVenue.getId(), processOrderDataHandler))
			
		// Failed to place syscall due to internal error
		Toast.makeText(this, "Unable to place order. Please restart application.", Toast.LENGTH_SHORT).show();
	}
	
	// Response codes
	public static final int HANDLE_ORDER_RESPONSE_SUCCESS = 0;
	public static final int HANDLE_ORDER_RESPONSE_FAILURE = 1;
	public static final int HANDLE_ORDER_RESPONSE_FAILURE_WITH_CODE = 2;
	
	// The handler code
	public Handler processOrderDataHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			Log.v("OrderCustomDrinkActivity", "VenueActivity.processOrderDataHandler.handleMessage(" + msg.arg1 + ", " + msg.arg2 + ", " + msg.obj + ")");
			
			switch (msg.what) {
			case HANDLE_ORDER_RESPONSE_SUCCESS:
				// The order was placed successfully 
				
				mApp.syncOrders();
				finish();

				break;
				
			case HANDLE_ORDER_RESPONSE_FAILURE:
				// The syscall was not placed
				Toast.makeText(OrderCustomDrinkActivity.this, "Unable to place order. Check your internet connection", Toast.LENGTH_SHORT).show();
				break;
				
			case HANDLE_ORDER_RESPONSE_FAILURE_WITH_CODE:
				// The syscall got an error code
				Toast.makeText(OrderCustomDrinkActivity.this, "Unable to place order. Venue is not accepting orders (" + msg.obj + ")", Toast.LENGTH_SHORT).show();
				break;
			}
			
		}
	};
	
	
	/**
	 * To update mixer list view based on selected spinner selection
	 */
	private void updateMixerList(int position) {
		if(mApp.mixers.size()>position){
			selectedCategory = mApp.mixers.get(position);
			Log.d("OrderCustomDrinks", "Size : "+mApp.mixers.get(position).getIngredients());
			mixersListView.setAdapter(new CustomDrinksListViewAdapter(this, R.layout.menu_item, selectedCategory.getIngredients()));
		}
		
		mixersListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
									long arg3) {
				// It will invoke when the custom drink list item selected
				
				addMixerLayout(selectedCategory.getIngredients().get(position));
				
				addOnsView.setVisibility(View.GONE);

			}
		});
	}
	/**
	 * Get mixer categories in strings array list from the application class 
	 * 
	 * @return
	 */
	private ArrayList<String> getMixerCategories(){
		
		ArrayList<String> categories = new ArrayList<String>();
		for(Category category: mApp.mixers){
			categories.add(category.getName());
		}
		return categories;
	}
	
	
}
