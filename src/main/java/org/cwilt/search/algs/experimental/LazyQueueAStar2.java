package org.cwilt.search.algs.experimental;
import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.Solution;
public class LazyQueueAStar2 extends org.cwilt.search.algs.basic.bestfirst.BestFirstSearch {

	public LazyQueueAStar2(SearchProblem prob, Limit l) {
		super(prob, l);
		this.split = SPLIT;
		open = new org.cwilt.search.utils.experimental.FasterFloatHeap<org.cwilt.search.search.SearchNode>(0, Double.MAX_VALUE, new org.cwilt.search.search.SearchNode.FGComparator(), split);
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

	
	private final int split;
	
	public LazyQueueAStar2(SearchProblem prob, Limit l, int s) {
		super(prob, l);
		this.split = s;
		open = new org.cwilt.search.utils.experimental.FasterFloatHeap<org.cwilt.search.search.SearchNode>(0, Double.MAX_VALUE, new org.cwilt.search.search.SearchNode.FGComparator(), split);
	}

	private static final int SPLIT = 5000;
	
	public SearchAlgorithm clone(){
		assert(l.getGenerations() == 0);
		LazyQueueAStar a = new LazyQueueAStar(prob, l.clone(), SPLIT);
		return a;
	}

}
