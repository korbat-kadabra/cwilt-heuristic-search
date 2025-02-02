package org.cwilt.search.algs.experimental;
import java.io.PrintStream;
import java.util.HashMap;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchNode;
public class DCLazyBeam extends LazyBeam {

	private HashMap<Object, SearchNode> globalClosed;
	private int globalDuplicates;
	private final int clsz;
	
	public DCLazyBeam(org.cwilt.search.search.SearchProblem initial, Limit l, int beamWidth, int clsz) {
		super(initial, l, beamWidth, clsz);
		globalDuplicates = 0;
		globalClosed = new HashMap<Object, SearchNode>();
		this.clsz = clsz;
	}
	
	@Override
	protected void addToClosed(SearchNode child){
		super.addToClosed(child);
		globalClosed.put(child.getState().getKey(), child);
	}
	
	@Override
	protected SearchNode getIncumbentNode(SearchNode child){
		SearchNode globalIncumbent = globalClosed.get(child.getState().getKey());
		if(globalIncumbent != null)
			globalDuplicates ++;
		return super.getIncumbentNode(child);
	}
	
	@Override
	public void printExtraData(PrintStream ps){
		super.printExtraData(ps);
		printPair(ps, "global duplicates", new Integer(globalDuplicates));
	}
	public DCLazyBeam clone(){
		super.checkClone(DCLazyBeam.class.getCanonicalName());
		return new DCLazyBeam(prob, l.clone(), beamWidth, clsz);
	}
}
