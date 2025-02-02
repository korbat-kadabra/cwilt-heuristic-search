package org.cwilt.search.domains.asteroids.planner;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.cwilt.search.domains.asteroids.AsteroidProblem;
public class MultithreadedVettingRRTPlanner extends AsteroidPlanner {
	private boolean foundSolution = false;
	private final VettingRRTPlannerThread[] planners;
	private final int nThreads;

	public MultithreadedVettingRRTPlanner(AsteroidProblem a, int timeout,
			String[] args) {
		super(a, timeout);
		nThreads = Integer.parseInt(args[8]);
		this.planners = new VettingRRTPlannerThread[nThreads];

		for (int i = 0; i < nThreads; i++) {
			planners[i] = new VettingRRTPlannerThread(args, i + 1);
		}

	}

	private int failures;

	private final synchronized void incrementFailures() {
		failures++;
	}

	private final synchronized void setPlan(ArrayList<AsteroidState> plan) {
		this.foundSolution = true;
		for(VettingRRTPlannerThread p : planners){
			p.stopPlanner();
		}
		super.path.addAll(plan);

	}

	private final class VettingRRTPlannerThread implements Runnable {
		int attemptNumber = 1;
		private final int threadID;

		private final String[] args;

		public VettingRRTPlannerThread(String[] args, int threadID) {
			this.args = args.clone();
			this.threadID = threadID;
			assert (threadID > 0);
		}
		public void stopPlanner(){
			synchronized(this){
				planner.stopPlanner();
			}
		}
		
		private VettingRRTPlanner planner;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (!foundSolution) {
				int oldSeed = Integer.parseInt(args[5]);
				args[5] = Integer.toString(oldSeed * threadID * attemptNumber);
				synchronized (this) {
					if(planner != null && planner.stop)
						return;
					
					planner = new VettingRRTPlanner(a, timeout, args);
				}

				try {
					planner.solve();
				} catch (SolverTimeout e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CrashedShip e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				ArrayList<AsteroidState> p = planner.getPlan();
				if (p.get(p.size() - 1).isGoal()) {
					// found the goal, ready to terminate
					setPlan(p);
				} else {
					incrementFailures();
					attemptNumber++;
				}
			}
		}

	}

	@Override
	public AsteroidState nextState() {
		throw new RuntimeException(
				"This doesn't know how to get the next state");
	}

	@Override
	public void solve() {
		ExecutorService executor = Executors.newFixedThreadPool(nThreads);
		for (int i = 0; i < nThreads; i++) {
			executor.execute(planners[i]);
		}
		executor.shutdown();
		try {
			executor.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("had to try " + failures + " times");

	}

}
