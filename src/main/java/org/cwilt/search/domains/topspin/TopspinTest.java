package org.cwilt.search.domains.topspin;
import java.util.HashMap;
import java.util.HashSet;

import org.junit.Test;

import org.cwilt.search.search.SearchState;
public class TopspinTest {
	public static HashSet<OperatorPair> getBanned(TopspinProblem p){
		HashMap<Object, OperatorPair> allowed = new HashMap<Object, OperatorPair>();
		HashSet<OperatorPair> banned = new HashSet<OperatorPair>();
//		System.err.println("Root: " + p.getInitial());
		for(SearchState.Child s : p.getInitial().expand()){
//			System.err.println("\t" + s.child);
			CanonicalTopspinState parent = (CanonicalTopspinState) s.child;
			for(SearchState.Child ss : s.child.expand()){
//				System.err.println("\t\t" + ss.child);
				CanonicalTopspinState child = (CanonicalTopspinState) ss.child;
				
				if(allowed.containsKey(child.getKey())){
//					System.err.println("this is a duplicate:");
//					System.err.println(child);
//					System.err.println("of this: " + allowed.get(child.getKey()));
					banned.add(new OperatorPair(parent.getGenerateID(), child.getGenerateID()));
				} else {
					allowed.put(ss.child.getKey(), new OperatorPair(parent.getGenerateID(), child.getGenerateID()));
				}
			}
		}
		return banned;
	}
	
	@Test
	public void test() throws ClassNotFoundException{
		TopspinProblem p = new TopspinProblem(10, 4, TopspinProblem.Cost.CANONICAL, new String[0]);
		System.err.println(getBanned(p));
	}
}
