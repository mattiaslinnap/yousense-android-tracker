package com.linnap.locationtracker.gps;

import android.location.Location;

/**
 * Location class that can be nicely serialized with Gson.
 */
public class LocationFix {
	public long time;
	public double lat;
	public double lng;
	public Float accuracy;
	public Double altitude;
	public Float bearing;
	public Float speed;
	
	public LocationFix() {		
	}
	
	public LocationFix(Location location) {
		this.time = location.getTime();
		this.lat = location.getLatitude();
		this.lng = location.getLongitude();
		this.accuracy = location.hasAccuracy() ? location.getAccuracy() : null;
		this.altitude = location.hasAltitude() ? location.getAltitude() : null;
		this.bearing = location.hasBearing() ? location.getBearing() : null;
		this.speed = location.hasSpeed() ? location.getSpeed() : null;
	}

	@Override
	public String toString() {
		return String.format("%.5f north, %.5f east at %d", lat, lng, time);
	}
}
