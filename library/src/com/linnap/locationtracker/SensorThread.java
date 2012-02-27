package com.linnap.locationtracker;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class SensorThread extends Thread {
	
	SensorService service;
	Handler handler;
	SensorScheduler scheduler;
	
	public SensorThread(SensorService service) {
		this.service = service;
		this.handler = null;  // Initialized once the thread is running.
		this.scheduler = null;  // Needs handler.		
	}
	
	public void run() {
		Looper.prepare();
		handler = new Handler();
		scheduler = new SensorScheduler(service, Looper.myLooper(), handler);
		scheduler.start();		
		Looper.loop();
	}
	
	/// Public access. Can be called from other threads.
	
	public void quit() {
		scheduler.stop();
		Looper looper = handler.getLooper();
		if (looper != null)
			looper.quit();
		else
			Log.wtf(SensorConfig.TAG, "Cannot quit SensorThread Looper, as it is null.");
	}

}
