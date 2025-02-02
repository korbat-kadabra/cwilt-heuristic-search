package org.cwilt.search.algs.basic;
import java.util.ArrayList;
import java.util.HashMap;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.basic.MinHeap;import org.cwilt.search.utils.experimental.BucketQueue;
public class DBFS extends SearchAlgorithm {

	private final HashMap<Object, SearchNode> closed;

	private final double t, p, bucket;

	public DBFS(SearchProblem prob, Limit l, double p, double t) {
		super(prob, l);
		closed = new HashMap<Object, SearchNode>();
		this.t = t;
		this.p = p;
		this.bucket = 1.0;
		this.open = new BucketQueue(bucket, this.p, this.t, id);
	}

	@Override
	protected void cleanup() {
		closed.clear();
	}

	@Override
	public SearchAlgorithm clone() {
		return new DBFS(prob, l.clone(), p, t);
	}

	@Override
	public SearchState findFirstGoal() {
		throw new org.cwilt.search.utils.basic.NotImplementedException();
	}

	@Override
	public void reset() {
		closed.clear();
	}

	private final org.cwilt.search.utils.experimental.BucketQueue open;

	private void setIncumbent() {
		while (!open.isEmpty()) {
			//System.err.println(closed.size());
			SearchNode n = open.fetchOneNode();
			MinHeap<SearchNode> locOL = new MinHeap<SearchNode>(
					new SearchNode.HComparator());
			locOL.insert(n);
			for (double i = 0; i <= n.getH(); i++) {
				if (!locOL.isEmpty()) {
					SearchNode current = locOL.poll();
					assert (current != null);
					if (current.getState().isGoal()) {
						if (getIncumbent() == null
								|| getIncumbent().getCost() > current.getG())
							setIncumbent ( new Solution(current, current.getG(),
									l.getDuration(), current.pathLength(),
									l.getExpansions(), l.getGenerations(),
									l.getDuplicates()));
						return;
					}
					closed.put(current.getState().getKey(), current);
					ArrayList<? extends SearchNode> children = current.expand();
					l.incrExp();
					l.incrGen(children.size());
					for (SearchNode child : children) {
						if (child.getState().isGoal()) {
							if (getIncumbent() == null
									|| getIncumbent().getCost() > child.getG())
								setIncumbent ( new Solution(child, child.getG(),
										l.getDuration(), child.pathLength(),
										l.getExpansions(), l.getGenerations(),
										l.getDuplicates()));
							return;
						}
						
						if (!closed.containsKey(child.getState().getKey()))
							locOL.insert(child);
					}
				} else {
					break;
				}
			}
			open.addAll(locOL);
			locOL.clear();
		}
	}

	@Override
	public ArrayList<SearchState> solve() {
		SearchNode i = SearchNode.makeInitial(initial);
		open.addNode(i);
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

}
