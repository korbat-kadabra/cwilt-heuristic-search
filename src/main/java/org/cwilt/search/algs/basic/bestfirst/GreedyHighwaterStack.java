package org.cwilt.search.algs.basic.bestfirst;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;
public class GreedyHighwaterStack extends Greedy{
	Stack<SearchNode> highwaterStack = new Stack<SearchNode>();
	HashMap<Double, AtomicInteger> counts = new HashMap<Double, AtomicInteger>();
	public GreedyHighwaterStack(SearchProblem initial, Limit l) {
		super(initial, l);
	}
	
	@Override
	protected double evaluateNode(SearchNode n){
		int amtToAdd = 0;
		if(highwaterStack.isEmpty()){
			//don't do anything
		} else if (highwaterStack.peek().getH() <= n.getH()){
			while(!highwaterStack.isEmpty() && highwaterStack.peek().getH() <= n.getH()){
				SearchNode popped = highwaterStack.pop();
				
				if(popped.getH() < n.getH()){
					AtomicInteger i = counts.remove(new Double(popped.getH()));
					amtToAdd += i.get();
					//reset the counter
				} else if (popped.getH() == n.getH()){
					//increment the counter and pop
					counts.get(new Double(popped.getH())).incrementAndGet();
				}
			}
		}
		highwaterStack.push(n);
		
		AtomicInteger i = counts.get(new Double(n.getH()));
		if(i == null){
			i = new AtomicInteger(1);
			counts.put(n.getH(), i);
		}
		
		counts.get(new Double(n.getH())).getAndAdd(amtToAdd);
		assert(!highwaterStack.isEmpty());
		return super.evaluateNode(n);
	}
	
	@Override
	public void printSearchData(PrintStream ps){
		super.printSearchData(ps);
		SearchAlgorithm.printPair(ps, "highwater_stack_size", new Integer(highwaterStack.size()));

		SearchAlgorithm.printPair(ps, "highwater_top", counts.get(highwaterStack.peek().getH()));

		int c = 0;
		for(SearchNode n : highwaterStack){
			c += counts.get(n.getH()).get();
			SearchAlgorithm.printPair(ps, "hw " + n.getH(), counts.get(n.getH()));
		}
		
		SearchAlgorithm.printPair(ps, "highwater_count", new Integer(c));
		
		//		ArrayList<SearchState> solution = this.getIncumbent().reconstructPath();
//		ArrayList<Object> keys = new ArrayList<Object>(solution.size());
//		for(SearchState s : solution)
//			keys.add(s.getKey());
//		while(!highwaterStack.isEmpty()){
//			SearchNode next = highwaterStack.pop();
//			if(keys.contains(next.getState().getKey())){
//				System.err.println("Found key");
//			} else {
//				System.err.println("Missing key");
//			}
//		}
	}
}
