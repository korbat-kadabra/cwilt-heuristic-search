package org.cwilt.search.algs.basic;
import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;
public class WIDAStar extends IDAStar {

	private final double weight;
	
	public WIDAStar(SearchProblem initial, Limit l, double weight) {
		super(initial, l);
		this.weight = weight;
	}

	public SearchAlgorithm clone(){
		return new WIDAStar(prob, l.clone(), weight);
	}
	
	protected double evaluateNode(SearchNode n){
		return weight * n.getH() + n.getG();
	}
}
