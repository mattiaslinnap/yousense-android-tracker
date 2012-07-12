package com.linnap.locationtracker.gps;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.linnap.locationtracker.SensorConfig;
import com.linnap.locationtracker.LocationTrackerService;

public class DistanceCycledGps implements LocationListener {

	enum State { HIGH, LOW, OFF };
	
	LocationTrackerService service;
	Looper looper;
	Handler handler;
	GpsMovementListener listener;
	LocationManager locationManager;
	State state;	
	long lastGpsFixMillis;
	GpsHistory history;
	
	public DistanceCycledGps(LocationTrackerService service, Looper looper, Handler handler, GpsMovementListener listener) {
		this.service = service;
		this.looper = looper;
		this.handler = handler;
		this.listener = listener;
		this.locationManager = (LocationManager)service.getSystemService(Context.LOCATION_SERVICE);
		this.state = State.OFF;
		this.lastGpsFixMillis = 0;
		this.history = new GpsHistory(service);
	}
	
	public synchronized void start() {
		switchToState(State.HIGH);
	}
	
	public synchronized void startOrPokeHigh() {
		service.logEvent("gps_poke");
		switchToState(State.HIGH);
	}
	
	public synchronized void stop() {
		switchToState(State.OFF);
	}
	
	public interface GpsMovementListener {
		public void noGpsMovement();
		public void locationChanged(Location location);
	}

	// Internals
	
	private synchronized void switchToState(State newState) {
		switch (newState) {
		case OFF: service.logEvent("gps_off"); break;
		case LOW: service.logEvent("gps_low"); break;
		case HIGH: service.logEvent("gps_high"); break;
		}
		service.log("GPS switching from " + state + " to " + newState);
		state = newState;
		
		history.clear();  // Must clear history, to make sure the new history is based on expected fix frequency.
		advanceGiveupTimer();
		handler.removeCallbacks(giveupTimer);
		locationManager.removeUpdates(this);  // TODO: don't do this unless actually changing delay.
		
		// Set GPS sensor to work at expected rate - or not at all.		
		if (state == State.HIGH || state == State.LOW) {
			handler.postDelayed(giveupTimer, SensorConfig.GPS_GIVEUP_CHECK_DELAY);
			
			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER,
					state == State.HIGH ? SensorConfig.GPS_HIGHSPEED_DELAY_MILLIS : SensorConfig.GPS_LOWSPEED_DELAY_MILLIS,
					0.0f,
					this,
					looper);
		}
	}
	
	// LocationListener Methods	
	
	public synchronized void onLocationChanged(Location location) {
		if (state != State.OFF && location != null) {
			listener.locationChanged(location);
			
			// Delay give-up timers
			advanceGiveupTimer();
			
			// Check for state changes.
			history.addFix(location);
			if (state == State.HIGH) {
				if (history.shouldSwitchToLowSpeed())
					switchToState(State.LOW);
			} else if (state == State.LOW) { 
				if (history.shouldSwitchToHighSpeed())
					switchToState(State.HIGH);
				else if (history.shouldReportStationary()) {
					service.log("GPS is actually stationary.");
					listener.noGpsMovement();
				}
			}
		}
	}

	public synchronized void onProviderDisabled(String provider) {
		service.log("GPS onProviderDisabled()");
		listener.noGpsMovement();
	}

	public synchronized void onProviderEnabled(String provider) {
		service.log("GPS onProviderEnabled()");
	}

	public synchronized void onStatusChanged(String provider, int status, Bundle extras) {
		service.log("GPS onStatusChanged to " + status);
		if (status != LocationProvider.AVAILABLE) {			
			listener.noGpsMovement();
		}
	}
	
	// Give-up Timer
	
	Runnable giveupTimer = new Runnable() {
		public void run() {
			synchronized (DistanceCycledGps.this) {
				if (state != State.OFF) {
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
}
