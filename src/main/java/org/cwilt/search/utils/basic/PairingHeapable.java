package org.cwilt.search.utils.basic;
import org.cwilt.search.utils.basic.PairingHeap.Position;
public interface PairingHeapable<T> {
	public void setPosition(Position<T> o);
	public Position<T> getPosition();
}
