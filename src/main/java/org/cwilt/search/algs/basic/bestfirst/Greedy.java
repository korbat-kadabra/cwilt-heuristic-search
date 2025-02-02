/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.algs.basic.bestfirst;
import java.io.PrintStream;
import java.util.ArrayList;

import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.basic.MinHeap;
public class Greedy extends BestFirstSearch {
	public SearchAlgorithm clone(){
		assert(l.getGenerations() == 0);
		Greedy a = new Greedy(prob, l.clone());
		return a;
	}
	
	protected final ArrayList<Double> counts;
	protected double openHead = Double.MAX_VALUE;
	protected int currentMinimumSize = 0;
	protected double highWater = 0;
	
	@Override
	protected double evaluateNode(SearchNode n){
		double nextH = n.getH();
		if(nextH > highWater){
			highWater = nextH;
		}
		if(openHead >= nextH){
			currentMinimumSize ++;
		}
		else {
			if(currentMinimumSize > 0){
				counts.add(new Double(currentMinimumSize));
			}
			currentMinimumSize = 0;
		}
		
		openHead = nextH;
		return nextH;
	}
	
	protected boolean solutionGoodEnough(){
		Solution inc = super.getIncumbent();
		if(inc == null)
			return false;
		else
			return true;
	}

	public Greedy(org.cwilt.search.search.SearchProblem initial, org.cwilt.search.search.Limit l) {
		super(initial, l);
		open = new MinHeap<SearchNode>(new SearchNode.HComparator());
		this.counts = new ArrayList<Double>();
	}
	@Override
	public void printExtraData(PrintStream ps){
		super.printExtraData(ps);
		double min = Double.MAX_VALUE;
		double max = 0;
		for(Double d : counts){
			if(d < min)
				min = d;
			if(d > max)
				max = d;
		}
		SearchAlgorithm.printPair(ps, "highwater", new Double(highWater));
		SearchAlgorithm.printPair(ps, "minRun", new Double(min));
		SearchAlgorithm.printPair(ps, "maxRun", new Double(max));
		SearchAlgorithm.printPair(ps, "avgRun", new Double(org.cwilt.search.utils.basic.Stats.mean(counts)));
		SearchAlgorithm.printPair(ps, "stdevRun", new Double(org.cwilt.search.utils.basic.Stats.stdev(counts)));
	}
}
