package org.cwilt.search.domains.hanoi.pdb_builder;
import java.io.IOException;

import org.cwilt.search.algs.basic.bestfirst.Greedy;
import org.cwilt.search.domains.hanoi.HanoiPDB;
import org.cwilt.search.search.Limit;
public class GreedyTest {
	private static HanoiPDB pdb;
	
	static{
		try {
			pdb = (HanoiPDB) HanoiPDB.readPDB("8_disk_hanoi_pdb");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static double getAverageExpansions(int[] map) throws IOException{
		double totalExpansions = 0;
		for(int i = 0; i <= 50; i++){
			HanoiMappedPDBProblem p = new HanoiMappedPDBProblem("hanoidata/12_disks/" +Integer.toString(i), "unit", pdb, map);
			Limit l = new Limit();
			Greedy g = new Greedy(p, l);
			g.solve();
			totalExpansions += g.getLimit().getExpansions();
		}
		return totalExpansions / 51.0d;
	}
	
	public static void main(String[] args) throws IOException {
		int[] map = new int[]{0,1,2,3,4,5,6,7};
		System.err.println(getAverageExpansions(map));
		int[] map2 = new int[]{0, 1, 2, 4, 5, 9, 10, 11};
		System.err.println(getAverageExpansions(map2));
	}
}
