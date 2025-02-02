package org.cwilt.search.algs.utils;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.utils.basic.MinHeap;
public class ReverseEnum extends SearchAlgorithm {
	private final HashMap<Object, SearchNode> closed;
	private final MinHeap<SearchNode> open;
	public HashMap<Object, SearchNode> getClosed() {
		return closed;
	}
	public ReverseEnum(SearchProblem prob, Limit l) {
		super(prob, l);
		this.closed = new HashMap<Object, SearchNode>();
		this.open = new MinHeap<SearchNode>(new SearchNode.GComparator());
	}

	@Override
	public void reset() {
		open.clear();
		closed.clear();
	}

	@Override
	protected void cleanup() {
		this.reset();
	}

	@Override
	public SearchAlgorithm clone() {
		return new ReverseEnum(prob, l.clone());
	}

	@Override
	public ArrayList<SearchState> solve() {

		ArrayList<SearchState> goals = prob.getGoals();
		for (SearchState goal : goals) {
			SearchNode first = SearchNode.makeInitial(goal);
			open.add(first);
			closed.put(first.getState().getKey(), first);
		}
		

		while (!open.isEmpty()) {
			SearchNode next = open.poll();
			ArrayList<? extends SearchNode> children = next.reverseExpand();
			for (SearchNode n : children) {
				SearchNode incumbent = closed.get(n.getState().getKey());
				if (incumbent == null || incumbent.getG() > n.getG()) {
					if (incumbent != null) {
						open.remove(incumbent);
						closed.remove(incumbent.getState().getKey());
					}
					open.add(n);
					closed.put(n.getState().getKey(), n);
				}
			}
		}
		
		return null;
	}

	public SearchNode lookup(SearchState s) {
		SearchNode n = closed.get(s.getKey());
//		assert(n != null);
		
		return n;
	}
	
	@Override
	public void printExtraData(PrintStream ps){
		Iterator<Entry<Object, SearchNode>> i = closed.entrySet().iterator();
		while(i.hasNext()){
			SearchNode next = i.next().getValue();
			StringBuffer b = new StringBuffer();
			b.append(next.getG());
			b.append(" ");
			b.append(next.getH());
			SearchAlgorithm.printPair(ps, next.getState().toString(), b.toString());
		}
	}
}
