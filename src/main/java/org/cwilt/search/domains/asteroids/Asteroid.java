package org.cwilt.search.domains.asteroids;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ListIterator;

public class Asteroid {
	private static final int SIZE_DECREMENT = 10;
	private static final int MINSIZE = 15;
	public final int size;
	public double x, y, vx, vy;
	public final Ellipse2D.Double asteroid;
	public static Asteroid advanceTime(Asteroid parent) {
		double newX = parent.x + parent.vx;
		double newY = parent.y + parent.vy;
		double newVX = parent.vx;
		double newVY = parent.vy;
		if(newX < 0){
			newX = -newX;
			newVX = -parent.vx;
		}
		else if(newX + parent.size >  AsteroidProblem.X_SIZE){
			double extra = newX + parent.size - AsteroidProblem.X_SIZE;
			newX = AsteroidProblem.X_SIZE - parent.size - extra;
			newVX = -parent.vx;
		}

		if(newY < 0){
			newY = -newY;
			newVY = -parent.vy;
		}
		else if(newY + parent.size >  AsteroidProblem.Y_SIZE){
			double extra = newY + parent.size - AsteroidProblem.Y_SIZE;
			newY = AsteroidProblem.Y_SIZE - parent.size - extra;
			newVY = -parent.vy;
		}
		return new Asteroid(parent.size, newX, newY, newVX, newVY);
	}

	@Override
	public String toString() {
		return "Asteroid [x=" + x + ", y=" + y + "]";
	}

	public Asteroid(int size, double x, double y, double dx, double dy){
		this.size = size;
		this.vx = dx;
		this.vy = dy;
		this.x = x;
		this.y = y;
		this.asteroid = new Ellipse2D.Double(x, y, size, size);
	}

	
	public void draw(Graphics2D g){
		assert(asteroid.x <= AsteroidProblem.X_SIZE - size);
		assert(asteroid.y <= AsteroidProblem.Y_SIZE - size);
		g.draw(asteroid);
		g.fill(asteroid);
	}
	
	public void advance(){
		double newX = x + vx;
		double newY = y + vy;
		if(newX < 0){
			x = -newX;
			vx = -vx;
		}
		else if(newX + size >  AsteroidProblem.X_SIZE){
			double extra = newX + size - AsteroidProblem.X_SIZE;
			x = AsteroidProblem.X_SIZE - size - extra;
			vx = -vx;
		} else 
			this.x = newX;
		

		if(newY < 0){
			y = -newY;
			vy = -vy;
		}
		else if(newY + size >  AsteroidProblem.Y_SIZE){
			double extra = newY + size - AsteroidProblem.Y_SIZE;
			y = AsteroidProblem.Y_SIZE - size - extra;
			vy = -vy;
		}
		else 
			this.y = newY;
		asteroid.setFrame(x, y, size, size);
	}
	
	private static final double NORMAL_RATIO = 0.1;
	
	public void split(ListIterator<Asteroid> l, PhaserPulse p){
		int newSize = size - SIZE_DECREMENT;
		//asteroid is now gone
		if(newSize <= MINSIZE)
			return;
		double baseDX = vx + p.dx;
		double baseDY = vy + p.dy;
		Asteroid a1 = new Asteroid(newSize, x, y, baseDX - baseDY * NORMAL_RATIO, baseDY + baseDX * NORMAL_RATIO);
		Asteroid a2 = new Asteroid(newSize, x, y, baseDX + baseDY * NORMAL_RATIO, baseDY - baseDX * NORMAL_RATIO);
		l.add(a1);
		l.add(a2);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + size;
		long temp;
		temp = Double.doubleToLongBits(vx);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(vy);
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
		Asteroid other = (Asteroid) obj;
		if (size != other.size)
			return false;
		if (Double.doubleToLongBits(vx) != Double.doubleToLongBits(other.vx))
			return false;
		if (Double.doubleToLongBits(vy) != Double.doubleToLongBits(other.vy))
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}
}
