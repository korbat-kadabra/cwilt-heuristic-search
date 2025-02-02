package org.cwilt.search.domains.asteroids.planner;
import java.util.ArrayList;

import org.cwilt.search.domains.asteroids.AsteroidProblem;
import org.cwilt.search.domains.asteroids.planner.AsteroidPlanner.CrashedShip;
public class GraftingSeededRRT extends SeededRRT {

	public final ArrayList<AsteroidState> branchesToGraft;

	public GraftingSeededRRT(AsteroidState root, int horizon, int treeSize,
			boolean lazyNN, int seed, ArrayList<AsteroidState> seedPlan,
			int nThreads, GraftingSeededRRT[] forest) throws CrashedShip {
		super(root, horizon, treeSize, lazyNN, seed, seedPlan, nThreads);
		this.branchesToGraft = new ArrayList<AsteroidState>();
		this.forest = forest;
	}

	private final GraftingSeededRRT[] forest;

	protected TreeBuilder makeTreeBuilder(int index) {
		return new GraftingTreeBuilder(index);
	}

	private static boolean destroyedAsteroid(AsteroidState parent,
			AsteroidState child) {
		assert (parent.getTime() == child.getTime() - 1);
		double parentScore = parent.getScore();
		double childScore = child.getScore();

		return parentScore < childScore;
	}

	private void graftNode(AsteroidState s) {
		int timeDifference = s.getTime() - super.root.getTime();
		// don't graft things on that are too late in the plan, because they are
		// not that helpful to the recipient tree
		if (timeDifference > (2 * super.horizon) / 3) {
			return;
		}

		// System.err.println("grafting an RRT");
		for (int i = 0; i < forest.length; i++) {
			if (forest[i] == this)
				continue;
			synchronized (forest[i]) {
				forest[i].branchesToGraft.add(s);
			}
		}
	}

	protected final class GraftingTreeBuilder extends SeededRRT.TreeBuilder {

		public GraftingTreeBuilder(int threadID) {
			super(threadID);
		}

		@Override
		public void run() {
			// System.err.println("Starting thread " + threadID);
			while (!treeDone()) {
				increaseCurrentTreeSize();

				synchronized (GraftingSeededRRT.this) {
					for (int i = 0; i < 4; i++) {

						assert (branchesToGraft != null);
						if (!branchesToGraft.isEmpty()) {
							AsteroidState s = branchesToGraft
									.remove(branchesToGraft.size() - 1);
							tree.add(s);
						} else {
							continue;
						}
					}
				}
				// System.err.println("Tree Size is " + currentTreeSize +
				// " in thread " + threadID);
				double xSample = r.nextDouble() * AsteroidProblem.X_SIZE;
				double ySample = r.nextDouble() * AsteroidProblem.Y_SIZE;
				double thetaSample = r.nextDouble() * Math.PI * 2;
				int time = r.nextInt(horizon);
				AsteroidState dummy = AsteroidState.dummyState(xSample,
						ySample, thetaSample, time);
				AsteroidState next;
				if (lazyNN)
					next = tree.approxNearestNeighbor(dummy,
							GraftingSeededRRT.this);
				else
					next = tree.nearestNeighbor(dummy, GraftingSeededRRT.this);
				assert (next != null);
				ArrayList<AsteroidState> children = next.expandRaw();
				if (children.isEmpty())
					continue;
				double currentDistance = Double.MAX_VALUE;
				while (true) {

					AsteroidState newNode = findNearestNode(children, dummy);
					if (newNode != null) {
						tree.add(newNode);
						if (destroyedAsteroid(next, newNode)) {
							graftNode(newNode);
						}

						if (newNode.getTime() == GraftingSeededRRT.super.endTime)
							break;
					} else
						break;
					double newDistance = rrtDistance(newNode, dummy);
					if (newDistance < currentDistance) {
						currentDistance = newDistance;
						children = newNode.expandRaw();
						next = newNode;
					} else
						break;
				}
			}
			// System.err.println("Exiting thread " + threadID);
		}
	}

}
