package org.cwilt.search.domains.topspin;
import org.junit.Test;

public class TopspinPDBTest {
	
	@Test
	public void simpleTest() throws ClassNotFoundException{
		TopspinPDB pdb3 = TopspinPDB.readPDB("/home/aifs2/group/data/topspin/pdb/canonical/12/3/0/2/java_canonical");
		TopspinPDB pdb4 = TopspinPDB.readPDB("/home/aifs2/group/data/topspin/pdb/canonical/12/4/0/2/java_canonical");
		
		pdb4.comparePDB(pdb3);
		pdb3.comparePDB(pdb4);
		
		System.err.println("done");
	}
	
//	@Test
//	public void stateCountTest() throws ClassNotFoundException {
//		TopspinProblem p = new TopspinProblem(10, 3, TopspinProblem.Cost.CANONICAL, new String[0]);
//	}
	
}
