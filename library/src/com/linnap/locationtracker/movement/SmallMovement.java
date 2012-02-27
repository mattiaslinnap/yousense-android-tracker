package com.linnap.locationtracker.movement;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.linnap.locationtracker.SensorConfig;
import com.linnap.locationtracker.movement.DutyCycledAccelerometer.MovementDetectedListener;
import com.linnap.locationtracker.wifi.WifiPlaceChange;
import com.linnap.locationtracker.wifi.WifiPlaceChange.MaybePlaceChangedListener;

public class SmallMovement implements MovementDetectedListener, MaybePlaceChangedListener {

	Context context;
	Handler handler;
	SmallMovementDistanceListener listener;
	DutyCycledAccelerometer accel;
	WifiPlaceChange placeChange;
	boolean running;
	int accelMovementPeriods;
	
	public SmallMovement(Context context, Handler handler, SmallMovementDistanceListener listener) {
		this.context = context;
		this.handler = handler;
		this.listener = listener;
		this.accel = new DutyCycledAccelerometer(context, handler, this);
		if (SensorConfig.MOVEMENT_USES_WIFI) {
			this.placeChange = new WifiPlaceChange(context, handler, this);
		}
		this.running = false;
		this.accelMovementPeriods = 0;
	}
	
	public synchronized void start() {
		running = true;
		accelMovementPeriods = 0;
		if (SensorConfig.MOVEMENT_USES_WIFI) {
			placeChange.clearCheckpoint();
			placeChange.startCheckpoint();
		}
		accel.start();
	}
	
	public synchronized void stop() {
		running = false;
		accelMovementPeriods = 0;
		if (SensorConfig.MOVEMENT_USES_WIFI) {
			placeChange.clearCheckpoint();
		}
		accel.stop();
	}
	
	public interface SmallMovementDistanceListener {
		public void maybeSmallMovement();
	}

	/// Internal Sensor Callbacks
	
	public synchronized void movementDetected(long durationMillis) {
		if (running) {
//			AppGlobals.getLog(context).log("SM movementDetected()");
			accelMovementPeriods += 1;
			if (accelMovementPeriods >= SensorConfig.MOVEMENT_MIN_ACCEL_MOVEMENT_PERIODS) {
				if (SensorConfig.MOVEMENT_USES_WIFI) {
//					AppGlobals.getLog(context).log("SM starting WiFi comparison");
					placeChange.startComparison();
				} else {
//					AppGlobals.getLog(context).log("SM not using WiFi, maybe moved");
					listener.maybeSmallMovement();
				}
			}
		}
	}

	public void maybePlaceChanged(boolean changed) {
		if (!SensorConfig.MOVEMENT_USES_WIFI)
			Log.wtf(SensorConfig.TAG, "MOVEMENT_USES_WIFI is off, but maybePlaceChanged() called");
		
		if (running) {
			if (changed) {
				// Wifi agrees with place change. Report.
//				AppGlobals.getLog(context).log("SM maybe WiFi change");
				listener.maybeSmallMovement();
				// Do not accel periods, stopping movement detection is up to SensorScheduler.
			} else {
				// No actual place change. Walking around in the same room. Reset distance.
//				AppGlobals.getLog(context).log("SM WiFi not changed");
				accelMovementPeriods = 0;
			}
		}
	}
	
}
