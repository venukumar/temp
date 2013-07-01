package com.vendsy.bartsy.service;

import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.vendsy.bartsy.BartsyApplication;
import com.vendsy.bartsy.model.Order;
import com.vendsy.bartsy.utils.Constants;
import com.vendsy.bartsy.utils.WebServices;
import com.vendsy.bartsy.view.AppObserver;

public class BackgroundService extends Service {

	private static final String TAG = "BackgroundService";
	
	// check for thread is running or not
	private boolean isRunning = false;
	private Thread thread;
	private Handler handler = new Handler();

	private BartsyApplication mApp;

	@Override
	public void onCreate() {
		super.onCreate();
		
		mApp = (BartsyApplication)getApplication();

		Log.i(TAG, "Background Service started...");
		isRunning = true;

		// Initiate NetworkThread
		thread = new Thread(new NetworkThread());
		thread.start();
	}

	/**
	 * Our onDestroy() is called by the Android application framework when it
	 * decides that our Service is no longer needed. Here we just kill the
	 * background thread
	 */

	@Override
	public void onDestroy() {
		super.onDestroy();
		isRunning = false;
		Toast.makeText(this, "Service destroyed...", Toast.LENGTH_LONG).show();
	}

	/**
	 * We are not use the binder service so we return null.
	 * 
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private class NetworkThread implements Runnable {
		public void finalize() {

			if (isRunning) {
				// create the NetworkThread object in thread
				thread = new Thread(new NetworkThread());
				// start thread
				thread.start();
			}
		}

		public void run() {
			while (isRunning) {

				
				try {
					
					// Print log 
					Log.d(TAG, ">>> Active profile: " + mApp.mProfile);
					Log.d(TAG, ">>> Active venue: " + mApp.mActiveVenue);
					String orders = "";
					for (Order order : mApp.mOrders) {
						orders += order + ", ";
					}
					Log.d(TAG, ">>> Open orders:  " + orders);
					
					
					// refresh the UI to update the timers in the order
					mApp.updateOrderTimers();
											
					// Send heartbeat for as long as we're checked in
					if (WebServices .isNetworkAvailable(BackgroundService.this) && mApp.mActiveVenue != null ) {			
						WebServices.postHeartbeatResponse(mApp.getApplicationContext(), mApp.loadBartsyId(), mApp.mActiveVenue.getId());
					}
				} catch (Exception e) {
					Log.w(TAG, " ******************************** Exception ***********************************\n"
									+ e.getMessage());
					e.printStackTrace();
				}
				
				
				
				try {
					// Thread in sleep
					Thread.sleep(Constants.monitorFrequency);
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
			}
		}
	}

}
