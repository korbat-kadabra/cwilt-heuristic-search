package org.cwilt.search.domains.topspin;
public class OperatorPair {
	private final int first, second;
	public OperatorPair(int first, int second){
		this.first = first;
		this.second = second;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + first;
		result = prime * result + second;
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
		OperatorPair other = (OperatorPair) obj;
		if (first != other.first)
			return false;
		if (second != other.second)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "OperatorPair [first=" + first + ", second=" + second + "]";
	}
	
	
	


}
