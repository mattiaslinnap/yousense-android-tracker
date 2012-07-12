package com.linnap.locationtracker;

import android.util.Log;

import com.linnap.locationtracker.gps.LocationFix;

/**
 * Extend this class, and provide an instance in your extended SensorService.getBindings().
 * @author ml421
 *
 */
public class EventBindings {
	//// App infrastructure bindings
	
	/**
	 * Call startForeground(your notification).
	 * Useful if you don't want the service to be killed while the app is in the background.
	 */
	public void startForegroundWithNotification() {}
	
	/**
	 * Log debug messages somewhere. Default implementation logs to LogCat.
	 */
	public void log(String message) {
		Log.i(SensorConfig.TAG, message);
	}
	
	/**
	 * Log exception messages somewhere. Default implementation logs to LogCat.
	 */
	public void log(String message, Throwable throwable) {
		Log.e(SensorConfig.TAG, message, throwable);
	}
	
	// Events from location tracker
	
	/**
	 * New GPS fix! Default implementation logs the coordinates.
	 */
	public void gpsFix(LocationFix fix) {
		log("Fix: " + fix);
	}
	
	/**
	 * Some sensor start/stop event. Default implementation logs the event tag.
	 */
	public void event(String tag) {
		log("Event: " + tag);
	}
}
