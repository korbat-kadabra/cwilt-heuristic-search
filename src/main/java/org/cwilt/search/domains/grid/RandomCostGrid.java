package org.cwilt.search.domains.grid;
import java.io.IOException;
import java.text.ParseException;
import java.util.Random;

public class RandomCostGrid extends GridProblem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7113362336238775861L;
	
	private final Random r;
	private final double min, range;
	
	protected double nextCost(GridCell cell){
		return min + range * r.nextDouble();
	}
	
	
	
	public RandomCostGrid(String path, boolean drawNumbers, int seed, double min, double max)
			throws IOException, ParseException {
		super(path, true, drawNumbers, COST.RANDOM);
		this.min = min;
		this.range = max - min;
		
		this.r = new Random(seed);
		initHValues();
	}
	public RandomCostGrid(String path, boolean drawNumbers, int seed, double min, double max, int sx, int sy, int gx, int gy)
			throws IOException, ParseException {
		super(path, true, drawNumbers, COST.RANDOM, sx, sy, gx, gy);
		this.min = min;
		this.range = max - min;

		this.r = new Random(seed);
		
		for(int i = 0; i < super.width; i++){
			for(int j = 0; j < super.height; j++){
				super.map[j][i].exitCost = nextCost(super.map[j][i]);
			}
		}
		initHValues();
	}

}
