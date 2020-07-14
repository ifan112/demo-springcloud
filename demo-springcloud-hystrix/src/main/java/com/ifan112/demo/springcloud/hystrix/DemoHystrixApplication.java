package com.ifan112.demo.springcloud.hystrix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.ConfigurableApplicationContext;

@EnableHystrix
@SpringBootApplication
public class DemoHystrixApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(DemoHystrixApplication.class, args);

        try {

            TestService testService = applicationContext.getBean(TestService.class);
            System.out.println(testService.get(-1));

        } finally {
            applicationContext.close();
        }
    }
}
