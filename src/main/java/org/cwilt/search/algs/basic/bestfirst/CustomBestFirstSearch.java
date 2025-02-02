package org.cwilt.search.algs.basic.bestfirst;

import java.util.Comparator;

import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchNode;
import org.cwilt.search.search.SearchProblem;
import org.cwilt.search.search.Solution;
import org.cwilt.search.utils.basic.MinHeap;

public class CustomBestFirstSearch extends BestFirstSearch{
	
	public final Comparator<SearchNode> comparator;
	
	public CustomBestFirstSearch(SearchProblem initial, Limit l, Comparator<SearchNode> comparator) {
		super(initial, l);
		this.comparator = comparator;
		open = new MinHeap<SearchNode>(comparator);
	}

	@Override
	protected boolean solutionGoodEnough() {
		Solution inc = super.getIncumbent();
		if (inc == null)
			return false;
		SearchNode goalNode = inc.getGoal();
		SearchNode headOpen = open.peek();
		if(headOpen == null){
			return true;
		}
		
		int rv = this.comparator.compare(goalNode, headOpen);
		if(rv <= 0){
			return true;
		} else {
			return false;
		}
	}

	
	public SearchAlgorithm clone() {
		assert (l.getGenerations() == 0);
		CustomBestFirstSearch a = new CustomBestFirstSearch(prob, l.clone(), this.comparator);
		return a;
	}


}
