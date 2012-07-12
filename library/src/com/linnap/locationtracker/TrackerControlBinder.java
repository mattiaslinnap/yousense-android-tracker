package com.linnap.locationtracker;

import android.os.Binder;

/**
 * Used by TrackerControlConnection to get an instance of the running service.
 */
public class TrackerControlBinder extends Binder {
	LocationTrackerService service;
	
	TrackerControlBinder(LocationTrackerService service) {
		this.service = service;
	}
}
