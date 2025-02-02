package org.cwilt.search.domains.greedysim;
import java.util.ArrayList;
import java.util.Random;

import org.cwilt.search.utils.basic.CorrelatedData;import org.cwilt.search.utils.basic.Stats;
public class GreedySim {
	private final double[] costs;
	private final int[] branching;
	private final int[][][] counts;
	private final int[][][] maxes;

	private final int maxBinSize;

	private final Random r;

	private final double hh, hd;
	private final double stdev;

	private final int dStarRange;
	private final double hBucketSize;

	private final Location start;
	private Location current;

	private static class Location {
		public final int dStarBucket;
		public final int hStarBucket;
		public final int hBucket;

		public Location(int d, int hs, int h) {
			this.dStarBucket = d;
			this.hStarBucket = hs;
			this.hBucket = h;
		}

		public boolean isGoal() {
			return hStarBucket == 0;
		}

		public String toString() {
			StringBuffer b = new StringBuffer();

			b.append("d*: ");
			b.append(dStarBucket);
			b.append(" h*: ");
			b.append(hStarBucket);
			b.append(" h: ");
			b.append(hBucket);

			return b.toString();
		}
	}

	private final CorrelatedData hhGen;
	private final CorrelatedData hdGen;

	public GreedySim(int seed, int dStarRange, double[] costs, int[] branching,
			double diameter, double hd, double hh, double stdev, int maxBinSize) {
		this.maxBinSize = maxBinSize;
		this.hh = hh;
		this.hd = hd;
		this.stdev = stdev;
		this.costs = costs;
		this.branching = branching;
		this.expandedAll = false;
		this.r = new Random(seed);
		double hStarRange = (diameter / getMinCost());

		this.hBucketSize = hStarRange / dStarRange;

		for (double cost : this.costs) {
			if (cost < hBucketSize) {
				System.out.println("Error: Invalid bucket size " + hBucketSize
						+ " with cost " + cost);
				throw new IllegalArgumentException();
			}
		}

		this.dStarRange = dStarRange;
		counts = new int[dStarRange][dStarRange][dStarRange];
		maxes = new int[dStarRange][dStarRange][dStarRange];

		double hMean = hBucketSize * dStarRange / 2;

		this.hhGen = new CorrelatedData(seed, hMean, hStarRange
				/ (this.stdev * 2), hMean, hStarRange / (this.stdev * 2),
				this.hh);
		this.hdGen = new CorrelatedData(seed, hMean, hStarRange
				/ (this.stdev * 2), dStarRange / 2, dStarRange
				/ (this.stdev * 2), this.hd);

		this.start = new Location(r.nextInt(dStarRange), r.nextInt(dStarRange),
				r.nextInt(dStarRange));
		this.current = this.start;
		addLocation(start);
	}

	public boolean isValid(Location l) {
		if (counts[l.dStarBucket][l.hStarBucket][l.hBucket] <= 0)
			return false;
		else
			return true;
	}

	public boolean rangeCheck(Location l) {
		if (l.dStarBucket < 0)
			return false;
		if (l.hStarBucket < 0)
			return false;
		if (l.hBucket < 0)
			return false;
		if (l.dStarBucket >= dStarRange)
			return false;
		if (l.hStarBucket >= dStarRange)
			return false;
		if (l.hBucket >= dStarRange)
			return false;
		return true;
	}

	private boolean canUse(Location l) {
		assert (rangeCheck(l));
		int ct = maxes[l.dStarBucket][l.hStarBucket][l.hBucket];
		return ct < maxBinSize;
	}

	public Location getNearest(Location l, int dist) {
		ArrayList<Location> choices = new ArrayList<Location>();
		for (int i = -dist; i <= dist; i++) {
			for (int j = -dist; j <= dist; j++) {
				for (int k = -dist; k <= dist; k++) {
					if (Math.abs(i) + Math.abs(j) + Math.abs(k) != dist)
						continue;
					Location here = new Location(l.dStarBucket + i,
							l.hStarBucket + j, l.hBucket + k);
					if (!rangeCheck(here))
						continue;
					if (canUse(here)) {
						choices.add(here);
					}
				}
			}
		}
		if (choices.size() == 0)
			return null;
		else
			return choices.get(r.nextInt(choices.size()));
	}

	private boolean expandedAll;

	public void addLocation(Location l) {
		if (expandedAll)
			return;
		else if (maxes[l.dStarBucket][l.hStarBucket][l.hBucket] < maxBinSize) {
			counts[l.dStarBucket][l.hStarBucket][l.hBucket]++;
			maxes[l.dStarBucket][l.hStarBucket][l.hBucket]++;

			if (l.hBucket < current.hBucket)
				current = l;
		} else {
			Location nearest = null;
			for (int i = 1; i < dStarRange * 3 && nearest == null; i++) {
				nearest = getNearest(l, i);
			}
			if (nearest == null) {
				for (int i = 1; i < dStarRange * 3 && nearest == null; i++) {
					nearest = getNearest(l, i);
				}
				assert (nearest == null);

				getNearest(l, dStarRange * 3);
				expandedAll = true;
				return;
			}
			counts[nearest.dStarBucket][nearest.hStarBucket][nearest.hBucket]++;
			maxes[nearest.dStarBucket][nearest.hStarBucket][nearest.hBucket]++;

			if (l.hBucket < nearest.hBucket)
				current = nearest;
		}
	}

	public Location searchHBin(int hBin) {
		ArrayList<Location> valid = new ArrayList<Location>();
		for (int i = 0; i < dStarRange; i++) {
			for (int j = 0; j < dStarRange; j++) {
				Location here = new Location(i, j, hBin);
				if (isValid(here)) {
					valid.add(here);
				}
			}
		}
		if (valid.size() == 0)
			return null;
		else {
			return valid.get(r.nextInt(valid.size()));
		}
	}

	public void globalSearchCurrent() {
		int currentH = current.hBucket;
		Location l = searchHBin(currentH);
		if (l != null)
			current = l;
		else {
			for (int i = 0; i < dStarRange && l == null; i++) {
				l = searchHBin(i);
			}
		}
		current = l;
	}

	private int expanded;

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("Expanded ");
		b.append(expanded);
		b.append("\n");

		for (int i = 0; i < dStarRange; i++) {
			b.append("d* = ");
			b.append(i);
			b.append("\n");
			for (int j = 0; j < dStarRange; j++) {
				for (int k = 0; k < dStarRange; k++) {
					b.append(String.format("%3d", maxes[i][j][k]));
					b.append(" ");
				}
				b.append("\n");
			}
		}

		return b.toString();
	}

	public void expand() {
		if (current == null)
			return;
		expanded++;
		assert (isValid(current));

		Location initialCurrent = current;
		
		counts[current.dStarBucket][current.hStarBucket][current.hBucket]--;

		double hStar = current.hStarBucket * hBucketSize;
		int children = getBranching();
		for (int i = 0; i < children; i++) {
			double nextHStar;
			if (r.nextBoolean())
				nextHStar = hStar + getCost();
			else
				nextHStar = hStar - getCost();
			int nextHStarBucket = hBucket(nextHStar);
			if (nextHStarBucket >= dStarRange)
				nextHStarBucket = dStarRange - 1;
			if (nextHStarBucket < 0)
				nextHStarBucket = 0;

			double nextH = hhGen.getY(nextHStar);
			int nextHBucket = hBucket(nextH);
			if (nextHBucket >= dStarRange)
				nextHBucket = dStarRange - 1;
			if (nextHBucket < 0)
				nextHBucket = 0;

			double nextD = hdGen.getY(nextH);
			int nextDBucket = (int) nextD;
			if (nextDBucket >= dStarRange)
				nextDBucket = dStarRange - 1;
			if (nextDBucket < 0)
				nextDBucket = 0;

			addLocation(new Location(nextDBucket, nextHStarBucket, nextHBucket));
		}
		if(current == initialCurrent)
			globalSearchCurrent();
	}

	public void solve() {
		while (current != null && !current.isGoal()) {
			expand();
		}
		assert (current != null);
	}

	private int hBucket(double h) {
		int toReturn = (int) (h / hBucketSize);
		if (toReturn >= counts.length)
			toReturn = counts.length - 1;
		return toReturn;
	}

	private double getMinCost() {
		double incumbent = Double.MAX_VALUE;
		for (double d : costs) {
			if (d < incumbent)
				incumbent = d;
		}
		return incumbent;
	}

	private int getBranching() {
		return branching[r.nextInt(branching.length)];
	}

	private double getCost() {
		return costs[r.nextInt(costs.length)];
	}

	public int getExpanded() {
		return expanded;
	}

	public static void simpleTest() {
		int sampleSize = 1000;

		double averages[][] = new double[11][11];
		double stdevs[][] = new double[11][11];

		for (double hd = 0; hd <= 10; hd++) {
			for (double hh = 0; hh <= 10; hh++) {
				double samples[] = new double[sampleSize];
				for (int i = 0; i < sampleSize; i++) {
					double[] costs = { 1.0, 1.0, 1.0, 1.0, 2.5, 2.5, };
					int[] branching = { 2, 2, 2, 3, 3, 3, 4, 4, 4 };
					GreedySim s = new GreedySim(i, 10, costs, branching, 5,
							hd / 10, hh / 10, 15, 5);
					s.solve();
					samples[i] = s.getExpanded();
				}
				int hdIX = (int) hd;
				int hhIX = (int) hh;
				averages[hdIX][hhIX] = Stats.mean(samples);
				stdevs[hdIX][hhIX] = Stats.stdev(samples);
				System.err.printf("%d %d mean: %f (%f)\n", hdIX, hhIX, Stats.mean(samples), Stats.stdev(samples));
			}
		}
		for (int i = 0; i < 11; i++) {
			for (int j = 0; j < 11; j++) {
				System.err.printf("%10.2f ", averages[i][j]);
			}
			System.err.printf("\n");
		}

		System.err.printf("\n");
		for (int i = 0; i < 11; i++) {
			for (int j = 0; j < 11; j++) {
				System.err.printf("%10.2f ", stdevs[i][j]);
			}
			System.err.printf("\n");
		}

	}
	public static void tinyTest(){
		double hd = 0;
		double hh = 10;
		
		int sampleSize = 10;
		double samples[] = new double[sampleSize];
		for (int i = 0; i < sampleSize; i++) {
			double[] costs = { 1.0, 1.0, 1.0, 1.0, 2.5, 2.5, };
			int[] branching = { 2, 2, 2, 3, 3, 3, 4, 4, 4 };
			GreedySim s = new GreedySim(i, 5, costs, branching, 5,
					hd / 10, hh / 10, 15, 5);
			s.solve();
			samples[i] = s.getExpanded();
		}
		int hdIX = (int) hd;
		int hhIX = (int) hh;
		
		System.err.printf("%d %d Mean: %f (%f)\n", hdIX, hhIX, Stats.mean(samples), Stats.stdev(samples));
	}
	
	public static void main(String[] args) {
		tinyTest();
	}

}
