package org.cwilt.search.domains.kiva.path.timeless;
import java.io.PrintStream;
import java.util.ArrayList;

import org.cwilt.search.domains.kiva.drive.Drive;
import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.kiva.problem.KivaProblem;
import org.cwilt.search.search.SearchState;
public class NavigationProblem implements org.cwilt.search.search.SearchProblem {

	public final Move initial;
	public final GridCell goal;
	public final Drive drive;
	public final KivaProblem problem;
	
	public NavigationProblem(Move initial,  GridCell goal, Drive d, KivaProblem p){
		this.initial = initial;
		this.goal = goal;
		this.drive = d;
		this.problem = p;
		initial.setProblem(this);
	}
	

	@Override
	public SearchState getInitial() {
		return initial;
	}

	@Override
	public SearchState getGoal() {
		throw new RuntimeException("Kiva problems can't ground goals");
	}

	@Override
	public ArrayList<SearchState> getGoals() {
		throw new RuntimeException("Kiva problems can't ground goals");
	}

	@Override
	public void setCalculateD() {
	}
	public void printProblemData(PrintStream p){
	}


}
