package org.cwilt.search.domains.random_tree.richter;

import org.junit.Test;

import org.cwilt.search.search.Limit;
public class RandomTreeTest {
	
	private static final int LENGTH = 10;
	
	public double doRun(int maxOperator){
		double [] expansions = new double[LENGTH];
		
		for(int i = 0; i < LENGTH; i++){
			RandomTreeProblem p = new RandomTreeProblem(500, maxOperator, 1, .7, 3, i * 100);
			org.cwilt.search.search.SearchAlgorithm a = new org.cwilt.search.algs.basic.bestfirst.Greedy(p, new Limit());
			a.solve();
			expansions[i] = a.getLimit().getExpansions();
		}
		return org.cwilt.search.utils.basic.Stats.mean(expansions);
	}
	
	@Test
	public void test(){
		for(int i = 1; i < 15; i++){
			int max = (int) Math.pow(2, i);
			double result = doRun(max);
			System.err.printf("%d %f\n", max, result);
		}

	}
}
