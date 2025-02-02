package org.cwilt.search.algs.basic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;
public class BBFIDAStar extends org.cwilt.search.search.SearchAlgorithm{
	private final LinkedList<BFSearchNode> forwardsExpand;
	private final LinkedList<BFSearchNode> backwardsExpand;
	private final HashMap<Object, BFSearchNode> closed;
	
	private enum DIRECTION {
		FORWARDS, BACKWARDS
	}
	
	/**
	 * Search nodes for this algorithm to use, needs to be a subclass so it can
	 * handle the special h values from this algpotithm.
	 * 
	 * @author cmo66
	 * 
	 */
	protected class BFSearchNode extends org.cwilt.search.search.SearchNode {
		/**
		 * 
		 */
		private static final long serialVersionUID = 963369607822118051L;
		protected final DIRECTION r;


		protected BFSearchNode(SearchNode parent, SearchState s, double g, DIRECTION r) {
			super(parent, s, g);
			this.r = r;
		}

		public ArrayList<? extends BFSearchNode> reverseExpand() {
			ArrayList<SearchState.Child> baseChildren = s.reverseExpand();
			ArrayList<BFSearchNode> children = new ArrayList<BFSearchNode>();
			Iterator<SearchState.Child> it = baseChildren.iterator();
			while (it.hasNext()) {
				SearchState.Child c = it.next();
				// children expanded in the backwards direction need CACHE for
				// their reason
				children.add(new BFSearchNode(this, c.child, c.transitionCost
						+ g, DIRECTION.BACKWARDS));
			}
			return children;
		}

		public ArrayList<? extends BFSearchNode> expand() {
			ArrayList<SearchState.Child> baseChildren = s.expand();
			ArrayList<BFSearchNode> children = new ArrayList<BFSearchNode>();
			Iterator<SearchState.Child> it = baseChildren.iterator();
			while (it.hasNext()) {
				SearchState.Child c = it.next();
				// children in the forward direction need EXP for their reason
				children.add(new BFSearchNode(this, c.child, c.transitionCost
						+ g, DIRECTION.FORWARDS));
			}
			return children;
		}

		public String toString() {
			return super.toString() + r.toString() + "\n";
		}
	}

	
	public BBFIDAStar(SearchProblem prob, Limit l) {
		super(prob, l);
		
		this.forwardsExpand = new LinkedList<BFSearchNode>();
		this.backwardsExpand = new LinkedList<BFSearchNode>();
		this.closed = new HashMap<Object, BFSearchNode>();
	}
	
	
	
	@Override
	public void reset() {
		this.forwardsExpand.clear();
		this.backwardsExpand.clear();
		this.closed.clear();
	}

	@Override
	protected void cleanup() {
		this.reset();
	}

	@Override
	public SearchAlgorithm clone() {
		return new BBFIDAStar(prob, l.clone());
	}

	@Override
	public ArrayList<SearchState> solve() {
		// TODO Auto-generated method stub
		return null;
	}
}
