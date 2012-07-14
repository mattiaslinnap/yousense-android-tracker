package com.linnap.locationtracker;


public class ExpectedState {

	public enum State { STOPPED, BACKGROUND, FULL_GPS };
	
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
		
		if (gpsLockCount < 0)
			gpsLockCount = 0;
	}	
	
	public synchronized State getExpectedState() {
		if (gpsLockCount > 0)
			return State.FULL_GPS;
		else if (background)
			return State.BACKGROUND;
		else
			return State.STOPPED;
	}
	
}
