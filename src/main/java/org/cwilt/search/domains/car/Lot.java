package org.cwilt.search.domains.car;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.cwilt.search.algs.basic.bestfirst.AStar;
import org.cwilt.search.search.Limit;
import org.cwilt.search.search.SearchAlgorithm;
import org.cwilt.search.search.SearchProblem;
import org.cwilt.search.search.SearchState;
public class Lot extends JPanel implements ActionListener, SearchProblem {

	public final int xSize;
	public final int ySize;
	public final double discr;
	public final double headingSlop;

	private int timeStep;

	public final double deltaH;
	public final double deltaS;

	private final Car destination;
	private final ArrayList<Shape> obstacles;

	private final GridCell world[][];

	public static final Shape readShape(String s) throws ParseException {
		String[] a = s.split("\\w+");
		if (a[0].charAt(0) == 'R') {
			return new Rectangle2D.Double(Double.parseDouble(a[1]), Double
					.parseDouble(a[2]), Double.parseDouble(a[3]), Double
					.parseDouble(a[4]));
		} else
			throw new ParseException(s, 0);
	}

	private static final String printShape(Shape s) {
		StringBuffer b = new StringBuffer();
		if (s instanceof Rectangle2D.Double) {
			Rectangle2D.Double d = (Rectangle2D.Double) s;
			b.append("R ");
			b.append(d.x);
			b.append(" ");
			b.append(d.y);
			b.append(" ");
			b.append(d.height);
			b.append(" ");
			b.append(d.width);
		}

		return s.toString();
	}

	public String toString() {
		StringBuffer b = new StringBuffer();

		b.append(xSize);
		b.append(" ");
		b.append(ySize);
		b.append("\n");
		b.append(c);
		b.append("\n");
		b.append(destination);
		b.append("\n");

		for (Shape s : obstacles) {
			b.append(printShape(s));
			b.append("\n");
		}

		return b.toString();
	}

	public Lot(String path, double discr, double headingSlop)
			throws FileNotFoundException, ParseException, IOException {
		this.headingSlop = headingSlop;
		this.discr = discr;
		this.obstacles = new ArrayList<Shape>();
		FileInputStream fs = new FileInputStream(path);
		DataInputStream ds = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(ds));
		Scanner s = new Scanner(br);

		this.xSize = s.nextInt();
		this.ySize = s.nextInt();

		this.deltaH = s.nextDouble();
		this.deltaS = s.nextDouble();

		Dimension preferredSize = new Dimension(xSize, ySize);
		this.setPreferredSize(preferredSize);

		c = new Car(s.nextLine(), this);
		destination = new Car(s.nextLine(), this);

		while (s.hasNext()) {
			obstacles.add(readShape(s.nextLine()));
		}

		this.world = new GridCell[(int) (xSize / discr)][(int) (ySize / discr)];
		initWorld(discr);
		br.close();
		s.close();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 557279934753765106L;
	Car c;
	Timer t;

	private class SwitchTimer implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (t.isRunning())
				t.stop();
			else
				t.start();
		}
	}

	private class Step implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Lot.this.actionPerformed(e);
		}
	}

	private void setTimer(Timer t) {
		this.t = t;
	}

	private void initWorld(double discr) {
		int xCell = (int) (destination.getxLoc() / discr);
		int yCell = (int) (destination.getyLoc() / discr);
		GridCell.initWorld(discr, world, xCell, yCell, obstacles);
	}

	public static void lotTest(Lot l) {

		SearchAlgorithm a = new AStar(l, new Limit());

		l.controls = a.solve();
		a.printSearchData(System.out);

		for (SearchState n : l.controls) {
			System.out.println(n);
		}

		Timer t = new Timer(100, l);
		l.setTimer(t);
		// t.start();

		JFrame frame = new JFrame("Parking Visualization");
		frame.setLayout(new GridBagLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		frame.add(l, c);
		JPanel extraWidgets = new JPanel();
		extraWidgets.setLayout(new GridLayout(0, 1));
		JButton quit = new JButton("Quit");
		quit.addActionListener(new org.cwilt.search.utils.basic.ExitHandler());
		extraWidgets.add(quit);

		JButton timer = new JButton("Stop/Go");
		timer.addActionListener(l.new SwitchTimer());
		extraWidgets.add(timer);

		JButton step = new JButton("Step");
		step.addActionListener(l.new Step());
		extraWidgets.add(step);

		frame.add(extraWidgets);
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void paintComponent(Graphics g) {
		Dimension d = getSize();
		Graphics2D g2 = createGraphics2D(d.width, d.height);

		g.setColor(Color.BLACK);

		if (c != null)
			c.draw((Graphics2D) g2);
		g2.dispose();
		g.drawImage(bimg, 0, 0, this);

	}

	private BufferedImage bimg;

	private Graphics2D createGraphics2D(int w, int h) {
		Graphics2D g2 = null;
		if (bimg == null || bimg.getWidth() != w || bimg.getHeight() != h) {
			bimg = (BufferedImage) createImage(w, h);
		}
		g2 = bimg.createGraphics();
		g2.clearRect(0, 0, w, h);
		return g2;
	}

	public double calculateH(Car c) {
		int xCell = (int) (c.getxLoc() / discr);
		int yCell = (int) (c.getyLoc() / discr);
		if (xCell < 0)
			return Double.MAX_VALUE;
		if (yCell < 0)
			return Double.MAX_VALUE;
		if (xCell >= world.length)
			return Double.MAX_VALUE;
		if (yCell >= world[0].length)
			return Double.MAX_VALUE;

		return world[xCell][yCell].distance / c.getMaxVelocity();
	}


	public int calculateD(Car c) {
		int xCell = (int) (c.getxLoc() / discr);
		int yCell = (int) (c.getyLoc() / discr);
		if (xCell < 0)
			return Integer.MAX_VALUE;
		if (yCell < 0)
			return Integer.MAX_VALUE;
		if (xCell >= world.length)
			return Integer.MAX_VALUE;
		if (yCell >= world[0].length)
			return Integer.MAX_VALUE;

		return world[xCell][yCell].d;
	}

	
	public GridCell getGridCell(Car c) {
		int xCell = (int) (c.getxLoc() / discr);
		int yCell = (int) (c.getyLoc() / discr);
		if (xCell < 0)
			return null;
		if (yCell < 0)
			return null;
		if (xCell >= world.length)
			return null;
		if (yCell >= world[0].length)
			return null;
		return world[xCell][yCell];
	}

	public boolean isGoal(Car c) {
		int xCell = (int) (c.getxLoc() / discr);
		int yCell = (int) (c.getyLoc() / discr);
		if (world[xCell][yCell].distance != 0)
			return false;
		else {
			double diff = c.getHeading() - destination.getHeading();
			while (Math.abs(diff) > Math.PI * 2) {
				if (diff > 0)
					diff -= Math.PI * 2;
				else
					diff += Math.PI * 2;
			}
			if (Math.abs(diff) < headingSlop) {
				if (Math.abs(c.getVelocity() - destination.getVelocity()) < 0.1)
					return true;
				else
					return false;
			} else
				return false;
		}
	}

	public Lot() {
		this.discr = 5.0;
		this.headingSlop = Math.PI / 8;
		this.deltaH = Math.PI / 16;
		this.deltaS = 1.0;
		this.xSize = 500;
		this.ySize = 500;
		Dimension preferredSize = new Dimension(xSize, ySize);
		this.setPreferredSize(preferredSize);
		this.c = new Car(100, 250, 0, 20, Math.PI / 6, 50, this);
		this.obstacles = new ArrayList<Shape>();
		this.destination = new Car(200, 250, 0, 10, Math.PI / 6, 50, this);
		this.world = new GridCell[(int) (xSize / discr)][(int) (ySize / discr)];
		initWorld(discr);
	}

	public static void main(String[] args) {
		lotTest(new Lot());
	}

	private List<SearchState> controls;

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (controls != null) {
			if (timeStep >= controls.size())
				return;
			CarState s = (CarState) controls.get(timeStep);
			CarState.ACTION a = s.getAction();

			System.err.println(a);

			switch (a) {
			case LEFT_GO:
				c.updateCar(-deltaH, deltaS);
				break;
			case RIGHT_GO:
				c.updateCar(deltaH, deltaS);
				break;
			case STRAIGHT_GO:
				c.updateCar(0, deltaS);
				break;
			case LEFT_SLOW:
				c.updateCar(-deltaH, -deltaS);
				break;
			case RIGHT_SLOW:
				c.updateCar(deltaH, -deltaS);
				break;
			case STRAIGHT_SLOW:
				c.updateCar(0, -deltaS);
				break;
			case LEFT_NO:
				c.updateCar(-deltaH, 0);
				break;
			case RIGHT_NO:
				c.updateCar(deltaH, 0);
				break;
			case STRAIGHT_NO:
				c.updateCar(0, 0);
				break;
			}

			this.repaint();

			timeStep++;
		}
	}

	@Override
	public SearchState getGoal() {
		return new CarState(this, this.destination);
	}

	@Override
	public ArrayList<SearchState> getGoals() {
		ArrayList<SearchState> toReturn = new ArrayList<SearchState>();
		toReturn.add(getGoal());
		return toReturn;
	}

	@Override
	public SearchState getInitial() {
		return new CarState(this, this.c);
	}

	@Override
	public void setCalculateD() {
	}
	public void printProblemData(PrintStream p){
		
	}

}
