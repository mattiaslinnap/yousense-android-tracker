package com.linnap.locationtracker.movement;

/**
 * Fixed-size buffer for calculating statistical summaries (average, variance) of data.
 * Silently stops accumulating on overflow.
 */
public class StatsAccumulator {

	int size;
	float[] buffer;
	int count;
	float sum;
	
	public StatsAccumulator(int size) {
		this.size = size;
		this.buffer = new float[size];
		this.count = 0;
		this.sum = 0;
	}
	
	public void clear() {
		count = 0;
		sum = 0;
	}
	
	public void add(float value) {
		if (count < size - 1) {
			buffer[count++] = value;
			sum += value;
		}
	}
	
	public float mean() {
		return sum / count;
	}
	
	public float variance() {
		float m = mean(); 
		float squaredDifference = 0;
		for (int i = 0; i < count; ++i)
			squaredDifference += (buffer[i] - m) * (buffer[i] - m);
		return squaredDifference / count;
	}
}
