package org.cwilt.search.domains.kiva.drive;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.kiva.path.simplified.SimpleMove;
import org.cwilt.search.domains.kiva.path.simplified.SimpleWait;
import org.cwilt.search.domains.kiva.path.temporal.TemporalMove;
import org.cwilt.search.domains.kiva.path.temporal.TemporalPick;
import org.cwilt.search.domains.kiva.path.temporal.TemporalWait;
import org.cwilt.search.domains.kiva.path.timeless.NavigationProblem;
import org.cwilt.search.domains.kiva.path.timeless.Move.DIRECTION;
import org.cwilt.search.domains.kiva.problem.KivaProblem;
import org.cwilt.search.domains.multiagent.problem.AgentState;
import org.cwilt.search.domains.multiagent.problem.JammedAgentException;
import org.cwilt.search.domains.multiagent.problem.MultiagentTask;
import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
import org.cwilt.search.domains.multiagent.problem.ReservationTable;
import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState.Child;
/**
 * Mobile unit that wants to move around the map
 * 
 * @author chris
 * 
 */

public class Drive implements org.cwilt.search.domains.multiagent.problem.Agent, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6154387903969927131L;
	
	public int nMoves(){
		int nMoves = 0;
		for(TemporalMove m : path){
			if (!m.isWait())
				nMoves ++;
		}
		return nMoves;
	}
	public int nWaits(){
		int nWaits = -1;
		for(TemporalMove m : path){
			if (m.isWait())
				nWaits ++;
		}
		return nWaits;
		
	}
	
	private final KivaProblem kp;

	private static int windowOffset = 0;
	private static final int windowLimit = 5;

	private AffineTransform transform;
	/**
	 * Drive's body
	 */
	private final Rectangle2D r = new Rectangle2D.Double(10, 10, 20, 20);
	/**
	 * Front part of the drive
	 */
	private final Arc2D.Double front = new Arc2D.Double(new Rectangle2D.Double(
			20 - 10 * Math.sqrt(2.0), 20 - 10 * Math.sqrt(2.0),
			20 * Math.sqrt(2.0), 20 * Math.sqrt(2.0)), 45, -90, Arc2D.PIE);
	/**
	 * Back part of the drive
	 */
	private final Arc2D.Double back = new Arc2D.Double(new Rectangle2D.Double(
			20 - 10 * Math.sqrt(2.0), 20 - 10 * Math.sqrt(2.0),
			20 * Math.sqrt(2.0), 20 * Math.sqrt(2.0)), 135, 90, Arc2D.PIE);
	/**
	 * The moves that this drive has committed to following
	 */
	private final ArrayList<TemporalMove> path = new ArrayList<TemporalMove>();

	/**
	 * The drive's pod, but this is currently not being used.
	 */
	private Pod pod;

	/**
	 * Drive's ID, unique per drive.
	 */
	public final int id;
	/**
	 * Tells whether or not the drive should draw its complete path
	 */
	private boolean drawPath = false;

	/**
	 * The JDialog for the popup.
	 */
	private JDialog dialog = null;

	/**
	 * The JPanel for the message in the dialog.
	 */
	private JPanel messagePane = null;

	/**
	 * Time step for the purposes of drawing.
	 */
	private int drawTimeStep;

	/**
	 * Frame step for the purposes of drawing.
	 */
	private int drawFrameStep;

	/**
	 * 
	 * @param id
	 *            ID for the new drive
	 * @param startTime
	 *            Time at which this drive starts out at
	 * @param start
	 *            Drive's start location
	 * @param p
	 *            Problem that this drive is a part of
	 * @param simpleDrive
	 *            Whether this drive should exhibit the full drive mechanics, or
	 *            if if it should just use the simplified mechanics
	 * @param tasks
	 *            Things this drive will have to do
	 */
	public Drive(int id, int startTime, GridCell start, KivaProblem p,
			boolean simpleDrive, ArrayList<MultiagentTask> tasks) {
		this.tasks = tasks;
		this.id = id;
		this.pod = null;
		this.kp = p;

		for (int i = 0; i <= startTime; i++) {
			if (!simpleDrive)
				path.add(new TemporalWait(start, DIRECTION.HORIZONTAL, i, null));
			else
				path.add(new SimpleWait(start, i, null));
			path.get(i).reserveSpace(p, this);
		}
		transform = new AffineTransform();
	}

	/**
	 * Returns the size of the path for this drive.
	 * 
	 * @return The size of the path.
	 */
	public int getPathSize() {
		return path.size();
	}

	/**
	 * Whether or not the path should be drawn.
	 * 
	 * @return True if the path is being drawn for this drive, false otherwise.
	 */
	public boolean isDrawPath() {
		return this.drawPath;
	}

	/**
	 * Sets the boolean for whether or not the path is drawn.
	 * 
	 * @param drawPath
	 *            True if the path should be drawn, false otherwise.
	 */
	public void setDrawPath(boolean drawPath) {
		this.drawPath = drawPath;
	}

	/**
	 * Adds a temporal move to the path for this drive.
	 * 
	 * @param tm
	 *            The new temporal move.
	 * @param p
	 *            The KivaProblem.
	 * @param reserve
	 *            Whether or not to reserve the space along the path
	 */
	public void addTemporalMove(TemporalMove tm, KivaProblem p, boolean reserve) {
		assert (this.getLastMove().getMoveType() == tm.getMoveType());
		if (reserve) {
			if (tm instanceof SimpleMove) {
				boolean goalMatches;
				if (this.getNextTask() == null)
					goalMatches = true;
				else
					goalMatches = (tm.canDo(p, this,
							(GridCell) this.getNextTask().destination));
				boolean startMatches = (tm.canDo(p, this,
						(GridCell) this.currentLocation()));
				assert (goalMatches || startMatches);
			} else {
				assert (tm.canDo(p, this, null));
			}
			tm.reserveSpace(p, this);
		}
		path.add(tm);
		assert (tm.key.time == path.size() - 1);
	}

	/**
	 * Sets the current time step and frame step.
	 * 
	 * @param timeStep
	 *            The current time step.
	 * @param frameStep
	 *            The frame step for the current time step.
	 */
	public void setTime(int timeStep, int frameStep) {
		this.drawTimeStep = timeStep;
		this.drawFrameStep = frameStep;
	}

	/**
	 * Draws the path of the drive.
	 * 
	 * @param d
	 *            The Graphics2D object for drawing.
	 */
	public void drawPath(Graphics2D d) {
		if (drawPath) {
			for (TemporalMove move : path) {
				move.draw(d, drawTimeStep, drawFrameStep);
			}
		}
	}

	/**
	 * Draws the drive according to the move for the current time step and frame
	 * step.
	 * 
	 * @param d
	 *            The Graphics2D object for drawing.
	 */
	public void draw(Graphics2D d) {
		AffineTransform oldTransform = d.getTransform();

		if (drawTimeStep >= path.size()) {
			return;
		}

		path.get(drawTimeStep).prepMatrix(transform, drawFrameStep);

		d.transform(transform);

		d.setColor(Color.CYAN);
		d.fill(front);
		d.setColor(Color.CYAN);
		d.fill(back);

		d.setColor(Color.orange);
		d.fill(r);

		if (pod != null) {
			// code to draw the pod on top of the drive should go here
		}

		d.setTransform(oldTransform);
	}

	@Override
	public String toString() {
		return "Drive " + id;
	}

	/**
	 * Determines whether or not the drive was clicked.
	 * 
	 * @param p
	 *            The point of the mouse click.
	 * @return True if the drive was clicked.
	 */
	public boolean containsClick(Point2D p) {
		Point2D untransformedPoint = new Point2D.Double();

		try {
			transform.inverseTransform(p, untransformedPoint);
		} catch (NoninvertibleTransformException ex) {
			// should not get here
			ex.printStackTrace();
		}

		return (r.contains(untransformedPoint)
				|| front.contains(untransformedPoint) || back
					.contains(untransformedPoint));
	}

	/**
	 * Picks up a pod.
	 * 
	 * @param p
	 *            The pod for this drive to pick up.
	 */
	public void pickUpPod(Pod p) {
		assert (pod == null);
		this.pod = p;
		pod.setDrive(this);
	}

	/**
	 * Gets the last move of this drive's path.
	 * 
	 * @return The drive's last move.
	 */
	public TemporalMove getLastMove() {
		return path.get(path.size() - 1);
	}

	/**
	 * Returns the location where the drive is located.
	 * 
	 * @return The current location of the drive.
	 */
	public GridCell currentLocation() {
		return getLastMove().endPosition;
	}

	/**
	 * Drops of the pod at the current location.
	 */
	public void dropOffPod() {
		assert (pod != null);
		pod.setDrive(null);
		pod.setLocation(this.currentLocation());
	}

	/**
	 * Makes this drive wander for the specified length of time.
	 * 
	 * @param time
	 *            The maximum time step.
	 * @param seed
	 *            The random number seed.
	 * @param p
	 *            The KivaProblem instance.
	 */

	public void wander(int time, int seed, KivaProblem p) {
		new NavigationProblem(this.getLastMove(), null, this, p);

		Random rand = new Random(seed);
		for (int i = 0; i < time; i++) {
			ArrayList<Child> c = this.getLastMove().expand();
			if (c.size() <= 0) {
				throw new RuntimeException("no valid moves at all");
			}

			Iterator<Child> iter = c.iterator();
			while (iter.hasNext()) {
				Child next = iter.next();
				if (next.child instanceof TemporalWait) {
					// iter.remove();
				}
			}
			if (c.size() <= 0) {
				throw new RuntimeException("only valid move is wait");
			}
			int index = rand.nextInt(c.size());
			Child child = c.get(index);
			this.addTemporalMove((TemporalMove) child.child, p, true);
		}
		this.printPath();
	}

	public int getCurrentTime() {
		return this.getLastMove().key.time;
	}

	/**
	 * Prints the path of the drive.
	 */
	public void printPath() {
		for (TemporalMove m : path) {
			System.out.print(m);
		}
	}

	/**
	 * Destroys the dialog window for this drive.
	 */
	private void destroyDialog() {
		if (this.dialog != null) {
			this.dialog.setVisible(false);
			this.dialog.dispose();
			this.dialog = null;
			this.messagePane = null;
			this.drawPath = false;
		}
	}

	/**
	 * Returns a message about about the drive. To be completed further later.
	 * 
	 * @return A message about the drive.
	 */
	public String getDriveMessage() {
		StringBuffer b = new StringBuffer();
		for (MultiagentTask t : tasks) {
			b.append(t);
			b.append("\n");
		}

		return b.toString();
	}

	/**
	 * Toggles the window and the path. If the window and the path are showing,
	 * then they are both hidden. If both are hidden, they are set to visible.
	 * 
	 * @param frame
	 *            The JFrame containing the main application.
	 */
	public void toggleWindowAndPath(JFrame frame) {
		if (this.drawPath) {
			destroyDialog();
		} else {
			this.drawPath = true;

			this.messagePane = new JPanel();
			this.messagePane.add(new JLabel(this.getDriveMessage()));

			this.dialog = new JDialog(frame, this.toString());
			this.dialog.add(this.messagePane);
			this.dialog.setBounds(500 + 25 * windowOffset,
					200 + 25 * windowOffset, 300, 200);
			this.dialog.setVisible(true);

			this.dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					if (isDrawPath()) {
						destroyDialog();
					}
				}

				@Override
				public void windowClosing(WindowEvent e) {
					destroyDialog();
				}
			});

			windowOffset++;

			if (windowOffset == windowLimit) {
				windowOffset = 0;
			}
		}
	}

	/**
	 * Removes all the moves in this drive's path until m is found, and removed.
	 * 
	 * @param m
	 *            Move to back up to
	 */
	@Override
	public void backoffUntil(AgentState m) {
		while (this.getLastMove() != m) {
			TemporalMove next = path.remove(path.size() - 1);
			next.releaseSpace(this.kp, this);
		}
	}

	/**
	 * Removes moves from this drive's path until it finds the specified move,
	 * which is not removed.
	 * 
	 * @param m
	 *            Move to look for
	 */
	public void backoffTemporal(TemporalMove m) {
		while (this.getLastMove() != m) {
			TemporalMove next = path.remove(path.size() - 1);
			next.releaseSpace(this.kp, this);
		}
	}

	@Override
	public AgentState getState() {
		return (AgentState) this.getLastMove();
	}
	
	public ArrayList<MultiagentTask> getTasks(){
		return tasks;
	}
	
	private final ArrayList<MultiagentTask> tasks;
	private int currentTask;

	@Override
	public MultiagentTask getNextTask() {
		if (currentTask >= tasks.size())
			return null;
		return tasks.get(currentTask);
	}

	@Override
	public void addToPath(AgentState vertex) {
		SimpleMove p = (SimpleMove) vertex;
		this.addTemporalMove(p, kp, true);
	}

	@Override
	public boolean canDoTask() {
		MultiagentTask t = getNextTask();

		for (int i = 0; i < t.timeAtDestination; i++) {
			boolean empty = kp.res.checkReservation(
					this.getLastMove().endPosition, this.getCurrentTime() + i
							+ 1, this);
			if (!empty)
				return false;
		}

		return true;
	}

	@Override
	public void doTask(boolean taskCompleted) {
		MultiagentTask t = getNextTask();

		for (int i = 0; i < t.timeAtDestination && !taskCompleted; i++) {
			TemporalMove m = this.getLastMove();
			TemporalMove next;
			if (kp.simpleDrives)
				next = new SimpleWait(m.endPosition, this.getCurrentTime() + 1,
						null);
			else
				next = new TemporalPick(m.endPosition,
						this.getLastMove().endDir, this.getCurrentTime() + 1,
						null);
			this.addTemporalMove(next, this.kp, true);
		}
		t.isDone();
		this.currentTask++;
	}

	@Override
	public ReservationTable getReservationTable() {
		return this.kp.res;
	}

	@Override
	public int getID() {
		return id;
	}

	@Override
	public void failedTask(MultiagentTask t) {
		int currentIndex = tasks.size() - 1;
		while (true) {
			tasks.get(currentIndex).markFailed();
			if (tasks.get(currentIndex) == t) {
				this.currentTask = currentIndex;
				break;
			}
			currentIndex--;
		}
	}

	/**
	 * @param time
	 *            Amount of time to see if this drive can sit at its present
	 *            locations
	 */
	public boolean canAddWaitMoves(int time) {
		for (int i = 0; i < time; i++) {
			TemporalMove m = this.getLastMove();
			TemporalMove next;
			if (kp.simpleDrives)
				next = new SimpleWait(m.endPosition, this.getCurrentTime() + i + 1,
						null);
			else
				next = new TemporalWait(m.endPosition,
						this.getLastMove().endDir, this.getCurrentTime() + i + 1,
						null);
			if (!next
					.canDo(kp, this, (GridCell) this.getNextTask().destination))
				return false;
			// this.addTemporalMove(next, this.kp, true);
		}
		return true;
	}

	@Override
	public void addWaitMoves(int time) throws JammedAgentException {
		for (int i = 0; i < time; i++) {
			TemporalMove m = this.getLastMove();
			TemporalMove next;
			if (kp.simpleDrives)
				next = new SimpleWait(m.endPosition, this.getCurrentTime() + 1,
						null);
			else
				next = new TemporalWait(m.endPosition,
						this.getLastMove().endDir, this.getCurrentTime() + 1,
						null);
			this.addTemporalMove(next, this.kp, true);
		}
	}

	@Override
	public SearchProblem getSimpleProblem(MultiagentVertex v) {
		NavigationProblem np = new NavigationProblem(this.getLastMove(),
				(GridCell) v, this, kp);
		return np;
	}

	@Override
	public void doMove(MultiagentVertex original, MultiagentVertex next) {
		assert (original != null);
		assert (next != null);
		TemporalMove nextMove;
		if (original.equals(next)) {
			// make a wait move
			nextMove = new SimpleWait((GridCell) original,
					this.getCurrentTime() + 1, null);
		} else {
			// do a move
			nextMove = new SimpleMove((GridCell) original, (GridCell) next,
					this.getCurrentTime() + 1, null);
		}
		assert (nextMove != null);
		this.addTemporalMove(nextMove, this.kp, true);
	}

	@Override
	public int getCurrentTaskID() {
		return this.currentTask;
	}

	@Override
	public MultiagentTask getTask(int taskID) {
		if (tasks.size() <= taskID)
			return null;
		return tasks.get(taskID);
	}

	@Override
	public void doGlobalGhostMove(MultiagentVertex original,
			MultiagentVertex next) {
		assert (original != null);
		assert (next != null);
		original.incrementUsage(next);
		TemporalMove nextMove;
		if (original.equals(next)) {
			// make a wait move
			nextMove = new SimpleWait((GridCell) original,
					this.getCurrentTime() + 1, null);
		} else {
			// do a move
			nextMove = new SimpleMove((GridCell) original, (GridCell) next,
					this.getCurrentTime() + 1, null);
		}
		assert (nextMove != null);
		this.addTemporalMove(nextMove, this.kp, false);
	}

	@Override
	public void doTaskGlobalGhost() {
		MultiagentTask t = getNextTask();

		for (int i = 0; i < t.timeAtDestination; i++) {
			TemporalMove m = this.getLastMove();
			TemporalMove next;
			if (kp.simpleDrives)
				next = new SimpleWait(m.endPosition, this.getCurrentTime() + 1,
						null);
			else
				next = new TemporalPick(m.endPosition,
						this.getLastMove().endDir, this.getCurrentTime() + 1,
						null);
			this.addTemporalMove(next, this.kp, false);
		}
		t.isDone();
		this.currentTask++;
	}

	public boolean canClaimSpace(GridCell location, int start, int amount) {
		for (int i = start; i < start + amount; i++) {
			boolean okay = kp.res.checkReservation(location, i, this);
			if (!okay)
				return false;
		}
		return true;
	}

	private static int WANDER_AMOUNT = 10;
	
	public void clearPath(TemporalMove newInitial){
		//clear the drive's path completely
		path.clear();
		path.add(newInitial);
		
		//reset the current task to be the first task
		currentTask = 0;
		
		for(MultiagentTask t : tasks){
			t.markFailed();
		}
		
	}
	
	public boolean wander() {
		// create a path
		DFSRandomWalk rw = new DFSRandomWalk(WANDER_AMOUNT, 333, kp, this);
		ArrayList<SimpleMove> path = rw.getPath();
		if (path.size() < WANDER_AMOUNT)
			return false;
		for (int i = 1; i < path.size(); i++) {
			this.addTemporalMove(path.get(i), kp, true);
		}

		return true;
	}
	@Override
	public boolean isDone() {
		//check if this is the last task
		int taskID = this.getCurrentTaskID();
		if(taskID != this.getTasks().size() - 1){
			return false;
		}
		
		MultiagentTask t = this.getNextTask();
		if(t.destination.equals(this.getLastMove().endPosition)){
			return true;
		} else {
			return false;
		}
		
	}
	@Override
	public AgentState getPreviousState(int count) {
		return (AgentState) path.get(path.size() - 1 - count);
	}
}
