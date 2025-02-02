package org.cwilt.search.domains.asteroids;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Set;

public class ExplodedShip extends Ship{

	protected ExplodedShip(Ship s) {
		super(s.theta, s.x, s.y, s.game);
		if(super.game != null){
			super.game.t.stop();
		}
	}
	public void advance(){
	}
	
	private static final Ellipse2D.Double explosion = new Ellipse2D.Double(-12, -27, 24, 50);

	public void draw(Graphics2D g){
		AffineTransform t = g.getTransform();

		g.translate(x, y);
		g.rotate(theta + Math.PI / 2);
		g.scale(2, 2);
		
		g.setColor(Color.RED);
		g.fill(explosion);

		g.setTransform(t);
		super.draw(g);
	}
	
	public Ship expand(Set<Integer> controls){
		return null;
	}
	private ExplodedShip(ExplodedShip parent){
		super(parent);
	}
	
	@Override
	public Ship clone() {
		return new ExplodedShip(this);
	}
	@Override
	public ArrayList<Set<Integer>> getActions() {
		return new ArrayList<Set<Integer>>();
	}
	@Override
	public boolean isTerminal(){
		return true;
	}
	@Override
	public Ship explode(){
		assert(false);
		throw new RuntimeException("Exploding an already exploded ship");
	}
}
