package com.linnap.locationtracker;

import android.hardware.SensorManager;

public class SensorConfig {

	public static final String TAG = "locationtracker";
	
	// Duty-Cycled Accelerometer
	
	public static int ACCELEROMETER_RATE = SensorManager.SENSOR_DELAY_FASTEST;	
	public static long ACCELEROMETER_ACTIVE_MILLIS = 1000;
	public static long ACCELEROMETER_SLEEP_MILLIS = 9000;
	public static int ACCELEROMETER_ACCUMULATOR_SIZE = 50;  // Adjust to hold a bit more than all sensor events within active_millis.
	public static double ACCELEROMETER_MAGNITUDE_VARIANCE_FOR_MOVEMENT = 0.1f;  // Minimum variance of accel magnitude within active_millis that means movement.
	
	// WiFi Place Change Detector
	
	public static long WIFI_SCAN_TIMEOUT_MILLIS = 5000;
	public static int WIFI_MIN_STRONG_RSSI = -70;
	public static double WIFI_SCAN_MAX_TANIMOTO_SIMILARITY_FOR_SAME_PLACE = 0.4;
	
	// Movement Detector
	
	public static boolean MOVEMENT_USES_WIFI = true;
	public static double MOVEMENT_MIN_ACCEL_MOVEMENT_PERIODS = 3;

	// Distance-Cycled GPS
	
	public static long GPS_HIGHSPEED_DELAY_MILLIS = 1000;
	public static long GPS_LOWSPEED_DELAY_MILLIS = 16000;
	
	public static int GPS_HISTORY_SIZE = 15;
	public static int GPS_HIGHTOLOW_SPEEDS_CONSIDERED = 15; // 120 fixes at 1Hz, or >2 minutes.
	public static int GPS_LOWTOOFF_DISTANCES_CONSIDERED = 15; // 15 fixes at 1/16Hz, or >4 minutes.
	
	public static float GPS_SWITCH_SPEED_THRESHOLD = 3.0f; // m/s. Switch to other speed if above/below this.
	public static float GPS_STATIONARY_DISTANCE_THRESHOLD = 20.0f; // m. Switch GPS off if within this distance.
	
	public static long GPS_GIVEUP_AFTER_NO_FIX_FOR_MILLIS = 60000;  // Switch GPS off if no fixes within this time.
	public static long GPS_GIVEUP_CHECK_DELAY = 5000;
	
	public static float GPS_MAX_VALID_SPEED = 100.0f; // Speed readings above this are considered invalid.
	
}
