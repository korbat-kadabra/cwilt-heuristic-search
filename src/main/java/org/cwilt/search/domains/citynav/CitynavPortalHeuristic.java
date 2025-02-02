package org.cwilt.search.domains.citynav;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cwilt.search.utils.basic.Heapable;
public class CitynavPortalHeuristic {
	public final List<Place> portals;
	public final Citynav citynav;

	public final Map<Place, PortalPlaceData> allPlaces;

	public CitynavPortalHeuristic(Citynav c, List<Place> portals) {
		this.citynav = c;
		this.portals = portals;
		this.allPlaces = new HashMap<Place, PortalPlaceData>();
		for (City currentCity : this.citynav.getAllCities()) {
			for (Place p : currentCity.getAllPlaces()) {
				allPlaces.put(p, new PortalPlaceData(p));
			}
		}
		this.initInterportalDistances();
		this.initClosestPortalDistances();
	}

	public double calculateHeuristic(Place start, Place goal) {
		PortalPlaceData ppd1 = this.allPlaces.get(start);
		PortalPlaceData ppd2 = this.allPlaces.get(goal);
		double interportalDistance = this.interportalDistances
				.get(new PlacePair(ppd1.closestPortal, ppd2.closestPortal));
		double toReturn = interportalDistance - ppd1.closestPortalDistance - ppd2.closestPortalDistance;
		return Math.max(0, toReturn);
	}

	public class PortalPlaceDataComparator implements Comparator<PortalPlaceData> {
		@Override
		public int compare(PortalPlaceData o1, PortalPlaceData o2) {
			double diff = o1.closestPortalDistance - o2.closestPortalDistance;
			if (diff < 0) {
				return -1;
			} else if (diff > 0) {
				return 1;
			}
			return 0;
		}
	}

	private final HashMap<PlacePair, Double> interportalDistances = new HashMap<PlacePair, Double>();

	private void initInterportalDistances() {
		for (Place p1 : portals) {
			for (Place p2 : portals) {
				this.interportalDistances.put(new PlacePair(p1, p2), citynav.getHStar(p1, p2));
			}
		}

	}

	private void initClosestPortalDistances() {
		for(Map.Entry<Place, PortalPlaceData> ppd : allPlaces.entrySet()){
			ppd.getValue().closestPortal = portals.get(0);
			ppd.getValue().closestPortalDistance = citynav.getHStar(ppd.getKey(), ppd.getValue().closestPortal);
			for(Place portal : portals){
				double newDistance = citynav.getHStar(ppd.getKey(), portal);
				if(newDistance < ppd.getValue().closestPortalDistance){
					ppd.getValue().closestPortal = portal;
					ppd.getValue().closestPortalDistance = newDistance;
				}
			}
		}
	}

	private static class PlacePair {
		public final Place p1, p2;

		public PlacePair(Place p1, Place p2) {
			this.p1 = p1;
			this.p2 = p2;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((p1 == null) ? 0 : p1.hashCode());
			result = prime * result + ((p2 == null) ? 0 : p2.hashCode());
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
			PlacePair other = (PlacePair) obj;
			if (p1 == null) {
				if (other.p1 != null)
					return false;
			} else if (!p1.equals(other.p1))
				return false;
			if (p2 == null) {
				if (other.p2 != null)
					return false;
			} else if (!p2.equals(other.p2))
				return false;
			return true;
		}

	}

	public class PortalPlaceData implements Heapable {
		public final Place place;
		private Place closestPortal = null;
		private double closestPortalDistance = Double.MAX_VALUE;

		public PortalPlaceData(Place p) {
			this.place = p;
		}

		private int heapIndex = Heapable.NO_POS;

		@Override
		public int getHeapIndex() {
			return heapIndex;
		}

		@Override
		public void setHeapIndex(int ix) {
			this.heapIndex = ix;
		}
	}

}
