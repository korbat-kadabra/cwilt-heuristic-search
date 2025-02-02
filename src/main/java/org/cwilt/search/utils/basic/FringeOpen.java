package org.cwilt.search.utils.basic;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class FringeOpen<T> implements List<T> {

	public int nodeCount() {
		if (head == null)
			return 0;
		Node<T> n = head;
		int count = 0;
		while (n != null) {
			n = n.next;
			count++;
		}
		return count;
	}

	public FringeOpen() {
		head = null;
		size = 0;
	}

	private static class Node<T> {
		T payload;
		Node<T> next;
		Node<T> prev;

		Node(T payload) {
			this.payload = payload;
		}
	}

	Node<T> head;
	int size;

	@Override
	public boolean add(T e) {
		size++;
		Node<T> newNode = new Node<T>(e);
		if (head != null)
			head.prev = newNode;
		newNode.next = head;
		head = newNode;
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		if (c.isEmpty())
			return false;
		Iterator<? extends T> i = c.iterator();
		while (i.hasNext()) {
			this.add(i.next());
		}
		return true;
	}

	@Override
	public void clear() {
		size = 0;
		head = null;
	}

	@Override
	public boolean contains(Object o) {
		Node<T> current = head;
		while (current != null) {
			if (o.equals(current.payload))
				return true;
			current = current.next;
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		if (c.isEmpty())
			return false;
		Iterator<?> i = c.iterator();
		while (i.hasNext()) {
			if (!this.contains(i.next()))
				return false;
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return head == null;
	}

	public ListIterator<T> listIterator() {
		return new FringeIterator(0);
	}

	private class FringeIterator implements ListIterator<T> {
		private Node<T> lastReturned = null;
		private Node<T> next = head;
		private int nextIndex;

		FringeIterator(int index) {
			if (index < 0 || index > size)
				throw new IndexOutOfBoundsException("Index: " + index
						+ ", Size: " + size);
			if (index <= (size)) {
				next = head;
				for (nextIndex = 0; nextIndex < index; nextIndex++)
					next = next.next;
			} else {
				next = head;
			}
		}

		public boolean hasNext() {
			return next != null;
		}

		public T next() {
			if (next == null)
				throw new NoSuchElementException();
			lastReturned = next;
			next = next.next;
			nextIndex++;
			return lastReturned.payload;
		}

		public boolean hasPrevious() {
			return nextIndex != 0;
		}

		public T previous() {
			if (nextIndex == 0)
				throw new NoSuchElementException();

			lastReturned = next = next.prev;
			nextIndex--;
			return lastReturned.payload;
		}

		public int nextIndex() {
			return nextIndex;
		}

		public int previousIndex() {
			return nextIndex - 1;
		}

		public void remove() {
			Node<T> toRemove = lastReturned;
			lastReturned = lastReturned.prev;
			if (toRemove == null) {
				throw new IllegalStateException();
			}
			size--;
			if (toRemove.prev == null) {
				head = toRemove.next;
			} else {
				toRemove.prev.next = toRemove.next;
			}
			if (toRemove.next != null) {
				toRemove.next.prev = toRemove.prev;
			}
		}

		public void set(T e) {
			if (lastReturned == head)
				throw new IllegalStateException();
			lastReturned.payload = e;
		}

		public void add(T e) {
			Node<T> toAdd = new Node<T>(e);
			toAdd.next = next;
			size++;
			next = toAdd;
			toAdd.prev = lastReturned;
			if (lastReturned == null) {
				head = toAdd;
				if (head.next != null)
					head.next.prev = toAdd;
			} else {
				toAdd.next = lastReturned.next;
				if (lastReturned.next != null)
					lastReturned.next.prev = toAdd;
				lastReturned.next = toAdd;
			}
			nextIndex++;
		}

	}

	/**
	 * Checks the list's internal pointers
	 */
	public void checkList() {
		Node<T> current = head;

		while (current != null) {
			if (current.prev != null) {
				assert (current.prev.next == current);
			}
			if (current.next != null) {
				if (current.next.prev != current) {
					System.err.println("failed at node " + current.payload);
				}
				assert (current.next.prev == current);
			}
			current = current.next;
		}
		if (size != nodeCount()) {
			System.err.println("size local: " + size);
			System.err.println("node count: " + this.nodeCount());
			System.err.println(this);
			assert (size == nodeCount());
		}
	}

	@Override
	public Iterator<T> iterator() {
		return new FringeIterator(0);
	}

	@Override
	public boolean remove(Object o) {
		Node<T> current = head;
		while (current != null) {
			if (o.equals(current.payload)) {
				size--;
				Node<T> p = current.prev;
				Node<T> n = current.next;
				if (p != null)
					p.next = n;
				if (n != null)
					n.prev = p;
				return true;
			} else {
				current = current.next;
			}
		}
		return false;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		if (c.isEmpty())
			return false;
		Iterator<?> i = c.iterator();
		boolean removed = false;
		while (i.hasNext()) {
			boolean rejected = this.remove(i.next());
			if (removed == false && rejected == true)
				removed = true;
		}
		return removed;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Object[] toArray() {
		Object[] ary = new Object[size];
		Node<T> current = head;
		int ix = 0;
		while (current != null) {
			ary[ix] = current.payload;
			current = current.next;
			ix++;
		}

		return ary;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S> S[] toArray(S[] a) {
		if (a.length < size)
			a = (S[]) Array.newInstance(a.getClass().getComponentType(), size);
		else if (a.length > size)
			a[size] = null;
		Node<T> e = head;
		for (int i = 0; i < size; i++) {
			a[i] = (S) e.payload;
			e = e.next;
		}
		return a;
	}

	@Override
	public void add(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T get(int index) {
		Node<T> c = head;
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		for (int i = 0; i < index; i++) {
			c = c.next;
		}
		return c.payload;
	}

	@Override
	public int indexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int lastIndexOf(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return new FringeIterator(index);
	}

	@Override
	public T remove(int index) {
		Node<T> c = head;
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		for (int i = 0; i < index; i++) {
			c = c.next;
		}
		size--;
		if (c.prev != null)
			c.prev.next = c.next;
		if (c.next != null)
			c.next.prev = c.prev;

		return c.payload;
	}

	@Override
	public T set(int index, T element) {
		Node<T> c = head;
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		for (int i = 0; i < index; i++) {
			c = c.next;
		}
		T toReturn = c.payload;
		c.payload = element;
		return toReturn;
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("[");
		Node<T> n = head;
		while (n != null) {
			b.append(n.payload);
			b.append(",");
			n = n.next;
		}
		b.append("]");

		return b.toString();
	}

}
