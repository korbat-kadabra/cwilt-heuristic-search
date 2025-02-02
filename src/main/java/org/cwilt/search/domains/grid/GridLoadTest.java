package org.cwilt.search.domains.grid;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.Scanner;

import org.junit.Test;

public class GridLoadTest {
	public void rt2test(){
		double sum = 0;
		double rt2 = Math.sqrt(2);
		for(int i = 0; i < 1000; i++){
			sum = sum + 1;
			sum = sum + rt2;
		}
		System.err.println(sum);
		double sum2 = 1000 + 1000 * rt2;
		System.err.println(sum2);
	}

	public void babyLoadTest() throws IOException, ParseException{
		GridProblem p = new GridProblem("griddata/movingai/32room_000.map", false, false, 10, 10, 12, 12);
		org.cwilt.search.search.SearchAlgorithm a = new org.cwilt.search.algs.basic.bestfirst.AStar(p, new org.cwilt.search.search.Limit());
		a.solve();
		a.printSearchData(System.out);
	}
	
	
	@Test
	public void loadTest() throws ParseException, IOException{
		GridProblem p;		
		FileInputStream fs = new FileInputStream("griddata/movingai/8room_000.map.scen");
		DataInputStream ds = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(ds));
		Scanner s = new Scanner(br);
		s.nextLine();
		
		for(int i = 0; i < 1200; i++){
			s.nextLine();
		}
		
		for(int i = 0; i < 5; i++){
			String next = s.nextLine();
			String[] ary = next.split("\t");
			p = new GridProblem("griddata/movingai/8room_000.map", false, false, Integer.parseInt(ary[4]), Integer.parseInt(ary[5]), Integer.parseInt(ary[6]), Integer.parseInt(ary[7]));
			org.cwilt.search.search.SearchAlgorithm a = new org.cwilt.search.algs.basic.bestfirst.AStar(p, new org.cwilt.search.search.Limit());
			a.solve();
			long astarTime = a.getLimit().getDuration();
			org.cwilt.search.search.SearchAlgorithm b = new org.cwilt.search.algs.basic.sfbs.SFBSA(p, new org.cwilt.search.search.Limit(5000, Long.MAX_VALUE, Long.MAX_VALUE, false), false);
			b.solve();
			long sfbsTime = b.getLimit().getDuration();
			if(astarTime > sfbsTime)
				System.err.println(next + " A* " + astarTime + " SF " + sfbsTime);
		}
		s.close();
		br.close();
		
	}
	
}
