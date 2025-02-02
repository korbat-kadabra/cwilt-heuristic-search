package org.cwilt.search.misc.analysis;

import org.cwilt.search.domains.grid.CellGridProblem;
import org.cwilt.search.domains.grid.GridProblem;
import org.cwilt.search.domains.grid.GridState;
import org.cwilt.search.domains.hanoi.HanoiProblem;
import org.cwilt.search.domains.hanoi.HanoiState;
import org.cwilt.search.domains.pancake.PancakeProblem;
import org.cwilt.search.domains.pancake.PancakeState;
import org.cwilt.search.domains.robot.RobotProblem;
import org.cwilt.search.domains.robot.RobotState;
import org.cwilt.search.domains.tiles.TileProblem;
import org.cwilt.search.domains.tiles.TileState;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;

import org.cwilt.search.search.SearchNode;
import org.cwilt.search.utils.basic.MinHeap;

public class Cell {
	public static final Color colors[] = { Color.RED, Color.BLUE, Color.GREEN,
			Color.ORANGE, Color.YELLOW, Color.CYAN };

	private final SearchNode center;
	private final HashSet<Object> region;
	private final HashSet<Object> border;
	private final ArrayList<SearchNode> borderNodes;
	private final ArrayList<Object> core;
	public final ArrayList<Cell> neighbors;

	public int getBranchingFactor() {
		return neighbors.size();
	}

	int coreCount;
	int regionCount;
	int borderCount;
	double coreDepth;
	boolean coreComplete;
	boolean cellComplete;

	private static final SearchNode bfsHillClimb(SearchNode start) {
		double bestH = start.getH();

		HashSet<Object> closed = new HashSet<Object>();
		LinkedList<SearchNode> open = new LinkedList<SearchNode>();

		closed.add(start.getState().getKey());
		open.add(start);

		while (!open.isEmpty()) {
			SearchNode next = open.pop();
			ArrayList<? extends SearchNode> children = next.expand();
			for (SearchNode child : children) {
				if (child.getH() < bestH)
					return child;
				else if (child.getH() == bestH
						&& !closed.contains(child.getState().getKey())) {
					open.add(child);
					closed.add(child.getState().getKey());
				}
			}
		}

		return null;
	}

	private static final SearchNode hillClimb(SearchNode start) {
		while (true) {
			int bestChild = 0;
			ArrayList<? extends SearchNode> children = start.expand();
			for (int i = 1; i < children.size(); i++) {
				if (children.get(i).getH() < children.get(bestChild).getH()) {
					bestChild = i;
				}
			}
			if (children.get(bestChild).getH() > start.getH())
				break;
			else if (children.get(bestChild).getH() == start.getH()) {
				SearchNode next = bfsHillClimb(start);
				if (next != null)
					start = next;
				else
					break;
			} else
				start = children.get(bestChild);
		}

		return start;
	}

	public boolean regionContainsNode(Object key) {
		return region.contains(key);
	}

	public boolean coreContainsNode(Object key) {
		return core.contains(key);
	}

	private boolean neighborCell(Object key) {
		for (Cell c : neighbors) {
			if (c.coreContainsNode(key))
				return true;
		}
		return false;
	}

	public Cell(SearchNode center, boolean createNeighbors, boolean coreOnly) {
		this.center = hillClimb(center);
		this.region = new HashSet<Object>();
		this.border = new HashSet<Object>();
		this.core = new ArrayList<Object>();
		this.coreComplete = false;
		this.neighbors = new ArrayList<Cell>();
		this.borderNodes = new ArrayList<SearchNode>();
		defineRegion(coreOnly);
		// now, if this actually worked, try to do the neighbors
		if (cellComplete && createNeighbors) {
			for (SearchNode bn : borderNodes) {
				if (!neighborCell(bn.getState().getKey())) {
					SearchNode newCenter = hillClimb(bn);
					neighbors.add(new Cell(newCenter, false, false));
				}
			}
		}
	}

	private void defineRegion(boolean coreOnly) {
		System.gc();
		{
			ArrayList<? extends SearchNode> children = center.expand();
			// check the children to make sure this really is a local minimum
			for (SearchNode child : children) {
				assert (child.getH() >= center.getH());
			}
		}
		MinHeap<SearchNode> ol = new MinHeap<SearchNode>(
				new SearchNode.HComparator());
		HashSet<Object> openHash = new HashSet<Object>();
		ol.insert(center);
		openHash.add(center.getState().getKey());
		boolean inCore = true;

		Comparator<SearchNode> n = new SearchNode.HComparator();
		SearchNode lastCore = null;
		ArrayList<Object> nextCore = new ArrayList<Object>();
		try {
			while (!ol.isEmpty()) {

				// if(region.size() % 1000 == 0)
				// System.err.println(region.size() + " nodes expanded");

				SearchNode next = ol.poll();
				openHash.remove(next.getState().getKey());
				region.add(next.getState().getKey());
				// System.err.println(n.compare(next, lastCore));
				if (inCore
						&& (lastCore == null || n.compare(next, lastCore) > 0)) {
					core.addAll(nextCore);
					nextCore.clear();
					lastCore = next;
				} else if (inCore) {
					lastCore = next;
				}
				if (!inCore && coreOnly)
					return;
				regionCount++;
				ArrayList<? extends SearchNode> children = next.expand();

				boolean allInCore = true;
				boolean nextToCore = false;
				for (SearchNode child : children) {
					if (n.compare(child, next) < 0
							&& core.contains(child.getState().getKey())
							&& allInCore) {
						nextToCore = true;
					} else if (n.compare(child, next) < 0) {
						allInCore = false;
					}
				}
				if (allInCore && nextToCore) {
					core.add(next.getState().getKey());
				}

				for (SearchNode child : children) {
					if (region.contains(child.getState().getKey()))
						continue;
					else if (border.contains(child.getState().getKey()))
						continue;
					else if (n.compare(child, next) < 0) {
						if (inCore) {
							inCore = false;
							coreCount = region.size();
							coreDepth = next.getH() - center.getH();
							this.coreComplete = true;
						}
						border.add(child.getState().getKey());
						borderNodes.add(child);
						borderCount++;
					} else if (!openHash.contains(child.getState().getKey())) {
						ol.insert(child);
						openHash.add(child.getState().getKey());
					}
				}
				if (inCore)
					nextCore.add(next.getState().getKey());

			}
			cellComplete = true;
		} catch (java.lang.OutOfMemoryError e) {
			if (inCore) {
				inCore = false;
				coreComplete = false;
				coreCount = region.size();
				coreDepth = lastCore.getH() - center.getH();
			}
			ol.clear();
			openHash.clear();
			System.gc();
		}
		if (inCore) {
			inCore = false;
			coreComplete = true;
			coreCount = region.size();
			assert (lastCore != null);
			assert (center != null);
			coreDepth = lastCore.getH() - center.getH();
		}
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("core complete: ");
		b.append(coreComplete);
		b.append("\ncore size: ");
		b.append(coreCount);
		b.append("\ncore depth: ");
		b.append(coreDepth);
		b.append("\nregion size: ");
		b.append(regionCount);
		b.append("\nborder size: ");
		b.append(borderCount);
		b.append("\nbranching factor: ");
		b.append(neighbors.size());
		return b.toString();
	}

	private static void tiletest() {
		CellAnalysis ca = new CellAnalysis();
		for (int i = 0; i < 100; i++) {
			TileProblem tp = TileProblem.random(4, 4, 100000 * i, "unit");
			TileState ts = new TileState(tp, false);
			ca.addCell(new Cell(SearchNode.makeInitial(ts), true, false));
		}
		System.err.println(ca);
	}

	private static void tiletestinv() {
		CellAnalysis ca = new CellAnalysis();
		for (int i = 0; i < 100; i++) {
			TileProblem tp = TileProblem.random(4, 4, 100000 * i, "inverse");
			TileState ts = new TileState(tp, false);
			ca.addCell(new Cell(SearchNode.makeInitial(ts), false, false));
		}
		System.err.println(ca);
	}

	private static void robottest() throws IOException,
			ParseException {
		CellAnalysis ca = new CellAnalysis();
		for (int i = 1; i <= 40; i++) {
			System.err.println("done " + i);
			String path = "/home/aifs2/group/data/dyn_robot_instances/instance/liney/200/200/25/"
					+ i;
			RobotProblem rp = new RobotProblem(path);
			RobotState rs = rp.getInitial();
			ca.addCell(new Cell(SearchNode.makeInitial(rs), false, false));
		}
		System.err.println(ca);
	}

	private static void robottesttiny() throws IOException,
			ParseException {
		CellAnalysis ca = new CellAnalysis();
		for (int i = 1; i <= 40; i++) {
			System.err.println("working on " + i);
			String path = "robotdata/200/" + i;
			RobotProblem rp = new RobotProblem(path);
			RobotState rs = rp.getInitial();
			ca.addCell(new Cell(SearchNode.makeInitial(rs), false, true));
		}
		System.err.println(ca);
	}

	private static void hanoitest() throws IOException, ClassNotFoundException {
		CellAnalysis ca = new CellAnalysis();
		String[] a = new String[2];
		a[0] = "/home/aifs2/group/data/hanoi/pdb/4/10/java";
		a[1] = "/home/aifs2/group/data/hanoi/pdb/4/2/java";

		double[] costs = new double[12];
		for (int i = 0; i < costs.length; i++) {
			costs[i] = 1;
		}
		HanoiProblem hp = new HanoiProblem(4, 12, costs, a);

		System.err.println("loaded");
		for (int i = 0; i < 100; i++) {
			System.err.println(i);
			hp = HanoiProblem.randomProblem(hp, i);
			HanoiState s = hp.getInitial();
			ca.addCell(new Cell(SearchNode.makeInitial(s), true, false));
		}
		System.err.println(ca);
	}

	// private static void topspintest() throws ClassNotFoundException {
	// CellAnalysis ca = new CellAnalysis();
	// String[] a = new String[1];
	// a[0] = "/home/aifs2/group/data/topspin/pdb/11/4/7/java";
	// TopspinProblem tp = new TopspinProblem(11, 4,
	// TopspinProblem.Cost.CANONICAL,
	// a);
	// System.err.println("done loading");
	// for (int i = 0; i < 1; i++) {
	// System.err.println(i);
	// tp = TopspinProblem.randomProblem(tp, i);
	// TopspinState s = tp.getInitial();
	// ca.addCell(new Cell(SearchNode.makeInitial(s), false, false));
	// }
	// System.err.println(ca);
	// }

	// private static void topspintest2() throws ClassNotFoundException {
	// CellAnalysis ca = new CellAnalysis();
	// String[] a = new String[0];
	// TopspinProblem tp = new TopspinProblem(8, 4,
	// TopspinProblem.Cost.CANONICAL,
	// a);
	// for (int i = 0; i < 100; i++) {
	// tp = TopspinProblem.randomProblem(tp, i);
	// TopspinState s = tp.getInitial();
	// ca.addCell(new Cell(SearchNode.makeInitial(s), true, false));
	// }
	// System.err.println(ca);
	// }

	private static void pancaketest() {
		CellAnalysis ca = new CellAnalysis();
		for (int i = 0; i < 1; i++) {
			PancakeProblem p = new PancakeProblem(i, 9);
			PancakeState s = p.getInitial();
			ca.addCell(new Cell(SearchNode.makeInitial(s), true, false));
		}
		System.err.println(ca);
	}

	private static void gridtest2() throws FileNotFoundException,
			ParseException {
		GridProblem gp = new CellGridProblem(false);
		GridState s = gp.getInitial();
		Cell c = new Cell(SearchNode.makeInitial(s), true, false);
		System.err.println(c);
		gp.setCell(c);
		gp.resetExpGenData();
		GridProblem.displayGridProblem(gp);
	}

	private static void gridtest() throws IOException, ParseException {
		String path = "griddata/50x50";
		CellAnalysis ca = new CellAnalysis();
		for (int i = 1; i <= 20; i++) {
			GridProblem gp = new GridProblem(path, false, true, null);
			GridState s = gp.getInitial();
			ca.addCell(new Cell(SearchNode.makeInitial(s), true, false));
		}
		System.err.println(ca);
	}

	public static void main(String[] args) throws IOException,
			ClassNotFoundException, ParseException {
		if (args[0].compareTo("tile") == 0)
			tiletest();
		else if (args[0].compareTo("hanoi") == 0)
			hanoitest();
		// else if(args[0].compareTo("topspin") == 0)
		// topspintest();
		else if (args[0].compareTo("pancake") == 0)
			pancaketest();
		else if (args[0].compareTo("grid") == 0)
			gridtest();
		// else if(args[0].compareTo("topspin2") == 0)
		// topspintest2();
		else if (args[0].compareTo("grid2") == 0)
			gridtest2();
		else if (args[0].compareTo("robot") == 0)
			robottest();
		else if (args[0].compareTo("robotsmall") == 0)
			robottesttiny();
		else if (args[0].compareTo("tileinv") == 0)
			tiletestinv();
		else
			System.err.println("invlaid selection");
	}
}
