package org.cwilt.search.algs.basic.bestfirst;

import java.io.PrintStream;
import java.util.ArrayList;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.basic.MinHeap;
public class DCAStar extends AStar{

	public SearchAlgorithm clone(){
		return new DCAStar(prob, l.clone());
	}
	
	float branchingCount;
	float uniqueBranchingCount;
	float expCount;
	
	public DCAStar(SearchProblem initial, Limit l) {
		super(initial, l);
		open = new MinHeap<SearchNode>(new SearchNode.FGComparator());
		branchingCount = 0;
		uniqueBranchingCount = 0;
		expCount = 0;
	}

	protected Solution processNode(SearchNode current) {
		if (current.getState().isGoal()) {
			if (getIncumbent() == null || getIncumbent().getCost() > current.getG())
				return new Solution(current, current.getG(), l.getDuration(),
						current.pathLength(), l.getExpansions(), l.getGenerations(),
						l.getDuplicates());
		} else {
			ArrayList<? extends SearchNode> children = current.expand();
			branchingCount += children.size();
			l.incrExp();
			expCount ++;
			for (SearchNode child : children) {
				if(!closed.containsKey(child.getState().getKey())){
					uniqueBranchingCount ++;
				}
				l.incrGen();
				considerChild(child);
			}
		}
		return null;
	}
	
	@Override
	public void printExtraData(PrintStream ps){
		super.printExtraData(ps);
		printPair(ps, "branch", new Double(branchingCount / expCount));
		printPair(ps, "branchUnique", new Double(uniqueBranchingCount / expCount));
	}


}
