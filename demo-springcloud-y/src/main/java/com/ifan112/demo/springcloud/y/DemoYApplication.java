package com.ifan112.demo.springcloud.y;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DemoYApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoYApplication.class, args);
    }
}
