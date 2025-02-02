package org.cwilt.search.domains.kiva.path.simplified;
import java.io.Serializable;

import org.cwilt.search.domains.kiva.drive.Drive;
import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.kiva.path.timeless.NavigationProblem;
import org.cwilt.search.domains.kiva.problem.KivaProblem;
public class SimpleWait extends SimpleMove implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8081410653290076586L;


	public SimpleWait(GridCell startP, int time, NavigationProblem problem) {
		super(startP, startP, time, problem);
	}

	
	public boolean canDo(KivaProblem p, Drive d) {
		return super.startPosition.canLeave()
				&& p.res.checkReservation(super.startPosition, key.time, d)
				&& p.res.checkReservation(super.endPosition, key.time, d);
	}
    @Override
    public boolean isWait(){
    	return true;
    }

}
