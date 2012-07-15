package com.linnap.locationtracker.wifi;

import java.util.ArrayList;
import java.util.List;

import android.net.wifi.ScanResult;

public class ScanResultsData {
	List<Result> results;
	boolean failed;
	
	public ScanResultsData(List<ScanResult> scanResults, boolean failed) {
		this.results = new ArrayList<Result>();
		this.failed = failed;
		if (scanResults != null) {
			for (ScanResult scanResult : scanResults)
				this.results.add(new Result(scanResult));
		}
	}
	
	class Result {
		String ssid;
		String bssid;
		int level;
		
		public Result(ScanResult scanResult) {
			this.ssid = scanResult.SSID;
			this.bssid = scanResult.BSSID;
			this.level = scanResult.level;
		}
	}
	
	@Override
	public String toString() {
		return String.format("%d APs, scan %s", results.size(), failed ? "failed" : "succeeded");
	}
}
