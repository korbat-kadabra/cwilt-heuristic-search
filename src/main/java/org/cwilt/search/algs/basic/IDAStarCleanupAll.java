package org.cwilt.search.algs.basic;
import java.util.Iterator;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.Solution;
public class IDAStarCleanupAll extends IDAStarCleanup{

	public IDAStarCleanupAll(SearchProblem initial, Limit l, int iterations) {
		super(initial, l, iterations);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void doIteration(double bound, SearchNode n) {
		if (evaluateNode(n) > bound) {
			//don't have to do anything for nodes that are outside the bound
		} else {
			if (n.getState().isGoal()) {
				setIncumbent ( new Solution(n, n.getG(), l.getDuration(), n
						.pathLength(), l.getExpansions(), l.getGenerations(), l
						.getDuplicates()));
				return;
			}

			SearchNode incumbent = expanded.get(n.getState().getKey());
			if(incumbent != null && incumbent.getF() <= n.getF()){
				//child is junk
				l.incrDup();
				return;
			}
			if(incumbent != null){
				l.incrReExp();
			}

			SearchNode incumbent2 = globalExpanded.get(n.getState().getKey());
			if(incumbent2 != null && incumbent2.getF() < n.getF()){
				//child is junk
				l.incrDup();
				return;
			}
			if(incumbent2 != null){
				this.internalExp ++;
			}
			
			expanded.put(n.getState().getKey(), n);
			
			Iterator<? extends SearchNode> i = n.expand().iterator();
			l.incrExp();
			while (i.hasNext()) {
				l.incrGen();
				SearchNode nextNode = i.next();				
				doIteration(bound, nextNode);
			}
		}
	}

}
