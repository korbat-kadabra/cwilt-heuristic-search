package org.cwilt.search.domains.multiagent.solvers;
import java.util.ArrayList;
import java.util.PriorityQueue;

import org.cwilt.search.algs.basic.bestfirst.AStar;
import org.cwilt.search.domains.multiagent.problem.Agent;
import org.cwilt.search.domains.multiagent.problem.AgentState;
import org.cwilt.search.domains.multiagent.problem.MultiagentProblem;
import org.cwilt.search.domains.multiagent.problem.MultiagentTask;
import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;
public class ReservationSolver extends MultiagentSolver {
	private final PriorityQueue<Agent> agentQueue;

	public ReservationSolver(MultiagentProblem kp) {
		super(kp);
		this.agentQueue = new PriorityQueue<Agent>(problem.getAgents().size() + 1,
				new MultiagentSolver.AgentComparator());
		for (Agent d : problem.getAgents()) {
			assert(d != null);
			agentQueue.add(d);
		}
	}


	private static final Limit getLimit(){
		return new Limit();
//		return new Limit(1000, 100000, 100000, false);
	}
	/**
	 * 
	 * @param agent Next agent to do tasks for
	 * @return if a plan was successfully found
	 */
	private boolean doTask(Agent agent) {
		assert (agent != null);
		
		MultiagentTask task = agent.getNextTask();
		MultiagentTask firstTask = task;
		AgentState first = agent.getState();
		
		assert (task != null);
		while (task != null && task.destination.isPopular()) {
			SearchProblem np = agent.getSimpleProblem(task.destination);
			AStar a = new AStar(np, getLimit());
			ArrayList<SearchState> path = a.solve();
			if(path == null){
				a.printSearchData(System.err);
			}
			assert (path != null);
			for(int i = 1; i < path.size(); i++){
				agent.addToPath((AgentState) path.get(i));
			}
			if(agent.canDoTask()){
				agent.doTask(false);
			} else {
				agent.backoffUntil(first);
				agent.failedTask(task);
				return false;
			}
			task = agent.getNextTask();
		}
		
		//go sit on the unpopular destination
		assert(task != null);
		assert(task.destination != null);
		assert(agent != null);
		SearchProblem np = agent.getSimpleProblem(task.destination);
		AStar a = new AStar(np, getLimit());
		ArrayList<SearchState> returnPath = a.solve();
		if (returnPath == null) {
			agent.backoffUntil(first);
			agent.failedTask(firstTask);
			return false;
		}
		for (int i = 1; i < returnPath.size(); i++) {
			AgentState nextMove = (AgentState) returnPath.get(i);
			agent.addToPath(nextMove);
		}
		assert(agent.canDoTask());
		agent.doTask(false);
		
		return true;

	}

	@Override
	public void solve() {
		while (!agentQueue.isEmpty()) {
			Agent agent = agentQueue.poll();
			boolean finished = doTask(agent);
			if (finished) {
				//if the agent is not done, add it back to the queue.
				if(agent.getNextTask() != null)
					agentQueue.add(agent);
			} else {
				agent.addWaitMoves(10);
				agentQueue.add(agent);
			}
		}
	}
}
