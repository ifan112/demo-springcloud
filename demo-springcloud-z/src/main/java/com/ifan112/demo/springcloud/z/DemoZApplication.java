package com.ifan112.demo.springcloud.z;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DemoZApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoZApplication.class, args);
    }
}
