package org.cwilt.search.domains.multiagent.solvers.autoqueue;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.cwilt.search.domains.kiva.problem.KivaProblem;
public class MapSerializer {
	public static void main(String[] args){
		final int nAgents = 200;
		KivaProblem p = new KivaProblem(new org.cwilt.search.domains.kiva.map.Grid("movingai/den011d.map"),
				nAgents, 0, true, KivaProblem.TASK_TYPE.GAME);
//		multiagent.solvers.MultiagentSolver s = new multiagent.solvers.GlobalGhostSolver(p);
//		p.setSolver(s);
		p.map.buildGateways(p.map.findCorridors(), p.res);
		p.rebuildTaskRoutings(false);
		

	    try{
	      //use buffering
	      OutputStream file = new FileOutputStream( "/home/cmw/game_map.serial" );
	      OutputStream buffer = new BufferedOutputStream( file );
	      ObjectOutput output = new ObjectOutputStream( buffer );
	      try{
	        output.writeObject(p.map);
	      }
	      finally{
	        output.close();
	      }
	    }  
	    catch(IOException ex){
	    	System.err.println(ex.getMessage());
	    }

	}
}
