package com.linnap.locationtracker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class SensorThread extends Thread {
	
	Context context;
	Handler handler;
	SensorScheduler scheduler;
	
	public SensorThread(Context context) {
		this.context = context;
		this.handler = null;  // Initialized once the thread is running.
		this.scheduler = null;  // Needs handler.		
	}
	
	public void run() {
		Looper.prepare();
		handler = new Handler();
		scheduler = new SensorScheduler(context, Looper.myLooper(), handler);
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
