package org.snowjak.rays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SettingsTest {
	
	@Test
	public void testNearlyEqualForEquals() {
		
		final double d1 = 1.0d, d2 = d1 + (Settings.getInstance().getDoubleEqualityEpsilon() / 2d);
		assertTrue("Double values should be nearlyEqual but are not!", Settings.getInstance().nearlyEqual(d1, d2));
	}
	
	@Test
	public void testNearlyEqualForNotEquals() {
		
		final double d1 = 1.0d, d2 = d1 + (Settings.getInstance().getDoubleEqualityEpsilon() * 2d);
		assertFalse("Double values should not be nearlyEqual but are!", Settings.getInstance().nearlyEqual(d1, d2));
	}
	
}
