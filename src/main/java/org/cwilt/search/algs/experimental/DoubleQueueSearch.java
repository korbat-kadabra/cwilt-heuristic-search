package org.cwilt.search.algs.experimental;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.basic.Heapable;import org.cwilt.search.utils.basic.MinHeap;import org.cwilt.search.utils.basic.MinHeap2;
public class DoubleQueueSearch extends SearchAlgorithm {

	public static final class DQSearchNode extends org.cwilt.search.search.SearchNode implements org.cwilt.search.utils.basic.DoubleHeapable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -5164233312883096884L;
		private int secondIX;
		public DQSearchNode(org.cwilt.search.search.SearchNode parent, SearchState s, double g){
			super(parent, s, g);
			this.secondIX = Heapable.NO_POS;
		}
		public ArrayList<DQSearchNode> expand() {
			ArrayList<SearchState.Child> baseChildren = super.s.expand();
			ArrayList<DQSearchNode> children = new ArrayList<DQSearchNode>();
			Iterator<SearchState.Child> it = baseChildren.iterator();
			while (it.hasNext()) {
				SearchState.Child c = it.next();
				children.add(new DQSearchNode(this, c.child, c.transitionCost + g));
			}
			return children;
		}
		public ArrayList<SearchNode> reverseExpand(){
			ArrayList<SearchState.Child> baseChildren = super.s.reverseExpand();
			ArrayList<SearchNode> children = new ArrayList<SearchNode>();
			Iterator<SearchState.Child> it = baseChildren.iterator();
			while (it.hasNext()) {
				SearchState.Child c = it.next();
				children.add(new DQSearchNode(this, c.child, c.transitionCost + g));
			}
			return children;
		}
		@Override
		public int getHeap2Index() {
			return secondIX;
		}
		@Override
		public void setHeap2Index(int ix) {
			secondIX = ix;
		}
		public static class HComparator implements Comparator<DQSearchNode> {
			@Override
			public int compare(DQSearchNode arg0, DQSearchNode arg1) {
				if (arg0.getH() > arg1.getH())
					return 1;
				else if (arg0.getH() < arg1.getH())
					return -1;
				else
					return 0;
			}
		}
		public static class DComparator implements Comparator<DQSearchNode> {
			@Override
			public int compare(DQSearchNode arg0, DQSearchNode arg1) {
				if (arg0.getH() > arg1.getH())
					return 1;
				else if (arg0.getD() < arg1.getD())
					return -1;
				else
					return 0;
			}
		}
		public static class FGComparator implements Comparator<DQSearchNode> {
			@Override
			public int compare(DQSearchNode arg0, DQSearchNode arg1) {
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

		
		public static class WFGComparator implements Comparator<DQSearchNode> {
			private final double weight;

			public WFGComparator(double w) {
				this.weight = w;
			}

			@Override
			public int compare(DQSearchNode arg0, DQSearchNode arg1) {
				double g0 = arg0.getG();
				double h0 = arg0.getF() - g0;
				double fp0 = g0 + weight * h0;

				double g1 = arg1.getG();
				double h1 = arg1.getF() - g1;
				double fp1 = g1 + weight * h1;

				if (fp0 == fp1) {
					if (arg0.equals(arg1))
						return 0;
					else {
						if (g0 > g1)
							return -1;
						else if (g0 < g1)
							return 1;
						else
							return 0;
					}
				} else if (fp0 < fp1)
					return -1;
				else
					return 1;
			}

		}

		

	}
	
	private final MinHeap<DQSearchNode> open1;
	private final MinHeap2<DQSearchNode> open2;
	private final HashMap<Object, DQSearchNode> closed;

	
	private final double weight;
	
	
	public DoubleQueueSearch(SearchProblem prob, Limit l, double weight) {
		super(prob, l);
		this.weight = weight;
		if(weight != -1){
		this.open1 = new MinHeap<DQSearchNode>(new DQSearchNode.HComparator());
		this.open2 = new MinHeap2<DQSearchNode>(new DQSearchNode.WFGComparator(weight));
		} else {
			this.open1 = new MinHeap<DQSearchNode>(new DQSearchNode.HComparator());
			this.open2 = new MinHeap2<DQSearchNode>(new DQSearchNode.DComparator());
		}
		this.closed = new HashMap<Object, DQSearchNode>();
	}
	
	public DoubleQueueSearch(SearchProblem prob, Limit l) {
		super(prob, l);
		this.weight = -1;
		this.open1 = new MinHeap<DQSearchNode>(new DQSearchNode.HComparator());
		this.open2 = new MinHeap2<DQSearchNode>(new DQSearchNode.DComparator());
		this.closed = new HashMap<Object, DQSearchNode>();
		prob.setCalculateD();
	}
	
	@Override
	public void reset() {
		this.cleanup();
	}

	@Override
	protected void cleanup() {
		open1.clear();
		open2.clear();
		closed.clear();
	}

	@Override
	public SearchAlgorithm clone() {
		if(weight == -1){
			return new DoubleQueueSearch(prob, l);
		} else
			return new DoubleQueueSearch(prob, l, weight);
	}

	@Override
	public ArrayList<SearchState> solve() {
		DQSearchNode initial = new DQSearchNode(null, prob.getInitial(), 0);

		open1.insert(initial);
		open2.insert(initial);
		closed.put(initial.getState().getKey(), initial);

		try {
			setIncumbent();
		} catch (OutOfMemoryError e) {
			l.setOutOfMemory();
			l.endClock();
			cleanup();
		}
		if (getIncumbent() == null)
			return null;
		else {
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			return finalPath;
		}
	}
	
	protected void setIncumbent() {
		l.startClock();
		Solution goal = null;
		boolean flag = false;
		while (!open1.isEmpty() && goal == null && l.keepGoing()) {
			DQSearchNode current;
			if(flag){
				current = open1.poll();
				open2.removeAt(current.getHeap2Index());
			}
			else{
				current = open2.poll();
				open1.removeAt(current.getHeapIndex());
			}
			flag = !flag;
			goal = processNode(current);
		}
		l.endClock();
		if (goal != null)
			setIncumbent ( goal );
	}
	protected Solution processNode(DQSearchNode current) {
		if (current.getState().isGoal()) {
			if (getIncumbent() == null || getIncumbent().getCost() > current.getG())
				return new Solution(current, current.getG(), l.getDuration(),
						current.pathLength(), l.getExpansions(), l
								.getGenerations(), l.getDuplicates());
		} else {
			ArrayList<DQSearchNode> children = current.expand();
			l.incrExp();
			Iterator<DQSearchNode> childIter = children.iterator();
			while (childIter.hasNext()) {
				l.incrGen();
				considerChild(childIter.next());
			}
		}
		return null;
	}
	
	protected void considerChild(DQSearchNode child) {
		// look for the child in the hash table
		DQSearchNode incumbent = closed.get(child.getState().getKey());
		if (incumbent == null) {
			open1.insert(child);
			open2.insert(child);
			closed.put(child.getState().getKey(), child);
		} else if (child.getG() < incumbent.getG()) {
			int ix1 = incumbent.getHeapIndex();
			int ix2 = incumbent.getHeap2Index();
			if (ix1 != Heapable.NO_POS){
				open1.removeAt(ix1);
				open2.removeAt(ix2);
			}
			closed.remove(incumbent);
			open1.insert(child);
			open2.insert(child);
			closed.put(child.getState().getKey(), child);
		}
		if (incumbent != null)
			l.incrDup();
	}


}
