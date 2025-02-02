package org.cwilt.search.domains.car;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

public class GridCell implements Comparable<GridCell>, org.cwilt.search.utils.basic.Heapable {

	public boolean blocked() {
		return this.r != null;
	}

	public ArrayList<GridCell> cardinal(GridCell[][] world) {
		GridCell next = this;
		ArrayList<GridCell> g = new ArrayList<GridCell>(4);

		boolean posLeft = next.x != 0;
		boolean posRight = next.x < world.length - 1;
		boolean posUp = next.y != 0;
		boolean posDown = next.y < world[0].length - 1;

		boolean openLeft = posLeft && world[next.x - 1][next.y].r != null;

		boolean openRight = posRight && world[next.x + 1][next.y].r != null;
		boolean openUp = posUp && world[next.x][next.y - 1].r != null;
		boolean openDown = posDown && world[next.x][next.y + 1].r != null;

		if (openLeft) {
			g.add(world[next.x - 1][next.y]);
		}

		if (openRight) {
			g.add(world[next.x + 1][next.y]);
		}

		if (openUp) {
			g.add(world[next.x][next.y - 1]);
		}
		if (openDown) {
			g.add(world[next.x][next.y + 1]);
		}
		return g;
	}

	public ArrayList<GridCell> diagonal(GridCell[][] world) {
		GridCell next = this;
		ArrayList<GridCell> g = new ArrayList<GridCell>(4);
		boolean posLeft = next.x != 0;
		boolean posRight = next.x < world.length - 1;
		boolean posUp = next.y != 0;
		boolean posDown = next.y < world[0].length - 1;

		if (posUp && posLeft && world[next.x - 1][next.y - 1].r != null) {
			g.add(world[next.x - 1][next.y - 1]);
		}
		if (posUp && posRight && world[next.x + 1][next.y - 1].r != null) {
			g.add(world[next.x + 1][next.y - 1]);
		}
		if (posDown && posLeft && world[next.x - 1][next.y + 1].r != null) {
			g.add(world[next.x - 1][next.y + 1]);
		}
		if (posDown && posRight && world[next.x + 1][next.y + 1].r != null) {
			g.add(world[next.x + 1][next.y + 1]);
		}
		return g;
	}

	public Rectangle2D r;

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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GridCell other = (GridCell) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	public double distance;
	public int d;
	public final int x;
	public final int y;

	public String toString() {
		return "x: " + x + " y: " + y + " distance: " + distance;
	}

	public GridCell(double xLoc, double yLoc, double size, int x, int y,
			boolean blocked) {
		this.x = x;
		this.y = y;
		this.distance = Double.MAX_VALUE;
		Rectangle2D cell = new Rectangle2D.Double(xLoc, yLoc, size, size);
		if (!blocked) {
			this.r = cell;
		} else {
			this.r = null;
		}
		this.heapIndex = org.cwilt.search.utils.basic.Heapable.NO_POS;
	}

	public GridCell(double xLoc, double yLoc, double size, int x, int y,
			ArrayList<Shape> obstacles) {
		this.heapIndex = org.cwilt.search.utils.basic.Heapable.NO_POS;
		boolean clear = true;
		this.x = x;
		this.y = y;
		this.distance = Double.MAX_VALUE;
		Rectangle2D cell = new Rectangle2D.Double(xLoc, yLoc, size, size);
		for (Shape s : obstacles) {
			if (s.intersects(cell)) {
				clear = false;
				break;
			}
		}
		if (clear)
			this.r = cell;
		else 
			this.r = null;
		this.distance = Double.MAX_VALUE;
	}

	@Override
	public int compareTo(GridCell arg0) {
		if (this.distance < arg0.distance)
			return -1;
		else if (this.distance > arg0.distance)
			return 1;
		else
			return 0;
	}

	/**
	 * Initializes the world
	 * 
	 * @param discr
	 *            discretization to use
	 * @param world 
	 * @param xLoc 
	 * @param yLoc 
	 * @param obstacles 
	 */
	public static final void initWorld(double discr, GridCell[][] world,
			int xLoc, int yLoc, ArrayList<Shape> obstacles) {
		if (obstacles != null) {
			for (int i = 0; i < world.length; i++) {
				for (int j = 0; j < world[0].length; j++) {
					world[i][j] = new GridCell(i * discr, j * discr, discr, i,
							j, obstacles);
				}
			}
		}
		int xCell = xLoc;
		int yCell = yLoc;
		world[xCell][yCell].distance = 0;
		org.cwilt.search.utils.basic.MinHeap<GridCell> g = new org.cwilt.search.utils.basic.MinHeap<GridCell>(
				new GridCellComparator());
		HashSet<GridCell> closed = new HashSet<GridCell>();

		g.insert(world[xCell][yCell]);
		while (!g.isEmpty()) {
			GridCell next = g.poll();

			closed.add(next);

			boolean posLeft = next.x != 0
					&& world[next.x - 1][next.y].distance == Double.MAX_VALUE;
			boolean openLeft = posLeft && world[next.x - 1][next.y].r != null;
			if (openLeft
					&& !closed.contains(world[next.x - 1][next.y])
					&& world[next.x - 1][next.y].heapIndex == org.cwilt.search.utils.basic.Heapable.NO_POS) {
				world[next.x - 1][next.y].distance = next.distance + discr;
				world[next.x - 1][next.y].d = next.d + 1;

				g.insert(world[next.x - 1][next.y]);
			}
			boolean posRight = next.x < world.length - 1
					&& world[next.x + 1][next.y].distance == Double.MAX_VALUE;
			boolean openRight = posRight && world[next.x + 1][next.y].r != null;
			if (openRight
					&& !closed.contains(world[next.x + 1][next.y])
					&& world[next.x + 1][next.y].heapIndex == org.cwilt.search.utils.basic.Heapable.NO_POS) {
				world[next.x + 1][next.y].distance = next.distance + discr;
				world[next.x + 1][next.y].d = next.d + 1;
				g.insert(world[next.x + 1][next.y]);
			}
			boolean posUp = next.y != 0
					&& world[next.x][next.y - 1].distance == Double.MAX_VALUE;
			boolean openUp = posUp && world[next.x][next.y - 1].r != null;
			if (openUp
					&& !closed.contains(world[next.x][next.y - 1])
					&& world[next.x][next.y - 1].heapIndex == org.cwilt.search.utils.basic.Heapable.NO_POS) {
				world[next.x][next.y - 1].distance = next.distance + discr;
				world[next.x][next.y - 1].d = next.d + 1;
				g.insert(world[next.x][next.y - 1]);
			}
			boolean posDown = next.y < world[0].length - 1
					&& world[next.x][next.y + 1].distance == Double.MAX_VALUE;
			boolean openDown = posDown && world[next.x][next.y + 1].r != null;
			if (openDown
					&& !closed.contains(world[next.x][next.y + 1])
					&& world[next.x][next.y + 1].heapIndex == org.cwilt.search.utils.basic.Heapable.NO_POS) {
				world[next.x][next.y + 1].distance = next.distance + discr;
				world[next.x][next.y + 1].d = next.d + 1;
				g.insert(world[next.x][next.y + 1]);
			}

			if (posUp
					&& posLeft
					&& world[next.x - 1][next.y - 1].r != null
					&& !closed.contains(world[next.x - 1][next.y - 1])
					&& world[next.x - 1][next.y - 1].heapIndex == org.cwilt.search.utils.basic.Heapable.NO_POS) {
				world[next.x - 1][next.y - 1].distance = next.distance + discr
						* rt2;
				world[next.x - 1][next.y - 1].d = next.d + 1;
				g.insert(world[next.x - 1][next.y - 1]);
			}
			if (posUp
					&& posRight
					&& world[next.x + 1][next.y - 1].r != null
					&& !closed.contains(world[next.x + 1][next.y - 1])
					&& world[next.x + 1][next.y - 1].heapIndex == org.cwilt.search.utils.basic.Heapable.NO_POS) {
				world[next.x + 1][next.y - 1].distance = next.distance + discr
						* rt2;
				world[next.x + 1][next.y - 1].d = next.d + 1;
				g.insert(world[next.x + 1][next.y - 1]);
			}
			if (posDown
					&& posLeft
					&& world[next.x - 1][next.y + 1].r != null
					&& !closed.contains(world[next.x - 1][next.y + 1])
					&& world[next.x - 1][next.y + 1].heapIndex == org.cwilt.search.utils.basic.Heapable.NO_POS) {
				world[next.x - 1][next.y + 1].distance = next.distance + discr
						* rt2;
				world[next.x - 1][next.y + 1].d = next.d + 1;
				g.insert(world[next.x - 1][next.y + 1]);
			}
			if (posDown
					&& posRight
					&& world[next.x + 1][next.y + 1].r != null
					&& !closed.contains(world[next.x + 1][next.y + 1])
					&& world[next.x + 1][next.y + 1].heapIndex == org.cwilt.search.utils.basic.Heapable.NO_POS) {
				world[next.x + 1][next.y + 1].distance = next.distance + discr
						* rt2;
				world[next.x + 1][next.y + 1].d = next.d + 1;
				g.insert(world[next.x + 1][next.y + 1]);
			}

		}
	}

	public static class GridCellComparator implements Comparator<GridCell> {

		@Override
		public int compare(GridCell arg0, GridCell arg1) {
			if (arg0.equals(arg1))
				return 0;
			if (arg0.distance < arg1.distance)
				return -1;
			if (arg0.distance > arg1.distance)
				return 1;
			return 0;
		}

	}

	/**
	 * Initializes the world
	 * 
	 * @param discr
	 *            discretization to use
	 * @param world 
	 * @param xLoc 
	 * @param yLoc 
	 * @param obstacles 
	 */
	public static final void initWorldSimple(double discr, GridCell[][] world,
			int xLoc, int yLoc, ArrayList<Shape> obstacles) {
		if (obstacles != null) {
			for (int i = 0; i < world.length; i++) {
				for (int j = 0; j < world[0].length; j++) {
					world[i][j] = new GridCell(i * discr, j * discr, discr, i,
							j, obstacles);
				}
			}
		}
		int xCell = xLoc;
		int yCell = yLoc;
		world[xCell][yCell].distance = 0;
		org.cwilt.search.utils.basic.MinHeap<GridCell> g = new org.cwilt.search.utils.basic.MinHeap<GridCell>(
				new GridCellComparator());
		g.insert(world[xCell][yCell]);
		while (!g.isEmpty()) {
			GridCell next = g.poll();
			boolean posLeft = next.x != 0
					&& world[next.x - 1][next.y].distance == Double.MAX_VALUE;
			if (posLeft) {
				world[next.x - 1][next.y].distance = next.distance + discr;
				g.insert(world[next.x - 1][next.y]);
			}
			boolean posRight = next.x < world.length - 1
					&& world[next.x + 1][next.y].distance == Double.MAX_VALUE;
			if (posRight) {
				world[next.x + 1][next.y].distance = next.distance + discr;
				g.insert(world[next.x + 1][next.y]);
			}
			boolean posUp = next.y != 0
					&& world[next.x][next.y - 1].distance == Double.MAX_VALUE;
			if (posUp) {
				world[next.x][next.y - 1].distance = next.distance + discr;
				g.insert(world[next.x][next.y - 1]);
			}
			boolean posDown = next.y < world[0].length - 1
					&& world[next.x][next.y + 1].distance == Double.MAX_VALUE;
			if (posDown) {
				world[next.x][next.y + 1].distance = next.distance + discr;
				g.insert(world[next.x][next.y + 1]);
			}

			if (posUp && posLeft) {
				world[next.x - 1][next.y - 1].distance = next.distance + discr
						* rt2;
				g.insert(world[next.x - 1][next.y - 1]);
			}
			if (posUp && posRight) {
				world[next.x + 1][next.y - 1].distance = next.distance + discr
						* rt2;
				g.insert(world[next.x + 1][next.y - 1]);
			}
			if (posDown && posLeft) {
				world[next.x - 1][next.y + 1].distance = next.distance + discr
						* rt2;
				g.insert(world[next.x - 1][next.y + 1]);
			}
			if (posDown && posRight) {
				world[next.x + 1][next.y + 1].distance = next.distance + discr
						* rt2;
				g.insert(world[next.x + 1][next.y + 1]);
			}

		}
	}

	private static final double rt2 = Math.sqrt(2);

	private int heapIndex;

	@Override
	public int getHeapIndex() {
		return heapIndex;
	}

	@Override
	public void setHeapIndex(int ix) {
		heapIndex = ix;
	}
}
