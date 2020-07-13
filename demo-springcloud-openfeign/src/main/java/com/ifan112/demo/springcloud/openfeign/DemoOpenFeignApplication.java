package com.ifan112.demo.springcloud.openfeign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class DemoOpenFeignApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext
                = SpringApplication.run(DemoOpenFeignApplication.class, args);

        YService yService = applicationContext.getBean(YService.class);
        String resp = yService.test();

        System.out.println(resp);

        applicationContext.close();
    }
}
