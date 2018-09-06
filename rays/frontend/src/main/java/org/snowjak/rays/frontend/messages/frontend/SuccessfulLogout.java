package org.snowjak.rays.frontend.messages.frontend;

import org.springframework.security.core.Authentication;

public class SuccessfulLogout {
	
	private Authentication authentication;
	
	public SuccessfulLogout(Authentication authentication) {
		
		this.authentication = authentication;
	}
	
	public Authentication getAuthentication() {
		
		return authentication;
	}
	
}
