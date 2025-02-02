package org.cwilt.search.domains.citynav.tests;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cwilt.search.domains.citynav.City;
import org.cwilt.search.domains.citynav.Citynav;
import org.cwilt.search.domains.citynav.CitynavPortalHeuristic;
import org.cwilt.search.domains.citynav.Place;
public class CitynavPortalTest {

	private static final class CitynavResult {
		private final double initialTau, hillClimbedTau, nexusTau;
		private final double initialExpansions, hillClimbedExapnsions, nexusExpansions;

		public CitynavResult(double it, double hct, double nt, double ie, double hce, double ne) {
			this.initialTau = it;
			this.hillClimbedTau = hct;
			this.nexusTau = nt;
			this.initialExpansions = ie;
			this.hillClimbedExapnsions = hce;
			this.nexusExpansions = ne;
		}

		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append(initialTau);
			buf.append("\t");
			buf.append(initialExpansions);
			buf.append("\t");
			buf.append(hillClimbedTau);
			buf.append("\t");
			buf.append(this.hillClimbedExapnsions);
			buf.append("\t");
			buf.append(nexusTau);
			buf.append("\t");
			buf.append(nexusExpansions);
			return buf.toString();
		}
	}

	public CitynavPortalTest(int seed) {
		this.random = new Random(seed);
		cn = new Citynav(150, 150, 3, 3, 2, seed);
	}

	private final Random random;
	private final Citynav cn;

	private Place getRandomPlace(Citynav cn) {
		List<City> cities = cn.getAllCities();
		int ix = random.nextInt(cities.size());
		City c = cities.get(ix);
		List<Place> places = c.getAllPlaces();
		ix = random.nextInt(places.size());
		return places.get(ix);
	}

	public static void main(String[] args) throws UnknownHostException, FileNotFoundException, UnsupportedEncodingException {
		int startingSeed;
		InetAddress addr = InetAddress.getLocalHost();
		String outfileName = addr.getHostName() + "_output";
		startingSeed = addr.getHostName().hashCode();
		
		PrintWriter writer = new PrintWriter(outfileName, "UTF-8");
		
		for (int i = 0; i < 10; i++) {
			System.err.println(i);;
			CitynavPortalTest t = new CitynavPortalTest(i* 100 + startingSeed);
			CitynavResult r = t.tt();
			writer.println(r.toString());
		}
		writer.close();
	}

	public CitynavResult tt() {
		List<Place> portals = new ArrayList<Place>();
		for (@SuppressWarnings("unused")
		City c : cn.getAllCities()) {
			portals.add(getRandomPlace(cn));
		}

		CitynavPortalHeuristic cph = new CitynavPortalHeuristic(cn, portals);
		cn.setPortalHeuristic(cph);

		CitynavPortalConstructor incumbent = new CitynavPortalConstructor(cn, portals, 0, 0);
		CitynavPortalConstructor initial = incumbent;
		double incumbentQuality = incumbent.rateHeuristicTau();
		double initialQuality = incumbentQuality;

		int unchangedCount = 0;

		while (unchangedCount < portals.size()) {
			// System.err.println(unchangedCount);
			List<CitynavPortalConstructor> children = incumbent.generateChildren(2 * cn.getAllCities().size());
			boolean changed = false;
			for (CitynavPortalConstructor child : children) {
				double childQuality = child.rateHeuristicTau();
				if (childQuality > incumbentQuality) {
					unchangedCount = 0;
					incumbent = child;
					incumbentQuality = childQuality;
					changed = true;
				}
			}

			if (!changed) {
				unchangedCount++;
			}
		}
		ArrayList<Place> nexusNodes = new ArrayList<Place>();
		for (City c : cn.getAllCities()) {
			nexusNodes.add(c.getPlace(0));
		}
		CitynavPortalConstructor nexusConstructor = new CitynavPortalConstructor(cn, nexusNodes, 0, 0);

		CitynavResult result = new CitynavResult(initialQuality, incumbentQuality, nexusConstructor.rateHeuristicTau(),
				initial.rateHeuristicGBFSExpansions(), incumbent.rateHeuristicGBFSExpansions(),
				nexusConstructor.rateHeuristicGBFSExpansions());
		
		return result;
	}

}
