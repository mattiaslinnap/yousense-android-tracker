package com.linnap.locationtracker.movement;

public class AccelerometerData {
	float magnitude_variance;
	
	public AccelerometerData(float magnitude_variance) {
		this.magnitude_variance = magnitude_variance;
	}
	
	@Override
	public String toString() {
		return "variance=" + magnitude_variance;
	}
}
