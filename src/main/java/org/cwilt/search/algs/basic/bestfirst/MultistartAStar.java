package org.cwilt.search.algs.basic.bestfirst;
import java.util.ArrayList;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.basic.MinHeap;
public class MultistartAStar extends BestFirstSearch {

	public interface MultistartSearchProblem extends org.cwilt.search.search.SearchProblem {
		public ArrayList<SearchState> getStarts();
	}

	public MultistartAStar(MultistartSearchProblem prob, Limit l) {
		super(prob, l);
		open = new MinHeap<SearchNode>(new SearchNode.FGComparator());
	}

	@Override
	public SearchAlgorithm clone() {
		return new MultistartAStar((MultistartSearchProblem) prob, l);
	}
	
	protected boolean solutionGoodEnough(){
		Solution inc = super.getIncumbent();
		if(inc == null)
			return false;
		if(inc.getCost() <= open.peek().getF())
			return true;
		else
			return false;
	}


	@Override
	public ArrayList<SearchState> solve() {
		for (SearchState initial : ((MultistartSearchProblem) prob).getStarts()) {
			SearchNode i = SearchNode.makeInitial(initial);
			open.add(i);
			closed.put(i.getState().getKey(), i);
		}
		try {
			setIncumbent();
		} catch (OutOfMemoryError e) {
			l.setOutOfMemory();
			l.endClock();
			open.clear();
			closed.clear();
		}
		if (getIncumbent() == null)
			return null;
		else {
			// System.err.println(incumbent.getGoal().printParents());
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			return finalPath;
		}
	}

}
