package org.cwilt.search.utils.basic;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.cwilt.search.search.SearchNode;
public class FGBucketPriorityQueue extends AbstractBucketQueue{
	private int size;
	private int firstOpen;
	private final BucketPriorityQueue.Bucketer primaryBucketer;
	private final BucketPriorityQueue.Bucketer secondaryBucketer;

	private final ArrayList<BucketPriorityQueue> nodes;

	public FGBucketPriorityQueue(BucketPriorityQueue.Bucketer pBucketer, BucketPriorityQueue.Bucketer sBucketer ) {
		this.primaryBucketer = pBucketer;
		this.secondaryBucketer = sBucketer;
		this.size = 0;
		this.firstOpen = Integer.MAX_VALUE;
		this.nodes = new ArrayList<BucketPriorityQueue>();
	}
	
	public SearchNode peek() {
		if (size <= 0) {
			throw new NoSuchElementException();
		}

		BucketPriorityQueue minarr = nodes.get(firstOpen);
		if (minarr == null)
			System.err.println(this);
		assert (minarr != null);
		assert (minarr.size() > 0);
		SearchNode toReturn = minarr.peek();
		return toReturn;
	}

	public List<SearchNode> getAll() {
		ArrayList<SearchNode> allNodes = new ArrayList<SearchNode>(size);
		for(BucketPriorityQueue a : nodes){
			if(a != null)
				allNodes.addAll(a.getAll());
		}
		return allNodes;
	}

	public SearchNode pop() {
		if (size <= 0) {
			throw new NoSuchElementException();
		}

		BucketPriorityQueue minarr = nodes.get(firstOpen);
		if (minarr == null)
			System.err.println(this);
		assert (minarr != null);
		assert (minarr.size() > 0);
		SearchNode toReturn = minarr.pop();

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
		if(size == 0)
			firstOpen = Integer.MAX_VALUE;
		
		assert(toReturn.getHeapIndex() == Heapable.NO_POS);
		return toReturn;
	}

	public boolean add(SearchNode e) {
		int index = primaryBucketer.bucketItem(e);
		// update first index
		if (index < firstOpen)
			firstOpen = index;
		// make sure there is room
		if (this.nodes.size() <= index) {
			for (int i = this.nodes.size(); i <= index; i++) {
				this.nodes.add(null);
			}
		}

		BucketPriorityQueue next = this.nodes.get(index);

		if (next == null) {
			this.nodes.set(index, new BucketPriorityQueue(secondaryBucketer));
			next = this.nodes.get(index);
		}
		assert (next != null);

		next.add(e);

		size++;

		assert(e.getHeapIndex() != Heapable.NO_POS);
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

//	private boolean check(){
//		for(int i = 0; i < nodes.size(); i++){
//			if(i < firstOpen){
//				if(nodes.get(i) != null && nodes.get(i).size() != 0){
//					System.err.println(this);
//					return false;
//				}
//			}
//			if(i == firstOpen){
//				if(nodes.get(i).size() == 0){
//					System.err.println(this);
//					return false;
//				}
//			}				
//		}
//		return true;
//	}
	
	public void remove(SearchNode incumbent, int ix) {
		int arrayIndex = primaryBucketer.bucketItem(incumbent);
		BucketPriorityQueue array = nodes.get(arrayIndex);
		// only one thing in the array
		size--;
		array.remove(incumbent, ix);
		if(array.size() == 0)
		{
			nodes.set(arrayIndex, null);
			if (firstOpen == arrayIndex) {
				for (int i = firstOpen + 1; i < nodes.size(); i++) {
					if (nodes.get(i) != null) {
						firstOpen = i;
						assert (!nodes.get(i).isEmpty());
					}
				}
			}
		}
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
