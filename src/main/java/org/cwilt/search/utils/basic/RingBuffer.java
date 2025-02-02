/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.utils.basic;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;


public class RingBuffer<Item extends Heapable> extends
		java.util.AbstractQueue<Item> implements Queue<Item>{
	private Object[] a; // queue elements
	private int n = 0; // number of elements on queue
	private int first = 0; // index of first element of queue
	private int last = 0; // index of next available slot

	// cast needed since no generic array creation in Java
	public RingBuffer(int capacity) {
		a = new Object[capacity];
	}

	@Override
	public boolean isEmpty() {
		return n == 0;
	}

	@Override
	public int size() {
		return n;
	}

	@Override
	public Iterator<Item> iterator() {
		return new RingBufferIterator();
	}

	// an iterator, doesn't implement remove() since it's optional
	private class RingBufferIterator implements Iterator<Item> {
		private int i = 0;

		public boolean hasNext() {
			return i < n;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		public Item next() {
			if (!hasNext())
				throw new NoSuchElementException();
			Item item = (Item) a[(i + first) % a.length];
			i++;
			return item;
		}
	}

	// a test client
	public static void main(String[] args) {
		RingBuffer<HeapTestItem> ring = new RingBuffer<HeapTestItem>(3);
		ring.addWithReplace(new HeapTestItem(10, 10));
		ring.addWithReplace(new HeapTestItem(11, 11));
		ring.addWithReplace(new HeapTestItem(12, 0));
		ring.addWithReplace(new HeapTestItem(13, 0));
		ring.addWithReplace(new HeapTestItem(14, 0));
		ring.replaceAt(new HeapTestItem(22, 0), 1);
		ring.poll();

		for (HeapTestItem s : ring) {
			System.out.println(s);
		}

		System.out.println();

		while (!ring.isEmpty()) {
			System.out.println(ring.poll());
		}

	}

	public Item addWithReplace(Item item) {
		if (n == a.length) {
			Item toReturn = poll();
			offer(item);
			return toReturn;
		} else {
			offer(item);
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public void replaceAt(Item i, int index) {
		Item oldItem = (Item) a[index];
		assert (oldItem != null);
		oldItem.setHeapIndex(Heapable.NO_POS);
		a[index] = i;
		i.setHeapIndex(index);
	}

	@Override
	public boolean offer(Item item) {
		if (n == a.length)
			return false;
		a[last] = item;
		last = (last + 1) % a.length; // wrap-around
		n++;
		item.setHeapIndex(last);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Item peek() {
		return (Item) a[first];
	}

	@SuppressWarnings("unchecked")
	@Override
	public Item element() {
		if (isEmpty())
			throw new NoSuchElementException();
		Item i = (Item) a[first];
		i.setHeapIndex(Heapable.NO_POS);
		return i;
	}

	@Override
	public Item poll() {
		if (isEmpty())
			return null;
		return remove();
	}

	@SuppressWarnings("unchecked")
	public Item remove() {
		if (isEmpty())
			throw new NoSuchElementException();
		Item item = (Item) a[first];
		a[first] = null; // to help with garbage collection
		n--;
		first = (first + 1) % a.length; // wrap-around
		item.setHeapIndex(Heapable.NO_POS);
		return item;
	}

	@Override
	public void clear() {
		for (int i = 0; i < a.length; i++) {
			a[i] = null;
		}
		first = 0;
		last = 0;
		n = 0;
	}
}
