package org.cwilt.search.algs.basic;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;
public class IDAStarCleanup extends org.cwilt.search.search.SearchAlgorithm{
	protected final HashMap<Object, SearchNode> expanded;
	protected final HashMap<Object, SearchNode> globalExpanded;
	protected int internalExp = 0;
	
	public SearchAlgorithm clone() {
		IDAStarCleanup i = new IDAStarCleanup(prob, l.clone(), iterations);
		return i;
	}
	
	private final double bounds[];
	private final int iterations;

	public IDAStarCleanup(org.cwilt.search.search.SearchProblem initial, Limit l, int iterations) {
		super(initial, l);
		if(iterations < 1)
			throw new IllegalArgumentException("Can't do less than 1 iteration");
		
		this.iterations = iterations;
		this.expanded = new HashMap<Object, SearchNode>();
		this.globalExpanded = new HashMap<Object, SearchNode>();
		org.cwilt.search.algs.basic.bestfirst.AStar a = new org.cwilt.search.algs.basic.bestfirst.AStar(prob, new Limit());
		a.solve();
		this.bounds = new double[iterations];
		double current = super.initial.h();
		this.bounds[iterations - 1] = a.getFinalCost();

		double delta = (this.bounds[iterations - 1] - current) / iterations;
		for(int i = 0; i < iterations - 1; i++){
			this.bounds[i] = current + delta;
			current += delta;
		}
		
//		System.err.println(Arrays.toString(bounds));
		
	}

	@Override
	public SearchState findFirstGoal() {
		assert (false);
		System.err.println("IDA* only runs once.");
		System.exit(1);
		// TODO Auto-generated method stub
		return null;
	}

	protected double evaluateNode(SearchNode n) {
		return n.getF();
	}
	
	protected void doIteration(double bound, SearchNode n) {
		if (evaluateNode(n) > bound) {
			//don't have to do anything for nodes that are outside the bound
		} else {
			if (n.getState().isGoal()) {
				setIncumbent ( new Solution(n, n.getG(), l.getDuration(), n
						.pathLength(), l.getExpansions(), l.getGenerations(), l
						.getDuplicates()));
				return;
			}
			if(super.getIncumbent() != null){
				return;
			}

			SearchNode incumbent = expanded.get(n.getState().getKey());
			if(incumbent != null && incumbent.getF() <= n.getF()){
				//child is junk
				l.incrDup();
				return;
			}
			if(incumbent != null){
				l.incrReExp();
			}

			SearchNode incumbent2 = globalExpanded.get(n.getState().getKey());
			if(incumbent2 != null && incumbent2.getF() < n.getF()){
				//child is junk
				l.incrDup();
				return;
			}
			if(incumbent2 != null){
				this.internalExp ++;
			}
			
			expanded.put(n.getState().getKey(), n);
			
			Iterator<? extends SearchNode> i = n.expand().iterator();
			l.incrExp();
			while (getIncumbent() == null && i.hasNext()) {
				l.incrGen();
				SearchNode nextNode = i.next();				
				doIteration(bound, nextNode);
			}
		}
	}

	private void setIncumbent() {
		l.startClock();
		SearchNode i = SearchNode.makeInitial(initial);
		for(int index = 0; index < bounds.length; index ++){
			doIteration(bounds[index], i);
			globalExpanded.putAll(expanded);
			expanded.clear();
		}
		l.endClock();
	}

	@Override
	public ArrayList<SearchState> solve() {
		try {
			setIncumbent();
		} catch (OutOfMemoryError e) {
			l.endClock();
			l.setOutOfMemory();
		}
		if (getIncumbent() == null)
			return null;
		else {
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			return finalPath;
		}
	}

	public void cleanup() {
		return;
	}

	@Override
	public void reset() {
		expanded.clear();
	}
	
	@Override
	public void printExtraData(PrintStream ps){
		super.printExtraData(ps);
		SearchAlgorithm.printPair(ps, "internal expansions", new Integer(internalExp));
	}
}
