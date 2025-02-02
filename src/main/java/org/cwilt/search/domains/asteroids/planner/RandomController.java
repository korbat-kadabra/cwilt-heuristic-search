package org.cwilt.search.domains.asteroids.planner;
import java.util.ArrayList;
import java.util.Random;

import org.cwilt.search.domains.asteroids.AsteroidProblem;
import org.cwilt.search.search.SearchState.Child;
public class RandomController extends AsteroidPlanner {
	private final Random r;
	public RandomController(AsteroidProblem a, int timeout, String[] args) {
		super(a, timeout);
		int seed = Integer.parseInt(args[2]);
		this.r = new Random(seed);
		
	}

	@Override
	public AsteroidState nextState() {
		ArrayList<Child> children = super.getCurrentState().expand();
		if(children.size() == 0)
			return null;
		return (AsteroidState) children.get(r.nextInt(children.size())).child;
	}

}
