package org.snowjak.rays.material;

import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.snowjak.rays.geometry.Normal3D;
import org.snowjak.rays.geometry.Vector3D;

public class FresnelTest {
	
	@Test
	public void test() {
		
		final Normal3D n = Normal3D.from(Vector3D.J);
		final double n1 = 1.0, n2 = 1.3;
		
		final Vector3D wi = new Vector3D(+1, -1, 0).normalize();
		final Vector3D wr = new Vector3D(+wi.getX(), -wi.getY(), +wi.getZ()).normalize();
		final Vector3D wt = new Vector3D(cos((90d - 32.95) * PI / 180d), -sin((90 - 32.95) * PI / 180d), 0).normalize();
		final double reflectance = (0.04556 + 0.002075) / 2d;
		final var result = new Fresnel(wi, n, n1, n2);
		
		assertEquals("Result #1-reflectance not as expected", reflectance, result.getReflectance(), 0.0001);
		assertEquals("Result #1-transmittance not as expected", 1d - reflectance, result.getTransmittance(), 0.0001);
		assertFalse("Result #1 should not be flagged as TIR!", result.isTotalInternalReflection());
		
		assertEquals("Result #1-reflected-X not as expected", wr.getX(), result.getReflection().getX(), 0.0001);
		assertEquals("Result #1-reflected-Y not as expected", wr.getY(), result.getReflection().getY(), 0.0001);
		assertEquals("Result #1-reflected-Z not as expected", wr.getZ(), result.getReflection().getZ(), 0.0001);
		
		assertEquals("Result #1-transmitted-X not as expected", wt.getX(), result.getTransmission().getX(), 0.0001);
		assertEquals("Result #1-transmitted-Y not as expected", wt.getY(), result.getTransmission().getY(), 0.0001);
		assertEquals("Result #1-transmitted-Z not as expected", wt.getZ(), result.getTransmission().getZ(), 0.0001);
		
	}
	
	@Test
	public void test_TIR() {
		
		final Normal3D n = Normal3D.from(Vector3D.J);
		
		final Vector3D wi = new Vector3D(+cos((75d) * PI / 180d), -sin((75d) * PI / 180d), 0).normalize();
		final Vector3D wr = new Vector3D(+wi.getX(), -wi.getY(), +wi.getZ()).normalize();
		final var result = new Fresnel(wi, n, 5, 1);
		
		assertEquals("Result #2-reflectance not as expected", 1d, result.getReflectance(), 0.00001);
		assertEquals("Result #2-transmittance not as expected", 0d, result.getTransmittance(), 0.00001);
		assertTrue("Result #2 should be flagged as TIR!", result.isTotalInternalReflection());
		
		assertEquals("Result #2-reflected-X not as expected", wr.getX(), result.getReflection().getX(), 0.0001);
		assertEquals("Result #2-reflected-Y not as expected", wr.getY(), result.getReflection().getY(), 0.0001);
		assertEquals("Result #2-reflected-Z not as expected", wr.getZ(), result.getReflection().getZ(), 0.0001);
	}
	
}
