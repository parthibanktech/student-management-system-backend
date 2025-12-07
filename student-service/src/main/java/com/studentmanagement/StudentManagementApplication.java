package com.studentmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main Application Class for Student Management Microservice
 * 
 * @author Student Management Team
 * @version 1.0.0
 */


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main Application Class for Student Management Microservice
 * 
 * @author Student Management Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class StudentManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentManagementApplication.class, args);
        System.out.println("Student Service Application Started...");
        System.out.println("🚀 Student Management Microservice Started Successfully!");
    }
}
