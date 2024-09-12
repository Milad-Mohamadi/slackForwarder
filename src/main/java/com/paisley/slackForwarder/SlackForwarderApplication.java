package com.paisley.slackForwarder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SlackForwarderApplication {

	public static void main(String[] args) {
		SpringApplication.run(SlackForwarderApplication.class, args);
	}

}
