package org.cwilt.search.domains.kiva.path.timeless;
import java.util.ArrayList;

import org.cwilt.search.domains.kiva.drive.Drive;
import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.kiva.path.temporal.TemporalMove;
import org.cwilt.search.domains.kiva.problem.KivaProblem;
import org.cwilt.search.search.SearchState;
public abstract class Move extends SearchState {

	public static enum DIRECTION {
		VERTICAL, HORIZONTAL, TURN_H, TURN_V
	}

	public final GridCell startPosition;
	public final GridCell endPosition;
	public final DIRECTION startDir;
	public final DIRECTION endDir;

	private NavigationProblem problem;
	
	protected NavigationProblem getProblem(){
		return problem;
	}
	
	public void setProblem(NavigationProblem p) {
		this.problem = p;
	}

	protected Move(GridCell startP, GridCell endP, DIRECTION startD,
			DIRECTION endD, NavigationProblem problem) {
		this.startPosition = startP;
		this.endPosition = endP;
		this.startDir = startD;
		this.endDir = endD;
		this.problem = problem;
	}

	protected abstract void getChildren(ArrayList<Move> children);

	protected boolean isTemporal() {
		return false;
	}
	
	public enum MOVETYPE{
		TEMPORAL, SIMPLE, TIMELESS
	}
	
	public abstract MOVETYPE getMoveType();

	public abstract boolean canDo(KivaProblem p, Drive d, GridCell destination);

	@Override
	public ArrayList<Child> expand() {	
		ArrayList<Move> children = new ArrayList<Move>();

		this.getChildren(children);

		ArrayList<Child> nodeChildren = new ArrayList<Child>();

		for (Move m : children) {
			assert(problem != null);
			if (m.canDo(problem.problem, problem.drive, problem.goal)) {
				nodeChildren.add(new Child(m, m.getCost()));
				m.setProblem(problem);
				assert (this.isTemporal() == m.isTemporal());
			}
		}
		return nodeChildren;
	}

	private final double getCost() {
		if(getProblem().problem.ghost()){
			return 1.0d + endPosition.getUsage() * 0.d;
		} else 
			return 1;
	}

	@Override
	public ArrayList<Child> reverseExpand() {
		throw new RuntimeException("Can't reverse expand kiva moves");
	}
	
	@Override
	public double h() {
		return endPosition.simpleH(problem.goal);
	}

	@Override
	public int d() {
		return endPosition.manhattan(problem.goal);
	}

	@Override
	public boolean isGoal() {
		assert (problem != null);
		return problem.goal.equals(endPosition);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((startDir == null) ? 0 : startDir.hashCode());
		result = prime * result + ((endDir == null) ? 0 : endDir.hashCode());
		result = prime * result
				+ ((startPosition == null) ? 0 : startPosition.hashCode());
		result = prime * result
				+ ((endPosition == null) ? 0 : endPosition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Move other = (Move) obj;
		if (this.startPosition != other.startPosition
				&& (this.startPosition == null || !this.startPosition
						.equals(other.startPosition))) {
			return false;
		}
		if (this.endPosition != other.endPosition
				&& (this.endPosition == null || !this.endPosition
						.equals(other.endPosition))) {
			return false;
		}
		if (this.startDir != other.startDir) {
			return false;
		}
		if (this.endDir != other.endDir) {
			return false;
		}
		return true;
	}

	@Override
	public int lexOrder(SearchState s) {
		throw new RuntimeException("Kiva moves don't have lexical ordering");
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append(this.getClass().getCanonicalName());
		b.append(" ");
		b.append(startPosition);
		if (this.startDir == DIRECTION.HORIZONTAL)
			b.append("H");
		else
			b.append("V");
		b.append("->");
		b.append(endPosition);
		if (this.endDir == DIRECTION.HORIZONTAL)
			b.append("H");
		else
			b.append("V");
		b.append(" ");
		if (this.isTemporal()) {
			b.append("@");
			TemporalMove t = (TemporalMove) this;
			b.append(t.key.time);
		}
		b.append("\n");
		return b.toString();
	}

}
