package org.snowjak.rays.frontend.messages.frontend;

import org.springframework.security.core.Authentication;

public class SuccessfulLogin {
	
	private Authentication authentication;
	
	public SuccessfulLogin(Authentication authentication) {
		
		this.authentication = authentication;
	}
	
	public Authentication getAuthentication() {
		
		return authentication;
	}
	
}
