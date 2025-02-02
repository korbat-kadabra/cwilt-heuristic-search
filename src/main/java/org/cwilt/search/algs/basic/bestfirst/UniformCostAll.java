package org.cwilt.search.algs.basic.bestfirst;
import java.util.Comparator;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;
public class UniformCostAll extends BestFirstSearchAll {

	public UniformCostAll(SearchProblem initial, Limit l) {
		super(initial, l);
	}

	@Override
	protected Comparator<SearchNode> getComparator() {
		return new SearchNode.GComparator();
	}

}
