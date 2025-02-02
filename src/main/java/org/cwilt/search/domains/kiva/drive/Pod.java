package org.cwilt.search.domains.kiva.drive;
import java.awt.Graphics2D;

import org.cwilt.search.domains.kiva.map.GridCell;
public class Pod {
	private GridCell location;
	private Drive drive;
	
	public Pod (GridCell g){
		this.location = g;
		this.drive = null;
	}
	
	public boolean isStationary(){
		assert(location != null);
		return drive == null;
	}
	public GridCell getLocation(){
		return location;
	}
	public Drive getDrive(){
		return drive;
	}
	public void setDrive(Drive d){
		this.drive = d;
	}
	public void setLocation(GridCell g){
		this.location = g;
	}
	public void draw(Graphics2D g){
		//TODO this needs to do something.
	}
}

