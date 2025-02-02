package org.cwilt.search.misc.analysis;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import org.cwilt.search.algs.basic.BreadthFirst;
import org.cwilt.search.algs.basic.bestfirst.AStar;
import org.cwilt.search.domains.custom_grid.CustomGrid;
import org.cwilt.search.domains.grid.GridProblem;
import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchState;
import org.cwilt.search.utils.basic.Stats;
public class HStar {
	
	public static void gridTest(String filepath) throws IOException, ParseException{
		int max = 100;

		ArrayList<Double> h = new ArrayList<Double>();
		ArrayList<Double> hStar = new ArrayList<Double>();
		
		GridProblem gp = new GridProblem(filepath, true, true, null);
		int done = 0;
		for(int i = 0; done < max; i++){
			GridProblem gpp = gp.cloneNewInitial(i * 100);
			AStar a = new AStar(gpp, new Limit());
			ArrayList<SearchState> path = a.solve();
			if(path == null)
				continue;

			h.add(gpp.getInitial().h());
			hStar.add((double)(path.size() - 1));
			
			//System.out.printf("%f\t%d\n", gpp.getInitial().h(), path.size() - 1);
			done ++;
		}
		
		System.out.printf("let h = [|");
		for(Double d : h){
			System.out.printf("%f;", d);
		}
		System.out.printf("|];;\n");
		
		System.out.printf("let hStar = [|");
		for(Double d : hStar){
			System.out.printf("%f;", d);
		}
		System.out.printf("|];;\n");

	}
	
	public static void customGridTest(String filepath, double hd, double hh, double herr) throws IOException, ParseException{
		CustomGrid cgp = new CustomGrid(1, filepath, hd, hh, herr);
		int done = 0;
		
		ArrayList<Double> hStar = new ArrayList<Double>();
		ArrayList<Double> h = new ArrayList<Double>();
		ArrayList<Double> dStar = new ArrayList<Double>();

		System.err.println("done creating problem");

		long startTime = System.currentTimeMillis();
		
		for(int i = 0; done < 100; i++){
			cgp.changeInitial(i);
			AStar a = new AStar(cgp, new Limit());
			ArrayList<SearchState> path = a.solve();
			if(path == null)
				continue;
			hStar.add(a.getFinalCost());
			h.add(cgp.getInitial().h());
			BreadthFirst b = new BreadthFirst(cgp, new Limit());
			path = b.solve();
			Integer l = path.size();
			dStar.add(dStar.size(), l.doubleValue());
			done ++;
		}
		System.err.printf("hh: %f hd: %f\n", Stats.pearson(hStar, h), Stats.pearson(h, dStar));
		long runTime = System.currentTimeMillis() - startTime;
		System.err.println(runTime / 1000);
	}
	
	public static void main(String[] args) throws IOException, ParseException{
		String filepath = "/home/aifs2/group/data/grid_instances/uniform/instance/Unit/Four-way/0.35/2000/1200/1";
		gridTest(filepath);
	}
}
