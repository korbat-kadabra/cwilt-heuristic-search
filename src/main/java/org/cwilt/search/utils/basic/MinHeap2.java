package org.cwilt.search.utils.basic;
import java.util.Comparator;

public class MinHeap2<T extends DoubleHeapable> extends MinHeap<T> {

	public MinHeap2(Comparator<T> c) {
		super(c);
	}
	protected void setHeapIndex(Heapable item, int ix){
		DoubleHeapable d = (DoubleHeapable) item;
		d.setHeap2Index(ix);
	}
	protected int getHeapIndex(Heapable item){
		DoubleHeapable d = (DoubleHeapable) item;
		return d.getHeap2Index();
	}


}
