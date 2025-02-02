package org.cwilt.search.algs.experimental;
import java.util.WeakHashMap;

import org.cwilt.search.algs.basic.Beam;
import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchNode;
public class WeakBeam extends Beam implements Cloneable{

	public WeakBeam(org.cwilt.search.search.SearchProblem initial, Limit l, int beamWidth) {
		super(initial, l, beamWidth);
		super.closed = new WeakHashMap<Object, SearchNode>();
	}

	public WeakBeam clone(){
		return new WeakBeam(prob, l.clone(), beamWidth);
	}
}
