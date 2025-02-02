package org.cwilt.search.algs.experimental.bidirectional;
import java.util.ArrayList;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;
public class DHAddAStarSingle extends HAddAStar{
	protected final double ratio;
	private final int fExp;
	private final int bExp;

	public DHAddAStarSingle(SearchProblem prob, Limit l, double ratio) {
		super(prob, l, -1, new WHAComp(1));
		assert(ratio > 0);
		this.ratio = ratio;
		if(ratio <= 1){
			bExp = 1;
			fExp = (int) (1.0d / ratio);
		} else {
			throw new RuntimeException("invalid ratio selection");
		}
		
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
			double openHead = next.getG() + next.getH();
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
//		increaseCache(10);

		assert (hAdd != Double.MAX_VALUE);

		while (keepGoing()) {
			// double the expansions
			int currentExp = 0;
			while (keepGoing() && currentExp < fExp) {
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
				currentExp ++;
			}
			//don't increase the cache the last time through.
			if(!keepGoing()){
				break;
			}

			increaseCache(bExp);
			//System.err.printf("Herr\t%d\t%f\n", super.cacheSize, super.hAdd);
			//System.err.printf("sr.addData(Math.log10(%d), %f);\n", l.getExpansions() - super.cacheSize, open.peek().getF());
			//System.err.println();
		}
		l.endClock();
	}


}
