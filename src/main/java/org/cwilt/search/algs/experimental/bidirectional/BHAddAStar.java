package org.cwilt.search.algs.experimental.bidirectional;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;
public class BHAddAStar extends HAddAStar {

	private int allFwd, allBwd= 0;
	private final SimpleRegression forwardRegress = new SimpleRegression();
	private final SimpleRegression backwardRegress = new SimpleRegression();

	
	public BHAddAStar(SearchProblem prob, Limit l) {
		super(prob, l, -1);
	}

	protected BHAddAStar(SearchProblem prob, Limit l, Comparator<HAddSearchNode> c) {
		super(prob, l, -1, c);
	}

	public BHAddAStar clone() {
		return new BHAddAStar(prob, l);
	}

	private void expandForward(int amount) {
		int fExp = 0;
		while (keepGoing() && fExp < amount) {
			HAddSearchNode current = open.poll();
			processNode(current);
			fExp++;
		}

	}

	protected void setIncumbent() {
		l.startClock();

		ArrayList<SearchState> goals = prob.getGoals();

		for (SearchState goalState : goals) {
			HAddSearchNode goal = this.makeInitialReverse(goalState);
			gb.add(goal);
			borderNodes.put(goal.getState().getKey(), goal);
		}
		assert (hAdd != Double.MAX_VALUE);


		int fExp = 100;
		int bExp = 100;
		while (keepGoing()) {
			fExp = fExp * 2;
			bExp = bExp * 2;
			// double the expansions
			expandForward(fExp);
			// do a few backwards expansions too
			increaseCache(bExp);

//			System.err.printf("fExp(end + 1) = %f;\nfFVal(end + 1) = %f;\n",
//					Math.log10(fExp), open.peek().getF());
//			System.err.printf("bExp(end + 1) = %f;\nhAdd(end + 1) = %f;\n",
//					Math.log10(cacheSize), super.hAdd);
			if(!keepGoing())
				break;
			forwardRegress.addData(Math.log10(fExp), open.peek().getF());
			backwardRegress.addData(Math.log10(cacheSize), super.hAdd);

			if (fExp > 400)
				break;
		}

		while (keepGoing()) {
			int nextFExp = fExp * 2;
			int nextBExp = bExp * 2;
			// double the expansions
			// need to decide if should go forwards or backwards
			double currentF = open.peek().getF();

//			System.err.printf("fExp(end+1) = %f;\nfFVal(end+1) = %f;\n",
//					Math.log10(fExp), open.peek().getF());
//			System.err.printf("bExp(end+1) = %f;\nhAdd(end+1) = %f;\n",
//					Math.log10(cacheSize), super.hAdd);

			double mf = forwardRegress.getSlope();
			double bf = forwardRegress.getIntercept();
			double mb = backwardRegress.getSlope();
			double bb = backwardRegress.getIntercept();

			// how big each would be if it got nextExp added to it
			double nextFwdExp = fExp + nextFExp;
			double nextBwdExp = super.cacheSize + nextBExp;

			double allForward = ((Math.log10(nextFwdExp) * mf + bf) - currentF) / nextFwdExp;
			double allBackward = ((Math.log10(nextBwdExp) * mb + bb) - hAdd)  / nextBwdExp;

//			System.err.println("fwd = " + allForward + ";");
//			System.err.println("bwd = " + allBackward + ";");

			if (allForward < allBackward) {
				increaseCache(nextBExp);
				bExp = bExp * 2;
				backwardRegress.addData(Math.log10(cacheSize), super.hAdd);
				backwardRegress.regress();
				allBwd++;
			} else {
				expandForward(nextFExp);
				if(open.isEmpty())
					break;
				forwardRegress.addData(Math.log10(fExp), open.peek().getF());
				forwardRegress.regress();
				fExp = fExp * 2;
				allFwd++;
			}

		}
		l.endClock();
	}

	@Override
	public void printExtraData(PrintStream ps) {
		super.printExtraData(ps);
		super.printPair(ps, "allFwd", new Integer(allFwd));
		super.printPair(ps, "allBwd", new Integer(allBwd));
		super.printPair(ps, "fCorr", new Double(forwardRegress.getRSquare()));
		super.printPair(ps, "bCorr", new Double(backwardRegress.getRSquare()));
	}

}
