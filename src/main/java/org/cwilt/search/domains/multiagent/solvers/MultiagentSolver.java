package org.cwilt.search.domains.multiagent.solvers;
import java.util.Comparator;

import org.cwilt.search.domains.multiagent.problem.Agent;
import org.cwilt.search.domains.multiagent.problem.MultiagentProblem;
import org.cwilt.search.domains.multiagent.solvers.queue.QueueOverflow;
public abstract class MultiagentSolver {
	
	public static class AgentComparator implements Comparator<Agent> {
		@Override
		public int compare(Agent arg0, Agent arg1) {
			int d0Time = arg0.getCurrentTime();
			int d1Time = arg1.getCurrentTime();
			if (d0Time < d1Time)
				return -1;
			else if (d0Time > d1Time)
				return 1;
			return 0;
		}
	}


	
	protected final MultiagentProblem problem;

	public MultiagentSolver(MultiagentProblem p){
		this.problem = p;
	}
	
	/**
	 * Solves the problem routing all of the agents
	 * @throws QueueOverflow 
	 */
	public abstract void solve() throws QueueOverflow;
}
