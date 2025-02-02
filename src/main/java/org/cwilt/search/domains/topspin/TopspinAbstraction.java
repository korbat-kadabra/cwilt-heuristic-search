package org.cwilt.search.domains.topspin;
import java.io.Serializable;

public class TopspinAbstraction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2594005298608140660L;
	private final int startIX;
	private final int endIX;
	
	public int getStart(){
		return startIX;
	}
	public int getEnd(){
		return endIX;
	}
	public TopspinAbstraction(int startIX, int endIX){
		this.startIX = startIX;
		this.endIX = endIX;
	}

	
	public TopspinState abstractStateAsState(TopspinState s){
		return s.abstractState(startIX, endIX);
	}

	public Object abstractState(TopspinState s){
		return s.abstractState(startIX, endIX).getKey();
	}
}
