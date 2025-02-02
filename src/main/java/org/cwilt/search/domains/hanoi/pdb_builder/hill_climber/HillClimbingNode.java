package org.cwilt.search.domains.hanoi.pdb_builder.hill_climber;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.cwilt.search.domains.hanoi.HanoiPDB;
import org.cwilt.search.domains.hanoi.HanoiProblem;
import org.cwilt.search.domains.hanoi.pdb_builder.HanoiMappedPDBProblem;
import org.cwilt.search.domains.hanoi.pdb_builder.HanoiPDBMap;
import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchNodeDepth;import org.cwilt.search.utils.basic.MinHeap;
public class HillClimbingNode implements Comparable<HillClimbingNode> {
	private final HanoiPDBMap map;
	private final double tau;

	private static double[] randomDStarValues = new double[] { 61, 53, 51, 63, 63, 61, 62, 59, 60, 62, 31, 71, 60, 66,
			62, 54, 56, 60, 53, 47, 58, 67, 61, 62, 67, 65, 43, 71, 72, 58, 68, 64, 66, 51, 61, 52, 62, 70, 54, 65, 54,
			62, 59, 66, 70, 52, 66, 61, 64, 71, 56, };
	
	public double getH(String problemName) throws IOException{
		HanoiMappedPDBProblem p = new HanoiMappedPDBProblem(problemName, "unit", pdb, map.getPDBMap());
		return p.calculateH(p.getInitial());
		
	}
	
	private double calculateTau() {
		KendallsCorrelation k = new KendallsCorrelation();
		double[] h = new double[dStarValues.length];
		for (int i = 0; i <= 50; i++) {
			String problemName = "hanoidata/12_disks/" + Integer.toString(i);
			try {
				h[i] = getH(problemName);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
//		try {
//			HanoiMappedPDBProblem p = new HanoiMappedPDBProblem("hanoidata/12_disks/1", "unit", pdb, map.getPDBMap());
//			for(int i = 51; i < 102; i++){
//				double hxx = p.calculateH((HanoiState) nodesNearGoal.get(i - 51).getState());
//				h[i] = hxx;
//			}
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
		
		return k.correlation(dStarValues, h);
	}

	public static HanoiPDB pdb;

	private static double[] dStarValues;
	private static List<SearchNode> nodesNearGoal = new ArrayList<SearchNode>(51);

	static {
		int COUNT = 0;
		// search backwards from the goal to get some more nodes to include in
		// the analysis
		dStarValues = new double[51 + COUNT];
		for (int i = 0; i < randomDStarValues.length; i++) {
			dStarValues[i] = randomDStarValues[i];
		}
		try {
			HanoiProblem g = new HanoiProblem(4, 12, new double[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new String[] {});

			SearchNodeDepth n = SearchNodeDepth.makeInitial(g.getInitial());
			MinHeap<SearchNode> open = new MinHeap<SearchNode>(new SearchNode.GComparator());
			open.add(n);
			HashMap<Object, SearchNode> closed = new HashMap<Object, SearchNode>();
			for(int i = 0; i < 80; i++){
				SearchNode next = open.poll();
				SearchNode incumbent = closed.get(next.getState().getKey());
				if(incumbent != null){
					// uniform cost search means no expansions out of order
					continue;
				}
				else {
					closed.put(next.getState().getKey(),  next);
					for(SearchNode nextNode : next.expand()){
						open.add(nextNode);
					}
				}
			}
			int index = randomDStarValues.length;
			Iterator<Map.Entry<Object, SearchNode>> iter = closed.entrySet().iterator();
			while(index < dStarValues.length){
				Map.Entry<Object, SearchNode> next = iter.next();
				nodesNearGoal.add(next.getValue());
				dStarValues[index] = next.getValue().getG();
				index ++;
			}
			
		} catch (ClassNotFoundException e1) {
			throw new RuntimeException(e1);
		}

		try {
			pdb = (HanoiPDB) HanoiPDB.readPDB("8_disk_hanoi_pdb");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private HillClimbingNode(HanoiPDBMap m) {
		this.map = m;
		this.tau = calculateTau();
	}

	private HillClimbingNode(int seed) {
		int[] disks = new int[8];
		// have to choose 8 random numbers between 0 and 11, all different
		ArrayList<Integer> choices = new ArrayList<Integer>(12);
		for (int i = 0; i < 12; i++) {
			choices.add(i);
		}
		Collections.shuffle(choices);
		for (int i = 0; i < 8; i++) {
			disks[i] = choices.get(i);
		}
		Arrays.sort(disks);
		this.map = new HanoiPDBMap(disks, 12);
		this.tau = calculateTau();
	}
	
	private HillClimbingNode(int[] seed) {
		Arrays.sort(seed);
		this.map = new HanoiPDBMap(seed, 12);
		this.tau = calculateTau();
	}

	public List<HillClimbingNode> generateNeighbors() {
		List<HillClimbingNode> neighbors = new ArrayList<HillClimbingNode>();

		for (HanoiPDBMap n : map.generateNeighbors()) {
			neighbors.add(new HillClimbingNode(n));
		}

		Collections.sort(neighbors);
		return neighbors;
	}

	public static void main(String[] args) {
		HillClimbingNode incumbent = new HillClimbingNode(5);

		while (true) {
			List<HillClimbingNode> children = incumbent.generateNeighbors();
			System.err.println(incumbent);
			for(HillClimbingNode n : children){
				System.err.println("\t" + n);
			}
			
			HillClimbingNode bestChild = children.get(0);
			if (bestChild.tau > incumbent.tau) {
				System.err.println("New best tau is " + bestChild.tau);
				incumbent = bestChild;
			} else {
				break;
			}
		}
		System.err.println(incumbent);
		System.err.println(new HillClimbingNode(new int[]{0,1,2,3,4,5,6,7}));
	}

	@Override
	public String toString() {
		return "HillClimbingNode [map=" + map + ", tau=" + tau + "]";
	}

	@Override
	public int compareTo(HillClimbingNode o) {
		if (this.tau > o.tau) {
			return -1;
		} else if (this.tau < o.tau) {
			return 1;
		} else {
			int h1 = this.hashCode();
			int h2 = o.hashCode();
			return h1 - h2;
		}
	}

}
