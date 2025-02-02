/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */

package org.cwilt.search.algs.basic;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.basic.Heapable;import org.cwilt.search.utils.basic.MinMaxHeap;
public class Beam extends SearchAlgorithm {
	public void reset() {
		assert (children != null);
		assert (parents != null);
		assert (closed != null);

		children.clear();
		parents.clear();
		closed.clear();
	}

	public SearchAlgorithm clone() {
		super.checkClone(org.cwilt.search.algs.basic.Beam.class.getCanonicalName());
		assert (parents.size() == 0);
		assert (children.size() == 0);
		assert (l.getGenerations() == 0);

		Beam b = new Beam(prob, l.clone(), beamWidth);
		b.c = new SearchNode.FGComparator();
		b.parents = new MinMaxHeap<SearchNode>(c);
		b.children = new MinMaxHeap<SearchNode>(c);
		b.closed = new HashMap<Object, SearchNode>();
		return b;
	}

	protected Comparator<SearchNode> c;
	protected int beamWidth;
	protected MinMaxHeap<SearchNode> parents;
	protected MinMaxHeap<SearchNode> children;
	protected AbstractMap<Object, SearchNode> closed;

	public Beam(org.cwilt.search.search.SearchProblem initial, Limit l, int beamWidth) {
		super(initial, l);
		this.beamWidth = beamWidth;
		this.c = new SearchNode.FGComparator();
		this.parents = new MinMaxHeap<SearchNode>(c);
		this.children = new MinMaxHeap<SearchNode>(c);
		this.closed = new HashMap<Object, SearchNode>();
	}

	protected SearchNode getIncumbentNode(SearchNode child) {
		return closed.get(child.getState().getKey());
	}

	protected void addToClosed(SearchNode child) {
		closed.put(child.getState().getKey(), child);
	}

	protected void considerChild(SearchNode child) {
		// look for the child in the hash table
		SearchNode incumbent = getIncumbentNode(child);
		if (incumbent != null && incumbent.getHeapIndex() == Heapable.NO_POS) {
			// don't check parent
		} else if (incumbent != null) {
			// check if the incumbent is in the parents or the children. If it
			// is in the parents, ignore it.
			if(parents.size() + 1 > incumbent.getHeapIndex()){
				SearchNode parentIncumbent = parents.get(incumbent.getHeapIndex());
				if(parentIncumbent == incumbent)
					incumbent = null;
			}
		}

		SearchNode worst = children.peekLargest();
		if (incumbent == null) {
			if (children.size() < beamWidth) {
				// have room
				children.insert(child);
				this.addToClosed(child);
			} else if (c.compare(child, worst) < 0) {
				// don't have room, but want to use this child
				children.popLargest();
				children.insert(child);
				this.addToClosed(child);
			}
		} else if (child.getG() < incumbent.getG()
				&& incumbent.getHeapIndex() != Heapable.NO_POS) {
			// want to use this child, and its predecessor is already in the
			// heap

			children.remove(incumbent.getHeapIndex());
			closed.remove(incumbent);
			children.insert(child);
			this.addToClosed(child);
		} else if (child.getG() < incumbent.getG()
				&& incumbent.getHeapIndex() == Heapable.NO_POS) {
			// want to use this child, and its predecessor isn't in the heap
			if (children.size() < beamWidth) {
				// have room
				children.insert(child);
				this.addToClosed(child);
			} else if (c.compare(child, worst) < 0) {
				// don't have room, but want to use this child
				children.popLargest();
				children.insert(child);
				this.addToClosed(child);
			}
		}
		if (incumbent != null)
			l.incrDup();
	}

	protected void processLayer() {
		children.clear();
		while (!parents.isEmpty()) {
			SearchNode current = parents.pop();
			if (current.getState().isGoal()) {
				if (getIncumbent() == null
						|| getIncumbent().getCost() > current.getG())
					setIncumbent(new Solution(current, current.getG(),
							l.getDuration(), current.pathLength(),
							l.getExpansions(), l.getGenerations(),
							l.getDuplicates()));
			} else {
				ArrayList<? extends SearchNode> children = current.expand();
				l.incrExp();
				Iterator<? extends SearchNode> childIter = children.iterator();
				while (childIter.hasNext()) {
					l.incrGen();
					considerChild(childIter.next());
				}
			}
		}
		MinMaxHeap<SearchNode> t = parents;
		parents = children;
		children = t;
	}

	protected void setIncumbent() {
		l.startClock();
		while (!parents.isEmpty() && l.keepGoing() && getIncumbent() == null) {
			processLayer();
		}
		l.endClock();
	}

	@Override
	public ArrayList<SearchState> solve() {
		SearchNode i = SearchNode.makeInitial(initial);
		parents.insert(i);
		this.addToClosed(i);

		try {
			setIncumbent();
		} catch (OutOfMemoryError e) {
			l.endClock();
			l.setOutOfMemory();
			this.cleanup();
		}
		if (getIncumbent() == null)
			return null;
		else {
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			return finalPath;
		}
	}

	protected void cleanup() {
		parents.clear();
		children.clear();
		closed.clear();
	}

}
