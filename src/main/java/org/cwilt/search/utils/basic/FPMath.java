package org.cwilt.search.utils.basic;
public class FPMath {

	private static final double FP_DEL = 0.0000000000000001;
	
	public static boolean fp_eq(double f1, double f2){
		return (Math.abs(f1 - f2) < FP_DEL);
	}
	
	
}
