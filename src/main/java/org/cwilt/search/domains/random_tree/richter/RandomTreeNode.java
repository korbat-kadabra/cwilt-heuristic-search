package org.cwilt.search.domains.random_tree.richter;
import java.util.ArrayList;

import org.cwilt.search.search.SearchState;
public class RandomTreeNode extends org.cwilt.search.search.SearchState {
	
	public String toString(){
		StringBuffer b = new StringBuffer();
		
		b.append("agd = ");
		b.append(this.approxGoalDistance);
		b.append(" h = ");
		b.append(h);
		
		return b.toString();
	}
	
	private RandomTreeNode(RandomTreeNode parent) {
		this.problem = parent.problem;
		double cost = problem.r.nextDouble()
				* (problem.maxEdgeCost - problem.minEdgeCost)
				+ problem.minEdgeCost;
		if(problem.r.nextBoolean()){
			cost *= -1;
		}
		
		this.approxGoalDistance = parent.approxGoalDistance + cost;
		
		double parentInducedHeuristic = this.approxGoalDistance * parent.errorFactor();
		double randomAccuracy = problem.hErrorFactor + (1 - problem.hErrorFactor) * problem.r.nextDouble();
		
		assert(randomAccuracy >= problem.hErrorFactor);
		assert(randomAccuracy <= 1.0);
		
		double randomHeuristic = this.approxGoalDistance * randomAccuracy;
		double direction;
		if(randomHeuristic - parentInducedHeuristic > 0){
			direction = 1;
		} else {
			direction = -1;
		}
		double m = Math.max(1, Math.abs(parentInducedHeuristic - randomHeuristic));
		this.h = parentInducedHeuristic + m * direction;
	}
	
	private double errorFactor(){
		return this.h / this.approxGoalDistance;
	}

	@Override
	public ArrayList<Child> expand() {
		ArrayList<Child> children = new ArrayList<Child>(problem.branchingFactor);
		for(int i = 0; i < problem.branchingFactor; i++){
			RandomTreeNode nextChild = new RandomTreeNode(this);
			double nextDeltaG = Math.abs(this.approxGoalDistance - nextChild.approxGoalDistance);
			//if the g value would go too large
			if(this.approxGoalDistance - nextChild.approxGoalDistance < 0){
				nextDeltaG = this.approxGoalDistance;
			}
			children.add(new Child(nextChild, nextDeltaG));
		}
		return children;
	}

	@Override
	public ArrayList<Child> reverseExpand() {
		throw new org.cwilt.search.search.NoCanonicalGoal();
	}

	public RandomTreeNode(RandomTreeProblem p) {
		this.problem = p;
		this.approxGoalDistance = p.rootAGD;
		this.h = p.rootAGD * p.hErrorFactor;
	}

	private final RandomTreeProblem problem;
	private final double approxGoalDistance;
	private final double h;
	
	@Override
	public double h() {
		return h;
	}

	@Override
	public int d() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isGoal() {
		return this.approxGoalDistance <= 0;
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
