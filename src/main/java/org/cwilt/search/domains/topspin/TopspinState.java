package org.cwilt.search.domains.topspin;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import org.cwilt.search.domains.topspin.TopspinProblem.Cost;
import org.cwilt.search.search.SearchState;
public class TopspinState extends SearchState implements Cloneable{

	public double gap(){
		int gapCount = 0;
		for(int i = 1; i < d.disks.length; i++){
			if(Math.abs(d.disks[i-1] - d.disks[i]) != 1){
				gapCount++;
			}
		}
		if(Math.abs(d.disks[0] - d.disks[d.disks.length - 1]) != 1){
			gapCount++;
		}
		gapCount = gapCount / 2;
		return gapCount;
	}
	
	protected static class Disks implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2237238573357209938L;
		public final short[] disks;
		public Disks(short[] disks){
			this.disks = disks;
		}
		
		public String toString(){
			return Arrays.toString(disks);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(disks);
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
			Disks other = (Disks) obj;
			if (!Arrays.equals(disks, other.disks))
				return false;
			return true;
		}
		
	}
	
	protected final Disks d;
	protected final TopspinProblem problem;

	public TopspinState clone(){
		return new TopspinState(Arrays.copyOf(d.disks, d.disks.length), problem);
	}
	
	public TopspinState(TopspinProblem p, Random r){
		this.d = new Disks(new short[p.getNDisks()]);
		for(short i = 0; i < p.getNDisks(); i++){
			d.disks[i] = i;
		}
		for(short i = 0; i < p.getNDisks(); i++){
			int nextIX = i + r.nextInt(p.getNDisks() - i);
			short temp = d.disks[i];
			d.disks[i] = d.disks[nextIX];
			d.disks[nextIX] = temp;
		}
		this.problem = p;
	}
	
	/**
	 * makes a goal configuration. One of possibly many.
	 * 
	 * @param size
	 * @param p 
	 */
	public TopspinState(int size, TopspinProblem p) {
		this.problem = p;
		this.d = new Disks(new short[size]);
		for (short i = 0; i < size; i++) {
			d.disks[i] = i;
		}
	}
	
	public TopspinState(short[] disks, TopspinProblem p){
		this.d = new Disks(disks);
		this.problem = p;
	}

	protected final void rotate(int k) {
		int c, v;
		int n = d.disks.length;
		if (k < 0 || k >= n) {
			k %= n;
			if (k < 0)
				k += n;
		}
		if (k == 0)
			return;
		c = 0;
		for (v = 0; c < n; v++) {
			int t = v, tp = v + k;
			short tmp = d.disks[v];
			c++;
			while (tp != v) {
				d.disks[t] = d.disks[tp];
				t = tp;
				tp += k;
				if (tp >= n)
					tp -= n;
				c++;
			}
			d.disks[t] = tmp;
		}
		return;
	}

	protected void turnstile(){
		int i, j;
		int n = problem.getTurnstileSize() - 1;
		j = problem.getTurnstileSize() / 2;
		for(i = 0; i < j; i++){
			short temp = d.disks[i];
			d.disks[i] = d.disks[n-i];
			d.disks[n-i] = temp;
		}
	}
	
	public String toString() {
		StringBuffer b = new StringBuffer();
		for (short s : d.disks) {
			b.append(s);
			b.append(" ");
		}
		return b.toString();
	}

	@Override
	public ArrayList<Child> expand() {
		ArrayList<Child> children = new ArrayList<Child>();
		TopspinState turnstileChild = this.clone();
		turnstileChild.turnstile();
		children.add(new Child(turnstileChild, problem.turnstileCost(turnstileChild)));

		for(int i = 1; i < d.disks.length; i++){
			TopspinState rotateChild = this.clone();
			rotateChild.rotate(i);
			children.add(new Child(rotateChild, problem.rotateCost(i)));
		}
		return children;
	}

	@Override
	public Object getKey() {
		return d;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + d.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		TopspinState other = (TopspinState) obj;
		if (!d.equals(other.d))
			return false;
		return true;
	}

	private final boolean disksMatch(int ix) {
		short disk1 = d.disks[ix];
		short disk2;
		if (ix + 1 >= d.disks.length)
			disk2 = d.disks[0];
		else
			disk2 = d.disks[ix + 1];
		if (disk1 + 1 == disk2)
			return true;
		else if (disk1 == disk2 + d.disks.length - 1)
			return true;
		else
			return false;
	}

	@Override
	public boolean isGoal() {
		for (int i = 0; i < d.disks.length - 1; i++) {
			if (!disksMatch(i))
				return false;
		}
		return true;
	}

	@Override
	public int lexOrder(SearchState s) {
		assert (false);
		System.err.println("cannot use lex order with topspin");
		System.exit(1);
		return 0;
	}
	
	static final short ABSTRACTED = -1;

	public TopspinState abstractState(TopspinAbstraction a){
		return a.abstractStateAsState(this);
	}
	public TopspinState abstractState(int startIX, int endIX){
		short[] ary = Arrays.copyOf(d.disks, d.disks.length);
		for(int i = 0; i < d.disks.length; i++){
			short thisValue = d.disks[i];
			if(thisValue == 0)
				ary[i] = thisValue;
			else if(thisValue >= startIX && thisValue <= endIX)
				ary[i] = thisValue;
			else
				ary[i] = ABSTRACTED;
		}
		if(problem.cost == Cost.CANONICAL || problem.cost == Cost.ADDITIVE)
			return new CanonicalTopspinState(ary, problem, -1);
		else{
			assert(false);
			return new TopspinState(ary, problem);
		}
			
	}

	public TopspinState abstractState(BitSet abstracted){
		short[] ary = Arrays.copyOf(d.disks, d.disks.length);
		for(int i = 0; i < d.disks.length; i++){
			short thisValue = d.disks[i];
			if(thisValue == 0){
//				if(abstracted.get(thisValue)){
//					throw new RuntimeException("Invalid Abstraction Function");
//				}
				ary[i] = thisValue;
			}
			else if(!abstracted.get(thisValue))
				ary[i] = thisValue;
			else
				ary[i] = ABSTRACTED;
		}
		if(problem.cost == Cost.CANONICAL || problem.cost == Cost.ADDITIVE)
			return new CanonicalTopspinState(ary, problem, -1);
		else{
			assert(false);
			return new TopspinState(ary, problem);
		}
			
	}

	
	public static void main(String[] args) throws ClassNotFoundException {
		String[] pdbArgs = new String[0];
		
		TopspinProblem p = new TopspinProblem(16, 6, Cost.CANONICAL, pdbArgs);
		
		TopspinState s = p.getInitial();
		System.err.println(s.toString());
		System.err.println(s.isGoal());

		s.rotate(16);
		System.err.println(s.toString());
		System.err.println(s.isGoal());

/*		
		s.turnstile();
		System.err.println(s.toString());
		System.err.println(s.isGoal());
	*/	
		s = s.abstractState(5, 10);
		System.err.println(s.toString());
		System.err.println(s.isGoal());
		
	}

	@Override
	public ArrayList<Child> reverseExpand() {
		return expand();
	}

	protected double simpleH(){
		return problem.calculateH(this);
	}
	
	@Override
	public double h() {
		return simpleH();
	}

	@Override
	public int d() {
		return problem.calculateD(this);
	}
	public int nChildren(){
		return d.disks.length;
	}
	public int inverseChild(int id){
		return problem.getInverse(id);
	}
}
