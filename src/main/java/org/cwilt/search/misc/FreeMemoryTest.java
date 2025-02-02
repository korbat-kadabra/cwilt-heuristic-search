package org.cwilt.search.misc;
import java.util.ArrayList;
import java.util.Random;

public class FreeMemoryTest {
	private final ArrayList<Double> l;
	private final Random r;
	
	public FreeMemoryTest(){
		this.r = new Random();
		this.l = new ArrayList<Double>();
	}
	
	public static boolean memoryCheck() {
		double freeMem = Runtime.getRuntime().freeMemory();
		double totalMem = Runtime.getRuntime().totalMemory();
		double fptm = totalMem * 0.05;
		boolean toReturn = fptm > freeMem;
		return toReturn;
	}
	
	public void freeMemWorkout(int max){
		for(int i = 0; i < max; i++){
			memoryCheck();
			l.add(r.nextDouble());
		}
	}
	public void workout(int max){
		for(int i = 0; i < max; i++){
			l.add(r.nextDouble());
		}
	}
	
	public static void main(String[] args){
		FreeMemoryTest f = new FreeMemoryTest();
		
		int count = Integer.parseInt(args[1]);
		
		long startTime = System.currentTimeMillis();
		if(args[0].equals("f")){
			f.freeMemWorkout(count);
		} else {
			f.workout(count);
		}
		long endTime = System.currentTimeMillis();
		System.out.println(endTime - startTime);
	}
}
