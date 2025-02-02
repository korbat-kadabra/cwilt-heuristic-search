package org.cwilt.search.algs.basic;
import java.util.ArrayList;
import java.util.Random;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;
public class RandomHillClimbing extends HillClimbing{

	private final Random r;
	
	public RandomHillClimbing(SearchProblem initial, Limit l) {
		super(initial, l);
		this.r = new Random(0);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void badChildren(ArrayList<? extends SearchNode> children){
		
		SearchNode current = children.get(r.nextInt(children.size()));
		while(true){
			ArrayList<? extends SearchNode> c = current.expand();
			l.incrExp();
			l.incrGen(c.size());
			
			for(SearchNode child : c){
				if(child.getH() < bestH){
					currentLocation = child;
					super.bestH = child.getH();
					closed.add(child.getState());
					return;
				}
			}
			current = c.get(r.nextInt(c.size()));
		}
	}


	@Override
	public SearchAlgorithm clone() {
		super.checkClone(RandomHillClimbing.class.getCanonicalName());
		return new RandomHillClimbing(prob, l.clone());
	}


}
