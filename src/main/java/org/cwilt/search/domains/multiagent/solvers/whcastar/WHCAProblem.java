package org.cwilt.search.domains.multiagent.solvers.whcastar;
import java.io.PrintStream;
import java.util.ArrayList;

import org.cwilt.search.domains.multiagent.problem.Agent;
import org.cwilt.search.search.NoCanonicalGoal;import org.cwilt.search.search.SearchState;
public class WHCAProblem implements org.cwilt.search.search.SearchProblem{
	
	@SuppressWarnings("unused")
	private final int window;
	final Agent agent;
	
	public WHCAProblem(int window, Agent agent){
		this.window = window;
		this.agent = agent;
	}
	
	@Override
	public SearchState getInitial() {
		return new WHCAState(this);
	}

	@Override
	public SearchState getGoal() {
		throw new NoCanonicalGoal();
	}

	@Override
	public ArrayList<SearchState> getGoals() {
		throw new NoCanonicalGoal();
	}

	@Override
	public void setCalculateD() {
	}

	@Override
	public void printProblemData(PrintStream ps) {
	}

}
