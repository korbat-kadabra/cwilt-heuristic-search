package org.cwilt.search.domains.kiva_lite;
import java.util.ArrayList;

import org.cwilt.search.search.SearchState;
public class LiteFiducial extends SearchState implements Comparable<LiteFiducial>{
	
	private final int x, y;
	private double h;
	private int d;
	private boolean blocked;
	
	public static final int LEFT = 0;
	public static final int RIGHT = 1;
	public static final int UP = 2;
	public static final int DOWN = 3;
	
	private final LiteFiducial[] neighbors;
	private final double[] costs;
	
	private final KivaLiteMap map;
	
	public void connectTo(int ix, LiteFiducial neighbor, double cost){
		neighbors[ix] = neighbor;
		costs[ix] = cost;
	}
	
	public void block(){
		this.blocked = true;
	}
	public void increaseCost(double factor){
		for(int i = 0; i < 4; i++){
			costs[i] = costs[i] * factor;
		}
	}
	public void setH(double value){
		this.h = value;
	}
	
	public LiteFiducial(int x, int y, KivaLiteMap map){
		this.x = x;
		this.y = y;
		this.neighbors = new LiteFiducial[4];
		this.costs = new double[4];
		this.blocked = false;
		this.map = map;
	}
	
	public boolean adjacent(LiteFiducial o){
		return adjacentX(o) != adjacentY(o);
	}
	private boolean adjacentX(LiteFiducial o){
		return this.x == o.x + 1 || this.x == o.x - 1;
	}
	private boolean adjacentY(LiteFiducial o){
		return this.y == o.y + 1 || this.y == o.y - 1;
	}
	
	@Override
	public ArrayList<Child> expand() {
		ArrayList<Child> children = new ArrayList<Child>(4);
		if(blocked)
			return children;
		for(int i = 0; i < 4; i++){
			if(neighbors[i] != null)
				children.add(new Child(neighbors[i], costs[i]));
		}
		return children;
	}
	@Override
	public ArrayList<Child> reverseExpand() {
		return this.expand();
	}
	@Override
	public double h() {
		switch(map.getHType()){
		case BACKWARDS:
			return h;
		case ZERO:
			return 0;
		case MANHATTAN:
			return x + y;
		}
		return 0;
	}
	@Override
	public int d() {
		return d;
	}
	@Override
	public boolean isGoal() {
		return x == 0 && y == 0;
	}
	@Override
	public Object getKey() {
		return this;
	}
	@Override
	public int lexOrder(SearchState s) {
		assert(s instanceof LiteFiducial);
		LiteFiducial other = (LiteFiducial) s;
		if(this.x < other.x)
			return -1;
		else if(this.x > other.x)
			return 1;
		else if(this.y < other.y)
			return -1;
		else if(this.y > other.y)
			return 1;
		else 
			return 0;
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
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LiteFiducial other = (LiteFiducial) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
	
	public String toString(){
		return "Fiducial x: " + x + " y: " + y;
	}


	@Override
	public int compareTo(LiteFiducial o) {
		if(this.equals(o))
			return 0;
		else if(this.h < o.h)
			return -1;
		else if(this.h > o.h)
			return 1;
		else{
			throw new IllegalArgumentException();
		}
	}
	
}
