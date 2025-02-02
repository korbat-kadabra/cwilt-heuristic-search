package org.cwilt.search.domains.topspin;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.cwilt.search.domains.topspin.TopspinProblem.Cost;
import org.cwilt.search.search.PDB;import org.cwilt.search.search.SearchNodeDepth;
public class TopspinPDB extends PDB {
	private final Cost cost;
	private final int nDisks;
	private final int turnstileSize;
	private final int nRealDisks;

	/**
	 * 
	 */
	private static final long serialVersionUID = 9162902085284714451L;

	protected TopspinPDB(TopspinProblem p, int nDisks, Cost c, TopspinAbstraction a) {
		super(new HashMap<Object, PDBValue>(), 
				SearchNodeDepth.makeInitial(p.getInitial().abstractState(a)));
		this.a = a;
		assert(c != null);
		this.cost = c;
		this.nDisks = p.getInitial().d.disks.length;
		this.turnstileSize = p.getTurnstileSize();
		this.nRealDisks = nDisks;
	}

	public class InvalidPDB extends RuntimeException {
		@Override
		public String getMessage() {
			return "the pattern database and the problem don't match";
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
	}

	public static void writePDB(TopspinProblem goal, String filename,
			int totalDisks, Cost cost, TopspinAbstraction a) {
		TopspinPDB p = new TopspinPDB(goal, totalDisks, cost, a);
		//p.printPDB();
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(p);
			out.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}finally {
    		try {
    			if(fos != null) {
        			fos.close();
    			}
			} catch (IOException e) {
				e.printStackTrace();
			}
        	try {
        		if(out != null) {
    				out.close();
        		}
			} catch (IOException e) {
				e.printStackTrace();
			}

        }

	}
	
	public final TopspinAbstraction getAbstraction(){
		return a;
	}
	
	private final TopspinAbstraction a;

	public static TopspinPDB readPDB(String filename)
			throws ClassNotFoundException {
		TopspinPDB p = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			p = (TopspinPDB) in.readObject();
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}finally {
    		try {
    			if(fis != null) {
        			fis.close();
    			}
			} catch (IOException e) {
				e.printStackTrace();
			}
        	try {
        		if(in != null) {
    				in.close();
        		}
			} catch (IOException e) {
				e.printStackTrace();
			}

        }

		return p;
	}
	
	private static boolean costsMatch(Cost c1, Cost c2){
		if(c1 == c2)
			return true;
		if(c1 == Cost.CANONICAL && c2 == Cost.ADDITIVE)
			return true;
		if(c2 == Cost.CANONICAL && c1 == Cost.ADDITIVE)
			return true;
		else
			return false;
	}

	public void checkPDB(TopspinProblem p) {
		if (nDisks != p.getInitial().d.disks.length) {
			System.err.println(nDisks + " " + p.getInitial().d.disks.length
					+ " (disks)");
			throw new InvalidPDB();
		}
		if (turnstileSize != p.getTurnstileSize()) {
			System.err.println(turnstileSize + " " + p.getTurnstileSize()
					+ " (turnstile)");
			throw new InvalidPDB();
		}
		if (!costsMatch(this.cost, p.cost)) {
			System.err.println("Costs do not match");
			System.err.println(cost);
			System.err.println(p.cost);
			throw new InvalidPDB();
		}
	}

	public int getnDisks() {
		return nDisks;
	}

	public int getnRealDisks() {
		return nRealDisks;
	}

	@Override
	protected ArrayList<SearchNodeDepth> makeGoals(SearchNodeDepth canonicalGoal) {
		ArrayList<SearchNodeDepth> toReturn = new ArrayList<SearchNodeDepth>();

		TopspinState s = (TopspinState) canonicalGoal.getState();

//		if (s.problem.cost != Cost.CANONICAL) {
//			for (int i = 0; i < s.d.disks.length; i++) {
//				TopspinState toAdd = s.clone();
//				toAdd.rotate(i);
//				toReturn.add(SearchNodeDepth.makeInitial(toAdd));
//			}
//		}
		TopspinState toAdd = s.clone();
		toReturn.add(SearchNodeDepth.makeInitial(toAdd));
		
		return toReturn;
	}

	public static void main(String[] args) throws ClassNotFoundException {
		if (args.length != 6) {
			System.err.println("destination nDisks turnstile startDisk keptDisks cost");
			for(int i = 0; i < args.length; i++){
				System.err.println("(i) = <" + args[i] + ">");
			}
		} else {
			int nDisks = Integer.parseInt(args[1]);
			int turnstile = Integer.parseInt(args[2]);
			int startDisk = Integer.parseInt(args[3]);
			int keptDisks = Integer.parseInt(args[4]);
			String path = args[0];
			String[] pdbArgs = new String[0];
			
			Cost c;
			if(args[5].equals("canonical")){
				if(startDisk != 0){
					throw new RuntimeException("Can't start a canonical PDB at something not zero");
				}
				c = Cost.CANONICAL;
			}
			else if(args[5].equals("sum"))
				c = Cost.SUM;
			else if(args[5].equals("stripe"))
				c = Cost.STRIPE;
			else if(args[5].equals("additive"))
				c = Cost.ADDITIVE;
			else throw new RuntimeException("invalid model");
			
			
			TopspinProblem g = new TopspinProblem(nDisks, turnstile, c,
					pdbArgs);
			if(startDisk != 0)
				g.setFreeZero();
			writePDB(g, path, keptDisks, c, new TopspinAbstraction(startDisk, startDisk + keptDisks));
		}
	}
}
