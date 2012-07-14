package com.linnap.locationtracker.gps;

import android.location.Location;
import android.os.Bundle;

/**
 * Location class that can be nicely serialized with Gson.
 */
public class LocationFix {
	public String provider;
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
		this.provider = location.getProvider();
		this.time = location.getTime();
		this.lat = location.getLatitude();
		this.lng = location.getLongitude();
		this.accuracy = location.hasAccuracy() ? location.getAccuracy() : null;
		this.altitude = location.hasAltitude() ? location.getAltitude() : null;
		this.bearing = location.hasBearing() ? location.getBearing() : null;
		this.speed = location.hasSpeed() ? location.getSpeed() : null;
	}
	
	public LocationFix(Bundle b) {
		if (b != null) {
			this.provider = b.getString("provider");
			this.time = b.getLong("time", 0);
			this.lat = b.getDouble("lat", 0.0);
			this.lng = b.getDouble("lng", 0.0);
			if (b.containsKey("accuracy"))
				this.accuracy = b.getFloat("accuracy", 0.0f);
			if (b.containsKey("altitude"))
				this.altitude = b.getDouble("altitude", 0.0);
			if (b.containsKey("bearing"))
				this.bearing = b.getFloat("bearing", 0.0f);
			if (b.containsKey("speed"))
				this.speed = b.getFloat("speed", 0.0f);
		}
	}

	@Override
	public String toString() {
		return String.format("%.5f north, %.5f east at %d", lat, lng, time);
	}
	
	public Bundle toBundle() {
		Bundle b = new Bundle();
		b.putString("provider",  provider);
		b.putLong("time", time);
		b.putDouble("lat", lat);
		b.putDouble("lng", lng);
		if (accuracy != null)
			b.putFloat("accuracy", accuracy.floatValue());
		if (altitude != null)
			b.putDouble("altitude", altitude.doubleValue());
		if (bearing != null)
			b.putFloat("bearing", bearing.floatValue());
		if (speed != null)
			b.putFloat("speed", speed.floatValue());		
		return b;
	}
}
