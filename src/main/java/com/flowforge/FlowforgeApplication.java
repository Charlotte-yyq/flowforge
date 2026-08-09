package com.flowforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FlowforgeApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlowforgeApplication.class, args);
	}

}
