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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;
public class Bulb extends SearchAlgorithm {

	/*
	 * Need to add a counter that gets set on the 0 discrepency run that says
	 * about how many nodes you can expand, then use that as the expansion limit
	 * on future probes.
	 */

	private final int beamWidth;
	private int restarts;
	private int discrCount;
	private final Comparator<SearchNode> c;

	private int nodeMax;

	private boolean outOfMemory() {
		return (closed.size() > nodeMax);
	}

	private final HashMap<Object, SearchNode> closed;

	private class Slice {
		private final ArrayList<SearchNode> slice;
		private double value;
		private int index;

		private void makeSuccessor(ArrayList<SearchNode> parents, int i) {
			this.index = i;
			this.value = -1;
			ArrayList<SearchNode> succs = generateNewSuccessors(parents);
			for (SearchNode s : succs) {
				if (s.getState().isGoal()) {
					Solution sol = new Solution(s, s.getG(), l.getDuration(), s
							.pathLength(), l.getExpansions(), l
							.getGenerations(), l.getDuplicates());
					if (getIncumbent() == null
							|| getIncumbent().getCost() > sol.getCost()) {
						setIncumbent ( sol );
					}
					this.value = 1;
					this.index = -1;
					return;
				}
			}
			if (succs.size() == 0 || succs.size() == index) {
				this.value = Double.MAX_VALUE;
				this.index = -1;
				return;
			}
			while (index < succs.size() && slice.size() < beamWidth) {
				if (!closed.containsKey(succs.get(index).getState().getKey())) {
					slice.add(succs.get(index));
					closed.put(succs.get(index).getState().getKey(), succs
							.get(index));
				} else {
					l.incrDup();
				}
				index++;
			}
		}

		public Slice(ArrayList<SearchNode> parents, int i) {
			slice = new ArrayList<SearchNode>(beamWidth);
			makeSuccessor(parents, i);
		}
	}

	/**
	 * does the initial probe into the tree with 0 discrepencies.
	 * 
	 * @param parents
	 *            parent nodes to use.
	 */
	private void firstProbe(ArrayList<SearchNode> parents) {
		if (getIncumbent() != null)
			return;
		if (!l.keepGoingNoMem())
			return;
		if (Limit.memoryCheck(0.95)) {
			super.m.incrRestarts();
			restarts++;
			nodeMax = closed.size();
			return;
		}
		Slice current = new Slice(parents, 0);
		if (current.value >= 0)
			return;
		if (current.slice.size() == 0)
			return;
		firstProbe(current.slice);
		for (SearchNode n : current.slice) {
			closed.remove(n.getState().getKey());
		}
	}

	private void bulbProbe(int discrepencies, ArrayList<SearchNode> parents) {
		if (getIncumbent() != null){
			return;
		}
		if (!l.keepGoingNoMem()){
			return;
		}
		Slice current = new Slice(parents, 0);
		if (current.value >= 0) {
			return;
		}
		if (discrepencies == 0) {
			if (current.slice.size() == 0) {
				return;
			}
			if (outOfMemory() == false)
				bulbProbe(0, current.slice);
			else {
				super.m.incrRestarts();
				restarts++;
				return;
			}
			for (SearchNode n : current.slice) {
				closed.remove(n.getState().getKey());
			}
		} else {
			if (current.slice.size() != 0) {
				for (SearchNode n : current.slice) {
					closed.remove(n.getState().getKey());
				}
			}
			while (true) {
				current = new Slice(parents, current.index);
				if (current.value >= 0) {
					if (current.value < Double.MAX_VALUE){
						return;
					}
					else
						break;
				}
				if (current.slice.size() == 0)
					continue;
				bulbProbe(discrepencies - 1, current.slice);
				for (SearchNode n : current.slice) {
					closed.remove(n.getState().getKey());
				}
				if (getIncumbent() != null){
					return;
				}
			}
			current = new Slice(parents, 0);
			if (current.value >= 0){
				return;
			}
			if (current.slice.size() == 0){
				return;
			}
			bulbProbe(discrepencies, current.slice);
			for (SearchNode n : current.slice) {
				closed.remove(n.getState().getKey());
			}
		}
	}

	private ArrayList<SearchNode> generateNewSuccessors(
			ArrayList<SearchNode> stateset) {
		ArrayList<SearchNode> succs = new ArrayList<SearchNode>(beamWidth);
		for (SearchNode s : stateset) {
			ArrayList<? extends SearchNode> children = s.expand();
			succs.addAll(children);
			l.incrExp();
			l.incrGen(children.size());
		}
		Collections.sort(succs, c);
		return succs;
	}

	public Bulb(org.cwilt.search.search.SearchProblem initial, Limit l, int beamWidth, int discrepencies) {
		super(initial, l);
		this.beamWidth = beamWidth;
		this.discrCount = discrepencies;
		this.closed = new HashMap<Object, SearchNode>();
		this.c = new SearchNode.FGComparator();
	}

	@Override
	public SearchAlgorithm clone() {
		return new Bulb(prob, l.clone(), beamWidth, discrCount);
	}

	@Override
	public SearchState findFirstGoal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void reset() {
		discrCount = 0;
		closed.clear();
	}

	@Override
	public ArrayList<SearchState> solve() {
		l.startClock();

		SearchNode i = SearchNode.makeInitial(super.initial);
		ArrayList<SearchNode> first = new ArrayList<SearchNode>();
		first.add(i);

		firstProbe(first);
		if (getIncumbent() == null)
			discrCount++;
		closed.clear();

	
		while (l.keepGoingNoMem() && getIncumbent() == null) {
			closed.put(i.getState().getKey(), i);
			bulbProbe(discrCount, first);
			if (getIncumbent() == null)
				discrCount++;
			closed.remove(i.getState().getKey());
		}
		l.endClock();
		if (getIncumbent() == null)
			return null;
		else {
			ArrayList<SearchState> finalPath = getIncumbent().reconstructPath();
			return finalPath;
		}
	}

	protected void cleanup() {
		closed.clear();
	}

	public void printExtraData(PrintStream ps) {
		printPair(ps, getName() + " discrepencies", new Integer(
				discrCount));
		printPair(ps, getName() + " restarts",
				new Integer(restarts));
		printPair(ps, getName() + " capacity", new Integer(nodeMax));
	}

	@Override
	public void printSearchData(PrintStream ps) {
		super.printSearchData(ps);
	}
}
