package org.cwilt.search.domains.kiva.path.temporal;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.cwilt.search.domains.kiva.drive.Drive;
import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.kiva.path.timeless.Move;
import org.cwilt.search.domains.kiva.path.timeless.NavigationProblem;
import org.cwilt.search.domains.kiva.problem.KivaProblem;
/**
 * The TemporalSlow class defines straight movement, either horizontally or
 * vertically, to the adjacent cell.
 * 
 * @author Carmen St. Jean
 * 
 */
public class TemporalSlow extends TemporalMove {
    private static final Rectangle2D r1 = new Rectangle2D.Double(
            SQ_POS + SQ_SIZE, SQ_POS, SQ_POS, SQ_SIZE);
    private static final Rectangle2D r2 = new Rectangle2D.Double(
            2 * SQ_POS + SQ_SIZE, SQ_POS, SQ_POS, SQ_SIZE);

    public TemporalSlow(GridCell start, GridCell end, DIRECTION dir, int time, NavigationProblem p) {
        super(start, end, dir, dir, time, p);

        if (dir == DIRECTION.HORIZONTAL) {
            assert (start.y == end.y);
            assert (Math.abs(start.x - end.x) == 1);
        }

        if (dir == DIRECTION.VERTICAL) {
            assert (start.x == end.x);
            assert (Math.abs(start.y - end.y) == 1);
        }
    }

    @Override
    protected void getChildren(ArrayList<Move> children) {
        super.getStraightChildren(children);
        super.getWaitChild(children);
        children.add(new TemporalTurnA(endPosition, endDir, key.time + 1, super.getProblem()));
    }
	public boolean canDo(KivaProblem p, Drive d, GridCell goal) {
		boolean atGoal = false;
		if(super.getProblem() == null)
			atGoal = false;
		else if( super.getProblem().goal.equals(endPosition))
			atGoal = true;
		
		
		return super.canDo(p, d, goal) && (super.endPosition.canEnter() || (atGoal ));
	}
	/**
	 * Draws the temporal slow move of a path.
	 * 
	 * @param d
	 *            The Graphics2D object for drawing.
	 * @param t
	 *            The current time step.
	 * @param f
	 *            The current frame step.
	 */
    public void draw(Graphics2D d, int t, int f) {
        super.draw(d, t, f);
        AffineTransform oldTransform = d.getTransform();

        d.translate(GridCell.CELL_SIZE / 2, GridCell.CELL_SIZE / 2);

        d.translate(startPosition.x * GridCell.CELL_SIZE, startPosition.y * GridCell.CELL_SIZE);

        if (startDir == DIRECTION.VERTICAL) {
            if (startPosition.y < endPosition.y) {
                d.rotate(Math.toRadians(90));
            } else {
                d.rotate(Math.toRadians(270));
            }
        } else if (startPosition.x > endPosition.x) {
            d.rotate(Math.toRadians(180));
        }

        d.translate(-GridCell.CELL_SIZE / 2, -GridCell.CELL_SIZE / 2);

        if (key.time > t || (key.time == t && f < KivaProblem.FRAME_RATE / 3)) {
            d.setColor(pathFutureColor);
            d.fill(r1);
            d.fill(r2);
        } else if (key.time == t && f < 2 * KivaProblem.FRAME_RATE / 3) {
            d.setColor(pathVisitedColor);
            d.fill(r1);
            d.setColor(pathFutureColor);
            d.fill(r2);
        } else {
            d.setColor(pathVisitedColor);
            d.fill(r1);
            d.fill(r2);
        }

        d.setTransform(oldTransform);
    }
}
