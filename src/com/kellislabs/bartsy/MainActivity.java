package com.kellislabs.bartsy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.PlusClient.OnAccessRevokedListener;
import com.google.android.gms.plus.PlusClient.OnPersonLoadedListener;
import com.google.android.gms.plus.model.people.Person;
import com.kellislabs.bartsy.ProfileDialogFragment.ProfileDialogListener;
import com.kellislabs.bartsy.R;
import com.kellislabs.bartsy.PeopleDialogFragment.UserDialogListener;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements  OnClickListener {
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        
		if (((BartsyApplication)getApplication()).activeVenue == null) {
			// No active venue - hide active menue UI
			findViewById(R.id.button_active_venue).setVisibility(View.GONE);
		} else {
			// Active venue exists - set up the active venue view
			// For now just show it
			findViewById(R.id.button_active_venue).setVisibility(View.VISIBLE);
		}
			

        
        // Set up button listeners
		((Button) findViewById(R.id.button_checkin)).setOnClickListener(this);
		((Button) findViewById(R.id.button_settings)).setOnClickListener(this);
		((View) findViewById(R.id.button_active_venue)).setOnClickListener(this);
        
        // Hide action bar
        getActionBar().hide();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    
	@Override
	public void onClick(View v) {
		Log.d("Bartsy", "Clicked on a button");
		
		Intent intent;
		
		
		switch (v.getId()) {
		case R.id.button_active_venue:
			intent = new Intent().setClass(this, VenueActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			break;
		case R.id.button_checkin:
			intent = new Intent().setClass(this, MapActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			break;
		case R.id.button_settings:
			intent = new Intent().setClass(this, SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			break;
		}
	}
}
