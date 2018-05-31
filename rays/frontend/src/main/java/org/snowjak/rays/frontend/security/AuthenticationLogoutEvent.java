package org.snowjak.rays.frontend.security;

import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.core.Authentication;

/**
 * Represents a "logout" event. As such, calling {@link #getAuthentication()} on
 * this event will always return the just-logged-out {@link Authentication}
 * object.
 * 
 * @author snowjak88
 *
 */
public class AuthenticationLogoutEvent extends AbstractAuthenticationEvent {
	
	private static final long serialVersionUID = 6781445881137973409L;
	
	public AuthenticationLogoutEvent(Authentication authentication) {
		
		super(authentication);
	}
	
}
