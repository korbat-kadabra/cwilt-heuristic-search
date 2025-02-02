package org.cwilt.search.algs.experimental;
import org.cwilt.search.algs.basic.Beam;import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchNode;import org.cwilt.search.utils.experimental.LazyHashMap;
public class LazyBeam extends Beam implements Cloneable{

	private final int clsz;
	
	public LazyBeam(org.cwilt.search.search.SearchProblem initial, Limit l, int beamWidth, int clsz) {
		super(initial, l, beamWidth);
		super.closed = new LazyHashMap<Object, SearchNode>(clsz * beamWidth);
		this.clsz = clsz;
	}

	public LazyBeam clone(){
		super.checkClone(LazyBeam.class.getCanonicalName());
		return new LazyBeam(prob, l.clone(), beamWidth, clsz);
	}
}
