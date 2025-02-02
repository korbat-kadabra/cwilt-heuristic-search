/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.search;
import java.util.ArrayList;

public class Solution {
	private final SearchNode goal;
	private final int length;
	private final long exp, gen, dup;
	private final long findTime;
	private final double cost;

	public SearchNode getGoal() {
		return goal;
	}

	public double getCost() {
		return cost;
	}
	

	public long getFindTime() {
		return findTime;
	}
	
	public ArrayList<SearchState> reconstructPath(){
		return goal.reconstructPath();
	}
	
	public int getLength() {
		return length;
	}
	
	public Solution(SearchNode g, double cost, long findTime, int length,
			long exp, long gen, long dup) {
		this.goal = g;
		this.cost = cost;
		this.findTime = findTime;
		this.length = length;
		this.exp = exp;
		this.gen = gen;
		this.dup = dup;
	}

	public String toString() {
		double ft = ((double) findTime) / (1000.0);
		return cost + "\t" + length + "\t" + exp + "\t" + gen + "\t" + dup
				+ "\t" + (ft);
	}
	public long getExp(){
		return exp;
	}
}
