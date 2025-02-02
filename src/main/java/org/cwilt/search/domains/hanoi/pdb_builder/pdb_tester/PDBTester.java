package org.cwilt.search.domains.hanoi.pdb_builder.pdb_tester;
import java.io.IOException;

import org.cwilt.search.algs.basic.bestfirst.Greedy;
import org.cwilt.search.domains.hanoi.HanoiPDB;
import org.cwilt.search.domains.hanoi.HanoiProblem;
import org.cwilt.search.domains.hanoi.pdb_builder.HanoiMappedPDBProblem;
import org.cwilt.search.search.Limit;
public class PDBTester {
	public static int[] maxTauPDB = new int[] { 0, 1, 2, 4, 5, 6, 7, 8 };
	public static int[] bottomPDB = new int[] { 0, 1, 2, 3, 4, 5, 6, 7 };

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		long maxTauExp = 0;
		long bottomExp = 0;

		for (int i = 0; i <= 50; i++) {
			HanoiPDB pdb = (HanoiPDB) HanoiPDB.readPDB("8_disk_hanoi_pdb");
			{
				HanoiProblem problem = new HanoiMappedPDBProblem("hanoidata/12_disks/" + Integer.toString(i), "unit",
						pdb, bottomPDB);
				Greedy g = new Greedy(problem, new Limit());
				g.solve();
				bottomExp += g.getLimit().getExpansions();
			}
			{
				HanoiProblem problem = new HanoiMappedPDBProblem("hanoidata/12_disks/" + Integer.toString(i), "unit",
						pdb, maxTauPDB);
				Greedy g = new Greedy(problem, new Limit());
				g.solve();
				maxTauExp += g.getLimit().getExpansions();
			}
		}
		System.err.println("Average for max Tau was " + (maxTauExp / 51.0d));
		System.err.println("Average for bottom was " + (bottomExp / 51.0d));
	}
}
