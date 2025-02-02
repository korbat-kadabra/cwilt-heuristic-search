package org.cwilt.search.algs.basic.bestfirst;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.basic.Heapable;
public abstract class BucketedBestFirst extends org.cwilt.search.search.SearchAlgorithm {

	public abstract org.cwilt.search.utils.basic.AbstractBucketQueue makeOpen();

	protected final double bucketSize;

	protected final org.cwilt.search.utils.basic.AbstractBucketQueue open;

	protected BucketedBestFirst(SearchProblem prob, Limit l, double bucketSize) {
		super(prob, l);
		this.bucketSize = bucketSize;
		open = makeOpen();
		closed = new HashMap<Object, SearchNode>();
	}

	protected final HashMap<Object, SearchNode> closed;

	@Override
	public ArrayList<SearchState> solve() {
		SearchNode i = SearchNode.makeInitial(initial);
		open.add(i);
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
			// System.err.println(incumbent.getGoal().printParents());
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			return finalPath;
		}
	}

	protected Solution processNode(SearchNode current) {

		// java.util.List<SearchNode> currentOpen = open.getAll();
		//
		// assert(currentOpen.size() == open.size());
		// java.util.Iterator<SearchNode> openIter = currentOpen.iterator();
		// java.util.Comparator<SearchNode> c = new SearchNode.FGComparator();
		// while(openIter.hasNext()){
		// assert(c.compare(current, openIter.next()) <= 0);
		// }

		if (current.getState().isGoal()) {
			if (getIncumbent() == null || getIncumbent().getCost() > current.getG())
				return new Solution(current, current.getG(), l.getDuration(),
						current.pathLength(), l.getExpansions(),
						l.getGenerations(), l.getDuplicates());
		} else {
			ArrayList<? extends SearchNode> children = current.expand();
			l.incrExp();
			Iterator<? extends SearchNode> childIter = children.iterator();
			while (childIter.hasNext()) {
				l.incrGen();
				considerChild(childIter.next());
			}
		}
		return null;
	}

	private void setIncumbent() {
		l.startClock();
		Solution goal = null;
		while (!open.isEmpty() && goal == null && l.keepGoing()) {
			SearchNode current = open.pop();

			goal = processNode(current);
		}
		l.endClock();
		if (goal != null)
			setIncumbent(goal);
	}

	public void considerChild(SearchNode child) {
		// look for the child in the hash table
		SearchNode incumbent = closed.get(child.getState().getKey());
		if (incumbent == null) {
			open.add(child);
			closed.put(child.getState().getKey(), child);
		} else if (child.getG() < incumbent.getG()) {
			int ix = incumbent.getHeapIndex();
			if (ix != Heapable.NO_POS)
				open.remove(incumbent, ix);
			closed.remove(incumbent);
			open.add(child);
			closed.put(child.getState().getKey(), child);
		}
		if (incumbent != null)
			l.incrDup();
	}

	public SearchState findFirstGoal() {
		// if the open list is empty, have to do some stuff, otherwise just
		// continue
		if (open.isEmpty()) {
			SearchNode i = SearchNode.makeInitial(initial);
			open.add(i);
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
	public void reset() {
		open.clear();
		closed.clear();
	}

}
