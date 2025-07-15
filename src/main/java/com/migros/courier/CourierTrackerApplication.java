package com.migros.courier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CourierTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(CourierTrackerApplication.class, args);
	}

}
