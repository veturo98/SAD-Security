package com.sad_security.sase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;

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
    
    @GetMapping("/registrati")
    public String getRegistrati() {
        return "registration";
    }

    @GetMapping("/dashboard")
    public String getDashboard(Model model, Authentication authentication) {
         
        if (authentication != null) {
            String username = authentication.getName(); // Ottieni l'username dell'utente autenticato
            model.addAttribute("username", username); // Aggiungi l'username al modello per Thymeleaf
        }
        
        return "dashboard";
    }
    

}
