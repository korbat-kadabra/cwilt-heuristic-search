package org.cwilt.search.domains.hanoi;
import java.io.IOException;

import org.cwilt.search.utils.TemporaryLoadAndWritePath;
import org.junit.Test;
public class DisjointPDBTest {

	@Test
	public void disjointTest() throws ClassNotFoundException, IOException{
		double[] costs = {49,36,25,16,9,4,1};
		int[] sizes = {4,3};
		boolean[] skipped = {false, false};
		DisjointHanoiPDB p = new DisjointHanoiPDB(costs, sizes, skipped);
		System.err.println(p);

		String[] pdbString = new String[0];
		HanoiProblem problem = new HanoiProblem(TemporaryLoadAndWritePath.getTempPath()
				+ "/hanoidata/1", "square", pdbString);
		System.err.println(problem);
		System.err.println(p.getH(problem.getInitial()));
		System.err.println(p.getD(problem.getInitial()));
	}

}
