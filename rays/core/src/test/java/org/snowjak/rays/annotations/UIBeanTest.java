package org.snowjak.rays.annotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.texture.ConstantTexture;
import org.snowjak.rays.texture.mapping.IdentityTextureMapping;
import org.snowjak.rays.texture.mapping.TilingTextureMapping;
import org.snowjak.rays.transform.RotationTransform;

public class UIBeanTest {
	
	@Test
	public void testVector() {
		
		final var bean = new UIBean<>(Vector3D.class);
		
		assertNotNull(bean.getFieldNames());
		assertEquals(3, bean.getFieldNames().size());
		assertTrue(bean.getFieldNames().contains("x"));
		assertTrue(bean.getFieldNames().contains("y"));
		assertTrue(bean.getFieldNames().contains("z"));
		
		assertTrue(Double.class.isAssignableFrom(bean.getFieldValue("x").getClass()));
		assertTrue(Double.class.isAssignableFrom(bean.getFieldValue("y").getClass()));
		assertTrue(Double.class.isAssignableFrom(bean.getFieldValue("z").getClass()));
		
		assertEquals(0.0, (Double) bean.getFieldValue("x"), 0.00001);
		assertEquals(0.0, (Double) bean.getFieldValue("y"), 0.00001);
		assertEquals(0.0, (Double) bean.getFieldValue("z"), 0.00001);
		
		assertEquals(Double.class, bean.getFieldType("x"));
		assertEquals(1, bean.getFieldAvailableTypes("x").size());
		assertTrue(bean.getFieldAvailableTypes("x").contains(Double.class));
		
		assertNull(bean.getFieldCollectedType("x"));
		
		final var json = Settings.getInstance().getGson().toJson(bean);
		final var expectedJson = "{\"x\":0.0,\"y\":0.0,\"z\":0.0}";
		assertEquals(expectedJson, json);
		
	}
	
	@Test
	public void testRotation() {
		
		final var bean = new UIBean<>(RotationTransform.class);
		
		assertEquals(2, bean.getFieldNames().size());
		assertTrue(bean.getFieldNames().contains("axis"));
		assertTrue(bean.getFieldNames().contains("degrees"));
		
		assertEquals(UIBean.class, bean.getFieldValue("axis").getClass());
		assertEquals(Vector3D.class, ((UIBean<?>) (bean.getFieldValue("axis"))).getType());
		
		assertEquals(Double.class, bean.getFieldValue("degrees").getClass());
		
		final var json = Settings.getInstance().getGson().toJson(bean);
		final var expectedJson = "{\"axis\":{\"x\":0.0,\"y\":0.0,\"z\":0.0},\"degrees\":0.0}";
		assertEquals(expectedJson, json);
		
	}
	
	@Test
	public void testTexture() {
		
		final var bean = new UIBean<>(ConstantTexture.class);
		
		assertEquals(2, bean.getFieldNames().size());
		assertTrue(bean.getFieldNames().contains("rgb"));
		assertTrue(bean.getFieldNames().contains("mapping"));
		
		assertEquals(UIBean.class, bean.getFieldValue("rgb").getClass());
		assertEquals(RGB.class, ((UIBean<?>) (bean.getFieldValue("rgb"))).getType());
		
		assertEquals(UIBean.class, bean.getFieldValue("mapping").getClass());
		assertEquals(2, bean.getFieldAvailableTypes("mapping").size());
		assertTrue(bean.getFieldAvailableTypes("mapping").contains(IdentityTextureMapping.class));
		assertTrue(bean.getFieldAvailableTypes("mapping").contains(TilingTextureMapping.class));
		assertEquals(IdentityTextureMapping.class, ((UIBean<?>) (bean.getFieldValue("mapping"))).getType());
		
		final var json = Settings.getInstance().getGson().toJson(bean);
		final var expectedJson = "{\"mapping\":{},\"rgb\":{\"red\":1.0,\"green\":1.0,\"blue\":1.0}}";
		assertEquals(expectedJson, json);
	}
	
}
