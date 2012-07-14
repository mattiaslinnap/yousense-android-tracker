package com.linnap.locationtracker;

import android.os.Binder;

/**
 * Used by TrackerControlConnection to get an instance of the running service.
 */
public class TrackerControlBinder extends Binder {
	LocationTrackerServiceX service;
	
	TrackerControlBinder(LocationTrackerServiceX service) {
		this.service = service;
	}
}
