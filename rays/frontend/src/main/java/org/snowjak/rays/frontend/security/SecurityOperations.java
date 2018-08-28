package org.snowjak.rays.frontend.security;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Implements Spring Security operations programmatically (i.e., without relying
 * on URL-driven filters).
 * 
 * @author snowjak88
 *
 */
@Component
public class SecurityOperations {
	
	private static final Logger LOG = LoggerFactory.getLogger(SecurityOperations.class);
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private ApplicationEventPublisher publisher;
	
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
		publisher.publishEvent(new AfterSuccessfulLoginEvent(authenticatedToken));
		
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
		publisher.publishEvent(new AuthenticationLogoutEvent(oldAuthentication));
		
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
