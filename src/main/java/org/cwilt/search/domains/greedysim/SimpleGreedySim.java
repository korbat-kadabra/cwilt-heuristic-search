package org.cwilt.search.domains.greedysim;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import java.util.List;

public class SimpleGreedySim {
	private final double maxCost;
	private final double minCost;
	private final double highwater;
	private final double initialH;
	private final Random r;
	public SimpleGreedySim(int seed, double min, double max, double highwater, double initialH){
		this.r = new Random(seed);
		this.minCost = min;
		this.maxCost = max;
		this.highwater = highwater;
		this.initialH = initialH;
	}
	
	private final double getCost(){
		return minCost + r.nextDouble() * (maxCost - minCost);
	}
	
	private class SGSNode implements Comparable<SGSNode>{
		public final double h;
		
		private SGSNode(SGSNode parent){
			double newH = parent.h + SimpleGreedySim.this.getCost();
			if(newH < 0)
				newH = 0;
			this.h = newH;
		}
		public SGSNode(double h){
			this.h = h;
		}
		@Override
		public int compareTo(SGSNode arg0) {
			if(this.h < arg0.h)
				return -1;
			else if(this.h > arg0.h)
				return 1;
			else
				return 0;
		}
		
		@Override
		public String toString() {
			return "SGSNode [h=" + h + "]";
		}

		public List<SGSNode> expand(){
			ArrayList<SGSNode> c = new ArrayList<SGSNode>(NCHILDREN);
			for(int i = 0; i < NCHILDREN; i++){
				SGSNode next = new SGSNode(this);
				if(next.h <= highwater)
					c.add(new SGSNode(this));
			}
			return c;
		}
	}
	int expanded;
	
	private static final int NCHILDREN = 2;
	public void run(){
		SGSNode root = new SGSNode(initialH);
		Queue<SGSNode> q = new LinkedList<SGSNode>();
		q.add(root);
		while(!q.isEmpty()){
			expanded ++;
			SGSNode next = q.poll();
			q.addAll(next.expand());
		}
	}
	
	public static void main(String[] args){
		SimpleGreedySim s = new SimpleGreedySim(0, 1.1, 2, 160, 150);
		s.run();
		System.out.println(s.expanded);
	}
	
}
