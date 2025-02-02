package org.cwilt.search.domains.multiagent.solvers.whcastar;
import java.util.ArrayList;

import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
import org.cwilt.search.search.NoCanonicalGoal;import org.cwilt.search.search.SearchState;
@SuppressWarnings("unused")
public class WHCAState extends SearchState{
	private final int currentGoalID;
	private final WHCAProblem problem;
	private final MultiagentVertex v;
	private final int time;
	
	public WHCAState(WHCAProblem problem){
		this.v = problem.agent.getState().getEndVertex();
		this.currentGoalID = problem.agent.getCurrentTaskID();
		this.problem = problem;
		this.time = problem.agent.getCurrentTime();
	}

	@Override
	public ArrayList<Child> expand() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Child> reverseExpand() {
		throw new NoCanonicalGoal();
	}

	@Override
	public double h() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int d() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isGoal() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean equals(Object other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int lexOrder(SearchState s) {
		// TODO Auto-generated method stub
		return 0;
	}
}
