package org.cwilt.search.domains.kiva.map;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.cwilt.search.domains.kiva.map.CorridorFinder.DIRECTION;
import org.cwilt.search.domains.kiva.map.GridCell.CELL_TYPE;
import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
import org.cwilt.search.domains.multiagent.problem.ReservationTable;
import org.cwilt.search.domains.multiagent.solvers.autoqueue.AutoQueue;
import org.cwilt.search.domains.multiagent.solvers.autoqueue.AutoQueue.AutoGateway;
import org.cwilt.search.domains.multiagent.solvers.queue.Disperser;
import org.cwilt.search.domains.multiagent.solvers.queue.EntryQueue;
import org.cwilt.search.domains.multiagent.solvers.queue.ExitQueue;
import org.cwilt.search.domains.multiagent.solvers.queue.AgentQueue.Gateway;
public class Grid implements org.cwilt.search.domains.multiagent.problem.MultiagentGraph, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8048270193240039316L;

	/**
	 * Writes this grid problem out to the desired path in the format for HOG2
	 * grid path planning system
	 * 
	 * @param path
	 *            Path to export this problem to
	 */
	public void writeHOG(String path) {

		try {
			FileWriter fstream = null;
			fstream = new FileWriter(path);
			BufferedWriter out = new BufferedWriter(fstream);

			out.write("type octile\n");
			out.write("height " + grid[0].length + "\n");
			out.write("width " + grid.length + "\n");
			out.write("map\n");
			for (int i = 0; i < grid[0].length; i++) {
				for (int j = 0; j < grid.length; j++) {
					out.write(grid[j][i].hogString());
				}
				out.write("\n");
			}
			out.close();

			if (fstream != null)
				fstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static final String xmlEdge(HashMap<MultiagentVertex, Integer> ids,
			MultiagentVertex start, MultiagentVertex end) {
		int startID = ids.get(start);
		int endID = ids.get(end);
		return "<prx_graph_edge source=\"" + startID + "\" target=\"" + endID
				+ "\" distance=\"1.000000\"/>";
	}

	@Override
	public HashMap<MultiagentVertex, Integer> writeXML(String path) {
		HashMap<MultiagentVertex, Integer> cellID = new HashMap<MultiagentVertex, Integer>();

		try {
			FileWriter fstream = null;
			fstream = new FileWriter(path);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n");
			out.write("<prx_graph directed=\"false\">\n");
			int currentID = 0;
			for (int i = 0; i < grid.length; i++) {
				for (int j = 0; j < grid[0].length; j++) {
					if (!grid[i][j].xmlOutput())
						continue;

					cellID.put(grid[i][j], currentID);
					currentID++;
					out.write(grid[i][j].xmlString());
					out.write("\n");
				}
			}

			for (int i = 0; i < grid.length; i++) {
				for (int j = 0; j < grid[0].length; j++) {
					Integer id = cellID.get(grid[i][j]);
					if (id == null)
						continue;
					List<MultiagentVertex> neighbors = grid[i][j]
							.getNeighbors();
					for (MultiagentVertex neighbor : neighbors) {
						out.write(xmlEdge(cellID, grid[i][j], neighbor));
						out.write("\n");
					}
				}
			}

			out.write("</prx_graph>");
			out.close();

			if (fstream != null)
				fstream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return cellID;

	}

	/**
	 * Gets a random pick cell, using the specified Random object.
	 * 
	 * @param r
	 *            Random number generator to use to select the pick cell
	 * @return A randomly selected high contention vertex.
	 */
	public GridCell randomPick(Random r) {
		return pickCells.get(r.nextInt(pickCells.size()));
	}

	private int nextStorage;

	/**
	 * Gets a randomly selected storage cell
	 * 
	 * @return Randomly selected storage cell
	 */
	public GridCell nextStorage() {
		while (true) {
			GridCell possibleNext = storageCells.get(nextStorage++);
			if ((possibleNext.canLeft() && possibleNext.left().canEnter())
					|| (possibleNext.canRight() && possibleNext.right()
							.canEnter())){
				if(possibleNext.isValidGoal())
					return possibleNext;
			}
		}
	}

	/**
	 * 
	 * @param path
	 *            Path to load the grid from
	 * @param r
	 *            Random number generator to use for this Grid
	 */
	public Grid(String path, Random r) {
		this.storageCells = new ArrayList<GridCell>();
		this.pickCells = new ArrayList<GridCell>();
		try {
			FileInputStream fstream;
			fstream = new FileInputStream(path);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			strLine = br.readLine();
			strLine = br.readLine();

			String sizeString = br.readLine();
			String[] sizeArray = sizeString.split("\\s+");

			assert (sizeArray.length == 2);

			int x = Integer.parseInt(sizeArray[0]);
			int y = Integer.parseInt(sizeArray[1]);

			grid = new GridCell[x][y];

			strLine = br.readLine();

			for (int i = 0; i < x; i++) {
				String[] nextRow = strLine.split("\\s+");
				for (int j = 0; j < y; j++) {
					grid[i][j] = new GridCell(i, j, nextRow[j], this);
					if (grid[i][j].isStorage()) {
						storageCells.add(grid[i][j]);
					}
					if (grid[i][j].isPick()) {
						pickCells.add(grid[i][j]);
					}

				}
				strLine = br.readLine();
			}
			in.close();
		} catch (FileNotFoundException e) {
			System.err.println("Failed to open file - " + path
					+ " does not exist");
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (this.grid == null) {
			throw new RuntimeException("Didn't parse something correctly");
		}

		Collections.shuffle(pickCells, r);
		Collections.shuffle(storageCells, r);
	}

	public Grid(String path) {
		this.storageCells = new ArrayList<GridCell>();
		this.pickCells = new ArrayList<GridCell>();
		try {
			FileInputStream fstream;
			fstream = new FileInputStream(path);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			strLine = br.readLine();

			String sizeString = br.readLine();
			String[] sizeArray = sizeString.split("\\s+");
			assert (sizeArray.length == 2);

			int x = Integer.parseInt(sizeArray[1]);
			sizeString = br.readLine();
			sizeArray = sizeString.split("\\s+");
			assert (sizeArray.length == 2);
			int y = Integer.parseInt(sizeArray[1]);

			grid = new GridCell[x][y];

			strLine = br.readLine();
			strLine = br.readLine();

			for (int i = 0; i < x; i++) {
				for (int j = 0; j < y; j++) {
					grid[i][j] = new GridCell(i, j, strLine.charAt(j), this);
					if (grid[i][j].isStorage()) {
						storageCells.add(grid[i][j]);
					}
					if (grid[i][j].isPick()) {
						pickCells.add(grid[i][j]);
					}
				}
				strLine = br.readLine();
			}
			in.close();
		} catch (FileNotFoundException e) {
			System.err.println("Failed to open file - " + path
					+ " does not exist");
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (this.grid == null) {
			throw new RuntimeException("Didn't parse something correctly");
		}
	}

	private void addDisperser(ExitQueue exit, EntryQueue entrance) {
		// do the left side
		HashSet<MultiagentVertex> toEntrance = new HashSet<MultiagentVertex>();
		HashSet<MultiagentVertex> toExit = new HashSet<MultiagentVertex>();
		LinkedList<MultiagentVertex> exits = new LinkedList<MultiagentVertex>();
		exits.addAll(entrance.entry);
		toEntrance.addAll(entrance.entry);

		exits.add(exit.getTarget());
		toExit.add(exit.getTarget());
		for (int i = 0; i < 17; i++) {
			MultiagentVertex next = exits.poll();
			if (toExit.contains(next)) {
				assert (!toEntrance.contains(next));
				List<MultiagentVertex> neighbors = next.getNeighbors();
				for (MultiagentVertex v : neighbors) {
					GridCell neighbor = (GridCell) v;
					if (!neighbor.isTravel())
						continue;
					if (toEntrance.contains(neighbor))
						continue;
					if (toExit.contains(neighbor))
						continue;
					toExit.add(neighbor);
					exits.add(neighbor);
				}
			} else if (toEntrance.contains(next)) {
				assert (!toExit.contains(next));
				List<MultiagentVertex> neighbors = next.getNeighbors();
				for (MultiagentVertex v : neighbors) {
					GridCell neighbor = (GridCell) v;
					if (!neighbor.isTravel())
						continue;
					if (toEntrance.contains(neighbor))
						continue;
					if (toExit.contains(neighbor))
						continue;
					toEntrance.add(neighbor);
					exits.add(neighbor);
				}
			}
		}
		List<MultiagentVertex> queue = new LinkedList<MultiagentVertex>();
		for (MultiagentVertex v : toExit) {
			GridCell gc = (GridCell) v;
			if (v.isPopular())
				continue;
			grid[gc.x][gc.y] = new GridCell(gc.x, gc.y, CELL_TYPE.QUEUE, this);
			// grid[gc.x][gc.y].setCustomColor(java.awt.Color.GREEN);
			queue.add(grid[gc.x][gc.y]);
		}
		Disperser d = new Disperser(queue);

		exit.setDisperser(d);
	}

	/**
	 * Adds a dispersion queue on the gateway
	 * 
	 * @param g
	 */
	public void addDisperser(Gateway g) {
		addDisperser(g.eq1.getExit(), g.eq2);
		addDisperser(g.eq2.getExit(), g.eq1);
	}

	/**
	 * 
	 * @param x
	 *            X location of the LEFT queue
	 * @param y
	 *            Y location of the LEFT queue
	 */
	public void addVerticalQueue(int x, int y) {

		grid[x][y] = new GridCell(x, y, GridCell.CELL_TYPE.PICK, this);
		grid[x + 1][y] = new GridCell(x + 1, y, GridCell.CELL_TYPE.PICK, this);
		for (int delta = -3; delta < 4; delta++) {
			if (delta == 0)
				continue;
			grid[x][y + delta] = new GridCell(x, y + delta,
					GridCell.CELL_TYPE.QUEUE, this);
			grid[x + 1][y + delta] = new GridCell(x + 1, y + delta,
					GridCell.CELL_TYPE.QUEUE, this);
		}
		grid[x - 1][y - 2] = new GridCell(x - 1, y - 2,
				GridCell.CELL_TYPE.QUEUE, this);
		grid[x + 2][y + 2] = new GridCell(x + 2, y + 2,
				GridCell.CELL_TYPE.QUEUE, this);

		grid[x - 1][y - 3] = new GridCell(x - 1, y - 3,
				GridCell.CELL_TYPE.QUEUE, this);
		grid[x + 2][y + 3] = new GridCell(x + 2, y + 3,
				GridCell.CELL_TYPE.QUEUE, this);

		grid[x - 2][y - 2] = new GridCell(x - 2, y - 2,
				GridCell.CELL_TYPE.QUEUE, this);
		grid[x + 3][y + 2] = new GridCell(x + 3, y + 2,
				GridCell.CELL_TYPE.QUEUE, this);

		grid[x][y + 4] = new GridCell(x, y + 4, GridCell.CELL_TYPE.PICK, this);
		grid[x + 1][y - 4] = new GridCell(x + 1, y - 4,
				GridCell.CELL_TYPE.PICK, this);

		ExitQueue leftExit = new ExitQueue(grid[x][y + 4]);
		leftExit.addVertex(grid[x][y + 3]);
		leftExit.addVertex(grid[x][y + 2]);
		leftExit.addVertex(grid[x][y + 1]);
		leftExit.addRoute(grid[x][y + 1], grid[x][y + 2]);
		leftExit.addRoute(grid[x][y + 2], grid[x][y + 3]);
		leftExit.addRoute(grid[x][y + 3], grid[x][y + 4]);

		ExitQueue rightExit = new ExitQueue(grid[x + 1][y - 4]);
		rightExit.addVertex(grid[x + 1][y - 1]);
		rightExit.addVertex(grid[x + 1][y - 2]);
		rightExit.addVertex(grid[x + 1][y - 3]);
		rightExit.addRoute(grid[x + 1][y - 1], grid[x + 1][y - 2]);
		rightExit.addRoute(grid[x + 1][y - 2], grid[x + 1][y - 3]);
		rightExit.addRoute(grid[x + 1][y - 3], grid[x + 1][y - 4]);

		EntryQueue leftEntry = new EntryQueue(grid[x][y], leftExit);
		leftEntry.addVertex(grid[x][y - 1]);
		leftEntry.addVertex(grid[x][y - 2]);
		leftEntry.addVertex(grid[x][y - 3]);
		leftEntry.addVertex(grid[x - 1][y - 2]);
		leftEntry.addVertex(grid[x - 1][y - 3]);
		leftEntry.addVertex(grid[x - 2][y - 2]);

		leftEntry.addRoute(grid[x][y - 1], grid[x][y]);
		leftEntry.addRoute(grid[x][y - 2], grid[x][y - 1]);
		leftEntry.addRoute(grid[x][y - 3], grid[x][y - 2]);
		leftEntry.addRoute(grid[x - 1][y - 2], grid[x][y - 2]);
		leftEntry.addRoute(grid[x - 1][y - 3], grid[x - 1][y - 2]);
		leftEntry.addRoute(grid[x - 2][y - 2], grid[x - 1][y - 2]);

		EntryQueue rightEntry = new EntryQueue(grid[x + 1][y], rightExit);
		rightEntry.addVertex(grid[x + 1][y + 1]);
		rightEntry.addVertex(grid[x + 1][y + 2]);
		rightEntry.addVertex(grid[x + 1][y + 3]);
		rightEntry.addVertex(grid[x + 2][y + 2]);
		rightEntry.addVertex(grid[x + 2][y + 3]);
		rightEntry.addVertex(grid[x + 3][y + 2]);

		rightEntry.addRoute(grid[x + 1][y + 1], grid[x + 1][y]);
		rightEntry.addRoute(grid[x + 1][y + 2], grid[x + 1][y + 1]);
		rightEntry.addRoute(grid[x + 1][y + 3], grid[x + 1][y + 2]);
		rightEntry.addRoute(grid[x + 2][y + 3], grid[x + 2][y + 2]);
		rightEntry.addRoute(grid[x + 2][y + 2], grid[x + 1][y + 2]);
		rightEntry.addRoute(grid[x + 3][y + 2], grid[x + 2][y + 2]);

		leftEntry.preparePaths();
		rightEntry.preparePaths();
		leftExit.preparePaths();
		rightExit.preparePaths();

		rightEntry.setGateway(leftEntry);
		addDisperser(rightEntry.getGateway());

	}

	/**
	 * 
	 * @param x
	 *            X location of the center of this queue
	 * @param y
	 *            Y location of the center of this queue
	 */
	public void addHorizontalQueue(int x, int y) {
		int deltaX = 0;
		assert (grid[x + deltaX][y].isTravel());

		grid[x + deltaX][y] = new GridCell(x + deltaX, y,
		GridCell.CELL_TYPE.BLOCKED, this);

		assert (grid[x + deltaX][y + 1].isTravel());
		grid[x + deltaX][y + 1] = new GridCell(x + deltaX, y + 1,
				GridCell.CELL_TYPE.PICK, this);

		assert (grid[x + deltaX][y - 1].isTravel());
		grid[x + deltaX][y - 1] = new GridCell(x + deltaX, y - 1,
				GridCell.CELL_TYPE.PICK, this);

		for (deltaX = 1; deltaX <= 3; deltaX++) {
			assert (grid[x + deltaX][y].isTravel());
			grid[x + deltaX][y] = new GridCell(x + deltaX, y,
					GridCell.CELL_TYPE.QUEUE, this);
			assert (grid[x + deltaX][y + 1].isTravel());
			grid[x + deltaX][y + 1] = new GridCell(x + deltaX, y + 1,
					GridCell.CELL_TYPE.QUEUE, this);
			assert (grid[x + deltaX][y - 1].isTravel());
			grid[x + deltaX][y - 1] = new GridCell(x + deltaX, y - 1,
					GridCell.CELL_TYPE.QUEUE, this);
			deltaX = deltaX * -1;
			assert (grid[x + deltaX][y].isTravel());
			grid[x + deltaX][y] = new GridCell(x + deltaX, y,
					GridCell.CELL_TYPE.QUEUE, this);
			assert (grid[x + deltaX][y + 1].isTravel());
			grid[x + deltaX][y + 1] = new GridCell(x + deltaX, y + 1,
					GridCell.CELL_TYPE.QUEUE, this);
			assert (grid[x + deltaX][y - 1].isTravel());
			grid[x + deltaX][y - 1] = new GridCell(x + deltaX, y - 1,
					GridCell.CELL_TYPE.QUEUE, this);
			deltaX = deltaX * -1;
		}
		assert (grid[x - 2][y - 2].isTravel());
		grid[x - 2][y - 2] = new GridCell(x - 2, y - 2,
				GridCell.CELL_TYPE.QUEUE, this);

		assert (grid[x + 2][y + 2].isTravel());
		grid[x + 2][y + 2] = new GridCell(x + 2, y + 2,
				GridCell.CELL_TYPE.QUEUE, this);

		grid[x - 4][y + 1] = new GridCell(x - 4, y + 1,
				GridCell.CELL_TYPE.PICK, this);
		grid[x + 4][y - 1] = new GridCell(x + 4, y - 1,
				GridCell.CELL_TYPE.PICK, this);
		ExitQueue leftExit = new ExitQueue(grid[x + 4][y - 1]);
		leftExit.addVertex(grid[x + 3][y - 1]);
		leftExit.addVertex(grid[x + 2][y - 1]);
		leftExit.addVertex(grid[x + 1][y - 1]);
		leftExit.addRoute(grid[x + 3][y - 1], grid[x + 4][y - 1]);
		leftExit.addRoute(grid[x + 2][y - 1], grid[x + 3][y - 1]);
		leftExit.addRoute(grid[x + 1][y - 1], grid[x + 2][y - 1]);

		ExitQueue rightExit = new ExitQueue(grid[x - 4][y + 1]);
		rightExit.addVertex(grid[x - 3][y + 1]);
		rightExit.addVertex(grid[x - 2][y + 1]);
		rightExit.addVertex(grid[x - 1][y + 1]);
		rightExit.addRoute(grid[x - 3][y + 1], grid[x - 4][y + 1]);
		rightExit.addRoute(grid[x - 2][y + 1], grid[x - 3][y + 1]);
		rightExit.addRoute(grid[x - 1][y + 1], grid[x - 2][y + 1]);

		EntryQueue leftEntry = new EntryQueue(grid[x][y - 1], leftExit);
		EntryQueue rightEntry = new EntryQueue(grid[x][y + 1], rightExit);
		leftEntry.addVertex(grid[x - 1][y - 1]);
		leftEntry.addVertex(grid[x - 1][y]);
		leftEntry.addVertex(grid[x - 2][y - 2]);
		leftEntry.addVertex(grid[x - 2][y - 1]);
		leftEntry.addVertex(grid[x - 2][y - 0]);
		leftEntry.addVertex(grid[x - 3][y - 1]);
		leftEntry.addVertex(grid[x - 3][y - 0]);
		leftEntry.addRoute(grid[x - 1][y - 1], grid[x - 0][y - 1]);
		leftEntry.addRoute(grid[x - 2][y - 1], grid[x - 1][y - 1]);
		leftEntry.addRoute(grid[x - 3][y - 1], grid[x - 2][y - 1]);
		leftEntry.addRoute(grid[x - 2][y], grid[x - 1][y]);
		leftEntry.addRoute(grid[x - 3][y], grid[x - 2][y]);
		leftEntry.addRoute(grid[x - 1][y], grid[x - 1][y - 1]);
		leftEntry.addRoute(grid[x - 2][y - 2], grid[x - 2][y - 1]);

		rightEntry.addVertex(grid[x + 1][y + 1]);
		rightEntry.addVertex(grid[x + 1][y]);
		rightEntry.addVertex(grid[x + 2][y + 2]);
		rightEntry.addVertex(grid[x + 2][y + 1]);
		rightEntry.addVertex(grid[x + 2][y + 0]);
		rightEntry.addVertex(grid[x + 3][y + 1]);
		rightEntry.addVertex(grid[x + 3][y + 0]);
		rightEntry.addRoute(grid[x + 1][y + 1], grid[x + 0][y + 1]);
		rightEntry.addRoute(grid[x + 2][y + 1], grid[x + 1][y + 1]);
		rightEntry.addRoute(grid[x + 3][y + 1], grid[x + 2][y + 1]);
		rightEntry.addRoute(grid[x + 2][y], grid[x + 1][y]);
		rightEntry.addRoute(grid[x + 3][y], grid[x + 2][y]);
		rightEntry.addRoute(grid[x + 1][y], grid[x + 1][y + 1]);
		rightEntry.addRoute(grid[x + 2][y + 2], grid[x + 2][y + 1]);

		leftEntry.preparePaths();
		rightEntry.preparePaths();
		leftExit.preparePaths();
		rightExit.preparePaths();

		rightEntry.setGateway(leftEntry);
		addDisperser(rightEntry.getGateway());

	}

	private void populateStoragePick(Random r) {
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				if (grid[i][j].isStorage()) {
					storageCells.add(grid[i][j]);
				}
				if (grid[i][j].isPick()) {
					pickCells.add(grid[i][j]);
				}
			}
		}
		Collections.shuffle(storageCells, r);
		Collections.shuffle(pickCells, r);
	}

	public GridCell[][] grid;

	private static final GridCell.CELL_TYPE getRandomType(Random r) {
		int i = r.nextInt(5);
		switch (i) {
		case 0:
			return GridCell.CELL_TYPE.BLOCKED;
		case 1:
			return GridCell.CELL_TYPE.PICK;
		case 2:
			return GridCell.CELL_TYPE.STORAGE;
		case 3:
			return GridCell.CELL_TYPE.QUEUE;
		case 4:
			return GridCell.CELL_TYPE.TRAVEL;
		default:
			return GridCell.CELL_TYPE.TRAVEL;
		}

	}

	/**
	 * Draws this grid
	 * 
	 * @param d
	 *            Graphics2D Object to use to draw this grid
	 * @param scale
	 *            How much to zoom in or out
	 */
	public void draw(Graphics2D d, double scale) {
		for (GridCell[] row : grid) {
			for (GridCell c : row) {
				c.draw(d, scale);
			}
		}
		for (GridCell[] row : grid) {
			for (GridCell c : row) {
				c.drawArrows(d, scale);
			}
		}
	}

	/**
	 * Makes a random grid
	 * 
	 * @param x
	 *            X size
	 * @param y
	 *            Y size
	 */
	public Grid(int x, int y) {
		this.grid = new GridCell[x][y];
		this.storageCells = new ArrayList<GridCell>();
		this.pickCells = new ArrayList<GridCell>();
	}

	/**
	 * Adds a storage block with its upper left corner at x, y
	 * 
	 * @param x
	 * @param y
	 */
	private void addStorageBlock(int x, int y) {
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 5; j++) {
				grid[x + i][y + j] = new GridCell(x + i, y + j,
						CELL_TYPE.STORAGE, this);
			}
		}
	}

	/**
	 * Adds a storage block with its upper left corner at x, y
	 * 
	 * @param x
	 * @param y
	 */
	private void addBlockedStorageBlock(int x, int y) {
		for (int j = 0; j < 5; j++) {
			grid[x + 0][y + j] = new GridCell(x + 0, y + j, CELL_TYPE.BLOCKED,
					this);
		}

		grid[x + 1][y + 0] = new GridCell(x + 1, y + 0, CELL_TYPE.BLOCKED, this);
		grid[x + 1][y + 1] = new GridCell(x + 1, y + 1, CELL_TYPE.STORAGE, this);
		grid[x + 1][y + 2] = new GridCell(x + 1, y + 2, CELL_TYPE.STORAGE, this);
		grid[x + 1][y + 3] = new GridCell(x + 1, y + 3, CELL_TYPE.STORAGE, this);
		grid[x + 1][y + 4] = new GridCell(x + 1, y + 4, CELL_TYPE.BLOCKED, this);
	}

	/**
	 * Adds a storage block with its upper left corner at x, y
	 * 
	 * @param x
	 * @param y
	 */
	@SuppressWarnings("unused")
	private void addBigBlockedStorageBlock(int x, int y) {
		for (int j = 0; j < 7; j++) {
			grid[x + 0][y + j] = new GridCell(x + 0, y + j, CELL_TYPE.BLOCKED,
					this);
		}

		grid[x + 1][y + 0] = new GridCell(x + 1, y + 0, CELL_TYPE.BLOCKED, this);
		grid[x + 1][y + 1] = new GridCell(x + 1, y + 1, CELL_TYPE.STORAGE, this);
		grid[x + 1][y + 2] = new GridCell(x + 1, y + 2, CELL_TYPE.STORAGE, this);
		grid[x + 1][y + 3] = new GridCell(x + 1, y + 3, CELL_TYPE.STORAGE, this);
		grid[x + 1][y + 4] = new GridCell(x + 1, y + 4, CELL_TYPE.STORAGE, this);
		grid[x + 1][y + 5] = new GridCell(x + 1, y + 5, CELL_TYPE.STORAGE, this);
		grid[x + 1][y + 6] = new GridCell(x + 1, y + 6, CELL_TYPE.BLOCKED, this);
	}

	/**
	 * 
	 * @param x
	 * @param y
	 */
	private void addLeftPick(int x, int y) {
		// place the pick cell
		GridCell e1 = new GridCell(x + 3, y + 4, CELL_TYPE.QUEUE, this);
		ExitQueue eq1 = new ExitQueue(e1);
		grid[x + 3][y + 4] = e1;

		GridCell p1 = new GridCell(x, y + 2, CELL_TYPE.PICK, this);
		EntryQueue q1 = new EntryQueue(p1, eq1);

		grid[x][y + 2] = p1;
		for (int i = 0; i < 4; i++) {
			grid[x + i][y] = new GridCell(x + i, y, CELL_TYPE.QUEUE, this);
			grid[x + i][y + 1] = new GridCell(x + i, y + 1, CELL_TYPE.QUEUE,
					this);
			q1.addVertex(grid[x + i][y]);
			q1.addVertex(grid[x + i][y + 1]);
		}
		grid[x + 3][y + 2] = new GridCell(x + 3, y + 2, CELL_TYPE.QUEUE, this);
		q1.addVertex(grid[x + 3][y + 2]);
		grid[x + 3][y + 3] = new GridCell(x + 3, y + 3, CELL_TYPE.QUEUE, this);
		q1.addVertex(grid[x + 3][y + 3]);
		// block some cells
		grid[x + 1][y + 2] = new GridCell(x + 1, y + 2, CELL_TYPE.BLOCKED, this);
		grid[x + 1][y + 3] = new GridCell(x + 1, y + 3, CELL_TYPE.BLOCKED, this);
		grid[x + 2][y + 2] = new GridCell(x + 2, y + 2, CELL_TYPE.BLOCKED, this);
		grid[x + 2][y + 3] = new GridCell(x + 2, y + 3, CELL_TYPE.BLOCKED, this);

		grid[x][y + 4] = new GridCell(x, y + 4, CELL_TYPE.QUEUE, this);
		grid[x + 1][y + 4] = new GridCell(x + 1, y + 4, CELL_TYPE.QUEUE, this);
		grid[x + 2][y + 4] = new GridCell(x + 2, y + 4, CELL_TYPE.QUEUE, this);
		grid[x][y + 3] = new GridCell(x, y + 3, CELL_TYPE.QUEUE, this);
		eq1.addVertex(grid[x][y + 4]);
		eq1.addVertex(grid[x + 1][y + 4]);
		eq1.addVertex(grid[x + 2][y + 4]);
		eq1.addVertex(grid[x][y + 3]);

		eq1.addRoute(grid[x + 2][y + 4], grid[x + 3][y + 4]);
		eq1.addRoute(grid[x + 1][y + 4], grid[x + 2][y + 4]);
		eq1.addRoute(grid[x][y + 4], grid[x + 1][y + 4]);
		eq1.addRoute(grid[x][y + 3], grid[x][y + 4]);

		q1.addRoute(grid[x][y], grid[x][y + 1]);
		q1.addRoute(grid[x][y + 1], grid[x][y + 2]);
		q1.addRoute(grid[x + 1][y], grid[x][y]);
		q1.addRoute(grid[x + 2][y], grid[x + 1][y]);
		q1.addRoute(grid[x + 3][y], grid[x + 2][y]);

		q1.addRoute(grid[x + 1][y + 1], grid[x][y + 1]);
		q1.addRoute(grid[x + 2][y + 1], grid[x + 1][y + 1]);
		q1.addRoute(grid[x + 3][y + 1], grid[x + 2][y + 1]);

		q1.addRoute(grid[x + 3][y + 2], grid[x + 3][y + 1]);
		q1.addRoute(grid[x + 3][y + 3], grid[x + 3][y + 2]);
	}

	/**
	 * 
	 * @param x
	 * @param y
	 */
	private void addLeftPickReverse(int x, int y) {
		// place the pick cell
		GridCell e1 = new GridCell(x + 3, y, CELL_TYPE.QUEUE, this);
		grid[x + 3][y] = e1;
		ExitQueue eq1 = new ExitQueue(e1);

		GridCell p1 = new GridCell(x, y + 2, CELL_TYPE.PICK, this);
		EntryQueue q1 = new EntryQueue(p1, eq1);
		grid[x][y + 2] = p1;

		for (int i = 0; i < 4; i++) {
			grid[x + i][y + 3] = new GridCell(x + i, y + 3, CELL_TYPE.QUEUE,
					this);
			grid[x + i][y + 4] = new GridCell(x + i, y + 4, CELL_TYPE.QUEUE,
					this);
			q1.addVertex(grid[x + i][y + 3]);
			q1.addVertex(grid[x + i][y + 4]);
		}
		grid[x + 3][y + 2] = new GridCell(x + 3, y + 2, CELL_TYPE.QUEUE, this);
		q1.addVertex(grid[x + 3][y + 2]);
		grid[x + 3][y + 1] = new GridCell(x + 3, y + 1, CELL_TYPE.QUEUE, this);
		q1.addVertex(grid[x + 3][y + 1]);
		// block some cells
		grid[x + 1][y + 2] = new GridCell(x + 1, y + 2, CELL_TYPE.BLOCKED, this);
		grid[x + 1][y + 1] = new GridCell(x + 1, y + 1, CELL_TYPE.BLOCKED, this);
		grid[x + 2][y + 2] = new GridCell(x + 2, y + 2, CELL_TYPE.BLOCKED, this);
		grid[x + 2][y + 1] = new GridCell(x + 2, y + 1, CELL_TYPE.BLOCKED, this);

		grid[x][y] = new GridCell(x, y, CELL_TYPE.QUEUE, this);
		grid[x + 1][y] = new GridCell(x + 1, y, CELL_TYPE.QUEUE, this);
		grid[x + 2][y] = new GridCell(x + 2, y, CELL_TYPE.QUEUE, this);
		grid[x][y + 1] = new GridCell(x, y + 1, CELL_TYPE.QUEUE, this);
		eq1.addVertex(grid[x][y]);
		eq1.addVertex(grid[x + 1][y]);
		eq1.addVertex(grid[x + 2][y]);
		eq1.addVertex(grid[x][y + 1]);

		eq1.addRoute(grid[x + 2][y + 0], grid[x + 3][y + 0]);
		eq1.addRoute(grid[x + 1][y + 0], grid[x + 2][y + 0]);
		eq1.addRoute(grid[x][y + 0], grid[x + 1][y + 0]);
		eq1.addRoute(grid[x][y + 1], grid[x][y + 0]);

		q1.addRoute(grid[x][y + 3], grid[x][y + 2]);
		q1.addRoute(grid[x][y + 4], grid[x][y + 3]);

		q1.addRoute(grid[x + 1][y + 3], grid[x][y + 3]);
		q1.addRoute(grid[x + 2][y + 3], grid[x + 1][y + 3]);
		q1.addRoute(grid[x + 3][y + 3], grid[x + 2][y + 3]);

		q1.addRoute(grid[x + 1][y + 4], grid[x][y + 4]);
		q1.addRoute(grid[x + 2][y + 4], grid[x + 1][y + 4]);
		q1.addRoute(grid[x + 3][y + 4], grid[x + 2][y + 4]);

		q1.addRoute(grid[x + 3][y + 1], grid[x + 3][y + 2]);
		q1.addRoute(grid[x + 3][y + 2], grid[x + 3][y + 3]);

	}

	private void addTopPick(int x, int y) {
		// place the pick cell
		grid[x + 4][y + 3] = new GridCell(x + 4, y + 3, CELL_TYPE.QUEUE, this);
		GridCell e1 = grid[x + 4][y + 3];
		ExitQueue eq1 = new ExitQueue(e1);

		GridCell p1 = new GridCell(x + 2, y, CELL_TYPE.PICK, this);
		EntryQueue q1 = new EntryQueue(p1, eq1);
		grid[x + 2][y] = p1;
		for (int i = 0; i < 4; i++) {
			grid[x][y + i] = new GridCell(x, y + i, CELL_TYPE.QUEUE, this);
			grid[x + 1][y + i] = new GridCell(x + 1, y + i, CELL_TYPE.QUEUE,
					this);
			q1.addVertex(grid[x][y + i]);
			q1.addVertex(grid[x + 1][y + i]);
		}
		grid[x + 2][y + 3] = new GridCell(x + 2, y + 3, CELL_TYPE.QUEUE, this);
		q1.addVertex(grid[x + 2][y + 3]);
		grid[x + 3][y + 3] = new GridCell(x + 3, y + 3, CELL_TYPE.QUEUE, this);
		q1.addVertex(grid[x + 3][y + 3]);
		// block some cells
		grid[x + 3][y + 2] = new GridCell(x + 3, y + 2, CELL_TYPE.BLOCKED, this);
		grid[x + 3][y + 1] = new GridCell(x + 3, y + 1, CELL_TYPE.BLOCKED, this);
		grid[x + 2][y + 2] = new GridCell(x + 2, y + 2, CELL_TYPE.BLOCKED, this);
		grid[x + 2][y + 1] = new GridCell(x + 2, y + 1, CELL_TYPE.BLOCKED, this);

		grid[x + 4][y] = new GridCell(x + 4, y, CELL_TYPE.QUEUE, this);
		grid[x + 4][y + 1] = new GridCell(x + 4, y + 1, CELL_TYPE.QUEUE, this);
		grid[x + 4][y + 2] = new GridCell(x + 4, y + 2, CELL_TYPE.QUEUE, this);
		grid[x + 3][y] = new GridCell(x + 3, y, CELL_TYPE.QUEUE, this);
		eq1.addVertex(grid[x + 4][y]);
		eq1.addVertex(grid[x + 4][y + 1]);
		eq1.addVertex(grid[x + 4][y + 2]);
		eq1.addVertex(grid[x + 3][y]);

		eq1.addRoute(grid[x + 3][y + 0], grid[x + 4][y + 0]);
		eq1.addRoute(grid[x + 4][y + 0], grid[x + 4][y + 1]);
		eq1.addRoute(grid[x + 4][y + 1], grid[x + 4][y + 2]);
		eq1.addRoute(grid[x + 4][y + 2], grid[x + 4][y + 3]);

		q1.addRoute(grid[x + 1][y], grid[x + 2][y]);
		q1.addRoute(grid[x][y], grid[x + 1][y]);

		q1.addRoute(grid[x][y + 1], grid[x][y + 0]);
		q1.addRoute(grid[x][y + 2], grid[x][y + 1]);
		q1.addRoute(grid[x][y + 3], grid[x][y + 2]);

		q1.addRoute(grid[x + 1][y + 1], grid[x + 1][y + 0]);
		q1.addRoute(grid[x + 1][y + 2], grid[x + 1][y + 1]);
		q1.addRoute(grid[x + 1][y + 3], grid[x + 1][y + 2]);

		q1.addRoute(grid[x + 3][y + 3], grid[x + 2][y + 3]);
		q1.addRoute(grid[x + 2][y + 3], grid[x + 1][y + 3]);

	}

	private void addBottomPick(int x, int y) {
		// place the pick cell
		GridCell e1 = new GridCell(x + 4, y, CELL_TYPE.QUEUE, this);
		ExitQueue eq1 = new ExitQueue(e1);
		grid[x + 4][y] = e1;

		GridCell p1 = new GridCell(x + 2, y + 3, CELL_TYPE.PICK, this);
		EntryQueue q1 = new EntryQueue(p1, eq1);
		grid[x + 2][y + 3] = p1;
		for (int i = 0; i < 4; i++) {
			grid[x][y + i] = new GridCell(x, y + i, CELL_TYPE.QUEUE, this);
			grid[x + 1][y + i] = new GridCell(x + 1, y + i, CELL_TYPE.QUEUE,
					this);
			q1.addVertex(grid[x][y + i]);
			q1.addVertex(grid[x + 1][y + i]);
		}
		grid[x + 2][y] = new GridCell(x + 2, y, CELL_TYPE.QUEUE, this);
		q1.addVertex(grid[x + 2][y]);
		grid[x + 3][y] = new GridCell(x + 3, y, CELL_TYPE.QUEUE, this);
		q1.addVertex(grid[x + 3][y]);
		// block some cells
		grid[x + 3][y + 2] = new GridCell(x + 3, y + 2, CELL_TYPE.BLOCKED, this);
		grid[x + 3][y + 1] = new GridCell(x + 3, y + 1, CELL_TYPE.BLOCKED, this);
		grid[x + 2][y + 2] = new GridCell(x + 2, y + 2, CELL_TYPE.BLOCKED, this);
		grid[x + 2][y + 1] = new GridCell(x + 2, y + 1, CELL_TYPE.BLOCKED, this);

		grid[x + 4][y + 1] = new GridCell(x + 4, y + 1, CELL_TYPE.QUEUE, this);
		grid[x + 4][y + 2] = new GridCell(x + 4, y + 2, CELL_TYPE.QUEUE, this);
		grid[x + 4][y + 3] = new GridCell(x + 4, y + 3, CELL_TYPE.QUEUE, this);
		grid[x + 3][y + 3] = new GridCell(x + 3, y + 3, CELL_TYPE.QUEUE, this);

		eq1.addVertex(grid[x + 4][y + 1]);
		eq1.addVertex(grid[x + 4][y + 2]);
		eq1.addVertex(grid[x + 4][y + 3]);
		eq1.addVertex(grid[x + 3][y + 3]);

		eq1.addRoute(grid[x + 3][y + 3], grid[x + 4][y + 3]);
		eq1.addRoute(grid[x + 4][y + 3], grid[x + 4][y + 2]);
		eq1.addRoute(grid[x + 4][y + 2], grid[x + 4][y + 1]);
		eq1.addRoute(grid[x + 4][y + 1], grid[x + 4][y + 0]);

		q1.addRoute(grid[x + 1][y + 3], grid[x + 2][y + 3]);
		q1.addRoute(grid[x][y + 3], grid[x + 1][y + 3]);

		q1.addRoute(grid[x][y + 0], grid[x][y + 1]);
		q1.addRoute(grid[x][y + 1], grid[x][y + 2]);
		q1.addRoute(grid[x][y + 2], grid[x][y + 3]);

		q1.addRoute(grid[x + 1][y + 0], grid[x + 1][y + 1]);
		q1.addRoute(grid[x + 1][y + 1], grid[x + 1][y + 2]);
		q1.addRoute(grid[x + 1][y + 2], grid[x + 1][y + 3]);

		q1.addRoute(grid[x + 3][y + 0], grid[x + 2][y + 0]);
		q1.addRoute(grid[x + 2][y + 0], grid[x + 1][y + 0]);

	}

	private int edgeDist(GridCell g) {
		int minDist1 = Math.min(g.x, g.y);
		int minDist2 = Math.min((grid.length - g.x - 1),
				(grid[0].length - g.y - 1));

		return Math.min(minDist1, minDist2);
	}

	private void blockEdges() {
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				if ((grid[i][j].isTravel() || grid[i][j].isStorage())
						&& edgeDist(grid[i][j]) <= 3) {

					grid[i][j] = new GridCell(i, j, CELL_TYPE.BLOCKED, this);
				}
			}
		}
	}

	public static Grid makeCustomMedium(int seed) {
		Random r = new Random(seed);

		final int XSZ = 100;
		final int YSZ = 200;

		Grid g = new Grid(XSZ, YSZ);

		for (int i = 0; i < XSZ; i++) {
			g.grid[i] = new GridCell[YSZ];
			for (int j = 0; j < YSZ; j++) {
				g.grid[i][j] = new GridCell(i, j, CELL_TYPE.TRAVEL, g);
			}
		}

		int rowCounter = 1;
		for (int i = 8; i < XSZ - 3; i += 3) {
			if (rowCounter % 4 == 0)
				i++;
			int colCounter = 1;
			for (int j = 8; j < YSZ - 10; j += 6) {
				if (colCounter % 4 == 0)
					j++;
				g.addStorageBlock(i, j);
				colCounter++;
			}
			rowCounter++;
		}

		for (int i = 4; i + 9 < XSZ; i += 5) {
			g.addTopPick(i, 0);
			g.addBottomPick(i, YSZ - 4);
		}

		for (int i = 4; i + 10 < YSZ; i += 10) {
			g.addLeftPick(0, i);
			g.addLeftPickReverse(0, i + 5);
		}
		g.blockEdges();
		g.populateStoragePick(r);
		return g;
	}

	private void makeCompetitionRow(int y, int XSZ) {
		final int BREAK = XSZ / 2 - 4;
		for (int i = 1; i < BREAK - 3; i += 3) {
			addBlockedStorageBlock(i, y + 1);
			addBlockedStorageBlock(i + BREAK + 9, y + 1);
		}
		makeCompetitionQueue(BREAK + 7, y + 3);
		for (int i = BREAK + 5; i < XSZ; i++) {
			grid[i][y + 7] = new GridCell(i, y + 7, CELL_TYPE.BLOCKED, this);
		}
	}

	private void makeSkinnyCompetitionRow(int y, int XSZ) {
		final int BREAK = XSZ / 2 - 4;
		for (int i = 1; i < BREAK - 3; i += 3) {
			addBlockedStorageBlock(i, y + 1);
			addBlockedStorageBlock(i + BREAK + 9, y + 1);
		}
		makeSkinnyCompetitionQueue(BREAK + 7, y + 3);
		for (int i = BREAK + 5; i < XSZ; i++) {
			grid[i][y + 7] = new GridCell(i, y + 7, CELL_TYPE.BLOCKED, this);
		}
	}

	public static Grid makeCustomCompetition(int seed) {
		Random r = new Random(seed);

		final int XSZ = 60;
		final int YSZ = 51;

		Grid g = new Grid(XSZ, YSZ);

		for (int i = 0; i < XSZ; i++) {
			g.grid[i] = new GridCell[YSZ];
			for (int j = 0; j < YSZ; j++) {
				g.grid[i][j] = new GridCell(i, j, CELL_TYPE.TRAVEL, g);
			}
		}

		for (int i = 1; i < YSZ - 8; i += 8) {
			g.makeCompetitionRow(i, XSZ);
		}

		g.populateStoragePick(r);
		return g;
	}

	public static Grid makeCustomSkinnyCompetition(int seed) {
		Random r = new Random(seed);

		final int XSZ = 60;
		final int YSZ = 51;

		Grid g = new Grid(XSZ, YSZ);

		for (int i = 0; i < XSZ; i++) {
			g.grid[i] = new GridCell[YSZ];
			for (int j = 0; j < YSZ; j++) {
				g.grid[i][j] = new GridCell(i, j, CELL_TYPE.TRAVEL, g);
			}
		}

		for (int i = 1; i < YSZ - 8; i += 8) {
			g.makeSkinnyCompetitionRow(i, XSZ);
		}

		g.populateStoragePick(r);
		return g;
	}

	private void makeCompetitionQueue(int x, int y) {
		for (int i = 1; i < 5; i++) {
			grid[x][y + i] = new GridCell(x, y + i, CELL_TYPE.BLOCKED, this);
			grid[x][y - i] = new GridCell(x, y - i, CELL_TYPE.BLOCKED, this);

		}

		grid[x][y] = new GridCell(x, y, CELL_TYPE.PICK, this);
		grid[x + 1][y + 1] = new GridCell(x + 1, y + 1, CELL_TYPE.BLOCKED, this);
		grid[x + 1][y - 1] = new GridCell(x + 1, y - 1, CELL_TYPE.BLOCKED, this);
		// make the exit queue
		grid[x + 1][y] = new GridCell(x + 1, y, CELL_TYPE.QUEUE, this);
		ExitQueue exit = new ExitQueue(grid[x + 1][y]);

		EntryQueue entry = new EntryQueue(grid[x][y], exit);

		for (int i = -1; i <= 1; i++) {
			grid[x - 1][y + i] = new GridCell(x - 1, y + i, CELL_TYPE.QUEUE,
					this);
			entry.addVertex(grid[x - 1][y + i]);
		}
		for (int i = -1; i <= 1; i++) {
			grid[x - 2][y + i] = new GridCell(x - 2, y + i, CELL_TYPE.QUEUE,
					this);
			entry.addVertex(grid[x - 2][y + i]);
		}
		grid[x - 1][y - 3] = new GridCell(x - 1, y - 3, CELL_TYPE.BLOCKED, this);
		grid[x - 1][y + 3] = new GridCell(x - 1, y + 3, CELL_TYPE.BLOCKED, this);
		grid[x - 2][y - 3] = new GridCell(x - 2, y - 3, CELL_TYPE.BLOCKED, this);
		grid[x - 2][y + 3] = new GridCell(x - 2, y + 3, CELL_TYPE.BLOCKED, this);
		grid[x - 1][y - 2] = new GridCell(x - 1, y - 2, CELL_TYPE.BLOCKED, this);
		grid[x - 1][y + 2] = new GridCell(x - 1, y + 2, CELL_TYPE.BLOCKED, this);
		grid[x - 2][y - 2] = new GridCell(x - 2, y - 2, CELL_TYPE.BLOCKED, this);
		grid[x - 2][y + 2] = new GridCell(x - 2, y + 2, CELL_TYPE.BLOCKED, this);

		entry.addRoute(grid[x - 1][y], grid[x][y]);
		entry.addRoute(grid[x - 2][y], grid[x - 1][y]);
		entry.addRoute(grid[x - 1][y - 1], grid[x - 1][y]);
		entry.addRoute(grid[x - 1][y + 1], grid[x - 1][y]);

		entry.addRoute(grid[x - 2][y - 1], grid[x - 1][y - 1]);
		entry.addRoute(grid[x - 2][y + 1], grid[x - 1][y + 1]);

	}

	private void makeSkinnyCompetitionQueue(int x, int y) {
		for (int i = 1; i < 5; i++) {
			grid[x][y + i] = new GridCell(x, y + i, CELL_TYPE.BLOCKED, this);
			grid[x][y - i] = new GridCell(x, y - i, CELL_TYPE.BLOCKED, this);

		}

		grid[x][y] = new GridCell(x, y, CELL_TYPE.PICK, this);
		grid[x + 1][y + 1] = new GridCell(x + 1, y + 1, CELL_TYPE.BLOCKED, this);
		grid[x + 1][y - 1] = new GridCell(x + 1, y - 1, CELL_TYPE.BLOCKED, this);
		// make the exit queue
		grid[x + 1][y] = new GridCell(x + 1, y, CELL_TYPE.QUEUE, this);
		ExitQueue exit = new ExitQueue(grid[x + 1][y]);

		EntryQueue entry = new EntryQueue(grid[x][y], exit);

		for (int i = 0; i <= 0; i++) {
			grid[x - 1][y + i] = new GridCell(x - 1, y + i, CELL_TYPE.QUEUE,
					this);
			entry.addVertex(grid[x - 1][y + i]);
		}
		for (int i = 0; i <= 0; i++) {
			grid[x - 2][y + i] = new GridCell(x - 2, y + i, CELL_TYPE.QUEUE,
					this);
			entry.addVertex(grid[x - 2][y + i]);
		}
		grid[x - 1][y - 3] = new GridCell(x - 1, y - 3, CELL_TYPE.BLOCKED, this);
		grid[x - 1][y + 3] = new GridCell(x - 1, y + 3, CELL_TYPE.BLOCKED, this);
		grid[x - 2][y - 3] = new GridCell(x - 2, y - 3, CELL_TYPE.BLOCKED, this);
		grid[x - 2][y + 3] = new GridCell(x - 2, y + 3, CELL_TYPE.BLOCKED, this);
		grid[x - 1][y - 2] = new GridCell(x - 1, y - 2, CELL_TYPE.BLOCKED, this);
		grid[x - 1][y + 2] = new GridCell(x - 1, y + 2, CELL_TYPE.BLOCKED, this);
		grid[x - 2][y - 2] = new GridCell(x - 2, y - 2, CELL_TYPE.BLOCKED, this);
		grid[x - 2][y + 2] = new GridCell(x - 2, y + 2, CELL_TYPE.BLOCKED, this);

		entry.addRoute(grid[x - 1][y], grid[x][y]);
		entry.addRoute(grid[x - 2][y], grid[x - 1][y]);

	}

	public static Grid makeCustomSmall(int seed) {
		Random r = new Random(seed);
		final int XSZ = 25;
		final int YSZ = 23;

		Grid g = new Grid(XSZ, YSZ);

		for (int i = 0; i < XSZ; i++) {
			g.grid[i] = new GridCell[YSZ];
			for (int j = 0; j < YSZ; j++) {
				g.grid[i][j] = new GridCell(i, j, CELL_TYPE.TRAVEL, g);
			}
		}

		int rowCounter = 1;
		for (int i = 8; i < XSZ - 3; i += 3) {
			if (rowCounter % 4 == 0)
				i++;
			int colCounter = 1;
			for (int j = 5; j < YSZ - 6; j += 6) {
				if (colCounter % 4 == 0)
					j++;
				g.addStorageBlock(i, j);
				colCounter++;
			}
			rowCounter++;
		}

		for (int i = 4; i + 9 < XSZ; i += 5) {
			g.addTopPick(i, 0);
			g.addBottomPick(i, YSZ - 4);
		}

		for (int i = 4; i + 10 < YSZ; i += 10) {
			g.addLeftPick(0, i);
			g.addLeftPickReverse(0, i + 5);
		}
		g.blockEdges();
		g.populateStoragePick(r);
		return g;
	}

	public Grid() {
		int defaultSize = 10;
		this.storageCells = new ArrayList<GridCell>();
		this.pickCells = new ArrayList<GridCell>();
		Random r = new Random();
		grid = new GridCell[defaultSize][defaultSize];
		for (int i = 0; i < defaultSize; i++) {
			for (int j = 0; j < defaultSize; j++) {
				grid[i][j] = new GridCell(i, j, getRandomType(r), this);
				if (grid[i][j].isStorage()) {
					storageCells.add(grid[i][j]);
				}
				if (grid[i][j].isPick()) {
					pickCells.add(grid[i][j]);
				}
			}
		}
	}

	/**
	 * This grid's storage cells
	 */
	public final ArrayList<GridCell> storageCells;
	/**
	 * The high contention cells
	 */
	public final ArrayList<GridCell> pickCells;

	/**
	 * Size of the map in the X direction
	 * 
	 * @return How many cells wide this map is
	 */
	public int xCellCount() {
		return grid[0].length;
	}

	/**
	 * Size of the map in the Y directon
	 * 
	 * @return How many cells tall this map is
	 */
	public int yCellCount() {
		return grid.length;
	}

	@Override
	public List<MultiagentVertex> getAllVertexes() {
		ArrayList<MultiagentVertex> v = new ArrayList<MultiagentVertex>(
				grid.length * grid[0].length);
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				v.add(grid[i][j]);
			}
		}

		return v;
	}

	/**
	 * Writes out some statistics about the map out to standard error.
	 * @param s 
	 */
	public void writeData(PrintStream s) {
		int blocked = 0;
		int open = 0;
		int storage = 0;
		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				if (grid[i][j].isStorage()) {
					storage++;
				} else if (grid[i][j].isBlocked())
					blocked++;
				else
					open++;
			}
		}
		s.printf("blocked %d open %d storage %d\n", blocked, open,
				storage);
		s.printf("x = %d y = %d\n", grid.length, grid[0].length);
	}
	
	public List<CandidateCorridor> findCorridors() {
		LinkedList<CandidateCorridor> candidates = new LinkedList<CandidateCorridor>();
		LinkedList<CorridorFinder> corridors = new LinkedList<CorridorFinder>();
		int[] widths = {2, 3, 4};
		int[] lengths = {9, 11};
		for(int width : widths){
			for(int length : lengths){
				corridors.add(new CorridorFinder(CorridorFinder.DIRECTION.EAST, width, length));
				corridors.add(new CorridorFinder(CorridorFinder.DIRECTION.NORTH, width, length));
			}
		}

		for (int i = 0; i < grid.length; i++) {
			for (int j = 0; j < grid[0].length; j++) {
				for (CorridorFinder f : corridors) {
					if (f.matches(this, i, j)) {
						f.mark(this, i, j);
//						System.err.println(f);
//						System.err.println(f.width);
//						System.err.printf("centered at %s\n",f.getCenters(this, i, j));
						CandidateCorridor c = CandidateCorridor.makeCandidateCorridor(f.getCenters(this, i, j), f.getOrientation());
						boolean mergedOnce = false;
						for(CandidateCorridor candidate : candidates){
							//if they are not adjacent and they are not the same, continue
							if(!candidate.areAdjacentOrSame(c))
								continue;
							candidate.merge(c);
							mergedOnce = true;
						}
						
						//if we didn't merge at all
						if(!mergedOnce){
							candidates.add(c);
						}
					}
				}
			}
		}
		while(true){
			boolean merged = false;
			Iterator<CandidateCorridor> iter = candidates.iterator();
			while(iter.hasNext()){
				CandidateCorridor next = iter.next();
				for(CandidateCorridor c: candidates){
					if(next == c)
						continue;
					if(!next.areAdjacentOrSame(c))
						continue;
					next.merge(c);
					iter.remove();
					merged = true;
					break;
				}
			}
			
			if(!merged)
				break;
		}
		//TODO the candidate corridors have to merge with one another as well, if they are adjacent.
		if(candidates.isEmpty()){
			throw new RuntimeException("No Corridors defined");
		}
		
		return candidates;
	}
	
	private static final int GATEWAY_SIZE = 6;

	public void buildGateways(List<CandidateCorridor> candidates, ReservationTable rt){
		List<AutoQueue.AutoGateway> gateways = new LinkedList<AutoQueue.AutoGateway>();
		for(CandidateCorridor c : candidates){
			DIRECTION d = c.getOrientation();
			AutoGateway g = null;
			if(d == DIRECTION.EAST)
				g = AutoQueue.buildHorizontalGateway(c, GATEWAY_SIZE, rt);
			if(d == DIRECTION.NORTH)
				g = AutoQueue.buildVerticalGateway(c, GATEWAY_SIZE, rt);
			if(g != null)
				gateways.add(g);
		}
		
		for(AutoGateway g : gateways){
			//check to see if the start and the end are connected to one another
			GridCell start = g.inbound.getPrimaryExit();
			GridCell end = g.outbound.getPrimaryExit();
			
			ArrayList<GridCell> path = start.openPath(end);
			if(path == null){
				
			}
			else if(path != null || path.size() < 100){
				g.inbound.remove();
				g.outbound.remove();
			}
			
			HashSet<GridCell> removedInbound = g.inbound.checkVertexes(3, new HashSet<GridCell>());
			HashSet<GridCell> removedOutbound = g.outbound.checkVertexes(3, new HashSet<GridCell>());
			for(GridCell gc : removedInbound){
				gc.changeToStorage();
			}
			for(GridCell gc : removedOutbound){
				gc.changeToStorage();
			}
		}
		this.clearAllHStarTables();
	}
	
	public void clearAllHStarTables(){
		for(GridCell row[] : grid){
			for(GridCell g : row){
				g.clearHStarTable();
			}
		}
	}
	
	public void distFromCorridors(List<CandidateCorridor> candidates){
		LinkedList<GridCell> open = new LinkedList<GridCell>();
		
		for(CandidateCorridor c : candidates){
			for(GridCell g : c.centers){
				open.add(g);
				g.setDistToNearestQueue(0);
			}
		}
		
		while(!open.isEmpty()){
			GridCell next = open.poll();
			List<MultiagentVertex> neighbors = next.getNeighbors();
			for(MultiagentVertex v : neighbors){
				if(v.getCongestion() != Double.MAX_VALUE)
					continue;
				GridCell neighbor = (GridCell) v;
				neighbor.setDistToNearestQueue(next.getCongestion() + 1);
				if(neighbor.getCongestion() > maxQueueDistance){
					this.maxQueueDistance = neighbor.getCongestion();
				}
				open.add(neighbor);
			}
		}
	}
	
	
	double getMaxQueueDistance(){
		return this.maxQueueDistance;
	}
	private double maxQueueDistance = 0;

	
}
