package org.cwilt.search.domains.vacuum;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Scanner;

import org.cwilt.search.search.NoCanonicalGoal;import org.cwilt.search.search.SearchState;
public class VacuumProblem implements org.cwilt.search.search.SearchProblem {
	public enum CELLTYPE {
		DIRT, CLEAR, BLOCKED, CHARGE
	}

	public class VacuumCell {
		public final int x, y;
		public final CELLTYPE celltype;
		public final int dirtID;

		public VacuumCell(int x, int y, CELLTYPE ct, int dirtID) {
			this.x = x;
			this.y = y;
			this.celltype = ct;
			this.dirtID = dirtID;
			if (celltype == CELLTYPE.DIRT) {
				assert (dirtID != -1);
			} else {
				assert (dirtID == -1);
			}
		}
	}

	public final VacuumCell[][] world;
	public final int height, width;
	public int startX, startY;

	private final ArrayList<VacuumCell> dirts;

	private void parseAIVacuum(Scanner s) throws ParseException {
		for (int i = 0; i < width; i++) {
			world[i] = new VacuumCell[height];
			String row = s.nextLine();
			for (int j = 0; j < height; j++) {
				CELLTYPE t;
				char c = row.charAt(j);
				if (c == '_')
					t = CELLTYPE.CLEAR;
				else if (c == '#')
					t = CELLTYPE.BLOCKED;
				else if (c == '*') {
					t = CELLTYPE.DIRT;
				} else if (c == ':')
					t = CELLTYPE.CHARGE;
				else if (c == '@') {
					t = CELLTYPE.CLEAR;
					startX = i;
					startY = j;
				} else {
					throw new ParseException(row, j);
				}
				int dirtID = -1;
				if (t == CELLTYPE.DIRT) {
					dirtID = dirts.size();
				}
				world[i][j] = new VacuumCell(i, j, t, dirtID);
				if (t == CELLTYPE.DIRT) {
					dirts.add(world[i][j]);
				}
			}
		}

	}

	private void parseResearchVacuum(Scanner s) throws ParseException {
		for (int i = 0; i < width; i++) {
			world[i] = new VacuumCell[height];
			String row = s.nextLine();
			for (int j = 0; j < height; j++) {
				CELLTYPE t;
				char c = row.charAt(j);
				if (c == ' ')
					t = CELLTYPE.CLEAR;
				else if (c == '#')
					t = CELLTYPE.BLOCKED;
				else if (c == '*') {
					t = CELLTYPE.DIRT;
				} else if (c == ':')
					t = CELLTYPE.CHARGE;
				else if (c == 'V') {
					t = CELLTYPE.CLEAR;
					startX = i;
					startY = j;
				} else {
					throw new ParseException(row, j);
				}
				int dirtID = -1;
				if (t == CELLTYPE.DIRT) {
					dirtID = dirts.size();
				}
				world[i][j] = new VacuumCell(i, j, t, dirtID);
				if (t == CELLTYPE.DIRT) {
					dirts.add(world[i][j]);
				}
			}
		}
	}
	public VacuumProblem(String path) throws IOException, ParseException {
		this.dirtMST = new HashMap<BitSet, Double>();
		FileInputStream fs = new FileInputStream(path);
		DataInputStream ds = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(ds));
		Scanner s = new Scanner(br);
		this.dirts = new ArrayList<VacuumCell>();
		height = s.nextInt();
		width = s.nextInt();
		s.nextLine();
		world = new VacuumCell[width][];
		try {
			parseAIVacuum(s);
		} catch (ParseException e) {
			parseResearchVacuum(s);
		}
		s.close();
		br.close();
	}

	@Override
	public SearchState getInitial() {
		VacuumCell[] s = dirts.toArray(new VacuumCell[dirts.size()]);
		return new VacuumState(startX, startY, s, this);
	}

	@Override
	public SearchState getGoal() {
		throw new NoCanonicalGoal();
	}

	@Override
	public ArrayList<SearchState> getGoals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCalculateD() {
	}

	@Override
	public void printProblemData(PrintStream ps) {
	}

	public static final double manhattan(VacuumCell c1, VacuumCell c2) {
		return Math.abs(c1.x - c2.x) + Math.abs(c1.y - c2.y);
	}

	private final HashMap<BitSet, Double> dirtMST;

	public double mst(VacuumState s) {
		double nearestDirt = Double.MAX_VALUE;
		double dirtCount = 0;
		BitSet dirtBitSet = new BitSet(s.dirts.length);
		for (int i = 0; i < s.dirts.length; i++) {
			if (s.dirts[i] != null) {
				dirtBitSet.set(i);
				dirtCount++;
				double dirtDistance = Math.abs(s.dirts[i].x - s.x)
						+ Math.abs(s.dirts[i].y - s.y);
				if (dirtDistance < nearestDirt) {
					nearestDirt = dirtDistance;
				}
			}
		}
		Double mstValue = dirtMST.get(dirtBitSet);
		if (mstValue == null) {
			mstValue = new MinimumSpanningTree(s.dirts).getCost();
			dirtMST.put(dirtBitSet, mstValue);
		}
		if (nearestDirt == Double.MAX_VALUE) {
			assert (dirtCount == 0);
		}
		if (dirtCount == 0) {
			nearestDirt = 0;
		}

		return mstValue + dirtCount + nearestDirt;
	}

	public double dirtBox(VacuumState s) {
		double nearestDirt = Double.MAX_VALUE;
		double dirtCount = 0;

		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = 0;
		int maxY = 0;

		for (VacuumCell c : s.dirts) {
			if (c == null)
				continue;
			dirtCount++;
			double manhattanHere = manhattan(c, world[s.x][s.y]);
			if (manhattanHere < nearestDirt)
				nearestDirt = manhattanHere;
			if (c.x < minX)
				minX = c.x;
			if (c.y < minY)
				minY = c.y;
			if (c.x > maxX)
				maxX = c.x;
			if (c.y > maxY)
				maxY = c.y;
		}
		if (nearestDirt == Double.MAX_VALUE) {
			nearestDirt = 0;
		}
		assert (nearestDirt < Double.MAX_VALUE);
		double dirtPickup = (maxX - minX) + (maxY - minY);
		return nearestDirt + dirtCount + dirtPickup;

	}

	public double calculateH(VacuumState s) {
		// return 0;
		return mst(s);
	}

}
