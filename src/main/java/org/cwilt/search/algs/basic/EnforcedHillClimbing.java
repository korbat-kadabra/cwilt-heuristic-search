/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.algs.basic;

import java.util.ArrayList;
import java.util.LinkedList;


import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;
public class EnforcedHillClimbing extends HillClimbing {

	public EnforcedHillClimbing(org.cwilt.search.search.SearchProblem initial, Limit l) {
		super(initial, l);
	}

	@Override
	protected void badChildren(ArrayList<? extends SearchNode> children){
		LinkedList<SearchNode> open = new LinkedList<SearchNode>();
		open.addAll(children);
		while(true){
			SearchNode next = open.removeFirst();
			ArrayList<? extends SearchNode> c = next.expand();
			l.incrExp();
			l.incrGen(c.size());
			for(SearchNode child : c){
				if(child.getH() < bestH){
					currentLocation = child;
					super.bestH = child.getH();
					closed.add(child.getState());
					return;
				}
				else if(!closed.contains(child.getState())){
					open.add(child);
				}
				else
					l.incrDup();
			}
		}
	}


	@Override
	public SearchAlgorithm clone() {
		super.checkClone(EnforcedHillClimbing.class.getCanonicalName());
		return new EnforcedHillClimbing(prob, l.clone());
	}


}
