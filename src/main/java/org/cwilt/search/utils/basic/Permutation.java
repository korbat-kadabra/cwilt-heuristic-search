/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.utils.basic;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

/**
 * 
 */

/**
 * @author cmo66
 * 
 */
public class Permutation implements Cloneable {
	
	
	// pos is the postion of each number. Where is value 5? pos[5]
	private int pos[];
	// val is the value of each index, the permutation.
	private int val[];

	static Random random = new Random(0);

	public int[] getVal() {
		return val;
	}

	// private boolean check() {
	// boolean acceptable = true;
	// for (int i = 0; acceptable && i < pos.length; i++) {
	// if (pos[i] == val[pos[i]])
	// acceptable = false;
	// }
	// return acceptable;
	// }

	private static String stringArray(int[] a) {
		String toReturn = "";
		for (int i = 0; i < a.length; i++) {
			toReturn = toReturn + String.format(" %5d", a[i]);
		}
		return toReturn;
	}

	private static int[] invertArray(int[] a) {
		int[] toReturn = new int[a.length];
		for (int i = 0; i < a.length; i++) {
			toReturn[a[i]] = i;
		}
		return toReturn;
	}

	private static void swapArray(int[] a, int i1, int i2) {
		int temp = a[i2];
		a[i2] = a[i1];
		a[i1] = temp;
	}

	private static long rankHelper(int n, int[] a, int[] a_inv) {
		if (n == 0)
			return 0;
		int s = a[n - 1];
		swapArray(a, n - 1, a_inv[n - 1]);
		swapArray(a_inv, s, n - 1);
		return ((long) s) + ((long) n) * rankHelper(n - 1, a, a_inv);
	}

	private long rank() {
		int[] valCopy = val.clone();
		return rankHelper(valCopy.length, valCopy, invertArray(valCopy));
	}
	
	public static long rank(char[] ary){
		int[] valCopy = new int[ary.length];
		for(int i = 0; i < ary.length; i++){
			valCopy[i] = ary[i];
		}
		return rankHelper(valCopy.length, valCopy, invertArray(valCopy));
	}

	private static void unrankHelper(int size, int rank, int[] a) {
		if (size > 0) {
			swapArray(a, size - 1, rank % size);
			unrankHelper(size - 1, rank / size, a);
		}
	}

	public Permutation(int size, int rank) {
		val = new int[size];
		for (int i = 0; i < size; i++)
			val[i] = i;

		unrankHelper(size, rank, val);

		pos = invertArray(val);
	}

	@Override
	public String toString() {
		return "pos " + stringArray(pos) + "\nval " + stringArray(val);
	}

	/**
	 * @param s
	 *            how big to make the default initial permutation
	 */
	public Permutation(int s) {
		pos = new int[s];
		val = new int[s];
		for (int i = 0; i < s; i++) {
			pos[i] = i;
			val[i] = i;
		}
	}

	/**
	 * 
	 * @param size
	 *            how many entries this permutation should have
	 * @return a permutation with that many entries
	 */
	public static Permutation randomPermutation(int size) {
		int[] arr = new int[size];
		for (int i = 0; i < size; i++) {
			arr[i] = i;
		}
		for (int k = arr.length - 1; k > 0; k--) {
			int w = random.nextInt(k);
			int temp = arr[w];
			arr[w] = arr[k];
			arr[k] = temp;
		}
		return new Permutation(arr);
	}

	public static int[] randomArrayPermutation(int size) {
		int[] arr = new int[size];
		for (int i = 0; i < size; i++) {
			arr[i] = i;
		}
		for (int k = arr.length - 1; k > 0; k--) {
			int w = random.nextInt(k);
			int temp = arr[w];
			arr[w] = arr[k];
			arr[k] = temp;
		}
		return arr;
	}

	/**
	 * 
	 * @param a
	 *            an array to make a permutation out of
	 */
	public Permutation(int[] a) {
		val = new int[a.length];
		pos = new int[a.length];

		for (int i = 0; i < a.length; i++) {
			val[i] = a[i];
			pos[a[i]] = i;
		}
	}

	private void swap(int i1, int i2) {
		int temp = val[i2];
		val[i2] = val[i1];
		val[i1] = temp;

		temp = pos[val[i2]];
		pos[val[i2]] = pos[val[i1]];
		pos[val[i1]] = temp;
	}

	@Override
	public int hashCode() {
		return (int) rank();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Permutation other = (Permutation) obj;
		if (!Arrays.equals(val, other.val))
			return false;
		return true;
	}

	private Permutation(int[] p, int[] v) {
		val = v;
		pos = p;
	}

	/**
	 * clones the permutation, making a deep copy
	 */
	public Permutation clone() {
		int[] newPA = new int[pos.length];
		int[] newVA = new int[pos.length];

		for (int i = 0; i < pos.length; i++) {
			newPA[i] = pos[i];
			newVA[i] = val[i];
		}

		return new Permutation(newPA, newVA);
	}

	/**
	 * 
	 * @param id
	 *            the waypoint to move up
	 * @return an arraylist containing all permutations in which this waypoint
	 *         has been moved forward in the schedule.
	 */
	public ArrayList<Permutation> promote(int id) {
		ArrayList<Permutation> toReturn = new ArrayList<Permutation>();

		int currentPosition = pos[id];

		for (int i = 0; i < currentPosition; i++) {
			Permutation p = this.clone();
			p.swap(i, currentPosition);
			toReturn.add(p);
		}

		assert (toReturn.size() > 0);

		return toReturn;
	}

	private static class Pair {
		public int first;
		public int second;

		public Pair(int first, int second) {
			assert (first != second);
			this.first = first;
			this.second = second;
		}
	}

	private static ArrayList<Pair> getAllPairs(int max) {
		ArrayList<Pair> toReturn = new ArrayList<Pair>();
		for (int i = 0; i < max; i++) {
			for (int j = 0; j < max && j < i; j++) {
				toReturn.add(new Permutation.Pair(i, j));
			}
		}
		return toReturn;
	}

	/**
	 * 
	 * @return an arraylist that has all possible permtiations that result from
	 *         making a single swap in this permutaiton.
	 */
	public ArrayList<Permutation> allSwaps() {
		ArrayList<Pair> allPairs = getAllPairs(pos.length);
		ArrayList<Permutation> allPermutations = new ArrayList<Permutation>();
		Iterator<Pair> i = allPairs.iterator();
		while (i.hasNext()) {
			Pair p = i.next();
			Permutation newPermutation = this.clone();
			newPermutation.swap(p.first, p.second);
			allPermutations.add(newPermutation);
		}
		return allPermutations;
	}

	/**
	 * 
	 * @param i
	 *            index whose position is desired to be known
	 * @return the position of index i
	 */
	public int getPosition(int i) {
		return pos[i];
	}

	/**
	 * 
	 * @param i
	 *            whose value is desired
	 * @return value of index i
	 */
	public int getValue(int i) {
		return val[i];
	}

	public static void main(String s[]) {
		int[] a = { 1, 3, 0, 2 };
		Permutation p = new Permutation(a);
		p.clone();
	}
}
