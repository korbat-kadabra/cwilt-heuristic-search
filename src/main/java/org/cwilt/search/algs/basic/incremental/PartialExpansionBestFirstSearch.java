package org.cwilt.search.algs.basic.incremental;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchNode;
import org.cwilt.search.search.SearchProblem;
import org.cwilt.search.search.SearchState;
import org.cwilt.search.search.Solution;
import org.cwilt.search.utils.basic.Heapable;
import org.cwilt.search.utils.basic.MinHeap;

public class PartialExpansionBestFirstSearch extends SearchAlgorithm{
	protected final HashMap<Object, SearchNode> closed;
	protected final MinHeap<SearchNode> open;
	private final Comparator<SearchNode> comparator;
	
	public PartialExpansionBestFirstSearch(SearchProblem prob, Limit l, Comparator<SearchNode> comparator) {
		super(prob, l);
		this.comparator = comparator;
		this.open = new MinHeap<SearchNode>(this.comparator);
		this.closed = new HashMap<Object, SearchNode>();
	}

	@Override
	public void reset() {
		this.open.clear();
		this.closed.clear();
	}

	@Override
	protected void cleanup() {
		this.open.clear();
		this.closed.clear();
	}

	@Override
	public SearchAlgorithm clone() {
		return new PartialExpansionBestFirstSearch(prob, l, comparator);
	}
	
	private double lastExpandedH = Double.MAX_VALUE;
	
	@Override
	public ArrayList<SearchState> solve() {
		l.startClock();
		SearchNode i = SearchNode.makeInitial(initial);
		open.add(i);
		closed.put(i.getState().getKey(), i);
		
		while(true) {
			if(!super.l.keepGoing()) {
				// give up if it is time to give up...
				break;
			}
			SearchNode next = open.poll();
			
			
			if(next == null) {
				// well done searching... if there is an incumbent, return it.
				Solution solution = super.getIncumbent();
				if(solution != null) {
					l.endClock();
					return solution.reconstructPath();
				} else {
					l.endClock();
					return null;
				}
			}

			double nexth = next.getH();
			double nextf = next.getF();

			LogManager.getLogger().trace("expanding node {} with f = {}, h = {}", System.identityHashCode(next), nextf, nexth);
			if(nexth > this.lastExpandedH) {
				LogManager.getLogger().info("NOT GOOD - heuristic is increasing from {} to {} f = {}!!!", this.lastExpandedH, nexth, nextf);
			}
			this.lastExpandedH = nexth;
			
			// check the goal
			if(next.getState().isGoal()) {
				// yay we're done!!!
				super.setIncumbent(next);
				// see if the incumbent is better than the current head of open...
				// if there is no head of open, we're done - nothing left to expand, and we found a goal.
				if(open.isEmpty()) {
					l.endClock();
					return super.getIncumbent().reconstructPath();
				}
				SearchNode incumbent = super.getIncumbent().getGoal();
				// compare the incumbent to the head of the open list using the comparator
				SearchNode headOpen = open.peek();
				if(this.comparator.compare(headOpen, incumbent) > 0) {
					l.endClock();
					return super.getIncumbent().reconstructPath();
				}
			}
			
			SearchNode nextChild = next.partialExpand();
			if(nextChild == null) {
				// this means we're done expanding the node - yay!
				l.incrExp();
				continue;
			}
			SearchNode incumbent = closed.get(nextChild.getState().getKey());
			l.incrGen();
			if(incumbent != null) {
				l.incrDup();
			}
			
			if(incumbent == null) {
				closed.put(nextChild.getState().getKey(), nextChild);
				open.add(nextChild);
			} else if (nextChild.getG() < incumbent.getG()) {
				// replace the incumbent with the child
				if(incumbent.getHeapIndex() == Heapable.NO_POS) {
					// looks like the incumbent isn't eveen there... gotta re-expand it.
					l.incrReExp();
				} else {
					open.removeAt(incumbent.getHeapIndex());
				}
				open.add(nextChild);
				closed.put(nextChild.getState().getKey(), nextChild);
			}
			// because we are doing partial expansion, put the child back in the queue...
			open.add(next);
		}
		l.endClock();
		return null;
	}
	
}
