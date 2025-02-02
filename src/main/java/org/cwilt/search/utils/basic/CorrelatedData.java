package org.cwilt.search.utils.basic;
import java.util.Random;

public class CorrelatedData {
	private final Random r;
	
	private final double xMean, xStdev, yMean, yStdev, corr, a;
	
	private static final int ARRSIZE = 10000;
	
	public double getY(double x){
		double zScore = (x - xMean) / xStdev;
		double yZScore = (zScore * a + r.nextGaussian()) / Math.sqrt(a*a + 1);
		return yZScore * yStdev + yMean;
	}
	
	public CorrelatedData(int seed, double xMean, double xStdev, double yMean,
			double yStdev, double corr) {
		this.xMean = xMean;
		this.yMean = yMean;
		this.xStdev = xStdev;
		this.yStdev = yStdev;
		r = new Random(seed);
		if(corr == 0)
			this.corr = 0.00001;
		else if(corr == 1)
			this.corr = .99999;
		else 
			this.corr = corr;
			
		this.a = this.corr / Math.sqrt(1 - (this.corr * this.corr));
	}
	

	private static double getGaussian(double mean, double stdev, Random r){
		return r.nextGaussian() * stdev + mean;
	}
	
	public static void main(String[] args){
		
		double xMean = 10;
		double yMean = 20;
		double xStdev = 5;
		double yStdev = 3;
		double corr = 0.5;
		
		Random random = new Random(100);
		int count = 5;
		double[] c2 = new double[count];
		for(int i = 0; i < count; i++){
			CorrelatedData d = new CorrelatedData(i * 10, xMean,xStdev,yMean,yStdev,corr);
			
			double[] xData = new double[ARRSIZE];
			double[] yData = new double[ARRSIZE];
			
			for(int j = 0; j < ARRSIZE; j++){
				xData[j] = CorrelatedData.getGaussian(xMean, xStdev, random);
				yData[j] = d.getY(xData[j]);
			}
			c2[i] = Stats.pearson(xData, yData);
			System.err.printf("x stdev %f y stdev %f\n",Stats.stdev(xData), Stats.stdev(yData));
		}
		System.err.println(Stats.mean(c2));
	}
}
