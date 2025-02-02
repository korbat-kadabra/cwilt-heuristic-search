package org.cwilt.search.domains.multiagent.solvers.queue;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

import org.cwilt.search.domains.multiagent.problem.Agent;
import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
import org.cwilt.search.domains.multiagent.problem.ReservationTable;
import org.cwilt.search.search.SearchState;
public class SearchToQueue implements org.cwilt.search.search.SearchProblem {
	public final boolean waitAtStart;

	public final HashSet<MultiagentVertex> waitLocations;
	public final boolean filterEarlyWaits;

	public final int estimatedCost;

	public SearchToQueue(MultiagentVertex start, MultiagentVertex goal,
			int timeAtGoal, Agent a, ReservationTable r, boolean waitAtStart,
			boolean filterEarlyWaits) {
		// assert(timeAtGoal <= 0);
		this.timeAtGoal = timeAtGoal;
		this.filterEarlyWaits = filterEarlyWaits;
		this.waitAtStart = waitAtStart;
		this.start = start;
		this.target = goal.getQueue();
		assert (target == null || target instanceof EntryQueue);
		this.goal = goal;
		this.agent = a;
		this.res = r;
		this.waitLocations = new HashSet<MultiagentVertex>();
		this.estimatedCost = (int) start.distanceTo(goal);
	}

	public MultiagentVertex bestWaitLocation() {
		if (waitLocations.isEmpty())
			return null;

		MultiagentVertex v = waitLocations.iterator().next();
		for (MultiagentVertex current : waitLocations) {
			if (current.nearestPopularVertex() > v.nearestPopularVertex())
				v = current;
		}
		return v;
	}

	public final Agent agent;
	public final int timeAtGoal;
	public final MultiagentVertex start;
	public final MultiagentVertex goal;
	public final AgentQueue target;
	public final ReservationTable res;

	@Override
	public SearchState getInitial() {
		return new QueueSearchNode(start, agent.getCurrentTime(), this, null);
	}

	@Override
	public SearchState getGoal() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ArrayList<SearchState> getGoals() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCalculateD() {
	}

	@Override
	public void printProblemData(PrintStream ps) {
	}
}
