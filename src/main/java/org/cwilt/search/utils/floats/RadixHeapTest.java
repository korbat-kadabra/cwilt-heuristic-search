package org.cwilt.search.utils.floats;
import java.util.PriorityQueue;
import java.util.Random;

import org.junit.Test;

import org.cwilt.search.utils.basic.HeapTestItem;
public class RadixHeapTest {
	private final RadixHeap<HeapTestItem> h = new RadixHeap<HeapTestItem>();
	private final PriorityQueue<HeapTestItem> pq = new PriorityQueue<HeapTestItem>(10, new HeapTestItem.HTComparator());

	private void add(HeapTestItem l) {
		h.add(l);
		h.check();
		HeapTestItem other = (HeapTestItem) l.clone();
		pq.add(other);
	}
	
	private void clear(){
		h.clear();
		pq.clear();
	}

	private void poll() {
		HeapTestItem rh = h.poll();
		h.check();
		HeapTestItem pql = pq.poll();
		assert (rh.equals(pql));
	}

	public Random r = new Random(0);

	@Test
	public void insertTest() {
		HeapTestItem test = new HeapTestItem(10, 10);
		add(test);
	}

	@Test
	public void test2() {
		for (long i = 1l; i < 20l; i++) {
			HeapTestItem test = new HeapTestItem(i, i);
			add(test);
		}
		Long next = 1l;
		while (!h.isEmpty()) {
			HeapTestItem peeked = h.peek();
			HeapTestItem polled = h.poll();
			assert (peeked == polled);
			assert (peeked.getF() == next);
			next++;
		}
	}

	public HeapTestItem longInRange() {
		if (h.isEmpty()) {
			return new HeapTestItem(r.nextDouble() * 100, 0);
		} else {
			double min = h.peek().getF();
			return new HeapTestItem(r.nextDouble() * 100 + min, 0);

		}
	}

	public void addRemove() {
		for (int i = 0; i < 3; i++) {
			HeapTestItem value = longInRange();
			add(value);
		}
		poll();
	}

	@Test
	public void test3() {
		for (int seed = 0; seed < 1000; seed++) {
			r = new Random(seed * 100 + 200);
			for (int i = 0; i < 1000; i++) {
				addRemove();
			}
			clear();
		}

	}

}
