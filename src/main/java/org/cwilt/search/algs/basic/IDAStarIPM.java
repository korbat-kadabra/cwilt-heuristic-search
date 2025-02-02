package org.cwilt.search.algs.basic;
import java.util.ArrayList;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;
public class IDAStarIPM extends org.cwilt.search.search.SearchAlgorithm {

	private double bound;
	private double nextBound;
	
	public IDAStarIPM(SearchProblem prob, Limit l) {
		super(prob, l);
		this.bound = 0;
	}

	@Override
	public void reset() {
		this.bound = 0;
	}

	@Override
	protected void cleanup() {

	}

	@Override
	public SearchAlgorithm clone() {
		return new IDAStarIPM(prob, l.clone());
	}

	private void doIteration(SearchState current, int currentID,
			double currentG, int depth) {
		assert (currentG <= bound);
		if(!l.keepGoing())
			return;
		
		if(super.getIncumbent() != null)
			return;
		
		if (current.isGoal()) {
			this.setIncumbent(new Solution(null, currentG, l.getDuration(),
					depth, l.getExpansions(), l.getGenerations(), l
							.getDuplicates()));
			return;
		}
		int nChildren = current.nChildren();
		l.incrExp();
		
		for (int i = 0; i < nChildren && l.keepGoing(); i++) {
			if(super.getIncumbent() != null)
				break;
			int inverse = current.inverseChild(i);
			// don't do the inverse
			if (inverse == currentID){
				continue;
			}
			double operatorCost = current.convertToChild(i, currentID);
			if(operatorCost < 0)
				continue;

			l.incrGen();
			// outside the bound
			double currentF = operatorCost + currentG + current.h();
			if (currentF > bound) {
				//just do the inverse
				current.convertToChild(inverse, -1);
				
				
				if (currentF < nextBound)
					nextBound = currentF;
				continue;
			} else if(super.getIncumbent() == null ){
				doIteration(current, i, currentG + operatorCost, depth + 1);
				//just do the inverse
				current.convertToChild(current.inverseChild(i), -1);
			}
		}
	}

	@Override
	public ArrayList<SearchState> solve() {
		l.startClock();
		
		SearchState initial = prob.getInitial();
		this.bound = initial.h();
		nextBound = Double.MAX_VALUE;
		
		while(super.getIncumbent() == null && l.keepGoing()){
			this.doIteration(initial, -1, 0, 0);
			bound = nextBound;
			nextBound = Double.MAX_VALUE;
		}
		l.endClock();
		return null;
	}

}
