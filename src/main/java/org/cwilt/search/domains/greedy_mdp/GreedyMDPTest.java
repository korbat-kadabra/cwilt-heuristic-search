package org.cwilt.search.domains.greedy_mdp;
import org.cwilt.search.domains.tiles.TileProblem;
import org.junit.Test;
public class GreedyMDPTest {
	private static final int SEED = 100;
	
	@Test
	public void test(){
		GreedyMDP m = new GreedyMDP(25, new TileProblem(2, 3, "unit"), SEED);
		GreedyMDPNode start = m.getNode(7, 7);

		int count = 0;
		while(!start.isGoal()){
			count ++;
			start = start.nextNode();
			System.err.println(count);
			System.err.println(start.verboseToString());
		}
		System.err.println(count);
	}
	
}
