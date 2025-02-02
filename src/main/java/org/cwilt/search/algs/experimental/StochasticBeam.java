package org.cwilt.search.algs.experimental;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchState;
public class StochasticBeam extends org.cwilt.search.algs.basic.Beam {
	
	static int instanceCount = 0;
	public SearchAlgorithm clone(){
		
		assert(l.getGenerations() == 0);
		StochasticBeam a = new StochasticBeam (prob, l.clone(), beamWidth);
		a.setSeed(r.nextLong());
		return a;
	}
	
	private void setSeed(long i){
		r.setSeed(i);
	}
	
	private final Random r;
	public static class RandomFGComparator implements Comparator<SearchNode>{
		@Override
		public int compare(SearchNode o1, SearchNode o2) {
			SBSearchNode sbo1 = (SBSearchNode) o1;
			SBSearchNode sbo2 = (SBSearchNode) o2;
			if(sbo1.randomValue < sbo2.randomValue)
				return 1;
			else if(sbo1.randomValue > sbo2.randomValue)
				return -1;
			else
				return 0;
		}
	}
	public SBSearchNode makeSBInitial(SearchState s){
		return new SBSearchNode(null, s, 0);
	}
	
	private final class SBSearchNode extends org.cwilt.search.search.SearchNode{
		/**
		 * 
		 */
		private static final long serialVersionUID = -5445486783097709522L;


		public String toString(){
			return "f " + getF() + " rand " + randomValue + "\n";
		}
		private SBSearchNode(org.cwilt.search.search.SearchNode parent, SearchState s, double g){
			super(parent, s, g);
			
			double range = worstF - bestF;
			if(range == 0)
				range = 1;
			double quality = 1 - ((getF() - bestF) / (range));
			
			if(quality > AGGRESSION)
				quality = AGGRESSION;
			else if(quality < (1 - AGGRESSION))
				quality = (1 - AGGRESSION);
			
			randomValue = Math.pow(r.nextDouble(), quality);
		}
		private final double randomValue;

		
		public ArrayList<SearchNode> expand() {
			ArrayList<SearchState.Child> baseChildren = super.s.expand();
			ArrayList<SearchNode> children = new ArrayList<SearchNode>();
			Iterator<SearchState.Child> it = baseChildren.iterator();
			while (it.hasNext()) {
				SearchState.Child c = it.next();
				children.add(new SBSearchNode(this, c.child, c.transitionCost + g));
			}
			return children;
		}
	}
	
	
	private double bestF;
	private double worstF;
	
	protected void processLayer(){
//		System.err.printf("id %d processing\n", id);
		this.bestF = Double.MAX_VALUE;
		this.worstF = 0;
		super.processLayer();
	}
	
	protected void considerChild(SearchNode child) {
		super.considerChild(child);
		if(child.getF() < bestF)
			bestF = child.getF();
		if(child.getF() > worstF)
			worstF = child.getF();
	}
	
	private final double AGGRESSION = .999;
	
	public StochasticBeam(org.cwilt.search.search.SearchProblem initial, Limit l, int beamWidth) {
		super(initial, l, beamWidth);
		r = new Random();
		++instanceCount;
		this.c = new RandomFGComparator();
	}
	

	@Override
	public ArrayList<SearchState> solve() {
		assert(initial != null);
		SearchNode i = makeSBInitial(initial);
		parents.insert(i);
		closed.put(i.getState().getKey(), i);

		setIncumbent();
		if (getIncumbent() == null)
			return null;
		else {
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			return finalPath;
		}
	}

	

}
