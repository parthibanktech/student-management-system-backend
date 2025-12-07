package com.studentmanagement.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class InfrastructureApplication {

    public static void main(String[] args) {
        System.out.println("Starting Infrastructure Service...");
        SpringApplication.run(InfrastructureApplication.class, args);
        System.out.println("Infrastructure Service Started Successfully");
    }
}
