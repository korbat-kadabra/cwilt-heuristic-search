package org.cwilt.search.domains.multiagent.solvers.queue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.PriorityQueue;

import org.cwilt.search.domains.multiagent.problem.Agent;
import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
import org.cwilt.search.domains.multiagent.problem.ReservationTable;
public abstract class AgentQueue {
	/**
	 * Tracks id's for all queues, ensuring that each queue instance gets a
	 * unique identifier
	 */
	private static int counter = 0;

	/**
	 * The id for this queue
	 */
	protected final int id;

	public abstract Gateway getGateway();

	public class Gateway {
		public final EntryQueue eq1;
		public final EntryQueue eq2;

		public Gateway(EntryQueue eq1, EntryQueue eq2) {
			this.eq1 = eq1;
			this.eq2 = eq2;
		}
	}

	public MultiagentVertex getTarget() {
		return this.target;
	}

	/**
	 * 
	 * @return The last vertex before the target, if there is one, otherwise
	 *         returns null because the target is not unique.
	 */
	public MultiagentVertex getFinalQueue() {
		MultiagentVertex finalQueue = null;
		for (MultiagentVertex v : queue) {
			MultiagentVertex next = routingTable.get(v);
			if (next == target && finalQueue != null) {
				System.err
						.println("It looks like this queue's near cell is not unique");
				return null;
			}
			if (next == target && finalQueue == null) {
				finalQueue = v;
			}
		}
		return finalQueue;
	}

	/**
	 * Vertex that all agents are trying to get to
	 */
	final MultiagentVertex target;
	/**
	 * Cells that this queue governs
	 */
	final ArrayList<MultiagentVertex> queue;
	/**
	 * Cells where agents may enter this queue
	 */
	public final ArrayList<MultiagentVertex> entry;
	/**
	 * Details how to route agents through this queue
	 */
	public final HashMap<MultiagentVertex, MultiagentVertex> routingTable;

	/**
	 * Adds a route from start to end in this queue
	 * 
	 * @param start
	 *            Start vertex
	 * @param end
	 *            End vertex to go to if at start
	 */
	public void addRoute(MultiagentVertex start, MultiagentVertex end) {
		assert (routingTable.get(start) == null);
		assert (!start.equals(end));
		assert (!start.equals(target));
		assert (queue.contains(start));
		assert (queue.contains(end));
		assert (start.getNeighbors().contains(end));

		routingTable.put(start, end);
	}

	protected boolean pathsPrepared = false;

	public abstract void preparePaths();

	public AgentQueue(MultiagentVertex t) {
		this.id = counter++;
		this.target = t;
		assert (t.getQueue() == null);
		t.setQueue(this);
		this.queue = new ArrayList<MultiagentVertex>(16);
		this.entry = new ArrayList<MultiagentVertex>(4);
		this.routingTable = new HashMap<MultiagentVertex, MultiagentVertex>();
		this.futureAgents = new PriorityQueue<ArrivingAgent>();
		this.currentAgents = new LinkedList<QueuedAgent>();
		queue.add(target);
	}

	public String toString() {
		StringBuffer b = new StringBuffer();

		b.append("Queue ID ");
		b.append(id);
		b.append(" at ");
		b.append(this.currentTime);
		b.append("\nManaging ");
		b.append(target.toString());
		b.append("\nEntrances:\n");
		b.append(entry.toString());
		b.append("\nRouting Table:\n");
		java.util.Iterator<java.util.Map.Entry<MultiagentVertex, MultiagentVertex>> iter = routingTable
				.entrySet().iterator();

		while (iter.hasNext()) {
			java.util.Map.Entry<MultiagentVertex, MultiagentVertex> next = iter
					.next();
			b.append("Vertex ");
			b.append(next.getKey());
			b.append(" routes to ");
			b.append(next.getValue());
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

	protected int currentTime;


	public void acceptAgent(Agent a, int goalTime) {
		assert (canAcceptAgent(a, a.getState().getEndVertex(),
				a.getCurrentTime(), goalTime));
		assert (a.getCurrentTime() > currentTime);
		futureAgents.add(new ArrivingAgent(a, a.getCurrentTime(), goalTime));
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
		boolean result = false;
		result = simulateForwards(arrivals, current, routingTable, target,
				currentTime);

		return result;
	}

	public void checkAgents() {
		for (QueuedAgent q : currentAgents) {
			if (q.a.getCurrentTime() != this.currentTime) {
				System.err.println("agent time " + q.a.getCurrentTime());
				System.err.println("This current time " + currentTime);
				assert (false);
			}

		}
	}

	protected final PriorityQueue<ArrivingAgent> futureAgents;
	protected final LinkedList<QueuedAgent> currentAgents;


	protected boolean earlyExit(Agent a, QueueSolver qs) {
		return false;
	}

	public void advanceAgents(QueueSolver s) throws QueueOverflow {
		currentTime++;
		HashMap<MultiagentVertex, Agent> current = new HashMap<MultiagentVertex, Agent>();
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
				// see whcih drive has been in the queue longer and then use
				// that to determine whchi drive gets the reservation.

				assert (false);
			} else {
				current.put(desired, q.a);
			}
		}

		Collections.sort(currentAgents);
		ListIterator<QueuedAgent> iter = currentAgents.listIterator();
		// go through and give everyone their next reservation
		while (iter.hasNext()) {
			QueuedAgent q = iter.next();
			q.queueTime++;
			// if this is the first time step in the queue, can't do anything
			// yet.
			if (q.queueTime == 1)
				continue;
			MultiagentVertex desired = routingTable.get(q.v);
			Agent currentOwner = current.get(desired);
			Agent nextOwner = current.get(desired);

			// if this is the end vertex, wait and decrement the counter,
			// otherwise exit.
			if (q.v.equals(target)) {
				q.goalTime--;
				if (q.goalTime < 0) {

					boolean accepted = pushAgentThrough(q.a, s);
					// assert (accepted || this instanceof EntryQueue);
					if (accepted) {
						iter.remove();
					}

				} else {
					current.put(q.v, q.a);
					q.a.doMove(q.v, q.v);
				}
				continue;
			}

			if (q.goalTime == 0 && !q.v.equals(target)) {

				boolean accepted = earlyExit(q.a, s);
				if (accepted) {
					iter.remove();
					continue;
				}
			}

			assert (desired != null);
			// if someone else owns the vertex, have to wait.
			if (currentOwner != null || nextOwner != null) {
				// if someone owns either vertex, have to wait.
				current.put(q.v, q.a);
				q.a.doMove(q.v, q.v);
			} else {
				current.put(desired, q.a);
				q.a.doMove(q.v, desired);
				if (desired != null)
					q.v = desired;
			}
		}

//		if (this.id == 38) {
//			System.err.println("Queue 38 at " + currentTime);
//			System.err.println(current);
//		}

		// copying over, to check if everything is still okay.
		PriorityQueue<ArrivingAgent> arrivals = new PriorityQueue<ArrivingAgent>();
		for (ArrivingAgent a : futureAgents) {
			arrivals.add(a);
		}
		LinkedList<QueuedAgent> currentAgents = new LinkedList<QueuedAgent>();
		for (QueuedAgent q : currentAgents) {
			currentAgents.addLast((QueuedAgent) q.clone());
		}
		// boolean result = false;
		// result =
		simulateForwards(arrivals, currentAgents, routingTable, target,
				currentTime);
		// assert (result);

	}

	public Double distanceToQueue(MultiagentVertex v) {
		return null;
	}

	/**
	 * Simulates action of this queue forwards with these agents due to arrive
	 * and currently in the queue
	 * 
	 * @param arrivals
	 *            Timetable of agent arrivals
	 * @param inQueue
	 *            Current state of the queue
	 * @return true if this simulation step nothing bad happened, false
	 *         otherwise
	 * @throws QueueOverflow
	 */
	private static boolean simulateOneStep(
			PriorityQueue<ArrivingAgent> arrivals,
			LinkedList<QueuedAgent> inQueue, int time,
			HashMap<MultiagentVertex, MultiagentVertex> routingTable,
			MultiagentVertex target) {

		HashMap<MultiagentVertex, QueuedAgent> current = new HashMap<MultiagentVertex, QueuedAgent>();
		// HashMap<MultiagentVertex, Agent> next = new HashMap<MultiagentVertex,
		// Agent>();

		assert (arrivals.isEmpty() || arrivals.peek().arrivalTime >= time);
		while (!arrivals.isEmpty() && arrivals.peek().arrivalTime == time) {
			assert (arrivals.peek().arrivalTime >= time);

			ArrivingAgent newArrival = arrivals.poll();
			inQueue.addLast(new QueuedAgent(newArrival.arrivalVertex,
					newArrival.agent, newArrival.goalTime));
		}
		// not perfect, but iterate through the arrivals looking for the ones
		// whose time step is next.
		if (!arrivals.isEmpty() && arrivals.peek().arrivalTime == time + 1) {
			for (ArrivingAgent a : arrivals) {
				if (a.arrivalTime == time + 1) {
					QueuedAgent temp = new QueuedAgent(a.arrivalVertex, a.agent, a.goalTime);
					current.put(a.arrivalVertex, temp);
				}
			}
		}

		// go through and give everyone their current reservations
		for (QueuedAgent q : inQueue) {
			MultiagentVertex desired = q.v;
			QueuedAgent currentOwner = current.get(q.v);
			if (currentOwner != null)
				return false;
			else
				current.put(desired, q);
		}
		Collections.sort(inQueue);

		ListIterator<QueuedAgent> iter = inQueue.listIterator();
		// go through and give everyone their next reservation
		while (iter.hasNext()) {
			QueuedAgent q = iter.next();
			assert (q.v != null);
			q.queueTime++;

			MultiagentVertex desired = routingTable.get(q.v);

			// on the first step they can't do anything yet?
			if (q.queueTime == 1) {
				desired = q.v;
			}

			QueuedAgent currentOwner = current.get(desired);
			QueuedAgent nextOwner = current.get(desired);

			// if this is the end vertex, wait and decrement the counter,
			// otherwise exit.
			assert (q.v != null);
			if (q.v.equals(target)) {
				q.goalTime--;
				if (q.goalTime < 0) {
					// TODO if actually executing forwards, have to dump this
					// guy off to the next controller.
					iter.remove();
				} else
					current.put(q.v, q);
				continue;
			}

			// if someone else owns the vertex, have to wait.
			if (currentOwner != null || nextOwner != null) {
				// Check to see who owns the vertex, and if this drive is ahead, reject the reservation request.
				// if someone owns either vertex, have to wait.
				if(currentOwner == nextOwner && currentOwner == q)
					continue;
				if(nextOwner.queueTime <= q.queueTime)
					return false;
				if(currentOwner.queueTime <= q.queueTime)
					return false;
				
				current.put(q.v, q);
			} else {
				current.put(desired, q);
				if (desired != null)
					q.v = desired;
			}
		}

		return true;
	}

	/**
	 * Simulates the action of this queue forwards with the specifed agents in
	 * the queue and due to arrive.
	 * 
	 * @return whether or not the simulation can be done with the specified
	 *         agents and arrivals
	 * @throws QueueOverflow
	 */
	private static boolean simulateForwards(
			PriorityQueue<ArrivingAgent> arrivals,
			LinkedList<QueuedAgent> inQueue,
			HashMap<MultiagentVertex, MultiagentVertex> routingTable,
			MultiagentVertex target, int simulationTime) {

		while (!arrivals.isEmpty() || !inQueue.isEmpty()) {
			assert (simulationTime < 1000);
			boolean passed = simulateOneStep(arrivals, inQueue, simulationTime,
					routingTable, target);
			simulationTime++;
			if (!passed)
				return false;
		}

		return true;
	}

	public void addVertex(MultiagentVertex v) {
		assert (!queue.contains(v));
		assert (v.getQueue() == null);
		v.setQueue(this);
		queue.add(v);
	}

	public boolean isDone() {
		return this.futureAgents.isEmpty() && this.currentAgents.isEmpty();
	}

	protected abstract boolean pushAgentThrough(Agent a, QueueSolver s)
			throws QueueOverflow;

	protected ReservationTable reservations;

	public void setReservationTable(ReservationTable reservationTable) {
		this.reservations = reservationTable;
	}

	public boolean entranceContains(MultiagentVertex v) {
		return entry.contains(v);
	}

	public boolean queueContains(MultiagentVertex next) {
		return queue.contains(next);
	}
}
