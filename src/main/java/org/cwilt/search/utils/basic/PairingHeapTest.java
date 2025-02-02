package org.cwilt.search.utils.basic;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

import org.junit.Test;

import org.cwilt.search.utils.basic.PairingHeap.Position;
public class PairingHeapTest {

    private static final class IntComparable implements Comparator<IntTest>{
   	 
        @Override
        public int compare(IntTest o1, IntTest o2) {
            return (o1.i>o2.i ? -1 : (o1.i==o2.i ? 0 : 1));
        }
    }
    
    private static final class IntTest implements PairingHeapable<IntTest>{
    	public final Integer i;
    	private Position<IntTest> pos;
    	
    	public IntTest(Integer i){
    		this.pos = null;
    		this.i = i;
    	}

		@Override
		public void setPosition(Position<IntTest> o) {
			this.pos = o;
		}

		@Override
		public Position<IntTest> getPosition() {
			return pos;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((i == null) ? 0 : i.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IntTest other = (IntTest) obj;
			if (i == null) {
				if (other.i != null)
					return false;
			} else if (!i.equals(other.i))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "IntTest [i=" + i + "]";
		}
		
    }
    
    private static final int NITEMS = 10000;
    private final Random r = new Random(10);
    
    
    @Test
    public void basicTest(){
    	
    	
    	PairingHeap<IntTest> ph = new PairingHeap<IntTest>(new IntComparable());
    	PriorityQueue<IntTest> q = new PriorityQueue<IntTest>(NITEMS, new IntComparable());
    	
    	
    	for(int i = 0; i < NITEMS; i++){
    		int next = r.nextInt();
    		q.add(new IntTest(next));
    		ph.add(new IntTest(next));
    	}
    	while(!q.isEmpty()){
    		IntTest qNext = q.poll();
    		IntTest pNext = ph.poll();
    		assert(qNext != null);
    		assert(pNext != null);
    		if(!qNext.equals(pNext)){
    			System.err.println("pairing " + pNext + " pq " + qNext);
    			assert(false);
    		}
    	}
    	assert(q.isEmpty());
    	assert(ph.isEmpty());
    }

    @Test
    public void removeTest(){
    	
    	
    	PairingHeap<IntTest> ph = new PairingHeap<IntTest>(new IntComparable());
    	PriorityQueue<IntTest> pq = new PriorityQueue<IntTest>(NITEMS, new IntComparable());
    	ArrayList<IntTest> toRemoveQueue = new ArrayList<IntTest>();
    	ArrayList<IntTest> toRemovePairing = new ArrayList<IntTest>();
    	
    	for(int i = 0; i < NITEMS; i++){
    		int next = r.nextInt();
    		IntTest p = new IntTest(next);
    		IntTest q = new IntTest(next);
    		pq.add(p);
    		ph.add(q);
    	}
    	
    	for(IntTest i : toRemovePairing){
    		boolean removed = ph.remove(i);
    		assert(removed);
    	}
    	for(IntTest i : toRemoveQueue){
    		boolean removed = pq.remove(i);
    		assert(removed);
    	}
    	
    	while(!pq.isEmpty()){
    		IntTest qNext = pq.poll();
    		IntTest pNext = ph.poll();
    		assert(qNext != null);
    		assert(pNext != null);
    		if(!qNext.equals(pNext)){
    			System.err.println("pairing " + pNext + " pq " + qNext);
    			assert(false);
    		}
    	}
    	assert(pq.isEmpty());
    	assert(ph.isEmpty());
    }

}
