package org.cwilt.search.domains.greedy_mdp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.cwilt.search.algs.utils.ReverseEnum;import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;
public class GreedyMDP {
	private final ArrayList<ArrayList<GreedyMDPNode>> mdp;
	
	final Random r;
	
	public GreedyMDP(int diameter, SearchProblem p, int seed) {
		this.r = new Random(seed);
		this.mdp = new ArrayList<ArrayList<GreedyMDPNode>>(diameter);
		for(int h = 0; h < diameter; h++){
			this.mdp.add(new ArrayList<GreedyMDPNode>(diameter - h + 1));
			for(int hStar = 0; hStar <= h; hStar++){
				this.mdp.get(h).add(new GreedyMDPNode(hStar, h, this));
			}
		}
		ReverseEnum e = new ReverseEnum(p, new Limit());
		e.solve();
		HashMap<Object, SearchNode> closed = e.getClosed();
		for(Map.Entry<Object, SearchNode> pair : closed.entrySet()){
			SearchNode next = pair.getValue();
			int nextH = (int) next.getH();
			int nextHStar = (int) next.getG();
			getNode(nextH, nextHStar).registerNode(next, closed);
		}

		for(int hStar = 0; hStar < diameter; hStar++){
			this.mdp.add(new ArrayList<GreedyMDPNode>(diameter - hStar + 1));
			for(int h = 0; h <= hStar; h++){
				getNode(h, hStar).prepareNeighbors();
			}
		}
		
		
	}
	
	public GreedyMDPNode getNode(int h, int hStar){
		GreedyMDPNode toReturn = this.mdp.get(hStar).get(h);
		assert(toReturn.h == h);
		assert(toReturn.hStar == hStar);
		return toReturn;
	}
	
	
	public String toString(){
		StringBuffer b = new StringBuffer();
		for(ArrayList<GreedyMDPNode> h : mdp){
			for(GreedyMDPNode n : h){
				b.append(n);
				b.append(" ");
			}
			b.append("\n");
		}
		return b.toString();
	}
}
