package org.snowjak.rays.frontend.ui.presentation;

import static org.junit.Assert.*;

import org.junit.Test;

public class ClassFieldAnalyzerTest {
	
	@Test
	public void test() {
		
		final var cp = new ClassFieldAnalyzer(TestBean.class);
		
		assertNotNull(cp);
		assertNotNull(cp.getFields());
		assertEquals(6, cp.getFields().size());
		
		assertTrue(cp.getFields().stream().anyMatch(of -> of.getName().equals("i")));
		assertTrue(cp.getFields().stream().anyMatch(of -> of.getName().equals("j")));
		assertTrue(cp.getFields().stream().anyMatch(of -> of.getName().equals("k")));
		assertTrue(cp.getFields().stream().anyMatch(of -> of.getName().equals("a")));
		assertTrue(cp.getFields().stream().anyMatch(of -> of.getName().equals("b")));
		assertTrue(cp.getFields().stream().anyMatch(of -> of.getName().equals("c")));
		
		assertTrue(
				cp.getFields().stream().filter(of -> of.getName().equals("i")).anyMatch(of -> of.getGetter() != null));
		assertTrue(
				cp.getFields().stream().filter(of -> of.getName().equals("j")).anyMatch(of -> of.getGetter() != null));
		assertTrue(
				cp.getFields().stream().filter(of -> of.getName().equals("k")).anyMatch(of -> of.getGetter() != null));
		assertTrue(
				cp.getFields().stream().filter(of -> of.getName().equals("a")).anyMatch(of -> of.getGetter() != null));
		assertTrue(
				cp.getFields().stream().filter(of -> of.getName().equals("b")).anyMatch(of -> of.getGetter() != null));
		assertTrue(
				cp.getFields().stream().filter(of -> of.getName().equals("c")).anyMatch(of -> of.getGetter() != null));
		
		assertTrue(
				cp.getFields().stream().filter(of -> of.getName().equals("i")).anyMatch(of -> of.getSetter() != null));
		assertTrue(
				cp.getFields().stream().filter(of -> of.getName().equals("j")).anyMatch(of -> of.getSetter() != null));
		assertTrue(
				cp.getFields().stream().filter(of -> of.getName().equals("k")).anyMatch(of -> of.getSetter() == null));
		assertTrue(
				cp.getFields().stream().filter(of -> of.getName().equals("a")).anyMatch(of -> of.getSetter() != null));
		assertTrue(
				cp.getFields().stream().filter(of -> of.getName().equals("b")).anyMatch(of -> of.getSetter() != null));
		assertTrue(
				cp.getFields().stream().filter(of -> of.getName().equals("c")).anyMatch(of -> of.getSetter() == null));
	}
	
	public static class TestBean {
		
		private int i, j, k;
		private String a, b, c;
		
		public int getI() {
			
			return i;
		}
		
		public void setI(int i) {
			
			this.i = i;
		}
		
		public int getJ() {
			
			return j;
		}
		
		public void setJ(int j) {
			
			this.j = j;
		}
		
		public int getK() {
			
			return k;
		}
		
		public String getA() {
			
			return a;
		}
		
		public void setA(String a) {
			
			this.a = a;
		}
		
		public String getB() {
			
			return b;
		}
		
		public void setB(String b) {
			
			this.b = b;
		}
		
		public String getC() {
			
			return c;
		}
		
	}
}
