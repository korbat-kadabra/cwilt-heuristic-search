package org.cwilt.search.domains.kiva.map;
import java.io.PrintStream;
import java.util.ArrayList;

import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
import org.cwilt.search.search.SearchState;
public class OpenPath implements org.cwilt.search.search.SearchProblem{
	
	public class OpenPathNode extends org.cwilt.search.search.SearchState{
		public final GridCell loc;
		
		public OpenPathNode(GridCell loc){
			this.loc = loc;
		}
		
		@Override
		public ArrayList<Child> expand() {
			ArrayList<Child> children = new ArrayList<Child>(4);
			for(MultiagentVertex v : this.loc.getNeighbors(loc)){
				GridCell n = (GridCell) v;
				if(n.isTravel()){
					children.add(new Child(new OpenPathNode(n), 1.0));
				}
			}
			return children;
		}

		@Override
		public ArrayList<Child> reverseExpand() {
			return this.expand();
		}

		@Override
		public double h() {
			return loc.distanceTo(end);
		}

		@Override
		public int d() {
			return (int) loc.distanceTo(end);
		}

		@Override
		public boolean isGoal() {
			return this.loc.equals(end);
		}

		@Override
		public Object getKey() {
			return loc;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((loc == null) ? 0 : loc.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			OpenPathNode other = (OpenPathNode) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (loc == null) {
				if (other.loc != null)
					return false;
			} else if (!loc.equals(other.loc))
				return false;
			return true;
		}

		@Override
		public int lexOrder(SearchState s) {
			// TODO Auto-generated method stub
			return 0;
		}

		private OpenPath getOuterType() {
			return OpenPath.this;
		}
		
	}
	
	private final GridCell start, end;
	
	public OpenPath(GridCell start, GridCell end){
		this.start = start;
		this.end = end;
	}
	
	@Override
	public SearchState getInitial() {
		return new OpenPathNode(start);
	}

	@Override
	public SearchState getGoal() {
		return new OpenPathNode(start);
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
