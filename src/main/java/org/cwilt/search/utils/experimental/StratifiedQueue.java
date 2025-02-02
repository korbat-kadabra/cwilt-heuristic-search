package org.cwilt.search.utils.experimental;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import org.cwilt.search.utils.basic.HeapTestItem;import org.cwilt.search.utils.basic.Heapable;import org.cwilt.search.utils.basic.MinMaxHeap;
public class StratifiedQueue<T extends Heapable> {
	private final Comparator<T> comparator;
	private final HeapItemComparator heapComparator;
	private final ArrayList<HeapItem<T>> heap;
	private final ArrayList<Integer> positions;
	private final int levelCapacity;

	private static class HeapItem<Type extends Heapable> {
		public String toString(){
			return Integer.toString(index);
		}
		public MinMaxHeap<Type> heap;
		private int capacity;
		private final int index;

		public HeapItem(int index, int maxSize, Comparator<Type> c) {
			this.capacity = maxSize;
			this.heap = new MinMaxHeap<Type>(c);
			this.index = index;
		}

		public void addItem(int index, Type i) {
			if (heap.size() < capacity)
				heap.insert(i);
			else {
				Type worst = heap.peekLargest();
				if (heap.c.compare(i, worst) < 0) {
					heap.popLargest();
					heap.insert(i);
				}
			}
		}

		public Type extract() {
			return heap.pop();
		}
	}

	private class HeapItemComparator implements Comparator<HeapItem<T>> {
		@Override
		public int compare(HeapItem<T> arg0, HeapItem<T> arg1) {
			T first = arg0.heap.peekSmallest();
			T second = arg1.heap.peekSmallest();
			if (first == null && second == null)
				return 0;
			else if (first == null)
				return 1;
			else if (second == null)
				return -1;
			else
				return comparator.compare(first, second);
		}
	}

	public T pop() {
		if (heap.size() == 0)
			return null;
		else
			return heap.get(0).extract();
	}
	private void ensureSize(int index) {
		if (heap.size() <= index) {
			for (int i = heap.size(); i <= index; i++) {
				heap.add(new HeapItem<T>(i, levelCapacity, comparator));
				positions.add(i);
			}
		}
	}


	public void insert(int index, T item) {
		ensureSize(index);
		assert(heap.size() == positions.size());
		if(positions.size() <= index){
			System.err.println("size " + positions.size());
			System.err.println("requested " + index);
		}
		int heapIndex = positions.get(index);
		HeapItem<T> h = heap.get(heapIndex);
		h.addItem(heapIndex, item);
		bubbleUp(heapIndex);
	}

	private static int parent(int ix) {
		return (ix - 1) / 2;
	}

	private void bubbleUp(int ix) {
		if (ix == 0)
			return;
		int parent = parent(ix);
		HeapItem<T> currentHeap = heap.get(ix);
		HeapItem<T> parentHeap = heap.get(parent);
		if (heapComparator.compare(currentHeap, parentHeap) < 0) {
			swap(ix, parent);
			bubbleUp(parent);
		}
	}

	private void swap(int ix1, int ix2) {
		HeapItem<T> h1 = heap.get(ix1);
		HeapItem<T> h2 = heap.get(ix2);
		heap.set(ix1, h2);
		heap.set(ix2, h1);
		positions.set(h2.index, ix1);
		positions.set(h1.index, ix2);
	}

	public StratifiedQueue(Comparator<T> comparator, int levelCapacity) {
		this.levelCapacity = levelCapacity;
		this.comparator = comparator;
		this.heapComparator = new HeapItemComparator();
		this.heap = new ArrayList<HeapItem<T>>();
		this.positions = new ArrayList<Integer>();
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < heap.size(); i++) {
			b.append("level ");
			b.append(i);
			b.append("\n");
			b.append(heap.get(i).heap.toString());
			b.append("\n");
		}
		return b.toString();
	}

	public static void main(String[] args) {
		StratifiedQueue<HeapTestItem> q = new StratifiedQueue<HeapTestItem>(
				new HeapTestItem.HTComparator(), 5);
		Random r = new Random();
		for(int i = 0; i < 10000; i++){
			q.insert(r.nextInt(10), new HeapTestItem(r.nextDouble(), r.nextDouble()));
		}
		System.err.println(q);
		
		System.err.println(q.pop());
		System.err.println(q);
	}
}
