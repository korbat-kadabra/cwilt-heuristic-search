/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.algs.basic.bestfirst;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.basic.Heapable;import org.cwilt.search.utils.basic.MinHeap;
public abstract class BestFirstSearchAll extends org.cwilt.search.search.SearchAlgorithm {

	public void reset() {
		closed.clear();
		open.clear();
	}
	private int solutionCount = 0;
	protected final HashMap<Object, SearchNode> closed;
//	protected final HashMap<Object, SearchNode> expanded;
	protected org.cwilt.search.utils.basic.MinHeap<SearchNode> open;
	protected abstract Comparator<SearchNode> getComparator();
	
	public BestFirstSearchAll(org.cwilt.search.search.SearchProblem initial, org.cwilt.search.search.Limit l) {
		super(initial, l);
		closed = new HashMap<Object, SearchNode>();
//		expanded = new HashMap<Object, SearchNode>();
		
		open = new MinHeap<SearchNode>(new SearchNode.FGComparator());
	}

	@Override
	public ArrayList<SearchState> solve() {
		SearchNode i = SearchNode.makeInitial(initial);
		open.insert(i);
		closed.put(i.getState().getKey(), i);

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
			
//			FileOutputStream fos = null;
//			ObjectOutputStream out = null;
//			try {
//				fos = new FileOutputStream("astarall");
//				out = new ObjectOutputStream(fos);
//				out.writeObject(expanded);
//				out.close();
//			} catch (IOException ex) {
//				ex.printStackTrace();
//			}

			
			// System.err.println(incumbent.getGoal().printParents());
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			return finalPath;
		}
	}

	protected Solution processNode(SearchNode current) {
		if (current.getState().isGoal()) {
			solutionCount ++;
			if (getIncumbent() == null || getIncumbent().getCost() > current.getG())
			{
				return new Solution(current, current.getG(), l.getDuration(),
						current.pathLength(), l.getExpansions(), l
								.getGenerations(), l.getDuplicates());
			}
		} else {
			ArrayList<? extends SearchNode> children = current.expand();
//			expanded.put(current.getState().getKey(), current);
			l.incrExp();
			Iterator<? extends SearchNode> childIter = children.iterator();
			while (childIter.hasNext()) {
				l.incrGen();
				considerChild(childIter.next());
			}
		}
		return null;
	}

	protected void setIncumbent() {
		l.startClock();
		while (!open.isEmpty() && (getIncumbent() == null || getIncumbent().getCost() >= open.peek().getF()) && l.keepGoing()) {
			Solution goal = null;
			SearchNode current = open.poll();
			goal = processNode(current);
			if (goal != null){
				setIncumbent(goal);
			}
		}
		l.endClock();
	}

	public void considerChild(SearchNode child) {
		// look for the child in the hash table
		SearchNode incumbentNode = closed.get(child.getState().getKey());
		if (incumbentNode == null) {
			open.insert(child);
			closed.put(child.getState().getKey(), child);
		} else if (child.getG() < incumbentNode.getG()) {
			int ix = incumbentNode.getHeapIndex();
			if (ix != Heapable.NO_POS)
				open.removeAt(ix);
			SearchNode r = closed.remove(incumbentNode.getState().getKey());
			assert(r != null);
			closed.put(child.getState().getKey(), child);
			open.add(child);
		}
		if (incumbentNode != null)
			l.incrDup();
	}


	public SearchState findFirstGoal() {
		// if the open list is empty, have to do some stuff, otherwise just
		// continue
		if (open.isEmpty()) {
			SearchNode i = SearchNode.makeInitial(initial);
			open.insert(i);
			closed.put(i.getState().getKey(), i);
		}
		this.setIncumbent();
		if (getIncumbent() != null)
			return getIncumbent().getGoal().getState();
		else
			return null;
	}

	protected void cleanup() {
		open.clear();
		closed.clear();
	}

	@Override
	public SearchAlgorithm clone() {
		return new AStarAll(prob, l.clone());
	}
	
	@Override
	public void printExtraData(PrintStream ps){
		super.printExtraData(ps);
		SearchAlgorithm.printPair(ps, "solution count", new Integer(solutionCount));
	}

}
