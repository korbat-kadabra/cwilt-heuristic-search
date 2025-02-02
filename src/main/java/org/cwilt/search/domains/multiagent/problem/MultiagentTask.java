package org.cwilt.search.domains.multiagent.problem;
import java.io.Serializable;

public class MultiagentTask implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6086093642261283734L;
	/**
	 * Where the mobile unit has to go to achieve this task
	 */
	public final MultiagentVertex destination;
	/**
	 * Amount of time the mobile unit has to sit on the destination to achieve the goal.
	 */
	public final int timeAtDestination;
	/**
	 * Determines if the task is completed
	 */
	private boolean isDone;
	/**
	 * 
	 * @return Whether or not this task has been completed yet
	 */
	public boolean isDone() {
		return isDone;
	}
	/**
	 * Marks this task as not yet completed
	 */
	public void markFailed() {
		this.isDone = false;
	}
	/**
	 * Marks this task as completed
	 */
	public void markDone() {
		this.isDone = true;
	}

	/**
	 * 
	 * @param v
	 *            Where the agent has to go to achieve this goal
	 * @param t
	 *            How long the agent has to set on the goal to achieve it.
	 */
	public MultiagentTask(MultiagentVertex v, int t) {
		this.isDone = false;
		assert (v != null);
		this.destination = v;
		this.timeAtDestination = t;
	}

	public String toString() {
		StringBuffer b = new StringBuffer();

		b.append("go to ");
		b.append(destination);
		b.append(" for ");
		b.append(this.timeAtDestination);

		return b.toString();
	}
}
