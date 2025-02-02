package org.cwilt.search.domains.random_tree.wilt;
import org.junit.Test;

import org.cwilt.search.search.Limit;
public class RandomTreeTest {
	private static final int LENGTH = 10;

	public static double doRun(double pCorrect, double maxOp, int depth,
			double hAccuracy, int branchingFactor) {
		double[] expansions = new double[LENGTH];

		for (int i = 0; i < LENGTH; i++) {
			RandomTreeProblem p = new RandomTreeProblem(pCorrect, maxOp, depth,
					hAccuracy, branchingFactor, i * 222);
			org.cwilt.search.search.SearchAlgorithm a = new org.cwilt.search.algs.basic.bestfirst.Greedy(p,
					new Limit(10000, Long.MAX_VALUE, Long.MAX_VALUE, false));
			a.solve();

			expansions[i] = a.getLimit().getExpansions();
		}
		return org.cwilt.search.utils.basic.Stats.mean(expansions);
	}

	@Test
	public void singleTest() {
		double maxCost = 2;
		int depth = 50;
		RandomTreeProblem p = new RandomTreeProblem(0.80, maxCost, depth, 0.90, 2, 10);
		org.cwilt.search.search.SearchAlgorithm a = new org.cwilt.search.algs.basic.bestfirst.Greedy(p,
				new Limit());
		a.solve();
		a.printSearchData(System.err);
		System.err.println(a.getLimit().getExpansions());
	}

	public static void main(String[] args){
		double pCorrect = Double.parseDouble(args[0]);
		double maxOp = Double.parseDouble(args[1]);
		int depth = Integer.parseInt(args[2]);
		double hAccuracy = Double.parseDouble(args[3]);
		int branchingFactor = Integer.parseInt(args[4]);
		double expansions = doRun(pCorrect, maxOp, depth, hAccuracy, branchingFactor);
		System.out.println(expansions);
	}
	
}
