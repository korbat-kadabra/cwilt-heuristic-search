package org.cwilt.search.domains.tiles;
public class AbstractedTileState extends TileState {



	/**
	 * 
	 */
	private static final long serialVersionUID = 8296041273586608750L;

	public AbstractedTileState(TileProblem p) {
		super(p, false);
	}

	
	public AbstractedTileState(TileState original, DIRECTION d) {
		super(original, d);
	}


	@Override
	protected TileState move(DIRECTION d){
		return new AbstractedTileState(this, d);
	}

	@Override
	protected double calculateMD(TileState original, int id){
		return original.prob.mdFromScratch(this);
	}
	
	public boolean isGoal(){
		for(int i = 0; i < super.b.c.length; i++){
			if(!super.prob.getCost().isAbstracted(super.b.c[i])){
				if(super.b.c[i] != i)
					return false;
			}
		}
		return true;
	}
	
}

