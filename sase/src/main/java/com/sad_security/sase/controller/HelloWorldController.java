package com.sad_security.sase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;




@Controller
public class HelloWorldController {

    @GetMapping("/")
    public String layout() {
        return "layout";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

  
    
    
    
}
