package org.cwilt.search.domains.greedysim;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;

import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;
public class FullGreedySim implements SearchProblem {
	public FullGreedySim(int branchingFactor, double hMean, double dMean,
			double hStdev, double dStdev, double hdCorr, int seed) {
		this.r = new Random(seed);
		this.branchingFactor = branchingFactor;
		this.hMean = hMean;
		this.dMean = dMean;
		this.dStdev = dStdev;
		this.hStdev = hStdev;
		this.hdCorr = hdCorr;
	}

	public final int branchingFactor;
	public final double hMean, dMean, dStdev, hStdev;
	public final double hdCorr;
	public final Random r;

	public int getNewDStar(int h, int parentDStar) {
		while (true) {
			double randomMotion = r.nextGaussian()
					* Math.sqrt(hStdev * Math.pow(1 - (hdCorr * hdCorr), 2));
			double mean = dMean + hdCorr * hStdev / dStdev * (h - hMean);
			int nextDStar = (int) (randomMotion + mean);
			if(Math.abs(nextDStar - parentDStar) > 1)
				continue;
			return Math.max(0, nextDStar);
		}
	}

	private class GreedySimNode extends org.cwilt.search.search.SearchState {
		public GreedySimNode(int h, int pd) {
			this.dStar = getNewDStar(h, pd);
			this.h = h;
		}

		private final int dStar;
		private final int h;

		@Override
		public String toString() {
			StringBuffer b = new StringBuffer();
			b.append("h: ");
			b.append(h);
			b.append(" d*: ");
			b.append(dStar);
			return b.toString();
		}

		@Override
		public ArrayList<Child> expand() {
			ArrayList<Child> children = new ArrayList<Child>(branchingFactor);
			for (int i = 0; i < branchingFactor; i++) {
				int nextH;
				if (i % 2 == 0)
					nextH = h - 1;
				else
					nextH = h + 1;
				if (nextH < 0)
					nextH = 0;
				children.add(new Child(new GreedySimNode(nextH, dStar), 1.0));
			}
			return children;
		}

		@Override
		public ArrayList<Child> reverseExpand() {
			return null;
		}

		@Override
		public double h() {
			return h;
		}

		@Override
		public int d() {
			return 0;
		}

		@Override
		public boolean isGoal() {
			return dStar == 0;
		}

		@Override
		public Object getKey() {
			return this;
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}

		@Override
		public boolean equals(Object other) {
			return this == other;
		}

		@Override
		public int lexOrder(SearchState s) {
			throw new org.cwilt.search.utils.basic.NotImplementedException();
		}

	}

	@Override
	public SearchState getInitial() {
		int d = (int) (dMean + r.nextGaussian() * dStdev);
		int h = (int) (hMean + r.nextGaussian() * hStdev);
		if(h < d)
			h = d;
		return new GreedySimNode(d, h);
	}

	@Override
	public SearchState getGoal() {
		return null;
	}

	@Override
	public ArrayList<SearchState> getGoals() {
		return null;
	}

	@Override
	public void setCalculateD() {
	}

	@Override
	public void printProblemData(PrintStream ps) {
	}

}
