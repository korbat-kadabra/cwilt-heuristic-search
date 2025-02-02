package org.cwilt.search.algs.experimental.bidirectional;
import java.util.Comparator;


import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;
public class DGHAddAstar extends DHAddAstar{

	public DGHAddAstar(SearchProblem prob, Limit l, double ratio, double weight) {
		super(prob, l, ratio, weight);
		throw new RuntimeException("This doesn't work any more now that nodes are only expanded in error order");
	}

	public static class GComp implements Comparator<HAddSearchNode>{

		public Comparator<SearchNode> n = new SearchNode.GComparator();
		@Override
		public int compare(HAddSearchNode o1, HAddSearchNode o2) {
			return n.compare(o1, o2);
		}
		
	}

	protected Comparator<HAddSearchNode> getComparator(){
		return new GComp();
	}
	@Override

	public SearchAlgorithm clone() {
		return new DGHAddAstar(prob, l.clone(), ratio, weight);
	}

	
}
