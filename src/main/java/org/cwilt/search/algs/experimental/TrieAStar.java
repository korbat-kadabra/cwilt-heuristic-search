package org.cwilt.search.algs.experimental;
import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.experimental.BinHeap;import org.cwilt.search.algs.basic.bestfirst.BestFirstSearch;
public class TrieAStar extends BestFirstSearch {
	public SearchAlgorithm clone(){
		assert(l.getGenerations() == 0);
		TrieAStar a = new TrieAStar(prob, l.clone());
		return a;
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

	public TrieAStar(org.cwilt.search.search.SearchProblem initial, org.cwilt.search.search.Limit l) {
		super(initial, l);
		open = new BinHeap<SearchNode>(BinHeap.HEAPTYPE.FHEAP, 0.0);
	}

}
