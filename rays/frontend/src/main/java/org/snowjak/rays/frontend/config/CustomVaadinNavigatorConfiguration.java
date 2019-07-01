package org.snowjak.rays.frontend.config;

import org.snowjak.rays.frontend.security.SecurityConsciousSpringNavigator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.navigator.SpringNavigator;

@Configuration
public class CustomVaadinNavigatorConfiguration {
	
	@Bean
	@UIScope
	public SpringNavigator vaadinNavigator() {
		
		return new SecurityConsciousSpringNavigator();
	}
	
}
