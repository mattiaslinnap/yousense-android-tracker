package com.linnap.locationtracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class SensorService extends Service {

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
		
//		AppGlobals.getLog(this).log("SensorService onCreate()");
		sensorThread = new SensorThread(this);
		sensorThread.start();
		
		// Keep service in foreground to avoid it being killed by the OS.
		// TODO
		//Notification notification = new Notification();
		//notification.icon = R.drawable.icon;
		//PendingIntent openActivity = PendingIntent.getActivity(this, 0, new Intent(this, PClientActivity.class), 0);
		//notification.setLatestEventInfo(this, "PClient", "Location Tracking Active", openActivity);
		//startForeground(NOTIFICATION_ID, notification);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
//		AppGlobals.getLog(this).log("SensorService onStartCommand()");
		return START_STICKY;  // Runs until explicitly stopped.
	}
	
	public void onDestroy() {
		super.onDestroy();
//		AppGlobals.getLog(this).log("SensorService onDestroy()");
		sensorThread.quit();
		
		wakeLock.release();
		wakeLock = null;
		
		stopForeground(true);
	}


}
