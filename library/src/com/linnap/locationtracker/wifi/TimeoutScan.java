package com.linnap.locationtracker.wifi;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;

import com.linnap.locationtracker.LocationTrackerService;
import com.linnap.locationtracker.SensorConfig;

/**
 * Wifi Scanner with timeouts.
 * This is a single-use class, create a new one each time.
 */
public class TimeoutScan {

	LocationTrackerService service;
	Handler handler;
	WifiManager wifiManager;	
	WifiScanFinished listener;
	boolean scanStarted;
	boolean listenerCalled;
	
	public TimeoutScan(LocationTrackerService service, Handler handler, WifiScanFinished listener) {
		this.service = service;
		this.handler = handler;
		this.wifiManager = (WifiManager)service.getSystemService(Context.WIFI_SERVICE);
		this.listener = listener;
		this.scanStarted = false;
		this.listenerCalled = false;
	}
	
	/**
	 * Starts the scan. Returns itself, for create and start() in one line.
	 */
	public synchronized TimeoutScan start() {
		if (scanStarted || listenerCalled) {
			throw new IllegalStateException("TimeoutScan is a single-use class.");
		} else {
			scanStarted = true;
			handler.postDelayed(timeout, SensorConfig.WIFI_SCAN_TIMEOUT_MILLIS);
			service.registerReceiver(scanResultsReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION), null, handler);
			service.event("wifi_scan_started", null);
			wifiManager.startScan();  // If startScan() fails, still not calling listener right now. Perhaps easier to think about threads.			
			return this;
		}
	}
	
	public interface WifiScanFinished {
		public void wifiScanFinished(List<ScanResult> results, boolean failed);
	}
	
	/// Implementation
	
	private synchronized void reportResults(List<ScanResult> results, boolean failed) {
		service.event("wifi_scan_results", null);
		if (!listenerCalled) {
			listenerCalled = true;
			// Unregister BroadcastReceiver only once
			service.unregisterReceiver(scanResultsReceiver);
			// Do not bother unregistering timeout handler.
			listener.wifiScanFinished(results, failed);
		} else {
			// Listener already called. Ignoring double reports.
		}
	}
	
	Runnable timeout = new Runnable() {
		public void run() {
			reportResults(new ArrayList<ScanResult>(), true);
		}
	};
	
	BroadcastReceiver scanResultsReceiver = new BroadcastReceiver() {
		public void onReceive(Context _context, Intent intent) {
			List<ScanResult> results = wifiManager.getScanResults();
			if (results == null) {
				reportResults(new ArrayList<ScanResult>(), true);
			} else {
				reportResults(results, false);
			}
		}
	};
}
