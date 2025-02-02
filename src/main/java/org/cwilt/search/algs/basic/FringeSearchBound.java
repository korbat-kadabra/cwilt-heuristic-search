package org.cwilt.search.algs.basic;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.Solution;
public class FringeSearchBound extends FringeSearch{

	private final HashSet<Object> expandedKeys;
	
	public FringeSearchBound(SearchProblem prob, Limit l, double bound) {
		super(prob, l);
		if(bound == 0){
			org.cwilt.search.algs.basic.bestfirst.AStar a = new org.cwilt.search.algs.basic.bestfirst.AStar(prob, new Limit());
			a.solve();
			super.fLimit = a.getFinalCost();
		} else {
			super.fLimit = bound;
		}
		this.expandedKeys = new HashSet<Object>();
	}
	protected void addRejected(SearchNode n){
		assert(n.getF() > super.fLimit);
	}
	
	protected void processNode(SearchNode n,
			ArrayList<SearchNode> rejectedChildren,
			ListIterator<SearchNode> openIter) {

		ArrayList<? extends SearchNode> children = n.expand();
		if(expandedKeys.contains(n.getState().getKey())){
			l.incrReExp();
		}
		
		expandedKeys.add(n.getState().getKey());
		l.incrExp();
		l.incrGen(children.size());
		for (SearchNode child : children) {
			if (child.getState().isGoal()) {
				if (getIncumbent() == null || getIncumbent().getCost() > n.getG()) {
					setIncumbent( new Solution(child, child.getG(),
							l.getDuration(), child.pathLength(),
							l.getExpansions(), l.getGenerations(),
							l.getDuplicates()));
				}
			}

			SearchNode incumbentNode = closed.get(child.getState().getKey());
			if (incumbentNode == null) {
				// if the child is good enough, add the child to this iteration
				if (child.getF() > fLimit) {
					addRejected(child);
				} else {
					openIter.add(child);
				}
				closed.put(child.getState().getKey(), child);
			} else if (child.getG() < incumbentNode.getG()) {
				double improvement = incumbentNode.getG() - child.getG();
				if (improvement < IMPROVEMENT_THRESHOLD) {
					continue;
				}

				SearchNode r = closed.remove(incumbentNode.getState().getKey());
				assert (r != null);
				// if the child is good enough, add the child to this iteration
				if (child.getF() > fLimit) {
					addRejected(child);
					minRejectedF = Math.min(minRejectedF, child.getF());
				} else {
					openIter.add(child);
				}
				closed.put(child.getState().getKey(), child);
			}
			if (incumbentNode != null)
				l.incrDup();
		}

	}

	
}
