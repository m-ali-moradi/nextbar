package com.nextbar.bar;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Basic context load test for the Bar application.
 *
 * This test ensures that the Spring application context can start successfully
 * without any configuration issues. It does not verify any specific beans or
 * functionality but serves as a sanity check for the overall application setup.
 */
@SpringBootTest(properties = {
		"spring.cloud.config.enabled=false",
		"spring.config.import="
})
public class BarApplicationTest {
	@Test
	void contextLoads() {
	}

}
