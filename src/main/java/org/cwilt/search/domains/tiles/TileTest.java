package org.cwilt.search.domains.tiles;
import org.junit.Test;

public class TileTest {
	@Test
	public void inPlaceTest(){
		TileProblem p = new TileProblem(3, 3, "unit");
		TileState s = (TileState) p.getInitial();
		System.err.println(s);
		for(int i = 0; i < 4; i++){
			double childCost =  s.convertToChild(i, -1);
			System.err.println("this child (" + i + ") cost " + childCost);
			if(childCost < 0)
				continue;
			System.err.println(s);
			int inverse = s.inverseChild(i);
			s.convertToChild(inverse, -1);
			
		}
	}
	
}
