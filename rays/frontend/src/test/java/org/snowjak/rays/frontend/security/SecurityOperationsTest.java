package org.snowjak.rays.frontend.security;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@WithAnonymousUser
@TestPropertySource(properties = { "rabbitmq.resultq=test-result", "rabbitmq.progressq=test-progress" })
public class SecurityOperationsTest {
	
	@Autowired
	private SecurityOperations security;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@MockBean
	private UserDetailsService userDetailsService;
	
	@Before
	public void setUpUserDetailsService() {
		
		when(userDetailsService.loadUserByUsername("testUser")).thenReturn(User.builder().username("testUser")
				.password(this.passwordEncoder.encode("password")).roles("USER").build());
		when(userDetailsService.loadUserByUsername(argThat(v -> !v.equals("testUser"))))
				.thenThrow(new UsernameNotFoundException("You tried to get a non-TEST user."));
	}
	
	@Test
	public void testIsAuthenticated_withoutAuthenticating() {
		
		assertFalse("SecurityOperations reported as authenticated before any authentication was done!",
				security.isAuthenticated());
	}
	
	@Test
	public void testIsAuthenticated_withAuthenticating() throws SecurityOperationException {
		
		security.doLogIn("testUser", "password");
		
		assertTrue("SecurityOperations reported as not authenticated after authentication was successful!",
				security.isAuthenticated());
	}
	
	@Test
	public void testDoLogIn() throws SecurityOperationException {
		
		final Authentication returnedAuth = security.doLogIn("testUser", "password");
		
		assertNotNull("Returned Authentication was null!", returnedAuth);
		assertEquals("Returned Auth did not have expected username.", "testUser", returnedAuth.getName());
		assertArrayEquals("Returned Auth did not have expected roles.", new String[] { "ROLE_USER" },
				returnedAuth.getAuthorities().stream().map((GrantedAuthority ga) -> ga.getAuthority())
						.collect(Collectors.toList()).toArray(new String[0]));
		
	}
	
	@Test
	@WithMockUser(username = "testUser", password = "password", authorities = "ROLE_USER")
	public void testDoLogOut() throws SecurityOperationException {
		
		security.doLogOut();
		
		assertFalse("Authentication has not been logged out!", security.isAuthenticated());
	}
	
}
