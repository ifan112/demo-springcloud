package com.ifan112.demo.springcloud.openfeign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "demo-springcloud-y")
public interface YService {

    @RequestMapping("/test")
    String test();
}
