package org.cwilt.search.misc.analysis;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import org.cwilt.search.domains.tiles.TileProblem;
import org.cwilt.search.search.SearchNode;
public class HHStar {
	private final Queue<SearchNode> open;
	private final HashMap<Object, SearchNode> closed;
	private final HashMap<Object, SearchNode> onOpen;
	private final SearchNode initial;

	public HHStar(SearchNode initial) {
		this.initial = initial;
		this.open = new LinkedList<SearchNode>();
		this.closed = new HashMap<Object, SearchNode>();
		this.onOpen= new HashMap<Object, SearchNode>();
	}

	private static boolean closeEnough(double v1, double v2){
		return Math.abs(v2 - v1) < 0.001;
	}
	
	public void countNodes() {
		open.add(initial);
		onOpen.put(initial.getState().getKey(), initial);
		while (!open.isEmpty()) {
			SearchNode next = open.poll();
			SearchNode incumbent = closed.get(next.getState().getKey());
			if (incumbent == null || incumbent.getF() > next.getF()) {
				closed.put(next.getState().perfectHash(), next);
				ArrayList<? extends SearchNode> children = next.expand();
				if(closed.size() % 100000 == 0)
					System.err.println("total: " + closed.size());
				for(SearchNode child : children){
					if(!onOpen.containsKey(child.getState().getKey()))
						if(closeEnough(child.getH(), child.getG()))
							open.add(child);
				}
			}
		}
	}
	
	public static void main(String[] args){
		TileProblem tp = new TileProblem(4, 4, "unit");

		HHStar h = new HHStar(SearchNode.makeInitial(tp.getInitial()));
		h.countNodes();
		System.err.println(h.closed.size());
	}
}
