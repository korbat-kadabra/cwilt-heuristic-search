package org.cwilt.search.domains.tiles;
import java.io.Serializable;

public class PackedTilesPDB implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3334102327870281203L;
	
	private static int factorial(int number){
		int fac = 1;
		for(int i = number; i > 1; i--){
			fac = i * fac;
		}
		return fac;
	}
	
	private final PatternValues[] pdb;
	
	private PackedTilesPDB(TileStateAbstractor a, TileState initial) {
		int abstractedTiles = 0;
		int nTiles = initial.prob.getAcross() * initial.prob.getDown();
		for (int i = 0; i < nTiles; i++) {
			if (a.isAbstracted(i))
				abstractedTiles++;
		}
		int nValues = factorial(nTiles - abstractedTiles);

		pdb = new PatternValues[nValues];
		for(int i = 0; i < nValues; i++){
			pdb[i] = new PatternValues();
		}
	}

	private static final class PatternValues implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 6815352680554786439L;
		@SuppressWarnings("unused")
		private final double[] hValues = new double[65536];
		@SuppressWarnings("unused")
		private final int[] dValues = new int[65536];
	}

}
