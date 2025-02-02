package org.cwilt.search.search;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.cwilt.search.utils.basic.Stats;
public class TauEstimator extends SearchAlgorithm{
	private final HashMap<Object, SearchNodeDepth> closed;
	private final LinkedList<SearchNodeDepth> open;
	
	public TauEstimator(SearchProblem prob, Limit l, double pKept) {
		super(prob, l);
		this.closed = new HashMap<Object, SearchNodeDepth>();
		this.open = new LinkedList<SearchNodeDepth>();
		this.pKept = pKept;
	}
	private final double pKept;
	@Override
	public void reset() {
		this.open.clear();
		this.closed.clear();
	}

	@Override
	protected void cleanup() {
		this.reset();
	}

	@Override
	public SearchAlgorithm clone() {
		return new TauEstimator(prob, l.clone(), pKept);
	}

	@Override
	public ArrayList<SearchState> solve() {
		SearchNodeDepth initial = SearchNodeDepth.makeInitial(prob.getGoal());
		open.add(initial);
		closed.put(initial.getState().getKey(), initial);
		l.startClock();
		while(!open.isEmpty() && l.keepGoing()){
			SearchNode next = open.poll();
			@SuppressWarnings("unchecked")
			ArrayList<? extends SearchNodeDepth> children = (ArrayList<? extends SearchNodeDepth>) next.reverseExpand();
			l.incrExp();
			l.incrGen(children.size());
			for(SearchNodeDepth n : children){
				SearchNodeDepth incumbent = closed.get(n.getState().getKey());
				if(incumbent == null){
					open.add(n);
					closed.put(n.getState().getKey(), n);
				} else {
					l.incrDup();
				}
				
			}
		}
		
		l.endClock();
		Iterator<Entry<Object, SearchNodeDepth>> it = closed.entrySet().iterator();
//		double[] hValues = new double[closed.size()];
//		double[] dValues = new double[closed.size()];
		ArrayList<Double> hValues = new ArrayList<Double>(closed.size());
		ArrayList<Double> dValues = new ArrayList<Double>(closed.size());
		
	    while (it.hasNext()) {
	        Map.Entry<Object, SearchNodeDepth> pairs = it.next();
	        if(r.nextDouble() <= this.pKept){
	        	hValues.add(pairs.getValue().getH());
	        	dValues.add((double) pairs.getValue().getDepth());
	        }
	    }
	    
	    double[] h = new double[hValues.size()];
	    double[] d = new double[hValues.size()];
	    
	    for(int i = 0; i < hValues.size(); i ++){
	    	h[i] = hValues.get(i);
	    	d[i] = dValues.get(i);
	    }
	    
	    final int NSAMPLES = 1000;
	    
	    double[] hSamples = new double[NSAMPLES];
	    double[] dSamples = new double[NSAMPLES];
	    
	    for(int i = 0; i < NSAMPLES; i++){
	    	int index = r.nextInt(hValues.size());
	    	hSamples[i] = h[index];
	    	dSamples[i] = d[index];
	    }
	    
	    tau = Stats.kendallTauBeta(hSamples, dSamples);
		return null;
	}
	private double tau;
	
	private final Random r = new Random(0);
	
	@Override
	public void printExtraData(PrintStream ps){
		SearchAlgorithm.printPair(ps, "tau", new Double(tau));
	}
}
