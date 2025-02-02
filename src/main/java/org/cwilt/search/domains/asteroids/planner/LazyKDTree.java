package org.cwilt.search.domains.asteroids.planner;
import java.awt.Graphics2D;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A lazy KD tree that returns an approximate nearest neighbor instead of the
 * actual nearest neighbor.
 * 
 * @author cmo66
 * 
 */
public class LazyKDTree {
	private static final int NODEMAX = 50;
	private static final int SAMPLE = 10;
	private final AsteroidState initial;

	private final Random r = new Random(3);

	private static enum NODETYPE {
		ROOT, LEFT, RIGHT
	}

	private AsteroidState betterState(AsteroidState original,
			AsteroidState other) {
		if (original.getTime() > other.getTime())
			return original;
		if (original.getTime() < other.getTime())
			return other;
		if (original.getValue() < other.getValue())
			return other;
		if (other.getValue() < original.getValue())
			return original;
		if (r.nextBoolean())
			return original;
		else
			return other;
	}

	private final class KDLeaf extends KDNode {
		private final Vector<AsteroidState> nodes = new Vector<AsteroidState>(
				NODEMAX);

		public KDLeaf(int index, NODETYPE t, KDInterior parent) {
			super(index, t, parent);
		}

		private double median() {
			double sample[] = new double[SAMPLE];
			for (int i = 0; i < SAMPLE; i++) {
				sample[i] = b.bucket(nodes.get(r.nextInt(NODEMAX)), level + 1);
			}
			java.util.Arrays.sort(sample);
			return sample[SAMPLE / 2];
		}

		@Override
		public void add(AsteroidState s, int level) {

			assert (level == super.level);
			if (nodes.size() < NODEMAX) {
				nodes.add(s);
			} else {
				// split this thing
				double split = median();
				KDInterior newNode = new KDInterior(level, split, t, parent);
				KDLeaf left = new KDLeaf(level + 1, NODETYPE.LEFT, newNode);
				KDLeaf right = new KDLeaf(level + 1, NODETYPE.RIGHT, newNode);

				for (AsteroidState state : nodes) {
					if (b.bucket(state, level + 1) < split) {
						left.nodes.add(state);
					} else {
						right.nodes.add(state);
					}
				}

				newNode.left = left;
				newNode.right = right;
				// adjust the root

				if (this == root)
					root = newNode;
				else {
					parent.setChild(newNode);
				}
			}
		}

		@Override
		public void setChild(KDNode newChild) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected AsteroidState approxNearestNeighbor(AsteroidState n,
				RRT metric) {
			double minDistance = Double.MAX_VALUE;
			AsteroidState incumbent = null;

			for (AsteroidState s : nodes) {
				assert (s != null);
				if (s.isTerminal())
					continue;
				double distance = metric.rrtDistance(s, n);
				if (distance < minDistance) {
					minDistance = distance;
					incumbent = s;
				}
			}
			return incumbent;
		}

		@Override
		public AsteroidState findBest() {
			// TODO I don't understand why this works the way it does, I tried
			// using the same criteria as the end, and those worked poorly.

			// AsteroidState inc = null;
			// double bestH = Double.MAX_VALUE;
			// for (AsteroidState s : nodes) {
			// if (s.isTerminal())
			// continue;
			// if (s == initial)
			// continue;
			// if (s.h() < bestH) {
			// bestH = s.h();
			// inc = s;
			// }
			// }

			// AsteroidState inc = null;
			// double bestValue = Double.MAX_VALUE * -1;
			// for (AsteroidState s : nodes) {
			// if (s.isTerminal())
			// continue;
			// if (s == initial)
			// continue;
			// if (s.getValue() > bestValue) {
			// bestValue = s.getValue();
			// inc = s;
			// }
			// }

			if (nodes.isEmpty())
				return initial;
			AsteroidState inc = nodes.get(0);
			for (AsteroidState s : nodes) {
				if (s.isTerminal())
					continue;
				if (s == initial)
					continue;
				inc = betterState(inc, s);
			}
			return inc;
		}

		@Override
		public void print(PrintStream ps) {
			for (AsteroidState s : nodes) {
				ps.println(s);
			}
		}

		@Override
		public void draw(Graphics2D g, int horizon) {
			for (AsteroidState s : nodes) {
				s.drawRRTVisualization(g, initial.getTime(), horizon);
			}
		}

		@Override
		public AsteroidState nearestNeighbor(AsteroidState a, RRT metric) {
			return this.approxNearestNeighbor(a, metric);
		}
	}

	private final class KDInterior extends KDNode {
		private KDInterior(int index, double split, NODETYPE t,
				KDInterior parent) {
			super(index, t, parent);
			this.split = split;
		}

		private KDNode left;
		private KDNode right;
		public final double split;

		@Override
		public void add(AsteroidState s, int level) {
			assert (this.level == level);
			double value = b.bucket(s, this.level);

			if (value < split)
				left.add(s, level + 1);
			else
				right.add(s, level + 1);
		}

		@Override
		public void setChild(KDNode newChild) {
			if (newChild.t == NODETYPE.LEFT)
				this.left = newChild;
			else if (newChild.t == NODETYPE.RIGHT)
				this.right = newChild;
			else
				assert (false);
		}

		@Override
		public AsteroidState findBest() {
			AsteroidState leftBest = left.findBest();
			AsteroidState rightBest = right.findBest();

			if (leftBest == null && rightBest == null)
				return null;
			else if (leftBest == null && rightBest != null)
				return rightBest;
			else if (leftBest != null && rightBest == null)
				return leftBest;
			return betterState(leftBest, rightBest);
		}

		@Override
		public void print(PrintStream ps) {
			left.print(ps);
			right.print(ps);
		}

		@Override
		public void draw(Graphics2D g, int horizon) {
			left.draw(g, horizon);
			right.draw(g, horizon);
		}

		@Override
		protected AsteroidState approxNearestNeighbor(AsteroidState n,
				RRT metric) {
			double v = b.bucket(n, this.level);
			if (v < split) {
				AsteroidState neighbor = left.approxNearestNeighbor(n, metric);
				if (neighbor != null)
					return neighbor;
				neighbor = right.approxNearestNeighbor(n, metric);
				assert (neighbor != null);
				return neighbor;
			} else {
				AsteroidState neighbor = right.approxNearestNeighbor(n, metric);
				if (neighbor != null)
					return neighbor;
				neighbor = left.approxNearestNeighbor(n, metric);
				assert (neighbor != null);
				return neighbor;
			}
		}

		@Override
		public AsteroidState nearestNeighbor(AsteroidState a, RRT metric) {
			AsteroidState leftBest = left.nearestNeighbor(a, metric);
			AsteroidState rightBest = right.nearestNeighbor(a, metric);

			if (leftBest == null && rightBest == null)
				return null;
			else if (leftBest == null && rightBest != null)
				return rightBest;
			else if (leftBest != null && rightBest == null)
				return leftBest;

			// both are not null, check both distances

			double leftDist = metric.rrtDistance(leftBest, a);
			double rightDist = metric.rrtDistance(rightBest, a);

			if (leftDist < rightDist)
				return leftBest;
			if (rightDist < leftDist)
				return rightBest;

			if (r.nextBoolean())
				return leftBest;
			else
				return rightBest;
		}
	}

	private abstract class KDNode {

		public abstract void print(PrintStream ps);

		public abstract AsteroidState findBest();

		protected abstract AsteroidState approxNearestNeighbor(AsteroidState n,
				RRT metric);

		protected final int level;
		protected final NODETYPE t;
		protected final KDInterior parent;

		protected KDNode(int level, NODETYPE t, KDInterior parent) {
			this.level = level;
			this.t = t;
			this.parent = parent;
		}

		public abstract void add(AsteroidState s, int level);

		public abstract void setChild(KDNode newChild);

		public abstract void draw(Graphics2D g, int horizon);

		public abstract AsteroidState nearestNeighbor(AsteroidState a,
				RRT metric);
	}

	static abstract class KDBucketer {
		public abstract double bucket(AsteroidState s, int index);
	}

	static class XYBucketer extends KDBucketer {
		public double bucket(AsteroidState s, int index) {
			if (index % 2 == 0) {
				return s.getShip().x;
			} else {
				return s.getShip().y;
			}
		}
	}

	static class XYThetaBucketer extends KDBucketer {
		public double bucket(AsteroidState s, int index) {
			if (index % 3 == 0) {
				return s.getShip().x;
			} else if (index % 3 == 1) {
				return s.getShip().y;
			} else {
				return s.getShip().theta;
			}
		}
	}

	private KDNode root;
	private final KDBucketer b;

	public LazyKDTree(AsteroidState initial, int endTime, KDBucketer b) {
		assert (!initial.isTerminal());
		this.root = new KDLeaf(0, NODETYPE.ROOT, null);
		this.b = b;
		this.initial = initial;
		this.endTime = endTime;
	}

	private final int endTime;
	private final Vector<AsteroidState> endNodes = new Vector<AsteroidState>();
	private final Set<AsteroidState> nodes = Collections.newSetFromMap(new ConcurrentHashMap<AsteroidState, Boolean>());

	public void add(AsteroidState s) {
		if (nodes.contains(s))
			return;
		nodes.add(s);
		assert (s.getTime() >= initial.getTime());

		if (s.getTime() == endTime)
			endNodes.add(s);
		else if (s.getTime() > endTime) {
			throw new RuntimeException(
					"Adding a node that is too far into the future");
		} else {
			synchronized (this) {
				root.add(s, 0);
			}
		}
	}

	public AsteroidState approxNearestNeighbor(AsteroidState a, RRT metric) {
		AsteroidState nn = null;
		synchronized (this) {
			KDNode current = root;
			nn = current.approxNearestNeighbor(a, metric);
			assert (nn != null);
		}
		return nn;
	}

	public AsteroidState nearestNeighbor(AsteroidState a, RRT metric) {
		AsteroidState nn = null;
		synchronized (this) {
			KDNode current = root;
			nn = current.nearestNeighbor(a, metric);
		}
		return nn;
	}

	public AsteroidState findBestHorizon() {
		if (endNodes.isEmpty())
			return null;
		Collections.shuffle(endNodes, r);
		double bestScore = endNodes.get(0).getValue();
		AsteroidState best = endNodes.get(0);
		for (AsteroidState s : endNodes) {
			if (s.getValue() > bestScore) {
				bestScore = s.getValue();
				best = s;
			}
		}
		return best;
	}

	public AsteroidState findBest() {
		if (endNodes.isEmpty())
			// found no terminal nodes, do the next best thing
			return root.findBest();
		else {
			// look through the terminal nodes and try to find the best one.
			Collections.shuffle(endNodes, r);
			double bestScore = endNodes.get(0).getValue();
			AsteroidState best = endNodes.get(0);
			for (AsteroidState s : endNodes) {
				if (s.getValue() > bestScore) {
					bestScore = s.getValue();
					best = s;
				}
			}

			// System.err.println(endNodes.size());
			return best;
		}
	}

	public void printTree() {
		root.print(System.out);
	}

	public void draw(Graphics2D g, int horizon) {
		root.draw(g, horizon);
		for (AsteroidState s : endNodes) {
			s.drawRRTTerminal(g);
		}
	}
}
