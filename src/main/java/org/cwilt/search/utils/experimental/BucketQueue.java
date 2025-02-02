package org.cwilt.search.utils.experimental;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.cwilt.search.search.SearchNode;
public class BucketQueue {
	private final double bucketSize;
	private final ArrayList<ArrayList<ArrayList<SearchNode>>> nodes;
	private final Random r;
	private final double paramP, paramT;
	private int currentCount;

	public BucketQueue(double bucketSize, double paramP, double paramT, int seed) {
		this.bucketSize = bucketSize;
		this.nodes = new ArrayList<ArrayList<ArrayList<SearchNode>>>();
		minH = minG = Integer.MAX_VALUE;
		this.paramP = paramP;
		this.paramT = paramT;
		maxH = maxG = 0;
		this.r = new Random(seed);
	}

	int minH, maxH, minG, maxG;

	public void addAll(Collection<SearchNode> c) {
		for (SearchNode n : c) {
			addNode(n);
		}
	}

	public void addNode(SearchNode n) {
		currentCount++;
		int gIX = (int) (n.getG() / bucketSize);
		int hIX = (int) (n.getH() / bucketSize);
		if (nodes.size() <= gIX) {
			nodes.ensureCapacity(gIX + 1);
			for (int i = nodes.size(); i < gIX + 1; i++) {
				nodes.add(null);
			}
		}
		ArrayList<ArrayList<SearchNode>> gIXArray = nodes.get(gIX);
		if (gIXArray == null) {
			nodes.set(gIX, new ArrayList<ArrayList<SearchNode>>());
			gIXArray = nodes.get(gIX);
		}
		if (gIXArray.size() <= hIX) {
			gIXArray.ensureCapacity(hIX + 1);
			for (int i = gIXArray.size(); i < hIX + 1; i++) {
				gIXArray.add(null);
			}
		}
		assert(hIX >= 0);
		ArrayList<SearchNode> hIXArray = gIXArray.get(hIX);
		if (hIXArray == null) {
			gIXArray.set(hIX, new ArrayList<SearchNode>());
			hIXArray = gIXArray.get(hIX);
		}
		hIXArray.add(n);
		if (hIX < minH)
			minH = hIX;
		if (hIX > maxH)
			maxH = hIX;
		if (gIX < minG)
			minG = gIX;
		if (gIX > maxG)
			maxG = gIX;
	}

	public void clear() {
		nodes.clear();
	}

	public boolean isEmpty() {
		return currentCount == 0;
	}
	private void fixIndices(){
		int newMinG = Integer.MAX_VALUE;
		int newMinH = Integer.MAX_VALUE;
		int newMaxH = 0;
		int newMaxG = 0;
		for (int i = minG; i <= maxG; i++) {
			for (int j = minH; j <= maxH; j++) {
//				System.err.printf("%d %d\n", i, j);
				if (nodes.get(i) == null) {
				} else if (nodes.get(i).size() <= j) {
				} else if (nodes.get(i).get(j) == null) {
				} else if (nodes.get(i).get(j).isEmpty()) {
				} else {
					if(i < newMinG)
						newMinG = i;
					if(j < newMinH)
						newMinH = j;
					if(i > newMaxG)
						newMaxG = i;
					if(j > newMaxH)
						newMaxH = j;
				}
			}
		}
		minG = newMinG;
		minH = newMinH;
		maxG = newMaxG;
		maxH = newMaxH;
	}
	
	public SearchNode fetchOneNode() {
		/*
		 * the max/min for g and h have to go up and down as things get added and deleted.
		 * */
		assert (currentCount > 0);
//		if(currentCount != count()){
//			System.err.printf("current count %d calculated count %d\n", currentCount, count());
//			assert(false);
//		}
		currentCount--;
		double pTotal = 0;
		double gMaxReturn;
		double gMaxReturnTest = r.nextDouble();
		if (gMaxReturnTest < paramP) {
			int diff = maxG - minG;
			if(diff > 0)
				gMaxReturn = r.nextInt(maxG - minG) + minG;
			else
				gMaxReturn = minG;
		} else
			gMaxReturn = maxG;

		for (int i = minG; i <= maxG; i++) {
			if (i * bucketSize > gMaxReturn)
				continue;
			for (int j = minH; j <= maxH; j++) {
				if (nodes.get(i) == null) {
				} else if (nodes.get(i).size() <= j) {
				} else if (nodes.get(i).get(j) == null) {
				} else if (nodes.get(i).get(j).isEmpty()) {
				} else {
					pTotal += Math.pow(paramT, ((j - minH) * bucketSize));
				}
			}
		}
		double randPick = r.nextDouble() * pTotal;

		ArrayList<SearchNode> lastList = null;

		double currentTotal = 0;
		for (int i = minG; i <= maxG; i++) {
			if (i * bucketSize > gMaxReturn)
				continue;
			for (int j = minH; j <= maxH; j++) {
				if (nodes.get(i) == null) {
				} else if (nodes.get(i).size() <= j) {
				} else if (nodes.get(i).get(j) == null) {
				} else if (nodes.get(i).get(j).isEmpty()) {
				} else {
					currentTotal += Math.pow(paramT, ((j - minH) * bucketSize));
					lastList = nodes.get(i).get(j);
					if (currentTotal > randPick) {
						assert (nodes.get(i).get(j).size() > 0);
						SearchNode toReturn = nodes.get(i).get(j)
								.remove(nodes.get(i).get(j).size() - 1);
						if(nodes.get(i).get(j).isEmpty()){
							nodes.get(i).set(j, null);
							fixIndices();
						}
						return toReturn;
					}
				}
			}
		}
		if(lastList == null){
			System.err.println(count() + " " + currentCount + " " + currentTotal + " " + randPick + " " + pTotal);
			assert (lastList != null);
		}
		return lastList.remove(lastList.size() - 1);
	}

	private int count() {
		int count = 0;
		for (int i = minG; i <= maxG; i++) {
			if(nodes.size() <= i)
				break;
			for (int j = minH; j <= maxH; j++) {
				if (nodes.get(i) == null) {
				} else if (nodes.get(i).size() <= j) {
				} else if (nodes.get(i).get(j) == null) {
				} else if (nodes.get(i).get(j).isEmpty()) {
				} else {
					count += nodes.get(i).get(j).size();
				}
			}
		}
		return count;
	}
}
