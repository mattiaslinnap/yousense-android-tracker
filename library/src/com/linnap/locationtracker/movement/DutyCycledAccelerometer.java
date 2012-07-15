package com.linnap.locationtracker.movement;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.FloatMath;

import com.linnap.locationtracker.LocationTrackerService;
import com.linnap.locationtracker.SensorConfig;
import com.linnap.locationtracker.StateChange;

public class DutyCycledAccelerometer implements SensorEventListener {
	
	LocationTrackerService service;
	MovementDetectedListener listener;
	Handler handler;
	SensorManager sensorManager;	
	Sensor accelerometer;
	StatsAccumulator magnitude;
	boolean running;
	
	public DutyCycledAccelerometer(LocationTrackerService service, Handler handler, MovementDetectedListener listener) {
		this.service = service;
		this.listener = listener;
		this.handler = handler;
		this.sensorManager = (SensorManager)service.getSystemService(Context.SENSOR_SERVICE);
		this.accelerometer = this.sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		this.magnitude = new StatsAccumulator(SensorConfig.ACCELEROMETER_ACCUMULATOR_SIZE);
		this.running = false;
	}
	
	public synchronized void start() {
		service.event("dutycycledaccelerometer_state", new StateChange("OFF", "ON"));
		running = true;
		startAccelerometer.run();
	}
	
	public synchronized void stop() {
		service.event("dutycycledaccelerometer_state", new StateChange("ON", "OFF"));
		running = false;
		sensorManager.unregisterListener(DutyCycledAccelerometer.this);
		handler.removeCallbacks(startAccelerometer);
		handler.removeCallbacks(stopAccelerometer);
	}
	
	public boolean isRunning() {
		return running;
	}
	
	/// Implementation.
	
	Runnable startAccelerometer = new Runnable() {
		public void run() {
			synchronized (DutyCycledAccelerometer.this) {
				if (running) {
					service.event("accelerometer_state", new StateChange("OFF", "ON"));
					magnitude.clear();
					sensorManager.registerListener(DutyCycledAccelerometer.this, accelerometer, SensorConfig.ACCELEROMETER_RATE, handler);
					handler.postDelayed(stopAccelerometer, SensorConfig.ACCELEROMETER_ACTIVE_MILLIS);
				}
			}
		}
	};
	
	Runnable stopAccelerometer = new Runnable() {
		public void run() {
			synchronized (DutyCycledAccelerometer.this) {
				if (running) {
					service.event("accelerometer_state", new StateChange("ON", "OFF"));
					sensorManager.unregisterListener(DutyCycledAccelerometer.this);
					if (magnitude.variance() >= SensorConfig.ACCELEROMETER_MAGNITUDE_VARIANCE_FOR_MOVEMENT) {
						// Worst-case estimate of duration includes sleep time.
						long duration = SensorConfig.ACCELEROMETER_ACTIVE_MILLIS + SensorConfig.ACCELEROMETER_SLEEP_MILLIS;
						listener.movementDetected(duration);						
					}
					handler.postDelayed(startAccelerometer, SensorConfig.ACCELEROMETER_SLEEP_MILLIS);
				}
			}
		}
	};

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {
		magnitude.add(FloatMath.sqrt(event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]));
	}
	
	
	/// Callbacks
	
	public interface MovementDetectedListener {
		public void movementDetected(long durationMillis);
	}
}
