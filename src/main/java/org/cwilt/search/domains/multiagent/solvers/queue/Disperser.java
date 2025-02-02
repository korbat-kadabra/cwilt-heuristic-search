package org.cwilt.search.domains.multiagent.solvers.queue;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.PriorityQueue;

import org.cwilt.search.domains.multiagent.problem.Agent;
import org.cwilt.search.domains.multiagent.problem.MultiagentTask;
import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
public class Disperser extends AgentQueue {
	public Disperser(List<MultiagentVertex> t) {
		super(t.get(0));
		ListIterator<MultiagentVertex> iter = t.listIterator();
		iter.next();
		while(iter.hasNext()){
			super.addVertex(iter.next());
		}
		this.routes = new ArrayList<ArrayList<MultiagentVertex>>();
		for(int i = 0; i < MAX_ROUTE; i++){
			this.routes.add(new ArrayList<MultiagentVertex>());
		}
	}

	private static final int MAX_ROUTE = 10;
	
	
	@Override
	public Gateway getGateway() {
		return null;
	}

	@Override
	public void preparePaths() {
		if (pathsPrepared)
			return;
		this.pathsPrepared = true;
		// this identifies the queue's entrance
		for (MultiagentVertex v : queue) {
			if (v.equals(target)) {
				continue;
			}
			for (MultiagentVertex neighbor : v.getNeighbors()) {
				// this checks to see if the cell borders open space
				if (neighbor.getQueue() == null)
					continue;
				if (
				// this is here to pick up the case when the entrance is another
				// queue's exit
				(neighbor.getQueue() != this && neighbor.getQueue().target
						.equals(neighbor))) {
					entry.add(v);
					break;
				}
			}
		}
		routes.add(new ArrayList<MultiagentVertex>());
		routes.get(0).addAll(this.entry);
		LinkedList<MultiagentVertex> open = new LinkedList<MultiagentVertex>();
		
		open.addAll(this.entry);
		while(!open.isEmpty()){
			MultiagentVertex next = open.poll();
			for(MultiagentVertex v : next.getNeighbors()){
				if(v.getQueue() != this)
					continue;
				
				int depth = getVertexLevel(v);
				if(depth != -1)
					continue;
				routes.get(getVertexLevel(next) + 1).add(v);
				
				if(!open.contains(v))
					open.add(v);
			}
		}
		
	}
	
	private int getVertexLevel(MultiagentVertex v){
		for(int i = 0; i < routes.size();i++){
			if(routes.get(i).contains(v))
				return i;
		}
		return -1;
	}
	ArrayList<ArrayList<MultiagentVertex>> routes = new ArrayList<ArrayList<MultiagentVertex>>();
	
	@Override
	protected boolean pushAgentThrough(Agent a, QueueSolver s)
			throws QueueOverflow {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	public String toString() {
		StringBuffer b = new StringBuffer();

		b.append("Queue ID ");
		b.append(id);		
		b.append(" at ");
		b.append(this.currentTime);

		b.append("\nEntrances:\n");
		b.append(entry.toString());
		b.append("\nRouting Table:\n");
		
		for(int i = 0; i < routes.size(); i++){
			ArrayList<MultiagentVertex> v = routes.get(i);
			if(v.isEmpty())
				continue;
			b.append(i);
			b.append(": ");
			for(MultiagentVertex vertex : v){
				b.append(vertex);
				b.append(" ");
			}
			b.append("\n");
			
		}
		
		b.append("\ncontaining following cells:\n");
		for (MultiagentVertex c : queue) {
			b.append("vertex ");
			b.append(c);
			b.append("\n");
		}
		return b.toString();
	}
	
	@Override
	public void advanceAgents(QueueSolver s) throws QueueOverflow {
		currentTime++;
		
		HashMap<MultiagentVertex, Agent> current = new HashMap<MultiagentVertex, Agent>();
		HashMap<MultiagentVertex, Agent> next = new HashMap<MultiagentVertex, Agent>();

		assert (futureAgents.isEmpty() || futureAgents.peek().arrivalTime >= currentTime);

		while (!futureAgents.isEmpty()
				&& futureAgents.peek().arrivalTime == currentTime) {

			ArrivingAgent newArrival = futureAgents.poll();
			currentAgents.addLast(new QueuedAgent(newArrival.agent.getState()
					.getEndVertex(), newArrival.agent, newArrival.goalTime));
		}

		// go through and give everyone their current reservations
		for (QueuedAgent q : currentAgents) {
			MultiagentVertex desired = q.v;
			Agent currentOwner = current.get(q.v);
			if (currentOwner != null) {
				assert (false);
			} else {
				current.put(desired, q.a);
			}
		}

		ListIterator<QueuedAgent> iter = currentAgents.listIterator();
		// go through and give everyone their next reservation
		while (iter.hasNext()) {
			QueuedAgent q = iter.next();
			q.queueTime++;
			// if this is the first time step in the queue, can't do anything
			// yet.
			if (q.queueTime == 1)
				continue;
			
			//consider the different places this agent can go.
			boolean moved = false;
			for(MultiagentVertex nextVertex : q.v.getNeighbors()){
				//check if vertex is outside the queue, and the agent can be ejected.
				if(nextVertex.getQueue() == null){
					boolean exited = earlyExit(q.a, s);
					if(exited){
						iter.remove();
						moved = true;
						break;
					}
				}
				//check if next is higher than or the same as current
				if(getVertexLevel(nextVertex) <= getVertexLevel(q.v))
					continue;
				if(nextVertex.getQueue() != this)
					continue;
				MultiagentVertex desired = nextVertex;
				Agent currentOwner = current.get(desired);
				Agent nextOwner = next.get(desired);
				if (currentOwner != null || nextOwner != null) {
					// if someone owns either vertex, have to wait.
					continue;
				} else {
					next.put(desired, q.a);
					q.a.doMove(q.v, desired);
					if (desired != null)
						q.v = desired;
					moved = true;
					break;
				}
			}
			//nothing else to do, so have to wait.
			if(!moved){
				next.put(q.v, q.a);
				q.a.doMove(q.v, q.v);
			}
			
		}

		
	}

	
	protected boolean earlyExit(Agent a, QueueSolver qs){

		// get the agent's next destination
		MultiagentTask t = a.getNextTask();

		// this task had better exist
		assert (t != null);
		// this task had better not be here.
		assert (t.destination != a.getState().getEndVertex());
		assert (a.getReservationTable() == super.reservations);
		SearchToQueue s = new SearchToQueue(a.getState().getEndVertex(),
				t.destination, 0, a, a.getReservationTable(),
				false, true);
		int limit = 2000;
		
		org.cwilt.search.search.SearchAlgorithm alg = new org.cwilt.search.algs.basic.bestfirst.AStar(s,
				new org.cwilt.search.search.Limit(limit, Long.MAX_VALUE, Long.MAX_VALUE, false));
		List<org.cwilt.search.search.SearchState> path = alg.solve();
		// assert(path != null);
		if (path == null) {
			// try again next time?
			// System.err.println("Finding a return path failed");
			// System.err.println("Starting at " + this.target + " to " +
			// t.destination);
			// alg.printSearchData(System.err);
			// System.exit(1);

			MultiagentVertex newTarget = s.bestWaitLocation();
			if(newTarget == null)
				return false;
			
			SearchToQueue newPlan = new SearchToQueue(a.getState()
					.getEndVertex(), newTarget, t.timeAtDestination, a,
					a.getReservationTable(), false, true);
			org.cwilt.search.search.SearchAlgorithm newAlg = new org.cwilt.search.algs.basic.bestfirst.AStar(
					newPlan, new org.cwilt.search.search.Limit(1000, Long.MAX_VALUE,
							Long.MAX_VALUE, false));
			List<org.cwilt.search.search.SearchState> newPath = newAlg.solve();

			if (newPath != null) {
				assert (newPath != null);
				for (int i = 1; i < newPath.size(); i++) {
					MultiagentVertex original = ((QueueSearchNode) newPath
							.get(i - 1)).key.v;
					MultiagentVertex next = ((QueueSearchNode) newPath.get(i)).key.v;
					a.doMove(original, next);
				}
				a.getReservationTable().claimIndefinitely(newTarget, a.getCurrentTime(), a);
				qs.acceptAgent(a);
				return true;
			} else {
				return false;
			}
		} else {
			for (int i = 1; i < path.size(); i++) {
				MultiagentVertex original = ((QueueSearchNode) path.get(i - 1)).key.v;
				MultiagentVertex next = ((QueueSearchNode) path.get(i)).key.v;
				a.doMove(original, next);
			}
			AgentQueue q = a.getState().getEndVertex().getQueue();
			if (q != null) {
				// add this agent to the queue
				q.acceptAgent(a,
						a.getTask(a.getCurrentTaskID() + 1).timeAtDestination);
			}
		}

		return true;

	}


	public boolean canAcceptAgent(Agent agent, MultiagentVertex v,
			int arrivalTime, int goalTime) {
		// reject everything that isn't even in the queue obviously
		if (!entry.contains(v))
			return false;
		assert (arrivalTime >= currentTime);

		PriorityQueue<ArrivingAgent> arrivals = new PriorityQueue<ArrivingAgent>();
		for (ArrivingAgent a : futureAgents) {
			arrivals.add(a);
		}
		arrivals.add(new ArrivingAgent(agent, v, arrivalTime, goalTime));
		LinkedList<QueuedAgent> current = new LinkedList<QueuedAgent>();
		for (QueuedAgent q : currentAgents) {
			current.addLast((QueuedAgent) q.clone());
		}
		return simulateForwards(arrivals, current, this.currentTime);
		
	}
	
	private static boolean simulateForwards(PriorityQueue<ArrivingAgent> arrivals,
			LinkedList<QueuedAgent> inQueue, int simulationTime) {
		return simulateOneStep(arrivals, inQueue, simulationTime);
	}

	private static boolean simulateOneStep(PriorityQueue<ArrivingAgent> arrivals,
			LinkedList<QueuedAgent> inQueue, int time) {
		HashMap<MultiagentVertex, Agent> current = new HashMap<MultiagentVertex, Agent>();
		while (!arrivals.isEmpty() && arrivals.peek().arrivalTime == time) {
			ArrivingAgent newArrival = arrivals.poll();
			inQueue.addLast(new QueuedAgent(newArrival.arrivalVertex,
					newArrival.agent, newArrival.goalTime));
		}

		// go through and give everyone their current reservations
		for (QueuedAgent q : inQueue) {
			MultiagentVertex desired = q.v;
			Agent currentOwner = current.get(q.v);
			if (currentOwner != null)
				return false;
			else
				current.put(desired, q.a);
		}
		return true;
	}

	
}
