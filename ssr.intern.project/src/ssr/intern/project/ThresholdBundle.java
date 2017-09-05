package ssr.intern.project;

import java.util.ArrayList;
import java.util.List;

public class ThresholdBundle {

	public static int threshold = 15;  //by default 
	public static int payment = 10;  //by default 
	private static List<ThresholdUpdateListener> listeners = new ArrayList<ThresholdUpdateListener>();
	
	public void addListener(ThresholdUpdateListener toAdd) {
        listeners.add(toAdd);
    }
	
	public static int getThreshold() {
		return threshold;
	}
	
	public void setThreshold(int newThreshold) {
		if (threshold != newThreshold) {
			threshold = newThreshold;
			for (ThresholdUpdateListener hl : listeners)
	            hl.thresholdUpdated();
			
		}
	}
	
	public static int getPayment() {
		return payment;
	}
	
	public void setPayment(int newPayment) {
		payment = newPayment;
	}
}
