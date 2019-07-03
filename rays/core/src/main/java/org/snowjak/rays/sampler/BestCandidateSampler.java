package org.snowjak.rays.sampler;

import static org.apache.commons.math3.util.FastMath.log;
import static org.apache.commons.math3.util.FastMath.min;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.snowjak.rays.Settings;
import org.snowjak.rays.Util;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.geometry.util.Pair;
import org.snowjak.rays.sample.FixedSample;
import org.snowjak.rays.sample.Sample;

/**
 * Implements best-candidate sampling, which aims to produce well-distributed
 * results at the cost of computation time.
 * <p>
 * <h3>Dart-throwing</h3> In order to produce well-distributed results, we
 * employ "dart-throwing":
 * <ul>
 * <li>For every sample-point we require, we generate <em>n</em>
 * candidate-points</li>
 * <li>We take the candidate-point that is most-distant from all current points,
 * and add it to that list of current points</li>
 * </ul>
 * </p>
 * <p>
 * <h3>Blocking</h3> To produce well-distributed results across multiple pixels,
 * we allocate a block of <code>n</code>x<code>n</code> pixels, and generate all
 * required film-points at once on that block. Samples for other dimensions
 * (e.g., lens-points) are generated only per-pixel.
 * </p>
 * 
 * @author snowjak88
 *
 */
@UIType(type = "best-candidate", fields = { @UIField(name = "xStart", type = Double.class, defaultValue = "0"),
		@UIField(name = "yStart", type = Double.class, defaultValue = "0"),
		@UIField(name = "xEnd", type = Double.class, defaultValue = "399"),
		@UIField(name = "yEnd", type = Double.class, defaultValue = "299"),
		@UIField(name = "samplesPerPixel", type = Integer.class, defaultValue = "4"),
		@UIField(name = "additional1DSamples", type = Integer.class, defaultValue = "4"),
		@UIField(name = "additional2DSamples", type = Integer.class, defaultValue = "4") })
public class BestCandidateSampler extends Sampler {
	
	private transient boolean initialized = false;
	
	private transient int blockSize;
	
	private transient Point2D[][][] block;
	
	private transient Point2D[] lensSamples;
	
	private transient Double[] tSamples;
	
	private transient int currentBlockX, currentBlockY;
	
	private transient int currentPixelX, currentPixelY, currentPixelSample;
	
	private transient long samplesGenerated = 0;
	
	/**
	 * Construct a new {@link BestCandidateSampler} across the given interval
	 * [<code>(xStart,yStart)</code>, <code>(xEnd,yEnd)</code>], with no additional
	 * points requested.
	 * 
	 * @param xStart
	 * @param yStart
	 * @param xEnd
	 * @param yEnd
	 * @param samplesPerPixel
	 */
	public BestCandidateSampler(int xStart, int yStart, int xEnd, int yEnd, int samplesPerPixel) {
		
		this(xStart, yStart, xEnd, yEnd, samplesPerPixel, 0, 0);
	}
	
	/**
	 * Construct a new {@link BestCandidateSampler} across the given interval
	 * [<code>(xStart,yStart)</code>, <code>(xEnd,yEnd)</code>].
	 * 
	 * @param xStart
	 * @param yStart
	 * @param xEnd
	 * @param yEnd
	 * @param samplesPerPixel
	 * @param additional1dSamples
	 * @param additional2dSamples
	 */
	public BestCandidateSampler(int xStart, int yStart, int xEnd, int yEnd, int samplesPerPixel,
			int additional1dSamples, int additional2dSamples) {
		
		super(xStart, yStart, xEnd, yEnd, samplesPerPixel, additional1dSamples, additional2dSamples);
		
		initialize();
	}
	
	/**
	 * Because this can be deserialized -- with only its startup parameters actually
	 * saved and restored -- we might need to initialize this outside of the
	 * constructor!
	 */
	private void initialize() {
		
		final int minDimensionSize = min(getXEnd() - getXStart() + 1, getYEnd() - getYStart() + 1);
		this.blockSize = (minDimensionSize >= Settings.getInstance().getSamplerBestCandidateBlockSize())
				? Settings.getInstance().getSamplerBestCandidateBlockSize()
				: (int) pow(2d, (int) log(2d, minDimensionSize));
		
		this.block = new Point2D[blockSize][blockSize][getSamplesPerPixel()];
		generateNewBlock();
		
		this.lensSamples = generateSamples(blockSize * blockSize * getSamplesPerPixel(),
				() -> new Point2D(Settings.RND.nextDouble(), Settings.RND.nextDouble()),
				(p1, p2) -> pow(p1.getX() - p2.getX(), 2) + pow(p1.getY() - p2.getY(), 2), (p) -> true, (p) -> {
				}).toArray(new Point2D[0]);
		
		this.tSamples = generateSamples(blockSize * blockSize * getSamplesPerPixel(), () -> Settings.RND.nextDouble(),
				(p1, p2) -> sqrt(pow(p1 - p2, 2)), (p) -> true, (p) -> {
				}).toArray(new Double[0]);
		
		this.currentBlockX = 0;
		this.currentBlockY = 0;
		
		this.currentPixelX = getXStart();
		this.currentPixelY = getYStart();
		this.currentPixelSample = 0;
		
		samplesGenerated = 0;
		setTotalSamples(-1);
		
		this.initialized = true;
	}
	
	@Override
	public boolean hasNextSample() {
		
		if (!initialized)
			initialize();
		
		return currentPixelY <= getYEnd();
	}
	
	@Override
	public Sample getNextSample() {
		
		if (!initialized)
			initialize();
		
		final var filmPoint = block[currentBlockX][currentBlockY][currentPixelSample]
				.add(new Pair(currentPixelX, currentPixelY));
		final var lensPoint = lensSamples[currentPixelSample];
		final double t = tSamples[currentBlockX * blockSize * getSamplesPerPixel()
				+ currentBlockY * getSamplesPerPixel() + currentPixelSample];
		
		final var result = new FixedSample(filmPoint, lensPoint, t, generateSamples(getAdditional1DSamples(),
				() -> Settings.RND.nextDouble(), (d1, d2) -> pow(d1 - d2, 2), (d) -> true, (d) -> {
				}),
				generateSamples(getAdditional2DSamples(),
						() -> new Point2D(Settings.RND.nextDouble(), Settings.RND.nextDouble()),
						(p1, p2) -> pow(p1.getX() - p2.getX(), 2) + pow(p1.getY() - p2.getY(), 2), (p) -> true, (p) -> {
						}));
		
		currentPixelSample++;
		if (currentPixelSample >= getSamplesPerPixel()) {
			
			currentPixelSample = 0;
			Util.shuffle(lensSamples);
			
			currentPixelX++;
			currentBlockX++;
			
			if (currentBlockX >= blockSize) {
				currentBlockX = 0;
				
				currentBlockY++;
				if (currentBlockY >= blockSize) {
					currentBlockY = 0;
					generateNewBlock();
				}
			}
			
			if (currentPixelX > getXEnd()) {
				currentPixelX = getXStart();
				currentBlockX = 0;
				
				currentPixelY++;
				currentBlockY++;
				
				if (currentBlockY >= blockSize) {
					currentBlockY = 0;
					generateNewBlock();
				}
			}
		}
		
		samplesGenerated++;
		
		return result;
	}
	
	private <T> List<T> generateSamples(int sampleCount, Supplier<T> sampleSupplier,
			BiFunction<T, T, Double> distanceMetric, Predicate<T> sampleFilter, Consumer<T> sampleConsumer) {
		
		final List<T> points = new ArrayList<>(sampleCount);
		
		final Function<T, Double> minPointDistance = (cp) -> points.stream()
				.mapToDouble(p -> distanceMetric.apply(cp, p)).min().orElse(0d);
		
		points.add(sampleSupplier.get());
		
		while (points.size() < sampleCount) {
			
			final var newPoint = IntStream.range(0, sampleCount).mapToObj(i -> sampleSupplier.get())
					.filter(sampleFilter)
					.sorted((p1, p2) -> Double.compare(minPointDistance.apply(p1), minPointDistance.apply(p2)))
					.findFirst();
			
			if (newPoint.isPresent()) {
				points.add(newPoint.get());
				sampleConsumer.accept(newPoint.get());
			}
		}
		
		return points;
	}
	
	private void generateNewBlock() {
		
		final Collection<Point2D> points = new ArrayList<>(blockSize * blockSize * getSamplesPerPixel());
		for (int x = 0; x < blockSize; x++)
			for (int y = 0; y < blockSize; y++)
				Arrays.fill(block[x][y], null);
			
		final var firstPoint = generatePoint(blockSize, blockSize);
		points.add(firstPoint);
		insertPointIntoBlock(firstPoint);
		
		final var desiredSize = blockSize * blockSize * getSamplesPerPixel();
		while (points.size() < desiredSize) {
			
			final Optional<Point2D> newPoint;
			
			if (points.size() < desiredSize / 2)
				newPoint = IntStream.range(0, blockSize).mapToObj(i -> generatePoint(blockSize, blockSize))
						.filter(p -> hasFreeIndex(block[(int) p.getX()][(int) p.getY()]))
						.sorted((p1, p2) -> Double.compare(distanceSq(p2, points), distanceSq(p1, points))).findFirst();
			
			else
				newPoint = getFreeIndices(block).stream()
						.map(indices -> generatePoint((int) indices[0], (int) indices[1], 1.0, 1.0))
						.sorted((p1, p2) -> Double.compare(distanceSq(p2, points), distanceSq(p1, points))).findFirst();
			
			if (newPoint.isPresent()) {
				points.add(newPoint.get());
				insertPointIntoBlock(newPoint.get());
			}
			
		}
	}
	
	private Point2D generatePoint(double xExtent, double yExtent) {
		
		return generatePoint(0, 0, xExtent, yExtent);
	}
	
	private Point2D generatePoint(int xOffset, int yOffset, double xExtent, double yExtent) {
		
		return new Point2D((double) xOffset + Settings.RND.nextDouble() * xExtent,
				(double) yOffset + Settings.RND.nextDouble() * yExtent);
	}
	
	private void insertPointIntoBlock(Point2D point) {
		
		final int x = (int) point.getX(), y = (int) point.getY();
		
		if (hasFreeIndex(block[x][y]))
			block[x][y][getFreeIndex(block[x][y])] = new Point2D(point.getX() - x, point.getY() - y);
	}
	
	private List<int[]> getFreeIndices(Object[][][] array) {
		
		final var result = new LinkedList<int[]>();
		
		for (int i = 0; i < array.length; i++)
			for (int j = 0; j < array[i].length; j++)
				for (int k = 0; k < array[i][j].length; k++) {
					if (array[i][j][k] == null)
						result.add(new int[] { i, j, k });
				}
		return result;
	}
	
	private boolean hasFreeIndex(Object[] array) {
		
		return (getFreeIndex(array) >= 0);
	}
	
	private int getFreeIndex(Object[] array) {
		
		for (int i = 0; i < array.length; i++)
			if (array[i] == null)
				return i;
		return -1;
	}
	
	private double distanceSq(Point2D cp, Collection<Point2D> points) {
		
		return points.stream().mapToDouble(p -> distanceSqBetween(p, cp)).min().orElse(0d);
	}
	
	private double distanceSqBetween(Point2D p1, Point2D p2) {
		
		return pow(p1.getX() - p2.getX(), 2) + pow(p1.getY() - p2.getY(), 2);
	}
	
	@Override
	public double getPercentComplete() {
		
		return ((double) samplesGenerated) / ((double) getTotalSamples());
	}
	
	@Override
	public Sampler partition(int xStart, int yStart, int xEnd, int yEnd) {
		
		return new BestCandidateSampler(xStart, yStart, xEnd, yEnd, getSamplesPerPixel(), getAdditional1DSamples(),
				getAdditional2DSamples());
	}
	
}
