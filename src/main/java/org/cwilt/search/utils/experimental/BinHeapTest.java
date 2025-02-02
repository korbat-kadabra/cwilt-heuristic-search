package org.cwilt.search.utils.experimental;
import java.util.Comparator;
import java.util.Queue;
import java.util.Random;

import org.junit.Test;

import org.cwilt.search.utils.basic.Heapable;import org.cwilt.search.utils.basic.MinHeap;import org.cwilt.search.utils.basic.PairingHeap.Position;import org.cwilt.search.utils.basic.PairingHeapable;
public class BinHeapTest {

	public static class BHT implements BinHeapable, Heapable, Cloneable,
			PairingHeapable<BHT>, Comparable<BHT> {

		public BHT clone() {
			return new BHT(f, g, id);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
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
			BHT other = (BHT) obj;
			if (id != other.id)
				return false;
			return true;
		}

		private final double f, g;
		private final int id;
		private static int COUNT;

		public BHT(double f, double g) {
			this.id = COUNT++;
			this.f = f;
			this.g = g;
		}

		public BHT(double f, double g, int id) {
			this.id = id;
			this.f = f;
			this.g = g;
		}

		@Override
		public double getF() {
			return f;
		}

		@Override
		public double getG() {
			return g;
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

		public static class FGComparator implements Comparator<BHT> {
			@Override
			public int compare(BHT arg0, BHT arg1) {
				if (arg0.getF() == arg1.getF()) {
					if (arg0.equals(arg1))
						return 0;
					else {
						if (arg0.getG() > arg1.getG())
							return -1;
						else if (arg0.getG() < arg1.getG())
							return 1;
						else
							return 0;
					}
				} else if (arg0.getF() < arg1.getF())
					return -1;
				else
					return 1;
			}
		}

		public static class ReverseFGComparator implements Comparator<BHT> {
			@Override
			public int compare(BHT arg1, BHT arg0) {
				if (arg0.getF() == arg1.getF()) {
					if (arg0.equals(arg1))
						return 0;
					else {
						if (arg0.getG() > arg1.getG())
							return -1;
						else if (arg0.getG() < arg1.getG())
							return 1;
						else
							return 0;
					}
				} else if (arg0.getF() < arg1.getF())
					return -1;
				else
					return 1;
			}
		}

		
		@Override
		public int compareTo(BHT arg0) {
			if (this.f < arg0.f)
				return -1;
			if (this.f > arg0.f)
				return 1;
			if (this.g < arg0.g)
				return -1;
			if (this.g > arg0.g)
				return 1;
			return 0;
		}

		private Position<BHT> position = null;

		@Override
		public void setPosition(Position<BHT> o) {
			this.position = o;
		}

		@Override
		public Position<BHT> getPosition() {
			return this.position;
		}
	}

	public BinHeapTest() {
		bh = new BinHeap<BHT>(BinHeap.HEAPTYPE.FHEAP, 0);
		mh = new MinHeap<BHT>(new BHT.FGComparator());
	}

	Random r = new Random(1777);

	Queue<BHT> bh;
	MinHeap<BHT> mh;

	@Test
	public void removeTest() {
		BHT b = new BHT(0, 0);
		bh.add(b);
		BHT out = bh.remove();
		assert (b == out);
	}

	@Test
	public void miscTest() {
		int ct = 2000;

		for (int i = 0; i < ct; i++) {
			BHT b = new BHT(r.nextDouble(), r.nextDouble());
			BHT copy = (BHT) b.clone();
			assert (bh.size() == mh.size());
			bh.add(b);
			mh.add(copy);
		}
		for (int i = 0; i < ct; i++) {
			BHT mhNext = mh.poll();
			BHT bhNext = bh.poll();
			assert (bh.size() == mh.size());

			assert (bhNext != null);
			assert (mhNext != null);
			if (mhNext.getF() != bhNext.getF()) {
				System.err.println("Failed on index " + i);
			}
			assert (mhNext.getF() == bhNext.getF());
		}

	}

	@Test
	public void longRemoveTest() {
		int ct = 2000;

		for (int i = 0; i < ct; i++) {
			BHT b = new BHT(r.nextDouble(), r.nextDouble());
			BHT copy = (BHT) b.clone();
			bh.add(b);
			mh.add(copy);
		}
		for (int i = 0; i < ct; i++) {
			BHT mhNext = mh.poll();
			BHT bhNext = bh.poll();

			assert (bh.size() == mh.size());

			assert (bhNext != null);
			assert (mhNext != null);
			assert (mhNext.getF() == bhNext.getF());
		}

	}

	@Test
	public void removeObjectTest() {
		BHT b = new BHT(0, 0);
		bh.add(b);
		boolean removeNoReturn = false;
		try {
			removeNoReturn = bh.remove(new Object());
			assert (false);
		} catch (ClassCastException cce) {

		}
		assert (!removeNoReturn);
		removeNoReturn = bh.remove(new BHT(1, 1));
		assert (!removeNoReturn);

		boolean removeReturn = bh.remove(b);
		assert (removeReturn);
		int bs = bh.size();
		int ms = mh.size();
		assert (ms == bs);

	}

	@Test
	public void insertFindTest() {
		BHT b = new BHT(0, 0);
		bh.add(b);
		boolean contained = bh.contains(b);
		assert (contained);
	}

	@Test
	public void insertTest() {
		for (int i = 0; i < 1000; i++) {
			BHT b = new BHT(r.nextDouble(), r.nextDouble());
			bh.add(b);
			assert (bh.contains(b));
		}
	}

	public void printTest() {
		double testDouble = 8.5243;
		BinHeap.printBinary(System.out, Double.doubleToLongBits(testDouble));
		System.out.println();

		long firstB = BinHeap.mantissaOnly(Double.doubleToLongBits(testDouble),
				0, 8);
		BinHeap.printBinary(System.out, firstB);
		System.out.println();

		firstB = BinHeap
				.mantissaOnly(Double.doubleToLongBits(testDouble), 8, 8);
		BinHeap.printBinary(System.out, firstB);
		System.out.println();

		firstB = BinHeap.mantissaOnly(Double.doubleToLongBits(testDouble), 16,
				8);
		BinHeap.printBinary(System.out, firstB);
		System.out.println();

		firstB = BinHeap.mantissaOnly(Double.doubleToLongBits(testDouble), 24,
				8);
		BinHeap.printBinary(System.out, firstB);
		System.out.println();

		firstB = BinHeap.mantissaOnly(Double.doubleToLongBits(testDouble), 32,
				8);
		BinHeap.printBinary(System.out, firstB);
		System.out.println();

		firstB = BinHeap.mantissaOnly(Double.doubleToLongBits(testDouble), 40,
				8);
		BinHeap.printBinary(System.out, firstB);
		System.out.println();

		firstB = BinHeap.mantissaOnly(Double.doubleToLongBits(testDouble), 48,
				4);
		BinHeap.printBinary(System.out, firstB);
		System.out.println();

		// long exp = BinHeap.exponentOnly(Double.doubleToLongBits(testDouble));
		// BinHeap.printBinary(System.out, exp);
		// System.out.println();
	}
}
