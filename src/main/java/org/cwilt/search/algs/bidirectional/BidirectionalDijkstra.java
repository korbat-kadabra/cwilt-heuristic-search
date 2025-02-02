package org.cwilt.search.algs.bidirectional;
import java.util.ArrayList;
import java.util.HashMap;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.utils.basic.MinHeap;
public class BidirectionalDijkstra extends org.cwilt.search.search.SearchAlgorithm {
	
	protected final HashMap<Object, BidirectionalSearchNode> closed;
	
	protected final MinHeap<BidirectionalSearchNode> forwards, backwards;
	public BidirectionalDijkstra(SearchProblem prob, Limit l) {
		super(prob, l);
		this.forwards = new MinHeap<BidirectionalSearchNode>(new BidirectionalSearchNode.GComparator());
		this.backwards= new MinHeap<BidirectionalSearchNode>(new BidirectionalSearchNode.GComparator());
		this.closed = new HashMap<Object, BidirectionalSearchNode>();
	}

	@Override
	public void reset() {
		this.cleanup();
	}

	@Override
	protected void cleanup() {
		forwards.clear();
		backwards.clear();
		closed.clear();
	}

	@Override
	public SearchAlgorithm clone() {
		return new BidirectionalDijkstra(super.prob, l.clone());
	}
	
	protected double evaluateForwardsNode(){
		return forwards.peek().getG();
	}
	
	protected double evaluateBackwardsNode(){
		return backwards.peek().getG();
	}
	
	@Override
	public ArrayList<SearchState> solve() {
		BidirectionalSearchNode fInit = BidirectionalSearchNode.makeInitialForwards(initial);
		BidirectionalSearchNode bInit = BidirectionalSearchNode.makeInitialBackwards(prob.getGoal());
		forwards.add(fInit);
		backwards.add(bInit);
		closed.put(fInit.getState().getKey(), fInit);
		closed.put(bInit.getState().getKey(), bInit);
		
		while(l.keepGoing() && (!forwards.isEmpty() || backwards.isEmpty()) && super.getIncumbent() == null){
			double fwdMin = evaluateForwardsNode();
			double bwdMin = evaluateBackwardsNode();
			
			if(fwdMin < bwdMin){
				//expand something going forwards
			} else {
				//expand something backwards
			}
		}
		
		return null;
	}

}
