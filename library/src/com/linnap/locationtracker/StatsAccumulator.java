package com.linnap.locationtracker;

/**
 * Fixed-size buffer for calculating statistical summaries (average, variance) of data.
 * Silently stops accumulating on overflow.
 */
public class StatsAccumulator {

	int size;
	double[] buffer;
	int count;
	double sum;
	
	public StatsAccumulator(int size) {
		this.size = size;
		this.buffer = new double[size];
		this.count = 0;
		this.sum = 0;
	}
	
	public void clear() {
		count = 0;
		sum = 0;
	}
	
	public void add(double value) {
		if (count < size - 1) {
			buffer[count++] = value;
			sum += value;
		}
	}
	
	public double mean() {
		return sum / count;
	}
	
	public double variance() {
		double m = mean(); 
		double squaredDifference = 0;
		for (int i = 0; i < count; ++i)
			squaredDifference += (buffer[i] - m) * (buffer[i] - m);
		return squaredDifference / count;
	}
}
