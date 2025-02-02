package org.cwilt.search.utils.basic;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;

import org.cwilt.search.search.SearchNode;
public class BucketPriorityQueue extends AbstractBucketQueue {
	public abstract static class Bucketer {
		public final double bucketSize;

		protected Bucketer(double bucketSize) {
			this.bucketSize = bucketSize;
		}

		public abstract int bucketItem(SearchNode b);
	}

	public static class FBucketer extends Bucketer {
		public FBucketer(double bucketSize) {
			super(bucketSize);
		}

		public int bucketItem(SearchNode b) {
			return (int) (b.getF() / bucketSize);
		}
	}

	public static class HBucketer extends Bucketer {
		public HBucketer(double bucketSize) {
			super(bucketSize);
		}

		public int bucketItem(SearchNode b) {
			return (int) (b.getH() / bucketSize);
		}
	}

	public static class WBucketer extends Bucketer {
		private final double weight;

		public WBucketer(double bucketSize, double weight) {
			super(bucketSize);
			this.weight = weight;
		}

		public int bucketItem(SearchNode b) {
			return (int) ((b.getH() * weight + b.getG()) / bucketSize);
		}
	}

	private int size;
	private int firstOpen;
	private Bucketer b;

	private final ArrayList<ArrayList<SearchNode>> nodes;

	public BucketPriorityQueue(Bucketer bucketer) {
		this.b = bucketer;
		this.size = 0;
		this.firstOpen = Integer.MAX_VALUE;
		this.nodes = new ArrayList<ArrayList<SearchNode>>();
	}

	public SearchNode peek() {
		// assert (check());
		if (size <= 0) {
			throw new NoSuchElementException();
		}
		ArrayList<SearchNode> minarr = nodes.get(firstOpen);
		// if (minarr == null)
		// System.err.println(this);
		assert (minarr != null);
		assert (minarr.size() > 0);
		SearchNode toReturn = minarr.get(minarr.size() - 1);
		// assert (check());

		return toReturn;
	}

	public List<SearchNode> getAll() {
		ArrayList<SearchNode> allNodes = new ArrayList<SearchNode>(size);
		for (ArrayList<SearchNode> a : nodes) {
			if (a != null)
				allNodes.addAll(a);
		}
		return allNodes;
	}

	public SearchNode pop() {
		// assert (check());
		if (size <= 0) {
			throw new NoSuchElementException();
		}
		ArrayList<SearchNode> minarr = nodes.get(firstOpen);
		if (minarr == null)
			System.err.println(this);
		assert (minarr != null);
		assert (minarr.size() > 0);
		SearchNode toReturn = minarr.remove(minarr.size() - 1);
		// SearchNode toReturn = minarr.remove(0);
		// java.util.Random r = new java.util.Random();
		// int toRemoveIndex = r.nextInt(minarr.size());
		// SearchNode toReturn = minarr.remove(toRemoveIndex);

		if (minarr.isEmpty()) {
			nodes.set(firstOpen, null);
			for (int i = firstOpen + 1; i < nodes.size(); i++) {
				if (nodes.get(i) != null) {
					firstOpen = i;
					if (nodes.get(i).isEmpty()) {
						System.err.printf("%d is empty\n", i);
						System.err.printf("size: %d\n", size);
						System.err.println(this);
						assert (false);
					}
					break;
				}
			}
		}
		size--;
		if (size == 0)
			firstOpen = Integer.MAX_VALUE;
		toReturn.setHeapIndex(Heapable.NO_POS);

		// assert (check());

		return toReturn;
	}

	public boolean add(SearchNode e) {
		// assert (check());
		int index = b.bucketItem(e);
		// update first index
		if (index < firstOpen)
			firstOpen = index;
		// make sure there is room
		if (this.nodes.size() <= index) {
			for (int i = this.nodes.size(); i <= index; i++) {
				this.nodes.add(null);
			}
		}

		ArrayList<SearchNode> next = this.nodes.get(index);

		if (next == null) {
			this.nodes.set(index, new ArrayList<SearchNode>());
			next = this.nodes.get(index);
		}
		assert (next != null);

		e.setHeapIndex(next.size());
		next.add(e);

		size++;

		// assert (check());

		return true;
	}

	public boolean addAll(Collection<? extends SearchNode> c) {
		for (SearchNode b : c) {
			this.add(b);
		}
		return true;
	}

	public void clear() {
		nodes.clear();
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public int size() {
		return size;
	}

	// private boolean check() {
	// for (int i = 0; i < nodes.size(); i++) {
	// if (i < firstOpen) {
	// if (nodes.get(i) != null) {
	// System.err.println("Empty list");
	// return false;
	// }
	// }
	// if (i == firstOpen) {
	// if (nodes.get(i) == null) {
	// System.err.println("firstOpen is null");
	// return false;
	// }
	// if (nodes.get(i).size() == 0) {
	// System.err.println("firstOpen is empty");
	// return false;
	// }
	// }
	// if (i > firstOpen) {
	// if (nodes.get(i) != null && nodes.get(i).size() == 0) {
	// System.err.println("Extra empty lists at index " + i);
	// return false;
	// }
	//
	// }
	// }
	// return true;
	// }

	public void remove(SearchNode incumbent, int ix) {
		// assert (check());
		int arrayIndex = b.bucketItem(incumbent);
		ArrayList<SearchNode> array = nodes.get(arrayIndex);
		SearchNode last = array.get(array.size() - 1);
		SearchNode toRemove = array.get(ix);
		// only one thing in the array
		size--;
		if (last == toRemove) {
			last.setHeapIndex(Heapable.NO_POS);
			array.remove(array.size() - 1);
			if (array.isEmpty()) {
				nodes.set(arrayIndex, null);
			}

			if (firstOpen == arrayIndex && array.isEmpty()) {
				int oldFirstOpen = firstOpen;
				for (int i = firstOpen + 1; i < nodes.size(); i++) {
					if (nodes.get(i) != null) {
						firstOpen = i;
						assert (!nodes.get(i).isEmpty());
						break;
					}
				}
				if (oldFirstOpen == firstOpen) {
					assert (this.size == 0);
					firstOpen = Integer.MAX_VALUE;
				}
			}
			// assert (check());
		} else {
			last.setHeapIndex(ix);
			toRemove.setHeapIndex(Heapable.NO_POS);
			array.set(ix, last);
			assert (array.size() > 1);
			array.remove(array.size() - 1);
		}
		// assert (check());
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("first open: ");
		b.append(firstOpen);
		b.append("\n");
		for (int i = 0; i < nodes.size(); i++) {
			b.append(i);
			b.append(" ");
			if (nodes.get(i) == null)
				b.append("(0)\n");
			else {
				b.append(nodes.get(i).size());
				b.append("\n");
			}
		}

		return b.toString();
	}
}
