/**
 * 
 */
package org.snowjak.rays.util;

/**
 * A Quad is simply a container for 4 objects.
 * 
 * @author snowjak88
 *
 */
public class Quad<A, B, C, D> {
	
	private final A a;
	private final B b;
	private final C c;
	private final D d;
	
	public Quad(A a, B b, C c, D d) {
		
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}
	
	public A getA() {
		
		return a;
	}
	
	public B getB() {
		
		return b;
	}
	
	public C getC() {
		
		return c;
	}
	
	public D getD() {
		
		return d;
	}
}
