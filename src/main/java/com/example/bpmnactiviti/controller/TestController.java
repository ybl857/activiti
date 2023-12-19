package com.example.bpmnactiviti.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author yanbl
 * @Date 2023/12/18 14:48
 * @Description ...
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/sayHello")
    public String sayHello() {
        return "hello world";
    }
}
