package com.linnap.locationtracker.schedule;

import com.linnap.locationtracker.LocationTrackerServiceX;
import com.linnap.locationtracker.SensorConfig;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class SensorThread extends Thread {
	
	LocationTrackerServiceX service;
	Handler handler;
	SensorScheduler sensorScheduler;

	public SensorThread(LocationTrackerServiceX service) {
		this.service = service;
		this.handler = null;  // Initialized once the thread is running.
		this.sensorScheduler = null; // Needs handler and looper
	}
	
	public void run() {
		Looper.prepare();
		handler = new Handler();
		sensorScheduler = new SensorScheduler(service, Looper.myLooper(), handler);
		sensorScheduler.start();
		Looper.loop();
	}
	
	/// Public access. Can be called from other threads.
	
	public void pokeGpsHigh() {
		sensorScheduler.pokeGpsHigh();
	}
	
	public void quit() {
		sensorScheduler.stop();
		Looper looper = handler.getLooper();
		if (looper != null)
			looper.quit();
		else
			Log.wtf(SensorConfig.TAG, "Cannot quit SensorThread Looper, as it is null.");
	}

}
