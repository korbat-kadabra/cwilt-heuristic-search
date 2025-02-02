package org.cwilt.search.algs.basic;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import org.cwilt.search.algs.experimental.DoubleQueueSearch.DQSearchNode;import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.Solution;import org.cwilt.search.utils.basic.Heapable;import org.cwilt.search.utils.basic.MinHeap;import org.cwilt.search.utils.basic.MinHeap2;
public class ASEpsilon extends org.cwilt.search.search.SearchAlgorithm {

	private final double bound;

	private final MinHeap2<DQSearchNode> open;
	private final HashMap<Object, DQSearchNode> closed;
	private final ArrayList<MinHeap<DQSearchNode>> focal;
	private final Comparator<DQSearchNode> cmp;

	private int bestDBucket;

	public ASEpsilon(SearchProblem prob, Limit l, double bound) {
		super(prob, l);
		this.cmp = new DQSearchNode.FGComparator();
		prob.setCalculateD();

		this.bound = bound;

		this.open = new MinHeap2<DQSearchNode>(new DQSearchNode.FGComparator());
		this.closed = new HashMap<Object, DQSearchNode>();
		this.focal = new ArrayList<MinHeap<DQSearchNode>>();
		int initialSize = prob.getInitial().d();
		focalSize(initialSize + 1);
	}

	private void checkChildD(DQSearchNode child){
		int childD = child.getD();
		double fBound = open.peek().getF() * bound;
		if(childD < this.bestDBucket && child.getF() <= fBound){
			this.bestDBucket = childD;
		}
	}
	
	private void considerChild(DQSearchNode child) {
		// look for the child in the hash table
		DQSearchNode incumbent = closed.get(child.getState().getKey());
		if (incumbent == null) {
			open.insert(child);
			closed.put(child.getState().getKey(), child);
			int dIX = child.getD();
			if (focal.size() <= dIX || focal.get(dIX) == null) {
				focalSize(dIX + 2);
			}
			focal.get(dIX).add(child);
			checkChildD(child);
		} else if (child.getG() < incumbent.getG()) {
			int ix = incumbent.getHeap2Index();
			int dIX = child.getD();

			if (ix != Heapable.NO_POS) {
				open.removeAt(ix);
				focal.get(dIX).remove(incumbent);
			}
			closed.remove(incumbent);
			open.insert(child);
			closed.put(child.getState().getKey(), child);

			assert (focal.get(dIX) != null);
			focal.get(dIX).add(child);
			checkChildD(child);
		}
		if (incumbent != null)
			l.incrDup();
	}

	private void setBestDBucket() {
		double bestBackupF = Double.MAX_VALUE;
		
		double fBound = open.peek().getF() * bound;
		for (int i = 0; i < focal.size(); i++) {
			if (focal.get(i) == null)
				continue;
			else if (focal.get(i).isEmpty())
				continue;
			if (focal.get(i).peek().getF() < fBound) {
				bestDBucket = i;
				break;
			} else {
				double current = focal.get(i).peek().getF();
				if(current < bestBackupF){
					bestBackupF = current;
				}
			}
		}
//		
//		if(oldBucket == this.bestDBucket){
//			this.bestDBucket = backupBucket;
//		}
		
	}

	
	/*
	private void upBestDBucket() {
		int oldBucket = this.bestDBucket;
		double fBound = open.peek().getF() * bound;
		for (int i = oldBucket; i < focal.size(); i++) {
			if (focal.get(i) == null)
				continue;
			else if (focal.get(i).isEmpty())
				continue;
			if (focal.get(i).peek().getF() < fBound) {
				bestDBucket = i;
				break;
			}
		}
		if(oldBucket == this.bestDBucket){
			setBestDBucket();
		}
	}
	 */
	@Override
	protected void cleanup() {
		open.clear();
		closed.clear();
		focal.clear();
	}

	@Override
	public SearchAlgorithm clone() {
		return new ASEpsilon(prob, l.clone(), bound);
	}

	@Override
	public void reset() {
		cleanup();
	}

	protected Solution processNode(DQSearchNode current) {
		if (current.getState().isGoal()) {
			if (getIncumbent() == null
					|| getIncumbent().getCost() > current.getG())
				return new Solution(current, current.getG(), l.getDuration(),
						current.pathLength(), l.getExpansions(),
						l.getGenerations(), l.getDuplicates());
		} else {
			ArrayList<? extends DQSearchNode> children = current.expand();
			l.incrExp();
			Iterator<? extends DQSearchNode> childIter = children.iterator();
			while (childIter.hasNext()) {
				l.incrGen();
				DQSearchNode next = childIter.next();
				considerChild(next);
			}
		}
		return null;
	}
//
//	private double peekF() {
//		if (open.isEmpty())
//			return Double.MAX_VALUE;
//		else
//			return open.peek().getF();
//	}

	protected void setIncumbent() {
		l.startClock();
		Solution goal = null;
		while (!open.isEmpty() && goal == null && l.keepGoing()) {
			//get head open's f value
			setBestDBucket();
			//remove from focal
			DQSearchNode current = focal.get(bestDBucket).poll();
			//remove from open
			open.removeAt(current.getHeap2Index());
			goal = processNode(current);

			
		}
		l.endClock();
		if (goal != null){
			setIncumbent(goal);
			double minFBound = open.peek().getF() * bound;
			if(minFBound < goal.getCost()){
				throw new RuntimeException("Bad solution");
			}
	
		}
	}

	private void focalSize(int ix) {
		focal.ensureCapacity(ix + 1);
		for (int i = focal.size(); i < ix; i++) {
			focal.add(new MinHeap<DQSearchNode>(cmp));
		}
	}

	@Override
	public ArrayList<SearchState> solve() {
		DQSearchNode i = new DQSearchNode(null, prob.getInitial(), 0);
		open.insert(i);
		closed.put(i.getState().getKey(), i);
		bestDBucket = i.getD();
		focal.get(bestDBucket).add(i);

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
			return finalPath;
		}
	}

}
