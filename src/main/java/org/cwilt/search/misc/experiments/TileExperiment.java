package org.cwilt.search.misc.experiments;
import org.junit.Test;

import org.cwilt.search.algs.experimental.*;
import org.cwilt.search.domains.tiles.TileProblem;
import org.cwilt.search.algs.basic.bestfirst.*;import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;
public class TileExperiment {

	private SearchAlgorithm getAlg(int i, TileProblem p) {
		if(i == 0)
			return new AStar(p, new Limit());
		else if(i == 1)
			return new Greedy(p, new Limit());
		else if(i == 2)
			return new DoubleQueueSearch(p, new Limit(), 1.42);
		else{
			double id = i - 2;
			return new WAStar(p, new Limit(), 1 + (id/10));
		}
	}

	@Test
	public void tileTest() {
		int n = 100;
		for (int testID = 0; testID < 200; testID++) {
			double[] costs = new double[n];
			double[] exps = new double[n];
			for (int i = 0; i < n; i++) {
				TileProblem p = TileProblem.random(3, 3, i * 1000, "inverse");
				SearchAlgorithm alg = getAlg(testID, p);
				alg.solve();
				if(alg.getIncumbent() == null)
				{
					System.err.println(testID + " failed");
				}
				assert(alg.getIncumbent() != null);
				costs[i] = alg.getIncumbent().getCost();
				exps[i] = alg.getIncumbent().getExp();
			}
			TileProblem p = TileProblem.random(3, 3, 0, "unit");
			System.err.print(getAlg(testID, p) + "\t");
			System.err.print(org.cwilt.search.utils.basic.Stats.mean(costs));
			System.err.println("\t" + org.cwilt.search.utils.basic.Stats.mean(exps));
		}
	}
}