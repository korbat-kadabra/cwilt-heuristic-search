package org.cwilt.search.domains.random_tree.wilt;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;

import org.cwilt.search.search.SearchState;
public class RandomTreeProblem implements org.cwilt.search.search.SearchProblem {
	public final double pCorrect;
	private final double opDiff;

	private final double initialH, initialHStar;
	public final double maxOp, minOp;

	public int branchingFactor;

	final Random r;

	public RandomTreeProblem(double pCorrect, double maxOp, int depth,
			double hAccuracy, int branchingFactor, int seed) {
		this.pCorrect = pCorrect;
		this.maxOp = maxOp;
		this.minOp = 1;
		this.opDiff = maxOp - minOp;
		assert (this.maxOp >= this.minOp);
		this.r = new Random(seed);
		this.branchingFactor = branchingFactor;
		this.initialHStar = maxOp * depth;
		this.initialH = initialHStar * hAccuracy;
	}

	public double nextOperatorCost() {
		return (minOp + (opDiff * r.nextDouble()));
	}

	@Override
	public SearchState getInitial() {
		return new RandomTreeNode(initialH, initialHStar, this);
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
	}

	@Override
	public void printProblemData(PrintStream ps) {
	}

}
