package org.cwilt.search.domains.asteroids.planner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import org.cwilt.search.domains.asteroids.AsteroidProblem;
public class VettingRRTPlanner extends RRTPlanner {

	private final Random r;
	
	public VettingRRTPlanner(AsteroidProblem a, int timeout, String[] args) {
		super(a, timeout, args);
		backupTreeSize = Integer.parseInt(args[6]);
		r = new Random(seed);
	}
	
	private final int backupTreeSize;
	
	private static final class AsteroidStateComparator implements Comparator<AsteroidState>{
		@Override
		public int compare(AsteroidState arg0, AsteroidState arg1) {
			double v0 = arg0.getValue();
			double v1 = arg1.getValue();
			
			assert(v0 < Double.MAX_VALUE);
			assert(v1 < Double.MAX_VALUE);
			
			if(v0 > v1)
				return -1;
			else if(v1 > v0)
				return 1;
			return 0;
		}
	}
	
	private final class ThreadedPlanner implements Runnable {
		private AsteroidState next;
		private final int threadID;
		
		public ThreadedPlanner (int threadID){
			this.threadID = threadID;
			assert(threadID > 0);
		}
		
		
		
		@Override
		public void run() {
			System.err.println("Current time is " + getCurrentState().getTime());
			if(getCurrentState().getTime() > 2000)
				return;
			if(getCurrentState().isTerminal())
				return;
			
			ArrayList<AsteroidState> children = getCurrentState().expandRaw();
			Collections.shuffle(children, r);
			Collections.sort(children, new AsteroidStateComparator());
			RRT planTree;
			
			for(int i = 0; i < Math.min(children.size(), 4); i++){
				AsteroidState s = children.get(i);
				if(s.isTerminal())
					continue;
				try {
					planTree = new SeededRRT(s, horizon, treeSize, lazyNN, seed * threadID, new ArrayList<AsteroidState>(), 1);
					planTree.buildRRT();
				} catch (CrashedShip e) {
					return;
				}
				ArrayList<AsteroidState> toReturn = planTree.bestActionHorizon();
				if(toReturn != null){
					System.err.println("accepting state " + i);
					next = s;
					return;
				} else {
					//System.err.println("rejecting state " + i);
				}
			}
			
			try {
				planTree = new SeededRRT(getCurrentState(), horizon, backupTreeSize, lazyNN, seed * threadID, new ArrayList<AsteroidState>(), 1);
				planTree.buildRRT();
			} catch (CrashedShip e) {
				return;
			}
			System.err.println("default");
			ArrayList<AsteroidState> plan = planTree.bestAction();
			if(plan == null)
				return;
			
			assert(plan.size() > 0);
			next = plan.get(plan.size() - 1);
		}
		public AsteroidState getNext(){
			return next;
		}
	}
	
	@Override
	public AsteroidState nextState() {
		if(stop)
			return null;
		
		ThreadedPlanner p = new ThreadedPlanner(1);
		p.run();
		
//		int thisCurrentTime = super.getCurrentState().getTime();
//		assert(currentPlan != null);
//		int planStartTime = currentPlan.get(currentPlan.size() - 1).getTime();
//		System.err.printf("%d %d\n", thisCurrentTime, planStartTime);
//		assert(thisCurrentTime == planStartTime - 1);
		return p.getNext();
	}
	
}
