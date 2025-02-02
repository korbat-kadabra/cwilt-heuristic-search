package org.cwilt.search.domains.hanoi;
import java.util.ArrayList;
import java.util.HashMap;

import org.cwilt.search.search.PDB;import org.cwilt.search.search.SearchNodeDepth;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;


public class HanoiPDB extends PDB {
	private final int nDisks;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HanoiPDB(HanoiProblem goal) {
		super(new HashMap<Object, PDBValue>(), SearchNodeDepth
				.makeInitial(goal.getInitial()));
		this.nDisks = goal.nDisks;
	}

	public static void writePDB(HanoiProblem goal, String filename) {
		HanoiPDB p = new HanoiPDB(goal);
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(p);
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				fos.close();
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static Object readPDB(String filename) throws ClassNotFoundException{
		Object p = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			p = in.readObject();
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				fis.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return p;
	}
	public static void main(String[] args) throws ClassNotFoundException{
		int pdbSize = Integer.parseInt(args[1]);
		String path = args[0];
		String[] pdbArgs = new String[0];
		double[] costs = new double[pdbSize];
		for(int i = 0; i < pdbSize; i++){
			costs[i] = 1;
		}
		HanoiProblem g = new HanoiProblem(4, pdbSize, costs, pdbArgs);
		writePDB(g, path);
	}

	public int getnDisks() {
		return nDisks;
	}

	@Override
	protected ArrayList<SearchNodeDepth> makeGoals(SearchNodeDepth canonicalGoal) {
		ArrayList<SearchNodeDepth> toReturn = new ArrayList<SearchNodeDepth>();
		toReturn.add(canonicalGoal);
		return toReturn;
	}
}
