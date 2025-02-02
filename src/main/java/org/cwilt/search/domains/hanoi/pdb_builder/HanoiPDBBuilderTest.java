package org.cwilt.search.domains.hanoi.pdb_builder;
import java.io.IOException;

import org.cwilt.search.domains.hanoi.HanoiPDB;
import org.junit.Test;
public class HanoiPDBBuilderTest {
	@Test
	public void testLoading() throws ClassNotFoundException, IOException{
		int[] keptDisks = new int[]{0,1,2,3,4,5,6,7};
		double[] costs = new double[]{1,1,1,1,1,1,1,1,1,1,1,1};
		HanoiPDB pdb = (HanoiPDB) HanoiPDB.readPDB("8_disk_hanoi_pdb");
		assert(costs.length == 12);
		String path = "hanoidata/10_0";
		HanoiMappedPDBProblem problem = new HanoiMappedPDBProblem(path, "unit", pdb, keptDisks);
		double initialH = problem.calculateH(problem.getInitial());
		assert(33 == initialH);
	}

}
