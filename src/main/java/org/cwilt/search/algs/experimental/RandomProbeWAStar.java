package org.cwilt.search.algs.experimental;
import org.cwilt.search.algs.basic.bestfirst.WAStar;import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;
public class RandomProbeWAStar extends RandomProbe{

	private final double weight;
	
	public RandomProbeWAStar(org.cwilt.search.search.SearchProblem initial, Limit l, 
			int probeLimit, double weight) {
		super(initial, l, probeLimit);
		this.weight = weight;
	}

	@Override
	protected SearchAlgorithm getSolver(org.cwilt.search.search.SearchProblem s, Limit l) {
		return new WAStar(s, l, weight);
	}

	@Override
	public SearchAlgorithm clone() {
		super.checkClone(RandomProbeWAStar.class.getCanonicalName());
		return new RandomProbeWAStar(prob, l.clone(), probeLimit, weight);
	}

}
