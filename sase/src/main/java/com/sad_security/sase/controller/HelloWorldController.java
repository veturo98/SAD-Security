package com.sad_security.sase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloWorldController {

    @GetMapping("/")
    public String SayHello() {
        return "Hello, world!";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    
}
