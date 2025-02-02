package org.cwilt.search.domains.multiagent.solvers.queue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.PriorityQueue;

import org.cwilt.search.domains.multiagent.problem.Agent;
import org.cwilt.search.domains.multiagent.problem.MultiagentProblem;
import org.cwilt.search.domains.multiagent.problem.MultiagentTask;
import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
import org.cwilt.search.domains.multiagent.solvers.MultiagentSolver;
public class QueueSolver extends MultiagentSolver {
	private final HashSet<Disperser> dispersers = new HashSet<Disperser>();
	
	public QueueSolver(MultiagentProblem p) {
		super(p);
		this.agents = new PriorityQueue<Agent>(10,
				new MultiagentSolver.AgentComparator());
		for (Agent a : problem.getAgents()) {
			agents.add(a);
		}

		for (MultiagentVertex v : p.getGraph().getAllVertexes()) {
			if (v.getQueue() != null) {
				if (v.getQueue() instanceof EntryQueue){
					EntryQueue e = (EntryQueue) v.getQueue();
					entryQueues.add(e);
				}
				else if (v.getQueue() instanceof ExitQueue)
					exitQueues.add((ExitQueue) v.getQueue());
				else if (v.getQueue() instanceof Disperser)
					dispersers.add((Disperser) v.getQueue());
				v.getQueue().preparePaths();
				v.getQueue().setReservationTable(
						super.problem.getReservationTable());
			}
		}
	}

	private final PriorityQueue<Agent> agents;
	private final HashSet<EntryQueue> entryQueues = new HashSet<EntryQueue>();
	private final HashSet<ExitQueue> exitQueues = new HashSet<ExitQueue>();
	private static final int TIMEOUT = 5000;
	private int currentTime = 0;


	public void solve() throws QueueOverflow {
		// give each agent exclusive access to its current location.

		for (Agent a : agents) {
			super.problem.getReservationTable().claimIndefinitely(
					a.getState().getEndVertex(), 1, a);
		}

		while (true) {
//			System.err.println("current time is " + currentTime);
			if (currentTime > TIMEOUT) {
				break;
//				throw new RuntimeException("Passed " + TIMEOUT + " time steps");
			}
			// pop all the robots whose time has come to move and plan them, or
			// issue a wait instruction.

			assert(agents.isEmpty() || agents.peek().getCurrentTime() >= currentTime);
			while (!agents.isEmpty()
					&& agents.peek().getCurrentTime() == currentTime) {


				Agent nextAgent = agents.poll();

				MultiagentTask nextTask = nextAgent.getNextTask();
				MultiagentVertex goalVertex = nextTask.destination;
				int goalTime = nextTask.timeAtDestination;

				//System.err.println(nextAgent+ " at " + currentTime + " " +
				//nextAgent.getState().getEndVertex()+ " going to " + goalVertex);
				
				
				SearchToQueue s = new SearchToQueue(nextAgent.getState()
						.getEndVertex(), goalVertex, goalTime, nextAgent,
						problem.getReservationTable(), true, false);
				int searchTime = s.estimatedCost * 30;


				
//				if (nextAgent.getID() == 166)
//					searchTime = Integer.MAX_VALUE;

				org.cwilt.search.algs.basic.bestfirst.AStarCustom a = new org.cwilt.search.algs.basic.bestfirst.AStarCustom(s,
						new org.cwilt.search.search.Limit(searchTime, Long.MAX_VALUE,
								Long.MAX_VALUE, false), new org.cwilt.search.search.SearchNode.FLComparator());
				ArrayList<org.cwilt.search.search.SearchState> path = a.solve();

				if (path == null) {
					// try again next time?
					// this currently assumes the agent is able to do this, if
					// it isn't try jiggling around instead?

					int waitToDo = ((int) a.getMinimumH()) + 10;
					boolean canWait = nextAgent.canAddWaitMoves(waitToDo);
					if (!canWait)
						assert (false);
					// boolean wandered = nextAgent.wander();
					// assert(wandered);
					System.err.println(nextAgent);
					System.err.println(nextAgent.getState());
					System.err.println("Looping at time " + currentTime);
					System.err.println("Minimum h was : " + a.getMinimumH());
					System.err.println("Expanded " + a.getLimit().getExpansions() + " nodes");
					System.err.println();
					nextAgent.addWaitMoves(waitToDo);
					agents.add(nextAgent);
				} else {
					problem.getReservationTable().releaseIndefiniteClaim(
							nextAgent.getState().getEndVertex());

					for (int i = 1; i < path.size(); i++) {
						MultiagentVertex original = ((QueueSearchNode) path
								.get(i - 1)).key.v;
						MultiagentVertex next = ((QueueSearchNode) path.get(i)).key.v;
						nextAgent.doMove(original, next);
					}
					assert (goalVertex != null);
					if (goalVertex.getQueue() == null) {
						// this agent didn't have to go through any queues to
						// get to its final destination, so it is done.
						continue;
					}
					goalVertex.getQueue().acceptAgent(nextAgent, goalTime);
				}

			}
			
			// advance time on all of the queues
			boolean allEmpty = true;
			for (AgentQueue q : entryQueues) {
//				System.err.println("advancing " + q);
				q.advanceAgents(this);
				q.checkAgents();
			}

			for (AgentQueue q : exitQueues) {
//				System.err.println("advancing " + q);
				q.advanceAgents(this);
				//q.checkAgents();
			}
			for (AgentQueue q : dispersers) {
//				System.err.println("advancing " + q);
				q.advanceAgents(this);
				q.checkAgents();
			}

			for (AgentQueue q : entryQueues) {
				if (!q.isDone()) {
					//System.err.println("entry");
					allEmpty = false;
				}
			}

			for (AgentQueue q : exitQueues) {
				if (!q.isDone()) {
					//System.err.println("exit");
					allEmpty = false;
				}
			}

			for (AgentQueue q : dispersers) {
				if (!q.isDone()) {
					allEmpty = false;
					//System.err.println("disperser");
				}
			}
			//System.err.println("Time " + currentTime + " allEmpty: " + allEmpty + " agents " + agents.isEmpty());
			if (allEmpty && agents.isEmpty())
				break;
			else if(agents.peek() != null){
				
//				System.err.println(agents.peek());
//				System.err.println(agents.peek().getCurrentTime());
			}
			currentTime++;
		}

	}

	public void acceptAgent(Agent a) {
		assert (a.getNextTask() != null);
		agents.add(a);
	}
}
