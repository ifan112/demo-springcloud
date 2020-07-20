package com.ifan112.demo.springcloud.y;

import com.ifan112.demo.springcloud.y.entity.Module;
import com.ifan112.demo.springcloud.y.repo.ModuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class TestController {

    @Autowired
    private ModuleRepository moduleRepository;

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

    @GetMapping("/test3")
    public ResponseEntity<Module> test3(Integer id) {
        Optional<Module> record = moduleRepository.findById(id);

        if (record.isPresent()) {
            return ResponseEntity.ok(record.get());
        }

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
