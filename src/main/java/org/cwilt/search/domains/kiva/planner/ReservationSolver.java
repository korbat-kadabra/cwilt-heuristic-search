package org.cwilt.search.domains.kiva.planner;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.cwilt.search.algs.basic.bestfirst.AStar;
import org.cwilt.search.domains.kiva.drive.Drive;
import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.kiva.path.simplified.SimpleWait;
import org.cwilt.search.domains.kiva.path.temporal.TemporalMove;
import org.cwilt.search.domains.kiva.path.temporal.TemporalPick;
import org.cwilt.search.domains.kiva.path.temporal.TemporalWait;
import org.cwilt.search.domains.kiva.path.timeless.NavigationProblem;
import org.cwilt.search.domains.kiva.path.timeless.Move.DIRECTION;
import org.cwilt.search.domains.kiva.problem.KivaProblem;
import org.cwilt.search.domains.multiagent.problem.MultiagentTask;
import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchState;
public class ReservationSolver extends Solver {
	private final KivaProblem problem;
	private final PriorityQueue<Drive> driveQueue;

	public ReservationSolver(KivaProblem kp) {
		this.problem = kp;
		this.driveQueue = new PriorityQueue<Drive>(problem.drives.length,
				new DriveComparator());
		for (Drive d : problem.drives) {
			driveQueue.add(d);
		}
	}

	private class DriveComparator implements Comparator<Drive> {
		@Override
		public int compare(Drive arg0, Drive arg1) {
			int d0Time = arg0.getCurrentTime();
			int d1Time = arg1.getCurrentTime();
			if (d0Time < d1Time)
				return -1;
			else if (d0Time > d1Time)
				return 1;
			if (arg0.id < arg1.id)
				return -1;
			else if (arg0.id > arg1.id)
				return 1;
			return 0;
		}
	}

	public TemporalMove getWait(Drive d) {
		if (problem.simpleDrives)
			return new TemporalWait(d.getLastMove().endPosition,
					DIRECTION.HORIZONTAL, d.getCurrentTime() + 1, null);
		else
			return new SimpleWait(d.getLastMove().endPosition,
					d.getCurrentTime() + 1, null);

	}

	public TemporalMove getPick(MultiagentTask t, Drive d) {
		if (problem.simpleDrives)
			return new TemporalPick((GridCell) t.destination, DIRECTION.HORIZONTAL,
					d.getCurrentTime() + 1, null);
		else
			return new SimpleWait((GridCell) t.destination, 
					d.getCurrentTime() + 1, null);

	}

	/**
	 * Tries to do the Task t.
	 * 
	 * @param t
	 *            Task to complete
	 * @return true if the task was completed, false otherwise.
	 */
	private boolean doTask(MultiagentTask t, Drive drive) {
		assert (t != null);
		assert(t.destination.isPopular());
		TemporalMove first = drive.getLastMove();
		NavigationProblem np = new NavigationProblem(first, (GridCell) t.destination,
				drive, problem);
		AStar a = new AStar(np, new Limit());
		ArrayList<SearchState> path = a.solve();
		assert (path != null);
		for(int i = 1; i < path.size(); i++){
			drive.addTemporalMove((TemporalMove) path.get(i), problem, true);
		}
		if(drive.canDoTask()){
			drive.doTask(false);
		} else {
			drive.backoffTemporal(first);
			return false;
		}
		
		MultiagentTask nextTask = drive.getNextTask();
		np = new NavigationProblem(drive.getLastMove(), (GridCell) nextTask.destination,
				drive, problem);
		a = new AStar(np, new Limit());
		ArrayList<SearchState> returnPath = a.solve();
		if (returnPath == null) {
			drive.backoffTemporal(first);
			drive.failedTask(t);
			return false;
		}
		
		for (int i = 1; i < returnPath.size(); i++) {
			TemporalMove nextMove = (TemporalMove) returnPath.get(i);
			drive.addTemporalMove(nextMove, problem, true);
		}
		assert(drive.canDoTask());
		drive.doTask(false);
		
		
		
		return true;
	}

	public void solve() {
		while (!driveQueue.isEmpty()) {
			Drive d = driveQueue.poll();
			MultiagentTask t = d.getNextTask();
			boolean finished = doTask(t, d);
			if (finished) {
				t.markDone();
				t = d.getNextTask();
				if (t != null)
					driveQueue.add(d);
			} else {
				// task failed, have to wait a bit and try again later.
				for (int i = 0; i < 10; i++) {
					TemporalMove first = getWait(d);
					d.addTemporalMove(first, problem, true);
				}
				driveQueue.add(d);
			}
		}
	}
}
