package org.cwilt.search.domains.multiagent.problem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.cwilt.search.domains.multiagent.solvers.autoqueue.AutoQueue;
import org.cwilt.search.domains.multiagent.solvers.queue.AgentQueue;


public interface MultiagentVertex {
	public boolean isPopular();
	public AgentQueue getQueue();
	public AutoQueue getAutoQueue();
	public void setQueue(AgentQueue a);
	public List<MultiagentVertex> getNeighbors(MultiagentVertex goal);
	public List<MultiagentVertex> getNeighbors();
	public int getID();
	public int hashCode();
	public boolean equals(Object other);
	public double distanceTo(MultiagentVertex other);
	public boolean isForbidden(MultiagentVertex start, MultiagentVertex goal);
	public ArrayList<MultiagentVertex> simplePath(MultiagentVertex goal);
	public double nearestPopularVertex();
	public void setColor(Color c);
	public boolean isTraversible();
	public void increaseUsage();
	public double getUsage();
	public boolean isLocalUsageMaximum();
	public int getDirectionalCount(int directionID);

	public void incrementUsage(MultiagentVertex other);
	public double getCongestion();
//	public double getHCongestion(MultiagentVertex other);
	public boolean isMultiagentGoal();

}
