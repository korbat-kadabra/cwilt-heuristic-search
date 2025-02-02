/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.cwilt.search.domains.kiva.path.temporal;
import java.util.ArrayList;

import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.kiva.path.timeless.Move;
import org.cwilt.search.domains.kiva.path.timeless.NavigationProblem;
/**
 * The TemporalWait class defines a movement where the drive sits idle.
 * 
 * @author Carmen St. Jean
 * 
 */
public class TemporalWait extends TemporalMove {
    public TemporalWait(GridCell pos, DIRECTION dir, int time, NavigationProblem p) {
        super(pos, pos, dir, dir, time, p);
    }

    @Override
    protected void getChildren(ArrayList<Move> children) {
    	super.getStraightChildren(children);
    	super.getWaitChild(children);
    }
    @Override
    public boolean isWait(){
    	return true;
    }
}
