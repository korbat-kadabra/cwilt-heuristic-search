package org.cwilt.search.utils.experimental;
import java.util.Queue;
import java.util.Random;

import org.junit.Test;

import org.cwilt.search.utils.basic.MinHeap;import org.cwilt.search.utils.basic.HeapTestItem;
public class HeapTest {


	public HeapTest() {
		bh = new FasterFloatHeap<HeapTestItem>(0, 100, new HeapTestItem.HTComparator(), 10);
		mh = new MinHeap<HeapTestItem>(new HeapTestItem.HTComparator());
	}

	Random r = new Random(1777);

	Queue<HeapTestItem> bh;
	MinHeap<HeapTestItem> mh;

	@Test
	public void removeTest() {
		HeapTestItem b = new HeapTestItem(0, 0);
		bh.add(b);
		HeapTestItem out = bh.remove();
		assert (b == out);
	}

	@Test
	public void miscTest() {
		int ct = 2000;

		for (int i = 0; i < ct; i++) {
			HeapTestItem b = new HeapTestItem(r.nextDouble(), r.nextDouble());
			HeapTestItem copy = (HeapTestItem) b.clone();
			assert (bh.size() == mh.size());
			bh.add(b);
			mh.add(copy);
		}
		for (int i = 0; i < ct; i++) {
			HeapTestItem mhNext = mh.poll();
			HeapTestItem bhNext = bh.poll();
			assert (bh.size() == mh.size());

			assert (bhNext != null);
			assert (mhNext != null);
			if (mhNext.getF() != bhNext.getF()) {
				System.err.println("Failed on index " + i);
			}
			assert (mhNext.getF() == bhNext.getF());
		}

	}

	@Test
	public void longRemoveTest() {
		int ct = 2000;

		for (int i = 0; i < ct; i++) {
			HeapTestItem b = new HeapTestItem(r.nextDouble(), r.nextDouble());
			HeapTestItem copy = (HeapTestItem) b.clone();
			bh.add(b);
			mh.add(copy);
		}
		for (int i = 0; i < ct; i++) {
			HeapTestItem mhNext = mh.poll();
			HeapTestItem bhNext = bh.poll();

			assert (bh.size() == mh.size());

			assert (bhNext != null);
			assert (mhNext != null);
			assert (mhNext.getF() == bhNext.getF());
		}

	}

	@Test
	public void removeObjectTest() {
		HeapTestItem b = new HeapTestItem(0, 0);
		bh.add(b);
		boolean removeNoReturn = false;
		try {
			removeNoReturn = bh.remove(new Object());
			assert (false);
		} catch (ClassCastException cce) {

		}
		assert (!removeNoReturn);
		removeNoReturn = bh.remove(new HeapTestItem(1, 1));
		assert (!removeNoReturn);

		boolean removeReturn = bh.remove(b);
		assert (removeReturn);
		int bs = bh.size();
		int ms = mh.size();
		assert (ms == bs);

	}

	@Test
	public void insertFindTest() {
		HeapTestItem b = new HeapTestItem(0, 0);
		bh.add(b);
		boolean contained = bh.contains(b);
		assert (contained);
	}

	@Test
	public void insertTest() {
		for (int i = 0; i < 1000; i++) {
			HeapTestItem b = new HeapTestItem(r.nextDouble(), r.nextDouble());
			bh.add(b);
			assert (bh.contains(b));
		}
	}

}
