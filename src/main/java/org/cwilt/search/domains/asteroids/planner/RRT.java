package org.cwilt.search.domains.asteroids.planner;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;

import org.cwilt.search.domains.asteroids.AsteroidProblem;
import org.cwilt.search.domains.asteroids.Ship;
import org.cwilt.search.domains.asteroids.planner.AsteroidPlanner.CrashedShip;
public class RRT {
	protected final int endTime;
	protected final LazyKDTree tree;

	protected final Random r;
	protected final AsteroidState root;
	protected final boolean lazyNN;
	protected final int horizon;
	protected final ArrayList<AsteroidState> seedPlan;

	public RRT(AsteroidState root, int horizon, int treeSize, boolean lazyNN,
			int seed, ArrayList<AsteroidState> seedPlan, int nThreads) throws CrashedShip {
		this.endTime = root.getTime() + horizon;
		this.nThreads = nThreads;
		this.seedPlan = seedPlan;
		this.horizon = horizon;
		this.tree = new LazyKDTree(root, endTime,
				new LazyKDTree.XYThetaBucketer());
		this.root = root;
		this.lazyNN = lazyNN;
		this.tree.add(root);
		if(seed == 0)
			throw new IllegalArgumentException("Seed to an RRT cannot be 0");
		this.r = new Random(seed);
		this.treeSize = treeSize;
		assert(seedPlan != null);
		
		if(seedPlan.contains(root)){
			//System.err.println("using a seeded RRT");
			for(AsteroidState s : seedPlan){
				tree.add(s);
			}
		}
	}

	protected final int treeSize;

	public void buildRRT() throws CrashedShip {
		
		for (int i = 0; i < treeSize; i++) {
			// System.err.println(i + " " + root.getTime());
			double xSample = r.nextDouble() * AsteroidProblem.X_SIZE;
			double ySample = r.nextDouble() * AsteroidProblem.Y_SIZE;
			double thetaSample = r.nextDouble() * Math.PI * 2;
			int time = r.nextInt(horizon);
			AsteroidState dummy = AsteroidState.dummyState(xSample, ySample,
					thetaSample, time);
			AsteroidState next;
			if (lazyNN)
				next = tree.approxNearestNeighbor(dummy, this);
			else
				next = tree.nearestNeighbor(dummy, this);
			assert (next != null);
			ArrayList<AsteroidState> children = next.expandRaw();
			if (children.isEmpty())
				continue;
			AsteroidState newNode = findNearestNode(children, dummy);
			if (newNode != null)
				tree.add(newNode);
		}
	}

	protected AsteroidState findNearestNode(ArrayList<AsteroidState> nodes,
			AsteroidState goal) {
		double minDistance = Double.MAX_VALUE;
		AsteroidState incumbent = null;

		for (AsteroidState s : nodes) {
			assert (s != null);
			if (s.isTerminal())
				continue;
			double distance = rrtDistance(s, goal);
			if (distance < minDistance) {
				minDistance = distance;
				incumbent = s;
			}
		}
		return incumbent;
	}

	public double rrtDistance(AsteroidState s1, AsteroidState s2) {
		Ship ship1 = s1.getShip();
		Ship ship2 = s2.getShip();
		double dx = ship1.x - ship2.x;
		double dy = ship1.y - ship2.y;
		double dt = ship1.theta - ship2.theta;
		dt = dt * 100;
		return Math.sqrt(dx * dx + dy * dy + dt * dt);
	}

	public ArrayList<AsteroidState> bestAction() {
		AsteroidState best = tree.findBest();
		return findAncestor(best);
	}
	
	protected final int nThreads;
	public ArrayList<AsteroidState> bestActionHorizon() {
		AsteroidState best = tree.findBestHorizon();
		if (best == null)
			return null;
		return findAncestor(best);
	}

	private ArrayList<AsteroidState> findAncestor(AsteroidState s) {
		ArrayList<AsteroidState> plan = new ArrayList<AsteroidState>(horizon);
		if (s == root)
			return null;
		assert (s != root);
		assert (s != null);
		while (!s.getParent().equals(root)) {
			plan.add(s);
			s = s.getParent();
			assert (s != null);
		}
		plan.add(s);
		assert (plan.size() > 0);
		return plan;
	}

	public void drawTree(String outputName) {
		BufferedImage bi = new BufferedImage(AsteroidProblem.X_SIZE,
				AsteroidProblem.Y_SIZE, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = bi.createGraphics();
		tree.draw(g2, horizon);
		File outputfile = new File(outputName);
		try {
			ImageIO.write(bi, "png", outputfile);
		} catch (IOException e) {
			System.err.println("Image IO Exception of some kind occurred:");
			e.printStackTrace();
		}
	}
}
