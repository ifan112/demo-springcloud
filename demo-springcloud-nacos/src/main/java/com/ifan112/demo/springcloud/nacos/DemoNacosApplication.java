package com.ifan112.demo.springcloud.nacos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DemoNacosApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoNacosApplication.class, args);
    }
}
