package com.workbridge.workbridge_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.workbridge.workbridge_app.config.SecurityProperties;

@SpringBootApplication
@EnableConfigurationProperties(SecurityProperties.class)
@EnableScheduling
public class WorkbridgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkbridgeApplication.class, args);
	}

}
