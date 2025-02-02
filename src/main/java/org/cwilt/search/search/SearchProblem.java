package org.cwilt.search.search;
import java.io.PrintStream;
import java.util.ArrayList;

public interface SearchProblem {
	public SearchState getInitial();
	public SearchState getGoal();
	public ArrayList<SearchState> getGoals();
	public void setCalculateD();
	public void printProblemData(PrintStream ps);
}
