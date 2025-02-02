package org.cwilt.search.domains.citynav.tests;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.correlation.KendallsCorrelation;

import org.cwilt.search.algs.basic.bestfirst.Greedy;
import org.cwilt.search.domains.citynav.City;
import org.cwilt.search.domains.citynav.Citynav;
import org.cwilt.search.domains.citynav.CitynavPortalHeuristic;
import org.cwilt.search.domains.citynav.Place;
import org.cwilt.search.search.Limit;
public class CitynavPortalConstructor {
	private final Random random;
	private final int childIndex;

	private final CitynavPortalHeuristic cph;

	public CitynavPortalConstructor(Citynav cn, List<Place> portals, int childIndex, int seed) {
		this.cn = cn;
		this.portals = portals;
		this.childIndex = childIndex % portals.size();
		this.random = new Random(seed);

		this.cph = new CitynavPortalHeuristic(cn, portals);
	}

	private Place getRandomPlace() {
		List<City> cities = cn.getAllCities();
		int ix = random.nextInt(cities.size());
		City c = cities.get(ix);
		List<Place> places = c.getAllPlaces();
		ix = random.nextInt(places.size());
		return places.get(ix);
	}

	public List<CitynavPortalConstructor> generateChildren(int desiredChildren) {
		List<CitynavPortalConstructor> children = new ArrayList<CitynavPortalConstructor>();

		for (int i = 0; i < desiredChildren; i++) {
			List<Place> newChildPortals = new ArrayList<Place>(portals.size());
			newChildPortals.addAll(portals);
			newChildPortals.set(childIndex, getRandomPlace());
			children.add(new CitynavPortalConstructor(cn, newChildPortals, childIndex + 1, random.nextInt()));
		}

		return children;
	}

	private final Citynav cn;
	private final List<Place> portals;

	private static final int GBFS_SAMPLE = 100;

	public double rateHeuristicGBFSExpansions() {
		cn.setPortalHeuristic(cph);
		
		double expansions = 0;
		for (int i = 0; i < GBFS_SAMPLE; i++) {
			Place randomStart = this.getRandomPlace();
			Place randomGoal = this.getRandomPlace();
			cn.setStart(randomStart);
			cn.setEnd(randomGoal);
			Greedy g = new Greedy(cn, new Limit());
			g.solve();
			expansions += g.getLimit().getExpansions();
		}
		return expansions / GBFS_SAMPLE;
	}
	
	private static final int SAMPLE_SIZE = 100000;
	
//	public double rateHeuristicGBFSSolutionLength() {
//		double expansions = 0;
//		for (int i = 0; i < GBFS_SAMPLE; i++) {
//			Place randomStart = this.getRandomPlace();
//			cn.setStart(randomStart);
//			Greedy g = new Greedy(cn, new Limit());
//			ArrayList<SearchState> children = g.solve();
//			expansions += children.size();
//		}
//		return expansions / GBFS_SAMPLE;
//	}

	public double rateHeuristicTau() {
		cn.setPortalHeuristic(cph);

		ArrayList<Double> costs = new ArrayList<Double>();
		ArrayList<Double> heuristics = new ArrayList<Double>();

		for (int i = 0; i < SAMPLE_SIZE; i++) {
			Place start = getRandomPlace();
			Place dest = getRandomPlace();
			costs.add(cn.getHStar(start, dest));
			heuristics.add(start.distTo(dest));
		}

		KendallsCorrelation kc = new KendallsCorrelation();

		double[] a1 = new double[costs.size()];
		double[] a2 = new double[heuristics.size()];

		for (int i = 0; i < costs.size(); i++) {
			a1[i] = costs.get(i);
			a2[i] = heuristics.get(i);
		}

		double corr = kc.correlation(a2, a1);

		return corr;
	}
}
