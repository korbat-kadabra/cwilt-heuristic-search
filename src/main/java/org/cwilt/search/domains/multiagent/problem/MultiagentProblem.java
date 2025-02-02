package org.cwilt.search.domains.multiagent.problem;
import java.io.PrintStream;
import java.util.List;

public interface MultiagentProblem {
	/**
	 * 
	 * @return List of all the agents in this problem
	 */
	public List<Agent> getAgents();

	/**
	 * 
	 * @return The underlying graph
	 */
	public MultiagentGraph getGraph();

	/**
	 * 
	 * @return the reservation table in charge of the problem
	 */
	public ReservationTable getReservationTable();

	/**
	 * 
	 * @param pathPrefix
	 *            Path to the place there file should go
	 * @param seed 
	 * 
	 * @param instanceName
	 *            Name of this problem
	 */
	void writeXML(String pathPrefix, int seed);

	/**
	 * 
	 * @param outpath 
	 * @param mapname 
	 * @param seed 
	 * @param pathPrefix
	 *            Path to the place there file should go
	 * 
	 * @param instanceName
	 *            Name of this problem
	 */
	void writeHOG(String outpath, String mapname, int seed);

	/**
	 * Writes data about the map out to stderr
	 * @param s 
	 */
	void writeGraphData(PrintStream s);

	public void setGhost();
}
