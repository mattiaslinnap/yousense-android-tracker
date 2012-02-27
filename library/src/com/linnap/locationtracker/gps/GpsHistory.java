package com.linnap.locationtracker.gps;

import java.util.ArrayDeque;

import android.content.Context;
import android.location.Location;

import com.linnap.locationtracker.SensorConfig;

public class GpsHistory {

	Context context;
	ArrayDeque<Location> history;
	
	public GpsHistory(Context context) {
		this.context = context;
		this.history = new ArrayDeque<Location>(SensorConfig.GPS_HISTORY_SIZE);
	}
	
	public synchronized void clear() {
		history.clear();
	}
	
	public synchronized void addFix(Location latest) {
		// Add fix to history.
		if (history.size() >= SensorConfig.GPS_HISTORY_SIZE)
			history.removeLast();
		
		history.addFirst(withValidSpeed(latest));
	}

	/**
	 * Assumes GPS is tracking at low speed.
	 */
	public synchronized boolean shouldSwitchToHighSpeed() {
		// If there is no history yet, should not do any switching.
		if (history.isEmpty()) {
//			AppGlobals.getLog(context).log("No history, perhaps still slow.");
			return false;
		}
		
		// Switch immediately if phone is moving fast.
		float speed = history.getFirst().getSpeed();
		if (speed > SensorConfig.GPS_SWITCH_SPEED_THRESHOLD) {
//			AppGlobals.getLog(context).log(String.format("Latest speed %.3f, go to fast.", speed));
			return true;
		} else {
//			AppGlobals.getLog(context).log(String.format("Latest speed %.3f, still slow.", speed));
			return false;
		}
	}

	/**
	 * Assumes GPS is tracking at high speed.
	 */
	public synchronized boolean shouldSwitchToLowSpeed() {
		// If history is too short to make a decision, should not do any switching.
		if (history.size() < SensorConfig.GPS_HIGHTOLOW_SPEEDS_CONSIDERED) {
//			AppGlobals.getLog(context).log(String.format("History %d, perhaps still fast.", history.size()));
			return false;
		}

		float maxSpeed = Float.NEGATIVE_INFINITY;
		for (Location loc : history)
			if (loc.getSpeed() > maxSpeed)
				maxSpeed = loc.getSpeed();
				
		if (maxSpeed < SensorConfig.GPS_SWITCH_SPEED_THRESHOLD) {
//			AppGlobals.getLog(context).log(String.format("Max speed %.3f, go to slow.", maxSpeed));
			return true;
		} else {
//			AppGlobals.getLog(context).log(String.format("Max speed %.3f, still fast.", maxSpeed));
			return false;
		}
	}
	
	/**
	 * Assumes GPS is tracking at low speed.
	 */
	public synchronized boolean shouldReportStationary() {
		// If history is too short to make a decision, should not do any switching.
		if (history.size() < SensorConfig.GPS_LOWTOOFF_DISTANCES_CONSIDERED)
			return false;
		
		float maxDistance = Float.NEGATIVE_INFINITY;
		Location latest = history.getFirst();
		for (Location historical : history) {
			float distance = latest.distanceTo(historical);
			
			if (distance > maxDistance)
				maxDistance = distance;
			
			if (maxDistance > SensorConfig.GPS_STATIONARY_DISTANCE_THRESHOLD) {
//				AppGlobals.getLog(context).log(String.format("Distance %.3f found, out of stationary bounds.", maxDistance));
				// Return false early, avoid computing all distances.
				return false;
			}
		}
		
//		AppGlobals.getLog(context).log(String.format("Max distance %.3f, stationary.", maxDistance));
		return true;
	}
	
	private Location withValidSpeed(Location location) {
		if (location.hasSpeed()) {
			if (location.getSpeed() >= 0.0f && location.getSpeed() < SensorConfig.GPS_MAX_VALID_SPEED)				
				return location;  // Speed from fix is valid.
//			else
//				AppGlobals.getLog(context).log(String.format("Fix has invalid speed: %.4f", location.getSpeed()));
		} else {
//			AppGlobals.getLog(context).log("Fix has no speed");
		}
		
		// Speed is missing or messed up. Computing between last points, if possible.
		Location valid = new Location(location);
		if (history.isEmpty()) {
			valid.setSpeed(0.0f);
		} else {
			Location last = history.getFirst();
			float distance = location.distanceTo(last);
			float seconds = (location.getTime() - last.getTime()) / 1000.0f;
			if (distance >= 0.0f && seconds > 0.0f) {
				valid.setSpeed(distance / seconds);
			} else {
//				AppGlobals.getLog(context).log(String.format("Cannot compute speed with distance %.3f m and %.3f sec", distance, seconds));
				valid.setSpeed(0.0f);
			}
		}
		return valid;
	}
}
