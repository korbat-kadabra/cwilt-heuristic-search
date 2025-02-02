package org.cwilt.search.domains.hanoi;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;

public class DisjointHanoiPDB implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 91416524412811823L;

	public String toString() {
		StringBuffer b = new StringBuffer();
		for (HanoiPDB p : pdbs) {
			b.append(p);
			b.append("\n");
		}
		return b.toString();
	}

	public DisjointHanoiPDB(double[] costs, int[] sizes, boolean[] skipped)
			throws ClassNotFoundException {
		this.pdbs = new HanoiPDB[sizes.length];
		this.abs = new HanoiAbstraction[sizes.length];
		int bottomDisk = 0;
		int index = 0;
		String[] pdbArgs = new String[0];

		for (int current : sizes) {
			if (!skipped[index]) {
				abs[index] = new HanoiAbstraction(bottomDisk, current);
				double[] hereCosts = Arrays.copyOfRange(costs, bottomDisk,
						bottomDisk + current);
				HanoiProblem g = new HanoiProblem(4, current, hereCosts,
						pdbArgs);
				pdbs[index] = new HanoiPDB(g);
			}
			index++;
			bottomDisk += current;
		}
	}

	public static void main(String[] args) throws ClassNotFoundException {
		String filename = args[0];
		String costString = args[1];
		int nDisks = 0;
		int[] sizes = new int[args.length - 2];
		boolean[] skipped = new boolean[args.length - 2];
		for (int i = 2; i < args.length; i++) {
			if (args[i].charAt(0) == 's') {
				sizes[i - 2] = Integer.parseInt(args[i].substring(1));
				nDisks += sizes[i - 2];
				skipped[i - 2] = true;
			} else {
				sizes[i - 2] = Integer.parseInt(args[i]);
				nDisks += sizes[i - 2];
				skipped[i - 2] = false;
			}
		}

		DisjointHanoiPDB p = new DisjointHanoiPDB(HanoiProblem.initCost(
				costString, nDisks), sizes, skipped);
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(p);
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

	protected final HanoiPDB[] pdbs;
	protected final HanoiAbstraction[] abs;

	public double getH(HanoiState hanoiState) {
		double h = 0;
		for (int i = 0; i < pdbs.length; i++) {
			if (abs[i] == null)
				continue;
			Object abstractedState = abs[i].abstractState(hanoiState);
			h += pdbs[i].getH(abstractedState);
		}

		return h;
	}

	public int getD(HanoiState hanoiState) {
		int h = 0;
		for (int i = 0; i < pdbs.length; i++) {
			if (abs[i] == null)
				continue;
			Object abstractedState = abs[i].abstractState(hanoiState);
			h += pdbs[i].getD(abstractedState);
		}
		return h;
	}
}
