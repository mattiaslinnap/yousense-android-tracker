package com.linnap.locationtracker.gps;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;

import com.linnap.locationtracker.LocationTrackerService;
import com.linnap.locationtracker.SensorConfig;

public class DistanceCycledGps implements LocationListener {

	public enum GpsState { OFF, LOW, HIGH, LOCK_HIGH };
	
	LocationTrackerService service;
	Handler handler;
	GpsMovementListener listener;
	LocationManager locationManager;
	GpsState state;	
	long lastGpsFixMillis;
	GpsHistory history;
	
	public DistanceCycledGps(LocationTrackerService service, GpsMovementListener listener) {
		this.service = service;
		this.handler = new Handler();
		this.listener = listener;
		this.locationManager = (LocationManager)service.getSystemService(Context.LOCATION_SERVICE);
		this.state = GpsState.OFF;
		this.lastGpsFixMillis = 0;
		this.history = new GpsHistory(service);
	}
	
	public interface GpsMovementListener {
		public void noGpsMovement();
		public void locationChanged(Location location);
	}
	
	public synchronized void switchToState(GpsState newState) {
		switch (newState) {
			case OFF: service.event("gps_off"); break;
			case LOW: service.event("gps_low"); break;
			case HIGH: service.event("gps_high"); break;
			case LOCK_HIGH: service.event("gps_lock_high"); break;
		}
		service.log("GPS switching from " + state + " to " + newState);
		state = newState;
		
		history.clear();  // Must clear history, to make sure the new history is based on expected fix frequency.
		advanceGiveupTimer();
		handler.removeCallbacks(giveupTimer);
		locationManager.removeUpdates(this);  // TODO: don't do this unless actually changing delay.
		
		// OFF and LOCK_HIGH do not have giveup timers.		
		if (stateHasGiveup(state)) {
			handler.postDelayed(giveupTimer, SensorConfig.GPS_GIVEUP_CHECK_DELAY);
		}
		
		if (state != GpsState.OFF) {
			long interval = (state == GpsState.LOW ? SensorConfig.GPS_LOWSPEED_DELAY_MILLIS : SensorConfig.GPS_HIGHSPEED_DELAY_MILLIS);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, interval, 0.0f, this);
		}
	}
	
	// LocationListener Methods	
	
	public synchronized void onLocationChanged(Location location) {
		if (state != GpsState.OFF && location != null) {
			listener.locationChanged(location);
			
			// Delay give-up timers
			if (stateHasGiveup(state))
				advanceGiveupTimer();
			
			// Check for state changes.
			if (stateHasHistory(state)) {
				history.addFix(location);
				if (state == GpsState.HIGH) {
					if (history.shouldSwitchToLowSpeed())
						switchToState(GpsState.LOW);
				} else if (state == GpsState.LOW) { 
					if (history.shouldSwitchToHighSpeed())
						switchToState(GpsState.HIGH);
					else if (history.shouldReportStationary()) {
						service.log("GPS is actually stationary.");
						listener.noGpsMovement();
					}
				}
			}
		}
	}

	public synchronized void onProviderDisabled(String provider) {
		service.log("GPS onProviderDisabled()");
		if (stateHasGiveup(state))
			listener.noGpsMovement();
	}

	public synchronized void onProviderEnabled(String provider) {
		service.log("GPS onProviderEnabled()");
	}

	public synchronized void onStatusChanged(String provider, int status, Bundle extras) {
		service.log("GPS onStatusChanged to " + status);
		if (stateHasGiveup(state)) {
			if (status != LocationProvider.AVAILABLE) {			
				listener.noGpsMovement();
			}
		}
	}
	
	// Give-up Timer
	
	Runnable giveupTimer = new Runnable() {
		public void run() {
			synchronized (DistanceCycledGps.this) {
				if (stateHasGiveup(state)) {
					if (shouldGiveup()) {
						service.log("GPS giveup timeout reached");
						listener.noGpsMovement();
					}
					
					handler.postDelayed(this, SensorConfig.GPS_GIVEUP_CHECK_DELAY);
				}
			}
		}
	};
	
	private void advanceGiveupTimer() { 
		lastGpsFixMillis = SystemClock.elapsedRealtime();
	}
	
	private boolean shouldGiveup() {
		return SystemClock.elapsedRealtime() - lastGpsFixMillis > SensorConfig.GPS_GIVEUP_AFTER_NO_FIX_FOR_MILLIS;
	}
	
	private static final boolean stateHasGiveup(GpsState state) {
		return state == GpsState.LOW || state == GpsState.HIGH;
	}
	
	private static final boolean stateHasHistory(GpsState state) {
		return state == GpsState.LOW || state == GpsState.HIGH;
	}
}
