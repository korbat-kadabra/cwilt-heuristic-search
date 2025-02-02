package org.cwilt.search.domains.kiva.vis;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.MouseMotionListener;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.epsgraphics.ColorMode;
import net.sf.epsgraphics.EpsGraphics;

import org.cwilt.search.domains.kiva.drive.Drive;
import org.cwilt.search.domains.kiva.map.GridCell;
import org.cwilt.search.domains.kiva.problem.KivaProblem;
import org.cwilt.search.domains.multiagent.solvers.queue.QueueOverflow;
public class KivaVisualizer extends JPanel implements ActionListener,
		ChangeListener, MouseListener, MouseWheelListener, MouseMotionListener {
    private static final long serialVersionUID = -4979611731516405903L;
    private final Timer t;
    private final KivaProblem prob;
    private double lastZoom = 1.0, zoom = 1.0;
    private AffineTransform transform = new AffineTransform();
    private Point2D lastClick;
    private JSlider timeSlider;
    private JSpinner speedSpinner;    
    private JFrame frame;

    public KivaVisualizer(KivaProblem prob) {
    	this.prob = prob;
    	t = new Timer(80, this);
        t.start();

        addMouseListener(this);
        addMouseWheelListener(this);
        addMouseMotionListener(this);
    }

    
    public KivaVisualizer(String path) {
        this.prob = new KivaProblem(path, 20, 0, true);
        t = new Timer(80, this);
        t.start();

        addMouseListener(this);
        addMouseWheelListener(this);
        addMouseMotionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        prob.advanceTime();
        this.repaint();
    }

	/**
	 * Sets up the display of the frame, which includes the map display and the
	 * user interface panel.
	 */
    public void display() {
        frame = new JFrame("Multiagent Path Planner");
        frame.setLayout(new GridBagLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 9;
        c.gridheight = 20;
        this.setPreferredSize(prob.getPreferredInitialSize());
        frame.add(this, c);
        
        JPanel extraWidgets = new JPanel();
        extraWidgets.setLayout(new GridBagLayout());
        extraWidgets.setPreferredSize(new Dimension(100, 
        		prob.getPreferredInitialSize().height));

        JLabel timeLabel = new JLabel("Time");
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        extraWidgets.add(timeLabel, c);
                
        int maxTime = prob.getMaxTimeStep() - 1;
        timeSlider = new JSlider(JSlider.VERTICAL, 0, maxTime + 1, 0);
        timeSlider.setPaintTicks(true);
        timeSlider.setPaintLabels(true);
        timeSlider.setMajorTickSpacing(10);
        timeSlider.setMinorTickSpacing(1);
        timeSlider.addChangeListener(this);
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        c.gridheight = 16;
        extraWidgets.add(timeSlider, c);

        JLabel speed = new JLabel("Simulation Speed");
        c.gridx = 0;
        c.gridy = 17;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        extraWidgets.add(speed, c);
        
        SpinnerNumberModel model = new SpinnerNumberModel(80, 1, 2 * 80, 1);
        speedSpinner = new JSpinner(model);
        c.gridx = 0;
        c.gridy = 18;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        extraWidgets.add(speedSpinner, c);
        
        JButton quit = new JButton("Quit");
        quit.addActionListener(new org.cwilt.search.utils.basic.ExitHandler());
        quit.setPreferredSize(new Dimension(100, 25));
        c.gridx = 0;
        c.gridy = 19;
        c.weightx = 0;
        c.weighty = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        extraWidgets.add(quit, c);
        
        c.gridx = 9;
        c.gridy = 0;
        c.weightx = 0.05;
        c.weighty = 0;
        c.gridwidth = 1;
        c.gridheight = 20;
        
        frame.add(extraWidgets, c);
        frame.pack();
        frame.setVisible(true);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setTransform(transform);
        
        
        int xTranslate = (int) (transform.getTranslateX() * -1 / transform.getScaleX());
        int yTranslate = (int) (transform.getTranslateY() * -1 / transform.getScaleY());
        int xSize = (int) (this.getWidth() / transform.getScaleX());
        int ySize = (int) (this.getHeight() / transform.getScaleY());
        
        g2d.clipRect(xTranslate, yTranslate, xSize, ySize);
        
        prob.draw(g2d, zoom);
        
        if (!timeSlider.getValueIsAdjusting()) {
        	timeSlider.setValue(prob.getTimeStep());
        }
        
        t.setDelay(161 - (Integer) speedSpinner.getValue());
    }

	/**
	 * Adds functionality zoom in and zoom out via mouse wheel. The zoom works
	 * such that the point where the mouse is situated does not move as the zoom
	 * changes.
	 * 
	 * A MouseWheelListener method.
	 * 
	 * @param e
	 *            The MouseEvent.
	 */
    public void mouseWheelMoved(MouseWheelEvent e) {
        int mapW = (int) (org.cwilt.search.domains.kiva.map.GridCell.CELL_SIZE * prob.map.xCellCount());
        int mapH = (int) (org.cwilt.search.domains.kiva.map.GridCell.CELL_SIZE * prob.map.yCellCount());

        int frameW = this.getWidth();
        int frameH = this.getHeight();
        
        Point2D p1 = e.getPoint();
        Point2D p2 = null;

        try {
            p2 = transform.inverseTransform(p1, null);
        } catch (NoninvertibleTransformException ex) {
            // should not get here
            ex.printStackTrace();
            return;
        }

        zoom -= (0.1 * e.getWheelRotation());
        zoom = Math.max(0.1, zoom);

        if (zoom < lastZoom) {
            if (frameH > frameW) {
                if (mapW * zoom < 0.25 * frameW) {
                    zoom = lastZoom;
                    return;
                }
            } else {
                if (mapH * zoom < 0.25 * frameH) {
                    zoom = lastZoom;
                    return;
                }
            }
        }

        transform.setToIdentity();
        transform.translate(p1.getX(), p1.getY());
        transform.scale(zoom, zoom);
        transform.translate(-p2.getX(), -p2.getY());
                
        lastZoom = zoom;
        
        repaint();
    }

	/**
	 * Adds drag and pan functionality to the map display.
	 * 
	 * A MouseMotionListener method.
	 * 
	 * @param e
	 *            The MouseEvent.
	 */
    public void mouseDragged(MouseEvent e) {
        Point2D p1 = e.getPoint();
        Point2D p2 = null;

        try {
            p2 = transform.inverseTransform(p1, null);
        } catch (NoninvertibleTransformException ex) {
            // should not get here
            ex.printStackTrace();
            return;
        }

        double diffX = p1.getX() - lastClick.getX();
        double diffY = p1.getY() - lastClick.getY();
        
        transform.setToIdentity();
        transform.translate(p1.getX(), p1.getY());
        transform.translate(diffX, diffY);        
        transform.scale(zoom, zoom);
        transform.translate(-p2.getX(), -p2.getY());

        lastClick = p1;

        repaint();
    }
    
    /**
     * Left blank so that nothing is done on movement of the mouse.
	 * 
	 * A MouseMotionListener method.
	 * 
	 * @param e
	 *            The MouseEvent.
	 */
    public void mouseMoved(MouseEvent e) {
    }

	/**
	 * Tests mouse clicks to see if they were on a drive or a grid cell. A click
	 * on a drive toggles the appearance of the drive's path and information
	 * dialog. For now, a click on a grid cell prints out the cell's information
	 * to System.err.
	 * 
	 * A MouseListener method.
	 * 
	 * @param e
	 *            The MouseEvent.
	 */
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            Point2D untransformedPoint = new Point2D.Double();

            try {
                transform.inverseTransform(e.getPoint(), untransformedPoint);
            } catch (NoninvertibleTransformException ex) {
                // should not get here
                ex.printStackTrace();
                return;
            }

            boolean clickedDrive = false;

            for (Drive d : prob.drives) {
                if (d.containsClick(untransformedPoint)) {
                    clickedDrive = true;

                    d.toggleWindowAndPath(frame);

                    break;
                }
            }

            if (!clickedDrive) {
                for (GridCell[] row : prob.map.grid) {
                    for (GridCell cell : row) {
                        if (cell.containsClick(untransformedPoint)) {
                            //System.err.println(cell + " clicked");
                        	cell.toggleWindow(frame);
                        }
                    }
                }
            }

            repaint();
        }
    }

	/**
	 * Keeps track of the last place the mouse was clicked, for use with the
	 * panning implemented in mouseDragged.
	 * 
	 * A MouseListener method.
     * 
	 * @param e
	 *            The MouseEvent.
	 */
    public void mousePressed(MouseEvent e) {
        lastClick = e.getPoint();
    }

    /**
     * An unimplemented event from MouseListener.
     * 
	 * @param e
	 *            The MouseEvent.
     */
    public void mouseReleased(MouseEvent e) {
    }
    
    /**
     * An unimplemented event from MouseListener.
     * 
	 * @param e
	 *            The MouseEvent.
     */
    public void mouseEntered(MouseEvent e) {
    }
    
    /**
     * An unimplemented event from MouseListener.
     * 
	 * @param e
	 *            The MouseEvent.
     */
    public void mouseExited(MouseEvent e) {
    }

	/**
	 * Sets the current time step according to the value of the time slider.
	 * 
	 * A ChangeListener method.
	 * 
	 * @param e
	 *            The ChangeEvent.
	 */
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		
	    prob.setTimeStep(source.getValue());
	}

    public static void main(String[] args) throws QueueOverflow {
        KivaVisualizer kv = null;

        if (args.length == 0) {
            kv = new KivaVisualizer("kivadata/layout-gap-occ-v2.txt");
        } else {
            kv = new KivaVisualizer(args[0]);
        }
        
        kv.prob.solve();
        kv.display();
    }
    
	public void exportEPS(String filename) {
		Dimension size = getPreferredSize();
		OutputStream out = null;
		try {
			out = new FileOutputStream(filename);
			EpsGraphics g2 = new EpsGraphics("timing eps", out, 0, 0,
					size.width, size.height, ColorMode.COLOR_RGB);
			g2.setPaint(Color.WHITE);
			g2.setColor(Color.BLACK);
			double scale = 0.1;
			g2.scale(scale, scale);
			prob.map.draw(g2, scale);
//			paintComponent(g2);
			g2.close();
		} catch (Exception e) {
			System.out.println(e);
		}finally {

        	try {
        		if(out != null) {
    				out.close();
        		}
			} catch (IOException e) {
				e.printStackTrace();
			}

        }

	}

}
