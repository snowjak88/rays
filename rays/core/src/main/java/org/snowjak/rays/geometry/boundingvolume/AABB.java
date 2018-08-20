package org.snowjak.rays.geometry.boundingvolume;

import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.transform.Transform;

/**
 * An <strong>A</strong>xis-<strong>A</strong>ligned <strong>B</strong>ounding
 * <strong>B</strong>ox is an "acceleration structure" used to help speed up the
 * rejection of Shapes when performing Ray-intersection testing. Unlike a
 * more-general "bounding box", an AABB is always axis-aligned (i.e., it can be
 * represented by only 2 points in global coordinates), and is therefore much
 * simpler to implement.
 * 
 * @author snowjak88
 */
public class AABB {
	
	private Point3D minExtent, maxExtent;
	
	/**
	 * Given an existing AABB (assumed to be given in object-local coordinates), and
	 * a {@link List} of {@link Transform}s (assumed to give the proper order for
	 * local-to-world transformation), compute the corresponding AABB in global
	 * coordinates.
	 * 
	 * @param copyOf
	 * @param localToWorld
	 */
	public AABB(AABB copyOf, List<Transform> localToWorld) {
		
		this(copyOf.getCorners(), localToWorld);
	}
	
	/**
	 * Given a collection of {@link Point3D}s (assumed to be expressed in
	 * object-local coordinates), and a {@link List} of {@link Transform}s (assumed
	 * to give the proper order for local-to-world transformation), compute the AABB
	 * in global coordinates that encompasses these Points.
	 * 
	 * @param localPoints
	 * @param localToWorld
	 */
	public AABB(Collection<Point3D> localPoints, List<Transform> localToWorld) {
		
		this(localPoints.stream().map(p -> {
			for (Transform t : localToWorld)
				p = t.localToWorld(p);
			return p;
		}).collect(Collectors.toCollection(LinkedList::new)));
	}
	
	/**
	 * Given a collection of {@link Point3D}s (assumed to be expressed in global
	 * coordinates), compute the AABB that encompasses them all.
	 * 
	 * @param globalPoints
	 */
	public AABB(Collection<Point3D> globalPoints) {
		
		double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
		
		for (Point3D p : globalPoints) {
			minX = min(minX, p.getX());
			minY = min(minY, p.getY());
			minZ = min(minZ, p.getZ());
			
			maxX = max(maxX, p.getX());
			maxY = max(maxY, p.getY());
			maxZ = max(maxZ, p.getZ());
		}
		
		this.minExtent = new Point3D(minX, minY, minZ);
		this.maxExtent = new Point3D(maxX, maxY, maxZ);
	}
	
	/**
	 * Create a new AxisAlignedBoundingBox, using the given extents (assumed to be
	 * expressed in global coordinates).
	 * 
	 * @param minExtent
	 * @param maxExtent
	 */
	public AABB(Point3D minExtent, Point3D maxExtent) {
		
		this.minExtent = minExtent;
		this.maxExtent = maxExtent;
	}
	
	/**
	 * Given a set of {@link AABB}s, compute the AABB that encompasses them all.
	 * 
	 * @param boundingBoxes
	 * @return
	 */
	public static AABB union(AABB... boundingBoxes) {
		
		return AABB.union(Arrays.asList(boundingBoxes));
	}
	
	/**
	 * Given a set of {@link AABB}s, compute the AABB that encompasses them all.
	 * 
	 * @param boundingBoxes
	 * @return
	 */
	public static AABB union(Collection<AABB> boundingBoxes) {
		
		return new AABB(boundingBoxes.stream().collect(LinkedList::new, (l, aabb) -> {
			l.add(aabb.minExtent);
			l.add(aabb.maxExtent);
		}, LinkedList::addAll));
	}
	
	/**
	 * Return <code>true</code> if this AABB fully contains the given {@code other}
	 * AABB.
	 * 
	 * @param other
	 * @return
	 */
	public boolean isContaining(AABB other) {
		
		return this.isContained(other.getMinExtent()) && this.isContained(other.getMaxExtent());
	}
	
	/**
	 * Return <code>true</code> if this AABB contains the given {@link Point3D}.
	 * 
	 * @param point
	 * @return
	 */
	public boolean isContained(Point3D point) {
		
		return (this.getMinExtent().getX() <= point.getX() && this.getMaxExtent().getX() >= point.getX())
				&& (this.getMinExtent().getY() <= point.getY() && this.getMaxExtent().getY() >= point.getY())
				&& (this.getMinExtent().getZ() <= point.getZ() && this.getMaxExtent().getZ() >= point.getZ());
	}
	
	/**
	 * Return <code>true</code> if this AABB overlaps the given {@code other} AABB.
	 * 
	 * @param other
	 * @return
	 */
	public boolean isOverlapping(AABB other) {
		
		return this.isContained(other.getMinExtent()) || this.isContained(other.getMaxExtent());
	}
	
	/**
	 * @return a {@link Collection} containing all 8 corners of this AABB, in no
	 *         particular order.
	 */
	public Collection<Point3D> getCorners() {
		
		return Arrays.asList(minExtent, maxExtent, new Point3D(minExtent.getX(), minExtent.getY(), maxExtent.getZ()),
				new Point3D(minExtent.getX(), maxExtent.getY(), minExtent.getZ()),
				new Point3D(minExtent.getX(), maxExtent.getY(), maxExtent.getZ()),
				new Point3D(maxExtent.getX(), minExtent.getY(), minExtent.getZ()),
				new Point3D(maxExtent.getX(), minExtent.getY(), maxExtent.getZ()),
				new Point3D(maxExtent.getX(), maxExtent.getY(), minExtent.getZ()));
	}
	
	/**
	 * Given a {@link Ray} (expressed in global coordinates), determine if that Ray
	 * intersects this AABB.
	 * 
	 * @param ray
	 * @return
	 */
	public boolean isIntersecting(Ray ray) {
		
		double temp;
		
		double tmin = (minExtent.getX() - ray.getOrigin().getX()) / ray.getDirection().getX();
		double tmax = (maxExtent.getX() - ray.getOrigin().getX()) / ray.getDirection().getX();
		
		if (tmin > tmax) {
			temp = tmin;
			tmin = tmax;
			tmax = temp;
		}
		
		double tymin = (minExtent.getY() - ray.getOrigin().getY()) / ray.getDirection().getY();
		double tymax = (maxExtent.getY() - ray.getOrigin().getY()) / ray.getDirection().getY();
		
		if (tymin > tymax) {
			temp = tymin;
			tymin = tymax;
			tymax = temp;
		}
		
		if ((tmin > tymax) || (tymin > tmax))
			return false;
		
		if (tymin > tmin)
			tmin = tymin;
		
		if (tymax < tmax)
			tmax = tymax;
		
		double tzmin = (minExtent.getZ() - ray.getOrigin().getZ()) / ray.getDirection().getZ();
		double tzmax = (maxExtent.getZ() - ray.getOrigin().getZ()) / ray.getDirection().getZ();
		
		if (tzmin > tzmax) {
			temp = tzmin;
			tzmin = tzmax;
			tzmax = temp;
		}
		
		if ((tmin > tzmax) || (tzmin > tmax))
			return false;
		
		return true;
	}
	
	public Point3D getMinExtent() {
		
		return minExtent;
	}
	
	protected void setMinExtent(Point3D minExtent) {
		
		this.minExtent = minExtent;
	}
	
	public Point3D getMaxExtent() {
		
		return maxExtent;
	}
	
	protected void setMaxExtent(Point3D maxExtent) {
		
		this.maxExtent = maxExtent;
	}
	
}
