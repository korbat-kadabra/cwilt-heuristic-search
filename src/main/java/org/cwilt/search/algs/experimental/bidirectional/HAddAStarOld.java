package org.cwilt.search.algs.experimental.bidirectional;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.basic.Heapable;import org.cwilt.search.utils.basic.MinHeap;
public class HAddAStarOld extends SearchAlgorithm{
	private final MinHeap<SearchNode> open;
	private final HashMap<Object, CachedNode> closed;
	
	

	private final int maxCache;

	protected void setIncumbent() {
		l.startClock();

		ArrayList<SearchState> goals = prob.getGoals();

		MinHeap<SearchNode> gb = new MinHeap<SearchNode>(new HErrComparator());
		HashSet<Object> onOpen = new HashSet<Object>();
		HashMap<Object, ArrayList<? extends SearchNode>> childrenMap = new HashMap<Object, ArrayList<? extends SearchNode>>();

		for (SearchState goalState : goals) {
			SearchNode goal = SearchNode.makeInitial(goalState);
			gb.add(goal);
		}

		while (!gb.isEmpty() && closed.size() < maxCache) {
			SearchNode next = gb.poll();
			CachedNode c = closed.get(next.getState().getKey());
			SearchNode incumbent = null;
			if(c != null){
				incumbent = c.payload;
			}
			if (incumbent == null || incumbent.getF() > next.getF()) {
				closed.put(next.getState().getKey(), new CachedNode(next, CachedNode.REASON.CACHE));
//				System.err.println(gb.size());
//				System.err.println(next);
//				System.err.println(gb);

				cacheSize ++;
				ArrayList<? extends SearchNode> children = next.reverseExpand();
				childrenMap.put(next.getState().getKey(), children);
				l.incrExp();
				l.incrGen(children.size());
				for (SearchNode child : children) {
					if (!onOpen.contains(child.getState().getKey()))
						gb.add(child);
				}
			}
		}

		Iterator<Map.Entry<Object, CachedNode>> i = closed.entrySet().iterator();

		hAdd = Double.MAX_VALUE;

		while (i.hasNext()) {
			Map.Entry<Object, CachedNode> next = i.next();
			// why is this the correct thing to do?
			if(next.getValue().r == CachedNode.REASON.EXP){
				continue;
			}
			Object parentKey = next.getKey();
			ArrayList<? extends SearchNode> children = childrenMap
					.get(parentKey);
			assert (children != null);
			for (SearchNode n : children) {
				if (!closed.containsKey(n.getState().getKey())) {
					// this child is not in the cache, so it is a border node.
					double hErr = n.getG() - n.getH();
					assert (hErr >= -0.000000001);
					if (hErr < hAdd)
						hAdd = hErr;
				}
			}
		}

		assert (hAdd != Double.MAX_VALUE);

		while (keepGoing()) {
			SearchNode current = open.poll();
			processNode(current);
		}
		l.endClock();
	}
	
	private boolean keepGoing(){
		if(open.isEmpty())
			return false;
		if(!l.keepGoing())
			return false;
		if(getIncumbent() == null)
			return true;
		else {
			SearchNode next = open.peek();
			if(next.getF() >= getIncumbent().getCost())
				return false;
			else
				return true;
		}
	}

	private int cacheSize;
	public HAddAStarOld(SearchProblem prob, Limit l, int maxCache) {
		super(prob, l);
		cacheSize = 0;
		this.maxCache = maxCache;
		this.open = new MinHeap<SearchNode>(new SearchNode.FGComparator());
		this.closed = new HashMap<Object, CachedNode>();
	}
	
	public static class CachedNode{
		public final REASON r;
		enum REASON {
			CACHE, EXP
		};
		public final SearchNode payload;
		public CachedNode(SearchNode p, REASON r){
			this.r = r;
			this.payload = p;
		}
	}
	
	@Override
	public void reset() {
		open.clear();
		closed.clear();
	}

	@Override
	protected void cleanup() {
		reset();
	}

	@Override
	public SearchAlgorithm clone() {
		return new HAddAStarOld(prob, l.clone(), maxCache);
	}

	private double hAdd;

	public static final class HErrComparator implements Comparator<SearchNode> {
		@Override
		public int compare(SearchNode o1, SearchNode o2) {
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

	private double shortcutLength;

	private class HAddSearchNode extends org.cwilt.search.search.SearchNode {

		/**
		 * 
		 */
		private static final long serialVersionUID = 963369607822118051L;

		protected HAddSearchNode(HAddSearchNode parent, SearchState s, double g, double f) {
			super(parent, s, g, f);
		}

		public ArrayList<? extends SearchNode> expand() {
//			robot.RobotState rs = (robot.RobotState) s;
//			if(rs.getX() == 3 && rs.getY() == 4 && rs.getSpeed() == 140 && rs.getHeading() == 0){
//				@SuppressWarnings("unused")
//				int a = 0;
//			}
			
			ArrayList<SearchState.Child> baseChildren = s.expand();
			ArrayList<HAddSearchNode> children = new ArrayList<HAddSearchNode>();
			Iterator<SearchState.Child> it = baseChildren.iterator();
			while (it.hasNext()) {
				SearchState.Child c = it.next();

//				robot.RobotState rs2 = (robot.RobotState) c.child;
//				if(rs2.getX() == 4 && rs2.getY() == 4 && rs2.getSpeed() == 100 && rs2.getHeading() == 0){
//					@SuppressWarnings("unused")
//					int a = 0;
//				}

				double childH;
				CachedNode cn = HAddAStarOld.this.closed.get(c.child.getKey());
				SearchNode childIncumbent = null;
				if(cn != null && cn.r == CachedNode.REASON.CACHE){
					childIncumbent = cn.payload;
				}
				if(childIncumbent != null)
					childH = childIncumbent.getG();
				else
					childH = c.child.h() + HAddAStarOld.this.hAdd;
				
//				if(c.child.isGoal()){
//					assert(childH == 0);
//				}
				
				double childF = c.transitionCost + g+ childH;
				children.add(new HAddSearchNode(this, c.child, c.transitionCost + g, childF));
			}
			return children;
		}


	}
	
	public SearchNode makeInitial(SearchState s) {
		SearchNode n = this.new HAddSearchNode(null, s, 0, s.h());
		return n;
	}
	
	
	@Override
	public void printExtraData(PrintStream ps) {
		SearchAlgorithm.printPair(ps, "shortcut length", shortcutLength);
		SearchAlgorithm.printPair(ps, "hAdd", hAdd);

		SearchAlgorithm.printPair(ps, "cache size", cacheSize);
	}


	protected void processNode(SearchNode current) {
		
		ArrayList<? extends SearchNode> children = current.expand();
		l.incrExp();
		Iterator<? extends SearchNode> childIter = children.iterator();
		while (childIter.hasNext()) {
			l.incrGen();
			SearchNode child = childIter.next();
			
//			if(child.getState().isGoal()){
//				assert(child.getH() == 0);
//			}
			
			considerChild(child);
		}
	}

	private void considerChild(SearchNode child) {
		// check if the child is a goal
		if (child.getState().isGoal()) {
			//goals should have h == 0
			//assert(child.getH() == 0);
			
			if (getIncumbent() == null || getIncumbent().getCost() > child.getG()) {
				setIncumbent( new Solution(child, child.getG(), l.getDuration(),
						child.pathLength(), l.getExpansions(),
						l.getGenerations(), l.getDuplicates()));
				return;
			}
		}
		
		CachedNode cn = closed.get(child.getState().getKey());
		
		if (cn != null && cn.r == CachedNode.REASON.CACHE) {
			if (getIncumbent() == null || getIncumbent().getCost() > child.getF()) {
				SearchNode blobHit = cn.payload;

				//the path needs to be constructed here.
				
				setIncumbent( new Solution(child, child.getG() + blobHit.getG(),
						l.getDuration(), child.pathLength()
								+ blobHit.pathLength() - 1, l.getExpansions(),
						l.getGenerations(), l.getDuplicates()));
			}
			// in any case, return since the child is either now the incumbent,
			// or the child is just junk
			return;
		}

		// look for the child in the hash table
		SearchNode incumbentNode = null;
		if(cn != null && cn.r == CachedNode.REASON.EXP){
			incumbentNode = cn.payload;
		}
		if (incumbentNode == null) {
			open.add(child);
			closed.put(child.getState().getKey(), new CachedNode(child, CachedNode.REASON.EXP));
		} else if (child.getG() < incumbentNode.getG()) {
			int ix = incumbentNode.getHeapIndex();
			if (ix != Heapable.NO_POS) {
				boolean r = open.remove(incumbentNode);
				assert(r);
			}
			open.add(child);
			CachedNode r = closed.remove(incumbentNode.getState().getKey());
			if(r.r == CachedNode.REASON.CACHE){
				throw new RuntimeException("Shouldn't remove a node in the cache from the closed list");
			}
			assert (r != null);
			closed.put(child.getState().getKey(), new CachedNode(child, CachedNode.REASON.EXP));
		}
		if (incumbentNode != null)
			l.incrDup();

	}

	public ArrayList<SearchState> solve() {
		SearchNode i = this.makeInitial(initial);
		open.add(i);
		closed.put(i.getState().getKey(), new CachedNode(i, CachedNode.REASON.EXP));

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
