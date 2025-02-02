package org.cwilt.search.domains.pancake;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;
import java.util.Scanner;

import org.cwilt.search.search.SearchState;
public class PancakeState extends SearchState {

	static final short ABSTRACTED = -1;
	public PancakeState abstractState(BitSet b){
		short[] newCakes = new short[stack.cakes.length];
		for(int i = 0; i < stack.cakes.length; i++){
			
			if(b.get(stack.cakes[i])){
				newCakes[i] = ABSTRACTED;
			} else {
				newCakes[i] = this.stack.cakes[i];
			}
		}
		PancakeStack newStack = new PancakeStack(newCakes);
		return new PancakeState(this.problem, newStack);
	}
	
	private static class PancakeStack implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 3110899113569771857L;
		public final short[] cakes;
		public PancakeStack(short[] cakes){
			this.cakes = cakes;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(cakes);
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
			PancakeStack other = (PancakeStack) obj;
			if (!Arrays.equals(cakes, other.cakes))
				return false;
			return true;
		}
		public String toString(){
			return Arrays.toString(cakes);
		}
	}
	
	public String toString(){
		return stack.toString() + " h = " + h();
	}
	
	
	
	private final PancakeStack stack;
	private final PancakeProblem problem;
	
	public PancakeState(Random r, PancakeProblem p, int nCakes){
		this.problem = p;
		short[] cakes = new short[nCakes];
		for(short i = 0; i < nCakes; i++){
			cakes[i] = i;
		}
		for(short i = 0; i < nCakes; i++){
			int nextIX = i + r.nextInt(nCakes - i);
			short temp = cakes[i];
			cakes[i] = cakes[nextIX];
			cakes[nextIX] = temp;
		}
		this.stack = new PancakeStack(cakes);
	}
	
	public PancakeState(String path, PancakeProblem p) throws IOException{
		this.problem = p;

		FileInputStream fs = new FileInputStream(path);
		DataInputStream ds = new DataInputStream(fs);
		BufferedReader br = new BufferedReader(new InputStreamReader(ds));
		Scanner s = new Scanner(br);
		int size = s.nextInt();
		short[] cakes = new short[size];
		
		for(int i = 0; i < size; i++){
			cakes[i] = s.nextShort();
		}
		this.stack = new PancakeStack(cakes);
		br.close();
		s.close();
	}
	
	
	public PancakeState(int nCakes, PancakeProblem p){
		this.problem = p;
		short[] cakes = new short[nCakes];
		for(short i = 0; i < nCakes; i++){
			cakes[i] = i;
		}
		this.stack = new PancakeStack(cakes);
	}
	
	public int getNCakes(){
		return stack.cakes.length;
	}
	
	private short[] swap(int ix){
		short[] toReturn = new short[stack.cakes.length];
		for(int i = 0; i < stack.cakes.length - ix; i++){
			toReturn[i] = stack.cakes[i];
		}
		for(int i = 0; i < ix; i++){
			toReturn[stack.cakes.length - ix + i] = stack.cakes[stack.cakes.length - 1 - i];
		}
		return toReturn;
	}

	
	private void inplaceSwap(int ix){
		for(int i = 0; i < (stack.cakes.length - ix) / 2; i++){
			int highIndex = stack.cakes.length - 1 - i;
			short high = stack.cakes[highIndex];
			
			int lowIndex = ix + i;
			short low = stack.cakes[lowIndex];
			
			stack.cakes[highIndex] = low;
			stack.cakes[lowIndex] = high;
		}
	}

	
	private PancakeState(PancakeProblem prob, PancakeStack s){
		this.stack = s;
		this.problem = prob;
	}
	
	
	@Override
	public ArrayList<Child> expand() {
		ArrayList<Child> children = new ArrayList<Child>();
		
		for(int i = 1; i < stack.cakes.length; i++){
			short[] childArray = swap(i + 1);
			PancakeState s = new PancakeState(this.problem, new PancakeStack(childArray));
			children.add(new Child(s, problem.priceSwap(childArray, i)));
		}
		
		return children;
	}

	@Override
	public double h() {
		return problem.calculateH(this);
	}

	@Override
	public int d() {
		return problem.calculateD(this);
	}

	@Override
	public double distTo(SearchState other){
		PancakeState o = (PancakeState) other;
		
		double gap = 0;

		//if they don't have the same cake on the bottom, at least one flip will have to be done.
		if(stack.cakes[0] != o.stack.cakes[0])
			gap ++;
		
		short[] cakeLocations = new short[stack.cakes.length];
		for(short i = 0; i < stack.cakes.length; i++){
			cakeLocations[o.stack.cakes[i]] = i;
		}
		for(int i = 1; i < stack.cakes.length; i++){
			short lowerCake = stack.cakes[i-1];
			short upperCake = stack.cakes[i];
			short lowerIX = cakeLocations[lowerCake];
			short upperIX = cakeLocations[upperCake];
			int diff = lowerIX - upperIX;
			if(Math.abs(diff) != 1){
				gap ++;
			}
		}
		
		return gap;
	}
	
	public double gap(){
		double gapH = 0.0;
		if(stack.cakes[0] != 0){
			gapH ++;
		}
		for(int i = 1; i < stack.cakes.length; i++){
			if(stack.cakes[i-1] == stack.cakes[i] + 1)
				continue;
			else if(stack.cakes[i-1] == stack.cakes[i] - 1)
				continue;
			gapH ++;
		}
		return gapH;
	}
	
	@Override
	public boolean isGoal() {
		for(short i = 0; i < stack.cakes.length; i++){
			if(stack.cakes[i] != i)
				return false;
		}
		return true;
	}

	@Override
	public Object getKey() {
		return stack;
	}



	@Override
	public int lexOrder(SearchState s) {
		throw new org.cwilt.search.utils.basic.NotImplementedException();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stack == null) ? 0 : stack.hashCode());
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
		PancakeState other = (PancakeState) obj;
		if (stack == null) {
			if (other.stack != null)
				return false;
		} else if (!stack.equals(other.stack))
			return false;
		return true;
	}

	
	public static void main(String[] args) throws FileNotFoundException{
		PancakeProblem p = new PancakeProblem(10, PancakeProblem.COST.SUM);
		PancakeState s = p.getInitial();
		System.err.println(s);
		System.err.println();
		
		ArrayList<Child> children = s.expand();
		for(Child c : children){
			System.err.println(c.child);
			System.err.println("costing " + c.transitionCost);
		}
	}

	@Override
	public ArrayList<Child> reverseExpand() {
		ArrayList<Child> children = new ArrayList<Child>();
		
		for(int i = 1; i < stack.cakes.length; i++){
			short[] childArray = swap(i + 1);
			PancakeState s = new PancakeState(this.problem, new PancakeStack(childArray));
			children.add(new Child(s, problem.priceSwap(childArray, i)));
		}
		return children;
	}

	
	@Override
	public int nChildren(){
		return stack.cakes.length - 2;
	}
	@Override
	public int inverseChild(int index){
		//in this domain each operator is its own inverse
		return index;
	}
	
	
	@Override
	public double convertToChild(int child, int parent){
		if(parent == child)
			return -1;
		inplaceSwap(child);
		
		return problem.priceSwap(this.stack.cakes, child);
	}
}
