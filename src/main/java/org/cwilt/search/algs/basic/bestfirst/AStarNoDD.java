package org.cwilt.search.algs.basic.bestfirst;
import java.util.ArrayList;
import java.util.Iterator;

import org.cwilt.search.algs.basic.bestfirst.MultistartAStar.MultistartSearchProblem;
import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.basic.Heapable;import org.cwilt.search.utils.basic.MinHeap;
public class AStarNoDD extends org.cwilt.search.search.SearchAlgorithm {
	private final MinHeap<SearchNode> open;

	public AStarNoDD(SearchProblem prob, Limit l) {
		super(prob, l);
		this.open = new MinHeap<SearchNode>(new SearchNode.FGComparator());
	}

	@Override
	public void reset() {
		this.open.clear();
	}

	@Override
	protected void cleanup() {
		this.open.clear();
	}

	@Override
	public SearchAlgorithm clone() {
		return new AStarNoDD(prob, l.clone());
	}

	protected void setIncumbent() {
		l.startClock();
		Solution goal = null;
		while (!open.isEmpty() && goal == null && l.keepGoing()) {
			SearchNode current = open.poll();
			assert (current != null);
			assert (current.getHeapIndex() == Heapable.NO_POS);
			goal = processNode(current);
		}
		l.endClock();
		if (goal != null)
			setIncumbent(goal);
	}

	protected Solution processNode(SearchNode current) {
		if (current.getState().isGoal()) {
			if (getIncumbent() == null
					|| getIncumbent().getCost() > current.getG())
				return new Solution(current, current.getG(), l.getDuration(),
						current.pathLength(), l.getExpansions(),
						l.getGenerations(), l.getDuplicates());
		} else {
			ArrayList<? extends SearchNode> children = current.expand();
			l.incrExp();
			Iterator<? extends SearchNode> childIter = children.iterator();
			while (childIter.hasNext()) {
				l.incrGen();
				SearchNode child = childIter.next();
				if (!current.getState().getKey()
						.equals(child.getState().getKey()))
					open.add(child);
				else
					l.incrDup();
			}
		}
		return null;
	}

	@Override
	public ArrayList<SearchState> solve() {
		if (prob instanceof MultistartAStar.MultistartSearchProblem) {
			for (SearchState initial : ((MultistartSearchProblem) prob)
					.getStarts()) {
				SearchNode i = SearchNode.makeInitial(initial);
				open.add(i);
			}
		} else {
			SearchNode i = SearchNode.makeInitial(initial);
			open.add(i);
		}

		try {
			setIncumbent();
		} catch (OutOfMemoryError e) {
			l.setOutOfMemory();
			l.endClock();
			open.clear();
		}
		if (getIncumbent() == null)
			return null;
		else {
			// System.err.println(incumbent.getGoal().printParents());
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			return finalPath;
		}

	}

}
