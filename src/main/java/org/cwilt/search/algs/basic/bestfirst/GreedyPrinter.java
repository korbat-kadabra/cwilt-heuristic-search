package org.cwilt.search.algs.basic.bestfirst;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchProblem;
/**
 * Class that tracks the head of the open list's h value and prints them out to a file.  
 * @author cmo66
 *
 */

public class GreedyPrinter extends Greedy{
	private static final BufferedWriter openWriter(String path) {
		try {
			FileWriter fstream;
			fstream = new FileWriter(path);
			return new BufferedWriter(fstream);
		} catch (IOException e) {
			System.err.println("Error opening this file: " + path);
			return null;
		}
	}

	@Override
	protected double evaluateNode(SearchNode n){
		double nextH = n.getH();
		if(openHead >= nextH){
			currentMinimumSize ++;
		}
		else {
			if(currentMinimumSize > 0){
				counts.add(new Double(currentMinimumSize));
			}
			currentMinimumSize = 0;
		}
		
		openHead = nextH;
		
		try {
			w.write(Long.toString(l.getExpansions()));
			w.write(" ");
			w.write(Double.toString(nextH));
			w.write("\n");
		} catch (IOException e) {
			System.err.println("Error writing to file");
		}
		
		return nextH;
	}
	
	
	@Override
	public void printExtraData(PrintStream ps){
		super.printExtraData(ps);
		try {
			w.close();
		} catch (IOException e) {
			System.err.println("Error closing out file");
		}
		
	}


	
	private final BufferedWriter w;
	public GreedyPrinter(SearchProblem initial, Limit l, String path) {
		super(initial, l);
		this.w = openWriter(path);
	}
	
	
}
