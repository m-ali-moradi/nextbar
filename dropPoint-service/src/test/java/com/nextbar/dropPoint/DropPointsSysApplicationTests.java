package com.nextbar.dropPoint;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Basic context load test for the DropPointsSys application.
 *
 * This test ensures that the Spring application context can start successfully
 * without any configuration issues. It does not verify any specific beans or
 * functionality, but serves as a sanity check for the overall application setup.
 */
@SpringBootTest(properties = {
		"spring.cloud.config.enabled=false",
		"spring.config.import=",
		"spring.cloud.stream.enabled=false",
		"spring.cloud.discovery.enabled=false",
		"eureka.client.enabled=false",
		"spring.main.lazy-initialization=true",
		"spring.data.jpa.repositories.bootstrap-mode=lazy",
		"security.internal.enabled=false",
		"jwt.secret=test-jwt-secret",
		"security.internal.secret=test-internal-secret",
		"INTERNAL_SERVICE_SECRET=test-internal-secret",
		"spring.datasource.url=jdbc:mysql://localhost:3306/nextbar_test",
		"spring.datasource.username=test",
		"spring.datasource.password=test",
		"spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
		"spring.datasource.hikari.initialization-fail-timeout=0",
		"spring.jpa.hibernate.ddl-auto=none",
		"spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect",
		"spring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false",
		"spring.sql.init.mode=never"
})
class DropPointsSysApplicationTests {

	@Test
	void contextLoads() {
	}

}
