package org.cwilt.search.domains.topspin;
import java.util.BitSet;

import org.cwilt.search.domains.topspin.TopspinProblem.Cost;
public class CustomTopspinPDB {//extends TopspinPDB {

//
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 7947054672425087574L;
//
//	protected CustomTopspinPDB(TopspinProblem p, int nDisks, Cost c,
//			CustomTopspinAbstraction a) {
//		super(p, nDisks, c, a);
//	}
	public static void main(String[] args) throws ClassNotFoundException {
		if (args.length < 4) {
			System.err.println("destination nDisks turnstile cost kept");
			for(int i = 0; i < args.length; i++){
				System.err.println("(i) = <" + args[i] + ">");
			}
		} else {
			int nDisks = Integer.parseInt(args[1]);
			int turnstile = Integer.parseInt(args[2]);
			String path = args[0];
			String[] pdbArgs = new String[0];
			
			Cost c;
			if(args[3].equals("canonical")){
				c = Cost.CANONICAL;
			}
			else if(args[3].equals("sum"))
				c = Cost.SUM;
			else if(args[3].equals("stripe"))
				c = Cost.STRIPE;
			else if(args[3].equals("additive"))
				c = Cost.ADDITIVE;
			else throw new RuntimeException("invalid model");
			
			
			TopspinProblem g = new TopspinProblem(nDisks, turnstile, c,
					pdbArgs);
			
			BitSet abstracted = new BitSet(nDisks);
			//set all of the bits to be true, indicating that everything is abstracted
			abstracted.set(0, nDisks);
			
//			for(int i = 0; i < args.length; i++){
//				System.err.println(i + " -> " + args[i]);
//			}
			
			for(int i = 4; i < args.length; i++){
				int nextAbstracted = Integer.parseInt(args[i]);
				if(nextAbstracted > nDisks)
					throw new IllegalArgumentException("Can't abstract a disk that doesn't exist");
				abstracted.clear(nextAbstracted);
			}
			if(abstracted.get(0))
				g.setFreeZero();
			
			TopspinPDB.writePDB(g, path, nDisks, c, new CustomTopspinAbstraction(abstracted));
		}
	}

}
