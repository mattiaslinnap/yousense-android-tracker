package com.linnap.locationtracker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.linnap.locationtracker.gps.LocationFix;
import com.linnap.locationtracker.schedule.SensorScheduler;

public class LocationTrackerService extends Service {

	//// Send these actions with startService()
	public static final String ACTION_START_BACKGROUND = "com.linnap.locationtracker.intent.ACTION_START_BACKGROUND";
	public static final String ACTION_STOP_BACKGROUND = "com.linnap.locationtracker.intent.ACTION_STOP_BACKGROUND";
	public static final String ACTION_LOCK_GPS = "com.linnap.locationtracker.intent.ACTION_LOCK_GPS";
	public static final String ACTION_UNLOCK_GPS = "com.linnap.locationtracker.intent.ACTION_UNLOCK_GPS";
	public static final String ACTION_MOCK_FIX = "com.linnap.locationtracker.intent.ACTION_MOCK_FIX";
	
	//// Binding API. Override this with custom EventBindings to link to the location tracker.
	public EventBindings getEventBindings() {
		return new EventBindings();
	};
	
	//// Static API. Get static info on current state.
	public static final LocationFix getLastGoodFix() {
		return lastGoodFix;
	}
	
	//// Internals
	
	private static LocationFix lastGoodFix = null; 
	EventBindings eventBindings;
	ExpectedState expectedState;
	SensorScheduler sensorScheduler;
	
	@Override
	public void onCreate() {
		super.onCreate();
		lastGoodFix = null;
		expectedState = new ExpectedState();
		eventBindings = getEventBindings();
		sensorScheduler = new SensorScheduler(this);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		expectedState.intentReceived(intent.getAction());
		sensorScheduler.switchToState(expectedState.getExpectedState());
		
		if (ACTION_MOCK_FIX.equals(intent.getAction())) {
			mockGpsFix(new LocationFix(intent.getExtras()));
		}
		
		return START_STICKY;
	}	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;  // No binding, it breaks startup logic.
	}
	
	//// Internal callbacks for locationtracker modules.
	
	public void gpsFix(LocationFix fix) {
		lastGoodFix = fix;
		eventBindings.gpsFix(fix);
	}
	
	public void mockGpsFix(LocationFix fix) {
		fix.provider = "mock";
		event("gps_mock");
		gpsFix(fix);
	}
	
	public void event(String tag) {
		eventBindings.event(tag);
	}
	
	public void log(String message) {
		eventBindings.log(message);
	}
	
	public void log(String message, Throwable throwable) {
		eventBindings.log(message, throwable);
	}
	
	public void startForegroundWithNotification() {
		eventBindings.startForegroundWithNotification();
	}
}
