package org.cwilt.search.domains.kiva.problem;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.cwilt.search.domains.kiva.drive.Drive;
import org.cwilt.search.domains.kiva.drive.Pod;
import org.cwilt.search.domains.kiva.map.CandidateCorridor;
import org.cwilt.search.domains.kiva.map.Grid;
import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.kiva.path.simplified.SimpleWait;
import org.cwilt.search.domains.multiagent.problem.Agent;
import org.cwilt.search.domains.multiagent.problem.MultiagentGraph;
import org.cwilt.search.domains.multiagent.problem.MultiagentProblem;
import org.cwilt.search.domains.multiagent.problem.MultiagentTask;
import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
import org.cwilt.search.domains.multiagent.problem.ReservationTable;
import org.cwilt.search.domains.multiagent.solvers.autoqueue.AutoQueue;
import org.cwilt.search.domains.multiagent.solvers.queue.AgentQueue;
import org.cwilt.search.domains.multiagent.solvers.queue.QueueOverflow;
public class KivaProblem implements MultiagentProblem, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4646833988991727352L;

	@Override
	public void writeHOG(String op, String mapname, int seed) {
		// String mapRelativePath = "maps/custom/" + instanceName + ".map";
		// String mapName = pathPrefix + mapRelativePath;
		// map.writeHOG(mapName);
		// String configName = pathPrefix + "maps/custom/" + instanceName
		// + ".scenario";
		String outpath = op + "_" + drives.length + "_" + seed + ".scenario";

		try {
			FileWriter fstream = null;
			fstream = new FileWriter(outpath);
			BufferedWriter out = new BufferedWriter(fstream);

			out.write("version 1.0\n");

			for (int i = 0; i < drives.length; i++) {
				GridCell start = (drives[i].getLastMove().endPosition);
				GridCell end = (GridCell) (tasks.get(i).get(
						tasks.get(i).size() - 1).destination);
				out.write("0\t");
				File f = new File(mapname);
				out.write("/Users/chris/research/santa/movingai/" + f.getName());
				out.write("\t");
				out.write(Integer.toString(map.xCellCount()));
				out.write("\t");
				out.write(Integer.toString(map.yCellCount()));
				out.write("\t");
				out.write(Integer.toString(start.y));
				out.write("\t");
				out.write(Integer.toString(start.x));
				out.write("\t");
				out.write(Integer.toString(end.y));
				out.write("\t");
				out.write(Integer.toString(end.x));
				out.write("\t");
				out.write(Double.toString(start.distanceTo(end)));
				out.write("\n");

			}

			out.close();

			if (fstream != null)
				fstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void writeXML(String mapName, int seed) {
		String mapPath = mapName + ".xml";
		HashMap<MultiagentVertex, Integer> vertexMap = map.writeXML(mapPath);
		String configName = mapName + "_" + drives.length + "_" + seed + ".xml";


		try {
			FileWriter fstream = null;
			fstream = new FileWriter(configName);
			BufferedWriter out = new BufferedWriter(fstream);
			
			File f = new File(mapPath);
			String nameOnly = f.getName();
			mapName = "/Users/chris/research/santa/movingai/" + nameOnly;
			
			out.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			out.write("<robots roadmap=\"" + mapName
					+ "\" planner=\"ParallelPS\">\n");

			for (int i = 0; i < drives.length; i++) {
				int startID = vertexMap
						.get(drives[i].getLastMove().endPosition);
				int endID = vertexMap.get(tasks.get(i).get(
						tasks.get(i).size() - 1).destination);
				out.write("\t<robot start=\"" + startID + "\" goal=\"" + endID
						+ "\"/>\n");
			}
			out.write("<simulate file=\"christest.xml\" value=\"true\"/>\n");
			out.write("</robots>\n");

			out.close();

			if (fstream != null)
				fstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static final int FRAME_RATE = 30;
	public final Grid map;
	public final Drive[] drives;
	private final List<Agent> drivesArraylist;
	public final Pod[] pods;
	public final ReservationTable res;
	private transient org.cwilt.search.domains.multiagent.solvers.MultiagentSolver solver;
	public final boolean simpleDrives;

	public Dimension getPreferredInitialSize() {
		int xSize = (int) (org.cwilt.search.domains.kiva.map.GridCell.CELL_SIZE * map.xCellCount());
		int ySize = (int) (org.cwilt.search.domains.kiva.map.GridCell.CELL_SIZE * map.yCellCount());
		Toolkit tk = Toolkit.getDefaultToolkit();
		xSize = Math.min(((int) tk.getScreenSize().getWidth()), xSize);

		ySize = Math.min(((int) tk.getScreenSize().getHeight()), ySize);

		xSize = 1400;
		ySize = 1050;
		return new Dimension(xSize, ySize);
	}

	public final Random r;
	public final ArrayList<ArrayList<MultiagentTask>> tasks;

	private static final int MAX_DURATION = 3;
	private static final int MIN_DURATION = 2;

	private final int nDrives;

	private void randomTasks() {
		for (int wave = 0; wave < nWaves; wave++) {
			int startTime = wave * separations;

			for (int i = 0; i < nDrives; i++) {
				int driveID = nDrives * wave + i;

				GridCell start = map.nextStorage();
				tasks.add(new ArrayList<MultiagentTask>(2));
				GridCell end = map.nextStorage();
				GridCell pick = map.randomPick(r);
				tasks.get(driveID).add(
						new MultiagentTask(pick, r.nextInt(MAX_DURATION
								- MIN_DURATION)
								+ MIN_DURATION));
				tasks.get(driveID).add(
						new MultiagentTask(end, r.nextInt(MAX_DURATION
								- MIN_DURATION)
								+ MIN_DURATION));
				drives[driveID] = new Drive(driveID, startTime, start, this,
						simpleDrives, tasks.get(driveID));
			}
		}
	}

	private static GridCell findCompetionPick(List<GridCell> picks, GridCell g) {
		for (GridCell pick : picks) {
			if (Math.abs(pick.y - g.y) <= 1)
				return pick;
		}
		return null;
	}

	private void competitionTasks() {
		ArrayList<GridCell> startCells = new ArrayList<GridCell>();
		ArrayList<GridCell> endCells = new ArrayList<GridCell>();
		// identify the start cells
		for (GridCell cell : map.storageCells) {
			if (cell.x < map.grid.length / 2)
				startCells.add(cell);
			else
				endCells.add(cell);
		}
		Collections.shuffle(startCells, r);

		for (int i = 0; i < nDrives; i++) {
			int driveID = i;

			GridCell start = startCells.get(i);
			tasks.add(new ArrayList<MultiagentTask>(2));

			GridCell end = endCells.get(i);
			GridCell pick = findCompetionPick(map.pickCells, end);
			tasks.get(driveID).add(new MultiagentTask(pick, 0));
			tasks.get(driveID).add(new MultiagentTask(end, 0));
			drives[driveID] = new Drive(driveID, 0, start, this, simpleDrives,
					tasks.get(driveID));
		}
	}

	@SuppressWarnings("unchecked")
	public void rebuildTaskRoutings(boolean reshuffle) {

		// if (reshuffle) {

		this.startCells.clear();
		this.endCells.clear();
		for (GridCell[] row : map.grid) {
			for (GridCell g : row) {
				if (!g.isTravel())
					continue;
				if (r.nextBoolean())
					startCells.add(g);
				else{
					if(!g.isValidGoal())
						continue;
					endCells.add(g);
				}
			}
		}

		Collections.shuffle(startCells, r);
		Collections.shuffle(endCells, r);

		// }
		tasks.clear();

		this.res.clear();

		for (int i = 0; i < nDrives; i++) {
			int driveID = i;
			GridCell start = startCells.get(i);
			tasks.add(new ArrayList<MultiagentTask>());
			GridCell end = endCells.get(i);
			end.setMultiagentGoal();

			ArrayList<MultiagentVertex> path = start.simplePath(end);

			for (int pathIndex = 0; pathIndex < path.size() - 1; pathIndex++) {
				GridCell current = (GridCell) path.get(pathIndex);
				GridCell next = (GridCell) path.get(pathIndex + 1);
				if (!current.isPopular())
					continue;
				if (current.isPopular() && next.isPopular())
					continue;

				if (current.getQueue() != null) {
					// must have gone through an exit
					if (current.getQueue().getGateway() == null) {
						continue;
					}
					AgentQueue nextTarget;
					if (current.getQueue().queueContains(next)) {
						if (current.getQueue().getGateway().eq1 == current
								.getQueue()) {
							nextTarget = current.getQueue().getGateway().eq2;
						} else {
							nextTarget = current.getQueue().getGateway().eq1;
						}
						// add the other queue
					} else {
						nextTarget = current.getQueue();
					}
					tasks.get(driveID).add(
							new MultiagentTask(nextTarget.getTarget(), 0));
				} else if (current.getAutoQueue() != null) {
					// must have gone through an exit
					GridCell gc = (GridCell) current;
					if (gc.getAutoQueue().getAutoGateway() == null) {
						continue;
					}
					AutoQueue nextTarget = current.getAutoQueue();
					tasks.get(driveID).add(
							new MultiagentTask(nextTarget.getTargets().get(0),
									0));

				} else {
					assert (false);
				}
				// for each vertex in the path, check if it is a queue, and if
				// it is an entry queue figure out if this path goes through the
				// correct side of this path.
			}

			// tasks.get(driveID).add(new MultiagentTask(end, 0));
			tasks.get(driveID).add(new MultiagentTask(end, 0));

			if (drives[driveID] == null) {
				drives[driveID] = new Drive(driveID, 0, start, this,
						simpleDrives, (ArrayList<MultiagentTask>) tasks.get(
								driveID).clone());
			} else {
				drives[driveID].getTasks().clear();
				drives[driveID].getTasks().addAll(tasks.get(driveID));
				drives[driveID].clearPath(new SimpleWait(start, 0, null));
			}
		}

	}

	private void gameTasks() {
		// form the queues
		// map.addVerticalQueue(24, 54);
		// map.addHorizontalQueue(48, 110);
		// map.addVerticalQueue(40, 134);
		// map.addHorizontalQueue(32, 174);
		// map.addHorizontalQueue(48, 206);
		// map.addVerticalQueue(72, 214);
		// map.addVerticalQueue(88, 198);
		// map.addHorizontalQueue(112, 174);
		// map.addHorizontalQueue(144, 174);
		// map.addVerticalQueue(88, 102);
		// map.addVerticalQueue(104, 38);
		// //block off the part made inaccessable by the queue
		// map.grid[33][171] = new GridCell(33, 171, GridCell.CELL_TYPE.CHARGER,
		// map);
		// map.grid[34][171] = new GridCell(34, 171, GridCell.CELL_TYPE.CHARGER,
		// map);
		// map.grid[35][171] = new GridCell(35, 171, GridCell.CELL_TYPE.CHARGER,
		// map);
		// map.grid[34][172] = new GridCell(34, 172, GridCell.CELL_TYPE.CHARGER,
		// map);
		// map.grid[35][170] = new GridCell(35, 170, GridCell.CELL_TYPE.TRAVEL,
		// map);
		// map.grid[109][33] = new GridCell(109, 33, GridCell.CELL_TYPE.TRAVEL,
		// map);
		// map.grid[109][34] = new GridCell(109, 34, GridCell.CELL_TYPE.TRAVEL,
		// map);
		// map.grid[109][35] = new GridCell(109, 35, GridCell.CELL_TYPE.TRAVEL,
		// map);
		// map.grid[115][171] = new GridCell(115, 171,
		// GridCell.CELL_TYPE.TRAVEL, map);
		// map.grid[116][171] = new GridCell(116, 171,
		// GridCell.CELL_TYPE.TRAVEL, map);

		// identify the start cells

		this.rebuildTaskRoutings(true);

	}

	private final ArrayList<GridCell> startCells = new ArrayList<GridCell>();
	private final ArrayList<GridCell> endCells = new ArrayList<GridCell>();

	@SuppressWarnings("unused")
	private void simpleTasks() {
		for (int i = 0; i < drives.length; i++) {
			GridCell start = map.grid[9][9];
			GridCell end = map.grid[10][12];
			GridCell pick = map.grid[9][13];
			tasks.add(new ArrayList<MultiagentTask>(3));
			tasks.get(i).add(
					new MultiagentTask(pick, r.nextInt(MAX_DURATION
							- MIN_DURATION)
							+ MIN_DURATION));
			tasks.get(i).add(
					new MultiagentTask(end, r.nextInt(MAX_DURATION
							- MIN_DURATION)
							+ MIN_DURATION));
			// tasks.get(i).add(new MultiagentTask(start, r.nextInt(MAX_DURATION
			// - MIN_DURATION) + MIN_DURATION));
			drives[i] = new Drive(i, 0, start, this, simpleDrives, tasks.get(i));
		}
	}

	public KivaProblem(String path, int nDrives, int randomSeed,
			boolean simpleDrives) {
		this.nDrives = nDrives;
		this.simpleDrives = simpleDrives;

		this.nWaves = 1;
		this.separations = 0;
		this.r = new Random(randomSeed);
		this.map = new Grid(path, r);
		this.pods = new Pod[map.storageCells.size()];
		for (int i = 0; i < map.storageCells.size(); i++) {
			this.pods[i] = new Pod(map.storageCells.get(i));
		}
		this.drives = new Drive[nDrives];
		this.tasks = new ArrayList<ArrayList<MultiagentTask>>(nDrives);
		this.res = new ReservationTable(map.xCellCount() * map.yCellCount());

		this.randomTasks();
		this.drivesArraylist = new ArrayList<Agent>(nDrives);
		for (Drive d : drives) {
			drivesArraylist.add(d);
		}

		this.solver = new org.cwilt.search.domains.multiagent.solvers.ReservationSolver(this);
		// this.solver = new ReservationSolver(this);
	}

	private final int nWaves, separations;

	public KivaProblem(Grid g, int nDrives, int nWaves, int separations,
			int randomSeed, boolean simpleDrives) {
		this.nDrives = nDrives;
		this.simpleDrives = simpleDrives;
		this.nWaves = nWaves;
		this.separations = separations;
		this.r = new Random(randomSeed);
		this.map = g;
		this.res = new ReservationTable(map.xCellCount() * map.yCellCount());
		this.pods = new Pod[map.storageCells.size()];
		for (int i = 0; i < map.storageCells.size(); i++) {
			this.pods[i] = new Pod(map.storageCells.get(i));
		}
		this.drives = new Drive[nDrives * nWaves];
		this.tasks = new ArrayList<ArrayList<MultiagentTask>>(nDrives * nWaves);
		this.randomTasks();
		this.drivesArraylist = new ArrayList<Agent>(nDrives * nWaves);
		for (Drive d : drives)
			drivesArraylist.add(d);
		this.solver = new org.cwilt.search.domains.multiagent.solvers.ReservationSolver(this);
		// this.solver = new ReservationSolver(this);
	}

	public enum TASK_TYPE {
		COMPETITION, GAME
	}

	public KivaProblem(Grid g, int nDrives, int randomSeed,
			boolean simpleDrives, TASK_TYPE t) {
		this.nDrives = nDrives;
		this.simpleDrives = simpleDrives;
		this.nWaves = 1;
		this.separations = 0;
		this.r = new Random(randomSeed);
		this.map = g;
		this.res = new ReservationTable(map.xCellCount() * map.yCellCount());
		this.pods = new Pod[map.storageCells.size()];
		for (int i = 0; i < map.storageCells.size(); i++) {
			this.pods[i] = new Pod(map.storageCells.get(i));
		}
		this.drives = new Drive[nDrives * nWaves];
		this.tasks = new ArrayList<ArrayList<MultiagentTask>>(nDrives * nWaves);
		switch (t) {
		case COMPETITION:
			this.competitionTasks();
			break;
		case GAME:
			List<CandidateCorridor> c = map.findCorridors();
			this.gameTasks();
			map.distFromCorridors(c);
			break;
		}
		this.drivesArraylist = new ArrayList<Agent>(nDrives * nWaves);
		for (Drive d : drives)
			drivesArraylist.add(d);
		this.solver = new org.cwilt.search.domains.multiagent.solvers.ReservationSolver(this);
		// this.solver = new ReservationSolver(this);
	}

	public void setSolver(org.cwilt.search.domains.multiagent.solvers.MultiagentSolver m) {
		this.solver = m;
	}

	public void solve() throws QueueOverflow {
		solver.solve();
	}

	/*
	 * public MultiagentTask getNextTask(Drive d){ for(MultiagentTask t :
	 * tasks.get(d.id)){ if(!t.isDone()) return t; } return null; }
	 */
	private int drawTimeStep;
	private int drawFrameStep;

	public void printTime() {
		System.err.println("time " + drawTimeStep + " frame " + drawFrameStep);
	}

	/**
	 * Determines the max time step possible by looking at the paths of all
	 * drives.
	 * 
	 * @return The max time step where action occurs.
	 */
	public int getMaxTimeStep() {
		if (drives.length == 0) {
			return 0; // / ?
		}

		int max = drives[0].getPathSize();

		for (int i = 1; i < drives.length; i++) {
			if (drives[i].getPathSize() > max) {
				max = drives[i].getPathSize();
			}
		}

		return max;
	}

	/**
	 * Sets the current time step according to the specified new value and
	 * resets the current frame step to zero.
	 * 
	 * @param t
	 *            The desired time step.
	 */
	public void setTimeStep(int t) {
		drawTimeStep = t;
		drawFrameStep = 0;
	}

	/**
	 * Gets the current time step.
	 * 
	 * @return The current time step.
	 */
	public int getTimeStep() {
		return drawTimeStep;
	}

	/**
	 * Draws the map, the paths of the drives, the drives, and the stationary
	 * pods (in that order).
	 * 
	 * @param d
	 *            The Graphics2D object for drawing.
	 * @param scale 
	 */
	public void draw(Graphics2D d, double scale) {
		map.draw(d, scale);

		for (Drive drive : drives) {
			drive.drawPath(d);
		}

		for (Drive drive : drives) {
			drive.draw(d);
		}

		for (Pod p : pods) {
			if (p.isStationary()) {
				p.draw(d);
			}
		}
	}

	public void advanceTime() {
		drawFrameStep++;
		if (drawFrameStep == FRAME_RATE) {
			drawFrameStep = 0;
			drawTimeStep++;
		}

		for (Drive d : drives) {
			d.setTime(drawTimeStep, drawFrameStep);
		}
	}

	@Override
	public List<Agent> getAgents() {
		return drivesArraylist;
	}

	@Override
	public MultiagentGraph getGraph() {
		return map;
	}

	@Override
	public ReservationTable getReservationTable() {
		return this.res;
	}

	@Override
	public void writeGraphData(PrintStream s) {
		map.writeData(s);
	}

	public void setGhost() {
		this.ghost = true;
	}

	private boolean ghost = false;

	public boolean ghost() {
		return ghost;
	}

	public void serializeKivaProblem(String outpath) {
		try {
			OutputStream file;
			file = new FileOutputStream(outpath);
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			try {
				output.writeObject(this);
			} finally {
				output.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException ioex) {
			ioex.printStackTrace();
			System.exit(1);
		}
	}

}
