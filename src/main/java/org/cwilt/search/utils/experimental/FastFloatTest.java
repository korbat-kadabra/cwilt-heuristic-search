package org.cwilt.search.utils.experimental;
//import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;

import org.junit.Test;


public class FastFloatTest extends BinHeapTest{
	@Test
	public void bucketComparatorTest(){
		FastFloatHeap.Bucket b1 = new FastFloatHeap.Bucket(0, 1, 0);
		FastFloatHeap.Bucket b2 = new FastFloatHeap.Bucket(1, 1, 1);
		FastFloatHeap.Bucket b3 = new FastFloatHeap.Bucket(1, 2, 2);
		FastFloatHeap.Bucket b4 = new FastFloatHeap.Bucket(2, 3, 3);
		
		ConcurrentSkipListSet<FastFloatHeap.Bucket> tsl = new ConcurrentSkipListSet<FastFloatHeap.Bucket>();
		tsl.add(b1);
		tsl.add(b2);
		tsl.add(b3);
		tsl.add(b4);
		
		assert(null == (tsl.floor(new FastFloatHeap.Bucket(-1, -1, 0))));
		assert(FastFloatHeap.validBucketChain(tsl));
//		System.err.println(tsl.ceiling(new FastFloatHeap.Bucket(0, 0, 0)));
//		System.err.println(tsl.ceiling(new FastFloatHeap.Bucket(1, 1, 0)));
//		System.err.println(tsl.ceiling(new FastFloatHeap.Bucket(1.5, 1.5, 0)));
//		System.err.println(tsl.ceiling(new FastFloatHeap.Bucket(2, 2, 0)));
//		System.err.println(tsl.ceiling(new FastFloatHeap.Bucket(3, 3, 0)));
//		
//		System.err.println("printing in order");
//		Iterator<FastFloatHeap.Bucket> sli = tsl.iterator();
//		while(sli.hasNext()){
//			System.err.println(sli.next());
//		}
	}
	

	@Test
	public void insertTest(){
		BHT first = new BHT(0, 0);
		bh.add(first);
		BHT peek = bh.peek();
		BHT poll = bh.poll();
		assert(peek == poll);
		assert(peek == first);
	}
	
	public FastFloatTest (){
		bh = new FastFloatHeap<BHT>(new BHT.FGComparator(), 0, 1, 1000);
	}

}
