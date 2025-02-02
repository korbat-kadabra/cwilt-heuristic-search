package org.cwilt.search.utils.experimental;
import java.util.Comparator;
import java.util.Random;

public class RandomizedMinHeap<T extends org.cwilt.search.utils.basic.Heapable> extends org.cwilt.search.utils.basic.MinHeap<T>{

	private final Random r;
	private final double pRandom;
	
	public RandomizedMinHeap(Comparator<T> c, int seed, double pRandom) {
		super(c);
		if(pRandom < 0 || pRandom > 1){
			throw new IllegalArgumentException("rRandom must be between 0 and 1");
		}
		
		this.r = new Random(seed);
		this.pRandom = pRandom;
	}
	
	@Override
	public T poll(){
		double v = r.nextDouble();
		if(v < pRandom){
			int randomIX = r.nextInt(super.heap.size());
			return removeAt(randomIX);
		} else {
			return super.poll();
		}
	}
}
