package org.cwilt.search.domains.kiva.path.simplified;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;

import org.cwilt.search.domains.kiva.drive.Drive;
import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.kiva.path.temporal.TemporalMove;
import org.cwilt.search.domains.kiva.path.timeless.Move;
import org.cwilt.search.domains.kiva.path.timeless.NavigationProblem;
import org.cwilt.search.domains.kiva.problem.KivaProblem;
import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
import org.cwilt.search.domains.multiagent.problem.ReservationTable.Reservation;

public class SimpleMove extends TemporalMove implements
		org.cwilt.search.domains.multiagent.problem.AgentState, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2179382900326016875L;
	private static final Rectangle2D r1 = new Rectangle2D.Double(SQ_POS
			+ SQ_SIZE, SQ_POS, SQ_POS, SQ_SIZE);
	private static final Rectangle2D r2 = new Rectangle2D.Double(2 * SQ_POS
			+ SQ_SIZE, SQ_POS, SQ_POS, SQ_SIZE);

	public SimpleMove(GridCell startP, GridCell endP, int time,
			NavigationProblem problem) {
		super(startP, endP, DIRECTION.HORIZONTAL, DIRECTION.HORIZONTAL, time,
				problem);
	}

	@Override
	protected void getChildren(ArrayList<Move> children) {
		super.getSimpleChildren(children);
	}

	@Override
	public Object getKey() {
		return key;
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

		d.translate(startPosition.x * GridCell.CELL_SIZE, startPosition.y
				* GridCell.CELL_SIZE);

		if (startPosition.y < endPosition.y) {
			d.rotate(Math.toRadians(90));
		} else if (startPosition.y > endPosition.y) {
			d.rotate(Math.toRadians(270));
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

	public boolean canDo(KivaProblem p, Drive d, GridCell goal) {
		boolean atGoal = false;
		if (goal == null)
			atGoal = false;
		else if (goal.equals(endPosition))
			atGoal = true;

		return super.canDo(p, d, goal)
				&& (super.endPosition.canEnter() || (atGoal));
	}

	@Override
	public double h() {
		return endPosition.manhattan(super.getProblem().goal);
	}

	@Override
	public MultiagentVertex getEndVertex() {
		return super.endPosition;
	}

	@Override
	public MultiagentVertex getStartVertex() {
		return super.startPosition;
	}

	@Override
	public ArrayList<Reservation> getReservations() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MOVETYPE getMoveType() {
		return MOVETYPE.SIMPLE;
	}
}
