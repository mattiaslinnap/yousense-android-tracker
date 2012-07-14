package com.linnap.locationtracker;


public class ExpectedState {

	public enum TrackerState { STOPPED, BACKGROUND, GPS_LOCKED };
	
	boolean background;
	int gpsLockCount;
	
	public ExpectedState() {
		this.background = false;
		this.gpsLockCount = 0;
	}
	
	public synchronized void intentReceived(String action) {
		if (LocationTrackerService.ACTION_START_BACKGROUND.equals(action))
			background = true;
		else if (LocationTrackerService.ACTION_STOP_BACKGROUND.equals(action))
			background = false;
		else if (LocationTrackerService.ACTION_LOCK_GPS.equals(action))
			++gpsLockCount;
		else if (LocationTrackerService.ACTION_UNLOCK_GPS.equals(action))
			--gpsLockCount;
	}	
	
	public synchronized TrackerState getExpectedState() {
		if (gpsLockCount > 0)
			return TrackerState.GPS_LOCKED;
		else if (background)
			return TrackerState.BACKGROUND;
		else
			return TrackerState.STOPPED;
	}
	
}
