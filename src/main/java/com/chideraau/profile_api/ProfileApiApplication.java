package com.chideraau.profile_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProfileApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProfileApiApplication.class, args);
		System.out.println("Profile API is running on http://localhost:8080");
        System.out.println("Test the endpoint: http://localhost:8080/me");
	}

}
