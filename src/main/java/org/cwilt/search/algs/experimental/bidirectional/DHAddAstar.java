package org.cwilt.search.algs.experimental.bidirectional;
import java.util.ArrayList;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;
public class DHAddAstar extends HAddAStar{
	protected final double ratio;
	protected final double weight;
	

	public DHAddAstar(SearchProblem prob, Limit l, double ratio, double weight) {
		super(prob, l, -1, new WHAComp(weight));
		assert(weight >= 1);
		assert(ratio > 0);
		this.ratio = ratio;
		this.weight = weight;
	}


	protected boolean keepGoing() {
		if (open.isEmpty())
			return false;
		if (!l.keepGoing())
			return false;
		if (getIncumbent() == null)
			return true;
		else {
			HAddSearchNode next = open.peek();
			double openHead = next.getG() + weight * next.getH();
			double incCost = getIncumbent().getCost();
			if(openHead < incCost){
				return true;
			} else
				return false;
		}
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

		int fExp = 100;
		while (keepGoing()) {
			int nextExp = fExp * 2;
			// double the expansions
			while (keepGoing() && fExp < nextExp) {
				HAddSearchNode current = open.poll();
				assert(current.r == REASON.EXP);
				HAddSearchNode prev = closed.get(current.getState().getKey());
				if(prev != current)
				{
					if(prev.getG() > current.getG()){
						System.err.println("skipping a node - incumbent is worse");
						continue;
					}
				}				
				processNode(current);
				fExp++;
			}
			//don't increase the cache the last time through.
			if(!keepGoing()){
				break;
			}

			increaseCache((int) (nextExp * ratio));
			if(weight > 1.0)
				open.reHeapify();
			//System.err.printf("Herr\t%d\t%f\n", super.cacheSize, super.hAdd);
			//System.err.printf("sr.addData(Math.log10(%d), %f);\n", l.getExpansions() - super.cacheSize, open.peek().getF());
			//System.err.println();
		}
		l.endClock();
	}


}
