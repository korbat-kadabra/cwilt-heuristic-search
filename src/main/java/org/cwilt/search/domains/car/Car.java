package org.cwilt.search.domains.car;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

public class Car implements Cloneable {
	private static final class SnappedCar {
		public final int xLoc;
		public final int yLoc;
		public final int wheelHeading;
		public final int heading;
		public final int velocity;

		public SnappedCar(Car c) {
			GridCell here = c.l.getGridCell(c);

			if (here == null) {
				this.xLoc = -1;
				this.yLoc = -1;
				this.wheelHeading = -1;
				this.heading = -1;
				this.velocity = -1;
			} else {
				this.xLoc = here.x;
				this.yLoc = here.y;
				this.wheelHeading = (int) (c.wheelHeading / c.l.deltaH);
				this.heading = (int) (c.heading / c.l.deltaH);
				this.velocity = (int) (c.velocity / c.l.deltaS);
			}
			
		}
		public String toString(){
			StringBuffer b = new StringBuffer();
			b.append("(");
			b.append(xLoc);
			b.append(",");
			b.append(yLoc);
			b.append(") heading ");
			b.append(heading);
			b.append(" wheel heading ");
			b.append(wheelHeading);
			b.append(" velocity ");
			b.append(velocity);
			return b.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + heading;
			result = prime * result + velocity;
			result = prime * result + wheelHeading;
			result = prime * result + xLoc;
			result = prime * result + yLoc;
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
			SnappedCar other = (SnappedCar) obj;
			if (heading != other.heading)
				return false;
			if (velocity != other.velocity)
				return false;
			if (wheelHeading != other.wheelHeading)
				return false;
			if (xLoc != other.xLoc)
				return false;
			if (yLoc != other.yLoc)
				return false;
			return true;
		}

	}

	private final double maxVelocity;
	private final double maxTurn;
	private final double length;
	private double xLoc;
	private double yLoc;

	private final Lot l;

	private double heading;
	private double wheelHeading;
	private double velocity;
	private Rectangle2D carBase;
	private Shape carLocation;

	public void setHeading(double h) {
		this.heading = h;
	}

	private void moveCar(double deltaT) {
		if (wheelHeading > 0.0001) {
			double fromCenter = heading + Math.PI / 2;
			double radius = length / Math.tan(wheelHeading);
			double xCenter = xLoc + Math.sin(heading) * radius;
			double yCenter = yLoc + Math.cos(heading) * radius;
			double dist = velocity / deltaT;
			double radiansTravelled = dist / (radius);
			double newHeading = heading - radiansTravelled;
			xLoc = xCenter + Math.cos(fromCenter - radiansTravelled) * radius;
			yLoc = yCenter - Math.sin(fromCenter - radiansTravelled) * radius;
			heading = newHeading;
		} else if (wheelHeading < -0.0001) {
			double fromCenter = heading - Math.PI / 2;
			double radius = length / Math.tan(wheelHeading) * -1;
			double xCenter = xLoc - Math.sin(heading) * radius;
			double yCenter = yLoc - Math.cos(heading) * radius;
			double dist = velocity / deltaT;
			double radiansTravelled = dist / (radius);
			double newHeading = heading + radiansTravelled;
			xLoc = xCenter + Math.cos(fromCenter + radiansTravelled) * radius;
			yLoc = yCenter - Math.sin(fromCenter + radiansTravelled) * radius;
			heading = newHeading;
		} else {
			double deltaX;
			double deltaY;
			double dist = velocity / deltaT;

			deltaX = dist * Math.cos(heading);
			deltaY = dist * -Math.sin(heading);
			xLoc = xLoc + deltaX;
			yLoc = yLoc + deltaY;
		}
	}

	private final static double DELTA_T = 1D;

	/**
	 * Gives the car its next action, and makes it update
	 * 
	 * @param steer
	 *            how much to steer
	 * @param accel
	 *            how much to accelerate
	 */
	public void updateCar(double steer, double accel) {

		moveCar(DELTA_T);

		// update the velocity and the heading
		double newVelocity = this.velocity + accel;
		if (newVelocity > maxVelocity)
			this.velocity = maxVelocity;
		else if (newVelocity + accel < maxVelocity * -.5)
			this.velocity = maxVelocity * -.5;
		else
			velocity = newVelocity;

		double newHeading = wheelHeading + steer;
		if (newHeading > maxTurn)
			wheelHeading = maxTurn;
		else if (newHeading < maxTurn * -1)
			wheelHeading = maxTurn * -1;
		else
			wheelHeading = newHeading;

	}

	public Car(double xLoc, double yLoc, double heading, double maxVelocity,
			double maxTurn, double length, Lot l) {
		this.l = l;
		this.xLoc = xLoc;
		this.maxTurn = maxTurn;
		this.yLoc = yLoc;
		this.heading = heading;
		this.velocity = 0;
		this.maxVelocity = maxVelocity;
		this.length = length;

		double offset = length * .33 / 2;

		carBase = new Rectangle2D.Double(0, -offset, length, length * .33);

		AffineTransform rat = AffineTransform.getTranslateInstance(xLoc, yLoc);
		rat.rotate(heading);
		carLocation = rat.createTransformedShape(carBase);
	}

	public boolean intersects(Shape s) {
		Area other = new Area(s);
		Area here = new Area(carLocation);
		other.intersect(here);
		return !other.isEmpty();
	}

	public boolean intersects(Car other) {
		return intersects(other.carLocation);
	}

	public void draw(Graphics2D g) {
		Color oldColor = g.getColor();
		AffineTransform oldTransform = g.getTransform();

		double offset = length * .33 / 2;

		Rectangle2D r = new Rectangle2D.Double(0, -offset, length, length * .33);
		Rectangle2D hood = new Rectangle2D.Double(length * .67, -offset,
				length * .33, length * .33);

		AffineTransform rat = AffineTransform.getTranslateInstance(xLoc, yLoc);
		rat.rotate(-heading);

		carLocation = rat.createTransformedShape(carBase);
		g.transform(rat);

		g.setColor(Color.BLACK);
		g.draw(r);
		g.setColor(Color.RED);
		g.fill(hood);

		g.setTransform(oldTransform);
		g.setColor(oldColor);
	}

	@Override
	public int hashCode() {
		SnappedCar s = new SnappedCar(this);
		return s.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Car other = (Car) obj;
		SnappedCar tsc = new SnappedCar(this);
		SnappedCar osc = new SnappedCar(other);
		return tsc.equals(osc);
	}

	public Object getKey() {
		return new SnappedCar(this);
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		/*
		b.append("xLoc: ");
		b.append(xLoc);
		b.append(" yLoc: ");
		b.append(yLoc);
		b.append(" heading: ");
		b.append(heading);
		b.append(" pi_wheel_heading: ");
		b.append(wheelHeading);
		b.append(" velocity: ");
		b.append(velocity);

		b.append(" max_velocity: ");
		b.append(maxVelocity);
		b.append(" max_turn: ");
		b.append(maxTurn);
		b.append(" length ");
		b.append(length);
		
		b.append(" ");
		*/
		b.append(new SnappedCar(this).toString());
		
		return b.toString();
	}

	public Car(String s, Lot l) {
		this.l = l;
		String[] a = s.split("\\w+");
		this.xLoc = Double.parseDouble(a[1]);
		this.yLoc = Double.parseDouble(a[3]);
		this.heading = Double.parseDouble(a[5]);
		this.wheelHeading = Double.parseDouble(a[7]);
		this.velocity = Double.parseDouble(a[9]);
		this.maxVelocity = Double.parseDouble(a[11]);
		this.maxTurn = Double.parseDouble(a[13]);
		this.length = Double.parseDouble(a[15]);

		AffineTransform rat = AffineTransform.getTranslateInstance(xLoc, yLoc);
		rat.rotate(heading);
		this.carLocation = rat.createTransformedShape(this.carBase);
	}

	private Car(Car c) {
		this.l = c.l;
		this.xLoc = c.xLoc;
		this.yLoc = c.yLoc;
		this.heading = c.heading;
		this.wheelHeading = c.wheelHeading;
		this.maxVelocity = c.maxVelocity;
		this.maxTurn = c.maxTurn;
		this.velocity = c.velocity;
		this.length = c.length;
		this.carBase = (Rectangle2D) c.carBase.clone();
		AffineTransform rat = AffineTransform.getTranslateInstance(xLoc, yLoc);
		rat.rotate(heading);
		double offset = length * .33 / 2;

		carBase = new Rectangle2D.Double(0, -offset, length, length * .33);
		this.carLocation = rat.createTransformedShape(this.carBase);

	}

	public Car clone() {
		return new Car(this);
	}

	public double getxLoc() {
		return xLoc;
	}

	public double getyLoc() {
		return yLoc;
	}

	public double getMaxVelocity() {
		return maxVelocity;
	}

	public double getHeading() {
		return heading;
	}

	public double getVelocity() {
		return velocity;
	}
}
