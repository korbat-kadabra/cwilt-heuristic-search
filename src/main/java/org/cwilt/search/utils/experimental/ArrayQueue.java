package org.cwilt.search.utils.experimental;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.cwilt.search.utils.basic.Heapable;
/**
 * Simple implementation of a queue using an array list, acts as a stack for
 * speed.
 * 
 * @author cmo66
 * 
 * @param <X>
 */
public class ArrayQueue<X extends Heapable> extends ArrayList<X> implements
		Queue<X> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7430661902092687913L;

	@Override
	public boolean add(X item) {
		super.add(item);
		item.setHeapIndex(super.size() - 1);
		return true;
	}

	@Override
	public X remove(int ix) {
		int last = super.size() - 1;
		if (last == ix) {
			super.get(last).setHeapIndex(Heapable.NO_POS);
			return super.remove(ix);
		} else {
			// swap this with the one currently at the end
			X oldLast = super.get(super.size() - 1);
			X toReturn = super.get(ix);
			super.set(ix, oldLast);
			super.remove(super.size() - 1);
			oldLast.setHeapIndex(ix);
			toReturn.setHeapIndex(Heapable.NO_POS);
			return toReturn;
		}
	}

	@Override
	public X element() {
		if (super.size() == 0)
			throw new NoSuchElementException();
		return super.get(super.size() - 1);
	}

	@Override
	public boolean offer(X e) {
		return super.add(e);
	}

	@Override
	public X peek() {
		if (super.size() == 0)
			return null;
		return super.get(super.size() - 1);
	}

	@Override
	public X poll() {
		if (super.size() == 0)
			return null;
		super.get(super.size() - 1).setHeapIndex(Heapable.NO_POS);
		return super.remove(super.size() - 1);
	}

	@Override
	public X remove() {
		if (super.size() == 0)
			throw new NoSuchElementException();
		super.get(super.size() - 1).setHeapIndex(Heapable.NO_POS);
		return super.remove(super.size() - 1);
	}

	@Override
	public boolean contains(Object victim) {
		Heapable v = (Heapable) victim;
		assert(v.getHeapIndex() < this.size());
		X r = get(v.getHeapIndex());
		if (r != victim)
			return false;
		else
			return true;
	}

	@Override
	public boolean remove(Object victim) {
		Heapable v = (Heapable) victim;
		int index = v.getHeapIndex();
		if(index == Heapable.NO_POS)
			return false;
		if(super.size() <= index)
			assert(false);
		X other = super.get(index);
		if(!other.equals(v))
			assert(false);
		//tag the leaving guy as gone
		v.setHeapIndex(Heapable.NO_POS);
		if(super.size() - 1 == index){
			super.remove(super.size() - 1);
			return true;
		}
		super.set(index, super.get(super.size() - 1));
		super.get(index).setHeapIndex(index);
		super.remove(super.size() - 1);

		
		return true;
	}
	
}
