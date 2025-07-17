package com.sad_security.sase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import org.springframework.security.core.Authentication;

@Controller
public class HomeController {

    @GetMapping("/")
    public String getLayout() {
        return "home";
    }

    @GetMapping("/login")
    public String getLogin() {
        return "login";
    }

    @GetMapping("/professore/login")
    public String getLoginProfessore() {
        return "loginProfessore";
    }

    @GetMapping("/home")
    public String getHome() {
        return "home";
    }
    
    @GetMapping("/registrati")
    public String getRegistrati() {
        return "registration";
    }

    @GetMapping("/studente/dashboard")
    public String getDashboard(Model model, Authentication authentication) {
         
        if (authentication != null) {
            String username = authentication.getName(); // Ottieni l'username dell'utente autenticato
            model.addAttribute("username", username); // Aggiungi l'username al modello per Thymeleaf
        }
        
        return "dashboard";
    }
    
     @GetMapping("/professore/profDashboard")
    public String getadminDashboard(Model model, Authentication authentication) {
         
        if (authentication != null) {
            String username = authentication.getName(); // Ottieni l'username dell'utente autenticato
            model.addAttribute("username", username); // Aggiungi l'username al modello per Thymeleaf
        }
        
        return "profDashboard";
    }


    @GetMapping("/accessDenied")
    public String accessDenied() {
        return "accessDenied";
    }
   
}
