package org.snowjak.rays.frontend.security;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.frontend.messages.frontend.SuccessfulLogin;
import org.snowjak.rays.frontend.messages.frontend.SuccessfulLogout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.EventBus;

/**
 * Implements Spring Security operations programmatically (i.e., without relying
 * on URL-driven filters).
 * 
 * @author snowjak88
 *
 */
@Component
@Scope(scopeName = "vaadin-session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SecurityOperations {
	
	private static final Logger LOG = LoggerFactory.getLogger(SecurityOperations.class);
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private EventBus bus;
	
	/**
	 * Returns <code>true</code> if an {@link Authentication} exists in the current
	 * {@link SecurityContext}, <em>and</em> it is not an anonymous authentication.
	 * 
	 * @return
	 */
	public boolean isAuthenticated() {
		
		return (SecurityContextHolder.getContext().getAuthentication() != null)
				&& (SecurityContextHolder.getContext().getAuthentication().isAuthenticated())
				&& !(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);
	}
	
	public Authentication doLogIn(String username, String password) throws SecurityOperationException {
		
		LOG.info("Performing log-in request ...");
		
		LOG.debug("Building username/password token ...");
		final Authentication preAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password);
		
		LOG.debug("Attempting to authenticate token ...");
		final Authentication authenticatedToken;
		try {
			authenticatedToken = authenticationManager.authenticate(preAuthenticationToken);
		} catch (AuthenticationException e) {
			
			LOG.info("Could not complete log-in -- {}: {}", e.getClass().getSimpleName(), e.getMessage());
			throw new SecurityOperationException(e);
		}
		
		LOG.debug("Storing the authenticated token in the SecurityContext ...");
		SecurityContextHolder.getContext().setAuthentication(authenticatedToken);
		
		LOG.trace("Signalling the completed log-in.");
		bus.post(new SuccessfulLogin(authenticatedToken));
		
		LOG.info("Completed log-in request.");
		return authenticatedToken;
	}
	
	public void doLogOut() {
		
		LOG.info("Performing log-out request ...");
		
		final Authentication oldAuthentication = SecurityContextHolder.getContext().getAuthentication();
		
		LOG.debug("Deleting authentication from security context ...");
		SecurityContextHolder.getContext().setAuthentication(null);
		
		LOG.debug("Clearing security context ...");
		SecurityContextHolder.clearContext();
		
		LOG.debug("Publishing logout event ...");
		bus.post(new SuccessfulLogout(oldAuthentication));
		
		LOG.info("Completed log-out.");
	}
	
	public Collection<String> getAuthorities() {
		
		LOG.debug("Get active authorities ...");
		
		if (!isAuthenticated()) {
			LOG.debug("Current session is not authenticated -- therefore, no authorities to return.");
			return Collections.emptyList();
		}
		
		final var result = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
				.map(ga -> ga.getAuthority()).collect(Collectors.toList());
		LOG.trace("Principal as authorities: {}", result);
		return result;
	}
	
	public boolean hasAuthority(String authority) {
		
		final var result = getAuthorities().contains(authority);
		LOG.trace("Principal has authority \"{}\"? ==> {}", authority, result);
		return result;
	}
	
}
