package com.linnap.locationtracker.wifi;

import java.util.List;

import android.net.wifi.ScanResult;
import android.os.Handler;

import com.linnap.locationtracker.LocationTrackerService;
import com.linnap.locationtracker.SensorConfig;
import com.linnap.locationtracker.wifi.TimeoutScan.WifiScanFinished;

/**
 * Detects significant place changes based on WiFi fingerprint changes.
 * Does not schedule itself, has to be polled.
 * Note that due to the asynchronous nature of WiFi scans, it may take up to a few seconds for checkpoint or placeChanged to actually update state.
 */
public class WifiPlaceChange {

	LocationTrackerService service;
	Handler handler;
	MaybePlaceChangedListener listener;
	WifiFingerprint checkpoint;
	TimeoutScan checkpointScan;
	TimeoutScan placeChangedScan;
	
	public WifiPlaceChange(LocationTrackerService service, Handler handler, MaybePlaceChangedListener listener) {
		this.service = service;
		this.handler = handler;
		this.listener = listener;
		this.checkpoint = null;
		this.checkpointScan = null;
		this.placeChangedScan = null;
	}

	/**
	 * Clears any checkpoint. Does not stop any checkpoint scans in progress.
	 */
	public synchronized void clearCheckpoint() {
		checkpoint = null;
	}
	
	/**
	 * Starts a new scan for a checkpoint. Checkpoint will be updated a few seconds later.
	 */
	public synchronized void startCheckpoint() {		
		if (checkpointScan == null) {
			checkpointScan = new TimeoutScan(service, handler, new WifiScanFinished() {
				public void wifiScanFinished(List<ScanResult> results, boolean failed) {
					updateCheckpoint(results, failed);
				}
			}).start();
		} else {			
			// Checkpointing already in progress. Do not start another one, wait for it to finish, and use its results.
		}
	}
	
	/**
	 * Starts a new scan for comparison to checkpoint. Listener will be called a few seconds later.
	 */
	public synchronized void startComparison() {
		// TODO: Consider optimising for no checkpoint. Could fire maybePlaceChanged(true) immediately.
		if (placeChangedScan == null) {
			placeChangedScan = new TimeoutScan(service, handler, new WifiScanFinished() {
				public void wifiScanFinished(List<ScanResult> results, boolean failed) {
					updatePlaceChanged(results, failed);
				}
			}).start();
		} else {
			// Scan already in progress. Do not start another one, wait it to finish and use its results.
		}
	}
	
	public interface MaybePlaceChangedListener {
		public void maybePlaceChanged(boolean changed);
	}
	
	/// State updates
	
	private synchronized void updateCheckpoint(List<ScanResult> results, boolean failed) {
		checkpoint = failed ? null : new WifiFingerprint(results);
		checkpointScan = null;
	}
	
	private synchronized void updatePlaceChanged(List<ScanResult> results, boolean failed) {
		if (checkpoint == null) {
			// Nothing to compare against. May have moved.
			listener.maybePlaceChanged(true);
		} else if (failed) {
			// Nothing to compare against. May have moved.
			listener.maybePlaceChanged(true);
		} else {
			WifiFingerprint now = new WifiFingerprint(results);
			double similarity = checkpoint.similarity(now);
			boolean maybeMoved = similarity < SensorConfig.WIFI_SCAN_MAX_TANIMOTO_SIMILARITY_FOR_SAME_PLACE;
			listener.maybePlaceChanged(maybeMoved);
		}
		placeChangedScan = null;
	}
}
