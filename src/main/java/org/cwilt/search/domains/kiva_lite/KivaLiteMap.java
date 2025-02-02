package org.cwilt.search.domains.kiva_lite;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;import org.cwilt.search.search.SearchState.Child;
public class KivaLiteMap implements SearchProblem{
	private final LiteFiducial[][] map;
	private final int seed;
	private final Random r;
	private final double scale, pBlocked, pExpensive;
	private double getCost(){
		return 1.0d + (r.nextDouble() * scale);
	}

	public enum Heuristic {
		MANHATTAN, BACKWARDS, ZERO
	}
	
	private final Heuristic h;
	
	public KivaLiteMap.Heuristic getHType(){
		return h;
	}
	
	private void blockCells(){
		for(int i = 0; i < map.length; i++){
			for(int j = 0; j < map[0].length; j++){
				if(r.nextDouble() < pBlocked){
					map[i][j].block();
				}
			}
		}
	}
	
	public void printProblemData(PrintStream p){
		
	}

	private void expensiveCells(){
		for(int i = 0; i < map.length; i++){
			for(int j = 0; j < map[0].length; j++){
				if(r.nextDouble() < pExpensive){
					map[i][j].increaseCost(10 + r.nextDouble());
				}
			}
		}
	}
	
	public LiteFiducial getFiducial(int x, int y){
		return map[x][y];
	}
	
	private void calculateH(){
		for(int i = 0; i < map.length; i++){
			for(int j = 0; j <map[0].length; j++){
				this.map[i][j].setH(Double.MAX_VALUE);
			}
		}
		TreeSet<LiteFiducial> open = new TreeSet<LiteFiducial>();
		open.add(map[0][0]);
		map[0][0].setH(0);
		while(!open.isEmpty()){
			LiteFiducial next = open.pollFirst();
			ArrayList<Child> children = next.expand();
			for(Child c : children){
				LiteFiducial child = (LiteFiducial) c.child;
				double childG = next.h() + c.transitionCost;
				if(child.h() > childG){
					open.remove(child);
					child.setH(childG);
					open.add(child);
				}
			}
		}
	}
	

	
	public KivaLiteMap(int xSize, int ySize, int seed, double s, KivaLiteMap.Heuristic h, double pBlocked, double pExpensive){
		assert(s > 1);
		this.pBlocked = pBlocked;
		this.pExpensive = pExpensive;
		this.h = h;
		this.scale = s - 1;
		this.map = new LiteFiducial[xSize][ySize];
		this.seed = seed;
		this.r = new Random(this.seed);
		for(int i = 0; i < xSize; i++){
			for(int j = 0; j < ySize; j++){
				this.map[i][j] = new LiteFiducial(i,j, this);
			}
		}
		for(int i = 0; i < ySize; i++){
			for(int j = 0; j < xSize; j++){
				LiteFiducial here = map[i][j];
				if(j > 0){
					double linkCost = getCost();
					here.connectTo(LiteFiducial.LEFT, map[i][j-1], linkCost);
					map[i][j-1].connectTo(LiteFiducial.RIGHT, here, linkCost);
				}
				if(j < ySize - 1){
					double linkCost = getCost();
					here.connectTo(LiteFiducial.RIGHT, map[i][j+1], linkCost);
					map[i][j+1].connectTo(LiteFiducial.LEFT, here, linkCost);
				}
				if(i > 0){
					double linkCost = getCost();
					here.connectTo(LiteFiducial.UP, map[i-1][j], linkCost);
					map[i-1][j].connectTo(LiteFiducial.DOWN, here, linkCost);
				}
				if(i < xSize - 1){
					double linkCost = getCost();
					here.connectTo(LiteFiducial.DOWN, map[i+1][j], linkCost);
					map[i+1][j].connectTo(LiteFiducial.UP, here, linkCost);
				}
			}
		}

		calculateH();
		blockCells();
		expensiveCells();
	}
	
	@Override
	public SearchState getInitial() {
		return map[map.length - 1][map[0].length - 1];
	}
	@Override
	public SearchState getGoal() {
		return map[0][0];
	}
	@Override
	public ArrayList<SearchState> getGoals() {
		ArrayList<SearchState> goals = new ArrayList<SearchState>(1);
		goals.add(getGoal());
		return goals;
	}
	@Override
	public void setCalculateD() {
		return;
	}
	
	public static void main(String[] args){
		final int ct = 100;
		final double[] exp = new double[ct];
		final double[] cost = new double[ct];

		int seed = 0;
		for(int i = 0; i < 100; ){
			KivaLiteMap km = new KivaLiteMap(100, 100, seed, 10, KivaLiteMap.Heuristic.BACKWARDS, .10, .10);
			SearchAlgorithm a = new org.cwilt.search.algs.basic.bestfirst.WAStar(km, new Limit(), 2);
			a.solve();
			if(a.getIncumbent() != null){
				cost[i] = a.getIncumbent().getCost();
				exp[i] = a.getIncumbent().getExp();
				i++;
			}
			seed ++;
		}
		System.err.println("cost: " + org.cwilt.search.utils.basic.Stats.mean(cost));
		System.err.println("exp: " + org.cwilt.search.utils.basic.Stats.mean(exp));
	}
}
