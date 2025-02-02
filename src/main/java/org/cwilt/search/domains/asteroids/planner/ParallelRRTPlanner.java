package org.cwilt.search.domains.asteroids.planner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.cwilt.search.domains.asteroids.AsteroidProblem;
public class ParallelRRTPlanner extends RRTPlanner {
	ArrayList<AsteroidState> currentPlan;

	public ParallelRRTPlanner(AsteroidProblem a, int timeout, String[] args) {
		super(a, timeout, args);
		currentPlan = new ArrayList<AsteroidState>();
		planTrees = new RRT[super.rrtCount];
	}

	private RRT[] planTrees;

	private final class RRTBuilder implements Runnable {
		private final int threadID;

		public RRTBuilder(int threadID) {
			this.threadID = threadID;
		}

		@Override
		public void run() {
			try {
				planTrees[threadID] = new SeededRRT(
						ParallelRRTPlanner.super.getCurrentState(), horizon,
						treeSize, lazyNN, seed + threadID * 100, currentPlan, ParallelRRTPlanner.super.threadsPerRRT);
				planTrees[threadID].buildRRT();
			} catch (CrashedShip e) {
				return;
			}
		}

	}

	@Override
	public AsteroidState nextState() {
		if (stop)
			return null;

		System.err.println("Current time is "
				+ super.getCurrentState().getTime());
		System.err.println("Current path is " + currentPlan.size()
				+ " nodes long");

		RRTBuilder[] builders = new RRTBuilder[super.rrtCount];
		for (int i = 0; i < super.rrtCount; i++) {
			builders[i] = new RRTBuilder(i);
		}
		ExecutorService executor = Executors.newFixedThreadPool(super.rrtCount);
		for (int i = 0; i < super.rrtCount; i++) {
			executor.execute(builders[i]);
		}
		executor.shutdown();
		try {
			executor.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		ArrayList<AsteroidState> plan = getBestPlan();
		if (plan == null)
			return null;
		currentPlan = plan;
		return plan.get(plan.size() - 1);
	}

	private static final class PlanComparator implements
			Comparator<ArrayList<AsteroidState>> {

		@Override
		public int compare(ArrayList<AsteroidState> arg0,
				ArrayList<AsteroidState> arg1) {
			assert (arg0 != null);
			assert (arg1 != null);
			if (arg0.size() > arg1.size())
				return -1;
			else if (arg0.size() < arg1.size())
				return 1;

			double arg0Value = arg0.get(arg0.size() - 1).getValue();
			double arg1Value = arg1.get(arg1.size() - 1).getValue();

			if (arg0Value > arg1Value)
				return -1;
			if (arg0Value < arg1Value)
				return 1;
			return 0;
		}

	}

	private ArrayList<AsteroidState> getBestPlan() {
		ArrayList<ArrayList<AsteroidState>> plans = new ArrayList<ArrayList<AsteroidState>>(
				super.rrtCount);
		for (int i = 0; i < super.rrtCount; i++) {
			ArrayList<AsteroidState> planHere = planTrees[i].bestAction();
			if (planHere != null)
				plans.add(planHere);
		}
		if (plans.isEmpty())
			return null;
		Collections.sort(plans, new PlanComparator());
		return plans.get(0);
	}

}
