package com.workbridge.workbridge_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WorkbridgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkbridgeApplication.class, args);
	}

}
