package com.sijo.boondmcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("com.sijo.boondmcp.config")
public class BoondMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoondMcpApplication.class, args);
    }
}