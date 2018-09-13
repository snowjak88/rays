package org.snowjak.rays.frontend.security;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.frontend.messages.frontend.RunInUIThread;
import org.snowjak.rays.frontend.messages.frontend.SuccessfulLogin;
import org.snowjak.rays.frontend.messages.frontend.SuccessfulLogout;
import org.snowjak.rays.frontend.ui.FrontEndUI;
import org.snowjak.rays.frontend.ui.UnauthorizedView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.navigator.View;
import com.vaadin.spring.navigator.SpringNavigator;

public class SecurityConsciousSpringNavigator extends SpringNavigator {
	
	private static final long serialVersionUID = 2019892667529552798L;
	private static final Logger LOG = LoggerFactory.getLogger(SecurityConsciousSpringNavigator.class);
	
	@Autowired
	private EventBus bus;
	
	@Autowired
	@Lazy
	private SecurityOperations security;
	
	@PostConstruct
	public void postConstruct() {
		
		bus.register(this);
	}
	
	@PreDestroy
	public void preDestroy() {
		
		bus.unregister(this);
	}
	
	@Subscribe
	public void onLogOut(SuccessfulLogout logout) {
		
		LOG.debug("Successful logout -- redirecting to home-page view (\"{}\") ...", FrontEndUI.HomeView.NAME);
		bus.post(new RunInUIThread(() -> navigateTo(FrontEndUI.HomeView.NAME)));
	}
	
	@Subscribe
	public void onLogIn(SuccessfulLogin login) {
		
		LOG.debug("Successful login -- redirecting to home-page view (\"{}\") ...", FrontEndUI.HomeView.NAME);
		bus.post(new RunInUIThread(() -> navigateTo(FrontEndUI.HomeView.NAME)));
	}
	
	@Override
	protected void navigateTo(View view, String viewName, String parameters) {
		
		LOG.debug("Authenticating navigateTo(\"{}\", ...)", viewName);
		
		if (isAuthorized(view)) {
			
			LOG.debug("Authenticated navigateTo(\"{}\",...).", viewName);
			super.navigateTo(view, viewName, parameters);
			
		} else {
			
			LOG.debug("Did not authenticate navigateTo(\"{}\",...). Redirecting to \"unauthorized\" view (\"{}\") ...",
					viewName, UnauthorizedView.NAME);
			super.navigateTo(UnauthorizedView.NAME);
			
		}
	}
	
	private boolean isAuthorized(View view) {
		
		LOG.trace("Does target View have an @AuthorizedViews?");
		if (view.getClass().isAnnotationPresent(AuthorizedViews.class)) {
			
			LOG.trace("Authenticating against @AuthorizedViews ...");
			final var annotation = view.getClass().getAnnotation(AuthorizedViews.class);
			final var isAuthenticated = Arrays.stream(annotation.value()).anyMatch(this::isAuthorized);
			
			if (isAuthenticated) {
				
				LOG.trace("At least one of the @AuthorizedViews's rules is successful.");
				return true;
				
			} else {
				
				LOG.trace("None of the @AuthorizedViews's rules are successful.");
				return false;
				
			}
			
		}
		
		LOG.trace("Does target View have an @AuthorizedView?");
		if (view.getClass().isAnnotationPresent(AuthorizedView.class)) {
			
			LOG.trace("Authenticating against @AuthorizedView ...");
			final var annotation = view.getClass().getAnnotation(AuthorizedView.class);
			final var isAuthenticated = this.isAuthorized(annotation);
			
			if (isAuthenticated) {
				
				LOG.trace("@AuthorizedView rule is successful.");
				return true;
				
			} else {
				
				LOG.trace("@AuthorizedView rule is not successful.");
				return false;
				
			}
			
		}
		
		LOG.trace(
				"Target View has neither @AuthorizedViews nor @AuthorizedView. Navigation is authenticated by default.");
		return true;
	}
	
	private boolean isAuthorized(AuthorizedView annotation) {
		
		final Set<String> authorities = new HashSet<>();
		
		if (annotation.value() != null)
			authorities.addAll(Arrays.asList(annotation.value()));
		if (annotation.authorities() != null)
			authorities.addAll(Arrays.asList(annotation.authorities()));
		
		return authorities.stream().allMatch(r -> security.hasAuthority(r));
	}
	
}
