package org.cwilt.search.algs.basic.bestfirst;
import org.cwilt.search.search.Limit;import org.cwilt.search.search.SearchAlgorithm;import org.cwilt.search.search.SearchProblem;import org.cwilt.search.utils.basic.AbstractBucketQueue;import org.cwilt.search.utils.basic.BucketPriorityQueue;
public class DoubleBucketedAStar extends BucketedBestFirst {

	public DoubleBucketedAStar(SearchProblem prob, Limit l, double bucketSize) {
		super(prob, l, bucketSize);
	}

	@Override
	public SearchAlgorithm clone() {
		return new DoubleBucketedAStar(prob, l.clone(), bucketSize);
	}

	@Override
	public AbstractBucketQueue makeOpen() {
		return new org.cwilt.search.utils.basic.FGBucketPriorityQueue(
				new BucketPriorityQueue.FBucketer(bucketSize),
				new BucketPriorityQueue.HBucketer(bucketSize));
	}

}
