/**
 * 
 * Author: Christopher Wilt
 *         University of New Hampshire
 *         Artificial Intelligence Research Group
 * 
 */
package org.cwilt.search.utils.basic;
public class NotImplementedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8645271528322929203L;
	@Override
	public String getMessage(){
		StackTraceElement[] s = this.getStackTrace();
		return "Not Implemented Exception\n" + s[0];
	}
}
