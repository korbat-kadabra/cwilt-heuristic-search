package org.cwilt.search.domains.custom_grid;
import java.util.ArrayList;
import java.util.Comparator;

import org.cwilt.search.search.SearchState;import org.cwilt.search.utils.basic.Heapable;
public class CustomGridCell extends org.cwilt.search.search.SearchState implements org.cwilt.search.utils.basic.Heapable{
	
	public double g;
	
	public static class GComparator implements Comparator<CustomGridCell>{

		@Override
		public int compare(CustomGridCell arg0, CustomGridCell arg1) {
			if(arg0.g < arg1.g)
				return -1;
			if(arg0.g > arg1.g)
				return 1;
			
			return 0;
		}
	}
	public static class HStarComparator implements Comparator<CustomGridCell>{

		@Override
		public int compare(CustomGridCell arg0, CustomGridCell arg1) {
			if(arg0.hStar < arg1.hStar)
				return -1;
			if(arg0.hStar > arg1.hStar)
				return 1;
			
			return 0;
		}
	}
	enum STATUS {
		BLOCKED, OPEN
	}
	public boolean isOpen(){
		return s == STATUS.OPEN;
	}
	
	public String toString(){
		return String.format("x: %d y: %d d*: %d (ix:%d)", x, y, dStar, heapIndex);
	}
	
	final int x, y;
	int dStar;
	double hStar;
	double h;
	private STATUS s;
	
	final ArrayList<CustomGridCell> neighbors;
	final ArrayList<Double> costs;
	
	private final CustomGrid problem;
	
	public CustomGridCell(int x, int y, CustomGrid problem, STATUS s){
		this.x = x;
		this.y = y;
		this.problem = problem;
		this.s = s;
		this.neighbors = new ArrayList<CustomGridCell>();
		this.costs = new ArrayList<Double>();
		this.heapIndex = Heapable.NO_POS;
		this.g = Double.MAX_VALUE;
	}
	
	public void connectGrid(){
		if(x > 0 && problem.map[y][x-1].s == STATUS.OPEN){
			this.neighbors.add(problem.map[y][x-1]);
		}
		if(x < problem.map[0].length - 1 && problem.map[y][x+1].s == STATUS.OPEN){
			this.neighbors.add(problem.map[y][x+1]);
		}
		if(y > 0 && problem.map[y-1][x].s == STATUS.OPEN){
			this.neighbors.add(problem.map[y-1][x]);
		}
		if(y < problem.map.length - 1 && problem.map[y+1][x].s == STATUS.OPEN){
			this.neighbors.add(problem.map[y+1][x]);
		}
	}
	
	@Override
	public ArrayList<Child> expand() {
		ArrayList<Child> children = new ArrayList<Child>();
		for(int i = 0; i < neighbors.size(); i++){
			children.add(new Child(neighbors.get(i), costs.get(i)));
		}
		return children;
	}

	@Override
	public Object getKey() {
		return this;
	}

	@Override
	public double h() {
		return this.h;
	}


	@Override
	public boolean isGoal() {
		return this.equals(problem.getGoal());
	}

	@Override
	public int lexOrder(SearchState s) {
		throw new org.cwilt.search.utils.basic.NotImplementedException();
	}

	@Override
	public ArrayList<Child> reverseExpand() {
		return expand();
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		CustomGridCell other = (CustomGridCell) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	private int heapIndex;
	
	@Override
	public int getHeapIndex() {
		return heapIndex;
	}

	@Override
	public void setHeapIndex(int ix) {
		heapIndex = ix;
	}

	@Override
	public int d() {
		assert(false);
		return 0;
	}
	
	

}
