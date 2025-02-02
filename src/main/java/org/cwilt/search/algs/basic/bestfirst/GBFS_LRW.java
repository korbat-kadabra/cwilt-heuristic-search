package org.cwilt.search.algs.basic.bestfirst;
import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.utils.basic.MinHeap;
public class GBFS_LRW extends BestFirstSearch{

	private final int stallSize;
	private final int maxLocalTry;
	
	protected GBFS_LRW(SearchProblem initial, Limit l) {
		super(initial, l);
		this.stallSize = 1000;
		this.maxLocalTry = 10;
		open = new MinHeap<SearchNode>(new SearchNode.HComparator());
	}

	
	protected GBFS_LRW(SearchProblem initial, Limit l, int stallSize, int maxLocalTry) {
		super(initial, l);
		this.stallSize = stallSize;
		this.maxLocalTry = maxLocalTry;
		open = new MinHeap<SearchNode>(new SearchNode.HComparator());
	}

	@Override
	protected boolean solutionGoodEnough() {
		return true;
	}

	@Override
	public SearchAlgorithm clone() {
		return new GBFS_LRW(super.prob, l.clone(), stallSize, maxLocalTry);
	}

}
