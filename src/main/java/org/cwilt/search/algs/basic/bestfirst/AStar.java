/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.algs.basic.bestfirst;

import java.io.PrintStream;

import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchNode;
import org.cwilt.search.search.Solution;
import org.cwilt.search.utils.basic.MinHeap;

public class AStar extends BestFirstSearch {

	protected boolean solutionGoodEnough() {
		Solution inc = super.getIncumbent();
		if (inc == null)
			return false;
		if (inc.getCost() <= open.peek().getF())
			return true;
		else
			return false;
	}

	public SearchAlgorithm clone() {
		assert (l.getGenerations() == 0);
		AStar a = new AStar(prob, l.clone());
		return a;
	}

	public AStar(org.cwilt.search.search.SearchProblem initial, org.cwilt.search.search.Limit l) {
		super(initial, l);
		open = new MinHeap<SearchNode>(new SearchNode.FGComparator());
	}

	@Override
	public void printExtraData(PrintStream ps) {
		super.printExtraData(ps);
		printPair(ps, "end open size", new Integer(open.size()));
	}
}
