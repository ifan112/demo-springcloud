package com.ifan112.demo.springcloud.x;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
