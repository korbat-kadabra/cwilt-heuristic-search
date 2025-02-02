package org.cwilt.search.algs.basic;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.basic.FringeOpen;
public class FringeSearch extends org.cwilt.search.search.SearchAlgorithm {
	
//	protected final HashMap<Object, SearchState> astarNodes = new HashMap<Object, SearchState>();
	protected int skips;
	protected int iterations;
	public FringeSearch(SearchProblem prob, Limit l) {
		super(prob, l);
		this.open = new FringeOpen<SearchNode>();
		this.closed = new HashMap<Object, SearchNode>();
		
		fLimit = prob.getInitial().h();
//		ArrayList<SearchState> path = (new algs.basic.bestfirst.AStar(prob, new Limit())).solve();
//		for(SearchState s : path){
//			astarNodes.put(s.getKey(), s);
//		}

	}

	protected final HashMap<Object, SearchNode> closed;
	protected final List<SearchNode> open;

	@Override
	public void reset() {
		open.clear();
		closed.clear();
	}

	@Override
	protected void cleanup() {
		open.clear();
		closed.clear();
	}

	@Override
	public SearchAlgorithm clone() {
		return new FringeSearch(prob, l.clone());
	}

	
	protected double minRejectedF;
	protected double fLimit;
	
	
	
	protected static final double IMPROVEMENT_THRESHOLD = 2.2E-13;

	protected void processNode(SearchNode n,
			ArrayList<SearchNode> rejectedChildren,
			ListIterator<SearchNode> openIter) {

		ArrayList<? extends SearchNode> children = n.expand();

		l.incrExp();
		l.incrGen(children.size());
//		boolean parentOnPath = astarNodes.containsKey(n.getState().getKey());
		for (SearchNode child : children) {
//			boolean onPath = astarNodes.containsKey(child.getState().getKey());
//			if(parentOnPath && onPath){
//				System.err.println(child);
//			}
			if (child.getState().isGoal()) {
				if (getIncumbent() == null || getIncumbent().getCost() > n.getG()) {
					setIncumbent( new Solution(child, child.getG(),
							l.getDuration(), child.pathLength(),
							l.getExpansions(), l.getGenerations(),
							l.getDuplicates()));
				}
			}

			SearchNode incumbentNode = closed.get(child.getState().getKey());
			if (incumbentNode == null) {
				// if the child is good enough, add the child to this iteration
				if (child.getF() > fLimit) {
//					assert(!onPath);
					addRejected(child);
				} else {
					openIter.add(child);
				}
				closed.put(child.getState().getKey(), child);
			} else if (child.getG() < incumbentNode.getG()) {
				double improvement = incumbentNode.getG() - child.getG();
				if (improvement < IMPROVEMENT_THRESHOLD) {
					continue;
				}

				SearchNode r = closed.remove(incumbentNode.getState().getKey());
				assert (r != null);
				// if the child is good enough, add the child to this iteration
				if (child.getF() > fLimit) {
//					assert(!onPath);
					addRejected(child);
					minRejectedF = Math.min(minRejectedF, child.getF());
				} else {
					openIter.add(child);
				}
				closed.put(child.getState().getKey(), child);
			}
			if (incumbentNode != null)
				l.incrDup();
		}

	}

	protected boolean keepGoing() {
		return l.keepGoing()
				&& (getIncumbent() == null || (getIncumbent().getCost() > fLimit));
	}
	protected ArrayList<SearchNode> rejectedChildren = new ArrayList<SearchNode>();
	
	protected void addRejected(SearchNode n){
		rejectedChildren.add(n);
	}
	
	protected void setIncumbent() {
		l.startClock();
		
		assert (open.size() == 1);


		while (!open.isEmpty() && keepGoing()) {
//			System.err.println("fLimit " + fLimit + " at " + l.getDuration());

			iterations ++;
			//System.err.printf("%d with %d\n", iterations, l.getExpansions());
			minRejectedF = Double.MAX_VALUE;

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
					l.incrReExp();
				}

				if (n.getF() > fLimit) {
					minRejectedF = Math.min(n.getF(), minRejectedF);
					skips ++;
					continue;
				}
				
				nIter.remove();
				processNode(n, rejectedChildren, nIter);
			}

			if (getIncumbent() == null || getIncumbent().getCost() > fLimit) {
				for (SearchNode n : rejectedChildren) {
					open.add(n);
					minRejectedF = Math.min(minRejectedF, n.getF());
				}
				rejectedChildren.clear();
			}
			if (minRejectedF < Double.MAX_VALUE)
				fLimit = minRejectedF;
		}
		l.endClock();
	}

	@Override
	public ArrayList<SearchState> solve() {
		SearchNode i = SearchNode.makeInitial(initial);

		open.add(i);

		try {
			setIncumbent();
		} catch (OutOfMemoryError e) {
			l.setOutOfMemory();
			l.endClock();
			open.clear();
			closed.clear();
		}
		if (getIncumbent() == null)
			return null;
		else {
//			System.err.println(getIncumbent().getGoal().printParents());
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			return finalPath;
		}
	}
	
	@Override
	public void printExtraData(PrintStream ps){
		super.printExtraData(ps);
		printPair(ps, "iterations", new Integer(iterations));
		printPair(ps, "skips", new Integer(skips));
	}

	
}
