package org.cwilt.search.utils.floats;
import org.junit.Test;

public class FloatTesting {
	@Test
	public void printing(){
		System.err.println(FloatUtils.doubleString(10));
	}
	@Test
	public void longIncrement(){
		long i = 1000000000;
		while(true){
			i = i * 2;
			System.err.println(i);
		}
	}
}
