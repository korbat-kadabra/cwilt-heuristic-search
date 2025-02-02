package org.cwilt.search.domains.asteroids.planner;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.cwilt.search.domains.asteroids.AsteroidProblem;
import org.cwilt.search.domains.asteroids.planner.AsteroidPlanner.CrashedShip;
public class SeededRRT extends RRT {

	public SeededRRT(AsteroidState root, int horizon, int treeSize,
			boolean lazyNN, int seed, ArrayList<AsteroidState> seedPlan,
			int nThreads) throws CrashedShip {
		super(root, horizon, treeSize, lazyNN, seed, seedPlan, nThreads);
	}

	private int currentTreeSize = 0;

	protected synchronized void increaseCurrentTreeSize() {
		currentTreeSize++;
	}

	protected synchronized boolean treeDone() {
		return currentTreeSize >= super.treeSize;
	}

	protected class TreeBuilder implements Runnable {

		@Override
		public void run() {
			// System.err.println("Starting thread " + threadID);
			while (!treeDone()) {
				increaseCurrentTreeSize();
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
					next = tree.approxNearestNeighbor(dummy, SeededRRT.this);
				else
					next = tree.nearestNeighbor(dummy, SeededRRT.this);
				assert (next != null);
				ArrayList<AsteroidState> children = next.expandRaw();
				if (children.isEmpty())
					continue;
				double currentDistance = Double.MAX_VALUE;
				while (true) {

					AsteroidState newNode = findNearestNode(children, dummy);
					if (newNode != null) {
						tree.add(newNode);
						if (newNode.getTime() == SeededRRT.super.endTime)
							break;
					} else
						break;
					double newDistance = rrtDistance(newNode, dummy);
					if (newDistance < currentDistance) {
						currentDistance = newDistance;
						children = newNode.expandRaw();
					} else
						break;
				}
			}
			// System.err.println("Exiting thread " + threadID);
		}

		@SuppressWarnings("unused")
		private final int threadID;

		public TreeBuilder(int threadID) {
			this.threadID = threadID;
		}

	}
	
	protected TreeBuilder makeTreeBuilder(int index){
		return new TreeBuilder(index);
	}

	public void buildRRT() throws CrashedShip {
		// assert(nThreads > 1);
		if (nThreads > 1) {
			TreeBuilder[] treeBuilders = new TreeBuilder[nThreads];
			for (int i = 0; i < nThreads; i++) {
				treeBuilders[i] = makeTreeBuilder(i);
			}
			ExecutorService executor = Executors.newFixedThreadPool(nThreads);
			for (int i = 0; i < nThreads; i++) {
				executor.execute(treeBuilders[i]);
			}
			executor.shutdown();
			try {
				executor.awaitTermination(5, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			TreeBuilder t = new TreeBuilder(0);
			t.run();
		}

	}

}
