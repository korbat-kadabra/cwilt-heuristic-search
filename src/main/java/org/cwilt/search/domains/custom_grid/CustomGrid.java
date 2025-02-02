package org.cwilt.search.domains.custom_grid;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

import org.cwilt.search.algs.basic.bestfirst.AStar;
import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchState;
import org.cwilt.search.utils.basic.Heapable;
import org.cwilt.search.utils.basic.MinHeap;
public class CustomGrid implements org.cwilt.search.search.SearchProblem {

	public final Random r;
	public final CustomGridCell[][] map;
	public final int height, width;
	private CustomGridCell start;
	private final CustomGridCell goal;
	private double hError;

	// private final double hdGoal, hhGoal;
	// private double dStdev;

	private double dAverage;

	public CustomGrid(int seed, String path, double hd, double hh, double hErr)
			throws IOException, ParseException {
		if (hh < 0 || hh > 1) {
			throw new IllegalArgumentException("hh");
		}
		if (hd < 0 || hd > 1) {
			throw new IllegalArgumentException("hd");
		}
		if (hErr < 0 || hErr > 1) {
			throw new IllegalArgumentException("hErr");
		}

		// this.hdGoal = hd;
		// this.hhGoal = hh;
		this.hError = hErr;

		this.r = new Random(seed);
		FileInputStream fs = new FileInputStream(path);
		DataInputStream ds = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(ds));
		Scanner s = new Scanner(br);
		try {
			height = s.nextInt();
			width = s.nextInt();
			s.nextLine();
			s.nextLine();
			map = new CustomGridCell[height][width];
			for (int i = 0; i < height; i++) {
				String row = s.nextLine();
				for (int j = 0; j < width; j++) {
					CustomGridCell.STATUS stat;
					char c = row.charAt(j);
					if (c == ' ')
						stat = CustomGridCell.STATUS.OPEN;
					else if (c == '#')
						stat = CustomGridCell.STATUS.BLOCKED;
					else {
						throw new ParseException(row, j);
					}
					map[i][j] = new CustomGridCell(j, i, this, stat);
				}
			}
			s.nextLine();
			s.nextLine();
			int xStart = s.nextInt();
			int yStart = height - s.nextInt() - 1;
			this.start = map[yStart][xStart];
			int xGoal = s.nextInt();
			int yGoal = height - s.nextInt() - 1;
			this.goal = map[yGoal][xGoal];
		} finally {
			br.close();
			s.close();
		}
		connectGrid();
		setDStar();
		setHStar();
		setH();
		checkHStar();
	}

	private void checkHStar() {
		double slop = 0.0001;
		MinHeap<CustomGridCell> q = new MinHeap<CustomGridCell>(
				new CustomGridCell.GComparator());
		HashSet<CustomGridCell> closed = new HashSet<CustomGridCell>();
		goal.g = 0;
		q.add(goal);
		while (!q.isEmpty()) {
			CustomGridCell next = q.poll();
			if (closed.contains(next))
				continue;
			closed.add(next);
			for (int i = 0; i < next.neighbors.size(); i++) {
				CustomGridCell c = next.neighbors.get(i);
				double nextG = next.g + next.costs.get(i);
				if (nextG < c.g) {
					if (c.getHeapIndex() != Heapable.NO_POS)
						q.removeAt(c.getHeapIndex());
					q.add(c);
					c.g = nextG;
				}
			}
		}
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				double diff = Math.abs(map[j][i].g - map[j][i].hStar);
				if (diff < slop || !map[j][i].isOpen()) {

				} else {
					CustomGridCell error = map[j][i];
					System.err.printf("%3e %3e %d %d open: %b\n", error.hStar,
							error.g, error.x, error.y, error.isOpen());
				}
			}
		}

	}

	private void connectGrid() {
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				if (map[i][j].isOpen())
					map[i][j].connectGrid();
				map[i][j].dStar = Integer.MAX_VALUE;
				map[i][j].h = Double.MAX_VALUE;
				map[i][j].hStar = Double.MAX_VALUE;
			}
		}
	}

	private void setDStar() {
		Queue<CustomGridCell> q = new LinkedList<CustomGridCell>();
		HashSet<CustomGridCell> closed = new HashSet<CustomGridCell>();
		CustomGridCell goal = (CustomGridCell) getGoal();
		q.add(goal);
		goal.dStar = 0;
		double dSum = 0;
		double dCount = 1;
		while (!q.isEmpty()) {
			CustomGridCell next = q.poll();
			if (closed.contains(next))
				continue;
			closed.add(next);
			for (CustomGridCell c : next.neighbors) {
				if (c.dStar == Integer.MAX_VALUE) {
					c.dStar = next.dStar + 1;
					q.add(c);
					dSum += c.dStar;
					dCount++;
				}
			}
		}
		this.dAverage = dSum / dCount;
		double dVariance = 0;
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				CustomGridCell g = map[i][j];
				if (!g.isOpen())
					continue;
				dVariance += Math.pow(dAverage - map[i][j].dStar, 2);
			}
		}
		dVariance = (dVariance / dCount);
		// dStdev = Math.sqrt(dVariance);
	}

	private void setH() {
		@SuppressWarnings("unused")
		double totalH = 0;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (map[i][j].isOpen()) {
					totalH += map[i][j].hStar;
					map[i][j].h = hError * map[i][j].hStar;
				} else {
				}
			}
		}
	}

	private void setHStar() {
		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				CustomGridCell g = map[i][j];
				if (!g.isOpen())
					continue;
				g.h = 0;

				for (int ix = 0; ix < g.neighbors.size(); ix++) {
					g.costs.add(1.0 + r.nextDouble());
				}
			}
		}
		MinHeap<CustomGridCell> q = new MinHeap<CustomGridCell>(
				new CustomGridCell.HStarComparator());
		HashSet<CustomGridCell> closed = new HashSet<CustomGridCell>();
		CustomGridCell goal = (CustomGridCell) getGoal();
		q.add(goal);
		goal.hStar = 0;
		while (!q.isEmpty()) {
			CustomGridCell next = q.poll();
			assert (next.getHeapIndex() == Heapable.NO_POS);
			if (closed.contains(next))
				continue;
			closed.add(next);
			for (int i = 0; i < next.neighbors.size(); i++) {
				CustomGridCell c = next.neighbors.get(i);
				double totalHStar = next.hStar + next.costs.get(i);
				if (totalHStar < c.hStar) {
					if (c.getHeapIndex() != Heapable.NO_POS)
						q.removeAt(c.getHeapIndex());
					c.hStar = totalHStar;
					q.add(c);
				}
			}
		}
		assert (q.isEmpty());
	}

	@Override
	public SearchState getGoal() {
		return goal;
	}

	@Override
	public ArrayList<SearchState> getGoals() {
		ArrayList<SearchState> toReturn = new ArrayList<SearchState>();
		toReturn.add(getGoal());
		return toReturn;
	}

	@Override
	public SearchState getInitial() {
		return start;
	}

	public static void main(String[] args) {
		try {
			CustomGrid c = new CustomGrid(0, args[0], .5, .5, .7);
			SearchAlgorithm a = new AStar(c, new Limit());
			a.solve();
			a.printSearchData(System.out);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public void changeInitial(int seed) {
		Random init = new Random(seed);
		CustomGridCell s = null;
		while (s == null) {
			int sx = init.nextInt(this.map[0].length);
			int sy = init.nextInt(this.map.length);
			if (map[sy][sx].isOpen()) {
				s = map[sy][sx];
			}
		}
		this.start = s;
	}

	public String toString() {
		StringBuffer b = new StringBuffer();

		for (int i = 0; i < map.length; i++) {
			for (int j = 0; j < map[0].length; j++) {
				b.append(map[i][j]);
				b.append(" ");
			}
			b.append("\n");
		}
		return b.toString();
	}

	@Override
	public void setCalculateD() {
	}

	public void printProblemData(PrintStream p) {

	}

}
