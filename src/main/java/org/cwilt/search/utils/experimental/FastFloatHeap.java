package org.cwilt.search.utils.experimental;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListSet;

import org.cwilt.search.utils.basic.FloatHistogram;import org.cwilt.search.utils.basic.Heapable;import org.cwilt.search.utils.basic.MinHeap;
public class FastFloatHeap<T extends BinHeapable> implements Queue<T> {

	public static boolean validBucketChain(ConcurrentSkipListSet<Bucket> b) {
		Iterator<Bucket> i = b.iterator();

		Bucket first = i.next();
		double end = first.max;
		while (i.hasNext()) {
			Bucket current = i.next();
			assert (current.min == end);
			end = current.max;

			Iterator<Bucket> i2 = b.iterator();
			// make sure all buckets have their own DISTINCT index
			while (i2.hasNext()) {
				Bucket c = i2.next();
				if (c == current)
					continue;
				if (c.index == current.index) {
					System.err.println("Duplicate indexes:");
					System.err.println(c);
					System.err.println(current);
					assert (c.index != current.index);
				}
			}
		}

		return true;
	}

	private void printBuckets() {
		System.err.println("printing in order");
		Iterator<FastFloatHeap.Bucket> sli = buckets.iterator();
		while (sli.hasNext()) {
			Bucket n = sli.next();
			System.err.println(n);
		}

	}

	private void checkBucket(Bucket previous, Bucket current) {
		Queue<T> next = backbone.get(current.index);
		assert (backbone != null);
		if (next == null) {
			System.err.println(current);
			printBuckets();
		}
		assert (next != null);
		Iterator<T> nodeIter = next.iterator();
		int currentIX = 0;
		while (nodeIter.hasNext()) {
			T nextNode = nodeIter.next();
			double f = nextNode.getF();
			assert (nextNode.getHeapIndex() == currentIX);
			currentIX++;
			if (!current.validF(previous, f)) {
				System.err.println(current.min);
				System.err.println(current.max);
				System.err.println(f);
				System.err.println(this.size);
				printBuckets();
				assert (current.validF(previous, f));
			}
		}
	}

	public boolean check() {
		assert (FastFloatHeap.validBucketChain(buckets));
		Iterator<Bucket> biter = buckets.iterator();
		checkBucket(null, biter.next());
		Iterator<Bucket> biter2 = buckets.iterator();

		while (biter.hasNext()) {
			checkBucket(biter2.next(), biter.next());
		}
		return true;
	}

	public static class Bucket implements Comparable<Bucket> {
		public String toString() {
			StringBuffer b = new StringBuffer();
			b.append("(");
			b.append(min);
			b.append(" ");
			b.append(max);
			b.append(" @ ");
			b.append(index);
			b.append(")");
			return b.toString();
		}

		public boolean validF(Bucket previous, double f) {
			if (previous == null)
				return validF(f);
			else if (previous.min == previous.max) {
				return f < max && f > min;
			} else
				return validF(f);
		}

		public boolean validF(double f) {
			if (min == max)
				return f == min;
			else {
				return f < max && f >= min;
			}
		}

		private final double min;
		private final double max;
		public final int index;

		public Bucket(double min, double max, int index) {
			this.index = index;
			this.max = max;
			this.min = min;
			assert (min <= max);
		}

		@Override
		public int compareTo(Bucket arg0) {
			if (this.max < arg0.max)
				return -1;
			else if (this.max > arg0.max) {
				return 1;
			} else {
				if (this.min < arg0.min)
					return -1;
				else if (this.min > arg0.min)
					return 1;
				else
					return 0;
			}
		}

		public boolean canSplit() {
			return this.min != this.max;
		}
	}

	private static final int MINSPLIT = 500;

	private final ConcurrentSkipListSet<Bucket> buckets;

	private final ArrayList<Queue<T>> backbone;
	private final Comparator<T> secondary;

	private void reset() {
		this.backbone.clear();
		this.backbone.add(new MinHeap<T>(this.secondary));
		Bucket b1 = new Bucket(expectedStart, expectedEnd, 0);
		buckets.add(b1);
	}

	private final double expectedStart;
	private final double expectedEnd;
	private final int BUCKETSZ;

	public FastFloatHeap(Comparator<T> secondary, double expectedStart,
			double expectedEnd, int split) {
		this.BUCKETSZ = split;
		this.secondary = secondary;
		this.expectedEnd = expectedEnd;
		this.expectedStart = expectedStart;
		this.backbone = new ArrayList<Queue<T>>();
		this.backbone.add(new MinHeap<T>(this.secondary));
		this.buckets = new ConcurrentSkipListSet<Bucket>();
		Bucket b1 = new Bucket(expectedStart, expectedEnd, 0);
		buckets.add(b1);
	}

	private Bucket getBucketIndex(double value) {
		Bucket head = buckets.first();
		if(head.validF(value))
			return head;
		
		Bucket comp = new Bucket(value, value, 0);
		
		Bucket next = buckets.ceiling(comp);
		if (next != null && next.validF(value)) {
			// this bucket was the one we were looking for
			assert (next.validF(value));
			return next;
		} else if (next != null && next.min != next.max) {
			// extend the size of the current head bucket
			Bucket newHead = new Bucket(value * .5, next.max, 0);
			assert (newHead.validF(value));
			boolean r1 = buckets.remove(next);
			boolean a1 = buckets.add(newHead);
			assert (r1);
			assert (a1);
			return newHead;
		} else if (next != null) {
			// head bucket is a singleton bucket, and this doesn't belong in
			// that bucket. Need to add a new real bucket for this guy.
			assert (check());
			Bucket newHead = new Bucket(value * .5, next.min, 0);
			Bucket oldHead = new Bucket(next.min, next.max, backbone.size());
			backbone.add(backbone.get(0));
			backbone.set(0, new MinHeap<T>(this.secondary));
			boolean r = buckets.remove(next);
			assert (r);
			boolean a1 = buckets.add(newHead);
			boolean a2 = buckets.add(oldHead);
			assert (a1);
			assert (a2);
			assert (check());
			return newHead;
		}

		Bucket lastBucket = buckets.last();
		Queue<T> newQueue = new ArrayQueue<T>();

		Bucket nextBucket = new Bucket(lastBucket.max, value + 1,
				backbone.size());
		backbone.add(newQueue);
		buckets.add(nextBucket);
		assert (nextBucket.validF(value));
		return nextBucket;
	}

	@Override
	public boolean add(T arg0) {
		assert (check());
		size++;
		double value = arg0.getF();

		Bucket bucket = getBucketIndex(value);
		assert (bucket.max > value || bucket.min == bucket.max);
		assert (bucket.validF(value));

		Queue<T> bb = backbone.get(bucket.index);
		// this fits in the bucket that was retrieved
		bb.add(arg0);
		// if (bucket.index != 0) {
		// arg0.setHeapIndex(bb.size() - 1);
		// }

		// if this made the first bucket too big, split it. Otherwise, don't
		// do anything.

		if (bb.size() > BUCKETSZ && bucket.canSplit()) {
			// sample 10% of each bucket
			splitBucket(bucket, (int) (BUCKETSZ * 0.1), 0);
		}
		assert (check());
		return true;
	}

	Random r = new Random(0);

	private void histogramSplit(Bucket b, double[] values) {

		ArrayList<T> bucket = null;

		if (backbone.get(b.index) instanceof ArrayQueue) {
			bucket = (ArrayQueue<T>) backbone.get(b.index);
		} else {
			MinHeap<T> h = (MinHeap<T>) backbone.get(b.index);
			bucket = h.getBackbone();
		}

		assert (bucket != null);
		ArrayQueue<T> left = new ArrayQueue<T>();
		ArrayQueue<T> right = new ArrayQueue<T>();
		ArrayQueue<T> center = new ArrayQueue<T>();
		// this bucket had better have different min and max values
		assert (b.min < b.max);

		FloatHistogram h = new FloatHistogram(values);

		double mid = h.mostCommon();

		assert (mid != b.max);
		for (T item : bucket) {
			double currentF = item.getF();
			if (center != null && currentF == mid) {
				center.add(item);
			} else if (currentF < mid) {
				left.add(item);
			} else {
				right.add(item);
			}
		}

		if (center != null && left.size() == 0 && mid == b.min) {
			left = center;
			center = null;
		}

		if (b.index == 0) {
			backbone.set(0, new MinHeap<T>(secondary, left));
		} else {
			backbone.set(b.index, left);
		}
		backbone.add(right);
		boolean r = buckets.remove(b);
		assert (r);

		Bucket leftBucket = new Bucket(b.min, mid, b.index);
		Bucket rightBucket = new Bucket(mid, b.max, backbone.size() - 1);
		assert (leftBucket.index != rightBucket.index);
		if (center != null) {
			backbone.add(center);
			Bucket centerBucket = new Bucket(mid, mid, backbone.size() - 1);
			boolean a = buckets.add(centerBucket);

			assert (leftBucket.index != centerBucket.index);
			assert (leftBucket.index != centerBucket.index);

			assert (a);
		}
		buckets.add(leftBucket);
		buckets.add(rightBucket);

		assert (check());
	}

	/**
	 * Splits the bucket b into 2 buckets splitting based upon a sample
	 * 
	 * @param b
	 */
	private void splitBucket(Bucket b, int nsamples, int tries) {

		ArrayList<T> bucket = null;

		if (backbone.get(b.index) instanceof ArrayQueue) {
			bucket = (ArrayQueue<T>) backbone.get(b.index);
		} else {
			MinHeap<T> h = (MinHeap<T>) backbone.get(b.index);
			bucket = h.getBackbone();
		}

		assert (bucket != null);
		ArrayQueue<T> left = new ArrayQueue<T>();
		ArrayQueue<T> right = new ArrayQueue<T>();
		ArrayQueue<T> center = null;
		// this bucket had better have different min and max values
		assert (b.min < b.max);

		double[] values = new double[nsamples];

		boolean allSame = true;
		for (int i = 0; i < nsamples; i++) {
			values[i] = bucket.get(r.nextInt(BUCKETSZ)).getF();
			if (i != 0) {
				if (values[i] != values[i - 1])
					allSame = false;
			}
		}

		double mid;
		if (allSame) {
			center = new ArrayQueue<T>();
			mid = values[0];
		} else {
			Arrays.sort(values);
			mid = values[nsamples / 2];
		}

		assert (mid != b.max);
		for (T item : bucket) {
			double currentF = item.getF();
			if (center != null && currentF == mid) {
				center.add(item);
			} else if (currentF < mid) {
				left.add(item);
			} else {
				right.add(item);
			}
		}

		if (left.size() > BUCKETSZ - MINSPLIT
				|| right.size() > BUCKETSZ - MINSPLIT) {
			// looks like the split failed?
			// check if one of the endpoints is the problem
			center = new ArrayQueue<T>();
			if (values[0] == mid) {
				left.clear();
				right.clear();
				for (T item : bucket) {
					double currentF = item.getF();
					if (center != null && currentF == mid) {
						center.add(item);
					} else if (currentF < mid) {
						left.add(item);
					} else {
						right.add(item);
					}
				}
			} else if (values[nsamples - 1] == mid) {
				left.clear();
				right.clear();
				for (T item : bucket) {
					double currentF = item.getF();
					if (center != null && currentF == mid) {
						center.add(item);
					} else if (currentF < mid) {
						left.add(item);
					} else {
						right.add(item);
					}
				}
			} else {
				histogramSplit(b, values);
				return;
			}
		}

		if (center != null && left.size() == 0 && mid == b.min) {
			left = center;
			center = null;
		}

		if (b.index == 0) {
			backbone.set(0, new MinHeap<T>(secondary, left));
		} else {
			backbone.set(b.index, left);
		}
		backbone.add(right);
		boolean r = buckets.remove(b);
		assert (r);
		Bucket leftBucket = new Bucket(b.min, mid, b.index);
		Bucket rightBucket = new Bucket(mid, b.max, backbone.size() - 1);
		assert (leftBucket.index != rightBucket.index);
		if (center != null) {
			backbone.add(center);
			Bucket centerBucket = new Bucket(mid, mid, backbone.size() - 1);
			boolean a = buckets.add(centerBucket);

			assert (leftBucket.index != centerBucket.index);
			assert (leftBucket.index != centerBucket.index);

			assert (a);
		}
		buckets.add(leftBucket);
		buckets.add(rightBucket);

		assert (check());
	}

	private void removeEmptyHead() {
		Bucket oldFirst = buckets.pollFirst();
		if (buckets.isEmpty()) {
			// whole thing is now empty
			this.reset();
		} else {
			Bucket newFirst = buckets.first();
			backbone.set(0, backbone.get(newFirst.index));
			backbone.set(newFirst.index, null);
			buckets.remove(oldFirst);
			buckets.remove(newFirst);
			Bucket newFirstCopy = new Bucket(newFirst.min, newFirst.max, 0);
			buckets.add(newFirstCopy);

			if (backbone.get(0) instanceof ArrayQueue) {
				ArrayQueue<T> zero = (ArrayQueue<T>) backbone.get(0);
				backbone.set(0, new MinHeap<T>(secondary, zero));
			}
		}

	}

	@Override
	public T poll() {
		assert (check());
		if (size == 0) {
			return null;
		}
		size--;
		Queue<T> firstQueue = backbone.get(0);
		while (firstQueue.isEmpty()) {
			removeEmptyHead();
			firstQueue = backbone.get(0);
		}
		T next = firstQueue.poll();
		if (firstQueue.isEmpty()) {
			removeEmptyHead();
		}
		assert (check());
		assert (next.getHeapIndex() == Heapable.NO_POS);
		return next;
	}

	@Override
	public boolean remove(Object a) {
		assert (check());
		@SuppressWarnings("unchecked")
		T arg0 = (T) a;
		if (arg0.getHeapIndex() == Heapable.NO_POS) {
			return false;
		}
		double value = arg0.getF();
		Bucket bucket = getBucketIndex(value);
		Queue<T> bb = backbone.get(bucket.index);

		boolean removed = bb.remove(arg0);

		if (removed)
			size--;
		if (bucket.index != 0 && removed) {
			arg0.setHeapIndex(Heapable.NO_POS);
		}
		assert (arg0.getHeapIndex() == Heapable.NO_POS);
		assert (check());
		return removed;
	}

	@Override
	public boolean contains(Object a) {
		@SuppressWarnings("unchecked")
		T arg0 = (T) a;
		double value = arg0.getF();
		Bucket bucket = getBucketIndex(value);
		Queue<T> bb = backbone.get(bucket.index);
		return bb.contains(arg0);
	}

	@Override
	public void clear() {
		for (int i = backbone.size() - 1; i >= 0; i--) {
			if (backbone.get(i) != null) {
				backbone.get(i).clear();
			}
			backbone.remove(i);
		}
	}

	@Override
	public T peek() {
		if (size == 0)
			return null;
		else
			return backbone.get(0).peek();
	}

	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		boolean mod = false;
		for (T item : arg0) {
			boolean m = this.add(item);
			if (m)
				mod = true;
		}
		return mod;
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		for (Object item : arg0) {
			boolean m = this.contains(item);
			if (!m)
				return false;
		}
		return true;
	}

	private int size;

	@Override
	public boolean isEmpty() {
		return this.size == 0;
	}

	@Override
	public Iterator<T> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		boolean mod = false;
		for (Object item : arg0) {
			boolean m = this.remove(item);
			if (m)
				mod = true;
		}
		return mod;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <Type> Type[] toArray(Type[] arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T element() {
		T toReturn = peek();
		if (toReturn == null)
			throw new NoSuchElementException();
		return toReturn;
	}

	@Override
	public boolean offer(T arg0) {
		return add(arg0);
	}

	@Override
	public T remove() {
		T toReturn = poll();
		if (toReturn == null) {
			throw new NoSuchElementException();
		}
		return toReturn;
	}

}
