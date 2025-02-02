package org.cwilt.search.algs.experimental.bidirectional;
import java.util.ArrayList;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;
public class DHAddIDAStar extends HAddIDAStar{
	private final double ratio;
	
	public DHAddIDAStar(SearchProblem prob, Limit l, double ratio) {
		super(prob, l, 0);
		this.ratio = ratio;
	}
	


	protected void setIncumbent() {
		l.startClock();

		ArrayList<SearchState> goals = prob.getGoals();

		
		for (SearchState goalState : goals) {
			HAddSearchNode goal = this.makeInitialReverse(goalState);
			gb.add(goal);
			borderNodes.put(goal.getState().getKey(), goal);
		}
		increaseCache(10);

		assert (hAdd != Double.MAX_VALUE);
		
		
		SearchState initial = prob.getInitial();
		this.bound = initial.h();
		nextBound = Double.MAX_VALUE;

		while (super.getIncumbent() == null && l.keepGoing()) {
			long currentExpansions = l.getExpansions();
			this.doIteration(initial, -1, 0, 0);
			long expanded = l.getExpansions() - currentExpansions;
			
			increaseCache((int) (expanded * ratio));
			
			bound = nextBound;
			nextBound = Double.MAX_VALUE;
		}

		
		
		l.endClock();
	}


	

}
