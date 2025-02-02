package org.cwilt.search.algs.basic.bestfirst;
import java.io.PrintStream;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchNode;import org.cwilt.search.utils.basic.MinHeap;
public class Speedy extends Greedy {

	public Speedy(SearchProblem initial, Limit l) {
		super(initial, l);
		super.open = new MinHeap<SearchNode>(new SearchNode.DComparator());
		initial.setCalculateD();
	}

	@Override
	protected double evaluateNode(SearchNode n){
		double nextD = n.getD();
		if(openHead >= nextD){
			currentMinimumSize ++;
		}
		else {
			if(currentMinimumSize > 0){
				counts.add(new Double(currentMinimumSize));
			}
			currentMinimumSize = 0;
		}
		
		openHead = nextD;
		return nextD;
	}

	@Override
	public void printSearchData(PrintStream ps){
		super.printSearchData(ps);
		SearchAlgorithm.printPair(ps, "initial d", new Integer(initial.d()));
	}
}
