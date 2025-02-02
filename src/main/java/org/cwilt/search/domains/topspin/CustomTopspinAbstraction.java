package org.cwilt.search.domains.topspin;
import java.util.BitSet;

public class CustomTopspinAbstraction extends TopspinAbstraction{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3037851773488277853L;
	private final BitSet abstracted;
	public CustomTopspinAbstraction(BitSet b) {
		super(0, 0);
		this.abstracted = b;
	}
	public TopspinState abstractStateAsState(TopspinState s){
		return s.abstractState(abstracted);
	}

	public Object abstractState(TopspinState s){
		return s.abstractState(abstracted).getKey();
	}
}
