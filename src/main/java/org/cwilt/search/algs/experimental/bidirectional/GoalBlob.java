package org.cwilt.search.algs.experimental.bidirectional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.basic.Heapable;import org.cwilt.search.utils.basic.MinHeap;
public class GoalBlob extends org.cwilt.search.search.SearchAlgorithm {

	private final HashMap<Object, SearchNode> closed;
	private final HashMap<Object, SearchNode> blob;
	private final MinHeap<SearchNode> open;
	private final int blobSize;
	private SearchNode blobHit;
	
	
	public GoalBlob(SearchProblem prob, Limit l, int blobSize) {
		super(prob, l);
		this.blobSize = blobSize;
		this.open = new MinHeap<SearchNode>(new SearchNode.FGComparator());
		this.blob = new HashMap<Object, SearchNode>();
		this.closed = new HashMap<Object, SearchNode>();
	}

	@Override
	protected void cleanup() {
		blob.clear();
	}

	@Override
	public SearchAlgorithm clone() {
		assert (l.getGenerations() == 0);
		GoalBlob b = new GoalBlob(prob, l.clone(), blobSize);
		return b;
	}

	@Override
	public void reset() {
		blob.clear();
		open.clear();
		closed.clear();
	}

	@Override
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

	@Override
	public ArrayList<SearchState> solve() {
		l.startClock();
		
		SearchNode i = SearchNode.makeInitial(initial);
		open.insert(i);
		closed.put(i.getState().getKey(), i);

		SearchNode goal = SearchNode.makeInitial(prob.getGoal());

		MinHeap<SearchNode> gb = new MinHeap<SearchNode>(
				new SearchNode.GComparator());

		gb.insert(goal);

		while (blobSize > blob.size() && !gb.isEmpty()) {
			SearchNode next = gb.poll();
			if (!blob.containsKey(next.getState().getKey())) {
				blob.put(next.getState().getKey(), next);
				l.incrExp();
				ArrayList<? extends SearchNode> children = next.reverseExpand();
				l.incrGen(children.size());
				for (SearchNode child : children) {
					if (!blob.containsKey(child.getState().getKey())) {
						gb.insert(child);
					}
				}
			}
		}

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
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			ArrayList<SearchState> blobPath = blobHit.reconstructPath();
			
			for(int ix = blobPath.size() - 1; ix > 0; ix--){
				finalPath.add(blobPath.get(ix));
			}
			
			return finalPath;
		}
	}

	private boolean isGoal(SearchNode n) {
		if (n.getState().isGoal())
			return true;
		else {
			return blob.containsKey(n.getState().getKey());
		}
	}

	private void setIncumbent() {
		l.startClock();
		Solution goal = null;
		while (!open.isEmpty() && goal == null && l.keepGoing()) {
			SearchNode current = open.poll();
			if (current.getState().isGoal()) {
				if (getIncumbent() == null || getIncumbent().getCost() > current.getG())
					goal = new Solution(current, current.getG(), l
							.getDuration(), current.pathLength(), l
							.getExpansions(), l.getGenerations(), l
							.getDuplicates());
			} else if (isGoal(current)) {
				blobHit = blob.get(current.getState().getKey());
				if (getIncumbent() == null || getIncumbent().getCost() > current.getG() + blobHit.getG())
					goal = new Solution(current, current.getG() + blobHit.getG(), l
							.getDuration(), current.pathLength() + blobHit.pathLength() - 1, l
							.getExpansions(), l.getGenerations(), l
							.getDuplicates());
			} else {
				ArrayList<? extends SearchNode> children = current.expand();
				l.incrExp();
				Iterator<? extends SearchNode> childIter = children.iterator();
				while (childIter.hasNext()) {
					l.incrGen();
					considerChild(childIter.next());
				}
			}
		}
		l.endClock();
		if (goal != null)
			setIncumbent(goal);
	}

	protected void considerChild(SearchNode child) {
		// look for the child in the hash table
		SearchNode incumbent = closed.get(child.getState().getKey());
		if (incumbent == null) {
			open.insert(child);
			closed.put(child.getState().getKey(), child);
		} else if (child.getG() < incumbent.getG()) {
			int ix = incumbent.getHeapIndex();
			if (ix != Heapable.NO_POS)
				open.removeAt(ix);
			closed.remove(incumbent);
			open.insert(child);
			closed.put(child.getState().getKey(), child);
		}
		if (incumbent != null)
			l.incrDup();
	}

}
