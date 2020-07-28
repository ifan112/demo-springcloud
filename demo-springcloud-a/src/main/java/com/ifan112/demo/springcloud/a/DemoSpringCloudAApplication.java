package com.ifan112.demo.springcloud.a;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class DemoSpringCloudAApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoSpringCloudAApplication.class, args);
    }
}
