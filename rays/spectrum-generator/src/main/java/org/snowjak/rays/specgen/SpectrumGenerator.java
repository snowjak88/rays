package org.snowjak.rays.specgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.spectrum.CIEXYZ;
import org.snowjak.rays.spectrum.RGB;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SpectrumGenerator is an application that can pre-generate spectra
 * corresponding to different CIE-XYZ triplets.
 * <p>
 * Oftentimes, we will want to model spectrum-transport for more photo-realistic
 * rendering. However, we can rarely find colors and materials defined in terms
 * of their spectral power-distributions! Moreover, constructing arbitrary
 * power-distributions to fit existing color triplets (whether sRGB or CIE-XYZ)
 * is prohibitively time-intensive.
 * </p>
 * <p>
 * Accordingly, this application is intended to help you pre-generate arbitrary
 * spectral power-distributions, to be persisted into the file-system.
 * </p>
 * 
 * @author snowjak88
 *
 */
@SpringBootApplication
public class SpectrumGenerator implements CommandLineRunner {
	
	private static final Logger LOG = LoggerFactory.getLogger(SpectrumGenerator.class);
	
	public static void main(String[] args) {
		
		SpringApplication.run(SpectrumGenerator.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {
		
		final var rgb = new RGB(0.5, 0.5, 0.5);
		final var xyz = CIEXYZ.fromRGB(rgb);
		
		LOG.info("Starting from " + rgb.toString());
		LOG.info("CIE XYZ = " + xyz.toString());
		
		final var result = new BruteForceSearchSpectrumGeneratorJob(BruteForceSearchSpectrumGeneratorJob.TABLE, xyz, 6,
				0d, +1d, 0.01d, 0.01, 16).generate();
		
		final var resultingXYZ = CIEXYZ.fromSpectrum(result);
		
		LOG.info("Found XYZ = " + resultingXYZ.toString());
		LOG.info("-> RGBColorspace = " + resultingXYZ.toRGB().toString());
		
	}
	
}
