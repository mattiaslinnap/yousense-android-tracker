package com.linnap.locationtracker.wifi;

import java.util.HashSet;
import java.util.List;

import com.linnap.locationtracker.SensorConfig;

import android.net.wifi.ScanResult;
import com.linnap.locationtracker.Sets;

public class WifiFingerprint {

	HashSet<String> binnedBssids;
	
	public WifiFingerprint(List<ScanResult> results) {
		binnedBssids = new HashSet<String>();
		for (ScanResult result : results)
			binnedBssids.add(binnedBssid(result));
	}
	
	/**
	 * Returns similarity to other fingerprint within range [0,1].
	 * High value: probably same place.
	 * Low value: probably different place.
	 * 
	 * If both fingerprints are empty sets, returns 0.0, as there is no way to tell for sure.
	 */
	public double similarity(WifiFingerprint other) {
		if (binnedBssids.size() == 0 && other.binnedBssids.size() == 0)
			return 0.0;
		else
			return Sets.tanimotoSimilarity(binnedBssids, other.binnedBssids);
	}
	
	private static String binnedBssid(ScanResult result) {
		return (result.level > SensorConfig.WIFI_MIN_STRONG_RSSI ? "strong_" : "weak_") + result.BSSID;
	}
}
