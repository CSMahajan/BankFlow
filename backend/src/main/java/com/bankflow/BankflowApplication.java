package com.bankflow;

import com.bankflow.security.ClamAvProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(ClamAvProperties.class)
@SpringBootApplication
public class BankflowApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankflowApplication.class, args);
    }
}
