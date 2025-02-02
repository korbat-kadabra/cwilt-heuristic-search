package org.cwilt.search.domains.hanoi.pdb_builder;
import java.io.IOException;

import org.cwilt.search.domains.hanoi.HanoiPDB;
import org.cwilt.search.domains.hanoi.HanoiProblem;
import org.cwilt.search.domains.hanoi.HanoiState;
public class HanoiMappedPDBProblem extends HanoiProblem {
	public HanoiMappedPDBProblem(String problemPath, String costNames, HanoiPDB pdbArgs, int[] keptDisks) throws IOException {
		super(problemPath, costNames, new HanoiPDB[] {pdbArgs});
		// the PDB should be the same size as the regular one
		assert(pdbs[0].getnDisks() == keptDisks.length);
		this.keptDisks = keptDisks;
	}

	public HanoiMappedPDBProblem(HanoiProblem parent, HanoiState initial, int[] keptDisks) {
		super(parent, initial);
		// the PDB should be the same size as the regular one
		assert(pdbs[0].getnDisks() == keptDisks.length);
		this.keptDisks = keptDisks;
	}

	
	private final int[] keptDisks;
	

	public double calculateH(HanoiState hanoiState) {
		double h = 0;
		for (int i = 0; i < pdbs.length; i++) {
			Object abstractedState = hanoiState.abstractState(keptDisks);
			h += pdbs[i].getH(abstractedState);
		}

		return h;
	}

	public int calculateD(HanoiState hanoiState) {
		int h = 0;
		for (int i = 0; i < pdbs.length; i++) {
			Object abstractedState = hanoiState.abstractState(keptDisks);
			h += pdbs[i].getD(abstractedState);
		}
		return h;
	}

}
