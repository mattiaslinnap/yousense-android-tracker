package com.linnap.locationtracker.gps;

import android.location.Location;

/**
 * Location class that can be nicely serialized with Gson.
 */
public class LocationFix {
	long time;
	double lat;
	double lng;
	Float accuracy;
	Double altitude;
	Float bearing;
	Float speed;
	
	public LocationFix(Location location) {
		this.time = location.getTime();
		this.lat = location.getLatitude();
		this.lng = location.getLongitude();
		this.accuracy = location.hasAccuracy() ? location.getAccuracy() : null;
		this.altitude = location.hasAltitude() ? location.getAltitude() : null;
		this.bearing = location.hasBearing() ? location.getBearing() : null;
		this.speed = location.hasSpeed() ? location.getSpeed() : null;
	}
}
