package org.cwilt.search.domains.hanoi.pdb_builder;
import org.junit.Test;

public class HanoiPDBMapTest {
	@Test
	public void t(){
		HanoiPDBMap h = new HanoiPDBMap(new int[]{2,3,4,5,6,7,8,9}, 12);
		assert(h.generateNeighbors().size() == 4*8);
	}
}
