package org.cwilt.search.utils.floats;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Stack;

import org.cwilt.search.utils.basic.Heapable;import org.cwilt.search.utils.experimental.ArrayQueue;import org.cwilt.search.utils.experimental.BinHeapable;
public class FloatRadixHeap<Type extends BinHeapable> implements Queue<Type> {
	private static class Bucket<T extends BinHeapable> implements
			Comparable<Bucket<T>> {
		public final int start, end;
		public int minValue, maxValue;
		private final ArrayQueue<T> items;

		public void add(int value, T item) {
			assert (value >= start && value <= end);
			items.add(item);
			if (value < minValue)
				minValue = value;
			if (value > maxValue)
				maxValue = value;

		}

		public Bucket(int start, int end) {
			this.start = start;
			this.end = end;
			this.minValue = Integer.MAX_VALUE;
			this.maxValue = Integer.MIN_VALUE;
			this.items = new ArrayQueue<T>();
		}

		@Override
		public String toString() {
			return "Bucket [start=" + start + ", end=" + end + "]("
					+ (items.size()) + ")";
		}

		private void precedes(Bucket<T> b) {
			assert (this.end == b.start - 1);
		}

		public T remove() {
			assert (!items.isEmpty());
			T toReturn = items.remove();
			if (items.isEmpty()) {
				this.minValue = Integer.MAX_VALUE;
				this.maxValue = Integer.MIN_VALUE;
			}
			assert(toReturn.getHeapIndex() == Heapable.NO_POS);
			return toReturn;
		}

		public T peek() {
			assert (!items.isEmpty());
			T toReturn = items.get(items.size() - 1);
			if (items.isEmpty()) {
				this.minValue = Integer.MAX_VALUE;
				this.maxValue = Integer.MIN_VALUE;
			}
			return toReturn;
		}

		@Override
		public int compareTo(Bucket<T> arg0) {
			if (this.start > arg0.start)
				return 1;
			else if (this.start < arg0.start)
				return -1;
			else
				return 0;
		}

		public boolean canPoll() {
			return this.minValue == this.maxValue && !items.isEmpty();
		}

		public void checkBucket() {
			if (items.isEmpty()) {
				assert (this.minValue == Long.MAX_VALUE);
				assert (this.maxValue == Long.MIN_VALUE);
				return;
			}

			assert (this.minValue <= this.end);
			assert (this.maxValue >= this.start);

			for (T item : items) {
				int value = Float.floatToIntBits((float) item.getF());
				assert (value >= this.minValue);
				assert (value >= this.start);
				assert (value <= this.maxValue);
				assert (value <= this.end);
			}
		}

	}

	private final int startValue;
	private final int endValue;
	private final LinkedList<Bucket<Type>> buckets;
	private Bucket<Type> firstOccupied;

	public void check() {
		Iterator<Bucket<Type>> iter = buckets.iterator();

		Bucket<Type> currentBucket = iter.next();
		while (iter.hasNext()) {
			Bucket<Type> nextBucket = iter.next();
			currentBucket.precedes(nextBucket);
			currentBucket = nextBucket;
		}

		for (Bucket<Type> b : buckets) {
			b.checkBucket();
		}
		assert (currentBucket.end == endValue);
	}

	public FloatRadixHeap() {
		this.buckets = new LinkedList<Bucket<Type>>();
		this.startValue = Float.floatToIntBits(0);
		this.endValue = Float.floatToIntBits(Float.MAX_VALUE);

		int start = startValue;
		buckets.add(new Bucket<Type>(start, start));
		start = start + 1;
		int value = 1;
		while (start <= endValue) {
			int next = Math.min(start + value, endValue);
			buckets.add(new Bucket<Type>(start, next));
			start = next + 1;
			value *= 2;
			value = value + 1;
		}
	}

	Bucket<Type> findBucket(long value) {
		for (Bucket<Type> bucket : buckets) {
			if (value <= bucket.end && value >= bucket.start) {
				return bucket;
			}
		}
		throw new RuntimeException("Failed to find bucket for " + value);
	}

	public boolean add(Type t) {
		int value = Float.floatToIntBits((float) t.getF());

		count++;
		Bucket<Type> bucket = findBucket(value);
		bucket.add(value, t);
		if (firstOccupied == null || bucket.compareTo(firstOccupied) < 0) {
			firstOccupied = bucket;
		}
		return true;
	}

	private void readd(int value, Type t) {
		Bucket<Type> bucket = findBucket(value);
		bucket.add(value, t);
		if (firstOccupied == null || bucket.compareTo(firstOccupied) < 0) {
			firstOccupied = bucket;
		}
	}

	public Type peek() {
		Type toReturn = null;
		if (firstOccupied != null && firstOccupied.canPoll()) {
			toReturn = firstOccupied.peek();
		} else {
			fixMinBucket();
			toReturn = firstOccupied.peek();
		}
		if (firstOccupied == null || firstOccupied.items.isEmpty())
			firstOccupied = null;
		return toReturn;
	}

	public Type poll() {
		count--;
		Type toReturn = null;
		if (firstOccupied != null && firstOccupied.canPoll()) {
			toReturn = firstOccupied.remove();
		} else {
			fixMinBucket();
			toReturn = firstOccupied.remove();
		}
		if (firstOccupied == null || firstOccupied.items.isEmpty())
			firstOccupied = null;
		return toReturn;
	}

	private void fixMinBucket() {
		ListIterator<Bucket<Type>> iter = buckets.listIterator();
		Bucket<Type> nextOccupied = null;
		while (iter.hasNext()) {
			nextOccupied = iter.next();
			if (!nextOccupied.items.isEmpty()) {
				iter.remove();
				break;
			} else {
				iter.remove();
			}
		}
		assert (nextOccupied != null);
		// found the bucket to split up.
		int min = nextOccupied.minValue;
		int max = nextOccupied.end;
		nextOccupied.checkBucket();
		assert (!nextOccupied.items.isEmpty());
		assert (min < max);

		int value = 0;
		Stack<Bucket<Type>> newBuckets = new Stack<Bucket<Type>>();
		while (min <= max) {
			int next = Math.min(min + value, max);
			if (value == Long.MAX_VALUE)
				next = max;
			Bucket<Type> nextBucket = new Bucket<Type>(min, next);
			min = next + 1;
			if (value == 0)
				value = 1;
			else {
				value *= 2;
				if (value < 0) {
					value = Integer.MAX_VALUE;
				}
			}
			newBuckets.push(nextBucket);
		}
		while (!newBuckets.isEmpty()) {
			Bucket<Type> b = newBuckets.pop();
			buckets.addFirst(b);
		}
		assert (!buckets.isEmpty());
		ListIterator<Type> items = nextOccupied.items.listIterator();
		while (items.hasNext()) {
			Type nextItem = items.next();
			int nextValue = Float.floatToIntBits((float) nextItem.getF());
			this.readd(nextValue, nextItem);
		}
		firstOccupied = buckets.get(0);
	}

	private int slowSize() {
		int ct = 0;
		for (Bucket<Type> t : buckets) {
			ct += t.items.size();
		}
		assert (ct == count);
		return ct;
	}

	public int size() {
		assert (this.slowSize() == count);
		return count;
	}

	public boolean isEmpty() {
		return this.count == 0;
	}

	private int count;

	public String toString() {
		StringBuffer b = new StringBuffer();
		Iterator<Bucket<Type>> iter = buckets.iterator();
		while (iter.hasNext()) {
			b.append(iter.next());
			b.append("\n");
		}
		return b.toString();
	}

	public void clear() {
		buckets.clear();
		buckets.add(new Bucket<Type>(startValue, startValue));
		int start = startValue + 1;
		int value = 1;
		while (start <= endValue) {
			int next = Math.min(start + value, endValue);
			buckets.add(new Bucket<Type>(start, next));
			start = next + 1;
			value *= 2;
			value = value + 1;
		}
		this.firstOccupied = null;
		this.count = 0;
	}

	@Override
	public boolean addAll(Collection<? extends Type> arg0) {
		boolean added = false;
		for (Type t : arg0) {
			added = add(t);
		}
		return added;
	}

	@Override
	public boolean contains(Object arg0) {
		@SuppressWarnings("unchecked")
		Type t = (Type) arg0;
		int index = t.getHeapIndex();
		if (index == Heapable.NO_POS)
			return false;
		int value = Float.floatToIntBits((float) t.getF());
		Bucket<Type> bucket = findBucket(value);
		if (bucket == null)
			return false;
		if (bucket.items.size() <= index) {
			return false;
		}
		Type atIndex = bucket.items.get(index);
		return t.equals(atIndex);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		for (Object o : arg0) {
			if (!contains(o))
				return false;
		}
		return true;
	}

	@Override
	public Iterator<Type> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object arg0) {
		@SuppressWarnings("unchecked")
		Type t = (Type) arg0;
		int index = t.getHeapIndex();
		if (index == Heapable.NO_POS)
			return false;
		int value = Float.floatToIntBits((float) t.getF());
		Bucket<Type> bucket = findBucket(value);
		if (bucket == null)
			return false;
		if (bucket.items.size() <= index) {
			return false;
		}
		Type atIndex = bucket.items.get(index);
		if (!t.equals(atIndex))
			return false;
		return bucket.items.remove(t);
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		boolean removed = false;
		for (Object o : arg0) {
			boolean removedSomething = remove(o);
			if (removedSomething)
				removed = true;
		}
		return removed;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Type element() {
		Type toRemove = peek();
		if(toRemove == null)
			throw new NoSuchElementException();
		else
			return toRemove;
	}

	@Override
	public boolean offer(Type arg0) {
		return add(arg0);
	}

	@Override
	public Type remove() {
		Type toRemove = poll();
		if(toRemove == null)
			throw new NoSuchElementException();
		else
			return toRemove;
	}
}
