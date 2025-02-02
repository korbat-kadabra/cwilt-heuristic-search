package org.cwilt.search.domains.pancake;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;

import org.cwilt.search.search.SearchState;
public class PancakeProblem implements org.cwilt.search.search.SearchProblem{
	public static enum HEURISTIC {
		PDB, GAP
	}
	public static enum COST {
		UNIT, SUM
	}
	
	public double priceSwap(short[] cakes, int index){
		if(this.c == COST.UNIT)
			return 1.0;
		else if(this.c == COST.SUM){
			double price = 0;
			for(int i = cakes.length - 1; i >= cakes.length-1-index; i--){
				if(cakes[i] != PancakeState.ABSTRACTED){
					price += cakes[i];
				}
			}
			return price;
		} else {
			throw new RuntimeException("Invalid Pancake Cost");
		}
		
	}
	
	private final PancakeState initial;
	private final HEURISTIC h;
	private final COST c;
	
	public COST getCost(){
		return c;
	}
	
//	public PancakeProblem(String path) throws FileNotFoundException{
//		this.initial = new PancakeState(path, this);
//		this.h = HEURISTIC.GAP;
//		this.c = COST.UNIT;
//		this.pdb = null;
//	}
	private final PancakePDB pdb;

	public PancakeProblem(String path, String[] probArgs, String cost) throws IOException, ClassNotFoundException{
		this.initial = new PancakeState(path, this);
		
		
		if(cost == null || cost.equals("unit"))
			this.c = COST.UNIT;
		else if(cost.equals("sum")){
			this.c = COST.SUM;
		} else {
			throw new RuntimeException("Invalid cost selction");
		}
		if(probArgs == null){
			this.h = HEURISTIC.GAP;
			this.pdb = null;
		} else if (probArgs.length == 1){
			this.h = HEURISTIC.PDB;
			this.pdb = PancakePDB.readPDB(probArgs[0]);
		} else 
			throw new RuntimeException("Too many problem arguments");

	}

	public PancakeState getInitial() {
		return initial;
	}
	
	public PancakeProblem(int seed, int nCakes){
		Random r = new Random(seed);
		this.initial = new PancakeState(r, this, nCakes);
		this.h = HEURISTIC.GAP;
		this.c = COST.UNIT;
		this.pdb = null;
	}
	
	public PancakeProblem(int nCakes, COST c){
		this.initial = new PancakeState(nCakes, this);
		this.h = HEURISTIC.GAP;
		this.c = c;
		this.pdb = null;
	}

	public double calculateH(PancakeState s){
		switch(h){
		case GAP: 
			return s.gap();
		case PDB:
			return pdb.getH(s.abstractState(pdb.getAbstraction()).getKey());
		}
		return 0;
	}

	public int calculateD(PancakeState s){
		switch(h){
		case GAP: 
			return (int) s.gap();
		case PDB:
			return pdb.getD(s.abstractState(pdb.getAbstraction()).getKey());
		}
		return 0;
	}

	
	@Override
	public SearchState getGoal() {
		return new PancakeState(initial.getNCakes(), this);
	}
	@Override
	public ArrayList<SearchState> getGoals() {
		ArrayList<SearchState> toReturn = new ArrayList<SearchState>();
		toReturn.add(getGoal());
		return toReturn;
	}

	@Override
	public void setCalculateD() {}
	
	public void printProblemData(PrintStream p){
		
	}

}
