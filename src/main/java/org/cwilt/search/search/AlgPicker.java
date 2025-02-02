package org.cwilt.search.search;
import org.cwilt.search.algs.basic.ASEpsilon;import org.cwilt.search.algs.basic.bestfirst.AStar;import org.cwilt.search.algs.basic.bestfirst.AStarAll;import org.cwilt.search.algs.basic.bestfirst.GreedyHighwaterStack;import org.cwilt.search.algs.basic.bestfirst.GreedyPrinter;import org.cwilt.search.algs.basic.bestfirst.Speedy;import org.cwilt.search.algs.basic.bestfirst.UniformCostTracker;import org.cwilt.search.algs.basic.Beam;import org.cwilt.search.algs.basic.BestFirstBeam;import org.cwilt.search.algs.basic.bestfirst.BucketedAStar;import org.cwilt.search.algs.basic.bestfirst.BucketedGreedy;import org.cwilt.search.algs.basic.Bulb;import org.cwilt.search.algs.basic.DBFS;import org.cwilt.search.algs.basic.bestfirst.DCAStar;import org.cwilt.search.algs.basic.bestfirst.DoubleBucketedAStar;import org.cwilt.search.algs.basic.FringeSearch;import org.cwilt.search.algs.basic.FringeSearchCR;import org.cwilt.search.algs.basic.bestfirst.Greedy;import org.cwilt.search.algs.basic.BIDAStarManzini;import org.cwilt.search.algs.basic.EnforcedHillClimbing;import org.cwilt.search.algs.basic.FringeSearchBound;import org.cwilt.search.algs.basic.FringeSearchTiebreaking;import org.cwilt.search.algs.basic.HillClimbing;import org.cwilt.search.algs.basic.IDAStar;import org.cwilt.search.algs.basic.IDAStarCleanup;import org.cwilt.search.algs.basic.IDAStarCleanupAll;//import algs.basic.IDAStarCleanupAll;
import org.cwilt.search.algs.basic.IDAStarClosed;import org.cwilt.search.algs.basic.IDAStarIPM;import org.cwilt.search.algs.basic.RandomHillClimbing;import org.cwilt.search.algs.basic.RecursiveFringeSearch;import org.cwilt.search.algs.basic.PerimeterSearch;import org.cwilt.search.algs.basic.sfbs.SFBSA;import org.cwilt.search.algs.basic.sfbs.SFBSBF;import org.cwilt.search.algs.basic.sfbs.SFBSJ1;import org.cwilt.search.algs.basic.bestfirst.UniformCost;import org.cwilt.search.algs.basic.bestfirst.WAStar;import org.cwilt.search.algs.basic.BreadthFirst;import org.cwilt.search.algs.basic.WIDAStar;import org.cwilt.search.algs.experimental.bidirectional.BHAddAStar;import org.cwilt.search.algs.experimental.bidirectional.BHStarIDAStar;import org.cwilt.search.algs.experimental.bidirectional.BIDAStar;import org.cwilt.search.algs.experimental.DCBeam;import org.cwilt.search.algs.experimental.DCLazyBeam;import org.cwilt.search.algs.experimental.DCWeakBeam;import org.cwilt.search.algs.experimental.bidirectional.DGHAddAstar;import org.cwilt.search.algs.experimental.bidirectional.DHAddAstar;import org.cwilt.search.algs.experimental.DTrieAStar;import org.cwilt.search.algs.experimental.DoubleQueueSearch;import org.cwilt.search.algs.experimental.bidirectional.DHAddAStarSingle;import org.cwilt.search.algs.experimental.bidirectional.DHAddIDAStar;import org.cwilt.search.algs.experimental.bidirectional.GCAStar;import org.cwilt.search.algs.experimental.bidirectional.GCIDAStar;import org.cwilt.search.algs.experimental.bidirectional.GoalBlob;import org.cwilt.search.algs.experimental.bidirectional.HAddAStar;import org.cwilt.search.algs.experimental.bidirectional.HAddAStarOld;import org.cwilt.search.algs.experimental.bidirectional.HAddIDAStar;import org.cwilt.search.algs.experimental.DynamicOpenAStar;import org.cwilt.search.algs.experimental.LazyBeam;import org.cwilt.search.algs.experimental.LazyQueueAStar;import org.cwilt.search.algs.experimental.LazyQueueAStar2;import org.cwilt.search.algs.experimental.RandomProbeBeam;import org.cwilt.search.algs.experimental.RandomWAStar;import org.cwilt.search.algs.experimental.RandomizedBeam;import org.cwilt.search.algs.experimental.StochasticBeam;import org.cwilt.search.algs.experimental.TrieAStar;import org.cwilt.search.algs.experimental.bidirectional.WBHAddAStar;import org.cwilt.search.algs.experimental.WeakBeam;import org.cwilt.search.algs.parallel.ParallelHD;import org.cwilt.search.algs.utils.ReverseEnum;import org.cwilt.search.algs.utils.ReverseGreedy;import org.cwilt.search.algs.utils.ReverseSpeedy;
public class AlgPicker {
	
	public static SearchAlgorithm pickAlg(String alg, String[] params,
			SearchProblem initial, Limit l) {
		SearchAlgorithm a = null;
		if (alg.compareTo("astar") == 0) {
			a = new AStar(initial, l);
		} else if (alg.compareTo("gcastar") == 0) {
			a = new GCAStar(initial, l);
		} else if (alg.compareTo("greedy_hws") == 0) {
			a = new GreedyHighwaterStack(initial, l);
		} else if (alg.compareTo("rev_greedy") == 0) {
			a = new ReverseGreedy(initial, l);
		} else if (alg.compareTo("rev_speedy") == 0) {
			a = new ReverseSpeedy(initial, l);
		} else if (alg.compareTo("revenum") == 0) {
			a = new ReverseEnum(initial, l);
		} else if (alg.compareTo("parallelhd") == 0) {
			a = new ParallelHD(initial, l);
		} else if (alg.compareTo("dynamic_astar") == 0) {
			a = new DynamicOpenAStar(initial, l);
		} else if (alg.compareTo("tau_estimator") == 0) {
			assert (params.length != 0);
			double pKept = Double.parseDouble(params[0]);
			a = new TauEstimator(initial, l, pKept);
		} else if (alg.compareTo("sfbsbf") == 0) {
			a = new SFBSBF(initial, l, false);
		} else if (alg.compareTo("sfbsj1") == 0) {
			a = new SFBSJ1(initial, l, false);
		} else if (alg.compareTo("sfbsa") == 0) {
			a = new SFBSA(initial, l, false);
		} else if (alg.compareTo("sfbsbf_lite") == 0) {
			a = new SFBSBF(initial, l, true);
		} else if (alg.compareTo("sfbsj1_lite") == 0) {
			a = new SFBSJ1(initial, l, true);
		} else if (alg.compareTo("sfbsa_lite") == 0) {
			a = new SFBSA(initial, l, true);
		} else if (alg.compareTo("ucs") == 0) {
			a = new UniformCost(initial, l);
		} else if (alg.compareTo("ucsall") == 0) {
			a = new UniformCost(initial, l);
		} else if (alg.compareTo("ucst") == 0) {
			a = new UniformCostTracker(initial, l);
		} else if (alg.compareTo("dtrieastar") == 0) {
			a = new DTrieAStar(initial, l);
		} else if (alg.compareTo("bhaddastar") == 0) {
			a = new BHAddAStar(initial, l);
		} else if (alg.compareTo("lqastar") == 0) {
			a = new LazyQueueAStar(initial, l);
		} else if (alg.compareTo("lqastarb") == 0) {
			assert (params.length != 0);
			int sz = Integer.parseInt(params[0]);
			a = new LazyQueueAStar(initial, l, sz);
		} else if (alg.compareTo("lqastar2") == 0) {
			a = new LazyQueueAStar2(initial, l);
		} else if (alg.compareTo("lqastar2b") == 0) {
			assert (params.length != 0);
			int sz = Integer.parseInt(params[0]);
			a = new LazyQueueAStar2(initial, l, sz);
		} else if (alg.compareTo("trieastar") == 0) {
			a = new TrieAStar(initial, l);
		} else if (alg.compareTo("dhaddastar") == 0) {
			double weight = Double.parseDouble(params[0]);
			a = new DHAddAstar(initial, l, weight, 1.0);
		} else if (alg.compareTo("dhaddidastar") == 0) {
			double weight = Double.parseDouble(params[0]);
			a = new DHAddIDAStar(initial, l, weight);
		} else if (alg.compareTo("dhaddastar_single") == 0) {
			double ratio = Double.parseDouble(params[0]);
			a = new DHAddAStarSingle(initial, l, ratio);
		} else if (alg.compareTo("wdhaddastar") == 0) {
			double weight = Double.parseDouble(params[0]);
			double ratio = Double.parseDouble(params[1]);
			a = new DHAddAstar(initial, l, ratio, weight);
		} else if (alg.compareTo("dghaddastar") == 0) {
			double weight = Double.parseDouble(params[0]);
			a = new DGHAddAstar(initial, l, weight, 1.0);
		} else if (alg.compareTo("astarall") == 0) {
			a = new AStarAll(initial, l);
		} else if (alg.compareTo("fringe") == 0) {
			a = new FringeSearch(initial, l);
		} else if (alg.compareTo("fringetie") == 0) {
			a = new FringeSearchTiebreaking(initial, l);
		} else if (alg.compareTo("fringe_norec") == 0) {
			a = new RecursiveFringeSearch(initial, l);
		} else if (alg.compareTo("hc") == 0) {
			a = new HillClimbing(initial, l);
		} else if (alg.compareTo("rhc") == 0) {
			a = new RandomHillClimbing(initial, l);
		} else if (alg.compareTo("ehc") == 0) {
			a = new EnforcedHillClimbing(initial, l);
		} else if (alg.compareTo("idastar_cleanup_all") == 0) {
			assert (params != null);
			assert (params.length != 0);
			int iterations = Integer.parseInt(params[0]);
			a = new IDAStarCleanupAll(initial, l, iterations);
		} else if (alg.compareTo("idastar_cleanup") == 0) {
			assert (params != null);
			assert (params.length != 0);
			int iterations = Integer.parseInt(params[0]);
			a = new IDAStarCleanup(initial, l, iterations);
		} else if (alg.compareTo("bucket_greedy") == 0) {
			assert (params.length != 0);
			double bucketSize = Double.parseDouble(params[0]);
			a = new BucketedGreedy(initial, l, bucketSize);
		} else if (alg.compareTo("bucket_astar") == 0) {
			assert (params.length != 0);
			double bucketSize = Double.parseDouble(params[0]);
			a = new BucketedAStar(initial, l, bucketSize);
		} else if (alg.compareTo("fringe_cr") == 0) {
			assert (params.length != 0);
			int nBuckets = Integer.parseInt(params[0]);
			a = new FringeSearchCR(initial, l, nBuckets);
		} else if (alg.compareTo("double_bucket_astar") == 0) {
			assert (params.length != 0);
			double bucketSize = Double.parseDouble(params[0]);
			a = new DoubleBucketedAStar(initial, l, bucketSize);
		} else if (alg.compareTo("dcastar") == 0) {
			a = new DCAStar(initial, l);
		} else if (alg.compareTo("aseps") == 0) {
			assert (params.length != 0);
			double weight = Double.parseDouble(params[0]);
			a = new ASEpsilon(initial, l, weight);
		} else if (alg.compareTo("haddastar") == 0) {
			assert (params.length != 0);
			int cacheSize = Integer.parseInt(params[0]);
			a = new HAddAStar(initial, l, cacheSize);
		} else if (alg.compareTo("haddidastar") == 0) {
			assert (params.length != 0);
			int cacheSize = Integer.parseInt(params[0]);
			a = new HAddIDAStar(initial, l, cacheSize);
		} else if (alg.compareTo("haddastarold") == 0) {
			assert (params.length != 0);
			int cacheSize = Integer.parseInt(params[0]);
			a = new HAddAStarOld(initial, l, cacheSize);
		} else if (alg.compareTo("widastar") == 0) {
			assert (params.length != 0);
			double weight = Double.parseDouble(params[0]);
			a = new WIDAStar(initial, l, weight);
		} else if (alg.compareTo("fringe_bound") == 0) {
			assert (params.length != 0);
			double bound = Double.parseDouble(params[0]);
			a = new FringeSearchBound(initial, l, bound);
		} else if (alg.compareTo("idastar_closed") == 0) {
			assert (params.length != 0);
			double weight = Double.parseDouble(params[0]);
			a = new IDAStarClosed(initial, l, weight);
		} else if (alg.compareTo("bfbeam") == 0) {
			assert (params.length != 0);
			int beamWidth = Integer.parseInt(params[0]);
			a = new BestFirstBeam(initial, l, beamWidth, 1.0);
		} else if (alg.compareTo("perimeter") == 0) {
			assert (params.length != 0);
			int beamWidth = Integer.parseInt(params[0]);
			a = new PerimeterSearch(initial, l, beamWidth);
		} else if (alg.compareTo("wbfbeam") == 0) {
			assert (params.length != 0);
			int beamWidth = Integer.parseInt(params[0]);
			double weight = Double.parseDouble(params[1]);
			a = new BestFirstBeam(initial, l, beamWidth, weight);
		} else if (alg.compareTo("dbfs") == 0) {
			assert (params.length != 0);
			double weight1 = Double.parseDouble(params[0]);
			double weight2 = Double.parseDouble(params[1]);
			a = new DBFS(initial, l, weight1, weight2);
		} else if (alg.compareTo("rwastar") == 0) {
			assert (params.length != 0);
			double weight = Double.parseDouble(params[0]);
			double pRandom = Double.parseDouble(params[1]);
			a = new RandomWAStar(initial, l, weight, pRandom);
		} else if (alg.compareTo("greedy_printer") == 0) {
			assert (params.length != 0);
			String path = params[0];
			a = new GreedyPrinter(initial, l, path);
		} else if (alg.compareTo("bhstaridastar") == 0) {
			assert (params.length != 0);
			int cacheSize = Integer.parseInt(params[0]);
			a = new BHStarIDAStar(initial, l, cacheSize);
		} else if (alg.compareTo("bidastar") == 0) {
			assert (params.length != 0);
			int cacheSize = Integer.parseInt(params[0]);
			a = new BIDAStar(initial, l, cacheSize);
		} else if (alg.compareTo("bidastar_manzini") == 0) {
			assert (params.length != 0);
			int cacheSize = Integer.parseInt(params[0]);
			a = new BIDAStarManzini(initial, l, cacheSize);
		} else if (alg.compareTo("greedy") == 0) {
			a = new Greedy(initial, l);
		} else if (alg.compareTo("speedy") == 0) {
			a = new Speedy(initial, l);
		} else if (alg.compareTo("breadth_first") == 0) {
			a = new BreadthFirst(initial, l);
		} else if (alg.compareTo("tile") == 0) {
			a = new org.cwilt.search.domains.tiles.TileSolver(l, initial);
		} else if (alg.compareTo("idastar") == 0) {
			a = new IDAStar(initial, l);
		} else if (alg.compareTo("idastar_ipm") == 0) {
			a = new IDAStarIPM(initial, l);
		} else if (alg.compareTo("dqs") == 0) {
			a = new DoubleQueueSearch(initial, l, 1.0);
		} else if (alg.compareTo("speedygreedy") == 0) {
			a = new DoubleQueueSearch(initial, l);
		} else if (alg.compareTo("gcidastar") == 0) {
			a = new GCIDAStar(initial, l);
		} else if (alg.compareTo("wdqs") == 0) {
			assert (params.length != 0);
			double weight = Double.parseDouble(params[0]);
			a = new DoubleQueueSearch(initial, l, weight);
		} else if (alg.compareTo("wastar") == 0) {
			assert (params.length != 0);
			double weight = Double.parseDouble(params[0]);
			a = new WAStar(initial, l, weight);
		} else if (alg.compareTo("wbhaddastar") == 0) {
			assert (params.length != 0);
			double weight = Double.parseDouble(params[0]);
			a = new WBHAddAStar(initial, l, weight);
		} else if (alg.compareTo("beam") == 0) {
			assert (params.length != 0);
			int width = Integer.parseInt(params[0]);
			a = new Beam(initial, l, width);
		} else if (alg.compareTo("blob") == 0) {
			assert (params.length != 0);
			int blobSize = Integer.parseInt(params[0]);
			a = new GoalBlob(initial, l, blobSize);
		} else if (alg.compareTo("dcbeam") == 0) {
			assert (params.length != 0);
			int width = Integer.parseInt(params[0]);
			a = new DCBeam(initial, l, width);
		} else if (alg.compareTo("weak_beam") == 0) {
			assert (params.length != 0);
			int width = Integer.parseInt(params[0]);
			a = new WeakBeam(initial, l, width);
		} else if (alg.compareTo("dc_weak_beam") == 0) {
			assert (params.length != 0);
			int width = Integer.parseInt(params[0]);
			a = new DCWeakBeam(initial, l, width);
		} else if (alg.compareTo("random_probe_beam") == 0) {
			assert (params.length != 0);
			int width = Integer.parseInt(params[0]);
			int depth = Integer.parseInt(params[1]);
			a = new RandomProbeBeam(initial, l, width, depth);
		} else if (alg.compareTo("dc_lazy_beam") == 0) {
			assert (params.length != 0);
			int width = Integer.parseInt(params[0]);
			int clsz = Integer.parseInt(params[1]);
			a = new DCLazyBeam(initial, l, width, clsz);
		} else if (alg.compareTo("lazy_beam") == 0) {
			assert (params.length != 0);
			int width = Integer.parseInt(params[0]);
			int clsz = Integer.parseInt(params[1]);
			a = new LazyBeam(initial, l, width, clsz);
		} else if (alg.compareTo("stochastic_beam") == 0) {
			assert (params.length != 0);
			int width = Integer.parseInt(params[0]);
			a = new StochasticBeam(initial, l, width);
		} else if (alg.compareTo("bulb") == 0) {
			assert (params.length >= 1);
			int width = Integer.parseInt(params[0]);
			a = new Bulb(initial, l, width, 0);
		} else if (alg.compareTo("randomized_beam") == 0) {
			assert (params.length == 2);
			int width = Integer.parseInt(params[0]);
			float aggression = Float.parseFloat(params[1]);
			a = new RandomizedBeam(initial, l, width, aggression);
		} 
		if (a == null) {
			System.err.println("failed to select an algorithm - <" + alg + ">");
			System.exit(1);
		}
		return a;
	}
}
