package org.cwilt.search.algs.basic;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;
public class IDAStarClosed extends org.cwilt.search.search.SearchAlgorithm{
	private final HashMap<Object, SearchNode> expanded;
	
	public SearchAlgorithm clone() {
		IDAStarClosed i = new IDAStarClosed(prob, l.clone(), bound);
		return i;
	}
	
	private final double bound;

	public IDAStarClosed(org.cwilt.search.search.SearchProblem initial, Limit l, double bound) {
		super(initial, l);
		this.expanded = new HashMap<Object, SearchNode>();
		this.bound = bound;
	}

	@Override
	public SearchState findFirstGoal() {
		assert (false);
		System.err.println("IDA* only runs once.");
		System.exit(1);
		// TODO Auto-generated method stub
		return null;
	}

	protected double evaluateNode(SearchNode n) {
		return n.getF();
	}
	
	private void doIteration(double bound, SearchNode n) {
		if (evaluateNode(n) > bound) {
			//don't have to do anything for nodes that are outside the bound
		} else {
			if (n.getState().isGoal()) {
				setIncumbent ( new Solution(n, n.getG(), l.getDuration(), n
						.pathLength(), l.getExpansions(), l.getGenerations(), l
						.getDuplicates()));
				return;
			}
			if(super.getIncumbent() != null){
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
			expanded.put(n.getState().getKey(), n);
			
			Iterator<? extends SearchNode> i = n.expand().iterator();
			l.incrExp();
			while (getIncumbent() == null && i.hasNext()) {
				l.incrGen();
				SearchNode nextNode = i.next();
				
				doIteration(bound, nextNode);
			}
		}
	}

	private void setIncumbent() {
		l.startClock();
		SearchNode i = SearchNode.makeInitial(initial);
		while (getIncumbent() == null && l.keepGoing()) {
			doIteration(bound, i);
		}
		l.endClock();
	}

	@Override
	public ArrayList<SearchState> solve() {
		try {
			setIncumbent();
		} catch (OutOfMemoryError e) {
			l.endClock();
			l.setOutOfMemory();
		}
		if (getIncumbent() == null)
			return null;
		else {
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			return finalPath;
		}
	}

	public void cleanup() {
		return;
	}

	@Override
	public void reset() {
		expanded.clear();
	}

}
