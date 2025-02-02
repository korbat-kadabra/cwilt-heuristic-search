/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.search;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public abstract class PDB implements PDBInterface, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5680159601753728074L;
	public static class PDBValue implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2614268290409351326L;
		private final int d;
		private final double h;
		public PDBValue(int d, double h){
			this.d = d;
			this.h = h;
		}
		@Override
		public String toString() {
			return "PDBValue [d=" + d + ", h=" + h + "]";
		}
		
		
	}
	protected final HashMap<Object, PDBValue> pdb;
	
	protected abstract ArrayList<SearchNodeDepth> makeGoals(SearchNodeDepth canonicalGoal);
	
	public double getH(Object o){
		PDBValue v = pdb.get(o);
		if(v == null){
			System.err.println("failed to find this:");
			System.err.println(o.getClass().getCanonicalName());
			System.err.println(o);
			
			System.err.println("loooking here");
			printPDB();
			throw new IllegalArgumentException();
		}
		return v.h;
	}
	public int getD(Object o){
		PDBValue v = pdb.get(o);
		assert(v != null);
		return v.d;
	}
	protected PDB(HashMap<Object, PDBValue> p, SearchNodeDepth initial){
		this.pdb = p;
		
		
		org.cwilt.search.utils.basic.MinHeap<SearchNode> open = new org.cwilt.search.utils.basic.MinHeap<SearchNode>(
				new SearchNode.GComparator());
		
		HashSet<Object> onOpen = new HashSet<Object>();
		
		ArrayList<SearchNodeDepth> goals = makeGoals(initial);

		for(SearchNodeDepth g : goals){
			open.insert(g);
			onOpen.add(g.getState().getKey());
		}
		
		while (!open.isEmpty()) {
			SearchNode next = open.poll();
			Object k = next.getState().getKey();
			if(pdb.containsKey(k))
				continue;
			ArrayList<? extends SearchNode> children = next.reverseExpand();
			SearchNodeDepth d = (SearchNodeDepth) next;
			pdb.put(k, new PDBValue(d.getDepth(),
					next.getG()));
			for (SearchNode c : children) {
				if (!pdb.containsKey(c.getState().getKey())) {
					if(!onOpen.contains(c.getState().getKey())){
						open.insert(c);
						onOpen.add(c.getState().getKey());
					}
				}
			}
		}
	}
	
	public void comparePDB(PDB other){
		Iterator<Map.Entry<Object, PDBValue>> iter = pdb.entrySet().iterator();
		int missed = 0;
		while(iter.hasNext()){
			Map.Entry<Object, PDBValue> next = iter.next();
			if(!other.pdb.containsKey(next.getKey())){
				missed ++;
				System.err.println("Missing this:");
				System.err.println(next.getKey());
			} 
		}
		System.err.println("Missed " + missed + "/" + pdb.size() + " items");
	}
	
	public void printPDB(){
		int i = 0;
		System.err.println("there are " + pdb.size() + " entries");
		for(Map.Entry<Object, PDBValue> entry: pdb.entrySet()){
			if(i > 100)
				break;
			System.err.println("Starting " + i);
			System.err.println(entry.getKey().getClass().getCanonicalName());
			System.err.println(entry.getKey());
			System.err.println(entry.getValue().h);
			System.err.println("Ending " + i);
			i++;
		}
	}
}
