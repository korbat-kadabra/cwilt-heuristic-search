package org.cwilt.search.utils.basic;
import java.util.List;

import org.cwilt.search.search.SearchNode;
public abstract class AbstractBucketQueue {
	public abstract SearchNode peek();
	public abstract boolean add(SearchNode i);
	public abstract void clear();
	public abstract boolean isEmpty();
	public abstract void remove(SearchNode incumbent, int ix);
	public abstract SearchNode pop();
	public abstract int size();
	public abstract List<SearchNode> getAll();
}
