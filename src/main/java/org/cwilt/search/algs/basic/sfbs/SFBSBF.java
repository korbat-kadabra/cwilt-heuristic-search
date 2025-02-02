package org.cwilt.search.algs.basic.sfbs;
import java.io.PrintStream;
import java.util.ArrayList;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;
public class SFBSBF extends SFBS {
	public SFBSBF(SearchProblem prob, Limit l, boolean lite) {
		super(prob, l, lite);
	}

	

	
	@Override
	protected SearchProblem getProblem(){
		return new SFBSProblemBF(start, goals);
	}

	
	private class SFBSProblemBF implements org.cwilt.search.algs.basic.bestfirst.MultistartAStar.MultistartSearchProblem{

		public void printProblemData(PrintStream p){
			
		}

		private final SearchState start;
		private final ArrayList<SearchState> goals;
		
		public SFBSProblemBF(SearchState start, ArrayList<SearchState> goals){
			this.start = start;
			this.goals = goals;
		}
		
		@Override
		public SearchState getInitial() {
			return start;
		}

		@Override
		public SearchState getGoal() {
			throw new UnsupportedOperationException();
		}

		@Override
		public ArrayList<SearchState> getGoals() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void setCalculateD() {
		}

		@Override
		public ArrayList<SearchState> getStarts() {
			ArrayList<SearchState> startStates = new ArrayList<SearchState>();
			
			for(SearchState g : goals)
				startStates.add(new SFBSStateBF(this.start, g, 0, 0));

			return startStates;
		}
		
	}

	protected class SFBSStateBF extends SFBSState {

		public SFBSStateBF(SearchState start, SearchState goal, double fg, double bg) {
			super(start, goal, fg, bg);
		}

		@Override
		public ArrayList<Child> expand() {
			ArrayList<Child> children = new ArrayList<Child>();
			assert(!start.equals(goal));
			
			ArrayList<Child> forwards = SFBSBF.this.expand(fGValue, start);
			ArrayList<Child> backwards = SFBSBF.this.reverseExpand(bGValue, goal);
			
			if(forwards == null || backwards == null)
				return children;
			
			if(forwards.size() > backwards.size()){
				//go backwards
				for(Child c : backwards){
					children.add(new Child(new SFBSStateBF(start, c.child, fGValue, bGValue + c.transitionCost), c.transitionCost));
				}
			} else {
				//go forwards
				for(Child c : forwards){
					children.add(new Child(new SFBSStateBF(c.child, goal, fGValue + c.transitionCost, bGValue), c.transitionCost));
				}
			}
			
			return children;
		}
		
	}

	@Override
	public SearchAlgorithm clone() {
		return new SFBSBF(prob, l.clone(), lite);
	}

}
