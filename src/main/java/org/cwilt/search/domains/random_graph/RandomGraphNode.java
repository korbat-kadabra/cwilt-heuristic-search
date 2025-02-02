package org.cwilt.search.domains.random_graph;
import java.util.ArrayList;

import org.cwilt.search.search.SearchState;import org.cwilt.search.utils.basic.Heapable;
public class RandomGraphNode extends org.cwilt.search.search.SearchState implements org.cwilt.search.utils.basic.Heapable, 
	java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8847716889632278703L;

	private final int id;
	
	private double hStar;
	private double h;
	
	private final RandomGraph g;
	private final ArrayList<RandomGraphNode> children;
	private final ArrayList<Double> costs;
	private final ArrayList<RandomGraphNode> parents;
	private final ArrayList<Double> parentCosts;
	
	void clear(){
		parents.clear();
		parentCosts.clear();
	}
	
	public int getNParents(){
		return parents.size();
	}
	
	public double getHStar(){
		return hStar;
	}
	
	public RandomGraphNode getParent(int i)
	{
		return parents.get(i);
	}
	public double getParentCost(int i)
	{
		return parentCosts.get(i);
	}
	
	public RandomGraphNode(RandomGraph g, int id) {
		this.id = id;
		this.g = g;
		this.children = new ArrayList<RandomGraphNode>();
		this.costs = new ArrayList<Double>();
		this.hStar = -1;
		this.parents = new ArrayList<RandomGraphNode>();
		this.parentCosts = new ArrayList<Double>();
		this.heapIndex = Heapable.NO_POS;
	}
	
	public void setHStar(double newHStar){
		this.hStar = newHStar;
	}
	public void setH(double newH){
		this.h = newH;
	}
	
	public void connectTo(int ix){
		children.add(g.getNode(ix));
		double min = g.getMinTransition();
		double diff = g.getMaxTransition() - min;
		double nextCost = g.getR().nextFloat() * diff + min;
		costs.add(nextCost);
		
		RandomGraphNode child = g.getNode(ix);
		child.parents.add(this);
		child.parentCosts.add(nextCost);
	}
	
	public void initChildren(int branchingFactor){
		for(int i = 0; i < branchingFactor; i++){
			connectTo(g.getR().nextInt(g.getNNodes()));
		}
	}
	
	public ArrayList<Child> expand(){
		ArrayList<Child> c = new ArrayList<Child>();

		for(int i = 0; i < children.size(); i++){
			c.add(new Child(children.get(i), costs.get(i)));
		}
		
		return c;
	}


	@Override
	public Object getKey() {
		return id;
	}

	@Override
	public double h() {
		return h;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		RandomGraphNode other = (RandomGraphNode) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public boolean isGoal() {
		return id == 0;
	}

	@Override
	public int lexOrder(SearchState s) {
		assert(false);
		return 0;
	}
	
	public String toString(){
		StringBuffer b = new StringBuffer();
		
		b.append(id);
		b.append(" with cost ");
		b.append(hStar);
		b.append("\n");
		for(int i = 0; i < children.size(); i++){
			b.append(children.get(i).id);
			b.append(" at cost ");
			b.append(costs.get(i));
			b.append("\n");
		}
				
		return b.toString();
	}

	private int heapIndex;
	
	@Override
	public int getHeapIndex() {
		return heapIndex;
	}

	@Override
	public void setHeapIndex(int ix) {
		this.heapIndex = ix;
	}

	@Override
	public ArrayList<Child> reverseExpand() {
		assert(false);
		return null;
	}

	@Override
	public int d() {
		assert(false);
		return 0;
	}
}
