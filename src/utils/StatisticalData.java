package utils;

import java.util.ArrayList;

public class StatisticalData {
	private double mean;
	private double standardDeviation;
	private ArrayList<Integer> data;
	
	public StatisticalData(double mean, double standardDeviation,ArrayList<Integer> data) {
		this.mean = mean;
		this.standardDeviation = standardDeviation;
		this.data = data;
	}
	
	public double getMean() {
		return mean;
	}
	
	public double getStandardDeviation() {
		return standardDeviation;
	}
	
	public ArrayList<Integer> getData() {
		return data;
	}
	
}
