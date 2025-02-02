package org.cwilt.search.algs.basic;
import java.util.ArrayList;
import java.util.ListIterator;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;
public class FringeSearchCR extends FringeSearch {
	private final int nBuckets;

	private java.util.HashSet<Object> expanded = new java.util.HashSet<Object>();
	public FringeSearchCR(SearchProblem prob, Limit l, int nBuckets) {
		super(prob, l);
		this.nBuckets = nBuckets;
		this.histogram = new int[nBuckets];
		this.exp = 0;
	}

	private int exp;

	@Override
	public SearchAlgorithm clone() {
		return new FringeSearchCR(prob, l.clone(), nBuckets);
	}

	private double fBound;

	protected boolean keepGoing() {
		return l.keepGoing()
				&& (getIncumbent() == null || (getIncumbent().getCost() > fBound));
	}

	protected void setIncumbent() {
		l.startClock();

		//HashSet<Object> expStates = new HashSet<Object>();

		assert (open.size() == 1);

		fLimit = open.get(0).getF();
//		fLimit = 3.512171168615772;
		fBound = fLimit;

		while (!open.isEmpty() && keepGoing()) {
			iterations++;

//			System.err.println("fbound " + fBound + " fLimit " + fLimit
//					+ " at " + l.getDuration());
			ArrayList<SearchNode> rejectedChildren = new ArrayList<SearchNode>();

			ListIterator<SearchNode> nIter = open.listIterator();
			while (nIter.hasNext() && keepGoing()) {
				SearchNode n = nIter.next();
				SearchNode expIncumbentNode = closed.get(n.getState().getKey());
				if (expIncumbentNode != null && expIncumbentNode != n) {
					double improvement = expIncumbentNode.getG() - n.getG();
					if (improvement < IMPROVEMENT_THRESHOLD) {
						nIter.remove();
						continue;
					}
					if (expIncumbentNode.getG() <= n.getG()) {
						nIter.remove();
						continue;
					}
				}

				if (n.getF() > fLimit) {
					logRejected(n.getF());
					continue;
				}

				
				nIter.remove();
				exp++;

				processNode(n, rejectedChildren, nIter);
				
				if (expanded.contains(n.getState())) {
					l.incrReExp();
				} else {
					expanded.add(n.getState());
				}
			}

			if (getIncumbent() == null || getIncumbent().getCost() < fLimit) {
				for (SearchNode n : rejectedChildren) {
					assert(!open.contains(n));
					open.add(n);
					logRejected(n.getF());
				}
			}
			setNewBound();
			clearHistogram();
		}
		l.endClock();
	}

	/**
	 * The histogram used to track expansions
	 */
	private final int[] histogram;
	/**
	 * The number of items that are not tracked by the histogram, mostly as a
	 * curiousity.
	 */
	int extra = 0;

	private void clearHistogram() {
		for (int i = 0; i < nBuckets; i++) {
			histogram[i] = 0;
		}
		extra = 0;
		exp = 0;
	}

	private void setNewBound() {
		double goal = -1;
		double sum = 0;
		double lastBucket = -1;
		for (int i = 0; i < nBuckets; i++) {
			sum += histogram[i];
			if (histogram[i] != 0)
				lastBucket = i;
			if (sum > exp * 2) {
				goal = i;
				break;
			}
		}
		fBound = fLimit;
		if (goal == -1) {
			fLimit = fLimit * ((lastBucket + nBuckets + 1) / nBuckets);
		} else {
			fLimit = fLimit * ((goal + nBuckets + 1) / nBuckets);
		}

	}

	private void logRejected(double r) {
		assert (r > fLimit);
		double pOver = r / fLimit - 1;
		pOver *= nBuckets;
		int index = (int) pOver;
		if (index >= nBuckets) {
			extra++;
		} else {
			histogram[index]++;
		}
	}

}
