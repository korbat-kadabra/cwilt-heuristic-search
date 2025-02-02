/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.algs.basic;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;
public class IDAStar extends SearchAlgorithm {

	public void reset() {
		bestRejectedF = 0;
	}

	public SearchAlgorithm clone() {
		IDAStar i = new IDAStar(prob, l.clone());
		i.bestRejectedF = bestRejectedF;
		return i;
	}

	private double bestRejectedF;

	public IDAStar(org.cwilt.search.search.SearchProblem initial, Limit l) {
		super(initial, l);
	}

	@Override
	public SearchState findFirstGoal() {
		assert (false);
		System.err.println("IDA* only runs once.");
		System.exit(1);
		// TODO Auto-generated method stub
		return null;
	}

	protected double evaluateNode(SearchNode n) {
		return n.getF();
	}
	private int iterations;
	
	private void doIteration(double bound, SearchNode n) {
		if (evaluateNode(n) > bound) {
			if (evaluateNode(n) < bestRejectedF)
				bestRejectedF = evaluateNode(n);
		} else {
			if (n.getState().isGoal()) {
				setIncumbent ( new Solution(n, n.getG(), l.getDuration(), n
						.pathLength(), l.getExpansions(), l.getGenerations(), l
						.getDuplicates()));
				return;
			}
			Iterator<? extends SearchNode> i = n.expand().iterator();
			l.incrExp();
			while (getIncumbent() == null && i.hasNext()) {
				l.incrGen();
				doIteration(bound, i.next());
			}
		}
	}

	private void setIncumbent() {
		l.startClock();
		SearchNode start = SearchNode.makeInitial(initial);
		double bound = this.evaluateNode(start);
		while (getIncumbent() == null && l.keepGoing()) {
			this.iterations ++;
			if (bestRejectedF < Double.MAX_VALUE)
				bound = bestRejectedF;
			bestRejectedF = Double.MAX_VALUE;
//			System.err.println("bound is " + bound);
			doIteration(bound, start);
			if (bestRejectedF == Double.MAX_VALUE) {
				l.endClock();
				return;
			}
		}
		l.endClock();
	}

	@Override
	public ArrayList<SearchState> solve() {
		try {
			setIncumbent();
		} catch (OutOfMemoryError e) {
			l.endClock();
			l.setOutOfMemory();
		}
		if (getIncumbent() == null)
			return null;
		else {
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			return finalPath;
		}
	}

	public void cleanup() {
		return;
	}
	
	public void printExtraData(PrintStream ps){
		super.printExtraData(ps);
		SearchAlgorithm.printPair(ps, "iterations", new Integer(iterations));
	}
}
