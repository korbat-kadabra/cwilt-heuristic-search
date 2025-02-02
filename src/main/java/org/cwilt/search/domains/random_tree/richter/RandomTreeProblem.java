package org.cwilt.search.domains.random_tree.richter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;

import org.cwilt.search.search.SearchState;
/**
 * 
 * Random Tree based upon the random tree model presented in
 * "The Joy of Forgetting: Faster Anytime Search via Restarting" by Silvia
 * Richter et al.
 * 
 * @author cmo66
 * 
 */

public class RandomTreeProblem implements org.cwilt.search.search.SearchProblem {
	public final double rootAGD;
	public final double maxEdgeCost;
	public final double minEdgeCost;
	public final double hErrorFactor;
	public final int branchingFactor;
	public final Random r;

	public RandomTreeProblem(double rootAGD, double maxEdgeCost,
			double minEdgeCost, double hErr, int bf, int seed) {
		this.rootAGD = rootAGD;
		this.maxEdgeCost = maxEdgeCost;
		this.minEdgeCost = minEdgeCost;
		this.hErrorFactor = hErr;
		this.branchingFactor = bf;
		this.r = new Random(seed);
	}

	@Override
	public SearchState getInitial() {
		return new RandomTreeNode(this);
	}

	@Override
	public SearchState getGoal() {
		throw new org.cwilt.search.search.NoCanonicalGoal();
	}

	@Override
	public ArrayList<SearchState> getGoals() {
		throw new org.cwilt.search.search.NoCanonicalGoal();
	}

	@Override
	public void setCalculateD() {
		// TODO Auto-generated method stub

	}

	@Override
	public void printProblemData(PrintStream ps) {
	}

}
