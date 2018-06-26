package org.snowjak.rays.specgen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("spectrum-generator")
public class SpectrumGeneratorProperties {
	
	private Set<String> availableGenerators = new HashSet<>();
	private Set<String> availableColors = new HashSet<>();
	private Map<String, List<Double>> colorDefinitions = new HashMap<>();
	
	public Set<String> getAvailableGenerators() {
		
		return availableGenerators;
	}
	
	public Set<String> getAvailableColors() {
		
		return availableColors;
	}
	
	public Map<String, List<Double>> getColorDefinitions() {
		
		return colorDefinitions;
	}
	
}