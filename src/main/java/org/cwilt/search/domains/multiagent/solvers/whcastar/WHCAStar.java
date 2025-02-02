package org.cwilt.search.domains.multiagent.solvers.whcastar;
import java.util.ArrayList;

import org.cwilt.search.domains.multiagent.problem.Agent;
import org.cwilt.search.domains.multiagent.problem.MultiagentProblem;
import org.cwilt.search.domains.multiagent.problem.ReservationTable;
@SuppressWarnings("unused")
public class WHCAStar extends org.cwilt.search.domains.multiagent.solvers.MultiagentSolver {

	private final int window;
	private int currentTime = 0;

	public WHCAStar(MultiagentProblem p, int window) {
		super(p);
		this.window = window;
		this.agents = new ArrayList<Agent>();
		for (Agent a : p.getAgents()) {
			agents.add(a);
		}
	}

	private final ArrayList<Agent> agents;
	private void planAgent(Agent a){
		assert(false);
	}
	
	@Override
	public void solve() {

		boolean allSolved = false;
		while (!allSolved) {
			// agents at their goals get first priority
			for(Agent a : agents){
				if(a.getCurrentTime() != this.currentTime)
					planAgent(a);
			}

			// next plan the other agents in a randomized order.

		}
		// TODO Auto-generated method stub

	}

}
