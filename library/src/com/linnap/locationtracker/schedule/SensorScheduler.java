package com.linnap.locationtracker.schedule;

import android.location.Location;

import com.linnap.locationtracker.ExpectedState.State;
import com.linnap.locationtracker.LocationTrackerService;
import com.linnap.locationtracker.gps.DistanceCycledGps;
import com.linnap.locationtracker.gps.DistanceCycledGps.GpsMovementListener;
import com.linnap.locationtracker.gps.LocationFix;
import com.linnap.locationtracker.movement.SmallMovement;
import com.linnap.locationtracker.movement.SmallMovement.SmallMovementDistanceListener;

public class SensorScheduler implements SmallMovementDistanceListener, GpsMovementListener {

	LocationTrackerService service;
	State state;
	
	SmallMovement smallMovement;
	DistanceCycledGps distanceCycledGps;
	boolean running;
	boolean gpsTracking;
	
	
	public SensorScheduler(LocationTrackerService service) {
		this.service = service;
		this.state = State.STOPPED;
		
		this.smallMovement = new SmallMovement(service, handler, this);
		this.distanceCycledGps = new DistanceCycledGps(service, looper, handler, this);
		this.running = false;
		this.gpsTracking = false;
	}
	
	public synchronized void switchToState(State newState) {
		
	}
	
	public synchronized void start() {
		running = true;
		gpsTracking = false;
		smallMovement.start();
	}
	
	public synchronized void stop() {
		running = false;
		gpsTracking = false;
		smallMovement.stop();
		distanceCycledGps.stop();
	}
	
	public synchronized void pokeGpsHigh() {
		if (running) {
			if (!gpsTracking) {
				smallMovement.stop();
				gpsTracking = true;
			}
			distanceCycledGps.startOrPokeHigh(); // Switch it into HIGH mode no matter what it is doing now.
		} else {
			service.log("Gps poked while SensorScheduler is stopped!");
		}
	}

	/// Callbacks from sensors

	public synchronized void maybeSmallMovement() {
		if (running) {
			if (!gpsTracking) {
				smallMovement.stop();
				distanceCycledGps.start();
				gpsTracking = true;
			}
		}
	}

	public synchronized void noGpsMovement() {
		if (running) {
			if (gpsTracking) {
				smallMovement.start();
				distanceCycledGps.stop();
				gpsTracking = false;
			}
		}
	}
	
	public synchronized void locationChanged(Location location) {
		service.gpsFix(new LocationFix(location));
	}
	
}
