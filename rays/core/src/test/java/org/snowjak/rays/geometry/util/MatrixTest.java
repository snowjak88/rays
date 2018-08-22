package org.snowjak.rays.geometry.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.util.Matrix;

public class MatrixTest {
	
	private Matrix a, b;
	
	@Before
	public void setUp() throws Exception {
		
		//@formatter:off
		this.a = new Matrix(new double[][] {	{ 1d, 3d,-8d, 4d },
												{-4d, 3d, 6d, 7d },
												{ 2d, 7d,-9d, 5d },
												{ 5d, 2d, 0d, 1d } });
		
		this.b = new Matrix(new double[][] {	{ 3d,-1d, 7d,-4d },
												{-6d, 9d, 7d, 2d },
												{-2d, 2d, 1d, 8d },
												{ 4d, 7d, -7d, -6d } });
		//@formatter:on
	}
	
	@Test
	public void testAdd() {
		
		Matrix c = a.add(b);
		
		//@formatter:off
		assertTrue(c.equals(new double[][] {	{  4d,  2d,  -1d,  0d },
												{ -10d, 12d, 13d,  9d },
												{   0d,  9d, -8d, 13d },
												{   9d,  9d, -7d, -5d } },
				0.0001));
		//@formatter:on
	}
	
	@Test
	public void testMultiplyDouble() {
		
		Matrix result = a.multiply(4d);
		
		//@formatter:off
		assertTrue(result.equals(new double[][] {	{  4d,  12d, -32d, 16d },
													{ -16d, 12d,  24d, 28d },
													{   8d, 28d, -36d, 20d },
													{  20d,  8d,   0d,  4d } },
				0.0001));
		//@formatter:on
	}
	
	@Test
	public void testMultiplyMatrix() {
		
		Matrix c = a.multiply(b);
		
		//@formatter:off
		assertTrue(c.equals(new double[][] {	{  17d, 38d,  -8d, -86d },
												{ -14d, 92d, -50d,  28d },
												{   2d, 78d,  19d,  -96d },
												{   7d, 20d,  42d,  -22d } },
				0.0001));
		//@formatter:on
	}
	
	@Test
	public void testInverse() {
		
		Matrix result = a.inverse();
		
		//@formatter:off
		assertTrue(result.equals(new double[][] {	{  (117d/1310d), -(21d/1310d), -(59d/655d), (269d/1310d) },
													{ -(489d/1310d), -(13d/1310d), (213d/655d), -(83d/1310d) },
													{   -(68d/655d),   (29d/655d),   (7d/655d),   (34d/655d) },
													{      (3d/10d),     (1d/10d),    -(1d/5d),     (1d/10d) } },
				0.0001));
		//@formatter:on
	}
	
	@Test
	public void testDeterminant() {
		
		double result = a.determinant();
		
		assertEquals(1310d, result, 0.0001);
	}
	
	@Test
	public void testTrace() {
		
		double result = a.trace();
		
		assertEquals(-4d, result, 0.0001);
	}
	
	@Test
	public void testSerialization() {
		
		final var expected = "{\"values\":[[1.0,3.0,-8.0,4.0],[-4.0,3.0,6.0,7.0],[2.0,7.0,-9.0,5.0],[5.0,2.0,0.0,1.0]]}";
		
		final var result = Settings.getInstance().getGson().toJson(this.a);
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testDeserialization() {
		
		final var json = "{\"values\":[[1.0,3.0,-8.0,4.0],[-4.0,3.0,6.0,7.0],[2.0,7.0,-9.0,5.0],[5.0,2.0,0.0,1.0]]}";
		
		final var result = Settings.getInstance().getGson().fromJson(json, Matrix.class);
		
		assertTrue(this.a.equals(result));
	}
	
}
