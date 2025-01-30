package com.workbridge.workbridge_app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import com.workbridge.workbridge_app.config.TestDatabaseConfig;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")  
@Import(TestDatabaseConfig.class)
class WorkbridgeApplicationTests {

	@Test
	void contextLoads() {
	}

}
