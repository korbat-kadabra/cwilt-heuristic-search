/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.domains.grid;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;

import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchState;import org.cwilt.search.algs.basic.bestfirst.WAStar;
public class CellGridProblem extends GridProblem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5735390850449156606L;

	public CellGridProblem(boolean pruneParent){
		super(100, 50, pruneParent, true);
		for(int i = 0; i < map.length; i++){
			for(int j = 0; j < map[0].length; j++){
				map[i][j] = new GridCell(STATUS.OPEN, j, i, 1.0);
			}
		}
		
		makeCell(20, 20, 20, 15);
//		makeCell(270, 10, 20, 80);
		makeCell(25,23,2,8);
//		makeCell(13,3,5,13);
//		makeCell(70,3,13,15);
		
	}
	
	protected CellGridProblem(int xSize, int ySize, int cellWidth, int cellHeight, boolean pruneParent) {
		super(xSize, ySize, pruneParent, true);
		for(int i = 0; i < map.length; i++){
			for(int j = 0; j < map[0].length; j++){
				map[i][j] = new GridCell(STATUS.OPEN, j, i, 1.0);
			}
		}
		boolean odd = true;
		for(int i = 1; i < width; i+= cellWidth + 2){
			odd = !odd;
			for(int j = 1; j < height; j += cellHeight + 2){
				if(odd)
					makeCell(i, j, cellWidth, cellHeight);
				else
					makeCell(i, j + cellHeight/2, cellWidth, cellHeight);
			}
		}
		
	}
	
	private void setCell(int x, int y){
		if(y >= map.length)
			return;
		if(x >= map[0].length - 1)
			return;
		map[y][x] = new GridProblem.GridCell(STATUS.BLOCKED, x, y, 1.0);
	}
	
	private void makeCell(int xStart, int yStart, int cellWidth, int cellHeight){
		for(int i = 0; i < cellWidth; i++){
			setCell(xStart + i, yStart);
			setCell(xStart + i, yStart + cellHeight);
		}
		for(int i = 0; i <= cellHeight; i++){
			setCell(xStart + cellWidth, yStart + i);
		}
	}
	

	public static void main(String[] args) throws FileNotFoundException,
			ParseException {
		CellGridProblem g = new CellGridProblem(true);
		WAStar a = new WAStar(g, new Limit(), 3);
		ArrayList<SearchState> path = a.solve();

		// a.printSearchData(System.err);

		for (SearchState s : path) {
			GridState gs = (GridState) s;
			g.onPath(gs.getX(), gs.getY());
		}
		a.printSearchData(System.out);
		displayGridProblem(g);
	}
}
