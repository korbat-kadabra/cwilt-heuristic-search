package org.cwilt.search.domains.tiles;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;

import org.cwilt.search.search.SearchNodeDepth;
public class TilesPDB extends org.cwilt.search.search.PDB {
    private final TileStateAbstractor abstractor;

    protected TilesPDB(HashMap<Object, PDBValue> p, TileState initial,
            TileStateAbstractor a) {
        super(p, SearchNodeDepth.makeInitial(a.abstractState(initial)));
        // TODO Auto-generated constructor stub
        this.abstractor = a;
    }

    /**
     * 
     */
    private static final long serialVersionUID = -5908877951542963542L;

    @Override
    protected ArrayList<SearchNodeDepth> makeGoals(SearchNodeDepth canonicalGoal) {
        ArrayList<SearchNodeDepth> toReturn = new ArrayList<SearchNodeDepth>();
        toReturn.add(canonicalGoal);
        return toReturn;
    }

    public double calculateH(TileState s) {
        TileState abstractedState = abstractor.abstractState(s);
        return super.getH(abstractedState.getKey());
    }

    public int calculateD(TileState s) {
        TileState abstractedState = abstractor.abstractState(s);
        return super.getD(abstractedState.getKey());
    }

    public void writePDB(String filename) {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            fos = new FileOutputStream(filename);
            out = new ObjectOutputStream(fos);
            out.writeObject(this);
            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }finally {
    		try {
    			if(fos != null) {
        			fos.close();
    			}
			} catch (IOException e) {
				e.printStackTrace();
			}
        	try {
        		if(out != null) {
    				out.close();
        		}
			} catch (IOException e) {
				e.printStackTrace();
			}

        }
    }

    public static TilesPDB readPDB(String filename)
            throws ClassNotFoundException {
        TilesPDB p = null;
        FileInputStream fis = null;
        ObjectInputStream in = null;
        try {
            fis = new FileInputStream(filename);
            in = new ObjectInputStream(fis);
            p = (TilesPDB) in.readObject();
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
        	try {
        		if(in != null) {
    				in.close();
        		}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		try {
    			if(fis != null) {
        			fis.close();
    			}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        }
        return p;
    }

    public static String buildFilename(String path, int across, int down,
            int[] keptTiles) {
        StringBuffer sb = new StringBuffer();

        sb.append(path);

        if (!path.endsWith(File.separator)) {
        	//just use the path as it is
        } else {
            sb.append(across);
            sb.append("x");
            sb.append(down);

            for (int i = 0; i < keptTiles.length; i++) {
                sb.append("_");
                sb.append(keptTiles[i]);
            }
        	
        }


        return sb.toString();
    }

    public static void buildPDB(String path, int across, int down,
            int[] keptTiles, String cost) {
        Arrays.sort(keptTiles);
        
        String filename = buildFilename(path, across, down, keptTiles);

        TileProblem tp = new TileProblem(across, down, cost);
        TileState initial = (TileState) tp.getInitial();

        BitSet b = new BitSet(across * down);
        b.set(0, across * down, true);

        TileStateAbstractor tsa = new TileStateAbstractor(b);

        for (int i = 0; i < keptTiles.length; i++) {
            tsa.refineTile(keptTiles[i]);
        }
        
        tsa.refineTile(0);
        
        TilesPDB pdb = new TilesPDB(new HashMap<Object, PDBValue>(), initial,
                tsa);
        pdb.writePDB(filename);
    }

    public static void main(String[] args) {
        // 0 File path - String
    	// 1 cost function to use
        // 2 Across - int
        // 3 Down - int
        // 4... Kept tiles - int list

        try {
            String path = args[0];
            String cost = args[1];
            
            int across = Integer.parseInt(args[2]);
            int down = Integer.parseInt(args[3]);

            int numberKept = args.length - 4;
            int[] keptTiles = new int[numberKept];

            StringBuffer p = new StringBuffer();
            p.append(path);
            p.append("/");
            p.append(across);
            p.append("x");
            p.append(down);
            for (int i = 0; i < numberKept; i++) {
                keptTiles[i] = Integer.parseInt(args[4 + i]);
                p.append("_");
                p.append(keptTiles[i]);
            }
            path = p.toString();
            
            buildPDB(path, across, down, keptTiles, cost);
        } catch (ArrayIndexOutOfBoundsException aex) {
            System.err.println("Not enough command line arguments provided.");
            aex.printStackTrace();
        } catch (NumberFormatException nex) {
            System.err.println("Expected an integer argument.");
            nex.printStackTrace();
        } catch (Exception ex) {
            System.err.println("Encountered problem while writing PDB.");
            ex.printStackTrace();
        }
    }
}
