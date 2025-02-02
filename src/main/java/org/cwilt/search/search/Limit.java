/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.search;

import java.io.Serializable;

import org.apache.logging.log4j.LogManager;

public class Limit implements Cloneable, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5928961729204654776L;
	private boolean outOfMemory;

	public boolean getOutOfMemory() {
		return outOfMemory;
	}

	public void resetRestartFlags() {
		this.stop = false;
		this.outOfMemory = false;
	}

	public String toString(){
		StringBuffer b = new StringBuffer();
		
		b.append("expanded ");
		b.append(expansions);
		b.append("\ngenerated ");
		b.append(generations);
		
		return b.toString();
	}
	
	public synchronized void synchronizedIncrExp(){
		expansions++;
	}
	public synchronized void synchronizedIncrDup(){
		duplicates++;
	}
	public synchronized void synchronizedIncrGen(int amt){
		this.generations += amt;
	}
	
	public Limit clone() {
		Limit l = new Limit(max_duration, max_expansions, max_generations, memcheck, this.maxMemoryPercent);
		l.duration = this.duration;
		l.expansions = this.expansions;
		l.generations = this.generations;
		l.startTime = this.startTime;
		l.duplicates = this.duplicates;
		l.reExpansions = this.reExpansions;
		l.stop = this.stop;
		l.outOfMemory = this.outOfMemory;
		return l;
	}

	private boolean stop = false;
	private long startTime;
	private long duration;
	private long reExpansions;
	private long expansions;
	private long generations;
	private long duplicates;
	private final boolean memcheck;
	
	private final long max_duration;
	private final long max_expansions;
	private final long max_generations;


	public Limit(long max_duration, long max_expansions, long max_generations, boolean memcheck, double maxMemoryPercent) {
		this.maxMemoryPercent = maxMemoryPercent;
		this.max_duration = max_duration;
		this.max_expansions = max_expansions;
		this.max_generations = max_generations;
		this.duration = 0;
		this.expansions = 0;
		this.generations = 0;
		this.reExpansions = 0;
		this.startTime = 0;
		this.duplicates = 0;
		this.stop = false;
		this.outOfMemory = false;
		this.memcheck = memcheck;
	}
	
	public static final double DEFAULT_MAX_MEMORY = 0.95;
	public Limit(long max_duration, long max_expansions, long max_generations, boolean memcheck) {
		this(max_duration, max_expansions, max_generations, memcheck, DEFAULT_MAX_MEMORY);
	}
	
	private final double maxMemoryPercent;

	public Limit() {
		this.maxMemoryPercent = 0.95;
		this.max_duration = Long.MAX_VALUE;
		this.max_expansions = Long.MAX_VALUE;
		this.max_generations = Long.MAX_VALUE;
		this.duration = 0;
		this.expansions = 0;
		this.reExpansions = 0;
		this.generations = 0;
		this.startTime = 0;
		this.duplicates = 0;
		this.stop = false;
		this.outOfMemory = false;
		this.memcheck = false;
	}

	public long getDuplicates() {
		return duplicates;
	}

	public void resetNodes() {
		expansions = 0L;
		generations = 0L;
		duplicates = 0L;
		reExpansions = 0L;
	}

	public void incrDup() {
		duplicates++;
	}

	public void incrDup(int i) {
		duplicates += i;
	}

	public void startClock() {
		if(startTime == 0)
			startTime = System.currentTimeMillis();
	}

	public void endClock() {
		if (startTime != 0)
			duration += System.currentTimeMillis() - startTime;
		startTime = 0;
	}

	public void incrExp() {
		expansions++;
	}

	public Limit childLimit(long expLimit) {
		long remTime = (max_duration - (System.currentTimeMillis() - startTime));
		long remExp = Math.min(expLimit, max_expansions - expansions);
		long remGen = max_generations - generations;
		return new Limit(remTime, remExp, remGen, memcheck, maxMemoryPercent);
	}

	public Limit childLimit() {
		long remTime = (max_duration - (System.currentTimeMillis() - startTime));
		long remExp = max_expansions - expansions;
		long remGen = max_generations - generations;
		return new Limit(remTime, remExp, remGen, memcheck, maxMemoryPercent);
	}

	
	public void incrGen() {
		generations++;
	}

	public void incrExp(int i) {
		expansions += i;
	}

	public void incrGen(int i) {
		generations += i;
	}

	public boolean keepGoingNoMem() {
		// if(System.currentTimeMillis() - startTime > max_duration){
		//			
		// System.err.printf("time %d start %d max %d\n",
		// System.currentTimeMillis(), startTime, max_duration);
		// }
		// if(stop)
		// System.err.println("stop");

		boolean toReturn = expansions < max_expansions
				&& generations < max_generations
				&& System.currentTimeMillis() - startTime < max_duration
				&& !stop;
		return toReturn;
	}

	public static boolean memoryCheck(double maxMemoryPercent) {
		long freeMem = Runtime.getRuntime().freeMemory();
		long maxMem = Runtime.getRuntime().maxMemory();
//		long totalMem = Runtime.getRuntime().totalMemory();
		double percent = (double) freeMem / (double) maxMem;
		boolean toReturn = percent > maxMemoryPercent;
		LogManager.getLogger().trace("Returning {} max memory: {} percent occupied: {}", toReturn, maxMem, percent);
		//		System.err.println(fptm + " -> " + freeMem);
		return toReturn;
	}

	public boolean keepGoing() {
		if(Thread.currentThread().isInterrupted()){
			return false;
		}
		
		boolean oom = false;
		if(memcheck && memoryCheck(this.maxMemoryPercent)){
			oom = true;
			this.outOfMemory = true;
		}
		return keepGoingNoMem() && !oom;
	}

	public void addTo(Limit l) {
		this.duplicates += l.duplicates;
		l.duplicates = 0;
		this.expansions += l.expansions;
		l.expansions = 0;
		this.generations += l.generations;
		l.generations = 0;
		this.reExpansions += l.reExpansions;
		l.reExpansions = 0;
		this.duration += l.duration;
		l.duration = 0;
		if (l.outOfMemory)
			this.outOfMemory = l.outOfMemory;
	}

	public long getDuration() {
		if (startTime == 0)
			return duration;
		return duration + System.currentTimeMillis() - startTime;
	}

	public long getExpansions() {
		return expansions;
	}

	public long getGenerations() {
		return generations;
	}

	public void stop() {
		stop = true;
	}

	public long getReExpansion(){
		return this.reExpansions;
	}
	
	public void incrReExp(){
		reExpansions ++;
	}
	
	public void setOutOfMemory() {
		outOfMemory = true;
	}
}
