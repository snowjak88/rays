/**
 * 
 */
package org.snowjak.rays.util;

/**
 * A Duo is simply a container for a pair of objects.
 * 
 * @author snowjak88
 *
 */
public class Duo<A, B> {
	
	private final A a;
	private final B b;
	
	public Duo(A a, B b) {
		
		this.a = a;
		this.b = b;
	}
	
	public A getA() {
		
		return a;
	}
	
	public B getB() {
		
		return b;
	}
}
