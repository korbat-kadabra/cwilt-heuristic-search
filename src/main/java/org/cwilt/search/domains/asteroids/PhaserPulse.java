package org.cwilt.search.domains.asteroids;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

public class PhaserPulse {
	private static final double SIZE = 2;
	private static final double SPEED = 15;
	
	final double dx, dy;
	public final Ellipse2D.Double shot;
	
	
	public static PhaserPulse advanceTime(PhaserPulse parent){
		double newX = parent.shot.x + parent.dx;
		double newY = parent.shot.y + parent.dy;
		PhaserPulse next = new PhaserPulse(newX, newY, parent.dx, parent.dy);
		if(next.offScreen())
			return null;
		else
			return next;
	}
	
	public PhaserPulse(double x, double y, double heading){
		this.dx = Math.cos(heading) * SPEED;
		this.dy = Math.sin(heading) * SPEED;
		this.shot = new Ellipse2D.Double(x - SIZE / 2, y - SIZE/2, SIZE, SIZE);
	}
	
	private PhaserPulse(double x, double y, double dx, double dy){
		this.dx = dx;
		this.dy = dy;
		this.shot = new Ellipse2D.Double(x - SIZE / 2, y - SIZE/2, SIZE, SIZE);
	}
	
	
	public void advance(){
		shot.x = shot.x + dx;
		shot.y = shot.y + dy;
	}
	
	public void draw(Graphics2D g){
		g.setColor(Color.RED);
		g.fill(shot);
		g.draw(shot);
	}
	
	public boolean offScreen(){
		if(shot.x < -SIZE)
			return true;
		if(shot.y < -SIZE)
			return true;
		if(shot.x > AsteroidProblem.X_SIZE + SIZE)
			return true;
		if(shot.y > AsteroidProblem.Y_SIZE + SIZE)
			return true;
		else
			return false;
	}

	@Override
	public String toString() {
		return "PhaserPulse [dx=" + dx + ", dy=" + dy + ", shot=" + shot.x + " " + shot.y + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(dx);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(dy);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((shot == null) ? 0 : shot.hashCode());
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
		PhaserPulse other = (PhaserPulse) obj;
		if (Double.doubleToLongBits(dx) != Double.doubleToLongBits(other.dx))
			return false;
		if (Double.doubleToLongBits(dy) != Double.doubleToLongBits(other.dy))
			return false;
		if (shot == null) {
			if (other.shot != null)
				return false;
		} else if (!shot.equals(other.shot))
			return false;
		return true;
	}
	
	
}
