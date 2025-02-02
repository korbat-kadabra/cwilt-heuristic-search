/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.cwilt.search.search.SearchState.Child;
import org.cwilt.search.utils.basic.Heapable;
import org.cwilt.search.utils.experimental.BinHeapable;

public class SearchNode implements Heapable, Comparable<SearchNode>, Serializable, BinHeapable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6357416610921366613L;
	
	public static class WFGComparator implements Comparator<SearchNode>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1734140338711340319L;
		private final double weight;

		public WFGComparator(double w) {
			this.weight = w;
		}

		@Override
		public int compare(SearchNode arg0, SearchNode arg1) {
			double g0 = arg0.getG();
			double h0 = arg0.getF() - g0;
			double fp0 = g0 + weight * h0;

			double g1 = arg1.getG();
			double h1 = arg1.getF() - g1;
			double fp1 = g1 + weight * h1;

			if (fp0 == fp1) {
				if (arg0.equals(arg1))
					return 0;
				else {
					if (g0 > g1)
						return -1;
					else if (g0 < g1)
						return 1;
					else
						return 0;
				}
			} else if (fp0 < fp1)
				return -1;
			else
				return 1;
		}

	}

	public static class FGComparator implements Comparator<SearchNode>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 832261326719023623L;

		@Override
		public int compare(SearchNode arg0, SearchNode arg1) {

			if (arg0.getF() < arg1.getF())
				return -1;
			else if (arg0.getF() > arg1.getF())
				return 1;
			else {
				if (arg0.equals(arg1))
					return 0;
				else {
					if (arg0.getG() > arg1.getG())
						return -1;
					else if (arg0.getG() < arg1.getG())
						return 1;
					else
						return 0;
					// return arg0.getState().lexOrder(arg1.getState());
				}
			}
		}
	}

	public static class FGLComparator implements Comparator<SearchNode>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2098441552202775085L;

		@Override
		public int compare(SearchNode arg0, SearchNode arg1) {
			if (arg0.getF() == arg1.getF()) {
				if (arg0.equals(arg1))
					return 0;
				else {
					if (arg0.getG() > arg1.getG())
						return -1;
					else if (arg0.getG() < arg1.getG())
						return 1;
					else
						return arg0.getState().lexOrder(arg1.getState());
				}
			} else if (arg0.getF() < arg1.getF())
				return -1;
			else
				return 1;
		}
	}

	public static class FLComparator implements Comparator<SearchNode>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4550951417585677674L;

		@Override
		public int compare(SearchNode arg0, SearchNode arg1) {
			int toReturn;

			if (arg0.getF() == arg1.getF()) {
				if (arg0.equals(arg1))
					toReturn = 0;
				else {
					toReturn = -1 * arg0.getState().lexOrder(arg1.getState());
				}
			} else if (arg0.getF() < arg1.getF())
				toReturn = -1;
			else if (arg0.getF() > arg1.getF())
				toReturn = 1;
			else {
				toReturn = 0;
				assert (false);
			}
			return toReturn;
		}
	}

	public static class GComparator implements Comparator<SearchNode>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4077555715237985975L;

		@Override
		public int compare(SearchNode arg0, SearchNode arg1) {
			if (arg0.getG() > arg1.getG())
				return 1;
			else if (arg0.getG() < arg1.getG())
				return -1;
			else
				return 0;
		}
	}

	public static class GHComparator implements Comparator<SearchNode>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1596939707139315237L;
		private final HComparator hc;

		public GHComparator() {
			this.hc = new HComparator();
		}

		@Override
		public int compare(SearchNode arg0, SearchNode arg1) {
			if (arg0.getG() > arg1.getG())
				return 1;
			else if (arg0.getG() < arg1.getG())
				return -1;
			else
				return hc.compare(arg0, arg1);
		}
	}

	public static class HComparator implements Comparator<SearchNode>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1824882215556318581L;

		@Override
		public int compare(SearchNode arg0, SearchNode arg1) {
			if (arg0.getH() > arg1.getH())
				return 1;
			else if (arg0.getH() < arg1.getH())
				return -1;
			else
				return 0;
		}
	}

	public static class HLComparator implements Comparator<SearchNode>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -4688049162283778005L;

		@Override
		public int compare(SearchNode arg0, SearchNode arg1) {
			if (arg0.getH() > arg1.getH())
				return 1;
			else if (arg0.getH() < arg1.getH())
				return -1;
			else
				return arg0.getState().lexOrder(arg1.getState());
		}
	}

	public static class DComparator implements Comparator<SearchNode>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2852169506203465784L;

		@Override
		public int compare(SearchNode arg0, SearchNode arg1) {

			double d0 = arg0.getD();
			double d1 = arg1.getD();
			if (d0 > d1)
				return 1;
			else if (d0 < d1)
				return -1;
			else {
				return 0;
			}
		}
	}

	public static class DLComparator implements Comparator<SearchNode>, Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4896740487328040182L;

		@Override
		public int compare(SearchNode arg0, SearchNode arg1) {
			if (arg0.getD() > arg1.getD())
				return 1;
			else if (arg0.getD() < arg1.getD())
				return -1;
			else {
				return arg0.getState().lexOrder(arg1.getState());
			}
		}
	}

	private int heapIndex;
	private SearchNode parent;
	protected SearchState s;
	protected double g;
	protected int d;
	private double f;

	public SearchNode getParent() {
		return parent;
	}

	protected SearchNode(SearchNode parent, SearchState s, double g) {
		assert (s != null);
		this.heapIndex = Heapable.NO_POS;
		this.g = g;
		this.f = g + s.h();
		this.d = s.d();
		this.parent = parent;
		this.s = s;
	}

	protected SearchNode(SearchNode parent, SearchState s, double g, double f) {
		assert (s != null);
		this.heapIndex = Heapable.NO_POS;
		this.g = g;
		this.f = f;
		this.d = s.d();
		this.parent = parent;
		this.s = s;
	}
	
	private ArrayList<Child> peChildren;
	
	/**
	 * Partially expands this node. If there are no more children left, this will
	 * return null.
	 * 
	 * @return
	 */
	public SearchNode partialExpand() {
		if(this.peChildren != null || !(this.s instanceof PartialExpansionSearchState)) {
			// okay not a partial expansion state - expand it fully.
			if(this.peChildren == null) {
				this.peChildren = this.getState().expand();
			}
			if(this.peChildren.isEmpty()) {
				return null;
			}
			Child nextChild = peChildren.remove(peChildren.size() - 1);
			return new SearchNode(this, nextChild.child, this.g + nextChild.transitionCost);
			
		} else {
			PartialExpansionSearchState next = (PartialExpansionSearchState) this.s;
			Child nextChild = next.getNextChild();
			if(nextChild == null) {
				return null;
			}
			SearchNode nextNode = new SearchNode(this, nextChild.child, this.g + nextChild.transitionCost);
			return nextNode;
		}
	}

	public ArrayList<? extends SearchNode> expand() {
		ArrayList<SearchState.Child> baseChildren = s.expand();
		int nChildren = baseChildren.size();

		ArrayList<SearchNode> children = new ArrayList<SearchNode>(nChildren);

		for (int i = 0; i < nChildren; i++) {
			SearchState.Child c = baseChildren.get(i);
			children.add(new SearchNode(this, c.child, c.transitionCost + g));
		}
		// Iterator<SearchState.Child> it = baseChildren.iterator();
		// while (it.hasNext()) {
		// SearchState.Child c = it.next();
		// }
		return children;
	}

	public ArrayList<? extends SearchNode> reverseExpand() {
		ArrayList<SearchState.Child> baseChildren = s.reverseExpand();
		ArrayList<SearchNode> children = new ArrayList<SearchNode>();
		Iterator<SearchState.Child> it = baseChildren.iterator();
		while (it.hasNext()) {
			SearchState.Child c = it.next();
			children.add(new SearchNode(this, c.child, c.transitionCost + g));
		}
		return children;
	}

	public SearchState getState() {
		return s;
	}

	public double getG() {
		return g;
	}

	public double getF() {
		return f;
	}

	public double getH() {
		return f - g;
	}

	public int getD() {
		return d;
	}

	public final ArrayList<SearchState> reconstructPath() {
		ArrayList<SearchState> path = new ArrayList<SearchState>();

		SearchNode currentNode = this;
		while (currentNode != null) {
			path.add(currentNode.s);
			currentNode = currentNode.parent;
		}
		Collections.reverse(path);
		return path;
	}

	public static SearchNode makeInitial(SearchState s) {
		SearchNode n = new SearchNode(null, s, 0);
		return n;
	}

	public String printParents() {
		if (parent == null)
			return "";
		else
			return parent.printParents() + "\n" + this;
	}

	public String toString() {
		return "f " + this.getF() + " g " + this.getG() + " h " + (this.getH()) + " state\n" + s + "\n";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(f);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((s == null) ? 0 : s.hashCode());
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
		SearchNode other = (SearchNode) obj;
		if (Double.doubleToLongBits(f) != Double.doubleToLongBits(other.f))
			return false;
		if (s == null) {
			if (other.s != null)
				return false;
		} else if (s == other.s)
			return true;
		else if (!s.equals(other.s))
			return false;
		return true;
	}

	public int pathLength() {
		int pathLength = 1;
		SearchNode n = this;
		while (n.parent != null) {
			n = n.parent;
			pathLength++;
		}
		return pathLength;
	}

	public synchronized int concurrentGetHeapIndex() {
		return this.heapIndex;
	}

	public synchronized void concurrentSetHeapIndex(int ix) {
		this.heapIndex = ix;
	}

	@Override
	public int getHeapIndex() {
		return heapIndex;
	}

	@Override
	public void setHeapIndex(int ix) {
		heapIndex = ix;
	}

	public void setParent(SearchNode currentLocation) {
		this.parent = currentLocation;
	}

	@Override
	public int compareTo(SearchNode arg1) {
		if (this.getF() == arg1.getF()) {
			if (this.equals(arg1))
				return 0;
			else {
				if (this.getG() > arg1.getG())
					return -1;
				else if (this.getG() < arg1.getG())
					return 1;
				else
					return this.getState().lexOrder(arg1.getState());
			}
		} else if (this.getF() < arg1.getF())
			return -1;
		else
			return 1;
	}

}
