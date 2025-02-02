package org.cwilt.search.search;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public class SearchNodeLong extends SearchNode{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3339745874415563933L;

	private static int id = 0;
	private static final synchronized int nextLong(){
		return id++;
	}
	
	protected final int tiebreak;
	protected SearchNodeLong(SearchNodeLong parent, SearchState s, double g) {
		super(parent, s, g);
		this.tiebreak = nextLong();
	}

	
//	private static final java.util.Random r = new java.util.Random();
	public static SearchNodeLong makeInitial(SearchState s) {
		SearchNodeLong n = new SearchNodeLong(null, s, 0);
		return n;
	}
	
	public static class HLComparator implements Comparator<SearchNodeLong> {
		@Override
		public int compare(SearchNodeLong arg0, SearchNodeLong arg1) {
			if (arg0.getH() > arg1.getH())
				return 1;
			else if (arg0.getH() < arg1.getH())
				return -1;
			else
				return  (arg0.tiebreak - arg1.tiebreak);
		}
	}
	public static class DLComparator implements Comparator<SearchNodeLong> {
		@Override
		public int compare(SearchNodeLong arg0, SearchNodeLong arg1) {
			if (arg0.getD() > arg1.getD())
				return 1;
			else if (arg0.getD() < arg1.getD())
				return -1;
			else
				return (int) (arg0.tiebreak - arg1.tiebreak);
		}
	}

	
	public ArrayList<SearchNodeLong> reverseExpand(){
		ArrayList<SearchState.Child> baseChildren = s.reverseExpand();
		ArrayList<SearchNodeLong> children = new ArrayList<SearchNodeLong>();
		Iterator<SearchState.Child> it = baseChildren.iterator();
		while (it.hasNext()) {
			SearchState.Child c = it.next();
			children.add(new SearchNodeLong(this, c.child, c.transitionCost + g));
		}
		return children;
	}
	
	public ArrayList<SearchNodeLong> expand() {
		ArrayList<SearchState.Child> baseChildren = s.expand();
		ArrayList<SearchNodeLong> children = new ArrayList<SearchNodeLong>();
		Iterator<SearchState.Child> it = baseChildren.iterator();
		while (it.hasNext()) {
			SearchState.Child c = it.next();
			children.add(new SearchNodeLong(this, c.child, c.transitionCost + g));
		}
		return children;
	}

}
