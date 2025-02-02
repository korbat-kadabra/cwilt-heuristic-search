package org.cwilt.search.algs.experimental.bidirectional;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;
/**
 * This search algorithm constructs a goal blob consisting only of nodes where
 * h=h*. This means that a cache hit on expansion can terminate immediately with
 * the optimal solution.
 * 
 * @author chris
 * 
 */

public class BHStarIDAStar extends BIDAStar {

	public BHStarIDAStar(SearchProblem prob, Limit l, int cacheSize) {
		super(prob, l, cacheSize);
	}

	public BHStarIDAStar clone() {
		return new BHStarIDAStar(prob, l.clone(), cacheSize);
	}

	private static final boolean closeEnough(double d1, double d2) {
		return Math.abs(d1 - d2) < 0.001;
	}

	protected void setIncumbent() {
		l.startClock();

		SearchNode goal = SearchNode.makeInitial(prob.getGoal());

		Queue<SearchNode> gb = new LinkedList<SearchNode>();

		gb.add(goal);

		while (cacheSize > cache.size() && !gb.isEmpty()) {
			SearchNode next = gb.poll();
			if (!cache.containsKey(next.getState().getKey())
					&& closeEnough(next.getG(), next.getH())) {
				cache.put(next.getState().getKey(), next);
				l.incrExp();
				ArrayList<? extends SearchNode> children = next.reverseExpand();
				l.incrGen(children.size());
				for (SearchNode child : children) {
					if (!cache.containsKey(child.getState().getKey())) {
						gb.add(child);
					}
				}
			}
		}

		doIDAStar();
		l.endClock();
	}

}
