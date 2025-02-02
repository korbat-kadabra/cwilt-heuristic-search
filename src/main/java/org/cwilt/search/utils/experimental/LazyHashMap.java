package org.cwilt.search.utils.experimental;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Set;

import org.cwilt.search.utils.basic.Heapable;import org.cwilt.search.utils.basic.RingBuffer;
public class LazyHashMap<K, V> extends AbstractMap<K, V> {

	private final HashMap<K, Item> ht;
	private final int maxSize;
	private final RingBuffer<Item> buffer;
	
	public LazyHashMap<K, V> clone(){
		assert(false);
		return null;
	}
	
	private class Item implements Heapable{
		public final K key;
		public final V value;
		private int index;
		public Item(K key, V value){
			this.value = value;
			this.key = key;
			this.index = Heapable.NO_POS;
		}
		@Override
		public int getHeapIndex() {
			// TODO Auto-generated method stub
			return index;
		}
		@Override
		public void setHeapIndex(int ix) {
			index = ix;
		}
	}
	
	public LazyHashMap(int htc){
		assert(htc != 0);
		this.ht = new HashMap<K, Item>();
		this.maxSize = htc;
		this.buffer = new RingBuffer<Item>(this.maxSize);
	}
	
	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		assert(false);
		return null;
	}

	@Override
	public V get(Object key){
		assert(ht != null);
		assert(key != null);
		Item i = ht.get(key);
		if(i == null)
			return null;
		return i.value;
	}
	
	@Override
	public V put(K key, V value){
		assert(value != null);
		assert(key != null);
		
		Item i = new Item(key, value);
		Item rejected = buffer.addWithReplace(i);
		
		if(rejected  != null){
			assert(ht.containsKey(rejected.key));
			ht.remove(rejected.key);
		}
		ht.put(key, i);
		return null;
	}
	
	@Override
	public void clear(){
		buffer.clear();
		ht.clear();
	}
	
	public static void main(String[] args){
		LazyHashMap<Integer, Integer> lhm = new LazyHashMap<Integer, Integer>(1);
		lhm.put(10, 100);
		lhm.put(11, 101);
		lhm.put(12, 102);
		lhm.put(13, 103);
		lhm.put(14, 104);
		lhm.put(15, 105);
		lhm.put(16, 106);
		lhm.put(17, 107);
		lhm.put(18, 108);
		lhm.put(19, 109);
		
	}

}
