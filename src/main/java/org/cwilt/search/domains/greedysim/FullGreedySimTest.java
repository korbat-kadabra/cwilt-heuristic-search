package org.cwilt.search.domains.greedysim;
import org.junit.Test;

import org.cwilt.search.search.Limit;
import org.cwilt.search.algs.basic.bestfirst.Greedy;
public class FullGreedySimTest {
	@Test
	public void test(){
		
		int branching = 4;
		double dMean = 37;
		double hMean = dMean;
		double hdCorr = .7;
		double dStdev = 5;
		double hStdev = dStdev;
		
		FullGreedySim gs = new FullGreedySim(branching, hMean, dMean, hStdev, dStdev, hdCorr, 100);
		Greedy g = new Greedy(gs, new Limit());
		g.solve();
		g.printSearchData(System.out);
	}
}
