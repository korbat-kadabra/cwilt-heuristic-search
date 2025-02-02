package org.cwilt.search.domains.topspin;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.cwilt.search.domains.topspin.TopspinProblem.Cost;
import org.cwilt.search.search.SearchState;
public class CanonicalTopspinState extends TopspinState {
	
	private final int generateID;
	
	public int getGenerateID(){
		return this.generateID;
	}
	
	private class InvalidState extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8387313241444194828L;

	}

	public CanonicalTopspinState(short[] disks, TopspinProblem p, int generateID) {
		super(disks, p);
		super.rotate(this.findZero());
		this.generateID = generateID;
	}

	public CanonicalTopspinState(int size, TopspinProblem p) {
		super(size, p);
		this.generateID = -1;
	}

	private int findZero() {
		for (int i = 0; i < d.disks.length; i++) {
			if (d.disks[i] == 0)
				return i;
		}
		throw new InvalidState();
	}

	public double distTo(SearchState o) {
		CanonicalTopspinState s = (CanonicalTopspinState) o;
		short[] ary = new short[this.d.disks.length];
		short[] locations = new short[this.d.disks.length];

		for (int i = 0; i < this.d.disks.length; i++) {
			locations[this.d.disks[i]] = (short) i;
		}

		for (int i = 0; i < this.d.disks.length; i++) {
			short diskHere = s.d.disks[i];
			ary[i] = locations[diskHere];
		}

		CanonicalTopspinState other = new CanonicalTopspinState(ary, problem, -1);

		return other.simpleH();
	}

	public CanonicalTopspinState(TopspinProblem p, Random r) {
		super(p, r);
		super.rotate(this.findZero());
		this.generateID = -1;
	}

	/**
	 * Going to make the forwards expand function do parent pruning I think,
	 * because that seems useful.
	 */
	@Override
	public ArrayList<Child> expand() {
		ArrayList<Child> children = new ArrayList<Child>();

		for (int i = 0; i < d.disks.length; i++) {
			if(super.problem.bannedPair(generateID, i))
				continue;
			
			CanonicalTopspinState child = this.copy(i);
			child.rotate(i);
			double cost = super.problem.turnstileCost(child);
			child.turnstile();
			child.rotate(child.findZero());
			children.add(new Child(child, cost));
		}

		// Collections.shuffle(children);
		return children;
	}

	public String toString(){
		return super.toString() + " ID = " + this.generateID;
	}
	
	/**
	 * The reverse expand has to be generous about making ALL of the children.
	 */
	public ArrayList<Child> reverseExpand() {
		ArrayList<Child> children = new ArrayList<Child>();

		for (int i = 0; i < d.disks.length; i++) {
			CanonicalTopspinState child = this.copy(i);
			child.rotate(i);
			double cost = super.problem.turnstileCost(child);
			child.turnstile();
			child.rotate(child.findZero());
			children.add(new Child(child, cost));
		}

		// Collections.shuffle(children);
		return children;
	}

	public CanonicalTopspinState copy(int id) {
		return new CanonicalTopspinState(d.disks.clone(), super.problem, id);
	}

	public static void main(String[] args) throws ClassNotFoundException {
		String[] pdbArgs = new String[0];

		TopspinProblem p = new TopspinProblem(16, 6, Cost.CANONICAL, pdbArgs);

		CanonicalTopspinState s = (CanonicalTopspinState) p.getInitial();
		System.err.println(s.toString());
		System.err.println(s.isGoal());

		System.err.println("children");
		ArrayList<Child> children = s.expand();
		for (Child c : children) {
			System.err.println(c.child + " at a price of " + c.transitionCost);
		}
	}

	protected double simpleH() {
		return problem.calculateH(this);
	}

	@Override
	public double h() {
		double toGoal = problem.calculateH(this);
		// double fromGoal = this.distTo(problem.getGoal());
		// return Math.max(fromGoal, toGoal);
		return toGoal;
	}

	@Override
	public double convertToChild(int id, int parentID) {
		if (id == parentID) {
			return -1;
		}
		
		this.rotate(id);
		this.turnstile();
		this.rotate(this.findZero());
		return 1;
	}

	
	public CanonicalTopspinState clone(){
		return new CanonicalTopspinState(Arrays.copyOf(d.disks, d.disks.length), problem, this.generateID);
	}

}
