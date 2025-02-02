package org.cwilt.search.domains.pancake;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;

import org.cwilt.search.search.PDB;import org.cwilt.search.search.SearchNodeDepth;public class PancakePDB extends PDB {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1034988331259491822L;
	
	private final PancakeProblem.COST cost;
	private final int nCakes;
	
	public final class IncompatiblePDB extends Exception{
		/**
		 * 
		 */
		private static final long serialVersionUID = 3172887944379452273L;

		public IncompatiblePDB(String s){
			super(s);
		}
	}
	
	/**
	 * 
	 * @param p Pancake problem to check for compatibility with this pattern database
	 * @throws IncompatiblePDB 
	 */
	public void PDBMatches(PancakeProblem p) throws IncompatiblePDB{
		if(this.cost != p.getCost()){
			throw new IncompatiblePDB("Costs don't match");
		}
		else if(this.nCakes != p.getInitial().getNCakes()){
			throw new IncompatiblePDB("Number of pancakes doesn't match");
		}
	}
	
	protected PancakePDB(PancakeProblem p, BitSet a) {
		super(new HashMap<Object, PDBValue>(), SearchNodeDepth.makeInitial(p
				.getInitial().abstractState(a)));
		this.cost = p.getCost();
		this.nCakes = p.getInitial().getNCakes();
		this.abstraction = a;
	}

	@Override
	protected ArrayList<SearchNodeDepth> makeGoals(SearchNodeDepth canonicalGoal) {
		ArrayList<SearchNodeDepth> goals = new ArrayList<SearchNodeDepth>(1);
		goals.add(canonicalGoal);
		return goals;
	}

	public static void writePDB(PancakeProblem goal, String filename, BitSet a) {
		PancakePDB p = new PancakePDB(goal, a);
//		p.printPDB();
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(p);
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if(out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static PancakePDB readPDB(String filename)
			throws ClassNotFoundException {
		PancakePDB p = null;
		ObjectInputStream in = null;
		try {
			FileInputStream fis = null;
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			p = (PancakePDB) in.readObject();
			fis.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException opx) {
					opx.printStackTrace();
				}
			}
		}
		return p;
	}
	
	private final BitSet abstraction;
	public BitSet getAbstraction(){
		return this.abstraction;
	}
	
	public static void main(String[] args){
		BitSet b = new BitSet();
		
		String path = args[0];
		int nCakes = Integer.parseInt(args[1]);
		PancakeProblem.COST cost;
		
		if(args[2].equals("unit")){
			cost = PancakeProblem.COST.UNIT;
		} else if(args[2].equals("sum"))
			cost = PancakeProblem.COST.SUM;
		else
			throw new RuntimeException("Invalid Cost selection: " + args[2]);
		
		for(int i = 0; i < nCakes; i++){
			b.set(i);
		}
		
		for(int i = 3; i < args.length; i++){
			int current = Integer.parseInt(args[i]);
			b.clear(current);
		}
		writePDB(new PancakeProblem(nCakes, cost), path, b);
	}

}
