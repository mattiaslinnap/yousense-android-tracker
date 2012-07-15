package com.linnap.locationtracker.wifi;

public class ScanStartedData {
	boolean failed;
	
	public ScanStartedData(boolean failed) {
		this.failed = failed;
	}
	
	@Override
	public String toString() {
		return "scan " + (failed ? "failed" : "succeeded");
	}
}
