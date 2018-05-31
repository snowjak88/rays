package org.snowjak.rays.frontend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class WebConfigurer implements WebMvcConfigurer {
	
	@Autowired
	private I18nConfig i18n;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		
		registry.addInterceptor(i18n.localeChangeInterceptor());
	}
	
}
