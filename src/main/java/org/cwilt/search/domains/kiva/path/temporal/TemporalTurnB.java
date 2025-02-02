/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.cwilt.search.domains.kiva.path.temporal;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import org.cwilt.search.domains.kiva.drive.Drive;
import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.kiva.path.timeless.Move;
import org.cwilt.search.domains.kiva.path.timeless.NavigationProblem;
import org.cwilt.search.domains.kiva.problem.KivaProblem;
/**
 * The TemporalTurnB class defines the second half of a 90 degree turn.
 * 
 * @author Carmen St. Jean
 * 
 */
public class TemporalTurnB extends TemporalMove {

    public TemporalTurnB(GridCell pos, DIRECTION dir, int time, NavigationProblem p) {
        super(pos, pos, dir, convert(dir), time, p);
    }

    private static DIRECTION convert(DIRECTION d) {
        if (d == DIRECTION.TURN_H) {
            return DIRECTION.VERTICAL;
        } else if(d == DIRECTION.TURN_V){
            return DIRECTION.HORIZONTAL;
        } else
        	throw new RuntimeException("invalid directoion in a Temporal Turn B");
    }

    public void reserveSpace(KivaProblem p, Drive d) {
        return;
    }
    
	/**
	 * Prepares the transformation matrix for the drive based on this turn B
	 * move and the current time frame step.
	 * 
	 * @param transform
	 *            The drive's transformation matrix where the move's
	 *            transformations will be added to.
	 * @param frame
	 *            The current time frame step.
	 */
    @Override
    public void prepMatrix(AffineTransform transform, int frame) {
        double theta = 45;

        if (startDir == DIRECTION.TURN_V) {
            theta = 135;
        }

        theta += TemporalMove.interpolate(0, 45, frame);

        transform.setToIdentity();
        transform.translate(GridCell.CELL_SIZE / 2, GridCell.CELL_SIZE / 2);
        transform.translate(GridCell.CELL_SIZE * startPosition.x, GridCell.CELL_SIZE * startPosition.y);
        transform.rotate(Math.toRadians(theta));
        transform.translate(-GridCell.CELL_SIZE / 2, -GridCell.CELL_SIZE / 2);
    }

    @Override
    protected void getChildren(ArrayList<Move> children) {
        super.getStraightChildren(children);
        super.getWaitChild(children);
    }

	/**
	 * Draws the temporal turn B move of a path; which is to draw nothing.
	 * 
	 * @param d
	 *            The Graphics2D object for drawing.
	 * @param t
	 *            The current time step.
	 * @param f
	 *            The current frame step.
	 */
    @Override
    public void draw(Graphics2D d, int t, int f) {
        // do nothing
    }
}
