package org.cwilt.search.algs.experimental.bidirectional;
import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchProblem;

public class WBHAddAStar extends BHAddAStar {
	private final double weight;
	
	public WBHAddAStar(SearchProblem prob, Limit l, double weight) {
		super(prob, l, new WHAComp(weight));
		this.weight = weight;
	}

	public BHAddAStar clone(){
		return new WBHAddAStar(prob, l, weight);
	}
}
