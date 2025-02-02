package org.cwilt.search.domains.vacuum;
import java.io.IOException;
import java.text.ParseException;

import org.junit.Test;

import org.cwilt.search.search.Limit;
import org.cwilt.search.algs.basic.bestfirst.AStar;
public class VacuumTest {
	@Test
	public void loadTest() throws IOException, ParseException{
		VacuumProblem vp = new VacuumProblem("/home/aifs2/group/data/vacuum_instances/uniform/500/500/0.35/10/1");
		AStar a = new AStar(vp, new Limit());
		a.solve();
		a.printSearchData(System.out);
	}
	
}
