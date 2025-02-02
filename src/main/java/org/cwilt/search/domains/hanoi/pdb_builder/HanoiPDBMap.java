package org.cwilt.search.domains.hanoi.pdb_builder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

public class HanoiPDBMap {
	private final int[] pdbMap;
	private final int totalDisks;
	public HanoiPDBMap(int[] map, int totalDisks){
		this.totalDisks = totalDisks;
		this.pdbMap = map;
	}
	
	public int[] getPDBMap(){
		return this.pdbMap;
	}
	private HanoiPDBMap(HanoiPDBMap parent, int abstractedDisk, int refinedDisk){
		this.totalDisks = parent.totalDisks;
		this.pdbMap = parent.pdbMap.clone();
		for(int i = 0; i < this.pdbMap.length; i++){
			int currentDisk = pdbMap[i];
			if(currentDisk == abstractedDisk){
				pdbMap[i] = refinedDisk;
			}
		}
		Arrays.sort(this.pdbMap);
	}
	
	@Override
	public String toString() {
		return "HanoiPDBMap [pdbMap=" + Arrays.toString(pdbMap) + "]";
	}
	public List<HanoiPDBMap> generateNeighbors(){
		ArrayList<HanoiPDBMap> neighbors = new ArrayList<HanoiPDBMap>();
		List<Integer> abstracted = new ArrayList<Integer>(totalDisks - this.pdbMap.length);
		BitSet s = new BitSet();
		for(int i : pdbMap){
			s.set(i);
		}
		for(int i = 0; i < totalDisks; i++){
			if(!s.get(i)){
				abstracted.add(i);
			}
		}
		for(int abstractedDisk : this.pdbMap){
			for(int refined : abstracted){
				neighbors.add(new HanoiPDBMap(this, abstractedDisk, refined));
			}
		}
		return neighbors;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(pdbMap);
		result = prime * result + totalDisks;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HanoiPDBMap other = (HanoiPDBMap) obj;
		if (!Arrays.equals(pdbMap, other.pdbMap))
			return false;
		if (totalDisks != other.totalDisks)
			return false;
		return true;
	}
	
}
