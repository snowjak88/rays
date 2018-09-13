package org.snowjak.rays;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.annotations.bean.Node;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.camera.OrthographicCamera;
import org.snowjak.rays.camera.PinholeCamera;
import org.snowjak.rays.filter.BoxFilter;
import org.snowjak.rays.filter.Filter;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.light.PointLight;
import org.snowjak.rays.material.LambertianMaterial;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.material.PerfectMirrorMaterial;
import org.snowjak.rays.renderer.MonteCarloRenderer;
import org.snowjak.rays.renderer.PathTracingRenderer;
import org.snowjak.rays.renderer.Renderer;
import org.snowjak.rays.sampler.BestCandidateSampler;
import org.snowjak.rays.sampler.PseudorandomSampler;
import org.snowjak.rays.sampler.Sampler;
import org.snowjak.rays.serialization.IsLoadable;
import org.snowjak.rays.shape.PlaneShape;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.shape.SphereShape;
import org.snowjak.rays.spectrum.ColorMappingFunctions;
import org.snowjak.rays.spectrum.distribution.AnalyticColorMappingFunctions;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.snowjak.rays.spectrum.distribution.TabulatedColorMappingFunctions;
import org.snowjak.rays.texture.ConstantTexture;
import org.snowjak.rays.texture.ImageTexture;
import org.snowjak.rays.texture.Texture;
import org.snowjak.rays.texture.mapping.IdentityTextureMapping;
import org.snowjak.rays.texture.mapping.TextureMapping;
import org.snowjak.rays.texture.mapping.TilingTextureMapping;
import org.snowjak.rays.transform.RotationTransform;
import org.snowjak.rays.transform.ScaleTransform;
import org.snowjak.rays.transform.Transform;
import org.snowjak.rays.transform.TranslationTransform;

import com.google.common.math.DoubleMath;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ClassRefTypeSignature;

/**
 * Represents application-wide defaults. Typically these will be given by
 * properties specified in "<code>core-settings.properties</code>".
 * 
 * @author snowjak88
 *
 */
public class Settings {
	
	public enum ComponentSpectrumName {
		WHITE("white"), RED("red"), GREEN("green"), BLUE("blue"), CYAN("cyan"), MAGENTA("magenta"), YELLOW("yellow");
		
		private String name;
		
		ComponentSpectrumName(String name) {
			
			this.name = name;
		}
		
		public String getName() {
			
			return name;
		}
	}
	
	/**
	 * @see #getDoubleEqualityEpsilon()
	 */
	private double doubleEqualityEpsilon = 1e-8;
	
	/**
	 * @see #getSamplerBestCandidateBlockSize()
	 */
	private int samplerBestCandidateBlockSize = 16;
	
	/**
	 * @see #getSpectrumBinCount()
	 */
	private int spectrumBinCount = 16;
	
	/**
	 * @see #getSpectrumRangeLow()
	 */
	private double spectrumRangeLow = 360.0;
	/**
	 * @see #getSpectrumRangeHigh()
	 */
	private double spectrumRangeHigh = 830.0;
	
	/**
	 * @see #getSpectrumRange()
	 */
	private Pair<Double, Double> spectrumRange = null;
	
	/**
	 * @see #getColorMappingFunctionDistribution()
	 */
	private ColorMappingFunctions colorMappingFunctions = null;
	
	/**
	 * @see #getIlluminatorSpectralPowerDistribution()
	 */
	private SpectralPowerDistribution illuminatorSpectralPowerDistribution = null;
	
	/**
	 * @see #getComponentSpectra()
	 */
	private Map<ComponentSpectrumName, SpectralPowerDistribution> componentSpectra = null;
	
	/**
	 * @see #getCieXyzIntegrationStepCount()
	 */
	private int cieXyzIntegrationStepCount = 32;
	
	/**
	 * A shared {@link Random} instance.
	 */
	public static final Random RND = new Random(System.currentTimeMillis());
	
	/**
	 * @see #getGson()
	 */
	private Gson gson = null;
	
	private final Properties coreSettings;
	private static Settings __INSTANCE = null;
	
	public static Settings getInstance() {
		
		if (__INSTANCE == null)
			__INSTANCE = new Settings();
		
		return __INSTANCE;
	}
	
	private Settings() {
		//
		//
		// Try to load the core-settings properties file.
		
		try (var settingsStream = new FileInputStream("data" + File.separator + "core-settings.properties")) {
			
			coreSettings = new Properties();
			coreSettings.load(settingsStream);
			System.getProperties().putAll(coreSettings);
			
			//
			//
			// Now that we've loaded the core-settings into a Properties object,
			// we can start initializing those fields we care about.
			doubleEqualityEpsilon = Double.parseDouble(coreSettings.getProperty(
					"org.snowjak.rays.math.double-equality-epsilon", Double.toString(getDoubleEqualityEpsilon())));
			
			samplerBestCandidateBlockSize = Integer
					.parseInt(coreSettings.getProperty("org.snowjak.rays.sampler.best-candidate.block-size",
							Integer.toString(getSamplerBestCandidateBlockSize())));
			
			spectrumBinCount = Integer.parseInt(coreSettings.getProperty("org.snowjak.rays.spectrum-bin-count",
					Integer.toString(getSpectrumBinCount())));
			
			spectrumRangeLow = Double.parseDouble(coreSettings.getProperty("org.snowjak.rays.spectrum-range-low",
					Double.toString(getSpectrumRangeLow())));
			
			spectrumRangeHigh = Double.parseDouble(coreSettings.getProperty("org.snowjak.rays.spectrum-range-high",
					Double.toString(getSpectrumRangeHigh())));
			
			cieXyzIntegrationStepCount = Integer
					.parseInt(coreSettings.getProperty("org.snowjak.rays.cie-xyz-integration-step-count",
							Integer.toString(getCieXyzIntegrationStepCount())));
			
		} catch (Throwable t) {
			throw new CannotLoadSettingsException("Cannot load core settings!", t);
		}
		
		try (var scan = new ClassGraph().enableAllInfo().whitelistPackages("org.snowjak.rays").scan()) {
			
			final var gb = new GsonBuilder();
			
			for (ClassInfo ci : scan.getClassesImplementing(IsLoadable.class.getName())) {
				final var loadableClass = ((ClassRefTypeSignature) ci.getMethodInfo("deserialize").get(0)
						.getTypeDescriptor().getResultType()).getClassInfo().loadClass();
				
				gb.registerTypeAdapter(loadableClass, ci.loadClass().getConstructor().newInstance());
			}
			
			//
			// Register type-adapter for Transform implementations.
			//
			//@formatter:off
			gb.registerTypeAdapterFactory(
				RuntimeTypeAdapterFactory
						.of(Transform.class, "type")
						.registerSubtype(RotationTransform.class, "rotate")
						.registerSubtype(ScaleTransform.class, "scale")
						.registerSubtype(TranslationTransform.class, "translate"));
			//@formatter:on
			
			//
			// Register type-adapter for Shape implementations.
			//
			//@formatter:off
			gb.registerTypeAdapterFactory(
				RuntimeTypeAdapterFactory
						.of(Shape.class, "type")
						.registerSubtype(PlaneShape.class, "plane")
						.registerSubtype(SphereShape.class, "sphere"));
			//@formatter:on
			
			//
			// Register type-adapter for Light implementations.
			//
			//@formatter:off
			gb.registerTypeAdapterFactory(
				RuntimeTypeAdapterFactory
						.of(Light.class, "type")
						.registerSubtype(PointLight.class, "point"));
			//@formatter:on
			
			//
			// Register type-adapter for Camera implementations.
			//
			//@formatter:off
			gb.registerTypeAdapterFactory(
				RuntimeTypeAdapterFactory
						.of(Camera.class, "type")
						.registerSubtype(OrthographicCamera.class, "orthographic")
						.registerSubtype(PinholeCamera.class, "pinhole"));
			//@formatter:on
			
			//
			// Register type-adapter for Renderer implementations.
			//
			//@formatter:off
			gb.registerTypeAdapterFactory(
				RuntimeTypeAdapterFactory
						.of(Sampler.class, "type")
						.registerSubtype(PseudorandomSampler.class, "pseudorandom")
						.registerSubtype(BestCandidateSampler.class, "best-candidate"));
			//@formatter:on
			
			//
			// Register type-adapter for Renderer implementations.
			//
			//@formatter:off
			gb.registerTypeAdapterFactory(
				RuntimeTypeAdapterFactory
						.of(Renderer.class, "type")
						.registerSubtype(PathTracingRenderer.class, "path-tracing")
						.registerSubtype(MonteCarloRenderer.class, "monte-carlo"));
			//@formatter:on
			
			//
			// Register type-adapter for Filter implementations.
			//
			//@formatter:off
			gb.registerTypeAdapterFactory(
				RuntimeTypeAdapterFactory
						.of(Filter.class, "type")
						.registerSubtype(BoxFilter.class, "box"));
			//@formatter:on
			
			//
			// Register type-adapter for Material implementations.
			//
			//@formatter:off
			gb.registerTypeAdapterFactory(
				RuntimeTypeAdapterFactory
						.of(Material.class, "type")
						.registerSubtype(LambertianMaterial.class, "lambertian")
						.registerSubtype(PerfectMirrorMaterial.class, "perfect-mirror"));
			//@formatter:on
			
			//
			// Register type-adapter for Texture implementations.
			//
			//@formatter:off
			gb.registerTypeAdapterFactory(
				RuntimeTypeAdapterFactory
						.of(Texture.class, "type")
						.registerSubtype(ImageTexture.class, "image")
						.registerSubtype(ConstantTexture.class, "constant"));
			//@formatter:on
			
			//
			// Register type-adapter for TextureMapping implementations.
			//
			//@formatter:off
			gb.registerTypeAdapterFactory(
				RuntimeTypeAdapterFactory
						.of(TextureMapping.class, "type")
						.registerSubtype(IdentityTextureMapping.class, "identity")
						.registerSubtype(TilingTextureMapping.class, "tiling"));
			//@formatter:on
			
			gb.registerTypeHierarchyAdapter(Node.class, new Node.Serializer());
			
			this.gson = gb.create();
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException("Cannot register JSON de/serialization handlers!", e);
		}
	}
	
	/**
	 * Two <code>double</code> values are considered to be "basically equal" if they
	 * are closer than this value.
	 * <p>
	 * Mapped to <code>org.snowjak.rays.math.double-equality-epsilon</code>
	 * </p>
	 * 
	 * @see #nearlyEqual(double, double)
	 */
	public double getDoubleEqualityEpsilon() {
		
		return doubleEqualityEpsilon;
	}
	
	/**
	 * When using the {@link BestCandidateSampler}, generate samples enough to cover
	 * a <code>blockSize</code> x <code>blockSize</code> block of pixels at once.
	 */
	public int getSamplerBestCandidateBlockSize() {
		
		return samplerBestCandidateBlockSize;
	}
	
	/**
	 * When performing spectral raytracing, each {@link SpectralPowerDistribution}
	 * we cast should contain so many "bins" -- i.e., distinct power-readings across
	 * its wavelength-domain.
	 */
	public int getSpectrumBinCount() {
		
		return spectrumBinCount;
	}
	
	/**
	 * When performing spectral raytracing, each {@link SpectralPowerDistribution}
	 * should span a certain wavelength-domain.
	 * 
	 * @see #getSpectrumRangeHigh()
	 * @see #getSpectrumRange()
	 */
	public double getSpectrumRangeLow() {
		
		return spectrumRangeLow;
	}
	
	/**
	 * When performing spectral raytracing, each {@link SpectralPowerDistribution}
	 * should span a certain wavelength-domain.
	 * 
	 * @see #getSpectrumRangeLow()
	 * @see #getSpectrumRange()
	 */
	public double getSpectrumRangeHigh() {
		
		return spectrumRangeHigh;
	}
	
	/**
	 * When performing spectral raytracing, each {@link SpectralPowerDistribution}
	 * should span a certain wavelength-domain.
	 * 
	 * @see #getSpectrumRangeLow()
	 * @see #getSpectrumRangeHigh()
	 */
	public Pair<Double, Double> getSpectrumRange() {
		
		if (spectrumRange == null)
			spectrumRange = new Pair<>(getSpectrumRangeLow(), getSpectrumRangeHigh());
		
		return spectrumRange;
	}
	
	/**
	 * The distribution of color-mapping-functions (for calculating {@link CIEXYZ}
	 * triplets from spectra).
	 */
	public ColorMappingFunctions getColorMappingFunctions() {
		
		if (colorMappingFunctions == null)
			if (coreSettings.containsKey("org.snowjak.rays.cie-csv-xyz-color-mapping-path"))
				try {
					colorMappingFunctions = TabulatedColorMappingFunctions.loadFromCSV(new FileInputStream(
							coreSettings.getProperty("org.snowjak.rays.cie-csv-xyz-color-mapping-path")));
				} catch (IOException e) {
					//
					//
					colorMappingFunctions = new AnalyticColorMappingFunctions();
				}
			else
				colorMappingFunctions = new AnalyticColorMappingFunctions();
			
		return colorMappingFunctions;
	}
	
	/**
	 * The spectral power-distribution associated with the standard illuminator.
	 * (Used to calculate {@link CIEXYZ} triplets from spectra.)
	 */
	public SpectralPowerDistribution getIlluminatorSpectralPowerDistribution() {
		
		if (illuminatorSpectralPowerDistribution == null)
			try {
				
				illuminatorSpectralPowerDistribution = SpectralPowerDistribution
						.loadFromCSV(new FileInputStream(
								coreSettings.getProperty("org.snowjak.rays.cie-csv-xyz-d65-standard-illuminator-path")))
						.normalize();
				
			} catch (IOException e) {
				//
				//
				throw new RuntimeException("Could not load standard-illuminator SDP.", e);
			}
		
		return illuminatorSpectralPowerDistribution;
	}
	
	public Map<ComponentSpectrumName, SpectralPowerDistribution> getComponentSpectra() {
		
		if (componentSpectra == null) {
			componentSpectra = new HashMap<>();
			
			for (ComponentSpectrumName name : ComponentSpectrumName.values()) {
				
				final String filePath = coreSettings.getProperty("org.snowjak.rays.component-spectra-path")
						+ File.separator + name.getName() + ".csv";
				try {
					
					final InputStream stream = new FileInputStream(filePath);
					componentSpectra.put(name, SpectralPowerDistribution.loadFromCSV(stream));
					
				} catch (IOException e) {
					//
					//
					throw new RuntimeException("Could not load component-spectrum from [" + filePath + "].", e);
				}
			}
		}
		
		return componentSpectra;
	}
	
	/**
	 * When calculating a CIE XYZ triplet from a {@link SpectralPowerDistribution},
	 * we need to compute a definite integral across a range of wavelengths. How
	 * many steps should we divide the total integral-range into, when calculating
	 * that definite integral?
	 */
	public int getCieXyzIntegrationStepCount() {
		
		return cieXyzIntegrationStepCount;
		
	}
	
	public Gson getGson() {
		
		return gson;
	}
	
	/**
	 * Determines whether two <code>double</code>s are "nearly equal".
	 * 
	 * @param d1
	 *            a <code>double</code> value
	 * @param d2
	 *            another <code>double</code> value
	 * @return <code>true</code> if these two values are within
	 *         {@link #doubleEqualityEpsilon} of each other
	 * @see #doubleEqualityEpsilon
	 */
	public boolean nearlyEqual(double d1, double d2) {
		
		return DoubleMath.fuzzyEquals(d1, d2, getDoubleEqualityEpsilon());
	}
	
	public static class CannotLoadSettingsException extends RuntimeException {
		
		private static final long serialVersionUID = 1636232835175064483L;
		
		public CannotLoadSettingsException() {
			
			super();
		}
		
		public CannotLoadSettingsException(String message, Throwable cause) {
			
			super(message, cause);
		}
		
		public CannotLoadSettingsException(String message) {
			
			super(message);
		}
		
		public CannotLoadSettingsException(Throwable cause) {
			
			super(cause);
		}
		
	}
	
}
