/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.utils.basic;
import java.util.Random;

public class R250_521 extends Random {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int r250_index;
    private int r521_index;
    private int[] r250_buffer = new int[250];
    private int[] r521_buffer = new int[521];

    public R250_521() {
        java.util.Random r = new java.util.Random();
        int i = 521;
        int mask1 = 1;
        int mask2 = 0xFFFFFFFF;
	
        while (i-- > 250) {
            r521_buffer[i] = r.nextInt();
        }
        while (i-- > 31) {
            r250_buffer[i] = r.nextInt();
            r521_buffer[i] = r.nextInt();
        }
    
        /*
        Establish linear independence of the bit columns
        by setting the diagonal bits and clearing all bits above
        */
        while (i-- > 0) {
            r250_buffer[i] = (r.nextInt() | mask1) & mask2;
            r521_buffer[i] = (r.nextInt() | mask1) & mask2;
            mask2 ^= mask1;
            mask1 >>= 1;
        }
        r250_buffer[0] = mask1;
        r521_buffer[0] = mask2;
        r250_index = 0;
        r521_index = 0;
    }
	
    protected int next() {
        int i1 = r250_index;
        int i2 = r521_index;
    
        int j1 = i1 - (250-103);
        if (j1 < 0)
            j1 = i1 + 103;
        int j2 = i2 - (521-168);
        if (j2 < 0)
            j2 = i2 + 168;
    
        int r = r250_buffer[j1] ^ r250_buffer[i1];
        r250_buffer[i1] = r;
        int s = r521_buffer[j2] ^ r521_buffer[i2];
        r521_buffer[i2] = s;
    
        i1 = (i1 != 249) ? (i1 + 1) : 0;
        r250_index = i1;
        i2 = (i2 != 521) ? (i2 + 1) : 0;
        r521_index = i2;
        
        return r ^ s;
    }
}
