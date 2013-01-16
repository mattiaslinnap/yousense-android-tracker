package com.linnap.locationtracker;

import java.util.Set;

public class Sets {

	public static int intersectionSize(Set<?> a, Set<?> b) {
		int common = 0;
		for (Object inA : a) {
			if (b.contains(inA))
				++common;
		}
		return common;
	}
	
	public static int unionSize(Set<?> a, Set<?> b) {
		return a.size() + b.size() - intersectionSize(a, b);
	}
	
	/**
	 * Computes Tanimoto set similarity metric in range [0,1].
	 * Returns 1.0 if both sets are empty.
	 * 
	 * http://en.wikipedia.org/wiki/Jaccard_index
	 * Not exactly the same as Tanimoto Coefficient. 
	 */
	public static double tanimotoSimilarity(Set<?> a, Set<?> b) {
		int intersection = intersectionSize(a, b);
		int union = a.size() + b.size() - intersection;
		
		if (union == 0)
			return 1.0;
		else
			return 1.0 * intersection / union;
	}
}
