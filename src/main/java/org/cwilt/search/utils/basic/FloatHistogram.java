package org.cwilt.search.utils.basic;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Class for making a histogram of floating point numbers.
 * 
 * @author chris
 *
 */
public class FloatHistogram {
	public static final class HistogramBucket{
		public final double value;
		private int count;
		public HistogramBucket(double v){
			this.value = v;
			this.count = 1;
		}
		public void incr(){
			this.count ++;
		}
		public int getCount(){
			return this.count;
		}
		public String toString(){
			return "value: " + value + " count: " + count;
		}
		
	}
	public static final class ValueSort implements Comparator<HistogramBucket>{

		@Override
		public int compare(HistogramBucket arg0, HistogramBucket arg1) {
			if(arg0.value < arg1.value)
				return -1;
			else if(arg0.value > arg1.value)
				return 1;
			else
				return 0;
		}
		
	}
	
	public static final class SizeSort implements Comparator<HistogramBucket>{

		@Override
		public int compare(HistogramBucket arg0, HistogramBucket arg1) {
			if(arg0.count > arg1.count)
				return -1;
			else if(arg0.count < arg1.count)
				return 1;
			else
				return 0;
		}
		
	}

	public static final SizeSort ss = new SizeSort();
	public static final ValueSort vs = new ValueSort();
	
	public final double[] floats;
	public final HistogramBucket[] bucketsByValue;
	public final HistogramBucket[] bucketsBySize;
	private int total;
	
	public FloatHistogram(double[] f){
		this.floats = f;
		HashMap<Double, HistogramBucket> hs = new HashMap<Double, HistogramBucket>();
		total = floats.length;
		for(double v : floats){
			HistogramBucket other = hs.get(v);
			if(other == null){
				other = new HistogramBucket(v);
				hs.put(v,  other);
			} else {
				other.incr();
			}
		}
		int count = hs.size();
		this.bucketsByValue = new HistogramBucket[count];
		this.bucketsBySize = new HistogramBucket[count];
		Iterator<Entry<Double, HistogramBucket>> i = hs.entrySet().iterator();
		int ix = 0;
		while(i.hasNext()){
			HistogramBucket next = i.next().getValue();
			bucketsByValue[ix] = next;
			bucketsBySize[ix] = next;
			ix ++;
		}
		Arrays.sort(this.bucketsByValue, vs);
		Arrays.sort(this.bucketsBySize, ss);
	}
	
	public double mostCommon(){
		return bucketsBySize[0].value;
	}
	
	public String toString(){
		StringBuffer b = new StringBuffer();
		b.append("size: ");
		b.append(total);
		b.append("value order: \n");
		for(HistogramBucket bk : bucketsByValue){
			b.append(bk);
			b.append("\n");
		}
		b.append("size order: \n");
		
		for(HistogramBucket bk : bucketsBySize){
			b.append(bk);
			b.append("\n");
		}
		
		
		return b.toString();
	}
}
