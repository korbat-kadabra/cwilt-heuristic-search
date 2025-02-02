package org.cwilt.search.domains.kiva_lite;
import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;
public class KivaLiteTest {

	
	@org.junit.Test
	public void doTest(){
		KivaLiteMap km = new KivaLiteMap(100, 100, 6, 10, KivaLiteMap.Heuristic.BACKWARDS, .10, .10);
		SearchAlgorithm a = new org.cwilt.search.algs.basic.bestfirst.WAStar(km, new Limit(), 2);
		a.solve();
		a.printSearchData(System.out);
	}
}
