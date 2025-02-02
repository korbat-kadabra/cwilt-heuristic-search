package org.cwilt.search.domains.tiles;
import java.io.Serializable;
import java.util.BitSet;

import org.cwilt.search.domains.tiles.TileState.TileBoard;
public class TileStateAbstractor implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 2449911017758314198L;
    private final BitSet abstractedTiles;
    
    public TileStateAbstractor(BitSet abstractedTiles) {
        //abstractedTiles = new BitSet(numTiles);
        //abstractedTiles.clear(0, numTiles);
        this.abstractedTiles = abstractedTiles;
    }
    
    public TileState abstractState(TileState ts) {        
        TileBoard board = ts.b;
        char[] c = board.c;
        
        char[] abstractedC = new char[c.length];
        
        for (int i = 0; i < c.length; i++) {
            if (isAbstracted(c[i])) {
                abstractedC[i] = 255;
            } else {
                abstractedC[i] = c[i];
            }
        }
        
        TileBoard abstractedBoard = new TileBoard(abstractedC); 
        
        return new TileState(ts.prob, abstractedBoard);
    }
    
    public boolean isAbstracted(int id) {
        return abstractedTiles.get(id);
    }
    
    public void abstractTile(int id){
        abstractedTiles.set(id);
    }
    
    public void refineTile(int id){
        abstractedTiles.clear(id);
    }
    
    public static void main(String [] args) {
        TileProblem tp = new TileProblem(4, 4, null);
        TileState ts = (TileState) tp.getInitial();
       
        System.out.println(ts);
        
        BitSet b = new BitSet(16);
        b.clear(0, 16);
        b.set(4);
        b.set(8);
        b.set(12);
                
        TileStateAbstractor tsa = new TileStateAbstractor(b);
        
        TileState abstractedTs = tsa.abstractState(ts);
        
        System.out.println(abstractedTs);
    }
}
