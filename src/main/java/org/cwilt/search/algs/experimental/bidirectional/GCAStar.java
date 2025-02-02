package org.cwilt.search.algs.experimental.bidirectional;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.basic.MinHeap;
/**
 * This algorithm builds a cache around the goal node consisting of all nodes
 * where there is no heuristic error.
 * 
 * @author cmo66
 * 
 */

public class GCAStar extends org.cwilt.search.algs.basic.bestfirst.BestFirstSearch {

	private final HashMap<Object, SearchNode> goalCell;
	private double shortcutLength;

	public GCAStar(SearchProblem prob, Limit l) {
		super(prob, l);
		this.goalCell = new HashMap<Object, SearchNode>();
		this.open = new MinHeap<SearchNode>(new SearchNode.FGComparator());
	}

	private static final boolean closeEnough(double d1, double d2) {
		return Math.abs(d1 - d2) < 0.001;
	}

	protected boolean solutionGoodEnough(){
		Solution inc = super.getIncumbent();
		if(inc == null)
			return false;
		if(inc.getCost() <= open.peek().getF())
			return true;
		else
			return false;
	}

	
	protected void setIncumbent() {
		l.startClock();

		ArrayList<SearchState> goals = prob.getGoals();

		Queue<SearchNode> gb = new LinkedList<SearchNode>();
		HashMap<Object, SearchNode> onOpen = new HashMap<Object, SearchNode>();

		for (SearchState goalState : goals) {
			SearchNode goal = SearchNode.makeInitial(goalState);
			gb.add(goal);

		}

		while (!gb.isEmpty()) {
			SearchNode next = gb.poll();
			SearchNode incumbent = goalCell.get(next.getState().getKey());
			if (incumbent == null || incumbent.getF() > next.getF()) {
				goalCell.put(next.getState().getKey(), next);
				ArrayList<? extends SearchNode> children = next.expand();
				l.incrExp();
				l.incrGen(children.size());
				for (SearchNode child : children) {
					if (!onOpen.containsKey(child.getState().getKey()))
						if (closeEnough(child.getH(), child.getG()))
							gb.add(child);
				}
			}
		}

		Solution goalSol = null;
		while (!open.isEmpty() && goalSol == null && l.keepGoing()) {
			SearchNode current = open.poll();
			goalSol = processNode(current);
		}
		l.endClock();
		if (goalSol != null)
			setIncumbent(goalSol);
	}

	@Override
	protected void cleanup() {
		super.cleanup();
		goalCell.clear();
	}

	@Override
	public GCAStar clone() {
		return new GCAStar(prob, l);
	}

	@Override
	public void reset() {
		super.reset();
		goalCell.clear();
	}

	public boolean isGoal(SearchNode n) {
		if (n.getState().isGoal())
			return true;
		else if (goalCell.containsKey(n.getState().getKey())) {
			shortcutLength = n.getH();
			return true;
		} else
			return false;
	}

	protected Solution processNode(SearchNode current) {
		if (current.getState().isGoal()) {
			if (getIncumbent() == null || getIncumbent().getCost() > current.getG())
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
				if (child.getState().isGoal()) {
					setIncumbent ( new Solution(child, child.getG(),
							l.getDuration(), child.pathLength(),
							l.getExpansions(), l.getGenerations(),
							l.getDuplicates()));
					return getIncumbent();
				} else if (isGoal(child)) {
					SearchNode blobHit = goalCell
							.get(child.getState().getKey());
					if (getIncumbent() == null
							|| getIncumbent().getCost() > child.getG()
									+ blobHit.getG()) {
						setIncumbent(new Solution(child, child.getG()
								+ blobHit.getG(), l.getDuration(),
								child.pathLength() + blobHit.pathLength() - 1,
								l.getExpansions(), l.getGenerations(),
								l.getDuplicates()));
						return getIncumbent();
					}
				}
				considerChild(child);
			}
		}
		return null;
	}

	@Override
	public void printExtraData(PrintStream ps) {
		SearchAlgorithm.printPair(ps, "shortcut length", shortcutLength);
		SearchAlgorithm.printPair(ps, "cache size", goalCell.size());
	}

}
