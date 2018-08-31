package org.snowjak.rays.annotations.bean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.snowjak.rays.RenderTask;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.Normal3D;

public class NodeTest {
	
	@Test
	public void test_literalDouble() {
		
		final var literal = new LiteralNode(Double.class, "0.0");
		
		assertTrue(Double.class.isAssignableFrom(literal.getDefaultValue().getClass()));
		assertEquals(0.0, (Double) literal.getDefaultValue(), 0.00001);
		assertEquals(0.0, (Double) literal.getValue(), 0.00001);
		
		literal.setValue(3.0);
		assertEquals(3.0, (Double) literal.getValue(), 0.00001);
		
		assertEquals("3.0", Settings.getInstance().getGson().toJson(literal));
	}
	
	@Test
	public void test_literalString() {
		
		final var literal = new LiteralNode(String.class, "nothing");
		
		assertTrue(String.class.isAssignableFrom(literal.getDefaultValue().getClass()));
		assertEquals("nothing", (String) literal.getDefaultValue());
		assertEquals("nothing", (String) literal.getValue());
		
		literal.setValue("something");
		assertEquals("something", (String) literal.getValue());
		
		assertEquals("\"something\"", Settings.getInstance().getGson().toJson(literal));
	}
	
	@Test
	public void test_collectionDouble() {
		
		final var coll = new CollectionNode(Double.class, "1.0");
		
		assertEquals(0, coll.getValues().size());
		
		assertTrue(Double.class.isAssignableFrom(coll.getType()));
		assertTrue(Double.class.isAssignableFrom(((LiteralNode) coll.getDefaultValue()).getType()));
		
		coll.addValue(coll.getDefaultValue());
		
		assertEquals(1, coll.getValues().size());
		assertTrue(LiteralNode.class.isAssignableFrom(coll.getValues().iterator().next().getClass()));
		assertTrue(Double.class.isAssignableFrom(coll.getValues().iterator().next().getType()));
		assertEquals(1.0, (Double) ((LiteralNode) coll.getValues().iterator().next()).getValue(), 0.00001);
		
		assertEquals("[1.0]", Settings.getInstance().getGson().toJson(coll));
	}
	
	@Test
	public void test_bean() {
		
		final var bean = new BeanNode(Normal3D.class);
		
		assertEquals(3, bean.getFieldNames().size());
		
		assertNotNull(bean.getField("x"));
		assertNotNull(bean.getField("y"));
		assertNotNull(bean.getField("z"));
		
		assertTrue(bean.getField("x") instanceof LiteralNode);
		assertTrue(bean.getField("y") instanceof LiteralNode);
		assertTrue(bean.getField("z") instanceof LiteralNode);
		
		assertEquals(0.0, (Double) ((LiteralNode) bean.getField("x")).getValue(), 0.00001);
		assertEquals(1.0, (Double) ((LiteralNode) bean.getField("y")).getValue(), 0.00001);
		assertEquals(0.0, (Double) ((LiteralNode) bean.getField("z")).getValue(), 0.00001);
		
		assertEquals("{\"x\":0.0,\"y\":1.0,\"z\":0.0}", Settings.getInstance().getGson().toJson(bean));
	}
	
	@Test
	public void test_renderTask() {
		
		final var bean = new BeanNode(RenderTask.class);
		
		assertEquals(4, bean.getFieldNames().size());
		
		assertNotNull(bean.getField("sampler"));
		assertNotNull(bean.getField("renderer"));
		assertNotNull(bean.getField("film"));
		assertNotNull(bean.getField("scene"));
		
		assertTrue(bean.getField("sampler") instanceof BeanNode);
		assertTrue(bean.getField("renderer") instanceof BeanNode);
		assertTrue(bean.getField("film") instanceof BeanNode);
	}
	
}
