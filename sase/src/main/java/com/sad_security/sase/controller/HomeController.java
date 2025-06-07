package com.sad_security.sase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String getLayout() {
        return "layout";
    }

    @GetMapping("/login")
    public String getLogin() {
        return "login";
    }

    @GetMapping("/home")
    public String getHome() {
        return "home";
    }

    @GetMapping("/dashboard")
    public String getDashboard() {
        return "dashboard";
    }
    

}
