package org.snowjak.rays.sampler;

import static org.apache.commons.math3.util.FastMath.log;
import static org.apache.commons.math3.util.FastMath.min;
import static org.apache.commons.math3.util.FastMath.pow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.snowjak.rays.Settings;
import org.snowjak.rays.Util;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.geometry.util.Pair;
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
public class BestCandidateSampler extends Sampler {
	
	private final int blockSize;
	private final Point2D[][][] block;
	
	private final Point2D[] lensSamples;
	
	private int currentBlockX, currentBlockY;
	
	private int currentPixelX, currentPixelY, currentPixelSample;
	
	public BestCandidateSampler(long renderId, int xStart, int yStart, int xEnd, int yEnd, int samplesPerPixel,
			int additional1dSamples, int additional2dSamples) {
		
		super(renderId, xStart, yStart, xEnd, yEnd, samplesPerPixel, additional1dSamples, additional2dSamples);
		
		final int minDimensionSize = min(xEnd - xStart + 1, yEnd - yStart + 1);
		this.blockSize = (minDimensionSize >= Settings.getInstance().getSamplerBestCandidateBlockSize())
				? Settings.getInstance().getSamplerBestCandidateBlockSize()
				: (int) pow(2d, (int) log(2d, minDimensionSize));
		
		this.block = new Point2D[blockSize][blockSize][samplesPerPixel];
		generateNewBlock();
		
		this.lensSamples = generateSamples(getAdditional2DSamples(),
				() -> new Point2D(Settings.RND.nextDouble(), Settings.RND.nextDouble()),
				(p1, p2) -> pow(p1.getX() - p2.getX(), 2) + pow(p1.getY() - p2.getY(), 2), (p) -> true, (p) -> {
				}).toArray(new Point2D[0]);
		
		this.currentBlockX = 0;
		this.currentBlockY = 0;
		
		this.currentPixelX = xStart;
		this.currentPixelY = yStart;
		this.currentPixelSample = 0;
	}
	
	@Override
	public boolean hasNextSample() {
		
		return currentPixelY <= getYEnd();
	}
	
	@Override
	public Sample getNextSample() {
		
		final var filmPoint = block[currentBlockX][currentBlockY][currentPixelSample]
				.add(new Pair(currentPixelX, currentPixelY));
		final var lensPoint = lensSamples[currentPixelSample];
		
		final var result = new Sample(getRenderId(), getSamplesPerPixel(), filmPoint, lensPoint, null, null,
				generateSamples(getAdditional1DSamples(), () -> Settings.RND.nextDouble(), (d1, d2) -> pow(d1 - d2, 2),
						(d) -> true, (d) -> {
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
		
		while (points.size() < blockSize * blockSize * getSamplesPerPixel()) {
			
			final var newPoint = IntStream.range(0, blockSize).mapToObj(i -> generatePoint(blockSize, blockSize))
					.filter(p -> hasFreeIndex(block[(int) p.getX()][(int) p.getY()]))
					.sorted((p1, p2) -> Double.compare(distanceSq(p1, points), distanceSq(p2, points))).findFirst();
			
			if (newPoint.isPresent()) {
				points.add(newPoint.get());
				insertPointIntoBlock(newPoint.get());
			}
			
		}
	}
	
	private Point2D generatePoint(double xExtent, double yExtent) {
		
		return new Point2D(Settings.RND.nextDouble() * xExtent, Settings.RND.nextDouble() * yExtent);
	}
	
	private void insertPointIntoBlock(Point2D point) {
		
		final int x = (int) point.getX(), y = (int) point.getY();
		
		if (hasFreeIndex(block[x][y]))
			block[x][y][getFreeIndex(block[x][y])] = new Point2D(point.getX() - x, point.getY() - y);
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
	
}
