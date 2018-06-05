package org.snowjak.rays.spectrum;

import static org.apache.commons.math3.util.FastMath.pow;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.util.Matrix;
import org.snowjak.rays.geometry.util.Triplet;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Represents a CIE XYZ tristimulus triplet (i.e., see
 * <a href="https://en.wikipedia.org/wiki/CIE_1931_color_space">the CIE 1931
 * color-space</a>).
 * 
 * <p>
 * The color-matching function data used here is the "2-deg XYZ CMFs transformed
 * from the CIE (2006) 2-deg LMS cone fundamentals" data, obtained from the
 * <a href="http://www.cvrl.org/">CVRL.org</a> database.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class CIEXYZ implements Serializable {
	
	private static final long serialVersionUID = 6070836284173038489L;
	
	public static final NavigableMap<Double, Triplet> COLOR_MAPPING_FUNCTIONS = Collections
			.unmodifiableNavigableMap(new XyzFileLoader().loadColorMappingFunctions());
	
	//@formatter:off
		private static final Matrix __CONVERSION_TO_RGB =
				new Matrix(new double[][] {
					{ 3.2406d,-1.5372d,-0.4986d, 0d },
					{-0.9689d, 1.8758d, 0.0415d, 0d },
					{ 0.0557d,-0.2040d, 1.0570d, 0d },
					{ 0d,      0d,      0d,      0d }
				});
		//@formatter:on
	
	//@formatter:off
	private static final Matrix __CONVERSION_FROM_RGB =
			new Matrix(new double[][] {
				{ 0.4124d, 0.3576d, 0.1805d, 0d },
				{ 0.2126d, 0.7152d, 0.0722d, 0d },
				{ 0.0193d, 0.1192d, 0.9505d, 0d },
				{ 0d,      0d,      0d,      0d }
			});
	//@formatter:on
	
	/**
	 * Calculate the CIE XYZ tristimulus triplet associated with the given
	 * wavelength (assumed to be expressed in nanometers). This method will
	 * linearly-interpolate between neighboring nodes in the loaded color-mapping
	 * function (given, e.g., at 1-nm increments)
	 * 
	 * @param wavelength
	 *            a given wavelength (given in nanometers)
	 * @return a CIE-XYZ tristimulus triplet representing the color-mapping
	 *         functions for the given wavelength
	 */
	public static CIEXYZ fromWavelength(double wavelength) {
		
		if (wavelength < COLOR_MAPPING_FUNCTIONS.firstKey())
			return new CIEXYZ(COLOR_MAPPING_FUNCTIONS.get(COLOR_MAPPING_FUNCTIONS.firstKey()));
		
		if (wavelength > COLOR_MAPPING_FUNCTIONS.lastKey())
			return new CIEXYZ(COLOR_MAPPING_FUNCTIONS.get(COLOR_MAPPING_FUNCTIONS.lastKey()));
		
		final Entry<Double, Triplet> lower = COLOR_MAPPING_FUNCTIONS.floorEntry(wavelength),
				upper = COLOR_MAPPING_FUNCTIONS.ceilingEntry(wavelength);
		
		final double fraction = (wavelength - lower.getKey()) / (upper.getKey() - lower.getKey());
		
		final Triplet lowerTriplet = lower.getValue(), upperTriplet = upper.getValue();
		
		return new CIEXYZ(upperTriplet.subtract(lowerTriplet).multiply(fraction).add(lowerTriplet));
		
	}
	
	/**
	 * Construct a new CIE XYZ triplet from a {@link RGB} triplet (where the RGB
	 * triplet is assumed to be in the
	 * <a href="https://en.wikipedia.org/wiki/SRGB">sRGB color-space</a>).
	 * <p>
	 * <strong>Note</strong> that each component of the RGB triplet is clamped to
	 * <code>[0,1]</code> as part of conversion.
	 * </p>
	 * 
	 * @param rgb
	 *            an RGB triplet, assumed to lie in the sRGB color-space
	 * @return a CIE XYZ triplet representing the equivalent color in the CIE 1931
	 *         color-space
	 */
	public static CIEXYZ fromRGB(RGB rgb) {
		
		Triplet linear = new Triplet(rgb.getComponents()).clamp(0d, 1d)
				.apply(c -> (c <= 0.04045d) ? (c / 12.92d) : (pow((c + 0.055d) / 1.055d, 2.4d)));
		
		return new CIEXYZ(__CONVERSION_FROM_RGB.multiply(linear, 0d));
	}
	
	private Triplet xyz;
	
	public CIEXYZ() {
		
		this(0d, 0d, 0d);
	}
	
	public CIEXYZ(double x, double y, double z) {
		
		this(new Triplet(x, y, z));
	}
	
	public CIEXYZ(Triplet xyz) {
		
		this.xyz = xyz;
	}
	
	/**
	 * Convert this CIE XYZ triplet to an {@link RGB} triplet (assumed to be in the
	 * sRGB color-space).
	 * 
	 * @return
	 */
	public RGB toRGB() {
		
		return new RGB(__CONVERSION_TO_RGB.multiply(xyz, 0d)
				.apply(c -> (c <= 0.0031308d) ? (12.92d * c) : (1.055d * pow(c, 1d / 2.4d) - 0.055d)));
	}
	
	public double getX() {
		
		return xyz.get(0);
	}
	
	protected void setX(double x) {
		
		xyz = new Triplet(x, getY(), getZ());
	}
	
	public double getY() {
		
		return xyz.get(1);
	}
	
	protected void setY(double y) {
		
		xyz = new Triplet(getX(), y, getZ());
	}
	
	public double getZ() {
		
		return xyz.get(2);
	}
	
	protected void setZ(double z) {
		
		xyz = new Triplet(getX(), getY(), z);
	}
	
	/**
	 * Loader helper-class, encapsulating the functionality needed to load an XML
	 * file containing CIE XYZ color-mapping-function data.
	 * 
	 * @author snowjak88
	 * @see XyzSaxHandler XyzSaxHandler for details on XML file-structure
	 *
	 */
	public static class XyzFileLoader {
		
		public NavigableMap<Double, Triplet> loadColorMappingFunctions() {
			
			var xyzSaxHandler = new XyzSaxHandler();
			try (var xmlInputStream = this.getClass().getClassLoader()
					.getResourceAsStream(Settings.getCieColorMappingFunctionsPath())) {
				
				SAXParserFactory.newInstance().newSAXParser().parse(xmlInputStream, xyzSaxHandler);
				return xyzSaxHandler.getColorMappingFunctions();
				
			} catch (IOException | ParserConfigurationException | SAXException e) {
				throw new RuntimeException("Could not load CIE-XYZ color-mapping functions.", e);
			}
		}
	}
	
	/**
	 * SAX {@link ContentHandler} implementation that converts a given XML file
	 * containing CIE XYZ color-mapping-function readings into a Map from
	 * wavelengths to {@link Triplet}s.
	 * 
	 * <p>
	 * The XML file is expected to be of the form:
	 * 
	 * <pre>
	 * &lt;Root&gt;
	 *   &lt;Structure&gt;
	 *     &lt;Field&gt;
	 *       &lt;Field_Name&gt;...&lt;/Field_Name&gt;
	 *       ...
	 *   &lt;/Structure&gt;
	 *   &lt;Records&gt;
	 *     &lt;Record&gt;
	 *       &lt;Field1&gt;<em>wavelength</em>&lt;/Field1&gt;
	 *       &lt;Field2&gt;<em>X</em>&lt;/Field2&gt;
	 *       &lt;Field3&gt;<em>Y</em>&lt;/Field3&gt;
	 *       &lt;Field4&gt;<em>Z</em>&lt;/Field4&gt;
	 *     &lt;/Record&gt;
	 *     &lt;Record&gt;
	 *       ...
	 * </pre>
	 * </p>
	 * 
	 * @author snowjak88
	 *
	 */
	public static class XyzSaxHandler extends DefaultHandler {
		
		private enum XyzSection {
			STRUCTURE, RECORDS
		};
		
		private enum XyzField {
			LAMBDA, X, Y, Z
		};
		
		private NavigableMap<Double, Triplet> triplets = new TreeMap<>();
		private NavigableMap<Double, Triplet> __current_building_map = new TreeMap<>();
		
		private Double __current_building_wavelength = null;
		private Triplet __current_building_entry = null;
		private XyzSection __currentSection = null;
		private XyzField __currentField = null;
		private StringBuilder __currentFieldBuilder = null;
		
		private Pattern fieldNamePattern = Pattern.compile("Field(\\d+)");
		
		private Locator documentLocator;
		
		/**
		 * @return the current Map of wavelengths to {@link Triplet}s, or an empty Map
		 *         if no such XML file has been successfully loaded yet
		 */
		public NavigableMap<Double, Triplet> getColorMappingFunctions() {
			
			return triplets;
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes)
				throws SAXException {
			
			Matcher fieldNameMatcher;
			
			if (qName.equals("Structure")) {
				__currentSection = XyzSection.STRUCTURE;
				
			} else if (qName.equals("Records")) {
				if (__currentSection == XyzSection.RECORDS)
					throw new SAXParseException("Encountered a <Records/> nested inside of a <Records/>",
							this.documentLocator);
				else if (__currentSection == XyzSection.STRUCTURE)
					throw new SAXParseException("Encountered a <Records/> nested inside of a <Structure/>",
							this.documentLocator);
				__currentSection = XyzSection.RECORDS;
				
			} else if (qName.equals("Record")) {
				if (__currentSection != XyzSection.RECORDS)
					throw new SAXParseException("Encountered a <Record/> outside of a <Records/>",
							this.documentLocator);
				
				__current_building_entry = new Triplet();
				
			} else if ((fieldNameMatcher = fieldNamePattern.matcher(qName)).matches()) {
				if (__currentSection == XyzSection.RECORDS) {
					
					__currentFieldBuilder = new StringBuilder();
					
					// Figure out what field we're currently building.
					switch (fieldNameMatcher.group(1)) {
					case "1":
						__currentField = XyzField.LAMBDA;
						break;
					case "2":
						__currentField = XyzField.X;
						break;
					case "3":
						__currentField = XyzField.Y;
						break;
					case "4":
						__currentField = XyzField.Z;
						break;
					default:
						throw new SAXParseException(
								"Encountered an unrecognized <Field.../> variant: \"" + qName + "\"", documentLocator);
					}
					
				} else if (__currentSection == XyzSection.STRUCTURE) {
					// Ignore all given "Structure"/"Field" entries
				} else {
					throw new SAXParseException("Encountered a <Field/> outside of a <Records/> or <Structure/>",
							this.documentLocator);
				}
			}
			
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			
			if (__currentFieldBuilder != null)
				__currentFieldBuilder.append(ch, start, length);
		}
		
		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			
			if (qName.equals("Structure") || qName.equals("Records")) {
				__currentSection = null;
				
			} else if (fieldNamePattern.matcher(qName).matches()) {
				
				final String currentFieldValue = __currentFieldBuilder.toString();
				
				switch (__currentField) {
				case LAMBDA:
					try {
						__current_building_wavelength = Double.parseDouble(currentFieldValue);
					} catch (NumberFormatException e) {
						throw new SAXParseException(
								"Cannot parse given wavelength, \"" + currentFieldValue + "\", as a double-value!",
								documentLocator);
					}
					break;
				case X:
					try {
						__current_building_entry = new Triplet(Double.parseDouble(currentFieldValue),
								__current_building_entry.get(1), __current_building_entry.get(2));
					} catch (NumberFormatException e) {
						throw new SAXParseException(
								"Cannot parse given X, \"" + currentFieldValue + "\", as a double-value!",
								documentLocator);
					}
					break;
				case Y:
					try {
						__current_building_entry = new Triplet(__current_building_entry.get(0),
								Double.parseDouble(currentFieldValue), __current_building_entry.get(2));
					} catch (NumberFormatException e) {
						throw new SAXParseException(
								"Cannot parse given Y, \"" + currentFieldValue + "\", as a double-value!",
								documentLocator);
					}
					break;
				case Z:
					try {
						__current_building_entry = new Triplet(__current_building_entry.get(0),
								__current_building_entry.get(1), Double.parseDouble(currentFieldValue));
					} catch (NumberFormatException e) {
						throw new SAXParseException(
								"Cannot parse given Z, \"" + currentFieldValue + "\", as a double-value!",
								documentLocator);
					}
					break;
				}
				
				__currentField = null;
				__currentFieldBuilder = null;
				
			} else if (qName.equals("Record")) {
				__current_building_map.put(__current_building_wavelength, __current_building_entry);
			}
		}
		
		@Override
		public void endDocument() throws SAXException {
			
			this.triplets = __current_building_map;
		}
		
		@Override
		public void setDocumentLocator(Locator locator) {
			
			this.documentLocator = locator;
		}
		
	}
}
