/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.algs.basic.bestfirst;
import java.util.Comparator;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;
public class AStarAll extends BestFirstSearchAll {

	public AStarAll(SearchProblem initial, Limit l) {
		super(initial, l);
	}

	@Override
	protected Comparator<SearchNode> getComparator() {
		return new SearchNode.FGComparator();
	}

}
