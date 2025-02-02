package org.cwilt.search.utils.experimental;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.cwilt.search.utils.experimental.BinHeap.EndMantissaBucket;
public class DBinHeap<T extends BinHeapable> implements Queue<T> {
	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object arg0) {
		try {
			// this is okay because the fail is caught and dealt with
			T e = (T) arg0;
			EndMantissaBucket<BinHeap<T>> lastBucket = fHeap.getFinalBucket(e
					.getF());
			if (lastBucket == null)
				return false;
			assert (lastBucket != null);

			int lastIndex = BinHeap.getLastIndex(e.getF());
			// System.err.println("contains last index for " +
			// Double.doubleToLongBits(e.getF()) + " " + lastIndex + " " + o);
			@SuppressWarnings("rawtypes")
			ArrayList finalContainer = lastBucket.children[lastIndex];
			if (finalContainer == null) {
				assert (false);
				return false;
			}
			BinHeap<T> l = (BinHeap<T>) lastBucket.children[lastIndex].get(0);
			return l.contains(e);

		} catch (ClassCastException cce) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object arg0) {
		try {
			assert(fHeap.check());
			// this is okay because the fail is caught and dealt with
			T e = (T) arg0;

			if (e.getHeapIndex() < 0) {
				return false;
			}

			EndMantissaBucket<BinHeap<T>> lastBucket = fHeap.getFinalBucket(e
					.getF());
			if (lastBucket == null)
				return false;
			assert (lastBucket != null);

			int lastIndex = BinHeap.getLastIndex(e.getF());
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

			BinHeap<T> bucket = (BinHeap<T>) lastBucket.children[lastIndex]
					.get(0);

			boolean removed = bucket.remove(e);

			if (removed == false)
				return false;

			if(bucket.isEmpty()){
				fHeap.remove(bucket);
			}
			this.size--;
			// bucket is either not empty or gone.
			return true;
		} catch (ClassCastException cce) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean add(T arg0) {
		size++;
		assert(fHeap.check());
		EndMantissaBucket<BinHeap<T>> lastBucket = fHeap
				.getOrMakeFinalBucket(arg0.getF());

		int lastIndex = BinHeap.getLastIndex(arg0.getF());

		ArrayList<BinHeap<T>> finalContainer = lastBucket.children[lastIndex];
		if (finalContainer == null) {
			lastBucket.children[lastIndex] = new ArrayList<T>();
		}

		if (lastBucket.firstOpen == -1 || lastBucket.firstOpen > lastIndex) {
			lastBucket.firstOpen = lastIndex;
		}

		BinHeap<T> gBinHeap = null;
		if (lastBucket.children[lastIndex].size() == 0) {
			fHeap.incrsize();
			gBinHeap = new BinHeap<T>(BinHeap.HEAPTYPE.GHEAP, arg0.getF());
			assert(lastBucket.children[lastIndex].size() == 0);
			lastBucket.children[lastIndex].add(gBinHeap);
			gBinHeap.setHeapIndex(0);
		} else
			gBinHeap = (BinHeap<T>) lastBucket.children[lastIndex].get(0);

		boolean r = gBinHeap.add(arg0);
		
		assert(fHeap.check());

		return r;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T peek() {
		EndMantissaBucket<BinHeap<T>> last = fHeap.getLeftBucket();
		BinHeap<T> toReturn = null;
		assert (last.firstOpen != -1);
		assert (last.children[last.firstOpen].size() > 0);
		toReturn = (BinHeap<T>) last.children[last.firstOpen].get(0);
		assert (toReturn != null);
		return toReturn.peek();
	}

	@SuppressWarnings("unchecked")
	@Override
	public T poll() {
		assert(fHeap.check());
		EndMantissaBucket<BinHeap<T>> last = fHeap.getLeftBucket();
		BinHeap<T> bucket = null;
		assert (last.firstOpen != -1);
		assert (last.children[last.firstOpen].size() > 0);
		bucket = (BinHeap<T>) last.children[last.firstOpen].get(0);
		assert (bucket != null);
		assert(!bucket.isEmpty());
		T t = bucket.poll();

		assert(fHeap.check());

		// check if this bucket is now empty, and should be removed.
		if (bucket.isEmpty()) {
			assert(fHeap.check());
			boolean r = fHeap.remove(bucket);
			assert(fHeap.check());
			//this thing wasn't found in the second heap
			assert(r);
		}

		assert(fHeap.check());
		return t;
	}

	private final BinHeap<BinHeap<T>> fHeap;

	public DBinHeap() {
		this.fHeap = new BinHeap<BinHeap<T>>(BinHeap.HEAPTYPE.FHEAP, 0);
	}

	@Override
	public boolean addAll(Collection<? extends T> arg0) {
		boolean addedSomething = false;

		for (T item : arg0) {
			boolean a = add(item);
			if (a) {
				addedSomething = true;
			}
		}
		return addedSomething;
	}

	@Override
	public void clear() {
		fHeap.clear();
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
	public boolean isEmpty() {
		return fHeap.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		boolean removed = false;
		for (Object o : arg0) {
			if (remove(o))
				removed = true;
		}
		return removed;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		boolean removed = false;
		
		for(Object x : arg0) {
			if(this.contains(x)) {
				if(this.remove(x)) {
					removed = true;
				}
				
			}
		}
		return removed;
	}

	private int size;

	@Override
	public int size() {
		return size;
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <Ty> Ty[] toArray(Ty[] arg0) {
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
		if (toReturn == null)
			throw new NoSuchElementException();
		return toReturn;
	}

}
