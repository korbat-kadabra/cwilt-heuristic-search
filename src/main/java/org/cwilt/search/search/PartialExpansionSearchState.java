package org.cwilt.search.search;

public abstract class PartialExpansionSearchState extends SearchState {
	/**
	 * Gets the next child. If it returns null, no more children exist.
	 * 
	 * @return the next child if there is one, otherwise will return null.
	 */
	public abstract Child getNextChild();

}
