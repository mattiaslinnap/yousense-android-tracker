package com.linnap.locationtracker;

/**
 * Holder for state change event reporting.
 */
public class StateChange {
	String from;
	String to;
	
	public StateChange(Object from, Object to) {
		this.from = from.toString();
		this.to = to.toString();
	}
	
	@Override
	public String toString() {
		return from + " to " + to;
	}
}
