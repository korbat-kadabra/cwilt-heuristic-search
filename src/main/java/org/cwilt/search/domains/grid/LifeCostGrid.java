package org.cwilt.search.domains.grid;
import java.io.IOException;
import java.text.ParseException;

public class LifeCostGrid extends RandomCostGrid {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1768718540425615737L;

	public LifeCostGrid(String path, boolean drawNumbers) throws IOException, ParseException {
		super(path, drawNumbers, 0, 0, 1);
	}

	protected double nextCost(GridCell cell){
		return cell.yPos;
	}
	

}
