package org.cwilt.search.domains.car;
import java.util.ArrayList;

import org.cwilt.search.search.SearchState;
public class CarState extends SearchState {

	public ArrayList<Child> reverseExpand(){
		assert(false);
		return null;
	}
	
	private Car c;
	private final Lot l;
	private final ACTION a;

	public ACTION getAction() {
		return a;
	}

	public CarState(Lot l, Car c) {
		this.l = l;
		this.c = l.c;
		this.a = ACTION.STRAIGHT_NO;
	}

	public static enum ACTION {
		STRAIGHT_GO, LEFT_GO, RIGHT_GO, LEFT_SLOW, RIGHT_SLOW, STRAIGHT_SLOW, LEFT_NO, RIGHT_NO, STRAIGHT_NO
	}

	private static final ACTION actions[] = { 
		ACTION.STRAIGHT_GO, 
		ACTION.LEFT_GO, ACTION.RIGHT_GO,
		ACTION.LEFT_SLOW, ACTION.RIGHT_SLOW,
		ACTION.STRAIGHT_SLOW, ACTION.LEFT_NO, ACTION.RIGHT_NO,
		ACTION.STRAIGHT_NO };

	public CarState(ACTION a, CarState parent) {
		this.l = parent.l;
		this.c = parent.c.clone();
		this.a = a;
		switch (a) {
		case LEFT_GO:
			c.updateCar(-l.deltaH, l.deltaS);
			break;
		case RIGHT_GO:
			c.updateCar(l.deltaH, l.deltaS);
			break;
		case STRAIGHT_GO:
			c.updateCar(0, l.deltaS);
			break;
		case LEFT_SLOW:
			c.updateCar(-l.deltaH, -l.deltaS);
			break;
		case RIGHT_SLOW:
			c.updateCar(l.deltaH, -l.deltaS);
			break;
		case STRAIGHT_SLOW:
			c.updateCar(0, -l.deltaS);
			break;
		case LEFT_NO:
			c.updateCar(-l.deltaH, 0);
			break;
		case RIGHT_NO:
			c.updateCar(l.deltaH, 0);
			break;
		case STRAIGHT_NO:
			c.updateCar(0, 0);
			break;
		}
	}

	@Override
	public ArrayList<Child> expand() {
		ArrayList<Child> children = new ArrayList<Child>();

		for (ACTION a : actions) {
			children.add(new Child(new CarState(a, this), 1.0));
		}

		return children;
	}

	@Override
	public Object getKey() {
		return c.getKey();
	}


	@Override
	public boolean isGoal() {
		return l.isGoal(c);
	}

	@Override
	public int lexOrder(SearchState s) {
		throw new org.cwilt.search.utils.basic.NotImplementedException();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime;
		result = prime * result + ((c == null) ? 0 : c.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		CarState other = (CarState) obj;
		if (c == null) {
			if (other.c != null)
				return false;
		} else if (!c.equals(other.c))
			return false;
		return true;
	}
	
	public String toString(){
		return c.toString();
	}

	@Override
	public int d() {
		return l.calculateD(c);
	}
	@Override
	public double h() {
		return l.calculateH(c);
	}

}
