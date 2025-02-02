package org.cwilt.search.domains.hanoi.pdb_builder.hill_climber;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.stat.correlation.KendallsCorrelation;

import org.cwilt.search.search.Limit;import org.cwilt.search.algs.basic.bestfirst.Greedy;
import org.cwilt.search.domains.hanoi.HanoiPDB;
import org.cwilt.search.domains.hanoi.HanoiProblem;
import org.cwilt.search.domains.hanoi.HanoiState;
import org.cwilt.search.domains.hanoi.pdb_builder.HanoiMappedPDBProblem;
import org.cwilt.search.domains.hanoi.pdb_builder.HanoiPDBMap;
public class HillClimberRandom implements Comparable<HillClimberRandom> {
	private final HanoiPDBMap map;
	private final double tau;

	public double getH(HanoiState hs) {
		HanoiMappedPDBProblem p = new HanoiMappedPDBProblem(rootProblem, hs, map.getPDBMap());
		return p.calculateH(p.getInitial());

	}

	private static HanoiProblem rootProblem;

	private double calculateTau() {
		KendallsCorrelation k = new KendallsCorrelation();
		double[] h = new double[dStarValues.length];
		for (int i = 0; i < dStarValues.length; i++) {
			h[i] = getH(states[i]);
		}
		
		return k.correlation(dStarValues, h);
	}

	public static HanoiPDB fullPDB;

	private static double[] dStarValues;
	private static HanoiState[] states;
	private static final int NODE_COUNT = 100000;

	static {
		dStarValues = new double[NODE_COUNT];
		states = new HanoiState[NODE_COUNT];


		try {
			HanoiPDB pdb = (HanoiPDB) HanoiPDB.readPDB("8_disk_hanoi_pdb");
			rootProblem = new HanoiProblem(4, 12, new double[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new HanoiPDB[] { pdb });

			fullPDB = (HanoiPDB) HanoiPDB.readPDB("12_disk_pdb");
//			fullPDB = (HanoiPDB) HanoiPDB.readPDB("8_disk_hanoi_pdb");

		
			HanoiProblem p = new HanoiProblem(4, 12, new double[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
					new HanoiPDB[] { fullPDB });
			
			System.err.println("loaded full problem");
			for (int i = 0; i < NODE_COUNT; i++) {
				HanoiState hs = HanoiState.randomState(p, i);
				dStarValues[i] = hs.d();
				states[i] = hs;
			}

		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private HillClimberRandom(HanoiPDBMap m) {
		this.map = m;
		this.tau = calculateTau();
	}
	
	private double calculateExpansions(){
		double totalExpansions = 0;
		double count = 0;
		
		for(int i = 0; i <= 50; i++){
			HanoiState hs = HanoiState.randomState(rootProblem, i);
			HanoiMappedPDBProblem p = new HanoiMappedPDBProblem(rootProblem, hs, map.getPDBMap());
			HanoiState hs2 = HanoiState.randomState(p, i);
			p = new HanoiMappedPDBProblem(rootProblem, hs2, map.getPDBMap());
			
			Greedy g = new Greedy(p, new Limit());
			g.solve();
			count ++;
			totalExpansions += g.getLimit().getExpansions();
		}
		
		return totalExpansions / count;
	}
	
	private HillClimberRandom(int seed) {
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

	private HillClimberRandom(int[] seed) {
		Arrays.sort(seed);
		this.map = new HanoiPDBMap(seed, 12);
		this.tau = calculateTau();
	}

	public List<HillClimberRandom> generateNeighbors() {
		List<HillClimberRandom> neighbors = new ArrayList<HillClimberRandom>();

		for (HanoiPDBMap n : map.generateNeighbors()) {
			neighbors.add(new HillClimberRandom(n));
		}

		Collections.sort(neighbors);
		return neighbors;
	}

	public static void hillClimb(){
		HillClimberRandom incumbent = new HillClimberRandom(5);

		System.err.println("starting");
		while (true) {
			List<HillClimberRandom> children = incumbent.generateNeighbors();
			System.err.println(incumbent);
			for (HillClimberRandom n : children) {
				System.err.println("\t" + n);
			}

			HillClimberRandom bestChild = children.get(0);
			if (bestChild.tau > incumbent.tau) {
				System.err.println("New best tau is " + bestChild.tau);
				incumbent = bestChild;
			} else {
				break;
			}
		}
		System.err.println(incumbent);
		System.err.println(new HillClimberRandom(new int[] { 0, 1, 2, 3, 4, 5, 6, 7 }));

	}
	
	public static void main(String[] args) {
		for(int i = 0; i < 200; i++){
			HillClimberRandom incumbent = new HillClimberRandom(5 * i);
			System.err.println(incumbent);
			System.err.println(incumbent.calculateExpansions());
		}

		HillClimberRandom hcr = new HillClimberRandom(new int[] { 0, 1, 2, 3, 4, 5, 6, 7 });
		System.err.println(hcr);
		System.err.println(hcr.calculateExpansions());

	}

	@Override
	public String toString() {
		return "HillClimbingNode [map=" + map + ", tau=" + tau + "]";
	}

	@Override
	public int compareTo(HillClimberRandom o) {
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
