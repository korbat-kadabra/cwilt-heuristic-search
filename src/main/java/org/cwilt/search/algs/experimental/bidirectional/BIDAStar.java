package org.cwilt.search.algs.experimental.bidirectional;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.basic.MinHeap;
/**
 * Bidirectional IDAStar. Does a backwards search, and the goal condition is
 * hitting one of the nodes stored in the cache, or running into the goal.
 * 
 * Nodes are expanded backwards in g-order.
 * 
 * @author chris
 * 
 */

public class BIDAStar extends SearchAlgorithm {

	private double shortcutLength;
	protected final HashMap<Object, SearchNode> cache;
	protected final int cacheSize;
	protected double bestRejectedF;

	public BIDAStar(SearchProblem prob, Limit l, int cacheSize) {
		super(prob, l);
		this.cacheSize = cacheSize;
		cache = new HashMap<Object, SearchNode>();
	}

	@Override
	protected void cleanup() {
		bestRejectedF = 0;
		cache.clear();
	}

	@Override
	public SearchAlgorithm clone() {
		return new BIDAStar(prob, l.clone(), cacheSize);
	}

	@Override
	public void reset() {
		bestRejectedF = 0;
		cache.clear();
	}

	protected SearchNode blobHit;

	protected boolean isGoal(SearchNode n) {
		if (n.getState().isGoal())
			return true;
		else if (cache.containsKey(n.getState().getKey())) {
			shortcutLength = n.getH();
			return true;
		} else
			return false;
	}

	protected void doIteration(double bound, SearchNode n) {
		if (n.getF() > bound) {
			if (n.getF() < bestRejectedF)
				bestRejectedF = n.getF();
		} else {
			if (n.getState().isGoal()) {
				setIncumbent ( new Solution(n, n.getG(), l.getDuration(),
						n.pathLength(), l.getExpansions(), l.getGenerations(),
						l.getDuplicates()));
				return;
			} else if (isGoal(n)) {
				blobHit = cache.get(n.getState().getKey());
				assert(blobHit != null);
				if (getIncumbent() == null
						|| getIncumbent().getCost() > n.getG() + blobHit.getG()) {
					setIncumbent ( new Solution(n, n.getG() + blobHit.getG(),
							l.getDuration(), n.pathLength()
									+ blobHit.pathLength() - 1,
							l.getExpansions(), l.getGenerations(),
							l.getDuplicates()));
					return;
				}
			}
			Iterator<? extends SearchNode> i = n.expand().iterator();
			l.incrExp();
			while (getIncumbent() == null && i.hasNext()) {
				l.incrGen();
				doIteration(bound, i.next());
			}
		}
	}

	protected void doIDAStar() {
		double bound = initial.h();
		while (getIncumbent() == null && l.keepGoing()) {
			if (bestRejectedF < Double.MAX_VALUE)
				bound = bestRejectedF;
			bestRejectedF = Double.MAX_VALUE;
			doIteration(bound, SearchNode.makeInitial(initial));
			if (bestRejectedF == Double.MAX_VALUE) {
				l.endClock();
				return;
			}
		}
	}

	protected void setIncumbent() {
		l.startClock();

		SearchNode goal = SearchNode.makeInitial(prob.getGoal());

		MinHeap<SearchNode> gb = new MinHeap<SearchNode>(
				new SearchNode.GComparator());

		gb.insert(goal);

		while (cacheSize > cache.size() && !gb.isEmpty()) {
			SearchNode next = gb.poll();
			if (!cache.containsKey(next.getState().getKey())) {
				cache.put(next.getState().getKey(), next);
				l.incrExp();
				ArrayList<? extends SearchNode> children = next.reverseExpand();
				l.incrGen(children.size());
				for (SearchNode child : children) {
					if (!cache.containsKey(child.getState().getKey())) {
						gb.insert(child);
					}
				}
			}
		}

		doIDAStar();
		l.endClock();
	}

	@Override
	public ArrayList<SearchState> solve() {
		try {
			setIncumbent();
		} catch (OutOfMemoryError e) {
			l.endClock();
			l.setOutOfMemory();
		}
		if (getIncumbent() == null)
			return null;
		else {
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			return finalPath;
		}
	}

	@Override
	public SearchState findFirstGoal() {
		throw new org.cwilt.search.utils.basic.NotImplementedException();
	}

	@Override
	public void printExtraData(PrintStream ps) {
		SearchAlgorithm.printPair(ps, "shortcut length", shortcutLength);
		SearchAlgorithm.printPair(ps, "cache size", cache.size());
	}
}
