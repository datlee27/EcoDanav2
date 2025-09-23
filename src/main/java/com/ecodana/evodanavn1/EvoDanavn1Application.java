package com.ecodana.evodanavn1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.ecodana.evodanavn1.model")
public class EvoDanavn1Application {

    public static void main(String[] args) {
        SpringApplication.run(EvoDanavn1Application.class, args);
    }

}
