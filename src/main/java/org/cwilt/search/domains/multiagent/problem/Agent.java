package org.cwilt.search.domains.multiagent.problem;
import org.cwilt.search.search.SearchProblem;
public interface Agent {

	/**
	 * 
	 * @return The state of this agent
	 */
	public AgentState getState();

	public int getID();

	/**
	 * 
	 * @return the next task this agent has to do
	 */
	public MultiagentTask getNextTask();

	/**
	 * 
	 * @return the ID of the task this agent is currently working on
	 */
	public int getCurrentTaskID();

	/**
	 * 
	 * @param taskID
	 *            the ID of the task to get
	 * @return the taskID-th task this agent is to perform
	 */
	public MultiagentTask getTask(int taskID);

	/**
	 * 
	 * @return The time this agent has a plan for
	 */
	public int getCurrentTime();

	/**
	 * 
	 * @param s 
	 */
	public void addToPath(AgentState s);

	/**
	 * Checks if it is possible to do the next task
	 * 
	 * @return If it is possible for this agent to do its next task. Returns
	 *         true if it is in the correct location and the location is free
	 *         for the required number of steps, false otherwise.
	 */
	public boolean canDoTask();

	/**
	 * Denotes that this task and all subsequent tasks have failed.
	 * @param t 
	 */
	public void failedTask(MultiagentTask t);

	/**
	 * Attempts to do the current task by appending the required number of wait
	 * actions
	 * @param taskCompleted 
	 */
	public void doTask(boolean taskCompleted);

	/**
	 * 
	 * @return The reservation table in charge of governing where and when this
	 *         agent can do things
	 */
	public ReservationTable getReservationTable();

	/**
	 * Has the agent wait at its current location.
	 * 
	 * @param i
	 *            amount of time to wait
	 * @throws JammedAgentException 
	 */
	public void addWaitMoves(int i) throws JammedAgentException;

	/**
	 * Has the agent wait at its current location.
	 * 
	 * @param i
	 *            amount of time to wait
	 * @return 
	 */
	public boolean canAddWaitMoves(int i);

	/**
	 * Removes moves from this agent's path until this move is found.
	 * 
	 * @param s
	 */
	public void backoffUntil(AgentState s);

	/**
	 * @param v
	 *            MultiagentVertex to navigate to
	 * @return A SearchProblem for getting to the specified location
	 */
	public SearchProblem getSimpleProblem(MultiagentVertex v);

	/**
	 * Has the mobile unit move from the original vertex to the next vertex.
	 * 
	 * @param original
	 *            Vertex the mobile unit starts at
	 * @param next
	 *            Vertex the mobile unit ends at
	 */
	public void doMove(MultiagentVertex original, MultiagentVertex next);

	/**
	 * Has the mobile unit move from the original vertex to the next vertex, but
	 * there are not restrictions on the occuapncy of the next vertex. The
	 * vertexes must be adjacent to one another.
	 * 
	 * @param original
	 *            Start Vertex
	 * @param next
	 *            End vertex
	 */

	public void doGlobalGhostMove(MultiagentVertex original,
			MultiagentVertex next);

	public void doTaskGlobalGhost();
	public boolean isDone();
	
	public AgentState getPreviousState(int count);
	// public boolean wander();

	// public void claimSpace(int amount);

}
