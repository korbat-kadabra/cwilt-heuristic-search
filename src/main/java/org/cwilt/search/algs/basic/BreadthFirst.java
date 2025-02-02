/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.algs.basic;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Queue;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;
public class BreadthFirst extends SearchAlgorithm {

	public BreadthFirst(org.cwilt.search.search.SearchProblem initial, Limit l) {
		super(initial, l);
		this.queue = new ArrayDeque<SearchNode>();
		this.closed = new HashMap<Object, SearchNode>();
	}

	private final Queue<SearchNode> queue;
	private final HashMap<Object, SearchNode> closed;

	@Override
	public SearchAlgorithm clone() {
		assert (queue.isEmpty());
		if (!queue.isEmpty()) {
			System.err.println("shouldn't clone a non empty algorithm");
			System.exit(1);
		}
		if (!closed.isEmpty()) {
			System.err.println("shouldn't clone a non empty algorithm");
			System.exit(1);
		}
		return new BreadthFirst(prob, l.clone());
	}

	@Override
	public SearchState findFirstGoal() {
		if (queue.isEmpty()) {
			SearchNode i = SearchNode.makeInitial(initial);
			queue.add(i);
			closed.put(i.getState().getKey(), i);
		}
		this.setIncumbent();
		if (getIncumbent() != null)
			return getIncumbent().getGoal().getState();
		else
			return null;
	}

	@Override
	public void reset() {
		queue.clear();
		closed.clear();
	}

	public void considerChild(SearchNode child) {
		// look for the child in the hash table
		SearchNode incumbent = closed.get(child.getState().getKey());
		if (incumbent == null) {
			queue.add(child);
			closed.put(child.getState().getKey(), child);
		}
		if (incumbent != null)
			l.incrDup();
	}

	private void setIncumbent() {
		l.startClock();
		Solution goal = null;
		while (!queue.isEmpty() && goal == null && l.keepGoing()) {
			SearchNode current = queue.poll();
			if (current.getState().isGoal()) {
				if (getIncumbent() == null || getIncumbent().getCost() > current.getG())
					goal = new Solution(current, current.getG(), l
							.getDuration(), current.pathLength(), l
							.getExpansions(), l.getGenerations(), l
							.getDuplicates());
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
		l.endClock();
		if (goal != null)
			setIncumbent(goal);
	}

	@Override
	public ArrayList<SearchState> solve() {
		SearchNode i = SearchNode.makeInitial(initial);
		queue.add(i);
		closed.put(i.getState().getKey(), i);

		try {
			setIncumbent();
		} catch (OutOfMemoryError e) {
			l.endClock();
			l.setOutOfMemory();
			queue.clear();
			closed.clear();
		}
		if (getIncumbent() == null)
			return null;
		else {
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			return finalPath;
		}
	}

	protected void cleanup(){
		queue.clear();
		closed.clear();
	}
}
