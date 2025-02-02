package org.cwilt.search.algs.experimental;
import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.utils.experimental.RandomizedMinHeap;
public class RandomWAStar extends org.cwilt.search.algs.basic.bestfirst.WAStar{

	private final double pRandom;
	
	public SearchAlgorithm clone(){
		return new RandomWAStar(prob, l.clone(), weight, pRandom);
	}
	
	public RandomWAStar(SearchProblem initial, Limit l, double weight, double pRandom) {
		super(initial, l, weight);
		this.pRandom = pRandom;
		open = new RandomizedMinHeap<SearchNode>(new SearchNode.WFGComparator(weight), super.id, pRandom);
	}

}
