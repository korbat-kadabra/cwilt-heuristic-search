package org.cwilt.search.domains.kiva.path.timeless;
import java.util.ArrayList;

import org.cwilt.search.domains.kiva.drive.Drive;
import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.kiva.problem.KivaProblem;
public class Wait extends Move {

	protected Wait(GridCell start, GridCell end, DIRECTION dir, NavigationProblem problem) {
		super(start, end, dir, dir, problem);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void getChildren(ArrayList<Move> children) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canDo(KivaProblem p, Drive d, GridCell goal) {
		return true;
	}

	@Override
	public Object getKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MOVETYPE getMoveType() {
		return MOVETYPE.TIMELESS;
	}


}
