package org.cwilt.search.algs.experimental.bidirectional;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
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

public class HAddAStar extends org.cwilt.search.search.SearchAlgorithm {

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
	public HashMap<Object, HAddSearchNode> getExpanded(){
		return closed;
	}

	protected final MinHeap<HAddSearchNode> open;
	protected final HashMap<Object, HAddSearchNode> closed;
	protected final MinHeap<HAddSearchNode> gb;
	protected final HashMap<Object, HAddSearchNode> borderNodes;
	protected double hAdd;
	protected int cacheSize;

	private final int initialCacheSize;
	private final Comparator<HAddSearchNode> comp;

	protected HAddAStar(SearchProblem prob, Limit l, int isz,
			Comparator<HAddSearchNode> c) {
		super(prob, l);
		this.comp = c;
		gb = new MinHeap<HAddSearchNode>(getComparator());
		// gb = new MinHeap<SearchNode>(new SearchNode.GComparator());
		// onOpen = new HashMap<Object, HAddSearchNode>();
		borderNodes = new HashMap<Object, HAddSearchNode>();
		this.open = new MinHeap<HAddSearchNode>(this.comp);
		this.closed = new HashMap<Object, HAddSearchNode>();
		this.initialCacheSize = isz;
	}

	public HAddAStar(SearchProblem prob, Limit l, int isz) {
		super(prob, l);
		gb = new MinHeap<HAddSearchNode>(getComparator());
		// gb = new MinHeap<SearchNode>(new SearchNode.GComparator());
		// onOpen = new HashMap<Object, HAddSearchNode>();
		borderNodes = new HashMap<Object, HAddSearchNode>();
		this.comp = new HAComp();
		this.open = new MinHeap<HAddSearchNode>(this.comp);
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

	public static enum REASON {
		CACHE, EXP;
	}

	/**
	 * Search nodes for this algorithm to use, needs to be a subclass so it can
	 * handle the special h values from this algpotithm.
	 * 
	 * @author cmo66
	 * 
	 */
	public class HAddSearchNode extends org.cwilt.search.search.SearchNode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 963369607822118051L;
		public final REASON r;

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
				HAddSearchNode cn = HAddAStar.this.closed.get(c.child.getKey());
				SearchNode childIncumbent = null;
				if (cn != null && cn.r == REASON.CACHE) {
					childIncumbent = cn;
				}
				if (childIncumbent != null)
					childH = childIncumbent.getG();
				else
					childH = (c.child.h() + HAddAStar.this.hAdd) * -1;
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

	protected void processNode(HAddSearchNode current) {
		ArrayList<? extends HAddSearchNode> children = current.expand();
		l.incrExp();
		Iterator<? extends HAddSearchNode> childIter = children.iterator();
		while (childIter.hasNext()) {
			l.incrGen();
			HAddSearchNode child = childIter.next();
			considerChild(child);
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
					if (c.getHeapIndex() != Heapable.NO_POS) {
						boolean r = open.remove(c);
						assert (r);
						if (getIncumbent() == null
								|| getIncumbent().getCost() > next.getG()
										+ c.getG()) {
							HAddSearchNode blobHit = next;
							setIncumbent(new Solution(c,
									c.getG() + blobHit.getG(),
									l.getDuration(), c.pathLength()
											+ blobHit.pathLength() - 1,
									l.getExpansions(), l.getGenerations(),
									l.getDuplicates()));
						}
					}
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
					} else if (prevClosed != null && prevClosed.r == REASON.EXP) {
						// this node was expanded forwards, but not backwards,
						// should turn it into an incumbent solution.
						if (getIncumbent() == null
								|| getIncumbent().getCost() > child.getG()
										+ prevClosed.getG()) {
							HAddSearchNode blobHit = child;
							setIncumbent(new Solution(prevClosed,
									prevClosed.getG() + blobHit.getG(),
									l.getDuration(), prevClosed.pathLength()
											+ blobHit.pathLength() - 1,
									l.getExpansions(), l.getGenerations(),
									l.getDuplicates()));
						}

						// remove the existing forwards expansion node from the
						// open list, if it is on the open list.
						if (prevClosed.getHeapIndex() != Heapable.NO_POS) {
							open.remove(prevClosed);
						}
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


		//ArrayList<SearchNode> border = new ArrayList<SearchNode>();

		/**
		 * The difference here is that all of the children are in a hash table,
		 * as opposed to being in their original arraylists.
		 */
//		Iterator<Map.Entry<Object, HAddSearchNode>> i = borderNodes.entrySet()
//		.iterator();
//
//		double oldHAdd = hAdd;
//		hAdd = Double.MAX_VALUE;
//		while (i.hasNext()) {
//			Map.Entry<Object, HAddSearchNode> next = i.next();
//			SearchNode n = next.getValue();
//			HAddSearchNode cn = closed.get(n.getState().getKey());
//
//			if (cn == null || cn.r == REASON.EXP) {
//				//border.add(n);
//				double hErr = n.getG() - n.getH();
//				assert (hErr >= -0.000000001);
//				if (hErr < hAdd) {
//					if (hAdd < oldHAdd) {
//						assert(hAdd > -0.00000000001);
//						System.err.printf("old hAdd: %.20f new hAdd: %.20f\n",
//								oldHAdd, hAdd);
//					}
//					hAdd = hErr;
//					if(hAdd < oldHAdd){
//						assert (Math.abs(hAdd - oldHAdd) < 0.0000000001);
//						hAdd = oldHAdd;
//					} else {
//					}
//				}
//			} else {
//				// this will help, but not completely sure if it valid.
//				// Certainly seems reasonable.
//				i.remove();
//			}
//		}

		assert (hAdd != Double.MAX_VALUE);
		if(gb.peek() != null)
			hAdd = gb.peek().getG() - gb.peek().getH();
		assert(hAdd == gb.peek().getG() - gb.peek().getH());
		//		if (hAdd > oldHAdd) {
//			open.reHeapify();
//		}
		
	}

	protected boolean keepGoing() {
		if (open.isEmpty())
			return false;
		if (!l.keepGoing())
			return false;
		if (getIncumbent() == null)
			return true;
		else {
			HAddSearchNode next = open.peek();
			
			if(next.getF() >= getIncumbent().getCost()){
				return false;
			} else
				return true;
		}
	}

	public ArrayList<SearchState> solve() {
		HAddSearchNode i = this.makeInitialForwards(initial);
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

	private void considerChild(HAddSearchNode child) {
		// check if the child is a goal
		if (child.getState().isGoal()) {
			if (getIncumbent() == null
					|| getIncumbent().getCost() > child.getG()) {
				setIncumbent(new Solution(child, child.getG(), l.getDuration(),
						child.pathLength(), l.getExpansions(),
						l.getGenerations(), l.getDuplicates()));
				return;
			}
		}

		HAddSearchNode cn = closed.get(child.getState().getKey());

		if (cn != null && cn.r == REASON.CACHE) {
			if (getIncumbent() == null
					|| getIncumbent().getCost() > child.getF()) {
				HAddSearchNode blobHit = cn;
				setIncumbent(new Solution(child, child.getG() + blobHit.getG(),
						l.getDuration(), child.pathLength()
								+ blobHit.pathLength() - 1, l.getExpansions(),
						l.getGenerations(), l.getDuplicates()));
			}
			// in any case, return since the child is either now the incumbent,
			// or the child is just junk
			return;
		}

		// look for the child in the hash table
		HAddSearchNode incumbentNode = null;
		if (cn != null && cn.r == REASON.EXP) {
			incumbentNode = cn;
		}
		if (incumbentNode == null) {
			open.add(child);
			closed.put(child.getState().getKey(), child);
		} else if (child.getG() < incumbentNode.getG()) {
			int ix = incumbentNode.getHeapIndex();
			if (ix != Heapable.NO_POS) {
				boolean r = open.remove(incumbentNode);
				assert (r);
			}
			open.add(child);
			HAddSearchNode r = closed.remove(incumbentNode.getState().getKey());
			if (r.r == REASON.CACHE) {
				throw new RuntimeException(
						"Shouldn't remove a node in the cache from the closed list");
			}
			assert (r != null);
			closed.put(child.getState().getKey(), child);
		}
		if (incumbentNode != null)
			
			l.incrDup();

	}

	public ArrayList<SearchState> getFinalPath() {
		if (getIncumbent() == null)
			return null;
		ArrayList<SearchState> path = new ArrayList<SearchState>();

		SearchNode last = getIncumbent().getGoal();

		while (last != null) {
			path.add(last.getState());
			last = last.getParent();
		}
		Collections.reverse(path);

		// if the incumbent is not a goal, add the rest of this path onto the
		// end
		if (!getIncumbent().getGoal().getState().isGoal()) {
			HAddSearchNode current = closed.get(getIncumbent().getGoal()
					.getState().getKey());
			assert (current != null);
			assert (current.r == REASON.CACHE);
			current = (HAddSearchNode) current.getParent();
			while (current != null) {
				path.add(current.getState());
				current = (HAddSearchNode) current.getParent();
			}
		}

		return path;
	}

	@Override
	public void reset() {
		open.clear();
		closed.clear();
	}

	@Override
	protected void cleanup() {
		open.clear();
		closed.clear();
	}

	@Override
	public SearchAlgorithm clone() {
		return new HAddAStar(prob, l.clone(), initialCacheSize);
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

		while (keepGoing()) {
			HAddSearchNode current = open.poll();
			processNode(current);
		}
		l.endClock();
	}

}
