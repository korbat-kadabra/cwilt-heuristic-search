package org.cwilt.search.algs.basic.sfbs;

import java.io.PrintStream;
import java.util.ArrayList;

import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchProblem;
import org.cwilt.search.search.SearchState;

public class SFBSJ1 extends SFBS {

	public SFBSJ1(SearchProblem prob, Limit l, boolean lite) {
		super(prob, l, lite);
	}

	@Override
	protected SearchProblem getProblem() {
		return new SFBSProblemJ(start, goals);
	}

	protected class SFBSStateJ extends SFBSState {

		public SFBSStateJ(SearchState start, SearchState goal, double fg,
				double bg) {
			super(start, goal, fg, bg);
		}

		@Override
		public ArrayList<Child> expand() {
			ArrayList<Child> children = new ArrayList<Child>();
			assert (!start.equals(goal));

			ArrayList<Child> forwards = SFBSJ1.this.expand(fGValue, start);
			if (forwards == null)
				return children;

			double forwardsAverage = 0;
			for (Child c : forwards) {
				forwardsAverage += c.child.distTo(goal);
			}
			forwardsAverage = forwardsAverage / forwards.size();

			ArrayList<Child> backwards = SFBSJ1.this.reverseExpand(bGValue,
					goal);
			if (backwards == null)
				return children;
			double backwardsAverage = 0;
			for (Child c : backwards) {
				backwardsAverage += start.distTo(c.child);
			}
			backwardsAverage = backwardsAverage / backwards.size();

			if (backwardsAverage > forwardsAverage) {
				// go backwards
				for (Child c : backwards) {
					children.add(new Child(new SFBSStateJ(start, c.child,
							fGValue, bGValue + c.transitionCost),
							c.transitionCost));
				}
			} else {
				// go forwards
				for (Child c : forwards) {
					children.add(new Child(new SFBSStateJ(c.child, goal,
							fGValue + c.transitionCost, bGValue),
							c.transitionCost));
				}
			}

			return children;
		}

	}

	private class SFBSProblemJ
			implements
			org.cwilt.search.algs.basic.bestfirst.MultistartAStar.MultistartSearchProblem {

		public void printProblemData(PrintStream p) {

		}

		private final SearchState start;
		private final ArrayList<SearchState> goals;

		public SFBSProblemJ(SearchState start, ArrayList<SearchState> goals) {
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

			for (SearchState g : goals)
				startStates.add(new SFBSStateJ(this.start, g, 0, 0));

			return startStates;

		}

	}

	@Override
	public SearchAlgorithm clone() {
		return new SFBSJ1(prob, l.clone(), lite);
	}

}
