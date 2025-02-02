package org.cwilt.search.domains.multiagent.solvers;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;

import org.cwilt.search.algs.basic.bestfirst.AStar;
import org.cwilt.search.domains.multiagent.problem.Agent;
import org.cwilt.search.domains.multiagent.problem.AgentState;
import org.cwilt.search.domains.multiagent.problem.MultiagentProblem;
import org.cwilt.search.domains.multiagent.problem.MultiagentTask;
import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;
public class GlobalGhostSolver extends MultiagentSolver {
	private final PriorityQueue<Agent> agentQueue;
	private final HashSet<MultiagentVertex> usedCells;
	public GlobalGhostSolver(MultiagentProblem kp) {
		super(kp);
		kp.setGhost();
		this.usedCells = new HashSet<MultiagentVertex>();
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
		
		assert (task != null);
		while (task != null && task.destination.isPopular()) {
			SearchProblem np = agent.getSimpleProblem(task.destination);
			AStar a = new AStar(np, getLimit());
			ArrayList<SearchState> path = a.solve();
			if(path == null)
				a.printSearchData(System.err);
			assert (path != null);
			for(int i = 1; i < path.size(); i++){
				AgentState s = (AgentState) path.get(i);

				
				MultiagentVertex end = s.getEndVertex();
				usedCells.add(end);
				end.increaseUsage();
				agent.doGlobalGhostMove(s.getStartVertex(), s.getEndVertex());
			}
			if(agent.canDoTask()){
				agent.doTaskGlobalGhost();
			} else {
				assert(false);
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
			assert(false);
			return false;
		}
		for (int i = 1; i < returnPath.size(); i++) {
			AgentState nextMove = (AgentState) returnPath.get(i);
			MultiagentVertex end = nextMove.getEndVertex();
			end.increaseUsage();
			usedCells.add(end);
			
			agent.doGlobalGhostMove(nextMove.getStartVertex(), nextMove.getEndVertex());
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
		Iterator<MultiagentVertex> iter = usedCells.iterator();
		float maxValue = 0;
		while(iter.hasNext()){
			float next = (float) iter.next().getUsage();
			if(next > maxValue)
				maxValue = next;
		}

//		for(MultiagentVertex v : super.problem.getGraph().getAllVertexes()){
//			if(v.isTraversible())
//				v.setColor(Color.white);
//		}
		System.err.println("Most used cell is used " + maxValue + " times");
		iter = usedCells.iterator();
		
//		while(iter.hasNext()){
//			MultiagentVertex next = iter.next();
//			float nextUsage = (float) next.getUsage();
//			float pUsed = nextUsage / maxValue;
//			next.setColor(new Color(1.0f, 0.0f, 0.0f, pUsed));
//			if(next.isLocalUsageMaximum())
//				next.setColor(Color.GREEN);
//		}
		
	}
}
