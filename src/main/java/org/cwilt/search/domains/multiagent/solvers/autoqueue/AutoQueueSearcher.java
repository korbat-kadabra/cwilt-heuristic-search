package org.cwilt.search.domains.multiagent.solvers.autoqueue;
import java.util.ArrayList;
import java.util.HashMap;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.SearchState.Child;import org.cwilt.search.utils.basic.MinHeap;
public class AutoQueueSearcher extends org.cwilt.search.search.SearchAlgorithm {

	public AutoQueueSearcher(SearchProblem prob, Limit l) {
		super(prob, l);
		open = new MinHeap<AutoQueueSearchNode>(new AutoQueueSearchNode.AutoQueueComparator());
		closed = new HashMap<AutoQueueSearchNode.QueueSearchKey, AutoQueueSearchNode>();
	}
	
	MinHeap<AutoQueueSearchNode> open;
	HashMap<AutoQueueSearchNode.QueueSearchKey, AutoQueueSearchNode> closed;
	
	
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
		return new AutoQueueSearcher(super.prob, l.clone());
	}

	@Override
	public ArrayList<SearchState> solve() {
		AutoQueueSearchNode init = (AutoQueueSearchNode) super.prob.getInitial();
		assert(init != null);
		open.add(init);
		closed.put(init.key, init);
		
		while(l.keepGoingNoMem()){
			AutoQueueSearchNode next = open.poll();
			if(next == null){
				break;
			}
			if(next.isGoal()){
				//return
				return next.reconstructPath();
			}
			ArrayList<Child> children = next.expand();
			//System.out.println(next);
			//System.out.println(children);
			l.incrExp();
			l.incrGen(children.size());
			for(int i = 0; i < children.size(); i++){
				Child nextChild = children.get(i);
				AutoQueueSearchNode nc = (AutoQueueSearchNode) nextChild.child;
				AutoQueueSearchNode inc = closed.get(nc.key);
				if(inc == null){
					//child is completely new
					open.add(nc);
					closed.put(nc.key, nc);
				} else if (nc.key.congestion < inc.key.congestion){
					//child is a duplicate, but is better
					open.add(nc);
					closed.put(nc.key, nc);
					l.incrDup();
				} else {
					l.incrDup();
				}
			}
		}
		return null;
	}

}
