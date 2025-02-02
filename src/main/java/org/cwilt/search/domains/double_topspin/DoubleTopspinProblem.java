package org.cwilt.search.domains.double_topspin;
import java.io.PrintStream;
import java.util.ArrayList;

import org.cwilt.search.search.SearchProblem;import org.cwilt.search.search.SearchState;
public class DoubleTopspinProblem implements SearchProblem {

	@Override
	public SearchState getInitial() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SearchState getGoal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<SearchState> getGoals() {
		ArrayList<SearchState> goals = new ArrayList<SearchState>(1);
		goals.add(this.getGoal());
		return goals;
	}

	@Override
	public void setCalculateD() {

	}

	@Override
	public void printProblemData(PrintStream ps) {

	}

}
