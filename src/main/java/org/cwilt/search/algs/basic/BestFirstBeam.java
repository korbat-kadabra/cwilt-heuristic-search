package org.cwilt.search.algs.basic;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.basic.Heapable;import org.cwilt.search.utils.basic.MinMaxHeap;
public class BestFirstBeam extends org.cwilt.search.search.SearchAlgorithm {

	private final int beamWidth;
	private final double weight;
	private final MinMaxHeap<SearchNode> open;
	private final HashMap<Object, SearchNode> closed;
	private final Comparator<SearchNode> c;

	public BestFirstBeam(SearchProblem prob, Limit l, int beamWidth,
			double weight) {
		super(prob, l);
		this.beamWidth = beamWidth;
		this.weight = weight;
		this.c = new SearchNode.WFGComparator(this.weight);
		this.open = new MinMaxHeap<SearchNode>(this.c);
		this.closed = new HashMap<Object, SearchNode>();
	}

	@Override
	protected void cleanup() {
		open.clear();
		closed.clear();
	}

	@Override
	public SearchAlgorithm clone() {
		return new BestFirstBeam(prob, l, beamWidth, weight);
	}

	@Override
	public void reset() {
		cleanup();
	}

	private SearchNode getIncumbentNode(SearchNode child) {
		return closed.get(child.getState().getKey());
	}

	private void addToClosed(SearchNode child) {
		closed.put(child.getState().getKey(), child);
	}

	protected boolean processNode(SearchNode child) {
		if (child.getState().isGoal())
			return true;
		// look for the child in the hash table
		SearchNode incumbent = getIncumbentNode(child);
		SearchNode worst = open.peekLargest();

		if (incumbent == null) {
			if (open.size() < beamWidth) {
				// have room
				open.insert(child);
				this.addToClosed(child);
			} else if (c.compare(child, worst) < 0) {
				// don't have room, but want to use this child
				open.popLargest();
				open.insert(child);
				this.addToClosed(child);
			}
		} else if (child.getG() < incumbent.getG()
				&& incumbent.getHeapIndex() != Heapable.NO_POS) {
			// want to use this child, and its predecessor is already in the
			// heap
			open.remove(incumbent.getHeapIndex());
			closed.remove(incumbent);
			open.insert(child);
			this.addToClosed(child);
		} else if (child.getG() < incumbent.getG()
				&& incumbent.getHeapIndex() == Heapable.NO_POS) {
			// want to use this child, and its predecessor isn't in the heap
			if (open.size() < beamWidth) {
				// have room
				open.insert(child);
				this.addToClosed(child);
			} else if (c.compare(child, worst) < 0) {
				// don't have room, but want to use this child
				open.popLargest();
				open.insert(child);
				this.addToClosed(child);
			}
		}
		if (incumbent != null)
			l.incrDup();
		return false;
	}

	@Override
	public ArrayList<SearchState> solve() {
		
		
		open.insert(SearchNode.makeInitial(prob.getInitial()));
		
		l.startClock();
		
		while (!open.isEmpty()) {
			SearchNode next = open.pop();
			l.incrExp();
			for (SearchNode n : next.expand()) {
				l.incrGen();
				if (processNode(n)) {
					setIncumbent(new Solution(n, n.getG(), l
							.getDuration(), n.pathLength(), l
							.getExpansions(), l.getGenerations(), l
							.getDuplicates()));
					l.endClock();
					ArrayList<SearchState> path = n.reconstructPath();
					return path;
				}
			}
		}
		l.endClock();
		return null;
	}

}
