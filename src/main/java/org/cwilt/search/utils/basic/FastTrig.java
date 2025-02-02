/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.utils.basic;
import java.util.Random;

public class FastTrig {

	private static final int ATAN2_BITS = 9;

	private static final int ATAN2_BITS2 = ATAN2_BITS << 1;
	private static final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
	private static final int ATAN2_COUNT = ATAN2_MASK + 1;
	private static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);

	private static final double INV_ATAN2_DIM_MINUS_1 = 1.0f / (ATAN2_DIM - 1);

	private static final double[] atan2 = new double[ATAN2_COUNT];
	private static final double[] acos = new double[ATAN2_DIM * 2];

	static {
		for (int i = 0; i < ATAN2_DIM; i++) {
			double x0 = (double) i / ATAN2_DIM;
			acos[i + ATAN2_DIM] = Math.acos(x0);
			acos[ATAN2_DIM - 1 + i] = Math.acos(-x0);
			for (int j = 0; j < ATAN2_DIM; j++) {
				double y0 = (double) j / ATAN2_DIM;
				atan2[j * ATAN2_DIM + i] = (double) Math.atan2(y0, x0);
			}
		}
	}

	private static final double acos(double theta) {
		if (theta <= 1 && theta >= -1) {
			theta = theta + 1;
			int ix = (int) (theta / (ATAN2_DIM * 2));
			return acos[ix];
		}
		System.err.println("Invalid: " + theta);
		return 0;
	}

	/**
	 * ATAN2
	 */

	private static final double atan2(double y, double x) {
		double add, mul;

		if (x < 0.0f) {
			if (y < 0.0f) {
				x = -x;
				y = -y;

				mul = 1.0f;
			} else {
				x = -x;
				mul = -1.0f;
			}

			add = -3.141592653f;
		} else {
			if (y < 0.0f) {
				y = -y;
				mul = -1.0f;
			} else {
				mul = 1.0f;
			}

			add = 0.0f;
		}

		double invDiv = 1.0f / (((x < y) ? y : x) * INV_ATAN2_DIM_MINUS_1);

		int xi = (int) (x * invDiv);
		int yi = (int) (y * invDiv);

		return (atan2[yi * ATAN2_DIM + xi] + add) * mul;
	}

	public static void main(String[] a) {
		Random r = new Random(0);
		for (int i = 0; i < 10000000; i++) {
			double d = r.nextDouble();
			double d2 = r.nextDouble();
			double diff = acos(d) - Math.acos(d);
			double diff2 = Math.atan2(d * 10, d2 * 10) - atan2(d * 10, d2 * 10);
			if (diff > 0.0001) {
				System.err.printf("acos %f %f %f %d\n", acos(d), Math.acos(d),
						d, i);
				System.exit(1);
			}
			if (diff2 > 0.001) {
				System.err.printf("atan2 %f %f %f %f %d\n", Math.atan2(d * 10,
						d2 * 10), atan2(d * 10, d2 * 10), d, d2, i);
				System.exit(1);
			}

		}
	}
}
