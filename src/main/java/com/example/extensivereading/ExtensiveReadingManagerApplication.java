package com.example.extensivereading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class ExtensiveReadingManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExtensiveReadingManagerApplication.class, args);
	}
	
	@Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
