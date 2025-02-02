/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.search;
import java.util.ArrayList;

public abstract class SearchState {

	public class Child {
		@Override
		public String toString() {
			return "Child [child=" + child + ", transitionCost="
					+ transitionCost + "]\n";
		}

		public SearchState child;
		public double transitionCost;

		public Child(SearchState s, double c) {
			child = s;
			transitionCost = c;
		}
	}

	public abstract ArrayList<Child> expand();

	public abstract ArrayList<Child> reverseExpand();

	public abstract double h();

	public abstract int d();

	public abstract boolean isGoal();

	public abstract Object getKey();

	public abstract int hashCode();

	public abstract boolean equals(Object other);

	/**
	 * Gets the distance to another search state. Like the heuristic, except can
	 * be used to get an arbitrary distance.
	 * 
	 * @param other
	 * @return
	 */
	public double distTo(SearchState other) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * Search nodes have to have a complete lexocraphical ordering, so ties are
	 * broken in this order, absent any other considerations.
	 * 
	 * @param s
	 *            other state
	 * @return lexicographic order of this as compared to s
	 */
	public abstract int lexOrder(SearchState s);

	public static class NoPerfectHash extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7359255210884312766L;

	}

	public long perfectHash() {
		throw new NoPerfectHash();
	}
	
	/**
	 * Attempts to do in place modification, if the domain supports that.
	 * 
	 * @param childID
	 *            ID of the child to get
	 * @param parentID
	 * 	          ID used to make the parent
	 * @return the cost of the action, or something less than zero if the
	 *         conversion is invalid.
	 */
	public double convertToChild(int childID, int parentID) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 
	 * @return how many children this node has
	 */
	public int nChildren(){
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 
	 * @param childID ID of the child to get the inverse of
	 * @return the inverse of the child in question
	 */
	public int inverseChild(int childID){
		throw new UnsupportedOperationException();
	}
}
