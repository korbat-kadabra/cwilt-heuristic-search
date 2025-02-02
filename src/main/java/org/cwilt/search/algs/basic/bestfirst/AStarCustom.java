package org.cwilt.search.algs.basic.bestfirst;
import java.util.Comparator;
import java.util.Map;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.utils.basic.MinHeap;
public class AStarCustom extends AStar {
	public double getMinimumH(){
		double minH = Double.MAX_VALUE;
		for(Map.Entry<Object, SearchNode> n : closed.entrySet()){
			if(n.getValue().getH() < minH){
				minH = n.getValue().getH();
			}
		}
		return minH;
	}
	
	public AStarCustom(SearchProblem initial, Limit l, Comparator<SearchNode> cmp) {
		super(initial, l);
		open = new MinHeap<SearchNode>(cmp);
	}

}
