package com.linnap.locationtracker;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import com.linnap.locationtracker.gps.DistanceCycledGps;
import com.linnap.locationtracker.gps.DistanceCycledGps.GpsMovementListener;
import com.linnap.locationtracker.gps.LocationFix;
import com.linnap.locationtracker.movement.SmallMovement;
import com.linnap.locationtracker.movement.SmallMovement.SmallMovementDistanceListener;

public class SensorScheduler implements SmallMovementDistanceListener, GpsMovementListener {

	SensorService service;
	Looper looper;  // Thread looper where to post events. Comes from SensorThread.Looper.
	Handler handler;  // Handler where to post delayed events. Comes from SensorThread.Looper.
	
	SmallMovement smallMovement;
	DistanceCycledGps distanceCycledGps;
	boolean running;
	boolean gpsTracking;
	
	public SensorScheduler(SensorService service, Looper looper, Handler handler) {
		this.service = service;
		this.looper = looper;
		this.handler = handler;
		
		this.smallMovement = new SmallMovement(service, handler, this);
		this.distanceCycledGps = new DistanceCycledGps(service, looper, handler, this);
		this.running = false;
		this.gpsTracking = false;
	}
	
	public synchronized void start() {
		service.log("SensorScheduler start()");
		running = true;
		gpsTracking = false;
		smallMovement.start();
	}
	
	public synchronized void stop() {
		service.log("SensorScheduler stop()");
		running = false;
		gpsTracking = false;
		smallMovement.stop();
		distanceCycledGps.stop();
	}

	/// Callbacks from sensors

	public synchronized void maybeSmallMovement() {
		service.log("SensorSch. maybeSmallMovement()");
		if (running) {
			if (!gpsTracking) {
				service.log("SensorScheduler starting GPS tracking");
				smallMovement.stop();
				distanceCycledGps.start();
				gpsTracking = true;
			}
		}
	}

	public synchronized void noGpsMovement() {
		service.log("SensorSch. noGpsMovement()");
		if (running) {
			if (gpsTracking) {
				service.log("SensorScheduler stopping GPS tracking");
				smallMovement.start();
				distanceCycledGps.stop();
				gpsTracking = false;
			}
		}
	}
	
	public synchronized void locationChanged(Location location) {
		LocationFix fix = new LocationFix(location);
		SensorService.lastKnownLocation = fix;
		service.locationChanged(fix);
	}
	
}
