package org.snowjak.rays.frontend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
	
}
