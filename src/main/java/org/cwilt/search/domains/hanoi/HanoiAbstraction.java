package org.cwilt.search.domains.hanoi;
import java.io.Serializable;

public class HanoiAbstraction implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2239070880398467081L;
	private final int bottomDisk;
	private final int nDisks;
	public HanoiAbstraction(int bottomDisk, int nDisks) {
		this.bottomDisk = bottomDisk;
		this.nDisks = nDisks;
	}
	public Object abstractState(HanoiState s){
		return s.abstractState(nDisks, bottomDisk);
	}
	public int getNDisks() {
		return nDisks;
	}
}
