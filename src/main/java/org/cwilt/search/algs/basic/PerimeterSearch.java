package org.cwilt.search.algs.basic;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.basic.Heapable;import org.cwilt.search.utils.basic.MinHeap;
public class PerimeterSearch extends org.cwilt.search.algs.basic.bestfirst.BestFirstSearch {

	private final int perimeterSize;
	private final ArrayList<SearchNode> perimeter;

	protected boolean solutionGoodEnough(){
		Solution inc = super.getIncumbent();
		if(inc == null)
			return false;
		if(inc.getCost() <= open.peek().getF())
			return true;
		else
			return false;
	}

	
	private void createPerimeter() {
		MinHeap<SearchNode> gb = new MinHeap<SearchNode>(
				new SearchNode.GComparator());
		HashMap<Object, SearchNode> gbClosed = new HashMap<Object, SearchNode>();
		ArrayList<ArrayList<? extends SearchNode>> storedChildren = new ArrayList<ArrayList<? extends SearchNode>>();
		ArrayList<SearchNode> newPerimeter = new ArrayList<SearchNode>();
		
		for (SearchState s : prob.getGoals()) {
			SearchNode newNode = SearchNode.makeInitial(s);
			gb.add(newNode);
			gbClosed.put(s.getKey(), newNode);
			l.incrGen();
		}

		while (newPerimeter.size() < perimeterSize) {
			SearchNode next = gb.poll();
			assert(next != null);
			if (next.getState().getKey().equals(prob.getInitial().getKey())) {
				// found the start going backwards, so can stop searching.
				this.setIncumbent(next);
				break;
			}
			newPerimeter.add(next);
			l.incrExp();
			ArrayList<? extends SearchNode> children = next.reverseExpand();
			storedChildren.add(children);
			l.incrGen(children.size());
			for (SearchNode c : children) {
				SearchNode incumbent = gbClosed.get(c.getState().getKey());
				if (incumbent == null) {
					gb.add(c);
					gbClosed.put(c.getState().getKey(), c);
				} else if (incumbent.getG() > c.getG()) {
					assert (incumbent.getHeapIndex() != Heapable.NO_POS);
					boolean removed = gb.remove(incumbent);
					assert (removed);
					gb.add(c); 
					gbClosed.put(c.getState().getKey(), c);
				}
			}
		}
		
		for(int i = 0; i < newPerimeter.size(); i++){
			ArrayList<? extends SearchNode> children = storedChildren.get(i);
			boolean allExpanded = true;
			for(SearchNode n : children){
				SearchNode incumbent = gbClosed.get(n.getState().getKey());
				assert(incumbent != null);
				if(incumbent.getHeapIndex() != Heapable.NO_POS){
					allExpanded = false;
					break;
				}
			}
			if(!allExpanded){
				perimeter.add(newPerimeter.get(i));
			}
		}
	}
	
	@Override
	public void printExtraData(PrintStream ps){
		SearchAlgorithm.printPair(ps, "perimeterSize", perimeter.size());
	}

	public PerimeterSearch(SearchProblem prob, Limit l, int perimeterSize) {
		super(prob, l);
		this.perimeterSize = perimeterSize;
		this.perimeter = new ArrayList<SearchNode>(perimeterSize);
		super.open = new MinHeap<SearchNode>(new SearchNode.FGComparator());
	}

	@Override
	public void reset() {
		cleanup();
	}

	@Override
	protected void cleanup() {
		perimeter.clear();
	}

	@Override
	public SearchAlgorithm clone() {
		return new PerimeterSearch(prob, l.clone(), this.perimeterSize);
	}

	private class PerimeterSearchNode extends SearchNode {

		/**
		 * 
		 */
		private static final long serialVersionUID = 9142032371522387729L;

		protected PerimeterSearchNode(SearchNode parent, SearchState s,
				double g, double f) {
			super(parent, s, g, f);
		}

		public ArrayList<? extends PerimeterSearchNode> expand() {
			ArrayList<SearchState.Child> baseChildren = s.expand();
			ArrayList<PerimeterSearchNode> children = new ArrayList<PerimeterSearchNode>();
			Iterator<SearchState.Child> it = baseChildren.iterator();
			while (it.hasNext()) {
				SearchState.Child c = it.next();
				PerimeterHit hit = calculateH(c.child);
				double childG = c.transitionCost + g;
				PerimeterSearchNode child = new PerimeterSearchNode(this, c.child, childG, childG + hit.dist);
				children.add(child);
				if(hit.closest.getState().getKey().equals(c.child.getKey())){
					createIncumbent(child, hit.closest);
				}
			}
			return children;
		}
	}

	private void createIncumbent(SearchNode forwards, SearchNode backwards) {
		assert (forwards != null);
		assert (backwards != null);
		Solution s = new Solution(forwards, forwards.getG() + backwards.getG(),
				l.getDuration(), forwards.pathLength() + backwards.pathLength()
						- 1, l.getExpansions(), l.getGenerations(),
				l.getDuplicates());
		this.setIncumbent(s);
	}

	private final class PerimeterHit {
		public final double dist;
		public final SearchNode closest;

		public PerimeterHit(double dist, SearchNode closest) {
			this.dist = dist;
			this.closest = closest;
		}
	}

	private PerimeterHit calculateH(SearchState s) {

		double best = Double.MAX_VALUE;
		SearchNode bestNode = null;

		for (SearchNode n : perimeter) {
			double hHere = n.getState().distTo(s) + n.getG();
			if (best > hHere) {
				best = hHere;
				bestNode = n;
			}
		}

		return new PerimeterHit(best, bestNode);
	}

	private PerimeterSearchNode makeInitialForwards(SearchState s) {
		assert(s != null);
		PerimeterSearchNode n = this.new PerimeterSearchNode(null, s, 0,
				calculateH(s).dist);
		return n;
	}

	public ArrayList<SearchState> solve() {

		createPerimeter();

		PerimeterSearchNode i = this.makeInitialForwards(initial);
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
	
	private boolean checkIncumbent(){
		return super.getIncumbent() == null || super.getIncumbent().getCost() > open.peek().getF();
	}
	
	protected void setIncumbent() {
		l.startClock();
		Solution goal = null;
		while (!open.isEmpty() && goal == null && l.keepGoing() && checkIncumbent()) {
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
