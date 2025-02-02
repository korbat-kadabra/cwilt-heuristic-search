package org.cwilt.search.domains.kiva.path.temporal;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import org.cwilt.search.domains.kiva.drive.Drive;
import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.kiva.path.simplified.SimpleMove;
import org.cwilt.search.domains.kiva.path.simplified.SimpleWait;
import org.cwilt.search.domains.kiva.path.timeless.Move;
import org.cwilt.search.domains.kiva.path.timeless.NavigationProblem;
import org.cwilt.search.domains.kiva.problem.KivaProblem;
/**
 * The TemporalMove class abstractly defines movement for one time step of a
 * drive's path. It defines the preparation the transformation matrix for
 * positioning the drive at each time step and the drawing of the move's path.
 * 
 * @author Carmen St. Jean
 * 
 */
public abstract class TemporalMove extends Move {
	protected static final double HEIGHT = 0.35;
	protected static final double SQ_POS = (1 - HEIGHT) * GridCell.CELL_SIZE
			/ 2;
	protected static final double SQ_SIZE = HEIGHT * GridCell.CELL_SIZE;

	protected static final Rectangle2D centerSquare = new Rectangle2D.Double(
			SQ_POS, SQ_POS, SQ_SIZE, SQ_SIZE);

	public boolean canDo(KivaProblem p, Drive d, GridCell goal) {
		return super.startPosition.canLeave()
				&& p.res.checkReservation(super.startPosition, key.time, d)
				&& p.res.checkReservation(super.endPosition, key.time, d);
	}

	protected static final Color pathVisitedColor = new Color(255, 140, 0, 75);
	protected static final Color pathFutureColor = new Color(255, 140, 0, 255);

	public void reserveSpace(KivaProblem p, Drive d) {
		assert(p != null);
		assert(d != null);
		assert(key != null);
		assert(startPosition != null);
		assert(endPosition != null);
		assert(p.res != null);
		p.res.reserveSpace(super.startPosition, key.time, d);
		p.res.reserveSpace(super.endPosition, key.time, d);
	}

	public void releaseSpace(KivaProblem p, Drive d) {
		p.res.releaseReservation(super.startPosition, key.time, d);
		p.res.releaseReservation(super.endPosition, key.time, d);
	}

	public final TemporalMoveKey key;

	protected TemporalMove(GridCell start, GridCell end, DIRECTION startD,
			DIRECTION endD, int time, NavigationProblem problem) {
		super(start, end, startD, endD, problem);
		this.key = new TemporalMoveKey(end, time, endD);
	}

	/**
	 * Prepares the transformation matrix for the drive based on this move and
	 * the current time frame step.
	 * 
	 * @param transform
	 *            The drive's transformation matrix where the move's
	 *            transformations will be added to.
	 * @param frame
	 *            The current time frame step.
	 */
	public void prepMatrix(AffineTransform transform, int frame) {
		double newX = interpolate(startPosition.x, endPosition.x, frame);
		double newY = interpolate(startPosition.y, endPosition.y, frame);

		assert (startDir == endDir);

		int theta = 0;

		if (startDir == DIRECTION.VERTICAL) {
			theta = 90;
		}

		transform.setToIdentity();
		transform.translate(GridCell.CELL_SIZE / 2, GridCell.CELL_SIZE / 2);
		transform.translate(GridCell.CELL_SIZE * newX, GridCell.CELL_SIZE
				* newY);
		transform.rotate(Math.toRadians(theta));
		transform.translate(-GridCell.CELL_SIZE / 2, -GridCell.CELL_SIZE / 2);
	}

	public static final double interpolate(double start, double end, int frame) {
		double propDone = ((double) frame) / ((double) KivaProblem.FRAME_RATE);
		return start + (propDone * (end - start));
	}

	/**
	 * Draws the temporal move of a path.
	 * 
	 * @param d
	 *            The Graphics2D object for drawing.
	 * @param t
	 *            The current time step.
	 * @param f
	 *            The current frame step.
	 */
	public void draw(Graphics2D d, int t, int f) {
		AffineTransform oldTransform = d.getTransform();

		d.translate(endPosition.x * GridCell.CELL_SIZE, endPosition.y
				* GridCell.CELL_SIZE);

		if (key.time >= t) {
			d.setColor(pathFutureColor);
			d.fill(centerSquare);
		} else {
			d.setColor(pathVisitedColor);
			d.fill(centerSquare);
		}

		d.setTransform(oldTransform);
	}

	protected boolean isTemporal() {
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + key.time;
		return result;
	}

	protected final void getStraightChildren(ArrayList<Move> children) {
		// forward/back children
		if (this.endDir == DIRECTION.HORIZONTAL) {
			if (endPosition.canLeft()) {
				children.add(new TemporalSlow(endPosition, endPosition.left(),
						endDir, key.time + 1, super.getProblem()));
			}
			if (endPosition.canRight()) {
				children.add(new TemporalSlow(endPosition, endPosition.right(),
						endDir, key.time + 1, super.getProblem()));
			}
		} else if (this.endDir == DIRECTION.VERTICAL) {
			if (endPosition.canUp()) {
				children.add(new TemporalSlow(endPosition, endPosition.up(),
						endDir, key.time + 1, super.getProblem()));
			}
			if (endPosition.canDown()) {
				children.add(new TemporalSlow(endPosition, endPosition.down(),
						endDir, key.time + 1, super.getProblem()));
			}
		}
	}

	protected final void getSimpleChildren(ArrayList<Move> children) {
		// forward/back children
		if (endPosition.canLeft() && !endPosition.left().isBlocked()) {
			children.add(new SimpleMove(endPosition, endPosition.left(),
					key.time + 1, super.getProblem()));
		}
		if (endPosition.canRight() && !endPosition.right().isBlocked()) {
			children.add(new SimpleMove(endPosition, endPosition.right(),
					key.time + 1, super.getProblem()));
		}
		if (endPosition.canUp() && !endPosition.up().isBlocked()) {
			children.add(new SimpleMove(endPosition, endPosition.up(),
					key.time + 1, super.getProblem()));
		}
		if (endPosition.canDown() && !endPosition.down().isBlocked()) {
			children.add(new SimpleMove(endPosition, endPosition.down(),
					key.time + 1, super.getProblem()));
		}
		children.add(new SimpleWait(endPosition, key.time + 1, super
				.getProblem()));
	}

	protected final void getFastChildren(ArrayList<Move> children) {
		throw new UnsupportedOperationException();
	}

	protected final void getWaitChild(ArrayList<Move> children) {
		// wait child
		children.add(new TemporalWait(endPosition, endDir, key.time + 1, super
				.getProblem()));
	}

	protected final void getTurnChild(ArrayList<Move> children) {
		children.add(new TemporalTurnA(endPosition, endDir, key.time + 1, super
				.getProblem()));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TemporalMove other = (TemporalMove) obj;
		if (key.time != other.key.time) {
			return false;
		}
		return true;
	}

	public Object getKey() {
		return key;
	}
	
	@Override
	public MOVETYPE getMoveType(){
		return MOVETYPE.TEMPORAL;
	}

	
	public int getTime(){
		return this.key.time;
	}
	public static final class TemporalMoveKey {
		public final GridCell end;
		public final int time;
		public final DIRECTION direction;
	
		public TemporalMoveKey(GridCell end, int time, DIRECTION direction) {
			this.end = end;
			this.time = time;
			this.direction = direction;
		}
	
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((direction == null) ? 0 : direction.hashCode());
			result = prime * result + ((end == null) ? 0 : end.hashCode());
			result = prime * result + time;
			return result;
		}
	
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TemporalMoveKey other = (TemporalMoveKey) obj;
			if (direction != other.direction)
				return false;
			if (end == null) {
				if (other.end != null)
					return false;
			} else if (!end.equals(other.end))
				return false;
			if (time != other.time)
				return false;
			return true;
		}
	
	}
	public boolean isWait() {
		return false;
	}
}
