package com.ifan112.demo.springcloud.hystrix;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.stereotype.Service;

@Service
public class TestService {

    // fallbackMethod 必须与被注解的方法有相同的签名
    @HystrixCommand(fallbackMethod = "error")
    public String get(Integer i) {
        if (i < 0) {
            throw new IllegalArgumentException("i invalid");
        }

        return "ok";
    }

    public String error(Integer i) {
        System.out.println("i: " + i);

        return "Error";
    }
}
