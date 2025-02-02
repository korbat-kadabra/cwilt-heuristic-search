/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.search;
import org.cwilt.search.domains.citynav.Citynav;
import org.cwilt.search.domains.grid.GridProblem;
import org.cwilt.search.domains.grid.LifeCostGrid;
import org.cwilt.search.domains.grid.RandomCostGrid;
import org.cwilt.search.domains.hanoi.HanoiProblem;
import org.cwilt.search.domains.pancake.PancakeProblem;
import org.cwilt.search.domains.robot.RobotProblem;
import org.cwilt.search.domains.tiles.TileProblem;
import org.cwilt.search.domains.topspin.TopspinProblem;
import org.cwilt.search.domains.vacuum.VacuumProblem;

import java.io.IOException;
import java.text.ParseException;

import org.cwilt.search.utils.basic.ArgParser;import org.cwilt.search.utils.basic.ArgParser.InvalidCommandLineInput;
/**
 * 
 * Main class for launching searches.
 * 
 * Important VM options
 * 
 * -Xmx2000m increases max memory
 * 
 * -verbosegc makes the garbage collector print out what the hell it is doing
 * -Xloggc:gcdata makes the garbage collection log go to a file
 * 
 * -XX:+UseConcMarkSweepGC says to use a particular kind of garbage collector,
 * rather important since in a search algorithm most of the time the garbage is
 * only on the minor heap.
 * 
 * option for profiling cpu:
 * -Xrunhprof:cpu=samples 
 * 
 * -ea -XX:+UseConcMarkSweepGC -Xmx2000m -Xms2000m
 * -Xmx7500m -Xms7500m
 * 
 * @author cmo66
 * 
 */

public class SearchMain {
	public static final int ALG = 0;
	public static final int TIME = 1;
	public static final int PROBLEM = 2;
	public static final int TYPE = 3;
	public static final int REST = 4;
	public static final int EXP = 5;
	public static final int GEN = 6;
	public static final int THREADS = 7;
	public static final int PROBARGS = 8;
	public static final int COST = 9;
	public static final int MEMORY = 10;

	public static final ArgParser.ArgTypes commandArgs[] = {
			new ArgParser.ArgTypes("--alg", ArgParser.ParamType.STRING),
			new ArgParser.ArgTypes("--time", ArgParser.ParamType.FLOAT),
			new ArgParser.ArgTypes("--problem", ArgParser.ParamType.STRING),
			new ArgParser.ArgTypes("--type", ArgParser.ParamType.STRING),
			new ArgParser.ArgTypes("--rest", ArgParser.ParamType.STRINGARR),
			new ArgParser.ArgTypes("--exp", ArgParser.ParamType.INTEGER),
			new ArgParser.ArgTypes("--gen", ArgParser.ParamType.INTEGER),
			new ArgParser.ArgTypes("--threads", ArgParser.ParamType.INTEGER),
			new ArgParser.ArgTypes("--probargs", ArgParser.ParamType.STRINGARR),
			new ArgParser.ArgTypes("--cost", ArgParser.ParamType.STRING), 
			new ArgParser.ArgTypes("--nomemcheck", ArgParser.ParamType.FLAG),
	};

	public static org.cwilt.search.search.Limit makeLimit(Object[] s) {
		long exp = Long.MAX_VALUE;
		long gen = Long.MAX_VALUE;
		long time = Long.MAX_VALUE;
		boolean memcheck = true;
		
		if (s[EXP] != null)
			exp = (Long) s[EXP];
		if (s[GEN] != null)
			gen = (Long) s[GEN];
		if (s[TIME] != null)
			time = (long) ((Double) s[TIME] * 1000);
		if(s[MEMORY] != null)
			memcheck = false;
		
		return new org.cwilt.search.search.Limit(time, exp, gen, memcheck);
	}
	
	public static void printArgs(String[] args){
		for(int i = 0; i < args.length; i++){
			System.err.println(i + ": [" + args[i] + "]");
		}
	}

	public static void main(String[] args) throws InvalidCommandLineInput,
			IOException, ClassNotFoundException, ParseException {

		Object[] s = null;
		if (args.length != 0) {
			s = ArgParser.parseArgs(commandArgs, args);
			if (s[ALG] == null) {
				System.err
						.println("Must select an algorithm (-alg arg not specified");
				printArgs(args);
				System.exit(1);
			}
			if (s[PROBLEM] == null) {
				System.err
						.println("Must select a problem (-problem arg not specified");
				printArgs(args);
				System.exit(1);
			}
			if (s[TYPE] == null) {
				System.err
						.println("Must select a type (-type arg not specified");
				printArgs(args);
				System.exit(1);
			}
		}
		if (s == null) {
			System.err.println("Error initializing arguments");
			printArgs(args);
			System.exit(1);
		}
		String alg = (String) s[ALG];
		String[] rest = (String[]) s[REST];
		String[] probargs = (String[]) s[PROBARGS];
		String problemPath = (String) s[PROBLEM];
		String problemType = (String) s[TYPE];
		String cost = (String) s[COST];

		long threads = 1;
		if (s[THREADS] != null)
			threads = (Long) s[THREADS];

		org.cwilt.search.search.Limit l = makeLimit(s);

		SearchProblem initial = loadInitial(problemType, problemPath, probargs,
				cost);
		SearchAlgorithm algRun = AlgPicker.pickAlg(alg, rest, initial, l);

		boolean restart = false;
		if (algRun instanceof org.cwilt.search.algs.experimental.StochasticBeam)
			restart = true;
		if (algRun instanceof org.cwilt.search.algs.experimental.RandomizedBeam)
			restart = true;
		if (algRun instanceof org.cwilt.search.algs.experimental.RandomProbe)
			restart = true;

		SearchAlgorithm m = null;
		if(threads != 1){
			m = new SearchMaster(threads, algRun, restart, initial);
		}
		else{
			m = algRun;
		}
		m.solve();

		m.printSearchData(System.out);
	}

	private static SearchProblem loadInitial(String problemType,
			String problemPath, String[] typeArgs, String cost)
			throws IOException, ClassNotFoundException, ParseException {
		SearchProblem s = null;
		boolean loaded = false;
		if (problemType.equals("tiles")) {
			loaded = true;
			TileProblem tp = new TileProblem(problemPath, cost, typeArgs);
			s = tp;
		}
		if (problemType.equals("grid")) {
			loaded = true;
			GridProblem gp;
			if(cost != null && cost.equals("random") && typeArgs.length == 3){
				int seed = Integer.parseInt(typeArgs[0]);
				double min = Double.parseDouble(typeArgs[1]);
				double max = Double.parseDouble(typeArgs[2]);
				gp = new RandomCostGrid(problemPath, false, seed, min, max);
				s = gp;
			}
			else if(typeArgs == null || typeArgs.length == 0)
				gp = new GridProblem(problemPath, true, false, null);
			else if(cost != null && cost.equals("life")){
				gp = new LifeCostGrid(problemPath, false);
				s = gp;
			}
			else if(cost != null && cost.equals("random") && typeArgs.length == 7){
				int seed = Integer.parseInt(typeArgs[0]);
				double min = Double.parseDouble(typeArgs[1]);
				double max = Double.parseDouble(typeArgs[2]);
				int startX = Integer.parseInt(typeArgs[3]);
				int startY = Integer.parseInt(typeArgs[4]);
				int endX = Integer.parseInt(typeArgs[5]);
				int endY = Integer.parseInt(typeArgs[6]);
				gp = new RandomCostGrid(problemPath, false, seed, min, max, startX, startY, endX, endY);
				s = gp;
			} else if (cost != null && cost.equals("unit") && typeArgs.length == 4){
				int startX = Integer.parseInt(typeArgs[0]);
				int startY = Integer.parseInt(typeArgs[1]);
				int endX = Integer.parseInt(typeArgs[2]);
				int endY = Integer.parseInt(typeArgs[3]);
				gp = new GridProblem(problemPath, true, false, GridProblem.COST.UNIT, startX, startY, endX, endY);
			}
			else {
				if (typeArgs.length != 4)
					throw new IllegalArgumentException(
							"problem path string didn't parse");
				int startX = Integer.parseInt(typeArgs[0]);
				int startY = Integer.parseInt(typeArgs[1]);
				int endX = Integer.parseInt(typeArgs[2]);
				int endY = Integer.parseInt(typeArgs[3]);
				gp = new GridProblem(problemPath, true, false, startX, startY, endX, endY);
			}
			s = gp;
		}
		if (problemType.equals("randomgrid")) {
			loaded = true;
			int seed = Integer.parseInt(typeArgs[0]);
			double min = Double.parseDouble(typeArgs[1]);
			double max = Double.parseDouble(typeArgs[2]);
			GridProblem gp = new RandomCostGrid(problemPath, false, seed, min, max);
			s = gp;
		}
		if (problemType.equals("robot")) {
			loaded = true;
			RobotProblem rp = new RobotProblem(problemPath);
			s = rp;
		}
		if (problemType.equals("hanoi")) {
			loaded = true;
			HanoiProblem hp = new HanoiProblem(problemPath, cost, typeArgs);
			s = hp;
		}
		if (problemType.equals("topspin")) {
			loaded = true;
			TopspinProblem tp = new TopspinProblem(problemPath, typeArgs, cost);
			s = tp;
		}
		if (problemType.equals("citynav")) {
			loaded = true;
			if (typeArgs.length != 5)
				throw new IllegalArgumentException(
						"problem path strind didn't parse");
			int nCities = Integer.parseInt(typeArgs[0]);
			int nPlaces = Integer.parseInt(typeArgs[1]);
			int nCityNeighbors = Integer.parseInt(typeArgs[2]);
			int nPlaceNeighbors = Integer.parseInt(typeArgs[3]);
			double worldSize = Double.parseDouble(typeArgs[4]);

			int seed = Integer.parseInt(problemPath);
			
			Citynav cp = new Citynav(nCities, nPlaces, nCityNeighbors,
					nPlaceNeighbors, worldSize, seed);
			s = cp;
		}
		if (problemType.equals("pancake")) {
			loaded = true;
			PancakeProblem pp = new PancakeProblem(problemPath, typeArgs, cost);
			s = pp;
		}
		if (problemType.equals("vacuum")) {
			loaded = true;
			VacuumProblem pp = new VacuumProblem(problemPath);
			s = pp;
		}
		if (!loaded) {
			System.err.println("invalid domain selection - " + problemType);
			assert (false);
			System.exit(1);
		}
		assert (s != null);
		return s;
	}
}
