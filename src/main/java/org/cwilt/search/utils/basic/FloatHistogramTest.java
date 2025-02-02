package org.cwilt.search.utils.basic;
import org.junit.Test;

public class FloatHistogramTest {
	@Test
	public void printTest(){
		double[] f = new double[10];
		f[0] = 1;
		f[1] = 1;
		f[2] = 2;
		
		FloatHistogram h = new FloatHistogram(f);
		
		System.err.println(h);
		System.err.println(h.mostCommon());
	}
}
