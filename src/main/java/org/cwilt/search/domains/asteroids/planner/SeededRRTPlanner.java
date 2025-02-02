package org.cwilt.search.domains.asteroids.planner;
import java.util.ArrayList;

import org.cwilt.search.domains.asteroids.AsteroidProblem;
public class SeededRRTPlanner extends RRTPlanner {
	ArrayList<AsteroidState> currentPlan;
	public SeededRRTPlanner(AsteroidProblem a, int timeout, String[] args) {
		super(a, timeout, args);
		currentPlan = new ArrayList<AsteroidState>();
	}

	@Override
	public AsteroidState nextState() {
		if(stop)
			return null;
		
		System.err.println("Current time is " + super.getCurrentState().getTime());
		System.err.println("Current path is " + currentPlan.size() + " nodes long");
		RRT planTree;
		try {
			planTree = new SeededRRT(super.getCurrentState(), horizon, treeSize, lazyNN, seed, currentPlan, super.threadsPerRRT);
			planTree.buildRRT();
		} catch (CrashedShip e) {
			return null;
		}
		if(super.getCurrentState().getTime() > 2000)
			return null;
		//String path = "/home/aifs2/cmo66/rrt/" + super.getCurrentState().getTime() + ".png";
		//planTree.drawTree(path);
		
		if(planTree.bestAction() == null)
			return null;
		ArrayList<AsteroidState> plan = planTree.bestAction();
		currentPlan = plan;
		return plan.get(plan.size() - 1);
	}

}
