package org.cwilt.search.algs.experimental.bidirectional;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.basic.Heapable;import org.cwilt.search.utils.basic.MinHeap;
/**
 * 
 * @author cmo66
 * 
 */

public class HAddIDAStar extends org.cwilt.search.search.SearchAlgorithm {

	public static class WHAComp implements Comparator<HAddSearchNode> {
		private final double weight;

		public WHAComp(double wt) {
			this.weight = wt;
		}

		@Override
		public int compare(HAddSearchNode arg0, HAddSearchNode arg1) {
			double arg0wf = (arg0.getF() - arg0.getG()) * weight + arg0.getG();
			double arg1wf = (arg1.getF() - arg1.getG()) * weight + arg1.getG();
			if (arg0wf == arg1wf) {
				if (arg0.equals(arg1))
					return 0;
				else {
					if (arg0.getG() > arg1.getG())
						return -1;
					else if (arg0.getG() < arg1.getG())
						return 1;
					else
						return 0;
					// return arg0.getState().lexOrder(arg1.getState());
				}
			} else if (arg0wf < arg1wf)
				return -1;
			else
				return 1;
		}
	}

	public static class HAComp implements Comparator<HAddSearchNode> {
		@Override
		public int compare(HAddSearchNode arg0, HAddSearchNode arg1) {
			if (arg0.getF() == arg1.getF()) {
				if (arg0.equals(arg1))
					return 0;
				else {
					if (arg0.getG() > arg1.getG())
						return -1;
					else if (arg0.getG() < arg1.getG())
						return 1;
					else
						return 0;
					// return arg0.getState().lexOrder(arg1.getState());
				}
			} else if (arg0.getF() < arg1.getF())
				return -1;
			else
				return 1;
		}
	}

	public static final class HErrComparator implements
			Comparator<HAddSearchNode> {
		@Override
		public int compare(HAddSearchNode o1, HAddSearchNode o2) {
			double hErr1 = o1.getG() - o1.getH();
			double hErr2 = o2.getG() - o2.getH();
			assert (hErr1 >= -.000000001);
			assert (hErr2 >= -.000000001);
			if (hErr1 < hErr2)
				return -1;
			else if (hErr1 > hErr2)
				return 1;
			else
				return 0;
		}
	}

	protected final HashMap<Object, HAddSearchNode> closed;
	protected final MinHeap<HAddSearchNode> gb;
	protected final HashMap<Object, HAddSearchNode> borderNodes;
	protected double hAdd;
	protected int cacheSize;

	private final int initialCacheSize;

	protected HAddIDAStar(SearchProblem prob, Limit l, int isz,
			Comparator<HAddSearchNode> c) {
		super(prob, l);
		gb = new MinHeap<HAddSearchNode>(getComparator());
		// gb = new MinHeap<SearchNode>(new SearchNode.GComparator());
		// onOpen = new HashMap<Object, HAddSearchNode>();
		borderNodes = new HashMap<Object, HAddSearchNode>();
		this.closed = new HashMap<Object, HAddSearchNode>();
		this.initialCacheSize = isz;
	}

	public HAddIDAStar(SearchProblem prob, Limit l, int isz) {
		super(prob, l);
		gb = new MinHeap<HAddSearchNode>(getComparator());
		// gb = new MinHeap<SearchNode>(new SearchNode.GComparator());
		// onOpen = new HashMap<Object, HAddSearchNode>();
		borderNodes = new HashMap<Object, HAddSearchNode>();
		this.closed = new HashMap<Object, HAddSearchNode>();
		this.initialCacheSize = isz;
	}

	protected Comparator<HAddSearchNode> getComparator() {
		return new HErrComparator();
	}

	@Override
	public void printExtraData(PrintStream ps) {
		SearchAlgorithm.printPair(ps, "shortcut length", shortcutLength);
		SearchAlgorithm.printPair(ps, "hAdd", hAdd);
		SearchAlgorithm.printPair(ps, "cache size", cacheSize);
	}

	public HAddSearchNode makeInitialForwards(SearchState s) {
		HAddSearchNode n = this.new HAddSearchNode(null, s, 0, s.h(), s.h()
				* -1, REASON.EXP);
		return n;
	}

	public HAddSearchNode makeInitialReverse(SearchState s) {
		HAddSearchNode n = this.new HAddSearchNode(null, s, 0, s.h(), s.h()
				* -1, REASON.CACHE);
		return n;
	}

	private double shortcutLength;

	protected static enum REASON {
		CACHE, EXP;
	}

	/**
	 * Search nodes for this algorithm to use, needs to be a subclass so it can
	 * handle the special h values from this algpotithm.
	 * 
	 * @author cmo66
	 * 
	 */
	protected class HAddSearchNode extends org.cwilt.search.search.SearchNode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 963369607822118051L;
		protected final REASON r;

		/**
		 * Original heuristic for this node (if negative) or the h* value (if
		 * positive).
		 */
		private final double originalH;

		protected HAddSearchNode(SearchNode parent, SearchState s, double g,
				double f, double originalH, REASON r) {
			super(parent, s, g, f);
			this.originalH = originalH;
			this.r = r;
		}

		public double getF() {
			if (this.originalH < 0) {
				return this.getG() + (this.originalH * -1);
			} else {
				return this.getG() + originalH;
			}
		}

		public double getH() {
			if (this.originalH < 0) {
				return (this.originalH * -1);
			} else {
				return originalH;
			}
		}

		public ArrayList<? extends HAddSearchNode> reverseExpand() {
			ArrayList<SearchState.Child> baseChildren = s.reverseExpand();
			ArrayList<HAddSearchNode> children = new ArrayList<HAddSearchNode>();
			Iterator<SearchState.Child> it = baseChildren.iterator();
			while (it.hasNext()) {
				SearchState.Child c = it.next();
				double childH = c.child.h();
				double childF = c.transitionCost + g + childH;
				// children expanded in the backwards direction need CACHE for
				// their reason
				children.add(new HAddSearchNode(this, c.child, c.transitionCost
						+ g, childF, childH, REASON.CACHE));
			}
			return children;
		}

		public ArrayList<? extends HAddSearchNode> expand() {
			ArrayList<SearchState.Child> baseChildren = s.expand();
			ArrayList<HAddSearchNode> children = new ArrayList<HAddSearchNode>();
			Iterator<SearchState.Child> it = baseChildren.iterator();
			while (it.hasNext()) {
				SearchState.Child c = it.next();
				double childH;
				HAddSearchNode cn = HAddIDAStar.this.closed.get(c.child
						.getKey());
				SearchNode childIncumbent = null;
				if (cn != null && cn.r == REASON.CACHE) {
					childIncumbent = cn;
				}
				if (childIncumbent != null)
					childH = childIncumbent.getG();
				else
					childH = (c.child.h() + HAddIDAStar.this.hAdd) * -1;
				double childF = c.transitionCost + g + childH;
				// children in the forward direction need EXP for their reason
				children.add(new HAddSearchNode(this, c.child, c.transitionCost
						+ g, childF, childH, REASON.EXP));
			}
			return children;
		}

		public String toString() {
			return super.toString() + r.toString() + "\n";
		}
	}

	private boolean checkBorderNodes() {

		Iterator<Map.Entry<Object, HAddSearchNode>> i = borderNodes.entrySet()
				.iterator();

		while (i.hasNext()) {
			Map.Entry<Object, HAddSearchNode> next = i.next();
			assert (next.getValue().getHeapIndex() != Heapable.NO_POS);
		}

		return true;
	}

	protected void increaseCache(int incrAmount) {
		int stop = incrAmount + cacheSize;
		while (!gb.isEmpty() && cacheSize < stop) {
			assert (checkBorderNodes());
			HAddSearchNode next = gb.poll();
			borderNodes.remove(next.getState().getKey());
			HAddSearchNode c = closed.get(next.getState().getKey());
			HAddSearchNode incumbent = null;
			if (c != null) {
				// if this node is already in the cache, don't have do do
				// anything with it.
				if (c.r == REASON.CACHE) {
					continue;
				} else {
					// this node was on the open list
					// node wasn't on the open list of THIS search.
				}
			}

			if (incumbent == null || incumbent.getF() > next.getF()) {
				assert (checkBorderNodes());
				closed.put(next.getState().getKey(), next);

				// System.err.println(gb.size());
				// System.err.println(next);
				cacheSize++;
				ArrayList<? extends HAddSearchNode> children = next
						.reverseExpand();
				l.incrExp();
				l.incrGen(children.size());
				for (HAddSearchNode child : children) {
					// previous backwards generation of this node
					HAddSearchNode prevGen = borderNodes.get(child.getState()
							.getKey());
					assert (prevGen == null || prevGen.getHeapIndex() != Heapable.NO_POS);

					assert (prevGen == null || prevGen.r == REASON.CACHE);
					// copy of this from the closed list
					HAddSearchNode prevClosed = closed.get(child.getState()
							.getKey());
					if (prevClosed != null && prevClosed.r == REASON.CACHE) {
						// this node has already been expanded backwards
						continue;
					}
					if (prevGen == null) {
						borderNodes.put(child.getState().getKey(), child);
						assert (child.getG() >= child.getH() - 0.00000000000001);
						boolean added = gb.add(child);
						assert (added);
					} else if (prevGen.getG() > child.getG()) {
						// duplicate?
						borderNodes.remove(child.getState().getKey());
						if (prevGen.getHeapIndex() == Heapable.NO_POS) {
							assert (false);
						}
						gb.remove(prevGen);
						boolean added = gb.add(child);
						assert (added);
						borderNodes.put(child.getState().getKey(), child);
					}
				}
			}
		}
		assert (!borderNodes.isEmpty());

		// ArrayList<SearchNode> border = new ArrayList<SearchNode>();

		/**
		 * The difference here is that all of the children are in a hash table,
		 * as opposed to being in their original arraylists.
		 */
		// Iterator<Map.Entry<Object, HAddSearchNode>> i =
		// borderNodes.entrySet()
		// .iterator();
		//
		// double oldHAdd = hAdd;
		// hAdd = Double.MAX_VALUE;
		// while (i.hasNext()) {
		// Map.Entry<Object, HAddSearchNode> next = i.next();
		// SearchNode n = next.getValue();
		// HAddSearchNode cn = closed.get(n.getState().getKey());
		//
		// if (cn == null || cn.r == REASON.EXP) {
		// //border.add(n);
		// double hErr = n.getG() - n.getH();
		// assert (hErr >= -0.000000001);
		// if (hErr < hAdd) {
		// if (hAdd < oldHAdd) {
		// assert(hAdd > -0.00000000001);
		// System.err.printf("old hAdd: %.20f new hAdd: %.20f\n",
		// oldHAdd, hAdd);
		// }
		// hAdd = hErr;
		// if(hAdd < oldHAdd){
		// assert (Math.abs(hAdd - oldHAdd) < 0.0000000001);
		// hAdd = oldHAdd;
		// } else {
		// }
		// }
		// } else {
		// // this will help, but not completely sure if it valid.
		// // Certainly seems reasonable.
		// i.remove();
		// }
		// }

		assert (hAdd != Double.MAX_VALUE);
		hAdd = gb.peek().getG() - gb.peek().getH();
		assert (hAdd == gb.peek().getG() - gb.peek().getH());
		// if (hAdd > oldHAdd) {
		// open.reHeapify();
		// }

	}

	protected boolean keepGoing() {
		if (!l.keepGoing())
			return false;
		if (getIncumbent() == null)
			return true;
		else {

			if (bound >= getIncumbent().getCost()) {
				return false;
			} else
				return true;
		}
	}

	public ArrayList<SearchState> solve() {
		try {
			setIncumbent();
		} catch (OutOfMemoryError e) {
			l.setOutOfMemory();
			l.endClock();
			closed.clear();
		}
		if (getIncumbent() == null)
			return null;
		else {
			// System.err.println(incumbent.getGoal().printParents());
			return null;
		}
	}

	@Override
	public void reset() {
		closed.clear();
	}

	@Override
	protected void cleanup() {
		closed.clear();
	}

	protected double bound;
	protected double nextBound;

	@Override
	public SearchAlgorithm clone() {
		return new HAddAStar(prob, l.clone(), initialCacheSize);
	}

	protected void doIteration(SearchState current, int currentID,
			double currentG, int depth) {
		assert (currentG <= bound);
		if (!l.keepGoing())
			return;

		if (super.getIncumbent() != null)
			return;

		if (current.isGoal()) {
			this.setIncumbent(new Solution(null, currentG, l.getDuration(),
					depth, l.getExpansions(), l.getGenerations(), l
							.getDuplicates()));
			return;
		}
		// current hit the cache
		
		int nChildren = current.nChildren();
		l.incrExp();

		for (int i = 0; i < nChildren && l.keepGoing(); i++) {
			if (super.getIncumbent() != null)
				break;
			int inverse = current.inverseChild(i);
			// don't do the inverse
			if (inverse == currentID) {
				continue;
			}
			double operatorCost = current.convertToChild(i, currentID);
			if (operatorCost < 0)
				continue;

			l.incrGen();

			HAddSearchNode cn = closed.get(current.getKey());

			
			double currentF = operatorCost + currentG;
			
			double childHeuristic;
			if(cn == null)
				childHeuristic = current.h();
			else
			{
				childHeuristic = cn.getG();
			}
			currentF = currentF + childHeuristic;
			
			if(currentF <= bound && cn != null){
				//hit the goal
				this.setIncumbent(new Solution(null, currentF, l.getDuration(),
						depth, l.getExpansions(), l.getGenerations(), l
								.getDuplicates()));
			}
			
			// outside the bound
			if (currentF > bound) {
				// just do the inverse
				current.convertToChild(inverse, -1);

				if (currentF < nextBound)
					nextBound = currentF;
				continue;
			} else if (super.getIncumbent() == null) {
				doIteration(current, i, currentG + operatorCost, depth + 1);
				// just do the inverse
				current.convertToChild(current.inverseChild(i), -1);
			}
		}
	}

	protected void setIncumbent() {
		l.startClock();

		ArrayList<SearchState> goals = prob.getGoals();

		assert (initialCacheSize >= 0);

		for (SearchState goalState : goals) {
			HAddSearchNode goal = this.makeInitialReverse(goalState);
			gb.add(goal);
			borderNodes.put(goal.getState().getKey(), goal);
		}
		increaseCache(initialCacheSize);

		assert (hAdd != Double.MAX_VALUE);

		SearchState initial = prob.getInitial();
		this.bound = initial.h();
		nextBound = Double.MAX_VALUE;

		while (super.getIncumbent() == null && l.keepGoing()) {
			this.doIteration(initial, -1, 0, 0);
			bound = nextBound;
			nextBound = Double.MAX_VALUE;
		}

		l.endClock();
	}

}
