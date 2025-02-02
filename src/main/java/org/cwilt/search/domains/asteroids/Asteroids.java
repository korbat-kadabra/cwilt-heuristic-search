package org.cwilt.search.domains.asteroids;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.cwilt.search.domains.asteroids.planner.AsteroidState;

public class Asteroids extends JPanel implements ActionListener, MouseListener, KeyListener{
	private AsteroidState current;
	private final AsteroidProblem problem;
	
	
	private void reset(){
		current = problem.getInitial();
		this.time = 0;
		this.repaint();
	}
	
	public Asteroids(AsteroidProblem problem){
		this.setPreferredSize(new Dimension(AsteroidProblem.X_SIZE, AsteroidProblem.Y_SIZE));
		this.setSize(this.getPreferredSize());
		this.setFocusable(true);
		this.t = new Timer(33, this);
		this.problem = problem;
		this.current = problem.getInitial();
		this.addKeyListener(this);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8825084115515264383L;
	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		assert(current.getTime() == time || current.isTerminal());
		time ++;
		if(current.isTerminal()){
			
		} else if(path != null){
			if(path.size() > time)
				current = path.get(time);
			else{
				ArrayList<AsteroidState> children = current.expandRaw();
				current = children.get(0);
			}
		} else {
			current = current.transition(pressed);
		}
		
		assert(current != null);
		this.repaint();
	}
	
	private int time;
	
	private ArrayList<AsteroidState> path;
	public void setPath(ArrayList<AsteroidState> path){
		assert(this.path == null);
		this.path = path;
	}
	
	public static void runGUI(Asteroids a) {
		JFrame frame = new JFrame("Asteroids");
		frame.addMouseListener(a);
		
		frame.setLayout(new GridBagLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1.0;
		c.weighty = 1.0;
		frame.add(a, c);
		JPanel extraWidgets = new JPanel();
		extraWidgets.setLayout(new GridLayout(0, 1));

		JButton quit = new JButton("Quit");
		quit.addActionListener(new org.cwilt.search.utils.basic.ExitHandler());
		extraWidgets.add(quit);

		JButton timer = new JButton("Stop/Go");
		timer.addActionListener(a.new SwitchTimer());
		extraWidgets.add(timer);

		JButton step = new JButton("Step");
		step.addActionListener(a.new Step());
		extraWidgets.add(step);

		JButton reset = new JButton("Reset");
		reset.addActionListener(a.new Reset());
		extraWidgets.add(reset);

		frame.add(extraWidgets);
		frame.pack();
		frame.setVisible(true);
	}

	public final Timer t;
	private class SwitchTimer implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (t.isRunning())
				t.stop();
			else
				t.start();
			Asteroids.this.requestFocus();
		}
	}

	private class Reset implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Asteroids.this.reset();
			Asteroids.this.requestFocus();
		}
	}

	private class Step implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			Asteroids.this.actionPerformed(e);
			Asteroids.this.requestFocus();
		}
	}

	public static void main(String[] args){
		AsteroidProblem problem = new AsteroidProblem(1);
		runGUI(new Asteroids(problem));
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
	
	@Override
	public void paintComponent(Graphics g) {
		Dimension d = getSize();
		Graphics2D g2 = createGraphics2D(d.width, d.height);

		g.setColor(Color.BLACK);
		assert(current != null);
		for(Asteroid a : current.getAsteroids())
			a.draw(g2);
		for(PhaserPulse p : current.getPulses())
			p.draw(g2);
		
		current.getShip().draw(g2);
		g2.dispose();
		g.drawImage(bimg, 0, 0, this);

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		this.requestFocus();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

 	

	protected final Set<Integer> pressed = new HashSet<Integer>();
	
	@Override
	public synchronized void keyPressed(KeyEvent e) {
		pressed.add(e.getKeyCode());
		repaint();
	}

	

	@Override
	public synchronized void keyReleased(KeyEvent e) {
		pressed.remove(e.getKeyCode());
		repaint();
	}




	@Override
	public void keyTyped(KeyEvent arg0) {
		
	}


}