package org.snowjak.rays.frontend;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AppTest {
	
	@Autowired
	private TestRestTemplate webClient;
	
	@Test
	public void appContextStartup() throws Exception {
		
	}
	
	@Test
	public void appRespondsAtRootURL() {
		
		try {
			final String result = this.webClient.getForObject("/", String.class);
			
			assertNotNull("App URL should not return null!", result);
		} catch (RestClientException e) {
			fail("Unexpected exception: " + e.getClass().getName() + ": " + e.getMessage());
		}
		
	}
	
}
