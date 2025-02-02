package org.cwilt.search.algs.experimental;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchNode;
import org.cwilt.search.search.SearchState;
import org.cwilt.search.search.Solution;
public abstract class RandomProbe extends SearchAlgorithm {
	protected final int probeLimit;
	private SearchNode currentLocation;
	protected final Random r;

	public RandomProbe(org.cwilt.search.search.SearchProblem initial, Limit l, int probeLimit) {
		super(initial, l);
		this.currentLocation = null;
		this.probeLimit = probeLimit;
		this.r = new Random();
	}

	@Override
	protected void cleanup() {
		currentLocation = null;
		return;
	}

	@Override
	public void reset() {
		currentLocation = null;
	}
	@Override
	public SearchState findFirstGoal() {
		assert (false);
		System.err.println("cannot find first goal");
		System.exit(1);
		return null;
	}
	
	@Override
	public ArrayList<SearchState> solve() {
		assert (currentLocation == null);
		currentLocation = SearchNode.makeInitial(initial);
		l.startClock();
		if (currentLocation.getState().isGoal())
			setIncumbent(currentLocation);
		for (int i = 0; i < 10000 && l.keepGoing() && getIncumbent() == null; i++) {
			ArrayList<? extends SearchNode> children = currentLocation.expand();
			for (SearchNode c : children) {
				if (c.getState().isGoal()) {
					setIncumbent(c);
				}
			}
			l.incrExp();
			l.incrGen(children.size());
			currentLocation = children.get(r.nextInt(children.size()));
		}
		if (getIncumbent() == null) {
			Limit childLimit = l.childLimit(probeLimit);
			SearchAlgorithm a = 
				getSolver(new GenericProblem(currentLocation.getState()), childLimit);
			List<SearchState> sol = a.solve();
			if (sol != null) {
				l.endClock();
				Solution s = a.getIncumbent();
				double toHereCost = currentLocation.getG();
				int toHerePathLength = currentLocation.pathLength();
				SearchNode n = a.getIncumbent().getGoal();
				while (n.getParent() != null) {
					n = n.getParent();
				}
				n.setParent(currentLocation);
				setIncumbent (new Solution(a.getIncumbent().getGoal(), s
						.getCost()
						+ toHereCost, l.getDuration(), sol.size()
						+ toHerePathLength, l.getExpansions(), l
						.getGenerations(), l.getDuplicates()));
			}
			l.addTo(a.getLimit());
		}
		l.endClock();
		if (getIncumbent() == null)
			return null;
		else
			return getIncumbent().reconstructPath();
	}

	protected class GenericProblem implements org.cwilt.search.search.SearchProblem{
		
		public void printProblemData(PrintStream p){
			
		}

		public GenericProblem(SearchState initial){
			this.initial = initial;
		}
		
		public final SearchState initial;

		@Override
		public SearchState getGoal() {
			throw new org.cwilt.search.utils.basic.NotImplementedException();
		}

		@Override
		public SearchState getInitial() {
			return initial;
		}

		@Override
		public ArrayList<SearchState> getGoals() {
			throw new org.cwilt.search.utils.basic.NotImplementedException();
		}

		@Override
		public void setCalculateD() {
		}
		
	}
	
	protected abstract SearchAlgorithm getSolver(org.cwilt.search.search.SearchProblem s, Limit l);
}
