package org.cwilt.search.domains.double_topspin;
import java.util.ArrayList;

import org.cwilt.search.search.SearchState;
public class DoubleTopspinState extends SearchState implements Cloneable{
	private enum SIDE {LEFT, RIGHT};
	private enum COLOR {WHITE, YELLOW, BROWN};
	private static final class Token implements Cloneable{
		public Token clone(){
			return new Token(this.color, this.size);
		}
		
		private COLOR color;
		private int size;
		public Token(COLOR c, int s){
			if(s != 1 && s != 2){
				throw new RuntimeException("Invalid value");
			}
			this.color = c;
			this.size = s;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((color == null) ? 0 : color.hashCode());
			result = prime * result + size;
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
			Token other = (Token) obj;
			if (color != other.color)
				return false;
			if (size != other.size)
				return false;
			return true;
		}
	}
	private final Token[] left;
	private final Token[] right;
	private final Token[] turnstile;
	
	public DoubleTopspinState(SIDE s, DoubleTopspinState parent){
		Token[] newLeft = parent.left.clone();
		Token[] newRight = parent.right.clone();
		Token[] newTurnstile = parent.turnstile.clone();
		this.side = s;
		this.left = newLeft;
		this.right = newRight;
		this.turnstile = newTurnstile;
	}
	
	private final SIDE side;
	
	private static final int TURNSTILE_SIZE = 3;
	private static final int RING_SIZE = 9;

	public DoubleTopspinState(DoubleTopspinProblem p) {
		this.left = new Token[TURNSTILE_SIZE];
		this.right = new Token[TURNSTILE_SIZE];
		this.turnstile = new Token[RING_SIZE];
		
		this.turnstile[0] = new Token(COLOR.WHITE, 1);
		this.turnstile[1] = new Token(COLOR.WHITE, 2);
		this.turnstile[2] = new Token(COLOR.WHITE, 1);
		
		this.left[0] = new Token(COLOR.BROWN, 2);
		this.left[1] = new Token(COLOR.BROWN, 1);
		this.left[2] = new Token(COLOR.BROWN, 2);
		this.left[3] = new Token(COLOR.WHITE, 1);
		this.left[4] = new Token(COLOR.WHITE, 2);
		this.left[5] = new Token(COLOR.WHITE, 1);
		this.left[6] = new Token(COLOR.BROWN, 2);
		this.left[7] = new Token(COLOR.BROWN, 1);
		this.left[8] = new Token(COLOR.BROWN, 2);
		
		this.right[0] = new Token(COLOR.YELLOW, 2);
		this.right[1] = new Token(COLOR.YELLOW, 1);
		this.right[2] = new Token(COLOR.YELLOW, 2);
		this.right[3] = new Token(COLOR.WHITE, 1);
		this.right[4] = new Token(COLOR.WHITE, 2);
		this.right[5] = new Token(COLOR.WHITE, 1);
		this.right[6] = new Token(COLOR.YELLOW, 2);
		this.right[7] = new Token(COLOR.YELLOW, 1);
		this.right[8] = new Token(COLOR.YELLOW, 2);

		this.side = SIDE.LEFT;
	}
	
	private final double SWITCH_COST = 1.0d;
	
	private final DoubleTopspinState switchSide() {
		if(this.side == SIDE.LEFT){
			return new DoubleTopspinState(SIDE.RIGHT, this);
		} else {
			return new DoubleTopspinState(SIDE.LEFT, this);
		}
	}
	
	@SuppressWarnings("unused")
	private final DoubleTopspinState rotateLeft(int count) {
		//can only rotate left if the turnstile is on the left
		assert(this.side == SIDE.LEFT);
		
		DoubleTopspinState toReturn = new DoubleTopspinState(this.side, this);
		
		ArrayList<Token> tokens = new ArrayList<Token>(TURNSTILE_SIZE + RING_SIZE);
		for(int i = 0; i < TURNSTILE_SIZE; i++){
			tokens.add(turnstile[i]);
		}
		for(int i = 0; i < RING_SIZE; i++){
			tokens.add(left[i]);
		}
		
		ArrayList<Token> finalTokens = new ArrayList<Token>(TURNSTILE_SIZE + RING_SIZE);
		for(int i = 1; i < TURNSTILE_SIZE + RING_SIZE; i++){
			finalTokens.add(tokens.get((i + count) % (TURNSTILE_SIZE + RING_SIZE)));
		}
		
		for(int i = 0; i < TURNSTILE_SIZE; i++){
			toReturn.turnstile[i] = finalTokens.get(i);
		}
		
		for(int i = 0; i < RING_SIZE; i++){
			toReturn.left[i] = finalTokens.get(i +  + TURNSTILE_SIZE);
		}
		
		return toReturn;
	}
	
	@Override
	public ArrayList<Child> expand() {
		ArrayList<Child> children = new ArrayList<Child>();
		children.add(new Child(switchSide(), SWITCH_COST));
		
		// TODO Auto-generated method stub
		return children;
	}

	@Override
	public ArrayList<Child> reverseExpand() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double h() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int d() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isGoal() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean equals(Object other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int lexOrder(SearchState s) {
		// TODO Auto-generated method stub
		return 0;
	}

}
