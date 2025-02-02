package org.cwilt.search.domains.multiagent.problem;
import java.util.ArrayList;


public interface AgentState {
	/**
	 * @return The ending vertex for this state
	 */
	public MultiagentVertex getEndVertex();
	/**
	 * @return The starting vertex for this state
	 */
	public MultiagentVertex getStartVertex();
	
	
	/**
	 * 
	 * @return The list of reservations required for executing this state
	 */
	public ArrayList<ReservationTable.Reservation> getReservations();
}
