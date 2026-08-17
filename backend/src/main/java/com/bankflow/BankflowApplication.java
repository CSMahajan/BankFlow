package com.bankflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class BankflowApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankflowApplication.class, args);
    }
}
