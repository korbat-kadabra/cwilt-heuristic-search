package org.cwilt.search.domains.multiagent.problem;
import java.util.HashMap;
import java.util.List;

public interface MultiagentGraph {
	/**
	 * gets all the vertexes in the graph
	 * 
	 * @return all the vertexes in the MultiagentGraph
	 */
	public List<MultiagentVertex> getAllVertexes();

	/**
	 * Writes the graph out, and returns the map of MultiagentVertexes to
	 * Integers that was used to generate the XML that got output
	 * 
	 * @param path
	 *            Path to output the XML to
	 * @return The map used to get the ID's for the cells
	 */
	public HashMap<MultiagentVertex, Integer> writeXML(String path);
}
