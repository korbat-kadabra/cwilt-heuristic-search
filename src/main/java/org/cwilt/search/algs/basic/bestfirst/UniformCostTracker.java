package org.cwilt.search.algs.basic.bestfirst;
import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;
public class UniformCostTracker extends org.cwilt.search.algs.basic.bestfirst.UniformCost {

	
	public SearchAlgorithm clone(){
		assert(l.getGenerations() == 0);
		UniformCostTracker a = new UniformCostTracker(prob, l.clone());
		return a;
	}
	public TreeMap<Double, AtomicInteger> nodes = new TreeMap<Double, AtomicInteger>();
	
	public UniformCostTracker(org.cwilt.search.search.SearchProblem initial, org.cwilt.search.search.Limit l) {
		super(initial, l);
	}
	protected double evaluateNode(SearchNode n){
		Double nextValue = n.getG();
		AtomicInteger nextCount = nodes.get(nextValue);
		if(nextCount != null)
			nextCount.incrementAndGet();
		else
			nodes.put(nextValue, new AtomicInteger(1));
		return nextValue;
	}

	
	@Override
	public void printExtraData(PrintStream ps){
		super.printExtraData(ps);
		StringBuffer b = new StringBuffer();
		for(Map.Entry<Double, AtomicInteger> v : nodes.entrySet()){
			ps.append(v.getKey().toString());
			ps.append(" ");
			ps.append(v.getValue().toString());
			ps.append("\n");
		}
		ps.print(b.toString());
	}
	
}
