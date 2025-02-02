/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.algs.experimental;

import org.cwilt.search.algs.basic.bestfirst.BestFirstSearch;

import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.experimental.DBinHeap;
public class DTrieAStar extends BestFirstSearch {

	protected boolean solutionGoodEnough(){
		Solution inc = super.getIncumbent();
		if(inc == null)
			return false;
		if(inc.getCost() <= open.peek().getF())
			return true;
		else
			return false;
	}

	public SearchAlgorithm clone(){
		assert(l.getGenerations() == 0);
		DTrieAStar a = new DTrieAStar(prob, l.clone());
		return a;
	}
	
	public DTrieAStar(org.cwilt.search.search.SearchProblem initial, org.cwilt.search.search.Limit l) {
		super(initial, l);
		open = new DBinHeap<SearchNode>();
	}
	
}
