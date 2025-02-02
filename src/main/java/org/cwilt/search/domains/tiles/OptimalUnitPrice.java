package org.cwilt.search.domains.tiles;
import java.io.IOException;
import java.util.ArrayList;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchNode;import org.cwilt.search.search.SearchState;import org.cwilt.search.algs.basic.bestfirst.AStar;
public class OptimalUnitPrice {

	public static void main(String[] args) throws IOException,
			ClassNotFoundException {
		TileProblem unit = new TileProblem(args[0], "unit", null);
		TileProblem inv = new TileProblem(args[0], "inverse", null);

		AStar a = new AStar(unit, new Limit());
		ArrayList<SearchState> path = a.solve();
		AStar a2 = new AStar(inv, new Limit());
		a2.solve();
		SearchNode current = SearchNode.makeInitial(inv.getInitial());
		for (int nodeID = 0; nodeID < path.size() - 1; nodeID++) {
			SearchState currentUnitNode = path.get(nodeID);
			SearchState nextUnitNode = path.get(nodeID + 1);
			assert (current.getState().getKey()
					.equals(currentUnitNode.getKey()));
			ArrayList<? extends SearchNode> children = current.expand();
			for (SearchNode c : children) {
				if (c.getState().getKey().equals(nextUnitNode.getKey())) {
					current = c;
					continue;
				}
			}
		}
		assert(current.getState().isGoal());
		System.err.printf("%f\t%f", a2.getFinalCost(), current.getG());
	}
}
