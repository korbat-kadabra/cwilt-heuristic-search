package org.cwilt.search.algs.experimental;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

import org.cwilt.search.algs.basic.Beam;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchState;import org.cwilt.search.utils.basic.MinMaxHeap;
public class RandomizedBeam extends Beam {
	public static class RandomFGComparator implements Comparator<SearchNode>{
		@Override
		public int compare(SearchNode o1, SearchNode o2) {
			RBSearchNode sbo1 = (RBSearchNode) o1;
			RBSearchNode sbo2 = (RBSearchNode) o2;
			if(sbo1.randomValue < sbo2.randomValue)
				return 1;
			else if(sbo1.randomValue > sbo2.randomValue)
				return -1;
			else
				return 0;
		}
	}

	private final class RBSearchNode extends org.cwilt.search.search.SearchNode{
		/**
		 * 
		 */
		private static final long serialVersionUID = 5765221290861517820L;


		public String toString(){
			return "f " + getF() + " rand " + randomValue + "\n";
		}
		private RBSearchNode(org.cwilt.search.search.SearchNode parent, SearchState s, double g){
			super(parent, s, g);
			
			randomValue = r.nextDouble();
		}
		private final double randomValue;

		
		public ArrayList<SearchNode> expand() {
			ArrayList<SearchState.Child> baseChildren = super.s.expand();
			ArrayList<SearchNode> children = new ArrayList<SearchNode>();
			Iterator<SearchState.Child> it = baseChildren.iterator();
			while (it.hasNext()) {
				SearchState.Child c = it.next();
				children.add(new RBSearchNode(this, c.child, c.transitionCost + g));
			}
			return children;
		}
	}

	
	private float aggression;
	private Random r;

	public RandomizedBeam(org.cwilt.search.search.SearchProblem initial, Limit l, int beamWidth,
			float aggression) {
		super(initial, l, beamWidth);
		this.aggression = aggression;
		this.r = new Random();
	}

	public RandomizedBeam clone() {
		return new RandomizedBeam(prob, l.clone(), beamWidth, aggression);
	}

	protected void processLayer() {
		float prob = r.nextFloat();
		// sometimes, just do an ordinary beam search
		if (prob < aggression) {
			super.processLayer();
		}
		// otherwise just take totally random nodes.
		else {
			children = new MinMaxHeap<SearchNode>(new RandomFGComparator());
			super.processLayer();
		}
	}

	public RBSearchNode makeRBInitial(SearchState s){
		return new RBSearchNode(null, s, 0);
	}

	
	@Override
	public ArrayList<SearchState> solve() {
		assert(initial != null);
		SearchNode i = makeRBInitial(initial);
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
