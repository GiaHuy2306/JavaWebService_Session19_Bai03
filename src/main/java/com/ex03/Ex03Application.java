package com.ex03;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Ex03Application {

    public static void main(String[] args) {
        SpringApplication.run(Ex03Application.class, args);
    }

}
