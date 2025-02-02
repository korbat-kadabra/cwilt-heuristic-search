package org.cwilt.search.domains.hanoi;
import java.util.ArrayList;

import org.junit.Test;

import org.cwilt.search.search.SearchState.Child;
public class HanoiTest {

	@Test
	public void hanoiInversesTest() throws ClassNotFoundException{
		String[] pdbs = new String[0];
		double[] costs = new double[4];
		for(int i = 0; i < costs.length; i++){
			costs[i] = 1;
		}
		
		HanoiProblem h = new HanoiProblem(4, 4, costs, pdbs);
		for(int i = 0; i < 16; i++){
			System.err.printf("%d -> %d\n", i, h.getInverse(i));
		}
		
		HanoiState s = h.getInitial();
		ArrayList<Child> children = s.expand();
		int i = 0;
		for(Child c : children){
			System.err.println(i);
			i++;
			System.err.println(c.child.getKey());
		}
		
		for(i = 0; i < 16; i++){
			double ret = s.convertToChild(i, -1);
			if(ret < 0)
				continue;
			System.err.println(i);
			System.err.println(s.getKey());
			s.convertToChild(s.inverseChild(i), -1);
		}
	}
}
