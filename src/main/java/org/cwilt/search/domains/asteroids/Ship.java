package org.cwilt.search.domains.asteroids;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Set;

public abstract class Ship implements Cloneable {
	public final double theta, x, y, dx, dy, dt;
	
	public abstract Ship clone();
	
	private static final Ellipse2D.Double saucer = new Ellipse2D.Double(-10, -25, 20, 30);
	private static final Rectangle2D.Double stardrive = new Rectangle2D.Double(-3, -1, 6, 15);
	private static final Rectangle2D.Double nacelleBoom = new Rectangle2D.Double(-7, 10, 14,
			3);
	private static final Rectangle2D.Double leftNacelle = new Rectangle2D.Double(-10, 6, 3,
			10);
	private static final Rectangle2D.Double rightNacelle = new Rectangle2D.Double(7, 6, 3,
			10);
	private static final Ellipse2D.Double leftBussard = new Ellipse2D.Double(-10, 4.5, 3, 3);
	private static final Ellipse2D.Double rightBussard = new Ellipse2D.Double(7, 4.5, 3, 3);

	private static final ArrayList<Shape> shipParts = new ArrayList<Shape>(7);
	
	static {
		shipParts.add(saucer);
		shipParts.add(stardrive);
		shipParts.add(nacelleBoom);
		shipParts.add(leftNacelle);
		shipParts.add(rightNacelle);
		shipParts.add(leftBussard);
		shipParts.add(rightBussard);
	}
	
	protected final Asteroids game;

	
	protected Ship(double heading, double x, double y, Asteroids cbw) {
		this.game = cbw;
		this.theta = heading;
		this.y = y;
		this.x = x;
		this.dx = 0;
		this.dy = 0;
		this.dt = 0;
	}
	protected Ship(Ship parent){
		this.game = parent.game;
		this.theta = parent.theta;
		this.y = parent.y;
		this.x = parent.x;
		this.dx = parent.dx;
		this.dy = parent.dy;
		this.dt = parent.dt;
	}
	protected Ship(double dx, double dy, double dt, Ship parent) {
		this.game = parent.game;
		this.theta = parent.theta + dt;
		this.y = parent.y + dy;
		this.x = parent.x + dx;
		this.dx = dx;
		this.dy = dy;
		this.dt = parent.dt;
	}
	

	public abstract Ship expand(Set<Integer> controls);
	
	protected int gunTimer = 0;
	protected static final int GUN_COOLDOWN = 5;

	public boolean canShoot(){
		return gunTimer == 0;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(dt);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(dx);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(dy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + gunTimer;
		temp = Double.doubleToLongBits(theta);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		Ship other = (Ship) obj;
		if (Double.doubleToLongBits(dt) != Double.doubleToLongBits(other.dt))
			return false;
		if (Double.doubleToLongBits(dx) != Double.doubleToLongBits(other.dx))
			return false;
		if (Double.doubleToLongBits(dy) != Double.doubleToLongBits(other.dy))
			return false;
		if (gunTimer != other.gunTimer)
			return false;
		if (Double.doubleToLongBits(theta) != Double
				.doubleToLongBits(other.theta))
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	public void draw(Graphics2D g) {
		AffineTransform t = g.getTransform();

		g.translate(x, y);
		g.rotate(theta + Math.PI / 2);
		g.scale(2, 2);

		g.setColor(Color.DARK_GRAY);
		g.draw(nacelleBoom);
		g.setColor(Color.GRAY);
		g.fill(nacelleBoom);

		g.setColor(Color.DARK_GRAY);
		g.draw(stardrive);
		g.setColor(Color.GRAY);
		g.fill(stardrive);

		g.setColor(Color.DARK_GRAY);
		g.draw(saucer);
		g.setColor(Color.GRAY);
		g.fill(saucer);

		g.setColor(Color.RED);
		g.fill(leftBussard);
		g.fill(rightBussard);

		g.setColor(Color.GRAY);
		g.fill(leftNacelle);
		g.setColor(Color.BLUE);
		g.draw(leftNacelle);
		g.setColor(Color.GRAY);
		g.fill(rightNacelle);
		g.setColor(Color.BLUE);
		g.draw(rightNacelle);


		g.setTransform(t);
	}
	
	public PhaserPulse shootGun(){
		PhaserPulse p = new PhaserPulse(x, y, theta);
		return p;
	}
	
	private static final double SIZE = 30;
	
	boolean offScreen(){
		if(x < SIZE)
			return true;
		if(y < SIZE)
			return true;
		if(x > AsteroidProblem.X_SIZE - SIZE)
			return true;
		if(y > AsteroidProblem.Y_SIZE - SIZE)
			return true;
		else
			return false;
	}
	
	public boolean collidesWith(Asteroid a){
		double distTo = Point2D.Double.distance(x, y, a.x + a.size / 2, a.y + a.size / 2);
		if (distTo > SIZE + a.size)
			return false;
		Area shipArea = new Area();
		for(Shape s : shipParts){
			shipArea.add(new Area(s));
		}
		AffineTransform transform = new AffineTransform();
		transform.scale(1, 1);
		transform.rotate(theta + Math.PI / 2);
		transform.translate(x, y);
		shipArea.transform(transform);
		Area asteroidArea = new Area(a.asteroid);
		asteroidArea.intersect(shipArea);
		return asteroidArea.isEmpty();
	}

	@Override
	public String toString() {
		String t = String.format("%1.3f", theta);
		String xp = String.format("%1.3f", x);
		String yp = String.format("%1.3f", y);
		return "Ship [theta=" + t + ", x=" + xp + ", y=" + yp + "]";
	}

	public abstract ArrayList<Set<Integer>> getActions();
	public abstract boolean isTerminal();
	
	public abstract Ship explode();
	public double angleTo(Asteroid a) {
		double aheadX = Math.cos(theta);
		double aheadY = Math.sin(theta);
		
		
		double asteroidX = a.x - x;
		double asteroidY = a.y - y;
		
		double asteroidDistance = Math.sqrt(asteroidX * asteroidX + asteroidY * asteroidY);
		asteroidX = asteroidX / asteroidDistance;
		asteroidY = asteroidY / asteroidDistance;
		
		double dotProduct = aheadX * asteroidX + aheadY * asteroidY;
		
		// TODO Auto-generated method stub
		return Math.abs(Math.acos(dotProduct));
	}
	
	
}
