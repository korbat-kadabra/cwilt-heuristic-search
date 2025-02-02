package org.cwilt.search.domains.vacuum;import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

import org.cwilt.search.domains.vacuum.VacuumProblem.VacuumCell;
/**
 * Find the minimum spanning tree that connects all remaining dirty cells (and
 * optionally a charging cell). This is for use with heuristic functions h2 and
 * h3. Since generating a minimum spanning tree is quite expensive, it is only
 * done when that tree has not been created before. All created trees are stored
 * here statically in a hash map, hashed by the dirty cells and charging cell
 * that they represent.
 * 
 * Implemented using Kruskal's algorithm.
 * 
 * @author Carmen St. Jean
 * 
 */
public class MinimumSpanningTree {
    private double cost = 0;

	public MinimumSpanningTree(VacuumCell[] dirts) {
        List<VacuumCell> newList = new ArrayList<VacuumCell>();
        // Only include the dirty cells that remain.
        for (int i = 0; i < dirts.length; i++) {
            if (dirts[i] != null) {
                newList.add(dirts[i]);
            }
        }
        buildMst(newList);
    }

    /**
     * Gets the cost of the minimum spanning tree.
     * 
     * @return The cost of the tree as a double.
     */
    public double getCost() {
        return cost;
    }

    /**
     * Builds a minimum spanning tree to connect the specified cells.
     * 
     * @param cells
     *            The cells for which a minimum spanning tree is required.
     */
    private void buildMst(List<VacuumCell> cells) {
        PriorityQueue<Edge> allEdges = new PriorityQueue<Edge>();
        List<Tree> forest = new ArrayList<Tree>();

        for (int i = 0; i < cells.size(); i++) {
            VacuumCell cell = cells.get(i);
            Tree tree = new Tree();
            tree.add(cell);
            forest.add(tree);

            for (int j = i + 1; j < cells.size(); j++) {
                allEdges.add(new Edge(cell, cells.get(j)));
            }
        }

        buildMstFromForest(allEdges, forest);
    }

    /**
     * Builds the minimum spanning tree from the forest of disjoint trees and
     * determines the cost of that minimum spanning tree.
     * 
     * @param allEdges
     *            All of the possible edges in the graph.
     * @param forest
     *            The forest of disjoint trees where each tree contains a single
     *            cell.
     * @return The list of edges that form the minimum spanning tree.
     */
    private List<Edge> buildMstFromForest(PriorityQueue<Edge> allEdges,
            List<Tree> forest) {
        List<Edge> mstEdges = new ArrayList<Edge>();

        while (!allEdges.isEmpty() && forest.size() > 1) {
            // Find the minimum edge.
            Edge minimumEdge = allEdges.poll();

            VacuumCell cellA = minimumEdge.getCellA();
            VacuumCell cellB = minimumEdge.getCellB();
            int indexTreeA = -1;
            int indexTreeB = -1;

            // Look through the forest for the trees that contain the two cells.
            for (int i = 0; i < forest.size(); i++) {
                Tree tree = forest.get(i);

                if (tree.contains(cellA)) {
                    indexTreeA = i;
                }

                if (tree.contains(cellB)) {
                    indexTreeB = i;
                }
            }

            // If the two cells are in two different trees, those trees should
            // be joined; otherwise ignore this edge completely.
            if (indexTreeA != indexTreeB) {
                mergeTrees(forest, indexTreeA, indexTreeB);
                mstEdges.add(minimumEdge);
                cost = cost + minimumEdge.getCost();
            }
        }

        return mstEdges;
    }

    /**
     * Combines two trees of the forest together.
     * 
     * @param forest
     *            The collection of disjoint trees.
     * @param indexTreeA
     *            The index of the first tree to be combined with the second
     *            tree.
     * @param indexTreeB
     *            The index of the second tree to be combined with the first
     *            tree.
     */
    private void mergeTrees(List<Tree> forest, int indexTreeA, int indexTreeB) {
        forest.get(indexTreeA).mergeIntoTree(forest.get(indexTreeB));
        forest.remove(indexTreeB);
    }

    /**
     * Defines an edge connecting two cells.
     * 
     * @author Carmen St. Jean
     * 
     */
    private static class Edge implements Comparable<Edge> {
        private final int BEFORE = -1;
        private final int EQUAL = 0;
        private final int AFTER = 1;

        private final VacuumCell cellA;
        private final VacuumCell cellB;
        private final double cost;

        /**
         * Creates an edge from two cells and calculates the cost of the edge.
         * 
         * @param cellA
         *            The first cell.
         * @param cellB
         *            The second cell.
         */
        public Edge(VacuumCell cellA, VacuumCell cellB) {
            this.cellA = cellA;
            this.cellB = cellB;
            this.cost = VacuumProblem.manhattan(cellA, cellB);
        }

        /**
         * Gets the primary cell stored for this edge.
         * 
         * @return The A cell.
         */
        public VacuumCell getCellA() {
            return cellA;
        }

        /**
         * Gets the secondary cell stored for this edge.
         * 
         * @return The B cell.
         */
        public VacuumCell getCellB() {
            return cellB;
        }

        /**
         * Gets the cost for this edge.
         * 
         * @return The edge's cost.
         */
        public double getCost() {
            return cost;
        }

        @Override
        public int compareTo(Edge cellPair) {
            // Sort lowest to highest distance.
            if (this.getCost() < cellPair.getCost()) {
                return BEFORE;
            }

            if (this.getCost() > cellPair.getCost()) {
                return AFTER;
            }

            return EQUAL;
        }
    }

    /**
     * Defines a tree where the nodes are unique cells.
     * 
     * @author Carmen St. Jean
     * 
     */
    private static class Tree extends HashSet<VacuumCell> {
        private static final long serialVersionUID = 7194448419581626719L;

        /**
         * Add all cells of another tree into this tree.
         * 
         * @param otherTree
         *            The other tree to be added to this tree.
         */
        public void mergeIntoTree(Tree otherTree) {
            for (VacuumCell cell : otherTree) {
                this.add(cell);
            }
        }
    }
}