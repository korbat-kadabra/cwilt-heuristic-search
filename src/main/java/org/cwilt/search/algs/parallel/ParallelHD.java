package org.cwilt.search.algs.parallel;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchNodeLong;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.utils.basic.Heapable;
/**
 * This algorithm expands nodes on h and d in parallel, but lets the two search
 * algorithms do a limited amount of interacting with one another.
 * 
 * @author cmo66
 * 
 */
public class ParallelHD extends org.cwilt.search.search.SearchAlgorithm {

	private final ConcurrentHashMap<Object, SearchNodeLong> closed;
	private final ConcurrentSkipListSet<SearchNodeLong> hOpen;
	private final ConcurrentSkipListSet<SearchNodeLong> dOpen;

	public ParallelHD(SearchProblem prob, Limit l) {
		super(prob, l);
		prob.setCalculateD();

		this.closed = new ConcurrentHashMap<Object, SearchNodeLong>();
		this.hOpen = new ConcurrentSkipListSet<SearchNodeLong>(
				new SearchNodeLong.HLComparator());
		this.dOpen = new ConcurrentSkipListSet<SearchNodeLong>(
				new SearchNodeLong.DLComparator());
	}

	@Override
	public void reset() {
		this.cleanup();
	}

	@Override
	protected void cleanup() {
		closed.clear();
		hOpen.clear();
		dOpen.clear();
	}

	@Override
	public SearchAlgorithm clone() {
		return new ParallelHD(prob, l.clone());
	}

	@Override
	public ArrayList<SearchState> solve() {
		ExpandingThread hThread = new ExpandingThread(hOpen, dOpen);
		ExpandingThread dThread = new ExpandingThread(dOpen, hOpen);

		SearchNodeLong initial = SearchNodeLong.makeInitial(prob.getInitial());
		l.startClock();
		hOpen.add(initial);
		dOpen.add(initial);
		closed.put(initial.getState().getKey(), initial);
		// heap index of 1 denotes that it is on an open list and has not been
		// expanded.
		// heap index of -1 denotes that it is has been expanded, and can be
		// skipped.
		initial.concurrentSetHeapIndex(1);

		Thread ht = new Thread(hThread);
		Thread dt = new Thread(dThread);
		
		ht.start();
		dt.start();
		
		try {
			ht.join();
			dt.join();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		l.endClock();

		if (super.getIncumbent() == null) {
			return null;
		} else {
			return super.getIncumbent().reconstructPath();
		}
	}
	
	private final boolean keepGoing(){
		if(this.getIncumbent() != null)
			return false;
		if(hOpen.isEmpty() && dOpen.isEmpty()){
			return false;
		}
		if(!l.keepGoing()){
			return false;
		}
		return true;
	}

	private final class ExpandingThread implements Runnable {
		private final ConcurrentSkipListSet<SearchNodeLong> open;
		private final ConcurrentSkipListSet<SearchNodeLong> other;
		
		public ExpandingThread(ConcurrentSkipListSet<SearchNodeLong> open,
				ConcurrentSkipListSet<SearchNodeLong> other) {
			this.open = open;
			this.other = other;
		}

		
		@Override
		public void run() {
			while (keepGoing()) {
				SearchNodeLong next = open.pollFirst();
				if (next == null)
					continue;
				int nextIndex = next.concurrentGetHeapIndex();
				if (nextIndex == Heapable.NO_POS)
					continue;

				if (next.getState().isGoal()) {
					setIncumbent(next);
					return;
				}
				
				next.concurrentSetHeapIndex(Heapable.NO_POS);
				ArrayList<? extends SearchNodeLong> children = next.expand();
				l.synchronizedIncrExp();
				l.synchronizedIncrGen(children.size());
				for (SearchNodeLong child : children) {
					SearchNodeLong incumbent = closed.get(child.getState().getKey());
					if (incumbent != null && incumbent.concurrentGetHeapIndex() == Heapable.NO_POS) {
						l.synchronizedIncrDup();
						continue;
					} else {
						child.setHeapIndex(1);
						closed.put(child.getState().getKey(), child);
						open.add(child);
						other.add(child);
					}
				}
			}
		}
	}

}
