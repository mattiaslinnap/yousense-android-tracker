package com.linnap.locationtracker;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import com.linnap.locationtracker.gps.DistanceCycledGps;
import com.linnap.locationtracker.gps.DistanceCycledGps.GpsMovementListener;
import com.linnap.locationtracker.movement.SmallMovement;
import com.linnap.locationtracker.movement.SmallMovement.SmallMovementDistanceListener;

public class SensorScheduler implements SmallMovementDistanceListener, GpsMovementListener {

	Context context;
	Looper looper;  // Thread looper where to post events. Comes from SensorThread.Looper.
	Handler handler;  // Handler where to post delayed events. Comes from SensorThread.Looper.
	
	SmallMovement smallMovement;
	DistanceCycledGps distanceCycledGps;
	boolean running;
	boolean gpsTracking;
	
	public SensorScheduler(Context context, Looper looper, Handler handler) {
		this.context = context;
		this.looper = looper;
		this.handler = handler;
		
		this.smallMovement = new SmallMovement(context, handler, this);
		this.distanceCycledGps = new DistanceCycledGps(context, looper, handler, this);
		this.running = false;
		this.gpsTracking = false;
	}
	
	public synchronized void start() {
//		AppGlobals.getLog(context).log("SensorScheduler start()");
		running = true;
		gpsTracking = false;
		smallMovement.start();
	}
	
	public synchronized void stop() {
//		AppGlobals.getLog(context).log("SensorScheduler stop()");
		running = false;
		gpsTracking = false;
		smallMovement.stop();
		distanceCycledGps.stop();
	}

	/// Callbacks from sensors

	public synchronized void maybeSmallMovement() {
//		AppGlobals.getLog(context).log("SensorSch. maybeSmallMovement()");
		if (running) {
			if (!gpsTracking) {
//				AppGlobals.getLog(context).log("SensorScheduler starting GPS tracking");
				smallMovement.stop();
				distanceCycledGps.start();
				gpsTracking = true;
			}
		}
	}

	public synchronized void noGpsMovement() {
//		AppGlobals.getLog(context).log("SensorSch. noGpsMovement()");
		if (running) {
			if (gpsTracking) {
//				AppGlobals.getLog(context).log("SensorScheduler stopping GPS tracking");
				smallMovement.start();
				distanceCycledGps.stop();
				gpsTracking = false;
			}
		}
	}
	
	public synchronized void locationChanged(Location location) {
		// TODO: pass on to some listener.
//		AppGlobals.getQueue(context, "location").append("gps", new LocationFix(location));
	}
	
}
