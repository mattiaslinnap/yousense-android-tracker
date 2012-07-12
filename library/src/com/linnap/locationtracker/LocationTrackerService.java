package com.linnap.locationtracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.linnap.locationtracker.gps.LocationFix;
import com.linnap.locationtracker.schedule.SensorThread;

public class LocationTrackerService extends Service {

	//// Binding API. Override this with custom EventBindings to link to the location tracker.
	public EventBindings getEventBindings() {
		return new EventBindings();
	};
	
	//// Last known state API
		
	public static LocationFix getLastGoodLocation() {
		return lastGoodLocation;
	}
	
	public static boolean isRunning() {
		return running;
	}
	
	//// Control api is accessible via TrackerControlBinder.
	
	
		
	//// Internals
	
	private volatile static boolean running = false;
	private volatile static LocationFix lastGoodLocation = null;
	
	EventBindings eventBindings;	
	WakeLock wakeLock;
	SensorThread sensorThread;
		
	@Override
	public void onCreate() {
		super.onCreate();
		running = true;
		lastGoodLocation = null;
		
		eventBindings = getEventBindings();
		
		wakeLock = ((PowerManager)getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, SensorConfig.TAG);
		wakeLock.setReferenceCounted(false);
		wakeLock.acquire();
		
		logEvent("start");
		sensorThread = new SensorThread(this);
		sensorThread.start();
		
		// Keep service in foreground to avoid it being killed by the OS.
		eventBindings.startForegroundWithNotification();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		logEvent("stop");
		sensorThread.quit();
		
		wakeLock.release();
		wakeLock = null;
		
		stopForeground(true);
		
		running = false;
		lastGoodLocation = null;
	}
	
	public void pokeGpsHigh() {
		sensorThread.pokeGpsHigh();
	}
	
	public void gpsFix(LocationFix fix) {
		lastGoodLocation = fix;
		eventBindings.gpsFix(fix);
	}
	
	public void mockGpsFix(LocationFix fix) {
		logEvent("gps_mock");
		gpsFix(fix);
	}
	
	public void logEvent(String tag) {
		eventBindings.event(tag);
	}
	
	public void log(String message) {
		eventBindings.log(message);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		log("Binding to LocationTracker");
		return new TrackerControlBinder(this);
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		log("All clients unbound from LocationTracker");
		return false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_REDELIVER_INTENT;  // If killed, maybe restart the service again if possible.
	}
}
