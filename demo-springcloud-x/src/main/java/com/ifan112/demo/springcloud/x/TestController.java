package com.ifan112.demo.springcloud.x;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class TestController {

    private static final String yServiceId = "demo-springcloud-y";

    private final RestTemplate restTemplate;

    public TestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/test")
    public String test() {
        return "X Test";
    }

    @GetMapping("/test2")
    public String test2() {
        StringBuilder respBuilder = new StringBuilder();
        respBuilder.append("X test2").append("\n");

        ResponseEntity<String> respEntity = restTemplate.getForEntity("http://" + yServiceId + "/test", String.class);
        System.out.println(respEntity.getStatusCode().toString());

        respBuilder.append(respEntity.getStatusCode().toString())
                .append("\n");
        respBuilder.append(respEntity.getBody());

        return respBuilder.toString();
    }

    @GetMapping("/test3")
    public String test3(@RequestParam Integer id) {
        StringBuilder respBuilder = new StringBuilder();
        respBuilder.append("X test3").append("\n");

        /*
         * restTemplate 在得到响应后，检查 Header 发现状态码是 4xx 或 5xx 时就会抛出异常
         *
         * 实际上，它依然得到了下面的响应内容：
         * {"timestamp":"2020-07-14T11:29:22.789+00:00","status":500,"error":"Internal Server Error","message":"","path":"/test2"}
         *
         * 除非上游服务在响应的消息体中主动地返回自己的异常栈，否则下游服务无法得到上游服务异常栈是什么
         */
        ResponseEntity<String> respEntity = restTemplate.getForEntity("http://" + yServiceId + "/test2?id=" + id, String.class);
        System.out.println(respEntity.getStatusCode().toString());

        respBuilder.append(respEntity.getStatusCode().toString())
                .append("\n");
        respBuilder.append(respEntity.getBody());

        return respBuilder.toString();
    }
}
