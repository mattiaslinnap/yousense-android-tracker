package com.linnap.locationtracker;

import com.linnap.locationtracker.gps.LocationFix;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Use this in an activity to control the tracker.
 */
public class TrackerControlConnection implements ServiceConnection {

	LocationTrackerService service;
	boolean pokeHighOnConnected;
	
	public TrackerControlConnection(boolean pokeHighOnConnected) {
		this.service = null;
		this.pokeHighOnConnected = pokeHighOnConnected;
	}
	
	
	@Override
	public void onServiceConnected(ComponentName className, IBinder binder) {
		service = ((TrackerControlBinder)binder).service;
		if (pokeHighOnConnected)
			pokeGpsHigh();
	}

	@Override
	public void onServiceDisconnected(ComponentName className) {
		service = null;
	}
	
	/**
	 * Insert a mock location somewhere into the GPS stream. May confuse the service! Does not currently affect the normal GPS behaviour.
	 */
	public void mockLocation(LocationFix fix) {
		if (service != null)
			service.gpsFix(fix);
	}
	
	/** 
	 * Put GPS into maximum resolution mode, no matter how the service should behave otherwise.
	 * It will time out eventually, and switch back to low or off mode.
	 */
	public void pokeGpsHigh() {
		if (service != null)
			service.pokeGpsHigh();
	}
	
}
