package org.cwilt.search.utils.floats;
public class FloatUtils {
	public static String doubleString(double value){
		StringBuffer b = new StringBuffer(64);
		long bits = Double.doubleToLongBits(value);
		long mask = 1l << 63;
		while(mask != 0){
			if((mask & bits) == 0){
				b.append("0");
			} else 
				b.append("1");
			mask >>>= 1;
		}
		return b.toString();
	}
}
