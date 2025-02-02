package org.cwilt.search.domains.greedy_mdp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.cwilt.search.search.SearchNode;
public class GreedyMDPNode {
	public final int h;
	public final int hStar;
	private final GreedyMDP mdp;
	private double[] probabilities;
	private GreedyMDPNode[] neighbors;
	
	public GreedyMDPNode(int h, int hStar, GreedyMDP mdp){
		this.h = h;
		this.hStar = hStar;
		this.mdp = mdp;
		this.targets = new HashMap<GreedyMDPNode, AtomicInteger>();
	}
	
	public void prepareNeighbors(){
		probabilities = new double[targets.size()];
		neighbors = new GreedyMDPNode[targets.size()];
		
		int i = 0;
		for(Map.Entry<GreedyMDPNode, AtomicInteger> e : targets.entrySet()){
			neighbors[i] = e.getKey();
			probabilities[i] = e.getValue().doubleValue() / targets.size();
			i++;
		}
	}
	
	public GreedyMDPNode nextNode(){
		double value = mdp.r.nextDouble();
		double accum = 0;
		for(int i = 0; i < probabilities.length; i++){
			accum += probabilities[i];
			if(accum > value)
				return neighbors[i];
		}
		throw new RuntimeException("didn't select a value for some reason");
	}
	
	public String verboseToString(){
		StringBuffer b = new StringBuffer();
		b.append(this);
		b.append("\n");
		for(Map.Entry<GreedyMDPNode, AtomicInteger> e : targets.entrySet()){
			b.append("\t");
			b.append(e.getKey());
			b.append(" -> ");
			b.append(e.getValue());
			b.append("\n");
		}
		
		return b.toString();
	}

	public boolean isGoal(){
		return this.hStar == 0;
	}
	private final HashMap<GreedyMDPNode, AtomicInteger> targets;
	
	public void registerNode(SearchNode n, HashMap<Object, SearchNode> closed){
		assert(n.getG() == hStar);
		assert(n.getH() == h);
		for(SearchNode child : n.expand()){
			SearchNode incumbent = closed.get(child.getState().getKey());
			assert(incumbent != null);
			int incumbentH = (int) incumbent.getH();
			int incumbentHStar = (int) incumbent.getG();
			GreedyMDPNode target = mdp.getNode(incumbentH, incumbentHStar);
			AtomicInteger amount = targets.get(target);
			if(amount == null){
				amount = new AtomicInteger(0);
				targets.put(target, amount);
			}
			amount.incrementAndGet();
		}
	}
	
	public String toString(){
		return String.format("h %2d h* %2d", h, hStar);
	}
	
}
