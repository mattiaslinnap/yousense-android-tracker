package com.linnap.locationtracker.schedule;

import android.content.Context;
import android.location.Location;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.linnap.locationtracker.ExpectedState.TrackerState;
import com.linnap.locationtracker.LocationTrackerService;
import com.linnap.locationtracker.SensorConfig;
import com.linnap.locationtracker.StateChange;
import com.linnap.locationtracker.gps.DistanceCycledGps;
import com.linnap.locationtracker.gps.DistanceCycledGps.GpsMovementListener;
import com.linnap.locationtracker.gps.DistanceCycledGps.GpsState;
import com.linnap.locationtracker.gps.LocationFix;
import com.linnap.locationtracker.movement.SmallMovement;
import com.linnap.locationtracker.movement.SmallMovement.SmallMovementDistanceListener;

public class SensorScheduler implements SmallMovementDistanceListener, GpsMovementListener {

	LocationTrackerService service;
	TrackerState state;
	
	WakeLock wakeLock;
	SmallMovement smallMovement;
	DistanceCycledGps distanceCycledGps;
	boolean gpsTracking;
	
	public SensorScheduler(LocationTrackerService service) {
		this.service = service;
		this.state = TrackerState.STOPPED;
		
		this.wakeLock = ((PowerManager)service.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, SensorConfig.TAG);
		this.wakeLock.setReferenceCounted(false);
		this.smallMovement = new SmallMovement(service, this);
		this.distanceCycledGps = new DistanceCycledGps(service, this);
		this.gpsTracking = false;
	}
	
	public synchronized void switchToState(TrackerState newState) {
		service.event("schedule_state", new StateChange(state, newState));
		state = newState;
		
		if (state != TrackerState.STOPPED) {
			wakeLock.acquire();
			service.startForegroundWithNotification();
		} else {
			wakeLock.release();
			service.stopForeground(true);
		}
		
		if (state == TrackerState.STOPPED) {
			smallMovement.stop();
			distanceCycledGps.switchToState(GpsState.OFF);
			gpsTracking = false;
		} else if (state == TrackerState.BACKGROUND) {
			smallMovement.stop();
			distanceCycledGps.switchToState(GpsState.HIGH);
			gpsTracking = true;
		} else {
			smallMovement.stop();
			distanceCycledGps.switchToState(GpsState.LOCK_HIGH);
			gpsTracking = true;
		}
		
	}

	/// Callbacks from sensors

	public synchronized void maybeSmallMovement() {
		if (state == TrackerState.BACKGROUND) {
			if (!gpsTracking) {
				smallMovement.stop();
				distanceCycledGps.switchToState(GpsState.HIGH);
				gpsTracking = true;
			}
		}
	}

	public synchronized void noGpsMovement() {
		if (state == TrackerState.BACKGROUND) {
			if (gpsTracking) {
				distanceCycledGps.switchToState(GpsState.OFF);
				smallMovement.start();				
				gpsTracking = false;
			}
		}
	}
	
	public synchronized void locationChanged(Location location) {
		service.gpsFix(new LocationFix(location));
	}
	
}
