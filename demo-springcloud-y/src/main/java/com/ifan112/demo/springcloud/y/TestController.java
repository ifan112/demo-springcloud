package com.ifan112.demo.springcloud.y;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public String test() {
        return "Y Test";
    }

    @GetMapping("test2")
    public String test2(@RequestParam Integer id) {
        /*
         * 默认情况下，Spring 框架不会处理任何异常，将异常向上抛给了 Tomcat，最后返回的响应如下：
         * HTTP/1.1 500
         * Content-Type: application/json
         * Connection: close
         *
         * {"timestamp":"2020-07-14T11:29:22.789+00:00","status":500,"error":"Internal Server Error","message":"","path":"/test2"}
         */

        if (id < 0) {
            throw new IllegalArgumentException("id invalid.");
        }

        return "Y Test2 OK";
    }
}
