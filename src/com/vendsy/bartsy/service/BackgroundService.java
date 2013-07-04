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
			
			// Oh my, an infinite loop
			
			while (isRunning) {

				try {

					// The main synchronization function that runs periodically
					mApp.syncOrders();
					
					// The less interesting hearteat syscall
					if (WebServices.isNetworkAvailable(BackgroundService.this)) {	
						mApp.performHeartbeat();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				try {
					// Sleep then wake up and continue the infinite loop
					Thread.sleep(Constants.monitorFrequency);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
