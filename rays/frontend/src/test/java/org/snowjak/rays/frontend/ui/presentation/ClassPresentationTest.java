package org.snowjak.rays.frontend.ui.presentation;

import static org.junit.Assert.*;

import org.junit.Test;
import org.snowjak.rays.frontend.ui.presentation.ClassPresentationTest.TestBean.InnerBean;

public class ClassPresentationTest {
	
	@Test
	public void test_hierarchical() {
		
		final var cp = new ClassPresentation();
		cp.setClass(TestBean.class);
		
		assertNotNull(cp.getFieldNames());
		assertEquals(3, cp.getFieldNames().size());
		assertTrue(cp.getFieldNames().contains("i"));
		assertTrue(cp.getFieldNames().contains("j"));
		assertTrue(cp.getFieldNames().contains("inner"));
		
		assertTrue(cp.getField("inner").get().getType().equals(InnerBean.class));
		assertTrue(cp.getValue("inner") instanceof ClassPresentation);
		
		cp.setValue("i", 0);
		cp.setValue("j", 1);
		((ClassPresentation) (cp.getValue("inner"))).setValue("a", "a");
		((ClassPresentation) (cp.getValue("inner"))).setValue("b", "b");
		
		final var bean = cp.instantiate(TestBean.class);
		assertNotNull(bean);
		assertTrue(bean instanceof TestBean);
		
		assertEquals(0, bean.getI());
		assertEquals(1, bean.getJ());
		
		assertNotNull(bean.getInner());
		assertTrue(bean.getInner() instanceof InnerBean);
		
		assertEquals("a", bean.getInner().getA());
		assertEquals("b", bean.getInner().getB());
	}
	
	@Test
	public void test_fieldNeedsConstructor() {
		
		final var cp = new ClassPresentation();
		cp.setClass(ConstructedBean.class);
		
		assertNotNull(cp.getFieldNames());
		assertEquals(2, cp.getFieldNames().size());
		assertTrue(cp.getFieldNames().contains("i"));
		assertTrue(cp.getFieldNames().contains("j"));
		
		cp.setValue("i", 0);
		cp.setValue("j", 1);
		
		final var bean = cp.instantiate(ConstructedBean.class);
		assertNotNull(bean);
		assertTrue(bean instanceof ConstructedBean);
		
		assertEquals(0, bean.getI());
		assertEquals(1, bean.getJ());
	}
	
	@Test
	public void test_fieldDoesntNeedConstructor() {
		
		final var cp = new ClassPresentation();
		cp.setClass(AnotherConstructedBean.class);
		
		assertNotNull(cp.getFieldNames());
		assertEquals(2, cp.getFieldNames().size());
		assertTrue(cp.getFieldNames().contains("i"));
		assertTrue(cp.getFieldNames().contains("j"));
		
		cp.setValue("i", 0);
		cp.setValue("j", 1);
		
		final var bean = cp.instantiate(AnotherConstructedBean.class);
		assertNotNull(bean);
		assertTrue(bean instanceof AnotherConstructedBean);
		
		assertEquals(0, bean.getI());
		assertEquals(1, bean.getJ());
	}
	
	public static class TestBean {
		
		private int i, j;
		
		private InnerBean inner;
		
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
		
		public InnerBean getInner() {
			
			return inner;
		}
		
		public void setInner(InnerBean inner) {
			
			this.inner = inner;
		}
		
		public static class InnerBean {
			
			private String a, b;
			
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
			
		}
		
	}
	
	public static class ConstructedBean {
		
		private int i, j;
		
		public ConstructedBean(int i) {
			
			this.i = i;
		}
		
		public int getJ() {
			
			return j;
		}
		
		public void setJ(int j) {
			
			this.j = j;
		}
		
		public int getI() {
			
			return i;
		}
		
	}
	
	public static class AnotherConstructedBean {
		
		private int i, j;
		
		public AnotherConstructedBean(int i) {
			
			this.i = i;
		}
		
		public int getJ() {
			
			return j;
		}
		
		public void setJ(int j) {
			
			this.j = j;
		}
		
		public int getI() {
			
			return i;
		}
		
		public void setI(int i) {
			
			this.i = i;
		}
		
	}
	
}
