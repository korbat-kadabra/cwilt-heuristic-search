package org.cwilt.search.domains.multiagent.solvers.autoqueue;
import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;

import org.cwilt.search.search.Limit;
import org.cwilt.search.domains.kiva.SearchTracker;
import org.cwilt.search.domains.kiva.map.CandidateCorridor;
import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.kiva.map.HStarValue;
import org.cwilt.search.domains.multiagent.problem.Agent;
import org.cwilt.search.domains.multiagent.problem.MultiagentTask;
import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
import org.cwilt.search.domains.multiagent.problem.ReservationTable;
import org.cwilt.search.domains.multiagent.solvers.queue.ArrivingAgent;
import org.cwilt.search.domains.multiagent.solvers.queue.QueueOverflow;
import org.cwilt.search.domains.multiagent.solvers.queue.QueuedAgent;
public class AutoQueue implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7330609564025092576L;
	private final List<GridCell> targets;

	private final int queueID;

	private static int queueCount = 0;

	public boolean followsRoute(GridCell start, GridCell end) {
		List<GridCell> targets = routes.get(start);
		assert (targets != null);
		return targets.contains(end);
	}

	public List<GridCell> getTargets() {
		return targets;
	}

	private AutoGateway gateway;

	public boolean containsCell(GridCell g) {
		return routes.containsKey(g);
	}

	private final void setAutoGateway(AutoGateway g) {
		assert (this.gateway == null);
		this.gateway = g;
	}

	public AutoGateway getAutoGateway() {
		return this.gateway;
	}

	private final HashMap<GridCell, List<GridCell>> routes;

	public static final class AutoGateway implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8691731549222066426L;
		public final AutoQueue inbound, outbound;

		public AutoGateway(AutoQueue inbound, AutoQueue outbound) {
			this.inbound = inbound;
			this.outbound = outbound;
			inbound.setAutoGateway(this);
			outbound.setAutoGateway(this);
		}
	}

	public AutoQueue(List<GridCell> targets, List<GridCell> allVertexes,
			QUEUE_ORIENTATION orientation, ReservationTable rt) {
		this.queueID = AutoQueue.queueCount++;
		this.reservations = rt;
		this.targets = targets;
		assert (!targets.isEmpty());
		this.routes = new HashMap<GridCell, List<GridCell>>();
		this.orientation = orientation;
		for (GridCell v : allVertexes) {
			GridCell g = (GridCell) v;
			g.setAutoQueue(this);
			routes.put(g, new ArrayList<GridCell>(3));
		}
		this.futureAgents = new PriorityQueue<ArrivingAgent>();
		this.currentAgents = new LinkedList<QueuedAgent>();
	}

	public String toString() {
		StringBuffer b = new StringBuffer();

		b.append("Auto Queue targeting:\n");
		for (GridCell target : targets) {
			b.append(target);
			b.append(" ");
		}
		b.append("\nPrimary Exit:\n");
		b.append(this.primaryExit);
		b.append("\n");
		for (Map.Entry<GridCell, List<GridCell>> item : routes.entrySet()) {
			b.append(item.getKey());
			b.append(" -> ");
			for (GridCell target : item.getValue()) {
				b.append(target);
				b.append(" ");
			}
			b.append("\n");
		}

		return b.toString();
	}

	public static final AutoGateway buildHorizontalGateway(CandidateCorridor c,
			int size, ReservationTable rt) {
		int inbound = c.getInbound();
		int outbound = c.getOutbound();
		int totalWidth = inbound + outbound;
		assert (totalWidth == c.width);
		List<GridCell> inboundVertexes = new ArrayList<GridCell>();
		List<GridCell> outboundVertexes = new ArrayList<GridCell>();

		List<GridCell> initialInboundVertexes = new ArrayList<GridCell>();
		List<GridCell> initialOutboundVertexes = new ArrayList<GridCell>();

		int inboundMaxY = Integer.MIN_VALUE;

		for (int i = 0; i < c.getLength(); i++) {
			for (int j = 0; j < inbound; j++) {
				int index = i * c.width + j;
				c.centers.get(index).setColor(Color.magenta);
				inboundVertexes.add(c.centers.get(index));
				initialInboundVertexes.add(c.centers.get(index));
				if (c.centers.get(index).y > inboundMaxY) {
					inboundMaxY = c.centers.get(index).y;
				}
			}
			for (int j = 0; j < outbound; j++) {
				int index = i * c.width + j + inbound;
				c.centers.get(index).setColor(Color.CYAN);
				outboundVertexes.add(c.centers.get(index));
				initialOutboundVertexes.add(c.centers.get(index));
			}
		}

		LinkedList<GridCell> open = new LinkedList<GridCell>();

		open.addAll(inboundVertexes);
		open.addAll(outboundVertexes);

		int initialInbound = inboundVertexes.size();
		int initialOutbound = outboundVertexes.size();

		while ((initialInbound + 2 * size >= inboundVertexes.size() || initialOutbound
				+ 2 * size >= outboundVertexes.size())
				&& !open.isEmpty()) {
			GridCell next = open.poll();
			if (inboundVertexes.contains(next)) {
				assert (!outboundVertexes.contains(next));
				List<MultiagentVertex> neighbors = next.getNeighbors();
				for (MultiagentVertex v : neighbors) {
					GridCell neighbor = (GridCell) v;
					if (!neighbor.isTravel()) {
						continue;
					}
					if (inboundVertexes.contains(neighbor))
						continue;
					if (outboundVertexes.contains(neighbor))
						continue;
					if (initialInbound + 2 * size < inboundVertexes.size()) {
						open.add(neighbor);
						continue;
					}
					if (neighbor.y > inboundMaxY)
						continue;
					inboundVertexes.add(neighbor);
					open.add(neighbor);
				}
			} else if (outboundVertexes.contains(next)) {
				assert (!inboundVertexes.contains(next));
				List<MultiagentVertex> neighbors = next.getNeighbors();
				for (MultiagentVertex v : neighbors) {
					GridCell neighbor = (GridCell) v;
					if (!neighbor.isTravel())
						continue;
					if (inboundVertexes.contains(neighbor))
						continue;
					if (outboundVertexes.contains(neighbor))
						continue;
					if (initialOutbound + 2 * size < outboundVertexes.size()) {
						open.add(neighbor);
						continue;
					}
					if (neighbor.y <= inboundMaxY)
						continue;
					outboundVertexes.add(neighbor);
					open.add(neighbor);
				}
			}
		}
		open.clear();
		open.addAll(outboundVertexes);
		open.addAll(inboundVertexes);

		for (int i = 0; i < BORDER_SIZE; i++) {
			GridCell next = open.poll();
			List<MultiagentVertex> neighbors = next.getNeighbors();
			for (MultiagentVertex v : neighbors) {
				GridCell neighbor = (GridCell) v;
				if (!neighbor.isTravel())
					continue;
				if (inboundVertexes.contains(neighbor))
					continue;
				if (outboundVertexes.contains(neighbor))
					continue;
				neighbor.setInvalidGoal();
				// neighbor.setColor(Color.yellow);
				open.add(neighbor);
			}

		}

		for (GridCell g : inboundVertexes) {
			g.setColor(Color.magenta);
		}
		for (GridCell g : outboundVertexes) {
			g.setColor(Color.cyan);
		}
		for (GridCell g : initialInboundVertexes) {
			g.setColor(Color.red);
		}
		for (GridCell g : initialOutboundVertexes) {
			g.setColor(Color.blue);
		}

		AutoQueue inboundQueue = new AutoQueue(initialInboundVertexes,
				inboundVertexes, QUEUE_ORIENTATION.LEFT, rt);
		AutoQueue outboundQueue = new AutoQueue(initialOutboundVertexes,
				outboundVertexes, QUEUE_ORIENTATION.RIGHT, rt);

		inboundQueue.routeQueue();
		outboundQueue.routeQueue();

		if (inboundQueue.primaryExit == null
				|| outboundQueue.primaryExit == null) {
			inboundQueue.remove();
			outboundQueue.remove();
			return null;
		}
		// TODO Have to check if this queue should be deleted

		return new AutoGateway(inboundQueue, outboundQueue);
	}

	private static final int BORDER_SIZE = 90;

	public static final AutoGateway buildVerticalGateway(CandidateCorridor c,
			int size, ReservationTable rt) {
		int inbound = c.getInbound();
		int outbound = c.getOutbound();
		int totalWidth = inbound + outbound;
		assert (totalWidth == c.width);
		List<GridCell> inboundVertexes = new ArrayList<GridCell>();
		List<GridCell> outboundVertexes = new ArrayList<GridCell>();

		List<GridCell> initialInboundVertexes = new ArrayList<GridCell>();
		List<GridCell> initialOutboundVertexes = new ArrayList<GridCell>();

		int inboundMinX = Integer.MIN_VALUE;

		for (int i = 0; i < c.getLength(); i++) {
			for (int j = 0; j < inbound; j++) {
				int index = i * c.width + j;
				c.centers.get(index).setColor(Color.magenta);
				inboundVertexes.add(c.centers.get(index));
				initialInboundVertexes.add(c.centers.get(index));

				if (c.centers.get(index).x > inboundMinX) {
					inboundMinX = c.centers.get(index).x;
				}
			}
			for (int j = 0; j < outbound; j++) {
				int index = i * c.width + j + inbound;
				c.centers.get(index).setColor(Color.CYAN);
				outboundVertexes.add(c.centers.get(index));
				initialOutboundVertexes.add(c.centers.get(index));
			}
		}

		LinkedList<GridCell> open = new LinkedList<GridCell>();

		open.addAll(inboundVertexes);
		open.addAll(outboundVertexes);

		int initialInbound = inboundVertexes.size();
		int initialOutbound = outboundVertexes.size();
		while ((initialInbound + 2 * size >= inboundVertexes.size() || initialOutbound
				+ 2 * size >= outboundVertexes.size())
				&& !open.isEmpty()) {
			GridCell next = open.poll();
			if (inboundVertexes.contains(next)) {
				assert (!outboundVertexes.contains(next));
				List<MultiagentVertex> neighbors = next.getNeighbors();
				for (MultiagentVertex v : neighbors) {
					GridCell neighbor = (GridCell) v;
					if (!neighbor.isTravel())
						continue;
					if (inboundVertexes.contains(neighbor))
						continue;
					if (outboundVertexes.contains(neighbor))
						continue;
					if (initialInbound + 2 * size < inboundVertexes.size()) {
						open.add(neighbor);
						continue;
					}
					if (neighbor.x > inboundMinX) {
						continue;
					}
					inboundVertexes.add(neighbor);
					open.add(neighbor);
				}
			} else if (outboundVertexes.contains(next)) {
				assert (!inboundVertexes.contains(next));
				List<MultiagentVertex> neighbors = next.getNeighbors();
				for (MultiagentVertex v : neighbors) {
					GridCell neighbor = (GridCell) v;
					if (!neighbor.isTravel())
						continue;
					if (inboundVertexes.contains(neighbor))
						continue;
					if (outboundVertexes.contains(neighbor))
						continue;
					if (initialOutbound + 2 * size < outboundVertexes.size()) {
						open.add(neighbor);
						continue;
					}
					if (neighbor.x <= inboundMinX) {
						continue;
					}
					outboundVertexes.add(neighbor);
					open.add(neighbor);
				}
			}
		}

		open.clear();
		open.addAll(outboundVertexes);
		open.addAll(inboundVertexes);

		for (int i = 0; i < BORDER_SIZE; i++) {
			GridCell next = open.poll();
			List<MultiagentVertex> neighbors = next.getNeighbors();
			for (MultiagentVertex v : neighbors) {
				GridCell neighbor = (GridCell) v;
				if (!neighbor.isTravel())
					continue;
				if (inboundVertexes.contains(neighbor))
					continue;
				if (outboundVertexes.contains(neighbor))
					continue;
				neighbor.setInvalidGoal();
				// neighbor.setColor(Color.yellow);
				open.add(neighbor);
			}

		}

		for (GridCell g : inboundVertexes) {
			g.setColor(Color.magenta);
		}
		for (GridCell g : outboundVertexes) {
			g.setColor(Color.cyan);
		}
		for (GridCell g : initialInboundVertexes) {
			g.setColor(Color.red);
		}
		for (GridCell g : initialOutboundVertexes) {
			g.setColor(Color.blue);
		}
		
		AutoQueue inboundQueue = new AutoQueue(initialInboundVertexes,
				inboundVertexes, QUEUE_ORIENTATION.DOWN, rt);
		AutoQueue outboundQueue = new AutoQueue(initialOutboundVertexes,
				outboundVertexes, QUEUE_ORIENTATION.UP, rt);

		inboundQueue.routeQueue();
		outboundQueue.routeQueue();

		if (inboundQueue.primaryExit == null
				|| outboundQueue.primaryExit == null) {
			inboundQueue.remove();
			outboundQueue.remove();
			return null;
		}

		return new AutoGateway(inboundQueue, outboundQueue);
	}

	private final QUEUE_ORIENTATION orientation;

	public enum QUEUE_ORIENTATION {
		UP, DOWN, LEFT, RIGHT
	}

	public void acceptAgent(Agent a, int goalTime) {
		assert (canAcceptAgent(a, a.getState().getEndVertex(),
				a.getCurrentTime(), goalTime));
		assert (a.getCurrentTime() > currentTime);
		futureAgents.add(new ArrivingAgent(a, a.getCurrentTime(), goalTime));
	}

	private void removeVertex(GridCell vertex) {
		vertex.setAutoQueue(null);
		vertex.changeToTravel();
		vertex.setColor(null);

	}

	/**
	 * Gets rid of this queue
	 */
	public void remove() {
		Iterator<Map.Entry<GridCell, List<GridCell>>> iter = routes.entrySet()
				.iterator();
		while (iter.hasNext()) {
			GridCell next = iter.next().getKey();
			this.removeVertex(next);
		}
	}

	public void routeQueue() {
		for (Map.Entry<GridCell, List<GridCell>> e : this.routes.entrySet()) {
			e.getKey().changeToQueue();
		}
		for (GridCell g : targets) {
			g.changeToPick();
		}

		switch (orientation) {
		case LEFT:
			routeLeft();
			break;
		case RIGHT:
			routeRight();
			break;
		case UP:
			routeUp();
			break;
		case DOWN:
			routeDown();
			break;
		}
		if (primaryExit == null) {
			this.remove();
			// if the primary exit is null, should just get rid of this queue.
		}
	}

	private GridCell primaryExit;

	public GridCell getPrimaryExit() {
		return this.primaryExit;
	}

	public List<GridCell> getRoutes(GridCell g) {
		return this.routes.get(g);
	}

	private HashMap<GridCell, Integer> getDepths() {
		LinkedList<GridCell> open = new LinkedList<GridCell>(targets);

		HashMap<GridCell, Integer> depths = new HashMap<GridCell, Integer>();
		for (GridCell g : open) {
			depths.put(g, new Integer(0));
		}

		while (!open.isEmpty()) {
			GridCell next = open.poll();
			Integer parentDepth = depths.get(next);
			Integer childDepth = new Integer(parentDepth + 1);
			for (MultiagentVertex c : next.getNeighbors()) {
				if (depths.containsKey(c))
					continue;
				GridCell child = (GridCell) c;
				if (child.getAutoQueue() != this)
					continue;
				open.add(child);
				depths.put(child, childDepth);
			}
		}

		return depths;
	}

	private static final class Node {
		public final MultiagentVertex v;
		public final double cost;

		public Node(MultiagentVertex v, double cost) {
			this.cost = cost;
			this.v = v;
		}

		@Override
		public String toString() {
			return "Node [v=" + v + ", cost=" + cost + "]\n";
		}

	}

	private boolean pathsPrepared = false;

	public void preparePaths() {
		// only prepare the paths for this queue once.
		if (pathsPrepared)
			return;
		this.pathsPrepared = true;

		this.initDistances();
	}

	private final HashMap<MultiagentVertex, HStarValue> distances = new HashMap<MultiagentVertex, HStarValue>();

	public Double distanceToQueue(MultiagentVertex v) {
		HStarValue hsv = distances.get(v);
		if (hsv == null)
			return null;
		return hsv.distHStar;
	}

	private final void initDistances() {

		Limit l = new Limit();
		l.startClock();

		List<MultiagentVertex> entry = new LinkedList<MultiagentVertex>();

		for (Map.Entry<GridCell, List<GridCell>> route : routes.entrySet()) {
			boolean skip = false;
			List<GridCell> targets = route.getValue();
			for (GridCell g : targets) {
				if (!routes.containsKey(g)) {
					skip = true;
				}
			}
			if (!skip)
				entry.add(route.getKey());
		}

		LinkedList<Node> open = new LinkedList<Node>();
		for (MultiagentVertex v : entry) {
			open.add(new Node(v, 0));
		}
		while (!open.isEmpty()) {
			Node n = open.poll();

			HStarValue inc = distances.get(n.v);
			if (inc != null) {
				// check to see how the previous version compares to the new
				// one.
				if (inc.distHStar > n.cost) {
					throw new RuntimeException("Shouldn't happen");
				} else if (inc.distHStar <= n.cost) {
					l.incrDup();
					continue;
				}
			}
			l.incrExp();
			distances.put(n.v, new HStarValue(n.cost));
			for (MultiagentVertex v : n.v.getNeighbors()) {
				if (v.isTraversible() && v.getAutoQueue() == null) {
					l.incrExp();
					open.add(new Node(v, n.cost + 1));
				}
			}
		}
		l.endClock();
		SearchTracker.getTracker().incrHStar(l);
	}

	/**
	 * Checks all of the vertexes in the queue, eliminating routes that lead
	 * nowhere.
	 * @param rec 
	 * @param changed 
	 * @return 
	 */
	public HashSet<GridCell> checkVertexes(int rec, HashSet<GridCell> changed) {
		if (rec == 0)
			return changed;
		Iterator<Map.Entry<GridCell, List<GridCell>>> iter = routes.entrySet()
				.iterator();
		while (iter.hasNext()) {
			Map.Entry<GridCell, List<GridCell>> next = iter.next();
			GridCell start = next.getKey();
			List<GridCell> targets = next.getValue();
			if (targets.isEmpty()) {
				iter.remove();
				removeVertex(start);
				changed.add(start);
			}
			Iterator<GridCell> targetIter = targets.iterator();
			while (targetIter.hasNext()) {
				GridCell g = targetIter.next();
				if (g.isTravel()) {
					ArrayList<GridCell> p = g.openPath(this.primaryExit);
					if (p == null) {
						targetIter.remove();
						changed.add(g);
					}
				} else if (routes.containsKey(g)) {

				} else {
					// TODO this has to go back
					// assert(false);
				}
			}
		}

		return checkVertexes(rec - 1, changed);
	}

	private final void routeLeft() {
		for (GridCell g : this.targets) {
			addRoute(g, g.left());
			addRoute(g.right(), g);
		}

		for (int i = 0; i < 100; i++) {
			GridCell n = this.targets.get(0).left(i);
			if (n == null)
				break;
			if (n.isTravel()) {
				this.primaryExit = n;
				// n.setColor(Color.pink);
				break;
			}
		}

		HashMap<GridCell, Integer> depths = getDepths();

		LinkedList<GridCell> open = new LinkedList<GridCell>();
		for (GridCell g : targets) {
			open.add(g.left());
		}
		while (!open.isEmpty()) {
			GridCell next = open.poll();
			int parentDepth = depths.get(next);
			for (MultiagentVertex c : next.getNeighbors()) {
				GridCell child = (GridCell) c;
				if (child.getAutoQueue() != null
						&& child.getAutoQueue() != this)
					continue;
				// child is an ordinary low contention cell
				if (!depths.containsKey(child) && child.getAutoQueue() == null) {
					addRoute(next, child);
					continue;
				}
				// child is under the control of this queue
				assert (depths.get(child) != null);
				int childDepth = depths.get(child);
				if (childDepth > parentDepth) {
					addRoute(next, child);
					open.add(child);
				}
			}
		}

		for (GridCell g : targets) {
			open.add(g.right());
		}
		while (!open.isEmpty()) {
			GridCell next = open.poll();
			int parentDepth = depths.get(next);
			for (MultiagentVertex c : next.getNeighbors()) {
				GridCell child = (GridCell) c;
				if (child.getAutoQueue() != null
						&& child.getAutoQueue() != this)
					continue;
				// child is an ordinary low contention cell
				if (!depths.containsKey(child) && child.getAutoQueue() == null) {
					continue;
				}
				// child is under the control of this queue
				assert (depths.get(child) != null);
				int childDepth = depths.get(child);
				if (childDepth > parentDepth) {
					addRoute(child, next);
					open.add(child);
				}
			}
		}

	}

	private final void routeRight() {
		for (GridCell g : this.targets) {
			addRoute(g, g.right());
			addRoute(g.left(), g);
		}
		for (int i = 0; i < 10; i++) {
			GridCell n = this.targets.get(0).right(i);
			if (n == null)
				break;

			if (n.isTravel()) {
				this.primaryExit = n;
				// n.setColor(Color.pink);

				break;
			}
		}

		HashMap<GridCell, Integer> depths = getDepths();

		LinkedList<GridCell> open = new LinkedList<GridCell>();
		for (GridCell g : targets) {
			open.add(g.right());
		}
		while (!open.isEmpty()) {
			GridCell next = open.poll();
			int parentDepth = depths.get(next);
			for (MultiagentVertex c : next.getNeighbors()) {
				GridCell child = (GridCell) c;
				if (child.getAutoQueue() != null
						&& child.getAutoQueue() != this)
					continue;
				// child is an ordinary low contention cell
				if (!depths.containsKey(child) && child.getAutoQueue() == null) {
					addRoute(next, child);
					continue;
				}
				// child is under the control of this queue
				assert (depths.get(child) != null);
				int childDepth = depths.get(child);
				if (childDepth > parentDepth) {
					addRoute(next, child);
					open.add(child);
				}
			}
		}

		for (GridCell g : targets) {
			open.add(g.left());
		}
		while (!open.isEmpty()) {
			GridCell next = open.poll();
			int parentDepth = depths.get(next);
			for (MultiagentVertex c : next.getNeighbors()) {
				GridCell child = (GridCell) c;
				if (child.getAutoQueue() != null
						&& child.getAutoQueue() != this)
					continue;
				// child is an ordinary low contention cell
				if (!depths.containsKey(child) && child.getAutoQueue() == null) {
					continue;
				}
				// child is under the control of this queue
				assert (depths.get(child) != null);
				int childDepth = depths.get(child);
				if (childDepth > parentDepth) {
					addRoute(child, next);
					open.add(child);
				}
			}
		}
	}

	private final void routeDown() {
		for (GridCell g : this.targets) {
			addRoute(g, g.up());
			addRoute(g.down(), g);
		}

		for (int i = 0; i < 100; i++) {
			GridCell n = this.targets.get(0).up(i);
			if (n == null)
				break;
			if (n.isTravel()) {
				this.primaryExit = n;
				// n.setColor(Color.pink);
				break;
			}
		}

		HashMap<GridCell, Integer> depths = getDepths();

		LinkedList<GridCell> open = new LinkedList<GridCell>();
		for (GridCell g : targets) {
			open.add(g.up());
		}
		while (!open.isEmpty()) {
			GridCell next = open.poll();
			int parentDepth = depths.get(next);
			for (MultiagentVertex c : next.getNeighbors()) {
				GridCell child = (GridCell) c;
				if (child.getAutoQueue() != null
						&& child.getAutoQueue() != this)
					continue;
				// child is an ordinary low contention cell
				if (!depths.containsKey(child) && child.getAutoQueue() == null) {
					addRoute(next, child);
					continue;
				}
				// child is under the control of this queue
				assert (depths.get(child) != null);
				int childDepth = depths.get(child);
				if (childDepth > parentDepth) {
					addRoute(next, child);
					open.add(child);
				}
			}
		}

		for (GridCell g : targets) {
			open.add(g.down());
		}
		while (!open.isEmpty()) {
			GridCell next = open.poll();
			int parentDepth = depths.get(next);
			for (MultiagentVertex c : next.getNeighbors()) {
				GridCell child = (GridCell) c;
				if (child.getAutoQueue() != null
						&& child.getAutoQueue() != this)
					continue;
				// child is an ordinary low contention cell
				if (!depths.containsKey(child) && child.getAutoQueue() == null) {
					continue;
				}
				// child is under the control of this queue
				assert (depths.get(child) != null);
				int childDepth = depths.get(child);
				if (childDepth > parentDepth) {
					addRoute(child, next);
					open.add(child);
				}
			}
		}

	}

	private void addRoute(MultiagentVertex start, GridCell end) {
		if (!routes.get(start).contains(end))
			routes.get(start).add(end);
	}

	private final void routeUp() {
		for (GridCell g : this.targets) {
			addRoute(g, g.down());
			addRoute(g.up(), g);
		}
		for (int i = 0; i < 100; i++) {
			GridCell n = this.targets.get(0).down(i);
			if (n == null)
				break;
			if (n.isTravel()) {
				this.primaryExit = n;
				// n.setColor(Color.pink);

				break;
			}
		}

		HashMap<GridCell, Integer> depths = getDepths();

		LinkedList<GridCell> open = new LinkedList<GridCell>();
		for (GridCell g : targets) {
			open.add(g.down());
		}
		while (!open.isEmpty()) {
			GridCell next = open.poll();

			int parentDepth = depths.get(next);
			for (MultiagentVertex c : next.getNeighbors()) {
				GridCell child = (GridCell) c;
				if (child.getAutoQueue() != null
						&& child.getAutoQueue() != this)
					continue;
				// child is an ordinary low contention cell
				if (!depths.containsKey(child) && child.getAutoQueue() == null) {
					addRoute(next, child);
					continue;
				}
				// child is under the control of this queue
				assert (depths.get(child) != null);
				int childDepth = depths.get(child);
				if (childDepth > parentDepth) {
					addRoute(next, child);
					open.add(child);
				}
			}
		}

		for (GridCell g : targets) {
			open.add(g.up());
		}
		while (!open.isEmpty()) {
			GridCell next = open.poll();
			int parentDepth = depths.get(next);
			for (MultiagentVertex c : next.getNeighbors()) {
				GridCell child = (GridCell) c;
				if (child.getAutoQueue() != null
						&& child.getAutoQueue() != this)
					continue;
				// child is an ordinary low contention cell
				if (!depths.containsKey(child) && child.getAutoQueue() == null) {
					continue;
				}
				// child is under the control of this queue
				assert (depths.get(child) != null);
				int childDepth = depths.get(child);
				if (childDepth > parentDepth) {
					addRoute(child, next);
					open.add(child);
				}
			}
		}
	}

	private int currentTime;
	protected final PriorityQueue<org.cwilt.search.domains.multiagent.solvers.queue.ArrivingAgent> futureAgents;
	protected final LinkedList<QueuedAgent> currentAgents;

	public void advanceAgents(AutoQueueSolver s) throws QueueOverflow {
		currentTime++;
		HashMap<MultiagentVertex, Agent> current = new HashMap<MultiagentVertex, Agent>();
		assert (futureAgents.isEmpty() || futureAgents.peek().arrivalTime >= currentTime);
		while (!futureAgents.isEmpty()
				&& futureAgents.peek().arrivalTime == currentTime) {

			org.cwilt.search.domains.multiagent.solvers.queue.ArrivingAgent newArrival = futureAgents
					.poll();
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
			List<GridCell> desiredCells = routes.get(q.v);

			GridCell next = null;
			boolean agentExited = false;
			for (GridCell desiredCell : desiredCells) {
				if (desiredCell.getAutoQueue() == null) {
					// this cell is a border cell, so try to route to the next
					// destination
					boolean exited = this.pushAgentThrough(q.a, s);
					if (exited) {
						agentExited = true;
						break;
					}
					// if this desired cell is outside, don't want to
					// accidentally go here.
					continue;
				}
				Agent currentOwner = current.get(desiredCell);
				Agent nextOwner = current.get(desiredCell);
				// if someone else owns the vertex, have to wait.
				if (currentOwner != null || nextOwner != null) {
					continue;
				} else {
					next = desiredCell;
					break;
				}
			}
			if (agentExited) {
				iter.remove();
				continue;
			}
			if (next == null) {
				current.put(q.v, q.a);
				q.a.doMove(q.v, q.v);
			} else {
				assert (next.getAutoQueue() == this);
				current.put(next, q.a);
				q.a.doMove(q.v, next);
				if (next != null)
					q.v = next;
			}

		}

		// copying over, to check if everything is still okay.
		PriorityQueue<ArrivingAgent> arrivals = new PriorityQueue<ArrivingAgent>();
		for (ArrivingAgent a : futureAgents) {
			arrivals.add(a);
		}
		LinkedList<QueuedAgent> currentAgents = new LinkedList<QueuedAgent>();
		for (QueuedAgent q : currentAgents) {
			currentAgents.addLast((QueuedAgent) q.clone());
		}
		boolean result = false;
		result = simulateForwards(arrivals, currentAgents, routes, currentTime);
		assert (result);

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
			HashMap<GridCell, List<GridCell>> routingTable) {

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
					QueuedAgent temp = new QueuedAgent(a.arrivalVertex,
							a.agent, a.goalTime);
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

			if (q.v == null) {
				iter.remove();
				continue;
			}
			q.queueTime++;
			// TODO it looks like this has to be updated so that it actually
			// terminates when the agent reaches the end of the queue.
			List<GridCell> desiredCells = routingTable.get(q.v);
			if (((GridCell) q.v).getAutoQueue() == null) {
				iter.remove();
				continue;
			}
			assert (desiredCells != null);
			for (GridCell desiredCell : desiredCells) {
				// on the first step they can't do anything yet?
				if (q.queueTime == 1) {
					desiredCell = (GridCell) q.v;
				}

				QueuedAgent currentOwner = current.get(desiredCell);
				QueuedAgent nextOwner = current.get(desiredCell);

				// if someone else owns the vertex, have to wait.
				if (currentOwner != null || nextOwner != null) {
					// Check to see who owns the vertex, and if this drive is
					// ahead, reject the reservation request.
					// if someone owns either vertex, have to wait.
					if (currentOwner == nextOwner && currentOwner == q)
						continue;
					if (nextOwner.queueTime <= q.queueTime)
						return false;
					if (currentOwner.queueTime <= q.queueTime)
						return false;

					current.put(q.v, q);
				} else {
					current.put(desiredCell, q);
					if (desiredCell != null)
						q.v = desiredCell;
				}
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
			HashMap<GridCell, List<GridCell>> routingTable, int simulationTime) {

		while (!arrivals.isEmpty() || !inQueue.isEmpty()) {
			// assert (simulationTime < 1000);
			boolean passed = simulateOneStep(arrivals, inQueue, simulationTime,
					routingTable);
			simulationTime++;
			if (!passed)
				return false;
		}

		return true;
	}

	public boolean isDone() {
		return this.futureAgents.isEmpty() && this.currentAgents.isEmpty();
	}

	public void checkAgents() {
		for (QueuedAgent q : currentAgents) {
			if (q.a.getCurrentTime() != this.currentTime) {
				System.err.println("agent " + q.a.getID() + " time "
						+ q.a.getCurrentTime());
				System.err.println("Failed in queue ID " + this.queueID);
				System.err.println("This current time " + currentTime);
				assert (false);
			}

		}
	}

	private final ReservationTable reservations;

	private boolean pushAgentThrough(Agent a, AutoQueueSolver qs) {
		// get the agent's next destination
		MultiagentTask t = a.getNextTask();

		if (this.containsCell((GridCell) t.destination)) {
			a.doTask(true);
		}
		// TODO the agents need to not use the "next" destination, but rather
		// the destination after the one they are currently working on. Maybe
		// this needs to be marked as completed a bit earlier or something.

		// this task had better exist

		t = a.getNextTask();

		assert (t != null);
		// this task had better not be here.
		assert (t.destination != a.getState().getEndVertex());
		assert (a.getReservationTable() == reservations);
		SearchToAutoQueue s = new SearchToAutoQueue(
				a.getState().getEndVertex(), t.destination,
				t.timeAtDestination, a, a.getReservationTable(), false, true);
		int allocatedExpansions = s.estimatedCost
				* AutoQueueSolver.EXPANSION_FACTOR;

		// AutoQueueSearcher alg = new AutoQueueSearcher(s, new
		// search.Limit(Long.MAX_VALUE, allocatedExpansions,
		// Long.MAX_VALUE, false));
		// ArrayList<search.SearchState> path = alg.solve();

		org.cwilt.search.search.SearchAlgorithm alg = new org.cwilt.search.algs.basic.bestfirst.AStar(s,
				new org.cwilt.search.search.Limit(Long.MAX_VALUE, allocatedExpansions,
						Long.MAX_VALUE, false));
		List<org.cwilt.search.search.SearchState> path = alg.solve();
		SearchTracker.getTracker().incrSearchTime3dAll(alg.getLimit());

		// assert(path != null);
		if (path == null) {
			SearchTracker.getTracker().incrSearchTime3dFail(alg.getLimit());

			// try again next time?
			// System.err.println("Finding a return path failed");
			// System.err.println("Starting at " + this.target + " to " +
			// t.destination);
			// alg.printSearchData(System.err);
			// System.exit(1);

			MultiagentVertex newTarget = s.bestWaitLocation();

			if (newTarget == null) {
				return false;
			}
			SearchToAutoQueue newPlan = new SearchToAutoQueue(a.getState()
					.getEndVertex(), newTarget, a.getCurrentTime(), a,
					a.getReservationTable(), false, true);
			org.cwilt.search.search.SearchAlgorithm newAlg = new org.cwilt.search.algs.basic.bestfirst.AStar(
					newPlan, new org.cwilt.search.search.Limit(1000, Long.MAX_VALUE,
							Long.MAX_VALUE, false));
			List<org.cwilt.search.search.SearchState> newPath = newAlg.solve();
			SearchTracker.getTracker().incrSearchTime3dAll(newAlg.getLimit());

			if (newPath != null) {
				assert (newPath != null);
				for (int i = 1; i < newPath.size(); i++) {
					MultiagentVertex original = ((AutoQueueSearchNode) newPath
							.get(i - 1)).key.v;
					MultiagentVertex next = ((AutoQueueSearchNode) newPath
							.get(i)).key.v;
					a.doMove(original, next);
				}
				a.getReservationTable().claimIndefinitely(newTarget,
						a.getCurrentTime(), a);
				qs.acceptAgent(a);
				return true;
			} else {
				return false;
			}
		} else {
			for (int i = 1; i < path.size(); i++) {
				MultiagentVertex original = ((AutoQueueSearchNode) path
						.get(i - 1)).key.v;
				MultiagentVertex next = ((AutoQueueSearchNode) path.get(i)).key.v;
				a.doMove(original, next);
			}
			AutoQueue q = ((GridCell) a.getState().getEndVertex())
					.getAutoQueue();
			if (q != null) {
				// add this agent to the queue
				q.acceptAgent(a,
						a.getTask(a.getCurrentTaskID() + 1).timeAtDestination);
			} else {
				this.solver.acceptAgent(a);
			}
		}

		return true;
	}

	public boolean canAcceptAgent(Agent agent, MultiagentVertex v,
			int arrivalTime, int goalTime) {
		// reject everything that isn't even in the queue obviously
		if (!routes.containsKey(v))
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
		result = simulateForwards(arrivals, current, routes, currentTime);

		return result;
	}

	public int getQueueID() {
		return queueID;
	}

	private AutoQueueSolver solver;

	public void setSolver(AutoQueueSolver s) {
		this.solver = s;
	}

}
