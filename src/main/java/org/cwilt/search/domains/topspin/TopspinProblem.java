package org.cwilt.search.domains.topspin;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import org.cwilt.search.search.SearchState;
public class TopspinProblem implements org.cwilt.search.search.SearchProblem {
	private final int turnstileSize;
	private final int nDisks;
	public final Cost cost;
	private final TopspinState initial;
	private final TopspinState goal;
	private final TopspinPDB[] pdbs;
	// private final TopspinAbstraction[] abs;
	private final int[] inverses;

	private final HashSet<OperatorPair> banned;

	public boolean bannedPair(int parent, int child) {
		if (banned == null)
			return false;
		OperatorPair p = new OperatorPair(parent, child);
		return banned.contains(p);
	}

	public int getInverse(int id) {
		return inverses[id];
	}

	private void initInverses() {
		for (int i = 0; i < nDisks; i++) {
			inverses[i] = i;
		}
		// fix the ones on the end

		inverses[0] = nDisks - turnstileSize + 1;
		inverses[nDisks - turnstileSize + 1] = 0;
		// if turnstile is 4
		if (turnstileSize == 4) {
			inverses[nDisks - 1] = nDisks - 2;
			inverses[nDisks - 2] = nDisks - 1;
		}
		// if turnstile is 3
		if (turnstileSize == 3) {
			// this is already okay.
		}

		// otherwise fail
		if (turnstileSize != 3 && turnstileSize != 4) {
			throw new RuntimeException("invalid turnstile size");
		}
	}

	public int getTurnstileSize() {
		return turnstileSize;
	}

	public static enum HEURISTIC {
		PDB, GAP
	}

	public static enum Cost {
		CANONICAL, SUM, ADDITIVE, STRIPE
	}

	private boolean freeZero = false;

	public void setFreeZero() {
		this.freeZero = true;
	}

	public double turnstileCost(TopspinState s) {
		if (cost == Cost.ADDITIVE) {
			double sum = 0;
			for (int i = 0; i < this.turnstileSize; i++) {
				if (s.d.disks[i] == 0 && freeZero)
					continue;
				if (s.d.disks[i] != TopspinState.ABSTRACTED)
					sum += 1.0 / turnstileSize;
			}
			return sum;
		} else if (cost == Cost.CANONICAL)
			return 1.0;
		else if (cost == Cost.SUM) {
			double sum = 0;
			for (int i = 0; i < this.turnstileSize; i++) {
				if (s.d.disks[i] != TopspinState.ABSTRACTED)
					sum += this.nDisks - s.d.disks[i];
			}
			return sum;
		} else if (cost == Cost.STRIPE){
			double sum = 0;
			for (int i = 0; i < this.turnstileSize; i++) {
				if (s.d.disks[i] != TopspinState.ABSTRACTED){
					double toAdd;
					if (s.d.disks[i] % 2 == 0)
						toAdd = 1;
					else
						toAdd = STRIPE_COST;
					sum += toAdd;
				}
			}
			return sum;
		} else {
			throw new RuntimeException("no way to calculate cost");
		}
	}
	private static final double STRIPE_COST = 10;
	
	public double rotateCost(int distance) {
		return 0;
	}

	public double calculateH(TopspinState s) {
		double newH = 0;
		if (h == HEURISTIC.PDB) {
			for (int i = 0; i < pdbs.length; i++) {
				Object abstractedState = pdbs[i].getAbstraction()
						.abstractState(s);
				newH += pdbs[i].getH(abstractedState);
			}
		} else if (h == HEURISTIC.GAP) {
			return s.gap();
		}
		return newH;
	}

	public int calculateD(TopspinState s) {
		int d = 0;
		for (int i = 0; i < pdbs.length; i++) {
			Object abstractedState = pdbs[i].getAbstraction().abstractState(s);
			d += pdbs[i].getD(abstractedState);
		}
		return d;
	}

	// public static final TopspinProblem randomProblem(TopspinProblem init,
	// int seed) {
	// Random r = new Random(seed);
	//
	// TopspinProblem toReturn = new TopspinProblem(init, r);
	//
	// return toReturn;
	// }

	private final HEURISTIC h;

	// private TopspinProblem(TopspinProblem p, Random r) {
	// this.h = p.h;
	// this.turnstileSize = p.turnstileSize;
	// this.nDisks = p.nDisks;
	// this.cost = p.cost;
	// this.initial = new TopspinState(this, r);
	// this.goal = new TopspinState(nDisks, this);
	// this.pdbs = p.pdbs;
	// this.inverses = p.inverses;
	// }

	public int getNDisks() {
		return nDisks;
	}

	private void loadPDBS(String[] pdbArgs) throws ClassNotFoundException {
		for (int i = 0; i < pdbArgs.length; i++) {
			pdbs[i] = TopspinPDB.readPDB(pdbArgs[i]);
		}
	}

	public TopspinProblem(int nDisks, int turnstileSize, Cost c,
			String[] pdbArgs) throws ClassNotFoundException {
		this.banned = null;
		this.nDisks = nDisks;
		this.turnstileSize = turnstileSize;
		this.cost = c;
		if (this.cost == Cost.CANONICAL || this.cost == Cost.SUM
				|| this.cost == Cost.ADDITIVE|| this.cost == Cost.STRIPE) {
			this.initial = new CanonicalTopspinState(nDisks, this);
			this.goal = new CanonicalTopspinState(nDisks, this);
			this.inverses = new int[nDisks];
			initInverses();
		} else {
			this.initial = new TopspinState(nDisks, this);
			this.goal = new TopspinState(nDisks, this);
			this.inverses = null;
			throw new RuntimeException("This shouldn't get called");
		}
		this.pdbs = new TopspinPDB[pdbArgs.length];

		if (pdbArgs.length != 0) {
			loadPDBS(pdbArgs);
			this.h = HEURISTIC.PDB;
		} else {
			this.h = HEURISTIC.GAP;
		}
	}

	public TopspinProblem(String path, String pdbArgs[], String cost)
			throws IOException, ClassNotFoundException {
		FileInputStream fs = new FileInputStream(path);
		DataInputStream ds = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(ds));
		Scanner s = new Scanner(br);
		this.nDisks = s.nextInt();
		this.turnstileSize = s.nextInt();
		short[] initialArray = new short[nDisks];
		for (int i = 0; i < nDisks; i++) {
			initialArray[i] = s.nextShort();
		}

		this.initial = new CanonicalTopspinState(initialArray, this, -1);
		this.goal = new CanonicalTopspinState(nDisks, this);

		if (cost.equals("canonical")) {
			this.inverses = new int[nDisks];
			initInverses();
			this.cost = Cost.CANONICAL;
			this.banned = null;
		} else if (cost.equals("canonical_rob")) {
			this.inverses = new int[nDisks];
			initInverses();
			this.cost = Cost.CANONICAL;
			this.banned = TopspinTest.getBanned(this);
		} else if (cost.equals("sum")) {
			this.inverses = null;
			this.cost = Cost.SUM;
			this.banned = null;
		} else if (cost.equals("stripe")) {
			this.inverses = null;
			this.cost = Cost.STRIPE;
			this.banned = null;
		} else {
			s.close();
			br.close();
			throw new RuntimeException("Invalid cost");
		}

		this.pdbs = new TopspinPDB[pdbArgs.length];

		if (pdbArgs.length != 0) {
			loadPDBS(pdbArgs);
			this.h = HEURISTIC.PDB;
		} else {
			this.h = HEURISTIC.GAP;
		}

		for (TopspinPDB p : this.pdbs) {
			p.checkPDB(this);
		}
		br.close();
		s.close();

	}

	public TopspinState getInitial() {
		return initial;
	}

	@Override
	public ArrayList<SearchState> getGoals() {
		ArrayList<SearchState> goals = new ArrayList<SearchState>();

		CanonicalTopspinState toAdd = new CanonicalTopspinState(nDisks, this);
		goals.add(toAdd);

		return goals;
	}

	public SearchState getGoal() {
		if (cost == Cost.CANONICAL)
			return goal;
		else if (cost == Cost.SUM)
			return goal;
		else if (cost == Cost.STRIPE)
			return goal;
		else {
			throw new org.cwilt.search.search.NoCanonicalGoal();
		}
	}

	@Override
	public void setCalculateD() {
	}

	@Override
	public void printProblemData(PrintStream ps) {
	}

}
