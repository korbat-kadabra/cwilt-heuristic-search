package org.cwilt.search.domains.asteroids.planner;
import java.util.ArrayList;

import org.cwilt.search.domains.asteroids.AsteroidProblem;
public class RRTPlanner extends AsteroidPlanner{
	protected final int horizon;
	protected final int seed;
	protected final int treeSize;
	protected final boolean lazyNN;
	protected boolean stop;
	
	public RRTPlanner(AsteroidProblem a, int timeout, String[] args) {
		super(a, timeout);
		this.stop = false;
		int horizon = Integer.parseInt(args[2]);
		int treeSize = Integer.parseInt(args[3]);
		int seed = Integer.parseInt(args[5]);
		
		boolean lazyNN; 
		if(args[4].equals("true"))
			lazyNN = true;
		else if(args[4].equals("false"))
			lazyNN = false;
		else
			throw new RuntimeException("Invalid lazyNN selection");
		this.horizon = horizon;
		this.treeSize = treeSize;
		this.seed = seed;
		this.lazyNN = lazyNN;
		this.threadsPerRRT = Integer.parseInt(args[7]);
		this.rrtCount = Integer.parseInt(args[8]);
	}
	
	protected final int rrtCount;
	protected final int threadsPerRRT;
	public void stopPlanner(){
		stop = true;
	}
	
	@Override
	public AsteroidState nextState() {
		if(stop)
			return null;
		
		System.err.println("Current time is " + super.getCurrentState().getTime());
		RRT planTree;
		try {
			planTree = new SeededRRT(super.getCurrentState(), horizon, treeSize, lazyNN, seed, new ArrayList<AsteroidState>(), threadsPerRRT);
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
		return plan.get(plan.size() - 1);
	}
	
}
