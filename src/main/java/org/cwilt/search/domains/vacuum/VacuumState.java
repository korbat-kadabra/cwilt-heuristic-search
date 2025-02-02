package org.cwilt.search.domains.vacuum;
import java.util.ArrayList;

import org.cwilt.search.domains.vacuum.VacuumProblem.VacuumCell;
import org.cwilt.search.search.SearchState;
public class VacuumState extends org.cwilt.search.search.SearchState{
	public final int x, y;
	public final VacuumCell[] dirts;
	private final VacuumProblem vp;
	
	public VacuumState(int x, int y, VacuumCell[] dirts, VacuumProblem vp){
		this.x = x;
		this.y = y;
		this.dirts = dirts;
		this.vp = vp;
	}
	
	@Override
	public ArrayList<Child> expand() {
		ArrayList<Child> children = new ArrayList<Child>(6);
		//left
		if(x > 0)
			children.add(new Child(new VacuumState(x-1, y, dirts, vp), 1.0));
		//right
		if(x < vp.width - 1)
			children.add(new Child(new VacuumState(x+1, y, dirts, vp), 1.0));		
		//up
		if(y > 0)
			children.add(new Child(new VacuumState(x, y-1, dirts, vp), 1.0));
		//down
		if(y < vp.height - 1)
			children.add(new Child(new VacuumState(x, y+1, dirts, vp), 1.0));
		//vacuum
		VacuumCell here = vp.world[x][y];
		if(here.dirtID != -1){
			if(dirts[here.dirtID] != null){
				VacuumCell[] newDirts = dirts.clone();
				newDirts[here.dirtID] = null;
				children.add(new Child(new VacuumState(x, y, newDirts, vp), 1.0));
			}
		}
		//recharge?
		return children;
	}

	@Override
	public ArrayList<Child> reverseExpand() {
		throw new UnsupportedOperationException();
	}

	@Override
	public double h() {
		return vp.calculateH(this);
	}

	@Override
	public int d() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isGoal() {
		for(VacuumCell c : dirts){
			if(c != null)
				return false;
		}
		return true;
	}

	@Override
	public Object getKey() {
		return this;
	}


	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dirts == null) ? 0 : dirts.hashCode());
		result = prime * result + x;
		result = prime * result + y;
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
		VacuumState other = (VacuumState) obj;
		if (dirts == null) {
			if (other.dirts != null)
				return false;
		} else if (!dirts.equals(other.dirts))
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
	
	private final int dirtCount(){
		int dirtCount = 0;
		for(VacuumCell c : dirts){
			if(c != null)
				dirtCount ++;
		}
		return dirtCount;
	}
	
	@Override
	public int lexOrder(SearchState state) {
		VacuumState s = (VacuumState) state;
		if(this.dirtCount() < s.dirtCount())
			return -1;
		else if(this.dirtCount() > s.dirtCount())
			return 1;
		else if(this.x < s.x)
			return -1;
		else if(this.x > s.x)
			return 1;
		else if(this.y < s.y)
			return -1;
		else if(this.y > s.y)
			return 1;
		else
			return 0;
	}

}
