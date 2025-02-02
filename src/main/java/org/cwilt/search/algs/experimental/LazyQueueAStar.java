package org.cwilt.search.algs.experimental;

import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchProblem;
import org.cwilt.search.search.Solution;
import org.cwilt.search.utils.experimental.FastFloatHeap;

public class LazyQueueAStar extends
		org.cwilt.search.algs.basic.bestfirst.BestFirstSearch {

	public LazyQueueAStar(SearchProblem prob, Limit l) {
		super(prob, l);
		this.split = SPLIT;
		double h = prob.getInitial().h();
		open = new org.cwilt.search.utils.experimental.FastFloatHeap<org.cwilt.search.search.SearchNode>(
				new org.cwilt.search.search.SearchNode.FGComparator(), h,
				h * 1.5, split);
	}

	protected boolean solutionGoodEnough() {
		Solution inc = super.getIncumbent();
		if (inc == null)
			return false;
		if (inc.getCost() <= open.peek().getF())
			return true;
		else
			return false;
	}

	private final int split;

	public LazyQueueAStar(SearchProblem prob, Limit l, int s) {
		super(prob, l);
		this.split = s;
		double h = prob.getInitial().h();
		open = new FastFloatHeap<org.cwilt.search.search.SearchNode>(
				new org.cwilt.search.search.SearchNode.FGComparator(), h, h * 1.5, split);
	}

	private static final int SPLIT = 5000;

	public SearchAlgorithm clone() {
		assert (l.getGenerations() == 0);
		LazyQueueAStar a = new LazyQueueAStar(prob, l.clone(), SPLIT);
		return a;
	}

}
