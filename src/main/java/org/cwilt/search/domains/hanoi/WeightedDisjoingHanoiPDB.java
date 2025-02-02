package org.cwilt.search.domains.hanoi;
public class WeightedDisjoingHanoiPDB extends DisjointHanoiPDB {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3709804017227405223L;
	private final double[] weights;
	
	public WeightedDisjoingHanoiPDB(double[] costs, int[] sizes, boolean[] skipped, double[] weights) throws ClassNotFoundException {
		super(costs, sizes, skipped);
		this.weights = weights;
	}

	public double getH(HanoiState hanoiState) {
		double h = 0;
		for (int i = 0; i < pdbs.length; i++) {
			if (abs[i] == null)
				continue;
			Object abstractedState = abs[i].abstractState(hanoiState);
			h += pdbs[i].getH(abstractedState) * weights[i];
		}

		return h;
	}

	public int getD(HanoiState hanoiState) {
		int h = 0;
		for (int i = 0; i < pdbs.length; i++) {
			if (abs[i] == null)
				continue;
			Object abstractedState = abs[i].abstractState(hanoiState);
			h += pdbs[i].getD(abstractedState) * weights[i];
		}
		return h;
	}

}
