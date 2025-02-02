/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.algs.basic;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;
public class HillClimbing extends org.cwilt.search.search.SearchAlgorithm {

	final HashSet<SearchState> closed;
	protected SearchNode currentLocation;
	protected double bestH;
	private final Random r;
	
	public HillClimbing(org.cwilt.search.search.SearchProblem initial, Limit l) {
		super(initial, l);
		closed = new HashSet<SearchState>();
		currentLocation = null;
		this.bestH = 0;
		this.r = new Random(super.id);
	}

	@Override
	public SearchAlgorithm clone() {
		super.checkClone(HillClimbing.class.getCanonicalName());
		return new HillClimbing(prob, l.clone());
	}

	@Override
	public SearchState findFirstGoal() {
		assert (false);
		System.err.println("Hill Climbing doesn't support find first goal");
		System.exit(1);
		return null;
	}

	@Override
	public void reset() {
		closed.clear();
		currentLocation = null;
	}

	private void setIncumbent() {
		while (getIncumbent() == null) {
			if(currentLocation.getState().isGoal()){
				setIncumbent(new Solution(currentLocation, currentLocation.getG(), l.getDuration(),
						currentLocation.pathLength(), l.getExpansions(), l.getGenerations(),
						l.getDuplicates()));
				break;
			}
			ArrayList<? extends SearchNode> children = currentLocation.expand();
			l.incrExp();
			l.incrGen(children.size());
			//if there aren't any children, quit
			if(children.isEmpty())
				break;
			SearchNode bestChild = null;
			for(int i = 0; i < children.size(); i++){
				boolean duplicate = closed.contains(children.get(i).getState());
				if(duplicate)
					l.incrDup();
				if(	!duplicate &&
					(bestChild == null || children.get(i).getH() < bestChild.getH()))
					bestChild = children.get(i);
			}
			//couldn't find any valid successor, so quit.
			if(bestChild == null)
				break;
			//move to the best child if it is better
			if(bestChild.getH() < bestH){
				currentLocation = bestChild;
				closed.add(bestChild.getState());
				this.bestH = currentLocation.getH();
			}
			//best child isn't better, so just do something random
			else{
				badChildren(children);
			}
		}
	}
	
	protected void badChildren(ArrayList<? extends SearchNode> children){
		int nextChild = r.nextInt(children.size());
		currentLocation = children.get(nextChild);
		closed.add(currentLocation.getState());
	}

	@Override
	public ArrayList<SearchState> solve() {

		l.startClock();
		currentLocation = SearchNode.makeInitial(initial);
		this.bestH = currentLocation.getH();
		closed.add(currentLocation.getState());

		try {
			setIncumbent();
		} catch (OutOfMemoryError e) {
			l.endClock();
			l.setOutOfMemory();
			closed.clear();
		}
		if (getIncumbent() == null)
			return null;
		else {
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			return finalPath;
		}
	}

	public void cleanup(){
		return;
	}

}
