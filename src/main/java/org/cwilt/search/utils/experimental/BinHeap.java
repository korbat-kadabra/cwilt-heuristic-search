package org.cwilt.search.utils.experimental;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.cwilt.search.utils.basic.Heapable;
public class BinHeap<T extends BinHeapable> implements BinHeapable, Queue<T> {

	private abstract static class MantissaBucket {
		public abstract int slowSize();

		public int firstOpen;

		public void print() {
			System.err.println("index " + index);
			if (parent != null)
				parent.print();
		}

		/**
		 * Empties the bucket at index bucketID
		 * 
		 * @param bucketID
		 *            Bucket that should be emptied
		 */
		public void emptyBucket(int bucketID) {
			assert (getChildren()[bucketID].isEmpty());
			getChildren()[bucketID] = null;
			assert (this.firstOpen != -1);
			if (this.firstOpen >= bucketID) {
				this.firstOpen = -1;
				for (int i = 0; i < getChildren().length; i++) {
					if (getChildren()[i] != null) {
						this.firstOpen = i;
						break;
					}
				}
			}
		}

		protected abstract boolean checkChildren();

		public boolean check(int ix) {
			if (this.index != ix) {
				System.err.println("Bucket doesn't match index " + index
						+ " ix " + ix);
				parent.print();
				return false;
			}
			return checkChildren();
		}

		protected MantissaBucket(MantissaBucket parent, int index) {
			this.parent = parent;
			this.index = index;
			this.firstOpen = -1;
		}

		public final MantissaBucket parent;
		public final int index;

		public abstract MantissaBucket[] getChildren();

		public abstract boolean isEmpty();
	}

	private static class MiddleMantissaBucket extends MantissaBucket {

		public int slowSize() {
			int s = 0;

			for (int i = 0; i < BUCKET_CT; i++) {
				if (children[i] != null) {
					s += children[i].slowSize();
				}
			}
			assert (s != 0);
			return s;
		}

		protected boolean checkChildren() {
			MantissaBucket[] children = getChildren();

			assert (firstOpen != -1);

			for (int i = 0; i < children.length; i++) {
				if (i < firstOpen) {
					assert (children[i] == null);
				}

				if (children[i] != null) {
					boolean passes = children[i].check(i);
					if (!passes)
						return false;
					if (children[i].isEmpty()) {
						System.err.println("child is empty");
						children[i].print();
						return false;
					}
				}
			}
			return true;
		}

		protected MantissaBucket[] children;
		public static final int BUCKET_CT = 256;

		public MiddleMantissaBucket(MantissaBucket parent, int index) {
			super(parent, index);
			children = new MantissaBucket[BUCKET_CT];
		}

		public boolean isEmpty() {
			for (int i = 0; i < BUCKET_CT; i++) {
				if (children[i] != null) {
					assert (firstOpen == i);
					return false;
				}
			}
			assert (firstOpen == -1);
			return true;
		}

		@Override
		public MantissaBucket[] getChildren() {
			return children;
		}
	}

	public static class EndMantissaBucket<Type extends BinHeapable> extends
			MantissaBucket {
		public int slowSize() {
			int s = 0;

			for (int i = 0; i < BUCKET_CT; i++) {
				if (children[i] != null)
					s += children[i].size();
			}
			return s;
		}

		private static final int BUCKET_CT = 16;
		@SuppressWarnings("rawtypes")
		protected ArrayList[] children;

		public EndMantissaBucket(MantissaBucket parent, int index) {
			super(parent, index);
			children = new ArrayList[BUCKET_CT];
		}

		public boolean isEmpty() {
			for (int i = 0; i < BUCKET_CT; i++) {
				if (children[i] != null) {
					assert (firstOpen == i);
					return false;
				}
			}
			assert (firstOpen == -1);
			return true;
		}

		@Override
		public MantissaBucket[] getChildren() {
			throw new UnsupportedOperationException();
		}

		@Override
		protected boolean checkChildren() {
			for (int i = 0; i < BUCKET_CT; i++) {
				if (i < firstOpen) {
					assert (children[i] == null);
				}
				if (children[i] != null) {
					for (int j = 0; j < children[i].size(); j++) {
						@SuppressWarnings("unchecked")
						Type item = (Type) children[i].get(j);
						assert (j == item.getHeapIndex());
					}
				}
			}
			return true;
		}
	}

	MiddleMantissaBucket[] base;
	private int firstExponent;
	private static final int BASE_BUCKET_COUNT = 2048;

	public enum HEAPTYPE {
		GHEAP, FHEAP
	}

	private double bucketItem(T item) {
		if (this.h == HEAPTYPE.FHEAP) {
			return item.getF();
		} else {
			return item.getG();
		}
	}

	private final HEAPTYPE h;

	public BinHeap(HEAPTYPE h, double f) {
		this.h = h;
		this.firstExponent = -1;
		this.f = f;
		size = 0;
		base = new MiddleMantissaBucket[BASE_BUCKET_COUNT];
	}

	public static final int mantissaOnly(long l, int startPosition, int keep) {
		l <<= 12 + startPosition;
		l >>>= 64 - keep;
		return (int) l;
	}

	public static final long exponentOnly(long l) {
		long toReturn = l;

		toReturn <<= 1;
		toReturn >>>= 53;

		return toReturn;
	}

	public static final void printBinary(PrintStream p, long l) {
		long mask = 0x8000000000000000l;
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < 64; i++) {
			char c;
			if ((mask & l) == 0)
				c = '0';
			else
				c = '1';
			b.append(c);
			if (i == 0 || i == 11 || i == 55)
				b.append(' ');

			mask = mask >>> 1;
		}
		p.append(b);
	}

	@Override
	public double getF() {
		return f;
	}

	private final double f;

	@Override
	public double getG() {
		throw new RuntimeException("no such method getG()");
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		for (T item : c) {
			this.add(item);
		}
		return true;
	}

	@Override
	public void clear() {
		for (int i = 0; i < BASE_BUCKET_COUNT; i++) {
			base[i] = null;
		}
	}

	@SuppressWarnings("unchecked")
	public EndMantissaBucket<T> getOrMakeFinalBucket(double d) {
		long value = Double.doubleToLongBits(d);

		int expIX = (int) BinHeap.exponentOnly(value);

		if (this.firstExponent == -1 || this.firstExponent > expIX) {
			this.firstExponent = expIX;
		}

		if (base[expIX] == null) {
			base[expIX] = new MiddleMantissaBucket(null, expIX);
		}
		MantissaBucket currentBucket = base[expIX];
		for (int shiftIX = 0; shiftIX <= 32; shiftIX += 8) {
			int newIndex = BinHeap.mantissaOnly(value, shiftIX, 8);

			MantissaBucket nextBucket = currentBucket.getChildren()[newIndex];

			if (currentBucket.firstOpen == -1
					|| currentBucket.firstOpen > newIndex) {
				currentBucket.firstOpen = newIndex;
			}

			if (nextBucket == null) {
				nextBucket = new MiddleMantissaBucket(currentBucket, newIndex);
				currentBucket.getChildren()[newIndex] = nextBucket;
			}
			currentBucket = nextBucket;
		}
		int newIndex = BinHeap.mantissaOnly(value, 40, 8);
		MantissaBucket nextBucket = currentBucket.getChildren()[newIndex];

		if (currentBucket.firstOpen == -1 || currentBucket.firstOpen > newIndex) {
			currentBucket.firstOpen = newIndex;
		}

		if (nextBucket == null) {
			nextBucket = new EndMantissaBucket<T>(currentBucket, newIndex);
			currentBucket.getChildren()[newIndex] = nextBucket;
			currentBucket = nextBucket;
		}
		assert (nextBucket instanceof EndMantissaBucket);
		return (EndMantissaBucket<T>) nextBucket;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean add(T e) {
		assert (check());
		size++;
		int lastIndex = getLastIndex(bucketItem(e));
		// System.err.println("add last index for " + value + " " + lastIndex +
		// " " + e);
		EndMantissaBucket<T> lastBucket = getOrMakeFinalBucket(bucketItem(e));
		@SuppressWarnings("rawtypes")
		ArrayList finalContainer = lastBucket.children[lastIndex];
		if (finalContainer == null) {
			lastBucket.children[lastIndex] = new ArrayList<T>(2);
		}

		if (lastBucket.firstOpen == -1 || lastBucket.firstOpen > lastIndex) {
			lastBucket.firstOpen = lastIndex;
		}

		e.setHeapIndex(lastBucket.children[lastIndex].size());
		lastBucket.children[lastIndex].add(e);

		assert (check());
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object o) {
		try {
			// this is okay because the fail is caught and dealt with
			T e = (T) o;

			if (e.getHeapIndex() == Heapable.NO_POS)
				return false;

			EndMantissaBucket<T> lastBucket = getFinalBucket(bucketItem(e));
			if (lastBucket == null)
				return false;
			assert (lastBucket != null);

			int lastIndex = getLastIndex(bucketItem(e));
			// System.err.println("contains last index for " +
			// Double.doubleToLongBits(e.getF()) + " " + lastIndex + " " + o);
			@SuppressWarnings("rawtypes")
			ArrayList finalContainer = lastBucket.children[lastIndex];
			if (finalContainer == null) {
				assert (false);
				return false;
			}
			int pos = e.getHeapIndex();
			if (lastBucket.children[lastIndex].size() <= pos)
				return false;
			Object retrieved = lastBucket.children[lastIndex].get(pos);
			// if the item is present, this is where it is going to be.
			return retrieved.equals(e);

		} catch (ClassCastException cce) {
			return false;
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object item : c) {
			if (!this.contains(item))
				return false;
		}
		return true;
	}

	private boolean slowIsEmpty() {
		for (int i = 0; i < BinHeap.BASE_BUCKET_COUNT; i++) {
			if (base[i] != null)
				return false;
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return this.firstExponent == -1;
	}

	@Override
	public Iterator<T> iterator() {
		throw new RuntimeException("can't iterate over a bin heap");
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object o) {
		try {
			assert (check());
			assert (size > 0);
			assert (!this.isEmpty());
			assert (!this.slowIsEmpty());
			// this is okay because the fail is caught and dealt with
			T e = (T) o;

			if (e.getHeapIndex() < 0) {
				return false;
			}

			EndMantissaBucket<T> lastBucket = getFinalBucket(bucketItem(e));
			if (lastBucket == null)
				return false;
			assert (lastBucket != null);

			int lastIndex = getLastIndex(bucketItem(e));
			// System.err.println("contains last index for " +
			// Double.doubleToLongBits(e.getF()) + " " + lastIndex + " " + o);
			@SuppressWarnings("rawtypes")
			ArrayList finalContainer = lastBucket.children[lastIndex];
			if (finalContainer == null) {
				// this shouldn't happen, because the item thinks its in the
				// BinHeap but can't find it.
				assert (false);
				return false;
			}

			Object toReturn = lastBucket.children[lastIndex].get(e
					.getHeapIndex());
			if (!(toReturn == e)) {
				assert (false);
				return false;
			} else {
				int lastChild = lastBucket.children[lastIndex].size() - 1;
				int toRemoveID = e.getHeapIndex();
				lastBucket.children[lastIndex].set(toRemoveID,
						lastBucket.children[lastIndex].get(lastChild));
				T moved = (T) lastBucket.children[lastIndex].get(toRemoveID);
				moved.setHeapIndex(toRemoveID);

				// TODO there is a bug here with the end index being written to
				// the remaining item, which clearly shouldn't happen.

				lastBucket.children[lastIndex].remove(lastChild);
				e.setHeapIndex(Heapable.NO_POS);

				if (lastBucket.children[lastIndex].isEmpty()) {
					// if this isn't the first index, just null it and be done,
					// otherwise, consider removing it.
					if(lastBucket.firstOpen == lastIndex)
						removeLastBucket(lastBucket);
					else
						lastBucket.children[lastIndex] = null;
				}
				this.size--;
				// bucket is either not empty or gone.
				assert (check());
				return true;
			}
		} catch (ClassCastException cce) {
			return false;
		}
	}

	private void removeLastBucket(EndMantissaBucket<T> last) {
		if (last.children[last.firstOpen].size() == 0) {
			int start = last.firstOpen;
			last.firstOpen = -1;
			last.children[start] = null;

			for (int i = start + 1; i < BinHeap.EndMantissaBucket.BUCKET_CT; i++) {
				if (last.children[i] != null) {
					assert (!last.children[i].isEmpty());
					last.firstOpen = i;
					break;
				}
			}
		}

		if (last.firstOpen == -1) {
			MantissaBucket left = last.parent;
			last.parent.emptyBucket(last.index);
			last.parent.getChildren()[last.index] = null;
			removeUpToRoot(left);
		}

	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean toReturn = false;
		for (Object o : c) {
			boolean pulled = remove(o);
			if (pulled) {
				toReturn = true;
			}
		}
		return toReturn;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	private int size;

	@SuppressWarnings("unused")
	private int slowSize() {
		int s = 0;

		for (int i = 0; i < BASE_BUCKET_COUNT; i++) {
			if (this.base[i] != null) {
				s += this.base[i].slowSize();
			}
		}
		return s;
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
	public <Type> Type[] toArray(Type[] a) {
		throw new UnsupportedOperationException();
	}

	static int getLastIndex(double d) {
		return mantissaOnly(Double.doubleToLongBits(d), 48, 4);
	}

	public EndMantissaBucket<T> getFinalBucket(double v) {
		long value = Double.doubleToLongBits(v);
		int expIX = (int) BinHeap.exponentOnly(value);

		if (base[expIX] == null) {
			return null;
		}
		MantissaBucket currentBucket = base[expIX];
		for (int shiftIX = 0; shiftIX <= 32; shiftIX += 8) {
			int newIndex = BinHeap.mantissaOnly(value, shiftIX, 8);

			MantissaBucket nextBucket = currentBucket.getChildren()[newIndex];
			if (nextBucket == null) {
				return null;
			}
			currentBucket = nextBucket;
		}
		int newIndex = BinHeap.mantissaOnly(value, 40, 8);
		MantissaBucket nextBucket = currentBucket.getChildren()[newIndex];
		if (nextBucket == null) {
			return null;
		}
		assert (nextBucket instanceof EndMantissaBucket);
		@SuppressWarnings("unchecked")
		EndMantissaBucket<T> lastBucket = (EndMantissaBucket<T>) nextBucket;
		return lastBucket;
	}

	@Override
	public T element() {
		T toReturn = peek();
		if (toReturn == null)
			throw new NoSuchElementException();
		return toReturn;
	}

	@Override
	public boolean offer(T e) {
		return add(e);
	}

	public EndMantissaBucket<T> getLeftBucket() {
		assert (check());
		MantissaBucket left = base[this.firstExponent];
		for (int depth = 0; depth < 6; depth++) {
			MantissaBucket[] children = left.getChildren();
			left = children[left.firstOpen];
			assert (left != null);
		}

		assert (left instanceof EndMantissaBucket);
		@SuppressWarnings("unchecked")
		EndMantissaBucket<T> last = (EndMantissaBucket<T>) left;

		return last;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T peek() {
		assert (!slowIsEmpty());

		EndMantissaBucket<T> last = getLeftBucket();
		T toReturn = null;
		assert (last.firstOpen != -1);
		assert (last.children[last.firstOpen].size() > 0);
		toReturn = (T) last.children[last.firstOpen]
				.get(last.children[last.firstOpen].size() - 1);
		assert (toReturn != null);
		return toReturn;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T poll() {
		assert (check());

		EndMantissaBucket<T> left = getLeftBucket();

		T toReturn = null;

		assert (left.firstOpen != -1);
		assert (left.children[left.firstOpen].size() > 0);
		toReturn = (T) left.children[left.firstOpen]
				.remove(left.children[left.firstOpen].size() - 1);
		assert (toReturn != null);

		// check if this was the last thing in its bucket.

		removeLastBucket(left);

		this.size--;
		assert (toReturn != null);
		toReturn.setHeapIndex(Heapable.NO_POS);

		assert (check());
		return toReturn;
	}

	private void removeUpToRoot(MantissaBucket left) {

		// deletes from this bucket
		boolean deleteFromRoot = false;

		while (left != null) {
			if (left.isEmpty()) {
				if (left.parent == null) {
					deleteFromRoot = true;
					break;
				}
				left.parent.getChildren()[left.index] = null;

				if (left.index > left.parent.firstOpen) {
					// the left child isn't the first open
				} else {
					left.parent.firstOpen = -1;
					for (int i = left.index + 1; i < BinHeap.MiddleMantissaBucket.BUCKET_CT; i++) {
						if (left.parent.getChildren()[i] != null) {
							left.parent.firstOpen = i;
							break;
						}
					}
				}
				if (left.parent.firstOpen == -1) {
					left = left.parent;
				} else
					break;
			} else {
				assert (false);
				break;
			}
		}
		if (deleteFromRoot) {
			assert (base[left.index] == left);
			base[left.index] = null;

			if (firstExponent < left.index) {
			} else {
				int baseIX = firstExponent;
				this.firstExponent = -1;
				for (int i = baseIX; i < BinHeap.BASE_BUCKET_COUNT; i++) {
					if (this.base[i] != null) {
						firstExponent = i;
						break;
					}
				}
			}
		}
	}

	public boolean check() {
		assert (size >= 0);
		// if (size != slowSize()) {
		// System.err.println("slow size is " + slowSize() + " with size "
		// + size);
		// }
		// assert (size == slowSize());

		for (int i = 0; i < firstExponent; i++) {
			if (base[i] != null)
				return false;
		}

		if ((firstExponent < 0 && size != 0)
				|| firstExponent >= BASE_BUCKET_COUNT) {
			System.err.println("first exponent is " + firstExponent
					+ " and size is " + size);
			return false;
		}

		if (firstExponent < 0) {
			for (int i = 0; i < firstExponent; i++) {
				if (base[i] != null)
					return false;
			}
			return true;
		}

		if (base[firstExponent] == null) {
			System.err.println("first exponent is " + firstExponent);
			return false;
		}

		for (int i = 0; i < BinHeap.BASE_BUCKET_COUNT; i++) {
			if (base[i] != null) {
				if (base[i].index != i) {
					System.err.println("Base bucket " + i + " has index "
							+ base[i].index);
				}
				boolean passes = base[i].check(i);
				if (!passes)
					return false;
			}
		}
		return true;
	}

	@Override
	public T remove() {
		T toReturn = poll();
		if (toReturn == null)
			throw new NoSuchElementException();
		return toReturn;
	}

	public void incrsize() {
		size++;
	}

	private int heapIndex = Heapable.NO_POS;

	@Override
	public int getHeapIndex() {
		return heapIndex;
	}

	@Override
	public void setHeapIndex(int ix) {
		this.heapIndex = ix;
	}

}
