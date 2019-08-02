package org.snowjak.rays.geometry;

import java.io.Serializable;
import java.lang.reflect.Type;

import org.snowjak.rays.Settings;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.serialization.IsLoadable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

/**
 * A Ray combines a {@link Point3D} ("origin") and a {@link Vector3D}
 * ("direction") in one object.
 * <p>
 * A Ray has certain core fields that will always be populated:
 * <ul>
 * <li>{@link Point3D} <code>origin</code></li>
 * <li>{@link Vector3D} <code>direction</code></li>
 * <li>double <code>t</code> (default = 0)</li>
 * <li><code>int depth</code> (default = 0)</li>
 * </ul>
 * There are also "window" fields that serve to communicate an interval along
 * the Ray that we are interested in. This may be used by the application to,
 * e.g., restrict intersection-reporting to a specific area of 3-D space.
 * <ul>
 * <li>Window-min-T (default = {@link Double#NEGATIVE_INFINITY})</li>
 * <li>Window-max-T (default = {@link Double#POSITIVE_INFINITY})</li>
 * </ul>
 * </p>
 * 
 * <h3>JSON</h3>
 * <p>A Ray may be serialized to JSON in the following format.</p>
 * <pre>
 * ...
 * {
 *     "o": <em>Point3D serialization</em>,
 *     "d": <em>Vector3D serialization</em>,
 *     <em>OPTIONAL:</em> "t": 0.0,
 *     <em>OPTIONAL:</em> "depth": 0,
 *     <em>OPTIONAL:</em> "minT": 0.0,
 *     <em>OPTIONAL:</em> "maxT": 0.0
 * }
 * ...
 * </pre>
 * 
 * @author snowjak88
 */
@UIType(fields = { @UIField(name = "origin", type = Point3D.class),
		@UIField(name = "direction", type = Vector3D.class) })
public class Ray implements Serializable {
	
	private static final long serialVersionUID = 8542616504681890448L;
	
	private Point3D origin;
	private Vector3D direction;
	private double t = 0;
	
	private int depth = 0;
	
	private double windowMinT = Double.NEGATIVE_INFINITY;
	private double windowMaxT = Double.POSITIVE_INFINITY;
	
	/**
	 * Construct a new Ray with the given origin and direction, and default t of 0
	 * and "ray-depth" of 0.
	 * 
	 * @param origin
	 * @param direction
	 */
	public Ray(Point3D origin, Vector3D direction) {
		
		this(origin, direction, 0d, 0);
	}
	
	/**
	 * Construct a new Ray with the given origin, direction, and "ray-depth", and
	 * default t of 0.0
	 * 
	 * @param origin
	 * @param direction
	 * @param depth
	 */
	public Ray(Point3D origin, Vector3D direction, int depth) {
		
		this(origin, direction, 0d, depth);
	}
	
	/**
	 * Construct a new Ray with the given origin, direction, and t, and default
	 * "ray-depth" of 0.
	 * 
	 * @param origin
	 * @param direction
	 * @param t
	 */
	public Ray(Point3D origin, Vector3D direction, double t) {
		
		this(origin, direction, t, 0);
	}
	
	/**
	 * Construct a new Ray with the given origin, direction, and "ray-depth".
	 * 
	 * @param origin
	 * @param direction
	 * @param t
	 * @param depth
	 */
	public Ray(Point3D origin, Vector3D direction, double t, int depth) {
		
		this(origin, direction, t, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	/**
	 * Construct a new Ray with the given origin, direction, and "ray-depth", also
	 * supplying values for ray-<em>t</em> windowing.
	 * 
	 * @param origin
	 * @param direction
	 * @param t
	 * @param depth
	 * @param windowMinT
	 * @param windowMaxT
	 */
	public Ray(Point3D origin, Vector3D direction, double t, int depth, double windowMinT, double windowMaxT) {
		
		this.origin = origin;
		this.direction = direction;
		this.t = t;
		this.depth = depth;
		this.windowMinT = windowMinT;
		this.windowMaxT = windowMaxT;
	}
	
	protected Ray() {
		
		this.origin = new Point3D();
		this.direction = new Vector3D();
		this.t = 0;
		this.windowMinT = 0;
		this.windowMaxT = 0;
	}
	
	/**
	 * Given the configured <em>t</em> parameter (see {@link #getT()}), calculate
	 * the corresponding point along this Ray.
	 * 
	 * @return
	 */
	public Point3D getPointAlong() {
		
		return getPointAlong(this.getT());
	}
	
	/**
	 * Given a <em>t</em> parameter, calculate the corresponding point along this
	 * Ray.
	 * 
	 * @param t
	 * @return
	 */
	public Point3D getPointAlong(double t) {
		
		return Point3D.from(origin.add(direction.multiply(t)));
	}
	
	/**
	 * Construct a new {@link Ray} as a copy of this Ray, using a different value
	 * for <em>t</em>.
	 * 
	 * @param t
	 * @return
	 */
	public Ray forT(double t) {
		
		return new Ray(this.getOrigin(), this.getDirection(), t, this.getDepth(), this.getWindowMinT(),
				this.getWindowMaxT());
	}
	
	/**
	 * @param t
	 * @return <code>true</code> if <code>t</code> is within [
	 *         {@link #getWindowMinT()}, {@link #getWindowMaxT()} ]
	 */
	public boolean isInWindow(double t) {
		
		return (t >= windowMinT) && (t <= windowMaxT);
	}
	
	public Point3D getOrigin() {
		
		return origin;
	}
	
	protected void setOrigin(Point3D origin) {
		
		this.origin = origin;
	}
	
	public Vector3D getDirection() {
		
		return direction;
	}
	
	protected void setDirection(Vector3D direction) {
		
		this.direction = direction;
	}
	
	public double getT() {
		
		return t;
	}
	
	protected void setT(double t) {
		
		this.t = t;
	}
	
	public int getDepth() {
		
		return depth;
	}
	
	protected void setDepth(int depth) {
		
		this.depth = depth;
	}
	
	public double getWindowMinT() {
		
		return windowMinT;
	}
	
	public void setWindowMinT(double windowMinT) {
		
		this.windowMinT = windowMinT;
	}
	
	public double getWindowMaxT() {
		
		return windowMaxT;
	}
	
	public void setWindowMaxT(double windowMaxT) {
		
		this.windowMaxT = windowMaxT;
	}
	
	@Override
	public String toString() {
		
		return "Ray:[origin=" + origin.toString() + ", direction=" + direction.toString() + ", t=" + t + ", depth="
				+ depth + ", windowMinT=" + windowMinT + ", windowMaxT=" + windowMaxT + "]";
	}
	
	public static class Loader implements IsLoadable<Ray> {
		
		@Override
		public Ray deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			
			if (!json.isJsonObject())
				throw new JsonParseException("Cannot deserialize Ray from JSON -- expecting a JSON object!");
			
			final var obj = json.getAsJsonObject();
			
			final Point3D origin = context.deserialize(obj.get("o"), Point3D.class);
			final Vector3D direction = context.deserialize(obj.get("d"), Vector3D.class);
			
			double t = 0;
			if (obj.has("t"))
				t = obj.get("t").getAsDouble();
			
			int depth = 0;
			if (obj.has("depth"))
				depth = obj.get("depth").getAsInt();
			
			double windowMinT = Double.NEGATIVE_INFINITY;
			if (obj.has("minT"))
				windowMinT = obj.get("minT").getAsDouble();
			
			double windowMaxT = Double.POSITIVE_INFINITY;
			if (obj.has("maxT"))
				windowMaxT = obj.get("maxT").getAsDouble();
			
			return new Ray(origin, direction, t, depth, windowMinT, windowMaxT);
		}
		
		@Override
		public JsonElement serialize(Ray src, Type typeOfSrc, JsonSerializationContext context) {
			
			final var obj = new JsonObject();
			
			obj.add("o", context.serialize(src.getOrigin()));
			obj.add("d", context.serialize(src.getDirection()));
			
			if (!Settings.getInstance().nearlyEqual(src.getT(), 0))
				obj.addProperty("t", src.getT());
			
			if (src.getDepth() != 0)
				obj.addProperty("depth", src.getDepth());
			
			if (!Settings.getInstance().nearlyEqual(src.getWindowMinT(), Double.NEGATIVE_INFINITY))
				obj.addProperty("minT", src.getWindowMinT());
			
			if (!Settings.getInstance().nearlyEqual(src.getWindowMaxT(), Double.POSITIVE_INFINITY))
				obj.addProperty("maxT", src.getWindowMaxT());
			
			return obj;
		}
		
	}
}
