package utils;

import java.util.ArrayList;

public class StatisticalData {
	public double changePerBlock_mean;
	public double changePerBlock_sd;
	public double changePerBlock_max;
	public double changePerBlock_min;
	public double changePerBlock_range;
	public double changePerBlock_skewness;
	public ArrayList<Integer> data;
	
	
	public double changeInterval_mean;
	public double changeInterval_sd;
	public double changeInterval_max;
	public double changeInterval_min;
	public double changeInterval_range;
	public double changeInterval_skewness;
	
	public double blockInterval_mean;
	public double blockInterval_sd;
	public double blockInterval_max;
	public double blockInterval_min;
	public double blockInterval_range;
	public double blockInterval_skewness;
	
	public double changePerInterval_mean;
	public double changePerInterval_sd;
	public double changePerInterval_max;
	public double changePerInterval_min;
	public double changePerInterval_range;
	public double changePerInterval_skewness;
	 
	
	public StatisticalData(double changePerBlock_mean, double changePerBlock_sd, double changePerBlock_max, double changePerBlock_min, double changePerBlock_skewness, ArrayList<Integer> data, double changeInterval_mean, double changeInterval_sd, 
			double changeInterval_max, double changeInterval_min, double changeInterval_skewness, double blockInterval_mean, double blockInterval_sd, double blockInterval_max, double blockInterval_min, double blockInterval_skewness, double changePerInterval_mean, double changePerInterval_sd, double changePerInterval_max, double changePerInterval_min, double changePerInterval_skewness) {
		this.changePerBlock_mean = changePerBlock_mean;
		this.changePerBlock_sd = changePerBlock_sd;
		this.changePerBlock_max = changePerBlock_max;
		this.changePerBlock_min = changePerBlock_min;
		this.changePerBlock_range = changePerBlock_max - changePerBlock_min;
		this.changePerBlock_skewness = changePerBlock_skewness;
		
		this.data = data;
		this.changeInterval_mean = changeInterval_mean;
		this.changeInterval_sd = changeInterval_sd;
		this.changeInterval_max = changeInterval_max;
		this.changeInterval_min = changeInterval_min;
		this.changeInterval_range = changeInterval_max - changeInterval_min;
		this.changeInterval_skewness = changeInterval_skewness;
		this.blockInterval_mean = blockInterval_mean;
		this.blockInterval_sd = blockInterval_sd;
		this.blockInterval_max = blockInterval_max;
		this.blockInterval_min = blockInterval_min;
		this.blockInterval_range = blockInterval_max - blockInterval_min;
		this.blockInterval_skewness = blockInterval_skewness;
		this.changePerInterval_mean = changePerInterval_mean;
		this.changePerInterval_sd = changePerInterval_sd;
		this.changePerInterval_max = changePerInterval_max;
		this.changePerInterval_min = changePerInterval_min;
		this.changePerInterval_range = changePerInterval_max - changePerInterval_min;
		this.changePerInterval_skewness = changePerInterval_skewness;
	}
	
	public boolean isValid() {
		return !Double.isNaN(changePerBlock_mean) && !Double.isNaN(changePerBlock_sd) && !Double.isNaN(changePerBlock_max) && !Double.isNaN(changePerBlock_min)
				&& !Double.isNaN(changePerBlock_range) && !Double.isNaN(changePerBlock_skewness) &&
				!Double.isNaN(changeInterval_mean) && !Double.isNaN(changeInterval_sd) && !Double.isNaN(changeInterval_max) && !Double.isNaN(changeInterval_min)
				&& !Double.isNaN(changeInterval_range) && !Double.isNaN(changeInterval_skewness) &&
				!Double.isNaN(blockInterval_mean) && !Double.isNaN(blockInterval_sd) && !Double.isNaN(blockInterval_max) && !Double.isNaN(blockInterval_min)
				&& !Double.isNaN(blockInterval_range) && !Double.isNaN(blockInterval_skewness) &&
				!Double.isNaN(changePerInterval_mean) && !Double.isNaN(changePerInterval_sd) && !Double.isNaN(changePerInterval_max) && !Double.isNaN(changePerInterval_min)
				&& !Double.isNaN(changePerInterval_range) && !Double.isNaN(changePerInterval_skewness);
	}
	
//	public double getMean() {
//		return ;
//	}
//	
//	public double getStandardDeviation() {
//		return standardDeviation;
//	}
//	
//	public ArrayList<Integer> getData() {
//		return data;
//	}
	
}
