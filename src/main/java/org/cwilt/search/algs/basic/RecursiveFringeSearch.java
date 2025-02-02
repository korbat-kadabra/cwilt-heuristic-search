package org.cwilt.search.algs.basic;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;
public class RecursiveFringeSearch extends org.cwilt.search.search.SearchAlgorithm{

	private int iterations;
	
	@Override
	public void printExtraData(PrintStream ps){
		super.printExtraData(ps);
		printPair(ps, "iterations", new Integer(iterations));
	}

	public RecursiveFringeSearch(SearchProblem prob, Limit l) {
		super(prob, l);
		this.open = new LinkedList<SearchNode>();
		this.closed = new HashMap<Object, SearchNode>();
	}
	
	private final HashMap<Object, SearchNode> closed;
	private final LinkedList<SearchNode> open;
	
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
		return new RecursiveFringeSearch(prob, l.clone());
	}
	
	private void recursiveExpandChildren(ArrayList<SearchNode> children){
		ArrayList<SearchNode> c1 = children;
		ArrayList<SearchNode> c2 = new ArrayList<SearchNode>();
		ArrayList<SearchNode> temp;
		
		while(!c1.isEmpty() && getIncumbent() == null && l.keepGoing()){
			Iterator<SearchNode> c1Iter = c1.iterator();
			while(c1Iter.hasNext() && getIncumbent() == null && l.keepGoing()){
				SearchNode n = c1Iter.next();
				if(n.getState().isGoal()){
					if (getIncumbent() == null || getIncumbent().getCost() > n.getG())
						setIncumbent ( new Solution(n, n.getG(), l.getDuration(),
								n.pathLength(), l.getExpansions(), l
										.getGenerations(), l.getDuplicates()));
					break;
				}
				processNode(n, c2, null);
			}
			c1.clear();
			temp = c1;
			c1 = c2;
			c2 = temp;
		}
	}
	
	
	private double minRejectedF;
	private double fLimit;
	
	protected void processNode(SearchNode n, ArrayList<SearchNode> recursiveChildren, ListIterator<SearchNode> openIter){
		ArrayList<? extends SearchNode> children = n.expand();
		l.incrExp();
		l.incrGen(children.size());
		for(SearchNode child : children){
			SearchNode incumbentNode = closed.get(child.getState().getKey());
			if (incumbentNode == null) {
				//if the child is good enough, add the child to this iteration
				if(child.getF() < fLimit)
					recursiveChildren.add(child);
				else {
					if(openIter == null)
						open.add(child);
					else
						openIter.add(child);
					minRejectedF = Math.min(minRejectedF, child.getF());
				}
				closed.put(child.getState().getKey(), child);
			} else if (child.getG() < incumbentNode.getG()) {
				SearchNode r = closed.remove(incumbentNode.getState().getKey());
				assert(r != null);
				//if the child is good enough, add the child to this iteration
				if(child.getF() <= fLimit)
					recursiveChildren.add(child);
				else
				{
					if(openIter == null)
						open.add(child);
					else
						openIter.add(child);
					minRejectedF = Math.min(minRejectedF, child.getF());
				}
				closed.put(child.getState().getKey(), child);
			}
			if (incumbentNode != null)
				l.incrDup();
		}

	}
	
	protected void setIncumbent() {
		l.startClock();
		
		assert(open.size() == 1);
		
		fLimit = open.peek().getF();
		
		while (!open.isEmpty() && getIncumbent() == null && l.keepGoing()) {
			iterations ++;
			ArrayList<SearchNode> recursiveChildren = new ArrayList<SearchNode>();
			minRejectedF = Double.MAX_VALUE;
			
			ListIterator<SearchNode> nIter = open.listIterator();
			while(nIter.hasNext() && getIncumbent() == null && l.keepGoing()){
				SearchNode n = nIter.next();

				SearchNode expIncumbentNode = closed.get(n.getState().getKey());
				if(expIncumbentNode != null){
					if(expIncumbentNode.getG() < n.getG()){
						nIter.remove();
						continue;
					}
				}
				
				if (n.getF() > fLimit){
					minRejectedF = Math.min(n.getF(), minRejectedF);
					continue;
				}
				if (n.getState().isGoal()) {
					if (getIncumbent() == null || getIncumbent().getCost() > n.getG())
						setIncumbent ( new Solution(n, n.getG(), l.getDuration(),
								n.pathLength(), l.getExpansions(), l
										.getGenerations(), l.getDuplicates()));
					break;
				}
				nIter.remove();
				processNode(n, recursiveChildren, nIter);
			}
			
			if(getIncumbent() == null){
				recursiveExpandChildren(recursiveChildren);
			}
			fLimit = minRejectedF;
		}
		l.endClock();
	}

	
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

}
