package org.cwilt.search.domains.kiva;
import org.cwilt.search.search.Limit;
public class SearchTracker {
	
	public void reset(){
		l2d = new Limit();
		l3d = new Limit();
		l3df = new Limit();
		hStar = new Limit();
	}
	
	private Limit l2d = new Limit();
	private Limit l3d = new Limit();
	private Limit l3df = new Limit();
	private Limit hStar = new Limit();
	
	public void incrSearchTime2d(Limit l){
		l = l.clone();
		l2d.addTo(l);
	}
	public void incrHStar(Limit l){
		l = l.clone();
		hStar.addTo(l);
	}
	public void incrSearchTime3dFail(Limit l){
		l = l.clone();
		l3df.addTo(l);
	}
	public void incrSearchTime3dAll(Limit l){
		l = l.clone();
		l3d.addTo(l);
	}
	private static SearchTracker t;
	public static SearchTracker getTracker(){
		if(t != null)
			return t;
		t = new SearchTracker();
		return t;
	}
	
	private SearchTracker(){
		
	}
	
	public String toString(){
		StringBuffer b = new StringBuffer();
		b.append("2d exp ");
		b.append(l2d.getExpansions());
		b.append(" time ");
		b.append(l2d.getDuration() / 1000.);

		b.append("\n3d exp ");
		b.append(l3d.getExpansions());
		b.append(" time ");
		b.append(l3d.getDuration() / 1000.);

		b.append("\n3df exp ");
		b.append(l3df.getExpansions());
		b.append(" time ");
		b.append(l3df.getDuration() / 1000.);

		b.append("\nhStar exp ");
		b.append(hStar.getExpansions());
		b.append(" time ");
		b.append(hStar.getDuration() / 1000.);
		b.append(" dup ");
		b.append(hStar.getDuplicates());
		b.append(" rexp ");
		b.append(hStar.getReExpansion());

		return b.toString();
	}
}
