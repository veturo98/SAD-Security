package com.sad_security.sase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;



@Controller
public class HelloWorldController {

    @GetMapping("/")
    @ResponseBody
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
