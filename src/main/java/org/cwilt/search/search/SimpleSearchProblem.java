package org.cwilt.search.search;
import java.io.PrintStream;
import java.util.ArrayList;

public class SimpleSearchProblem implements SearchProblem{

	public void printProblemData(PrintStream p){
		
	}
	
	public SimpleSearchProblem(SearchState initial){
		this.initial = initial;
	}

	private final SearchState initial;
	
	@Override
	public SearchState getGoal() {
		throw new NoCanonicalGoal();
	}
	
	public ArrayList<SearchState> getGoals(){
		throw new NoCanonicalGoal();
	}

	@Override
	public SearchState getInitial() {
		return initial;
	}
	
	@Override
	public void setCalculateD(){
		return;
	}

}
