package org.cwilt.search.domains.kiva.map;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.cwilt.search.algs.basic.bestfirst.AStar;
import org.cwilt.search.domains.kiva.SearchTracker;
import org.cwilt.search.domains.kiva.map.OpenPath.OpenPathNode;
import org.cwilt.search.domains.multiagent.problem.MultiagentVertex;
import org.cwilt.search.domains.multiagent.solvers.autoqueue.AutoQueue;
import org.cwilt.search.domains.multiagent.solvers.queue.AgentQueue;
import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchState;import org.cwilt.search.utils.basic.Heapable;import org.cwilt.search.utils.basic.MinHeap;
public class GridCell extends org.cwilt.search.search.SearchState implements MultiagentVertex,
		Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5560037669966617050L;

	private double distToNearestQueue;

	private AutoQueue queue;

	public AutoQueue getAutoQueue() {
		return queue;
	}

	public void setAutoQueue(AutoQueue q) {
		this.queue = q;
	}

	public boolean isBlocked() {
		return this.type == CELL_TYPE.BLOCKED;
	}

	public double nearestPopularVertex() {
		double nearest = Double.MAX_VALUE;

		for (MultiagentVertex v : this.g.pickCells) {
			double d = this.distanceTo(v);

			if (d < nearest)
				nearest = d;
		}

		return nearest;
	}

	private static Font font = new Font("Arial", Font.PLAIN, 7);

	private int calculateID() {
		int xc = this.g.yCellCount();
		int yc = this.g.xCellCount();

		int cid = this.x + xc * this.y;
		assert (cid < xc * yc);
		return cid;
	}

	private final int nodeID;
	private static int windowOffset = 0;
	private static final int windowLimit = 5;

	public static enum CELL_TYPE {
		PICK, QUEUE, BLOCKED, TRAVEL, STORAGE, CHARGER, UNKNOWN
	}

	public String hogString() {
		if (xmlOutput()) {
			return ".";
		} else
			return "@";
	}

	public String xmlString() {
		return "<prx_2D_graph_node x=\"" + x + "\" y=\"" + y + "\"/>";
	}

	public boolean xmlOutput() {
		switch (type) {
		case TRAVEL:
			return true;
		case PICK:
			return true;
		case QUEUE:
			return true;
		case BLOCKED:
			return false;
		case CHARGER:
			return false;
		case UNKNOWN:
			return false;
		case STORAGE:
			return true;
		default:
			assert (false);
			return false;

		}
	}

	public void changeToQueue() {
		this.type = CELL_TYPE.QUEUE;
	}

	public void changeToPick() {
		this.type = CELL_TYPE.PICK;
	}

	public void changeToTravel() {
		this.type = CELL_TYPE.TRAVEL;
	}

	private CELL_TYPE type;
	public final Grid g;
	public static final double CELL_SIZE = 40;
	private final Rectangle2D r;

	/**
	 * x and y location of this grid cell
	 */
	public final int x, y;

	/**
	 * The JDialog for the popup.
	 */
	private JDialog dialog = null;

	/**
	 * The JPanel for the message in the dialog.
	 */
	private JPanel messagePane = null;

	public GridCell(int x, int y, CELL_TYPE type, Grid g) {
		this.g = g;
		this.x = x;
		this.y = y;
		this.r = new Rectangle2D.Double(x * CELL_SIZE, y * CELL_SIZE,
				CELL_SIZE, CELL_SIZE);
		this.type = type;
		this.nodeID = this.calculateID();
		this.distToNearestQueue = Double.MAX_VALUE;
	}

	public GridCell(int x, int y, String s, Grid g) {
		this.g = g;
		this.x = x;
		this.y = y;
		this.r = new Rectangle2D.Double(x * CELL_SIZE, y * CELL_SIZE,
				CELL_SIZE, CELL_SIZE);
		this.type = extractType(s);
		this.nodeID = this.calculateID();
		this.distToNearestQueue = Double.MAX_VALUE;
	}

	public GridCell(int x, int y, char s, Grid g) {
		this.distToNearestQueue = Double.MAX_VALUE;
		this.g = g;
		this.x = x;
		this.y = y;
		this.r = new Rectangle2D.Double(x * CELL_SIZE, y * CELL_SIZE,
				CELL_SIZE, CELL_SIZE);
		this.type = extractTypeChar(s);
		this.nodeID = this.calculateID();
	}

	private String locationString() {
		StringBuffer b = new StringBuffer();
		b.append("(");
		b.append(x);
		b.append(",");
		b.append(y);
		b.append(")");
		return b.toString();
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("(");
		b.append(x);
		b.append(",");
		b.append(y);
		b.append(" ");
		switch (type) {
		case PICK:
			b.append("pick");
			break;
		case BLOCKED:
			b.append("blocked");
			break;
		case QUEUE:
			b.append("queue");
			break;
		case TRAVEL:
			b.append("travel");
			break;
		case STORAGE:
			b.append("storage");
			break;
		default:
			// throw new RuntimeException("strange type");
			b.append("default?");
			break;
		}
		b.append(")");
		return b.toString();
	}

	public double simpleH(GridCell other) {
		return ((Math.abs(x - other.x)) + Math.abs(y - other.y)) / 3;
	}

	public int manhattan(GridCell other) {
		return ((Math.abs(x - other.x)) + Math.abs(y - other.y));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		GridCell other = (GridCell) obj;
		if (x != other.x) {
			return false;
		}
		if (y != other.y) {
			return false;
		}
		return true;
	}

	/**
	 * Determines whether or not the grid cell was clicked.
	 * 
	 * @param p
	 *            The point of the mouse click.
	 * @return True if the grid cell was clicked.
	 */
	public boolean containsClick(Point2D p) {
		return (this.r.contains(p));
	}

	public boolean isStorage() {
		return this.type == CELL_TYPE.STORAGE;
	}

	public boolean isPick() {
		return this.type == CELL_TYPE.PICK;
	}

	public boolean isTravel() {
		return this.type == CELL_TYPE.TRAVEL;
	}

	public boolean canLeft() {
		return this.x != 0;
	}

	public GridCell left() {
		return this.g.grid[x - 1][y];
	}

	public GridCell left(int amt) {
		if (x + amt < 0)
			return null;
		return this.g.grid[x - amt][y];
	}

	public boolean canRight() {
		return this.x < g.grid.length - 1;
	}

	public GridCell right() {
		return this.g.grid[x + 1][y];
	}

	public GridCell right(int amt) {
		if (x + amt >= this.g.grid.length)
			return null;
		return this.g.grid[x + amt][y];
	}

	public boolean canUp() {
		return this.y > 0;
	}

	public GridCell up() {

		return this.g.grid[x][y - 1];
	}

	public GridCell up(int amt) {
		if (y - amt < 0)
			return null;
		return this.g.grid[x][y - amt];
	}

	public boolean canDown() {
		return this.y < g.grid[0].length - 1;
	}

	public GridCell down() {
		return this.g.grid[x][y + 1];
	}

	public GridCell down(int amt) {
		if (y + amt >= g.grid[x].length)
			return null;
		return this.g.grid[x][y + amt];
	}

	public boolean canLeave() {
		switch (this.type) {
		case QUEUE:
			return true;
		case PICK:
			return true;
		case BLOCKED:
			return false;
		case TRAVEL:
			return true;
		case STORAGE:
			return true;
		default:
			return false;
		}
	}

	public boolean canEnter() {
		switch (this.type) {
		case QUEUE:
			return true;
		case PICK:
			return true;
		case BLOCKED:
			return false;
		case TRAVEL:
			return true;
		case STORAGE:
			return false;
		default:
			return false;
		}
	}

	private static final CELL_TYPE extractType(String s) {
		switch (s.charAt(0)) {
		case 'X':
			return CELL_TYPE.BLOCKED;
		case 'Q':
			return CELL_TYPE.QUEUE;
		case 'L':
			return CELL_TYPE.PICK;
		case 'P':
			return CELL_TYPE.PICK;
		case 'U':
			return CELL_TYPE.TRAVEL;
		case 'K':
			return CELL_TYPE.STORAGE;
		case 'T':
			return CELL_TYPE.STORAGE;
		case 'A':
			return CELL_TYPE.TRAVEL;
		case 'S':
			return CELL_TYPE.BLOCKED;
		case 'M':
			return CELL_TYPE.STORAGE;
		case 'C':
			return CELL_TYPE.CHARGER; // chargers are blocked
		case 'F':
			return CELL_TYPE.STORAGE;
		case 'G':
			return CELL_TYPE.STORAGE;
		case 'B':
			return CELL_TYPE.STORAGE;
		default:
			System.err.println(s);
			return CELL_TYPE.BLOCKED;
		}
	}

	private static final CELL_TYPE extractTypeChar(char c) {
		switch (c) {
		case '@':
			return CELL_TYPE.BLOCKED;
		case 'T':
			return CELL_TYPE.BLOCKED;
		case '.':
			return CELL_TYPE.TRAVEL;

		default:
			System.err.println("Error unknown character: " + c);
			return CELL_TYPE.BLOCKED;
		}
	}

	private Color getColor() {
		if (customColor != null)
			return customColor;

		switch (type) {
		case PICK:
			return Color.pink;
		case BLOCKED:
			return Color.black;
		case QUEUE:
			return Color.cyan;
		case TRAVEL: {
			// float alpha = (float) (this.distToNearestQueue /
			// g.getMaxQueueDistance());
			// return new Color(1.0f, 0.0f, 0.0f, alpha);
			return Color.white;
		}
		case STORAGE:
			return Color.GRAY;
		case CHARGER:
			return Color.cyan;
		default:
			return Color.orange;
		}
	}
	
	private boolean validGoal = true;
	public boolean isValidGoal(){
		return validGoal;
	}
	public void setInvalidGoal(){
		this.validGoal = false;
	}

	// private Color getBWColor() {
	// if (customColor != null)
	// return customColor;
	//
	// switch (type) {
	// case PICK:
	// return Color.white;
	// case BLOCKED:
	// return Color.black;
	// case QUEUE:
	// return Color.LIGHT_GRAY;
	// case TRAVEL:
	// return Color.white;
	// case STORAGE:
	// return Color.GRAY;
	// case CHARGER:
	// return Color.cyan;
	// default:
	// return Color.orange;
	// }
	// }

	// private static final boolean BW_DRAW = false;

	private final Point getCenter() {
		return new Point((int) r.getCenterX(), (int) r.getCenterY());
	}

	public void drawArrows(Graphics2D g, double scale) {
		if (scale > 2.0) {
			g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.4f));
			if (this.queue != null) {
				for (GridCell target : this.queue.getRoutes(this)) {
					Shape arrow = ArrowDrawer.createArrowShape(
							this.getCenter(), target.getCenter());
					g.fill(arrow);
				}
			}
		}

	}

	private static final boolean DRAW_COORDINATES = true;
	public void draw(Graphics2D g, double scale) {
		// if (!BW_DRAW) {
		g.setColor(getColor());
		g.fill(r);
		g.setColor(Color.black);
		g.draw(r);
		if (scale > 2.0 && DRAW_COORDINATES) {
			 char[] s = locationString().toCharArray();
			 g.setFont(font);
			 g.drawChars(s, 0, s.length, (int) (x * CELL_SIZE + 1),
			 (int) ((y + 1) * CELL_SIZE - 1));
		}
		// } else {
		// g.setColor(getBWColor());
		//
		// g.fill(r);
		// g.setColor(Color.black);
		//
		// if (this.isPick()) {
		//
		// int baseX = (int) (x * CELL_SIZE);
		// int baseY = (int) (y * CELL_SIZE);
		// int oppX = baseX + (int) CELL_SIZE;
		// int oppY = baseY + (int) CELL_SIZE;
		// g.drawLine(baseX, baseY, oppX, oppY);
		// g.drawLine(baseX, oppY, oppX, baseY);
		// }
		//
		// g.draw(r);
		// }
	}

	/**
	 * Destroys the dialog window for this grid cell.
	 */
	private void destroyDialog() {
		if (this.dialog != null) {
			this.dialog.setVisible(false);
			this.dialog.dispose();
			this.dialog = null;
			this.messagePane = null;
		}
	}

	/**
	 * Returns a message about about the grid cell. To be completed further
	 * later.
	 * 
	 * @return A message about the grid cell.
	 */
	public String getGridCellMessage() {
		StringBuffer b = new StringBuffer();
		b.append(this.toString());
		b.append("\n");
		if (this.getQueue() != null) {
			b.append(this.getQueue().toString());
		} else if (this.getAutoQueue() != null) {
			b.append(this.getAutoQueue().toString());
		} else {
			b.append("no queue");
		}
		return b.toString();
	}

	/**
	 * Toggles the pop-up window for this grid cell.
	 * 
	 * @param frame
	 *            The JFrame containing the main application.
	 */
	public void toggleWindow(JFrame frame) {
		if (this.dialog != null) {
			destroyDialog();
		} else {
			this.messagePane = new JPanel();
			JScrollPane sp = new JScrollPane(new JTextArea(
					this.getGridCellMessage(), 100, 20));

			this.messagePane.add(sp);

			this.dialog = new JDialog(frame, "GridCell " + this.toString());
			this.dialog.add(this.messagePane);
			this.dialog.setBounds(700 + 25 * windowOffset,
					200 + 25 * windowOffset, 300, 200);
			this.dialog.setVisible(true);

			this.dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					destroyDialog();
				}

				@Override
				public void windowClosing(WindowEvent e) {
					destroyDialog();
				}
			});

			windowOffset++;

			if (windowOffset == windowLimit) {
				windowOffset = 0;
			}
		}
	}

	@Override
	public boolean isPopular() {
		return isPick();
	}

	@Override
	public int getID() {
		return this.nodeID;
	}

	private AgentQueue a;

	@Override
	public AgentQueue getQueue() {
		return a;
	}

	@Override
	public void setQueue(AgentQueue a) {
		assert (a != null);
		this.a = a;
	}

	@Override
	public List<MultiagentVertex> getNeighbors() {
		ArrayList<MultiagentVertex> neighbors = new ArrayList<MultiagentVertex>(
				4);
		if (canLeft() && left().canEnter())
			neighbors.add(left());
		if (canRight() && right().canEnter())
			neighbors.add(right());
		if (canUp() && up().canEnter())
			neighbors.add(up());
		if (canDown() && down().canEnter())
			neighbors.add(down());
		return neighbors;
	}

	public boolean isLocalUsageMaximum() {
		for (MultiagentVertex v : getNeighbors()) {
			if (v.getUsage() >= this.getUsage())
				return false;
		}
		return true;
	}

	@Override
	public List<MultiagentVertex> getNeighbors(MultiagentVertex goal) {
		ArrayList<MultiagentVertex> neighbors = new ArrayList<MultiagentVertex>(
				4);
		if (canLeft() && (left().canEnter() || left().equals(goal)))
			neighbors.add(left());
		if (canRight() && (right().canEnter() || right().equals(goal)))
			neighbors.add(right());
		if (canUp() && (up().canEnter() || up().equals(goal)))
			neighbors.add(up());
		if (canDown() && (down().canEnter() || down().equals(goal)))
			neighbors.add(down());
		return neighbors;
	}

	@Override
	public double distanceTo(MultiagentVertex other) {
		if (other.getQueue() != null) {
			Double dist = other.getQueue().distanceToQueue(this);
			if (dist != null)
				return dist;
		}

		if (other.getAutoQueue() != null) {
			Double dist = other.getAutoQueue().distanceToQueue(this);
			if (dist != null)
				return dist;
		}
		GridCell o = (GridCell) other;

		if (o.hStarTable == null) {
			o.initHStarTable();
		}

		HStarValue hsv = o.hStarTable.get(this);
		if (hsv != null)
			return hsv.distHStar;

		return Math.abs(((GridCell) other).x - this.x)
				+ Math.abs(((GridCell) other).y - this.y);
	}

	private static final class Node implements Heapable {
		public final MultiagentVertex v;
		public final double cost;
		public final double congestion;

		public Node(MultiagentVertex v, double cost, double congestion) {
			this.cost = cost;
			this.congestion = congestion;
			// assert(this.congestion < Double.MAX_VALUE);
			this.v = v;
		}

		@Override
		public String toString() {
			return "Node [v=" + v + ", cost=" + cost + ", congestion="
					+ congestion + "]";
		}

		private int heapIndex = Heapable.NO_POS;

		@Override
		public int getHeapIndex() {
			return heapIndex;
		}

		@Override
		public void setHeapIndex(int ix) {
			this.heapIndex = ix;
		}

		private static final class NodeComparator implements Comparator<Node> {

			@Override
			public int compare(Node arg0, Node arg1) {
				if (arg0.cost > arg1.cost)
					return 1;
				else if (arg0.cost > arg1.cost)
					return -1;
				else {
					if (arg0.congestion < arg1.congestion)
						return 1;
					else if (arg0.congestion > arg1.congestion)
						return -1;
					else
						return 0;
				}
			}

		}

	}

	private static int hStarTableCount = 0;

	public static int getHStarTableCount() {
		return hStarTableCount;
	}

	// private void initHStarTable() {
	// // TODO This method is way too slow. Something about calculating the
	// // congestion alongside the shortest path made this insanely slow.
	//
	// hStarTableCount++;
	// this.hStarTable = new HashMap<MultiagentVertex, HStarValue>();
	// Limit l = new Limit();
	// l.startClock();
	// LinkedList<Node> open = new LinkedList<Node>();
	// open.add(new Node(this, 0, this.getCongestion()));
	// while (!open.isEmpty()) {
	// Node n = open.poll();
	// HStarValue inc = hStarTable.get(n.v);
	// if (inc != null) {
	// // check to see how the previous version compares to the new
	// // one.
	// l.incrDup();
	// if (inc.distHStar > n.cost) {
	// throw new RuntimeException("Shouldn't happen");
	// } else if (inc.distHStar < n.cost) {
	// continue;
	// } else if (inc.congestionHStar >= n.congestion) {
	// continue;
	// }
	// l.incrReExp();
	// }
	// l.incrExp();
	// hStarTable.put(n.v, new HStarValue(n.cost, n.congestion));
	// for (MultiagentVertex v : n.v.getNeighbors()) {
	// if (v.isTraversible() && v.getAutoQueue() == null) {
	// l.incrExp();
	// open.add(new Node(v, n.cost + 1, n.congestion
	// + v.getCongestion()));
	// }
	// }
	// }
	// l.endClock();
	// SearchTracker.getTracker().incrHStar(l);
	//
	// // if (this.x == 106 && this.y == 174)
	// // try {
	// // printHStarTable();
	// // } catch (IOException e) {
	// // e.printStackTrace();
	// // }
	//
	// }

	private void initHStarTable() {
		// TODO This method is way too slow. Something about calculating the
		// congestion alongside the shortest path made this insanely slow.

		hStarTableCount++;
		this.hStarTable = new HashMap<MultiagentVertex, HStarValue>();
		Limit l = new Limit();
		l.startClock();
		MinHeap<Node> open = new MinHeap<Node>(new Node.NodeComparator());
		open.add(new Node(this, 0, this.getCongestion()));
		hStarTable.put(this, new HStarValue(0));

		while (!open.isEmpty()) {
			Node n = open.poll();
			HStarValue inc = hStarTable.get(n.v);
			assert (inc != null);

			if (inc != null) {
				// check to see how the previous version compares to the new
				// one.
				if (inc.distHStar > n.cost) {
					throw new RuntimeException("Shouldn't happen");
				} else if (inc.distHStar < n.cost) {
					throw new RuntimeException("Shouldn't happen");
				}
			}
			l.incrExp();
			for (MultiagentVertex v : n.v.getNeighbors()) {
				if (v.isTraversible() && v.getAutoQueue() == null) {
					HStarValue child_inc = hStarTable.get(v);
					if (child_inc == null) {

					} else if (child_inc.distHStar > n.cost) {
						// junk the child
						continue;
					} else if (child_inc.distHStar < n.cost) {
						// junk the child
						continue;
					} else {
						// child is replacing the incumbent
						HStarValue r = hStarTable.remove(v);
						assert (r != null);
					}
					Node childNode = new Node(v, n.cost + 1, n.congestion
							+ v.getCongestion());
					hStarTable.put(v, new HStarValue(childNode.cost));

					l.incrExp();
					open.add(childNode);
				}
			}
		}
		l.endClock();
		SearchTracker.getTracker().incrHStar(l);

		// if (this.x == 106 && this.y == 174)
		// try {
		// printHStarTable();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

	}

	//
	// private void printHStarTable() throws IOException {
	// java.io.FileWriter fstream = new java.io.FileWriter("out.txt");
	// java.io.BufferedWriter out = new java.io.BufferedWriter(fstream);
	// for (Map.Entry<MultiagentVertex, HStarValue> e : hStarTable.entrySet()) {
	// out.write(e.getKey().toString());
	// out.write(" ");
	// out.write(e.getValue().toString());
	// out.write(" ");
	// out.write(Double.toString(e.getKey().getCongestion()));
	// out.write("\n");
	// }
	//
	// out.close();
	// }

	private HashMap<MultiagentVertex, HStarValue> hStarTable = null;

	void clearHStarTable() {
		this.hStarTable = null;
	}

	@Override
	public boolean isForbidden(MultiagentVertex start, MultiagentVertex goal) {
		// can always stay in the start and the goal
		if (this.equals(start))
			return false;
		if (this.equals(goal))
			return false;
		if (this.type == CELL_TYPE.TRAVEL)
			return false;
		if (this.type == CELL_TYPE.STORAGE)
			return true;

		GridCell g = (GridCell) goal;
		// shouldn't be heading towards a queue, only towards pick cells.
		if (g.type == CELL_TYPE.QUEUE)
			assert (false);
		if (g.type == CELL_TYPE.PICK) {
			AgentQueue a = goal.getQueue();
			AutoQueue aq = ((GridCell) goal).getAutoQueue();

			if (a != null) {
				return !a.entranceContains(this);
			} else if (aq != null) {
				return !aq.containsCell(this);
			} else {
				assert (false);
			}
		}
		return true;
	}

	private Color customColor;

	public void setColor(Color color) {
		if (color == null) {
			this.customColor = null;
			return;
		}
		this.customColor = color;
	}

	private transient SimpleSearchProblem problem;

	private static class SimpleSearchProblem implements org.cwilt.search.search.SearchProblem {
		public final GridCell goal;
		public final GridCell start;

		public SimpleSearchProblem(GridCell start, GridCell goal) {
			this.start = start;
			start.problem = this;
			this.goal = goal;
		}

		@Override
		public SearchState getInitial() {
			return start;
		}

		@Override
		public SearchState getGoal() {
			return goal;
		}

		@Override
		public ArrayList<SearchState> getGoals() {
			ArrayList<SearchState> goals = new ArrayList<SearchState>();
			return goals;
		}

		@Override
		public void setCalculateD() {
		}

		@Override
		public void printProblemData(PrintStream ps) {
		}
	}

	public ArrayList<GridCell> openPath(GridCell end) {
		AStar a = new AStar(new OpenPath(this, end), new Limit());
		ArrayList<SearchState> solution = a.solve();
		if (solution == null)
			return null;

		ArrayList<GridCell> path = new ArrayList<GridCell>(solution.size());

		for (SearchState s : solution) {
			path.add(((OpenPathNode) s).loc);
		}

		return path;
	}

	@Override
	public ArrayList<MultiagentVertex> simplePath(MultiagentVertex goal) {
		AStar a = new AStar(new SimpleSearchProblem(this, (GridCell) goal),
				new Limit());
		ArrayList<SearchState> solution = a.solve();

		SearchTracker.getTracker().incrSearchTime2d(a.getLimit());
		if (solution == null) {
			assert (false);
			return null;
		}
		ArrayList<MultiagentVertex> path = new ArrayList<MultiagentVertex>(
				solution.size());
		for (SearchState s : solution) {
			path.add((MultiagentVertex) s);
		}

		return path;
	}

	@Override
	public ArrayList<Child> expand() {
		ArrayList<Child> children = new ArrayList<Child>();

		for (MultiagentVertex v : getNeighbors()) {
			GridCell c = (GridCell) v;

			if (this.isPopular() && this.queue != null) {
				if (this.queue.followsRoute(this, c)) {
					children.add(new Child(c, 1.0));
					c.problem = problem;
				}
			} else {

				if (c.canEnter()) {
					children.add(new Child(c, 1.0));
					c.problem = problem;
				}
			}
		}

		return children;
	}

	@Override
	public ArrayList<Child> reverseExpand() {
		return expand();
	}

	@Override
	public double h() {
		return this.distanceTo(this.problem.goal);
	}

	@Override
	public int d() {
		return (int) h();
	}

	@Override
	public boolean isGoal() {
		return this.equals(this.problem.goal);
	}

	@Override
	public Object getKey() {
		return this;
	}

	@Override
	public int lexOrder(SearchState s) {
		return 0;
	}

	@Override
	public boolean isTraversible() {
		return !isBlocked();
	}

	private double usage;

	public void increaseUsage() {
		usage++;
	}

	public double getUsage() {
		return usage;
	}

	public static final int UP = 0;
	public static final int DOWN = 1;
	public static final int LEFT = 2;
	public static final int RIGHT = 3;

	/**
	 * Used to get how many times this cell was traversed in the specified
	 * direction
	 * 
	 * @param directionID
	 *            ID of the direction to return
	 * @return how many times something went through this cell in the specified
	 *         direction
	 */
	public int getDirectionalCount(int directionID) {
		return usageCounts[directionID];
	}

	/**
	 * Tracks how often this cell is used in each direction
	 */
	private final int[] usageCounts = new int[4];

	/**
	 * Called to indicate that this cell got used, with other as the next
	 * destination
	 * 
	 * @param o
	 *            Next grid cell
	 */
	public final void incrementUsage(MultiagentVertex o) {
		GridCell other = (GridCell) o;
		int xDiff = this.x - other.x;
		int yDiff = this.y - other.y;
		assert (xDiff == 0 || yDiff == 0);
		assert (Math.abs(xDiff) <= 1);
		assert (Math.abs(yDiff) <= 1);
		if (xDiff < 0)
			usageCounts[RIGHT]++;
		if (xDiff > 0)
			usageCounts[LEFT]++;
		if (yDiff < 0)
			usageCounts[DOWN]++;
		if (yDiff > 0)
			usageCounts[UP]++;
	}

	/**
	 * 
	 * @param other
	 *            Other grid cell
	 * @return Whether or not this grid cell is adjacent to the other grid cell
	 */
	public boolean adjacent(GridCell other) {
		int xDiff = Math.abs(this.x - other.x);
		int yDiff = Math.abs(this.y - other.y);

		if (xDiff == 0 && yDiff == 1)
			return true;
		if (yDiff == 0 && xDiff == 1)
			return true;
		else
			return false;
	}

	public double getCongestion() {
		return distToNearestQueue;
	}

	public void setDistToNearestQueue(double distToNearestQueue) {
		this.distToNearestQueue = distToNearestQueue;
	}

	public void changeToStorage() {
		this.type = CELL_TYPE.STORAGE;
	}

	
	private boolean isMultiagentGoal = false;
	@Override
	public boolean isMultiagentGoal() {
		return isMultiagentGoal;
	}
	public void setMultiagentGoal(){
		this.isMultiagentGoal = true;
	}
	
	
//	@Override
//	public double getHCongestion(MultiagentVertex other) {
//		if (other.getQueue() != null) {
//			Double dist = other.getQueue().distanceToQueue(this);
//			if (dist != null)
//				return dist;
//		}
//
//		if (other.getAutoQueue() != null) {
//			Double dist = other.getAutoQueue().distanceToQueueCongestion(this);
//			if (dist != null)
//				return dist;
//		}
//		GridCell o = (GridCell) other;
//
//		if (o.hStarTable == null) {
//		}
//
//		HStarValue hsv = o.hStarTable.get(this);
//		if (hsv != null)
//			return hsv.congestionHStar;
//
//		return Math.abs(((GridCell) other).x - this.x)
//				+ Math.abs(((GridCell) other).y - this.y);
//	}
}
