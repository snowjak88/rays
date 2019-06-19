package org.snowjak.rays.acceleration;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.snowjak.rays.Primitive;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.boundingvolume.AABB;
import org.snowjak.rays.interact.Interaction;

public class HierarchicalBoundingBox implements AccelerationStructure {
	
	private transient TreeNode root = null;
	private Collection<Primitive> primitives;
	private Collection<Primitive> unaccelerated;
	
	public HierarchicalBoundingBox(Primitive... primitives) {
		
		this(Arrays.stream(primitives).collect(Collectors.toCollection(LinkedList::new)));
	}
	
	public HierarchicalBoundingBox(Collection<Primitive> primitives) {
		
		assert (primitives != null);
		assert (!primitives.isEmpty());
		
		final LinkedList<TreeNode> nodes = primitives.stream().filter(p -> p.getShape().getBoundingVolume() != null)
				.map(p -> new LeafNode(p)).collect(Collectors.toCollection(LinkedList::new));
		
		unaccelerated = primitives.stream().filter(p -> p.getShape().getBoundingVolume() == null)
				.collect(Collectors.toCollection(LinkedList::new));
		
		while (nodes.size() > 1) {
			
			TreeNode bestNode1 = null, bestNode2 = null;
			double bestVolume = Double.MAX_VALUE;
			
			for (var node1 : nodes)
				for (var node2 : nodes) {
					if (node1 == node2)
						continue;
					
					final var testAABB = AABB.union(node1.getAABB(), node2.getAABB());
					if (testAABB.getVolume() < bestVolume) {
						bestNode1 = node1;
						bestNode2 = node2;
						bestVolume = testAABB.getVolume();
					}
				}
			
			final var newNode = new BranchNode(bestNode1, bestNode2);
			
			nodes.remove(bestNode1);
			nodes.remove(bestNode2);
			nodes.add(newNode);
			
		}
		
		root = (nodes.isEmpty()) ? null : nodes.getFirst();
	}
	
	TreeNode getRootNode() {
		
		return root;
	}
	
	@Override
	public Interaction<Primitive> getInteraction(Ray ray) {
		
		if (root != null) {
			final var acceleratedIntersection = getHeldPrimitiveInteraction(root, ray);
			if (acceleratedIntersection != null)
				return acceleratedIntersection;
		}
		
		//@formatter:off
		return unaccelerated.stream()
				.filter(p -> p.isIntersectableWith(ray))
				.map(p -> p.getInteraction(ray))
				.filter(i -> i != null)
				.sorted((i1, i2) -> Double.compare(i1.getInteractingRay().getT(), i2.getInteractingRay().getT()))
				.findFirst().orElse(null);
		//@formatter:on
	}
	
	private Interaction<Primitive> getHeldPrimitiveInteraction(TreeNode node, Ray ray) {
		
		if (!node.getAABB().isIntersecting(ray))
			return null;
		
		if (node.isLeaf())
			return ((LeafNode) node).getPrimitive().getInteraction(ray);
		
		Interaction<Primitive> result1 = null, result2 = null;
		final BranchNode branchNode = (BranchNode) node;
		if (branchNode.getBranch1().getAABB().isIntersecting(ray))
			result1 = getHeldPrimitiveInteraction(branchNode.getBranch1(), ray);
		
		if (branchNode.getBranch2().getAABB().isIntersecting(ray))
			result2 = getHeldPrimitiveInteraction(branchNode.getBranch2(), ray);
		
		if (result1 != null && result2 != null)
			if (result1.getInteractingRay().getT() < result2.getInteractingRay().getT())
				return result1;
			else
				return result2;
			
		if (result1 != null)
			return result1;
		
		if (result2 != null)
			return result2;
		
		return null;
	}
	
	@Override
	public Collection<Primitive> getPrimitives() {
		
		if (this.primitives == null)
			this.primitives = getPrimitives(root);
		
		if (this.unaccelerated != null)
			this.primitives.addAll(unaccelerated);
		
		return primitives;
	}
	
	private Collection<Primitive> getPrimitives(TreeNode currentNode) {
		
		if (currentNode == null)
			return new LinkedList<>();
		
		if (currentNode.isLeaf()) {
			final var result = new LinkedList<Primitive>();
			result.add(((LeafNode) currentNode).getPrimitive());
			return result;
		}
		
		final var result = new LinkedList<Primitive>();
		result.addAll(getPrimitives(((BranchNode) currentNode).getBranch1()));
		result.addAll(getPrimitives(((BranchNode) currentNode).getBranch2()));
		
		return result;
	}
	
	static abstract class TreeNode {
		
		private final boolean isLeaf;
		
		public TreeNode(boolean isLeaf) {
			
			this.isLeaf = isLeaf;
		}
		
		public boolean isLeaf() {
			
			return isLeaf;
		}
		
		public boolean isBranch() {
			
			return !isLeaf;
		}
		
		public abstract AABB getAABB();
		
		public abstract double getVolume();
		
		public abstract boolean equals(TreeNode other);
	}
	
	static class BranchNode extends TreeNode {
		
		private final TreeNode branch1, branch2;
		private AABB aabb = null;
		
		public BranchNode(TreeNode branch1, TreeNode branch2) {
			
			super(false);
			this.branch1 = branch1;
			this.branch2 = branch2;
		}
		
		public TreeNode getBranch1() {
			
			return branch1;
		}
		
		public TreeNode getBranch2() {
			
			return branch2;
		}
		
		@Override
		public AABB getAABB() {
			
			if (this.aabb == null)
				this.aabb = AABB.union(branch1.getAABB(), branch2.getAABB());
			
			return this.aabb;
		}
		
		@Override
		public double getVolume() {
			
			return this.aabb.getVolume();
		}
		
		@Override
		public boolean equals(TreeNode other) {
			
			if (this.isBranch() != other.isBranch())
				return false;
			
			final BranchNode otherBranch = (BranchNode) other;
			if ((this.branch1.equals(otherBranch.branch1) && this.branch2.equals(otherBranch.branch2))
					|| (this.branch1.equals(otherBranch.branch2) && this.branch2.equals(otherBranch.branch1)))
				return true;
			
			return false;
		}
		
	}
	
	static class LeafNode extends TreeNode {
		
		private final Primitive primitive;
		private final AABB aabb;
		
		public LeafNode(Primitive primitive) {
			
			super(true);
			this.primitive = primitive;
			this.aabb = primitive.getShape().getBoundingVolume();
		}
		
		public Primitive getPrimitive() {
			
			return primitive;
		}
		
		@Override
		public AABB getAABB() {
			
			return aabb;
		}
		
		@Override
		public double getVolume() {
			
			return aabb.getVolume();
		}
		
		@Override
		public boolean equals(TreeNode other) {
			
			if (this.isLeaf() != other.isLeaf())
				return false;
			
			final LeafNode otherLeaf = (LeafNode) other;
			if (this.getPrimitive() == otherLeaf.getPrimitive())
				return true;
			
			return false;
		}
		
	}
	
}
