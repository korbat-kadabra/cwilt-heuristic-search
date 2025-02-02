package org.cwilt.search.domains.asteroids;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class BasicShip extends Ship {

	public BasicShip(double heading, double x, double y, Asteroids c) {
		super(heading, x, y, c);
	}

	private static final double THETA_INCR = Math.PI / 32;
	private static final double SPEED_INCR = 1;
	private static final double[] THETA_DELTAS = { THETA_INCR, 0, -THETA_INCR };
	private static final double[] SPEED_DELTAS = { SPEED_INCR, 0, -SPEED_INCR };

	private BasicShip(double dx, double dy, double dt, BasicShip parent,
			boolean shot) {
		super(dx, dy, dt, parent);
		if (parent.gunTimer > 0)
			super.gunTimer = parent.gunTimer - 1;
		if (shot) {
			super.gunTimer = Ship.GUN_COOLDOWN;
		}
	}

	public Ship expand(Set<Integer> controls) {
		double deltaSpeed = 0;
		double deltaTheta = 0;
		boolean shot = false;
		
		if (controls.contains(KeyEvent.VK_DOWN)) {
			deltaSpeed = -SPEED_INCR;
		}
		if (controls.contains(KeyEvent.VK_UP)) {
			deltaSpeed = SPEED_INCR;
		}
		if (controls.contains(KeyEvent.VK_LEFT)) {
			deltaTheta = -THETA_INCR;
		}
		if (controls.contains(KeyEvent.VK_RIGHT)) {
			deltaTheta = THETA_INCR;
		}
		if (controls.contains(KeyEvent.VK_SPACE) && super.canShoot()) {
			shot = true;
		}

		double newTheta = theta + deltaTheta;
		double newDX = dx + Math.cos(newTheta) * deltaSpeed;
		double newDY = dy + Math.sin(newTheta) * deltaSpeed;
		Ship newShip = new BasicShip(newDX, newDY, deltaTheta, this, shot);
		if (newShip.offScreen())
			return new ExplodedShip(this);
		else
			return newShip;
	}

	private BasicShip(BasicShip parent) {
		super(parent);
	}

	public Ship clone() {
		return new BasicShip(this);
	}

	private static final ArrayList<Set<Integer>> actions = new ArrayList<Set<Integer>>();
	{
		if (actions.isEmpty()) {
			for (double s : SPEED_DELTAS) {
				for (double t : THETA_DELTAS) {
					HashSet<Integer> noGun = new HashSet<Integer>();
					HashSet<Integer> gun = new HashSet<Integer>();
					if (s < 0) {
						noGun.add(KeyEvent.VK_DOWN);
						gun.add(KeyEvent.VK_DOWN);
					}
					if (s > 0) {
						noGun.add(KeyEvent.VK_UP);
						gun.add(KeyEvent.VK_UP);
					}
					if (t > 0) {
						noGun.add(KeyEvent.VK_LEFT);
						gun.add(KeyEvent.VK_LEFT);
					}
					if (t < 0) {
						noGun.add(KeyEvent.VK_RIGHT);
						gun.add(KeyEvent.VK_RIGHT);
					}
					gun.add(KeyEvent.VK_SPACE);
					actions.add(gun);
					actions.add(noGun);
				}
			}
		}
	}

	@Override
	public ArrayList<Set<Integer>> getActions() {
		return actions;
	}
	public boolean isTerminal(){
		return false;
	}
	
	public Ship explode(){
		return new ExplodedShip(this);
	}

}
