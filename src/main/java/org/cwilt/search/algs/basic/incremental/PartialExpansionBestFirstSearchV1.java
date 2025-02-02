package org.cwilt.search.algs.basic.incremental;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchNode;
import org.cwilt.search.search.SearchProblem;
import org.cwilt.search.search.SearchState;
import org.cwilt.search.search.Solution;
import org.cwilt.search.utils.basic.Heapable;

public abstract class PartialExpansionBestFirstSearchV1 extends SearchAlgorithm {

	protected final HashMap<Object, SearchNode> closed;
	/**
	 * Children must define open
	 */
	protected Queue<SearchNode> open;
	
	public PartialExpansionBestFirstSearchV1(SearchProblem prob, Limit l) {
		super(prob, l);
		closed = new HashMap<Object, SearchNode>();
	}

	@Override
	public void reset() {
		closed.clear();
		open.clear();
	}

	@Override
	protected void cleanup() {
		open.clear();
		closed.clear();
	}

	
	@Override
	public void printExtraData(PrintStream ps){
		super.printExtraData(ps);
	}


	@Override
	public ArrayList<SearchState> solve() {
		SearchNode i = SearchNode.makeInitial(initial);
		open.add(i);
		closed.put(i.getState().getKey(), i);

		try {
			setIncumbent();
		} catch (OutOfMemoryError e) {
			l.setOutOfMemory();
			l.endClock();
			open.clear();
			closed.clear();
		}
		if (getIncumbent() == null)
			return null;
		else {
			// System.err.println(incumbent.getGoal().printParents());
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			return finalPath;
		}
	}

	protected double evaluateNode(SearchNode n){
		return n.getF();
	}
	
	protected Solution processNode(SearchNode current) {
		
		if (current.getState().isGoal()) {
			if (getIncumbent() == null
					|| getIncumbent().getCost() > current.getG())
				return new Solution(current, current.getG(), l.getDuration(),
						current.pathLength(), l.getExpansions(),
						l.getGenerations(), l.getDuplicates());
		} else {
			SearchNode next = current.partialExpand();
			if(next == null) {
				l.incrExp();
				// done - this node has no more children.
				return null;
			} else {
				// not done - put this back on open.
				this.open.add(current);
				l.incrGen();
			}
			considerChild(next);
		}
		return null;
	}
	
	

	public void considerChild(SearchNode child) {
		// look for the child in the hash table
		SearchNode incumbentNode = closed.get(child.getState().getKey());
		if (incumbentNode == null) {
			open.add(child);
			closed.put(child.getState().getKey(), child);
		} else if (child.getG() < incumbentNode.getG()) {
			int ix = incumbentNode.getHeapIndex();
			if (ix != Heapable.NO_POS) {
				assert (open.contains(incumbentNode));

				boolean r = open.remove(incumbentNode);
				assert (incumbentNode.getHeapIndex() == Heapable.NO_POS);

				if (!r) {
					r = open.remove(incumbentNode);
					assert (r);
				}
			}
			open.add(child);
			SearchNode r = closed.remove(incumbentNode.getState().getKey());
			//have to put this back on the closed list
			closed.put(child.getState().getKey(), child);
			assert (r.getHeapIndex() == Heapable.NO_POS);
			assert (r != null);
		}
		if (incumbentNode != null)
			l.incrDup();
	}
	
	protected abstract boolean solutionGoodEnough();
	
	protected void setIncumbent() {
		l.startClock();
		Solution goal = null;
		while (!open.isEmpty() && goal == null && l.keepGoing() && !solutionGoodEnough()) {
			SearchNode current = open.poll();
			assert (current != null);
			assert(current.getHeapIndex() == Heapable.NO_POS);
			
			goal = processNode(current);
//			boolean cc = closedCheck();
//			if(!cc){
//				System.err.println(current);
//				assert(false);
//			}
		}
		l.endClock();
		if (goal != null)
			setIncumbent(goal);
	}
}
