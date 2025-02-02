package org.cwilt.search.domains.grid;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Scanner;

import org.cwilt.search.search.AlgPicker;import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchMain;import org.cwilt.search.utils.basic.ArgParser;import org.cwilt.search.utils.basic.ArgParser.InvalidCommandLineInput;
public class GridScenario {

	private Limit l;

	public void printData(PrintStream ps) {
		ps.println("#cols  \"sol cost\"\t\"sol length\"\t\"nodes expanded\"\t\"nodes generated\"\t\"duplicates encountered\"\t\"raw cpu time\"");
		ps.println("inf\t0\t0\t0\t0\t0.000000");

		double solTime = ((double) l.getDuration()) / (1000.0);
		SearchAlgorithm
				.printPair(ps, "total raw cpu time", new Double(solTime));
		SearchAlgorithm.printPair(ps, "peak virtual mem usage kb", new Long(
				Runtime.getRuntime().totalMemory()));
		SearchAlgorithm.printPair(ps, "number of duplicates found",
				new Long(l.getDuplicates()));
		SearchAlgorithm.printPair(ps, "total nodes expanded",
				new Long(l.getExpansions()));
		SearchAlgorithm.printPair(ps, "total nodes generated",
				new Long(l.getGenerations()));
		if (l.getOutOfMemory())
			SearchAlgorithm.printPair(ps, "out of memory", "true");

	}

	private GridProblem gp;
	
	private class Scenario{
		public final int startX, startY, endX, endY;
		public Scenario(int startX, int startY, int endX, int endY){
			this.startX = startX;
			this.startY = startY;
			this.endX = endX;
			this.endY = endY;
		}
	}
	
	private GridProblem getProblem(Scenario s){
		return new GridProblem(gp, s.startX, s.startY, s.endX, s.endY);
	}
	
	private final ArrayList<Scenario> scenarios;
	
	public GridScenario(String problem, String scenario, int start, int end)
			throws ParseException, IOException {
		this.l = new Limit();
		this.scenarios = new ArrayList<Scenario>(end - start);
		this.gp = new GridProblem(problem, true, false, 0, 0, 0, 0);

		FileInputStream fs = new FileInputStream(scenario);
		DataInputStream ds = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(ds));
		Scanner s = new Scanner(br);
		s.nextLine();
		for (int i = 0; i < start; i++) {
			s.nextLine();
		}
		for (int i = 0; i < end - start; i++) {
			String next = s.nextLine();
			String[] ary = next.split("\t");
			Scenario scen = new Scenario(Integer.parseInt(ary[4]),
					Integer.parseInt(ary[5]), Integer.parseInt(ary[6]),
					Integer.parseInt(ary[7]));
			scenarios.add(scen);
		}
		s.close();
		fs.close();
	}

	public void solve() {

	}

	public static void main(String[] args) throws InvalidCommandLineInput,
			IOException, ClassNotFoundException, ParseException {

		Object[] s = null;
		if (args.length != 0) {
			s = ArgParser.parseArgs(SearchMain.commandArgs, args);
			if (s[SearchMain.ALG] == null) {
				System.err
						.println("Must select an algorithm (-alg arg not specified");
				System.exit(1);
			}
			if (s[SearchMain.PROBLEM] == null) {
				System.err
						.println("Must select a problem (-problem arg not specified");
				System.exit(1);
			}
			if (s[SearchMain.TYPE] == null) {
				System.err
						.println("Must select a type (-type arg not specified");
				System.exit(1);
			}
		}
		if (s == null) {
			System.err.println("Error initializing arguments");
			System.exit(1);
		}
		String alg = (String) s[SearchMain.ALG];
		String[] rest = (String[]) s[SearchMain.REST];
		String[] probargs = (String[]) s[SearchMain.PROBARGS];
		String problemPath = (String) s[SearchMain.PROBLEM];
		String problemType = (String) s[SearchMain.TYPE];


		org.cwilt.search.search.Limit limit = SearchMain.makeLimit(s);
		
		int start = Integer.parseInt(probargs[0]);
		int end = Integer.parseInt(probargs[1]);
		
		GridScenario gs = new GridScenario(problemPath, problemType, start, end);
		
		for(Scenario scenario : gs.scenarios){
			GridProblem p = gs.getProblem(scenario);
			SearchAlgorithm algRun = AlgPicker.pickAlg(alg, rest, p, limit.clone());
			
			algRun.solve();
			
			//algRun.getLimit().addTo(gs.l);
			gs.l.addTo(algRun.getLimit());
		}
		gs.printData(System.out);
	}

}
