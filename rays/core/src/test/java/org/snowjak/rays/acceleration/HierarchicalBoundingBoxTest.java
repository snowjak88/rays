package org.snowjak.rays.acceleration;

import static org.junit.Assert.*;

import org.junit.Test;
import org.snowjak.rays.Primitive;
import org.snowjak.rays.acceleration.HierarchicalBoundingBox.BranchNode;
import org.snowjak.rays.acceleration.HierarchicalBoundingBox.LeafNode;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.shape.SphereShape;
import org.snowjak.rays.transform.TranslationTransform;

public class HierarchicalBoundingBoxTest {
	
	@Test
	public void testConstruction() {
		
		final Primitive p1 = new Primitive(new SphereShape(0.5, new TranslationTransform(-2, 0, 0)), null),
				p2 = new Primitive(new SphereShape(0.5), null),
				p3 = new Primitive(new SphereShape(0.5, new TranslationTransform(+4, 0, 0)), null);
		
		final HierarchicalBoundingBox hbb = new HierarchicalBoundingBox(p1, p2, p3);
		
		assertNotNull(hbb);
		assertNotNull(hbb.getRootNode());
		
		assertTrue(BranchNode.class.isAssignableFrom(hbb.getRootNode().getClass()));
		BranchNode root = (BranchNode) hbb.getRootNode();
		
		assertNotNull(root.getBranch1());
		assertNotNull(root.getBranch2());
		
		assertTrue(BranchNode.class.isAssignableFrom(root.getBranch1().getClass())
				|| BranchNode.class.isAssignableFrom(root.getBranch2().getClass()));
		assertTrue(LeafNode.class.isAssignableFrom(root.getBranch1().getClass())
				|| LeafNode.class.isAssignableFrom(root.getBranch2().getClass()));
		
		final LeafNode leaf;
		if (LeafNode.class.isAssignableFrom(root.getBranch1().getClass()))
			leaf = (LeafNode) root.getBranch1();
		else
			leaf = (LeafNode) root.getBranch2();
		
		assertEquals(p3, leaf.getPrimitive());
		
		final BranchNode branch;
		if (BranchNode.class.isAssignableFrom(root.getBranch1().getClass()))
			branch = (BranchNode) root.getBranch1();
		else
			branch = (BranchNode) root.getBranch2();
		
		assertNotNull(branch.getBranch1());
		assertNotNull(branch.getBranch2());
		
		assertTrue(LeafNode.class.isAssignableFrom(branch.getBranch1().getClass()));
		assertTrue(LeafNode.class.isAssignableFrom(branch.getBranch2().getClass()));
		
		assertTrue((((LeafNode) branch.getBranch1()).getPrimitive() == p1
				&& ((LeafNode) branch.getBranch2()).getPrimitive() == p2)
				|| (((LeafNode) branch.getBranch1()).getPrimitive() == p2
						&& ((LeafNode) branch.getBranch2()).getPrimitive() == p1));
	}
	
	@Test
	public void testGetInteraction() {
		
		final Primitive p1 = new Primitive(new SphereShape(0.5, new TranslationTransform(-2, 0, 0)), null),
				p2 = new Primitive(new SphereShape(0.5), null),
				p3 = new Primitive(new SphereShape(0.5, new TranslationTransform(+4, 0, 0)), null);
		
		final HierarchicalBoundingBox hbb = new HierarchicalBoundingBox(p1, p2, p3);
		
		final Ray ray = new Ray(new Point3D(0, -4, 0), new Vector3D(-2, 4).normalize());
		final Interaction<Primitive> interaction = hbb.getInteraction(ray);
		
		assertNotNull(interaction);
		
		assertEquals(p1, interaction.getInteracted());
	}
	
}
