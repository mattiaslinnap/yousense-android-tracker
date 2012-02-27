package com.linnap.locationtracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class SensorService extends Service {

	//// Binding API. Override these methods to customise and link to the location tracker.
	
	/**
	 * Called from SensorService.onCreate() to set up a persistent notification.
	 * This gives the SensorService foreground priority, and reduces the likelihood that it is killed by the OS.
	 * 
	 * Something like this:
	 * Notification notification = new Notification();
	 * notification.icon = R.drawable.icon;
	 * PendingIntent openActivity = PendingIntent.getActivity(this, 0, new Intent(this, YourMainActivity.class), 0);
	 * notification.setLatestEventInfo(this, "YourAppName", "Location Tracking Active", openActivity);
	 * startForeground(1234, notification);
	 */
	public void startForegroundWithNotification() {
	}
	
	/**
	 * Called when a new GPS fix is acquired. Do stuff with it.
	 */
	public synchronized void locationChanged(Location location) {
	}
	
	/**
	 * Called to log debug messages. Override with something else, or leave empty for nothing at all.
	 */
	public void log(String message) {
		Log.d(SensorConfig.TAG, message);
	}
	
	//// Internals
	
	public static final int NOTIFICATION_ID = 1;
	
	SensorThread sensorThread;
	WakeLock wakeLock;
	
	public IBinder onBind(Intent intent) {
		// No binding.
		return null;
	}

	public void onCreate() {
		super.onCreate();
		
		wakeLock = ((PowerManager)getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, SensorConfig.TAG);
		wakeLock.setReferenceCounted(false);
		wakeLock.acquire();
		
		log("SensorService onCreate()");
		sensorThread = new SensorThread(this);
		sensorThread.start();
		
		// Keep service in foreground to avoid it being killed by the OS.
		startForegroundWithNotification();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		log("SensorService onStartCommand()");
		return START_STICKY;  // Runs until explicitly stopped.
	}
	
	public void onDestroy() {
		super.onDestroy();
		log("SensorService onDestroy()");
		sensorThread.quit();
		
		wakeLock.release();
		wakeLock = null;
		
		stopForeground(true);
	}


}
