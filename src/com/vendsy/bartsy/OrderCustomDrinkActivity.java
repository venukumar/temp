package com.vendsy.bartsy;

import java.util.ArrayList;

import com.vendsy.bartsy.adapter.CustomDrinksListViewAdapter;
import com.vendsy.bartsy.dialog.DrinkDialogFragment;
import com.vendsy.bartsy.model.Category;
import com.vendsy.bartsy.model.Ingredient;
import com.vendsy.bartsy.model.Order;
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
public class OrderCustomDrinkActivity extends Activity{
	
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
	
	private float totalAmount;
	private TextView priceText;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_drinks_order_main);
		
		mApp = (BartsyApplication)getApplication();
		
		// To setup order view
		LinearLayout ordersLayoutView = (LinearLayout) findViewById(R.id.ordersView);
		ordersView = getLayoutInflater().inflate(R.layout.custom_drink_order, null);
		// Customize dialog for this drink
		((TextView) ordersView.findViewById(R.id.drinkTitle))
						.setText(mApp.selectedSpirit.getName());
		
		priceText = ((TextView) ordersView.findViewById(R.id.view_dialog_drink_price));
		priceText.setText("$"+ mApp.selectedSpirit.getPrice());
		
		mixerLayout = (LinearLayout) ordersView.findViewById(R.id.mixersLayout);
		
		
		
		ordersLayoutView.addView(ordersView);
		
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
		Button orderButton = (Button) ordersView.findViewById(R.id.orderButton);
		orderButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
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
	}
	
	/**
	 * To add mixer layout to the drink
	 */
	private void addMixerLayout(final Ingredient ingredient){
		
		
		final View mixerView = getLayoutInflater().inflate(R.layout.order_mini, null);
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
		order = new Order();
		
		final RadioGroup tipPercentage = (RadioGroup) ordersView.findViewById(R.id.tipPercentage);
		int selected = tipPercentage.getCheckedRadioButtonId();
		final EditText percentage = (EditText) ordersView.findViewById(R.id.editTextPercentage);
		
		// Gets a reference to our "selected" radio button
		RadioButton b = (RadioButton) tipPercentage.findViewById(selected);
		String tipPercentageValue;
		if (b.getText().toString().trim().length() == 0) {
			tipPercentageValue = percentage.getText().toString();
		} else {
			tipPercentageValue = b.getText().toString();
		}
		
		tipPercentageValue = tipPercentageValue.replace("%", "");
		
		try {
			order.tipAmount = Float.valueOf(tipPercentageValue);
		} catch (NumberFormatException e) {
		}

		order.initialize(Long.toString(mApp.mOrderIDs), // arg(0) - Client order  ID
				null, 									// arg(1) - This order still doesn't have a server-assigned ID
				mApp.selectedSpirit.getName(), 						// arg(2) - Title
				"", 				// arg(3) - Description
				String.valueOf(totalAmount), 						// arg(4) - Price
				Integer.toString(R.drawable.drinks), 	// arg(5) - Image resource for the order. for now always use the same picture for the drink drink.getImage(),
				mApp.mProfile); 						// arg(6) - Each order contains the profile of the sender (and later the profile of the person that should pick it up)
		
		// for now, it will not support to send drinks to other people
		order.orderReceiver = mApp.mProfile;
		
		// invokePaypalPayment(); // To enable paypal payment

		// Web service call - the response in handled asynchronously in processOrderDataHandler()
		if (WebServices.postOrderTOServer(this, order, mApp.mActiveVenue.getId(),
							processOrderDataHandler))
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
					
					// Add order to the list and update views
					mApp.addOrder(order);

					// Increment the local order count
					mApp.mOrderIDs++;
					
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
			mixersListView.setAdapter(new CustomDrinksListViewAdapter(this, R.layout.drink_item, selectedCategory.getIngredients()));
		}
		
		mixersListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
									long arg3) {
				// It will invoke when the custom drink list item selected
				
				addMixerLayout(selectedCategory.getIngredients().get(position));
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
