package org.cwilt.search.domains.random_tree.wilt;
import java.util.ArrayList;

import org.cwilt.search.search.SearchState;
public class RandomTreeNode extends org.cwilt.search.search.SearchState{
	
	private final double h, hStar;
	private final RandomTreeProblem problem;
	
	public RandomTreeNode(double h, double hStar, RandomTreeProblem problem){
		this.hStar = hStar;
		this.h = h;
		assert(this.h <= this.hStar);
		this.problem = problem;
	}
	
	public RandomTreeNode(RandomTreeNode parent, double opCost){
		this.problem = parent.problem;
		double nextHStar;
		double sign;
		
		if(problem.r.nextBoolean()){
			nextHStar = parent.hStar + opCost;
			sign = 1;
		}
		else {
			nextHStar = parent.hStar - opCost;
			sign = -1;
		}
			
		if(nextHStar < 0)
			nextHStar = 0;
		this.hStar = nextHStar;
		
		boolean increaseH;
		
		if(sign > 0){
			//in reality, got further from the goal
			if(problem.r.nextDouble() < problem.pCorrect){
				increaseH = true;
			} else {
				increaseH = false;
			}
		} else {
			//in reality got closer to the goal
			if(problem.r.nextDouble() < problem.pCorrect){
				increaseH = false;
			} else {
				increaseH = true;
			}
		}
		
		
		if(increaseH){
			this.h = Math.min(parent.h + opCost, this.hStar);
		} else {
			this.h = Math.max(parent.h - opCost, 0d);
		}
		assert(this.h >= 0);
		assert(this.h <= this.hStar);
	}
	
	@Override
	public ArrayList<Child> expand() {
		//System.err.println(this.h + " -> " + this.hStar);
		ArrayList<Child> children = new ArrayList<Child>(problem.branchingFactor);
		
		for(int i = 0; i < problem.branchingFactor; i++){
			double opCost = problem.nextOperatorCost();
			children.add(new Child(new RandomTreeNode(this, opCost), opCost));
		}
		
		return children;
	}

	@Override
	public ArrayList<Child> reverseExpand() {
		throw new org.cwilt.search.search.NoCanonicalGoal();
	}

	@Override
	public double h() {
		return h;
	}

	@Override
	public int d() {
		return 0;
	}

	@Override
	public boolean isGoal() {
		return this.hStar == 0;
	}

	@Override
	public Object getKey() {
		return this;
	}

	@Override
	public int hashCode() {
		return System.identityHashCode(this);
	}

	@Override
	public boolean equals(Object other) {
		return this == other;
	}

	@Override
	public int lexOrder(SearchState s) {
		return this.hashCode() - s.hashCode();
	}
	
}
