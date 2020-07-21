package com.ifan112.demo.springcloud.x;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class TestController {

    private static final String yServiceId = "demo-springcloud-y";

    private final RestTemplate restTemplate;

    @Autowired
    private RestTemplate normalRestTemplate;

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

    @GetMapping("/test4")
    public String test4(Integer id) {
        StringBuilder respBuilder = new StringBuilder();
        respBuilder.append("X test4").append("\n");

        ResponseEntity<String> respEntity = restTemplate.getForEntity("http://" + yServiceId + "/test3?id={id}", String.class, id);
        System.out.println(respEntity.getStatusCode().toString());

        respBuilder.append(respEntity.getStatusCode().toString())
                .append("\n");
        respBuilder.append(respEntity.getBody());

        return respBuilder.toString();
    }

    /**
     * 测试 sleuth 将会拦截并记录普通的 restTemplate 请求
     *
     * 这是因为 sleuth 将所有的 RestTemplate 实例都插入了请求拦截器 TracingClientHttpRequestInterceptor
     */
    @GetMapping("/test5")
    public String test5() {
        ResponseEntity<String> respEntity = normalRestTemplate.getForEntity("http://www.baidu.com", String.class);

        System.out.println(respEntity.getStatusCode());

        return respEntity.getBody();
    }

    /**
     * 测试三个服务调用
     */
    @GetMapping("/test6")
    public String test6(Integer id) {
        StringBuilder respBuilder = new StringBuilder();
        respBuilder.append("X test6").append("\n");

        ResponseEntity<String> respEntity = restTemplate.getForEntity("http://" + yServiceId + "/test4", String.class);
        System.out.println(respEntity.getStatusCode().toString());

        respBuilder.append(respEntity.getStatusCode().toString())
                .append("\n");
        respBuilder.append(respEntity.getBody());

        return respBuilder.toString();
    }
}
