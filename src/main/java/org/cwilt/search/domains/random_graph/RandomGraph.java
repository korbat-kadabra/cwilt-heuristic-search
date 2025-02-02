package org.cwilt.search.domains.random_graph;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import org.cwilt.search.algs.basic.bestfirst.AStar;
import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchState;import org.cwilt.search.utils.basic.MinHeap;
public class RandomGraph implements java.io.Serializable, org.cwilt.search.search.SearchProblem{

	public void printProblemData(PrintStream p){
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 5773503617094263175L;
	private final int branchingFactor;
	private final int nNodes;
	private final double minTransition;
	private final double maxTransition;
	private final RandomGraphNode[] graph;
	private final Random r;
	
	public int getNNodes(){
		return nNodes;
	}
	public double getMinTransition(){
		return minTransition;
	}
	public double getMaxTransition(){
		return maxTransition;
	}
	public Random getR(){
		return r;
	}
	
	public RandomGraphNode getNode(int id){
		return graph[id];
	}
	
	public RandomGraph(int size, int seed, int branchingFactor, double minTransition, double maxTransition){
		this.r = new Random(seed);
		this.graph = new RandomGraphNode[size];
		this.branchingFactor = branchingFactor;
		this.nNodes = size;
		this.maxTransition = maxTransition;
		this.minTransition = minTransition;
		
		double costDifference = maxTransition - minTransition;
		assert(costDifference >= 0);
		for(int i = 0; i < nNodes; i++){
			graph[i] = new RandomGraphNode(this, i);
		}
		for(int i = 0; i < nNodes; i++){
			graph[i].initChildren(this.branchingFactor);
		}
		for(int i = nNodes - 1; i > 0; i--){
			graph[i].connectTo(i-1);
		}
		this.setHStar();
		this.setH();
	}
	
	public String toString(){
		return Arrays.deepToString(graph);
	}
	
	private class RandomGraphNodeComparator implements Comparator<RandomGraphNode>{
		@Override
		public int compare(RandomGraphNode arg0, RandomGraphNode arg1) {
			double h0 = arg0.getHStar();
			double h1 = arg1.getHStar();
			if(h0 < h1)
				return -1;
			else if(h0 > h1)
				return 1;
			else
				return 0;
		}
	}
	
	private void setHStar(){
		MinHeap<RandomGraphNode> q = 
			new MinHeap<RandomGraphNode>(new RandomGraphNodeComparator());
		q.insert(graph[0]);
		graph[0].setHStar(0.0);
		
		while(!q.isEmpty()){
			RandomGraphNode next = q.poll();
			for(int i = 0; i < next.getNParents(); i++){
				RandomGraphNode child = next.getParent(i);

				double childTransitionCost = next.getParentCost(i);
				double childG = next.getHStar() + childTransitionCost;
				//new child
				if(child.getHStar() < 0){
					child.setHStar(childG);
					q.insert(child);
					continue;
				}
				//child's priority just got adjusted
				if(child.getHStar() > childG){
					child.setHStar(childG);
					q.bubbleUp(child.getHeapIndex());
					continue;
				}
				//otherwise this child is junk.
			}
		}
	}
	
	private void setH(){
		for(int i = 0; i < nNodes; i++){
			assert(graph[i].getHStar() >= 0);
			graph[i].setH(r.nextDouble() * graph[i].getHStar());
			graph[i].clear();
		}
	}
	
	public RandomGraphNode getInitial(){
		return graph[nNodes - 1];
	}
	
	public static void main(String[] args){
		RandomGraph g = new RandomGraph(250000, 0, 4, 6, 10);

		AStar a = new AStar(g, new Limit());
		a.solve();
		a.printSearchData(System.out);
	}
	@Override
	public SearchState getGoal() {
		throw new org.cwilt.search.search.NoCanonicalGoal();
	}
	
	public ArrayList<SearchState> getGoals(){
		throw new org.cwilt.search.search.NoCanonicalGoal();
	}
	
	
	
	@Override
	public void setCalculateD() {
		assert(false);
	}

}
