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
 * The TemporalTurnA class defines the first half of a 90 degree turn.
 * 
 * @author Carmen St. Jean
 * 
 */
public class TemporalTurnA extends TemporalMove {

	public TemporalTurnA(GridCell pos, DIRECTION initial, int time, NavigationProblem p) {
		super(pos, pos, initial, convert(initial), time, p);
	}

	private static final DIRECTION convert(DIRECTION d) {
		if (d == DIRECTION.HORIZONTAL)
			return DIRECTION.TURN_H;
		else if (d == DIRECTION.VERTICAL)
			return DIRECTION.TURN_V;
		else
			throw new RuntimeException("Invalid turn type for a temporal turn A");
	}

	public boolean canDo(KivaProblem p, Drive d) {
		return p.res.checkReservation(super.startPosition, key.time, d)
				&& p.res.checkReservation(super.endPosition, key.time, d)
				&& p.res.checkReservation(super.startPosition, key.time + 1, d)
				&& p.res.checkReservation(super.endPosition, key.time + 1, d);
	}

	public void reserveSpace(KivaProblem p, Drive d) {
		p.res.reserveSpace(super.startPosition, key.time, d);
		p.res.reserveSpace(super.endPosition, key.time, d);
		// reserve the time and space for the second part of the turn
		p.res.reserveSpace(super.startPosition, key.time + 1, d);
		p.res.reserveSpace(super.endPosition, key.time + 1, d);
	}

	/**
	 * Prepares the transformation matrix for the drive based on this turn A move
	 * and the current time frame step.
	 * 
	 * @param transform
	 *            The drive's transformation matrix where the move's transformations
	 *            will be added to.
	 * @param frame
	 *            The current time frame step.
	 */
	@Override
	public void prepMatrix(AffineTransform transform, int frame) {
		double theta = 0;

		if (startDir == DIRECTION.VERTICAL) {
			theta = 90;
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
		children.add(new TemporalTurnB(super.startPosition, super.endDir, key.time + 1, super.getProblem()));
	}

	/**
	 * Draws the temporal turn A move of a path; which is to draw nothing.
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
