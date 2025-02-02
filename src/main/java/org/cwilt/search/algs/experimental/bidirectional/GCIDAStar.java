package org.cwilt.search.algs.experimental.bidirectional;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.Solution;
/**
 * 
 * This search algorithm is suboptimal, checks for goals on generation (and not
 * expansion) and is therefore capable of returning suboptimal solutions.
 * 
 * @author cmo66
 * 
 */

public class GCIDAStar extends BIDAStar {

	public GCIDAStar(SearchProblem prob, Limit l) {
		super(prob, l, Integer.MAX_VALUE);
	}

	public GCIDAStar clone() {
		return new GCIDAStar(prob, l.clone());
	}

	protected void doIteration(double bound, SearchNode n) {
		if (n.getState().isGoal()) {
			setIncumbent (new Solution(n, n.getG(), l.getDuration(),
					n.pathLength(), l.getExpansions(), l.getGenerations(),
					l.getDuplicates()));
			return;
		} else if (isGoal(n)) {
			blobHit = cache.get(n.getState().getKey());
			if (getIncumbent() == null
					|| getIncumbent().getCost() > n.getG() + blobHit.getG()) {
				setIncumbent ( new Solution(n, n.getG() + blobHit.getG(),
						l.getDuration(), n.pathLength() + blobHit.pathLength()
								- 1, l.getExpansions(), l.getGenerations(),
						l.getDuplicates()));
				return;
			}
		} else if (n.getF() > bound) {
			if (n.getF() < bestRejectedF)
				bestRejectedF = n.getF();
		} else {
			Iterator<? extends SearchNode> i = n.expand().iterator();
			l.incrExp();
			while (getIncumbent() == null && i.hasNext()) {
				l.incrGen();
				doIteration(bound, i.next());
			}
		}
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
