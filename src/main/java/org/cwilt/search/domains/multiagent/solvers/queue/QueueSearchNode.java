package org.cwilt.search.domains.multiagent.solvers.queue;
import java.util.ArrayList;
import java.util.List;

import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
import org.cwilt.search.search.SearchState;
public class QueueSearchNode extends SearchState {
	private final SearchToQueue problem;

	public static final class QueueSearchKey {

		@Override
		public String toString() {
			return "QueueSearchKey [v=" + v + ", time=" + time
					+ ", congestion=" + congestion + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;
			temp = Double.doubleToLongBits(congestion);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			result = prime * result + time;
			result = prime * result + ((v == null) ? 0 : v.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			QueueSearchKey other = (QueueSearchKey) obj;
			if (Double.doubleToLongBits(congestion) != Double
					.doubleToLongBits(other.congestion))
				return false;
			if (time != other.time)
				return false;
			if (v == null) {
				if (other.v != null)
					return false;
			} else if (!v.equals(other.v))
				return false;
			return true;
		}

		public final MultiagentVertex v;
		public final int time;
		private final double congestion;

		public QueueSearchKey(MultiagentVertex v, int time, double congestion) {
			this.v = v;
			this.time = time;
			this.congestion = congestion;
		}


	}

	public final QueueSearchKey key;

	public QueueSearchNode(MultiagentVertex key, int time,
			SearchToQueue target, QueueSearchNode parent) {
		double congestion;
		if (parent == null) {
			congestion = 0;
		} else {
			congestion = parent.key.congestion + key.getCongestion();
		}
		

		this.key = new QueueSearchKey(key, time, congestion);

		assert(congestion < 100000000000000000.0d);

		this.problem = target;
		assert (target.res.checkReservation(key, time, target.agent));
	}

	@Override
	public ArrayList<Child> expand() {
		// generate the adjacent children
		List<MultiagentVertex> adj = key.v.getNeighbors(problem.goal);
		ArrayList<Child> children = new ArrayList<Child>(adj.size() + 1);

		// if this is not the start node
		if (!this.problem.start.equals(this.key.v)) {
			// and it is a queue cell, it should not have any children.
			if (this.key.v.getQueue() != null)
				return children;
		}

		if (key.v.getQueue() == null
				&& problem.res.canClaimIndefinitely(this.key.v,
						problem.agent.getCurrentTime() - 2, problem.agent)) {
			problem.waitLocations.add(key.v);
		}

		for (MultiagentVertex v : adj) {

			if (v.isForbidden(problem.start, problem.goal)) {
				continue;
			}

			boolean atStart = v.equals(problem.start);
			if (atStart && !problem.waitAtStart)
				continue;

			if (problem.res.checkReservation(v, key.time + 1, problem.agent)
					&& problem.res.checkReservation(key.v, key.time + 1,
							problem.agent)) {
				children.add(new Child(new QueueSearchNode(v, key.time + 1,
						problem, this), 1));
			}
		}

		// check to see if can wait at the start
		boolean atStart = key.v.equals(problem.start);

		if ((!atStart || problem.waitAtStart)
				&& problem.res.checkReservation(key.v, key.time + 1,
						problem.agent) && key.v.getQueue() == null
				&& !problem.filterEarlyWaits) {

			children.add(new Child(new QueueSearchNode(key.v, key.time + 1,
					problem, this), 1));
		}
		return children;
	}

	@Override
	public ArrayList<Child> reverseExpand() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double h() {
		return key.v.distanceTo(problem.goal);
	}

	@Override
	public int d() {
		return (int) h();
	}

	@Override
	public boolean isGoal() {
		if (problem.target == null) {
			return problem.goal.equals(key.v);
		} else
			return problem.target.canAcceptAgent(problem.agent, key.v,
					key.time, problem.timeAtGoal);
	}

	@Override
	public Object getKey() {
		return key;
	}

	@Override
	public int lexOrder(SearchState s) {
		QueueSearchNode other = (QueueSearchNode) s;
		assert (this.key.congestion > 0);
		assert (other.key.congestion > 0);
		return (int) (this.key.congestion - other.key.congestion);
	}

	public String toString() {
		return key.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QueueSearchNode other = (QueueSearchNode) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

}
