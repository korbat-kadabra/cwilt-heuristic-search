package org.cwilt.search.domains.asteroids;
import java.util.LinkedList;

import org.cwilt.search.domains.asteroids.planner.AsteroidState;
public class AsteroidProblem {
	private final AsteroidState initial;

	public final AsteroidState getInitial() {
		return initial;
	}

	public static final int X_SIZE = 800;
	public static final int Y_SIZE = 600;

	public AsteroidProblem(int nAsteroids) {
		LinkedList<Asteroid> asteroids = new LinkedList<Asteroid>();
		LinkedList<PhaserPulse> pulses = new LinkedList<PhaserPulse>();
		if(nAsteroids >= 1)
			asteroids.add(new Asteroid(50, 100, 300, 10, 10));
		if(nAsteroids >= 2)
			asteroids.add(new Asteroid(50, 100, 200, 10, 10));
		if(nAsteroids >= 3)
			asteroids.add(new Asteroid(50, 100, 100, 10, 10));
		if(nAsteroids >= 4)
			throw new RuntimeException("too many asteroids");
		Ship ship = new BasicShip(0, AsteroidProblem.X_SIZE / 2,
				AsteroidProblem.Y_SIZE / 2, null);

		this.initial = new AsteroidState(asteroids, pulses, ship);
	}
	
	public void setAsteroidsGUI(Asteroids a){
		
	}
}
