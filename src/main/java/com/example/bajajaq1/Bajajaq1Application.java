package com.example.bajajaq1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class Bajajaq1Application {

	public static void main(String[] args) {
		SpringApplication.run(Bajajaq1Application.class, args);
	}

}
