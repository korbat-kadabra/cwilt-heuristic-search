package org.cwilt.search.test.incremental;

import org.cwilt.search.algs.basic.bestfirst.AStar;
import org.cwilt.search.algs.basic.incremental.PartialExpansionBestFirstSearch;
import org.cwilt.search.domains.tiles.TileProblem;
import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchNode;
import org.junit.Test;

public class IncrementalAStarTest {

	public void test(int seed) {
		TileProblem tp = TileProblem.random(3, 3, seed, "unit");
		SearchAlgorithm incrementalAlg = new PartialExpansionBestFirstSearch(tp, new Limit(), new SearchNode.FGComparator());
		incrementalAlg.solve();
		SearchAlgorithm aStar = new AStar(tp, new Limit());
		aStar.solve();
		assert(aStar.getFinalCost() == incrementalAlg.getFinalCost());

//		aStar.printSearchData(System.out);
//		incrementalAlg.printSearchData(System.out);
	}
	
	@Test
	public void test() {
		for(int i = 0; i < 100; i++) {
			test(i);
		}
	}
}
