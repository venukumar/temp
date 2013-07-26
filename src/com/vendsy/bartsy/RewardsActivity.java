package com.vendsy.bartsy;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.vendsy.bartsy.model.Reward;
import com.vendsy.bartsy.utils.Utilities;
import com.vendsy.bartsy.utils.WebServices;
/**
 * 
 * @author Seenu Malireddy
 *
 */
public class RewardsActivity extends SherlockActivity {
	
	// Progress dialog
	private ProgressDialog progressDialog;
	private BartsyApplication mApp;

//	private ArrayList<Reward> rewardList = new ArrayList<Reward>();
	private LinearLayout rewardsListView;
	private HashMap<String, Bitmap> savedImages = new HashMap<String, Bitmap>();
	
	private Handler handler = new Handler();
	private ResponsiveScrollView responsiveScrollView;
	
	private int index = 0; // Present index of the records to pass in the Sys call
	
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rewards_main);
		
		mApp = (BartsyApplication) getApplication();
		
		// Set up the action bar custom view
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowHomeEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);

		
		LinearLayout rewardLayout = (LinearLayout)findViewById(R.id.rewards);
		
		responsiveScrollView = new ResponsiveScrollView(this);
//		responsiveScrollView.setOnEndScrollListener(new OnEndScrollListener(){
//			@Override
//			public void onEndScroll() {
//			}
//		});
//		
		rewardLayout.addView(responsiveScrollView);
		
		rewardsListView = new LinearLayout(this);
		// Set parameters for linear layout
		LayoutParams params = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		rewardsListView.setLayoutParams(params);
		rewardsListView.setOrientation(LinearLayout.VERTICAL);
				
		// Set dividers
		rewardsListView.setDividerDrawable(getResources().getDrawable(R.drawable.div_light_grey));
		rewardsListView.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE | LinearLayout.SHOW_DIVIDER_BEGINNING | LinearLayout.SHOW_DIVIDER_END);
		
		responsiveScrollView.addView(rewardsListView);
		
		loadNotifications();
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		
		case android.R.id.home:
			// app icon in action bar clicked; go home
//			Intent intent = new Intent(this, MainActivity.class);
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//			startActivity(intent);
			finish();
			return super.onOptionsItemSelected(item);
		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	
	/**
	 * Call getNotifications Sys call and get the notifications data from the server
	 */
	private void loadNotifications(){
				
		responsiveScrollView.setInProgress(true);
		// display progress dialog for first time
		if(index==0){
			progressDialog = Utilities.progressDialog(this, "Loading..");
			progressDialog.show();
		}
		
		// Background thread
		new Thread() {

			public void run() {
				
				final String response = WebServices.getRewards(mApp, mApp.mProfile.getBartsyId());
				// Post handler to access UI 
				handler.post(new Runnable() {
					
					@Override
					public void run() {
						if(progressDialog!=null && progressDialog.isShowing()){
							progressDialog.dismiss();
						}
						
						// getUserRewards web service response handling
						if (response != null){
							processResponse(response);
						}
					}
				});
			};
		}.start();

	}
	/**
	 * Parse getUserRewards Sys call response and display in the UI
	 * 
	 * @param response
	 */
	private void processResponse(String response) {
		ArrayList<Reward> rewardList = new ArrayList<Reward>();
		try {
			JSONObject json = new JSONObject(response);
			// Success response
			if(json.has("venues") && json.has("errorCode") && json.getInt("errorCode") ==0){
				JSONArray jsonArray = json.getJSONArray("venues");
				for (int i=0; i<jsonArray.length() ; i++) {
					JSONObject jsonObj = jsonArray.getJSONObject(i);
					Reward reward = new Reward(jsonObj);
					rewardList.add(reward);
				}
//				responsiveScrollView.setNoMoreItems(jsonArray.length()==0);
				
				// Update list view with new rewards
				updateRewardsView(rewardList);
			}
			// Error response
			else if(json.has("errorMessage")){
				Toast.makeText(getApplicationContext(), json.getString("errorMessage"), Toast.LENGTH_LONG).show();
			}
		} catch (JSONException e) {
		}
	}

	/**
	 * Update rewards in the view.
	 *  
	 */
	public void updateRewardsView(ArrayList<Reward> rewards){
		
		for(Reward reward: rewards){
			addRewardView(reward);
		}
		
		// Add the new rewards to the existing list and its count
//		this.rewardList.addAll(rewards);
//		index += notifications.size();
		
	}

	/**
	 * Add reward points view to the main listview and set the values for the view
	 * 
	 * @param reward
	 */
	public void addRewardView(Reward reward){
		// Inflate reward view
		View view = getLayoutInflater().inflate(R.layout.reward_item, null);
		
		
		// Set all information to the view
		ImageView profileImage = ((ImageView)view.findViewById(R.id.profileImage));
		(((TextView)view.findViewById(R.id.locationText))).setText(reward.getAddress());
		(((TextView)view.findViewById(R.id.pointsText))).setText(reward.getRewards()+" points");
		
		// Set profile image
		((TextView)view.findViewById(R.id.userNameText)).setText(reward.getVenueName());
		profileImage.setTag(WebServices.DOMAIN_NAME+reward.getVenueImage());
		WebServices.downloadImage(WebServices.DOMAIN_NAME+reward.getVenueImage(), profileImage, savedImages);		
		
		// Add view to the list view
		rewardsListView.addView(view);
	}
	
}
