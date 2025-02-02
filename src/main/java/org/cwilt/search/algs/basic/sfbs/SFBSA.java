package org.cwilt.search.algs.basic.sfbs;
import java.io.PrintStream;
import java.util.ArrayList;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;
public class SFBSA extends SFBS {
	
	public SFBSA(SearchProblem prob, Limit l, boolean lite) {
		super(prob, l, lite);
	}

	@Override
	public SearchAlgorithm clone() {
		return new SFBSA(prob, l.clone(), lite);
	}

	@Override
	protected SearchProblem getProblem(){
		return new SFBSProblemA(start, goals);
	}
	

	
	private class SFBSProblemA implements  org.cwilt.search.algs.basic.bestfirst.MultistartAStar.MultistartSearchProblem{

		public void printProblemData(PrintStream p){
			
		}

		private final SearchState start;
		private final ArrayList<SearchState> goals;
		public SFBSProblemA(SearchState start, ArrayList<SearchState> goals){
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
				startStates.add(new SFBSStateA(this.start, g, 0, 0, true));

			return startStates;

		}
		
	}

	protected class SFBSStateA extends SFBSState {

		private final boolean goForwards;
		
		public SFBSStateA(SearchState start, SearchState goal, double fg, double bg, boolean goForwards) {
			super(start, goal, fg, bg);
			this.goForwards = goForwards;
		}

		@Override
		public ArrayList<Child> expand() {
			ArrayList<Child> children = new ArrayList<Child>();
			assert(!start.equals(goal));
			
			if(!goForwards){
				//go backwards
				ArrayList<Child> backwards = SFBSA.this.reverseExpand(bGValue, goal);
				if(backwards == null)
					return children;
				
				for(Child c : backwards){
					children.add(new Child(new SFBSStateA(start, c.child, fGValue, bGValue + c.transitionCost, true), c.transitionCost));
				}
			} else {
				//go forwards
				ArrayList<Child> forwards = SFBSA.this.expand(fGValue, start);
				if(forwards == null)
					return children;
				for(Child c : forwards){
					children.add(new Child(new SFBSStateA(c.child, goal, fGValue + c.transitionCost, bGValue, false), c.transitionCost));
				}
			}
			
			return children;
		}
		
	}

	
}
