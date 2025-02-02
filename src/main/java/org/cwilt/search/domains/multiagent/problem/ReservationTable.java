package org.cwilt.search.domains.multiagent.problem;
import java.io.Serializable;
import java.util.HashMap;

public class ReservationTable implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8226125416996776947L;

	/**
	 * clears the reservation table of all reservations.
	 */
	public void clear() {
		this.reservations.clear();
	}

	public static final class Reservation implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 7758037860111665558L;
		public final int vertexID;
		public final int time;

		public Reservation(int vID, int t) {
			this.vertexID = vID;
			this.time = t;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + time;
			result = prime * result + vertexID;
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
			Reservation other = (Reservation) obj;
			if (time != other.time)
				return false;
			if (vertexID != other.vertexID)
				return false;
			return true;
		}
	}

	private final int[] latestReservations;
	private final int[] claimedIndefinitely;
	private final Agent[] claimants;

	private final HashMap<Reservation, Agent> reservations;

	public ReservationTable(int max) {
		this.reservations = new HashMap<Reservation, Agent>();
		this.latestReservations = new int[max];
		this.claimedIndefinitely = new int[max];
		this.claimants = new Agent[max];

		for (int i = 0; i < max; i++) {
			this.claimedIndefinitely[i] = Integer.MAX_VALUE;
		}
	}

	/**
	 * Exception that is thrown when an invalid reservation request is made.
	 * 
	 * @author cmo66
	 * 
	 */
	public class InvalidReservation extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 558466428069503154L;

		private final Agent incumbent, other;
		private final int time;
		private final MultiagentVertex v;

		public InvalidReservation(MultiagentVertex v, int time, Agent a,
				Agent incumbent) {
			this.incumbent = incumbent;
			this.v = v;
			this.other = a;
			this.time = time;
		}

		@Override
		public String getMessage() {
			StringBuffer b = new StringBuffer();

			b.append("Error making a reservation for ");
			b.append(v);
			b.append(" at time ");
			b.append(time);
			b.append(" for agent ");
			b.append(other);
			b.append(" conflicting with incumbent agent ");
			b.append(incumbent);
			return b.toString();
		}
	}

	/**
	 * Tries to reserve the space for this agent at the desired time.
	 * 
	 * @param v
	 *            Vertex to reserve
	 * @param time
	 *            Time to reserve the vertex for
	 * @param a
	 *            Agent to reserve the space and time for
	 */
	public void reserveSpace(MultiagentVertex v, int time, Agent a) {
		Reservation r = new Reservation(v.getID(), time);
		Agent incumbent = reservations.get(r);
		int vid = v.getID();
		if (claimedIndefinitely[vid] <= time) {
			boolean owner = a == claimants[vid];
			if (!owner) {
				System.err
						.println("This conficts with an indefinite reservation");
				assert (false);
			}
		}
		if (incumbent == null || incumbent.equals(a)) {
			reservations.put(r, a);
			if (time > latestReservations[vid])
				latestReservations[vid] = time;
		} else {
			throw new InvalidReservation(v, time, a, incumbent);
		}
	}

	/**
	 * Checks the reservation table to see if this agent can occupy the desired
	 * space at the desired time.
	 * 
	 * @param v
	 *            Vertex
	 * @param time
	 *            Time to check
	 * @param a
	 *            Agent to check for
	 * @return if the agent can occupy this vertex at the desired time
	 */
	public boolean checkReservation(MultiagentVertex v, int time, Agent a) {
		Reservation r = new Reservation(v.getID(), time);
		Agent incumbent = reservations.get(r);

		if (claimedIndefinitely[v.getID()] <= time) {
			return claimants[v.getID()] == a;
		}
		if (incumbent == null || incumbent.equals(a)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean canClaimIndefinitely(MultiagentVertex v, int time, Agent a) {
		int vID = v.getID();

		int lastTime = latestReservations[vID];
		if (lastTime > time) {
			Reservation r = new Reservation(vID, lastTime);
			Agent lastOwner = reservations.get(r);
			if(a == lastOwner)
				return true;
			
			return a == claimants[vID];
		}
		if (claimedIndefinitely[vID] != Integer.MAX_VALUE)
			return false;
		return true;
	}

	public void releaseIndefiniteClaim(MultiagentVertex v) {
		int vID = v.getID();
		this.claimants[vID] = null;
		claimedIndefinitely[vID] = Integer.MAX_VALUE;
	}

	public void claimIndefinitely(MultiagentVertex v, int time, Agent a) {
		assert (canClaimIndefinitely(v, time, a));
		int vID = v.getID();
		this.claimants[vID] = a;
		claimedIndefinitely[vID] = time;
	}

	public void releaseReservation(MultiagentVertex v, int time, Agent d) {
		Reservation r = new Reservation(v.getID(), time);
		Agent incumbent = reservations.get(r);
		assert (incumbent != null && incumbent == d);

	}
}
