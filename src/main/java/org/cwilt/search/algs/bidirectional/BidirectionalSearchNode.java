package org.cwilt.search.algs.bidirectional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchState;
public class BidirectionalSearchNode extends org.cwilt.search.search.SearchNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6576857383142274087L;

	protected BidirectionalSearchNode(SearchNode parent, SearchState s, double g, DIRECTION d) {
		super(parent, s, g);
		this.direction = d;
		// TODO Auto-generated constructor stub
	}
	
	public final DIRECTION direction;
	
	public enum DIRECTION {
		FORWARDS, BACKWARDS
	}
	
	public static class GComparator implements Comparator<BidirectionalSearchNode> {
		@Override
		public int compare(BidirectionalSearchNode arg0, BidirectionalSearchNode arg1) {
			if (arg0.getG() > arg1.getG())
				return 1;
			else if (arg0.getG() < arg1.getG())
				return -1;
			else
				return 0;
		}
	}


	protected BidirectionalSearchNode(BidirectionalSearchNode parent, SearchState s, double g, DIRECTION d) {
		super(parent, s, g);
		this.direction = d;
	}

	public static BidirectionalSearchNode makeInitialForwards(SearchState s) {
		BidirectionalSearchNode n = new BidirectionalSearchNode(null, s, 0, DIRECTION.FORWARDS);
		return n;
	}
	
	public static BidirectionalSearchNode makeInitialBackwards(SearchState s) {
		BidirectionalSearchNode n = new BidirectionalSearchNode(null, s, 0, DIRECTION.BACKWARDS);
		return n;
	}
	
	
	public ArrayList<BidirectionalSearchNode> reverseExpand(){
		ArrayList<SearchState.Child> baseChildren = s.reverseExpand();
		ArrayList<BidirectionalSearchNode> children = new ArrayList<BidirectionalSearchNode>();
		Iterator<SearchState.Child> it = baseChildren.iterator();
		while (it.hasNext()) {
			SearchState.Child c = it.next();
			children.add(new BidirectionalSearchNode(this, c.child, c.transitionCost + g, DIRECTION.BACKWARDS));
		}
		return children;
	}
	
	public ArrayList<BidirectionalSearchNode> expand() {
		ArrayList<SearchState.Child> baseChildren = s.expand();
		ArrayList<BidirectionalSearchNode> children = new ArrayList<BidirectionalSearchNode>();
		Iterator<SearchState.Child> it = baseChildren.iterator();
		while (it.hasNext()) {
			SearchState.Child c = it.next();
			children.add(new BidirectionalSearchNode(this, c.child, c.transitionCost + g, DIRECTION.BACKWARDS));
		}
		return children;
	}

}
