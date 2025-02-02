package org.cwilt.search.algs.experimental;
import org.cwilt.search.algs.basic.Beam;import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;
public class RandomProbeBeam extends RandomProbe {

	private final int beamWidth;

	public RandomProbeBeam(org.cwilt.search.search.SearchProblem initial, Limit l, int beamWidth,
			int probeDepth) {
		super(initial, l, probeDepth * beamWidth);
		this.beamWidth = beamWidth;
	}

	public RandomProbeBeam clone() {
		super.checkClone(RandomProbeBeam.class.getCanonicalName());
		return new RandomProbeBeam(prob, l, beamWidth, super.probeLimit / beamWidth);
	}

	@Override
	protected SearchAlgorithm getSolver(org.cwilt.search.search.SearchProblem s, Limit l) {
		return new Beam(s, l, beamWidth);
	}




}
