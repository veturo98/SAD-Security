package com.sad_security.sase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;



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

    @PostMapping("/home")
    public String home() {
        return "home";
    }

  
    
    
    
}
