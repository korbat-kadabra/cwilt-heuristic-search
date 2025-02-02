package org.cwilt.search.utils.experimental;
import java.util.Random;

import org.junit.Test;

import org.cwilt.search.utils.basic.HeapTestItem;
public class FasterFloatHeapTest {
	private final FasterFloatHeap<HeapTestItem> heap = new FasterFloatHeap<HeapTestItem>(0, MAX, new HeapTestItem.HTComparator(), 10);
	private final Random r = new Random(10);
	private static final double MAX = 50;
	private static final int COUNT = 10000;
	
	private static final int POINTERSTACK = 5;
	
	public int expIntTest(Random r){
		int max = (1 << (POINTERSTACK - 1)) - 1;
		int start = r.nextInt(max);
		
		int returning = (POINTERSTACK - 1) - (32 - Integer.numberOfLeadingZeros(start));
		assert(returning >= 0);
		assert(returning <= (POINTERSTACK - 1));
		return returning;
	}
	
	@Test 
	public void intTest(){
		Random r = new Random(0);
		int[] histogram = new int[POINTERSTACK];
		
		for(int i = 0; i < 10000; i++){
			int value = (expIntTest(r));
			histogram[value] ++;;
		}
		for(int i = 0; i < POINTERSTACK; i++){
			System.err.println(i + " = " + histogram[i]);
		}
	}
	
	//@Test
	public void testInt(){
		for(int i = 0; i < COUNT; i++){
			int v1 = r.nextInt(50);
			int v2 = r.nextInt(50);
			
			
			heap.add(new HeapTestItem(v1, v2));
			heap.check();
		}
		for(int i = 0; i < COUNT; i++){
			//System.err.println("popping " + i);
			heap.poll();
			heap.check();
		}
	}
	
	//@Test
	public void testFloat(){
		for(int i = 0; i < COUNT; i++){
			
			heap.add(new HeapTestItem(r, MAX));
			heap.check();
		}
		for(int i = 0; i < COUNT; i++){
			//System.err.println("popping " + i);
			heap.poll();
			heap.check();
		}
	}
}
