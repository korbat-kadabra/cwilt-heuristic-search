package org.cwilt.search.domains.hanoi.pdb_builder.hill_climber;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.cwilt.search.domains.hanoi.HanoiProblem;
import org.cwilt.search.domains.hanoi.HanoiState;
import org.cwilt.search.domains.hanoi.pdb_builder.HanoiMappedPDBProblem;
import org.cwilt.search.domains.hanoi.pdb_builder.HanoiPDBMap;
import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchNodeDepth;import org.cwilt.search.utils.basic.MinHeap;
public class GlobalHillChecker {
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		HanoiProblem g = new HanoiProblem(4, 10, new double[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, new String[] {});

		SearchNodeDepth n = SearchNodeDepth.makeInitial(g.getInitial());
		MinHeap<SearchNode> open = new MinHeap<SearchNode>(new SearchNode.GComparator());
		open.add(n);
		HashMap<Object, SearchNode> closed = new HashMap<Object, SearchNode>();
		
		int exp = 0;
		while (!open.isEmpty()) {
			exp ++;
			if(exp % 10000 == 0){
				System.err.println(exp);
			}
			SearchNode next = open.poll();
			SearchNode incumbent = closed.get(next.getState().getKey());
			if (incumbent != null) {
				// uniform cost search means no expansions out of order
				continue;
			} else {
				closed.put(next.getState().getKey(), next);
				for (SearchNode nextNode : next.expand()) {
					open.add(nextNode);
				}
			}
		}

		// calculate a global tau
		
		System.err.println("done");
		
		double[] h2 = new double[closed.size()];
		double[] dStar = new double[closed.size()];


		HanoiPDBMap map2 = new HanoiPDBMap(new int[]{0,1,2,4,5,6,7,8}, 10);
		List<HanoiPDBMap> children = map2.generateNeighbors();

		
		for(HanoiPDBMap map : children){
			HanoiMappedPDBProblem p2 = new HanoiMappedPDBProblem("hanoidata/12_disks/1", "unit", HillClimbingNode.pdb,
					map.getPDBMap());
			int i = 0;
			for (Map.Entry<Object, SearchNode> e : closed.entrySet()) {
				h2[i] = p2.calculateH((HanoiState) e.getValue().getState());
				dStar[i] = e.getValue().getG();
				i++;
			}
			KendallsCorrelation k = new KendallsCorrelation();
			System.err.println(map.toString() + "\t" + k.correlation(h2, dStar));
			
		}

		

	}
}
