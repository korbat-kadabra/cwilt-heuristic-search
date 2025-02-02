package org.cwilt.search.domains.asteroids.planner;
import java.util.LinkedList;
import java.util.Queue;

import org.cwilt.search.domains.asteroids.AsteroidProblem;

import java.util.HashSet;
public class RTAStarPlanner extends AsteroidPlanner{
	private final int lookahead;
	public RTAStarPlanner(AsteroidProblem a, int timeout, String[] args) {
		super(a, timeout);
		this.lookahead = Integer.parseInt(args[3]);
	}
	@Override
	public AsteroidState nextState() {
		System.err.println("Current time is " + super.getCurrentState().getTime());
		
		Queue<AsteroidState> open = new LinkedList<AsteroidState>();
		HashSet<AsteroidState> closed = new HashSet<AsteroidState>();
		HashSet<AsteroidState> onOpen = new HashSet<AsteroidState>();
		open.add(super.getCurrentState());
		onOpen.add(super.getCurrentState());
		
		double initialScore = super.getCurrentState().getScore();
		AsteroidState incumbent = null;

		int expanded = 0;
		for(int i = 0; i < lookahead && !open.isEmpty(); i++){
			AsteroidState next = open.poll();
			onOpen.remove(next);
			closed.add(next);
			if(next.isTerminal())
				continue;
			
			expanded ++;
			for(AsteroidState child : next.expandRaw()){
				if(child.getScore() > initialScore)
					incumbent = child;
				
				if(child.getScore() > initialScore){
					System.err.println("destroyed something");
					assert(false);
				}
				if(closed.contains(child)){
					continue;
				}
				if(onOpen.contains(child))
					continue;
				open.add(child);
				onOpen.add(child);
			}
		}

		System.err.println("expanded " + expanded + " nodes");

		
		if(incumbent != null){
			return findAncestor(incumbent, super.getCurrentState());
		}
		else if(!open.isEmpty()){
			return findAncestor(open.peek(), super.getCurrentState());
		} else
			return null;
	}
	
	private AsteroidState findAncestor(AsteroidState s, AsteroidState root) {
		if (s == root)
			return null;
		assert (s != root);
		assert (s != null);
		while (!s.getParent().equals(root)) {
			s = s.getParent();
			assert (s != null);
		}
		return s;
	}


}
