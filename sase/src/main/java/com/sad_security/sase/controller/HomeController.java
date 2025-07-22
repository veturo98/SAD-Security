package com.sad_security.sase.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;

import org.springframework.security.core.Authentication;

/**
 * Controller che si occupa del primo indirizzamento alle pagine principali.
 */
@Controller
public class HomeController {

    /**
     * Restituisce la vista della home page.
     *
     * @return nome della view "home"
     */
    @GetMapping("/")
    public String getLayout() {
        return "home";
    }

    /**
     * Restituisce la vista della pagina di login generale.
     *
     * @return nome della view "login"
     */
    @GetMapping("/login")
    public String getLogin() {
        return "login";
    }

    /**
     * Restituisce la vista della pagina di login per il professore.
     *
     * @return nome della view "loginProfessore"
     */
    @GetMapping("/professore/login")
    public String getLoginProfessore() {
        return "loginProfessore";
    }

    /**
     * Restituisce la vista della home page.
     *
     * @return nome della view "home"
     */
    @GetMapping("/home")
    public String getHome() {
        return "home";
    }

    /**
     * Restituisce la vista della pagina di registrazione.
     *
     * @return nome della view "registration"
     */
    @GetMapping("/registrati")
    public String getRegistrati() {
        return "registration";
    }

    /**
     * Restituisce la vista della dashboard dello studente,
     * aggiungendo lo username autenticato al modello per la view.
     *
     * @param model oggetto Model per la view
     * @param authentication oggetto di autenticazione Spring Security
     * @return nome della view "dashboard"
     */
    @GetMapping("/studente/dashboard")
    public String getDashboard(Model model, Authentication authentication) {

        if (authentication != null) {
            String username = authentication.getName();
            model.addAttribute("username", username); // Aggiunta dello username al modello per Thymeleaf
        }

        return "dashboard";
    }

    /**
     * Restituisce la vista della dashboard del professore,
     * aggiungendo lo username autenticato al modello per la view.
     *
     * @param model oggetto Model per la view
     * @param authentication oggetto di autenticazione Spring Security
     * @return nome della view "profDashboard"
     */
    @GetMapping("/professore/profDashboard")
    public String getadminDashboard(Model model, Authentication authentication) {

        if (authentication != null) {
            String username = authentication.getName();
            model.addAttribute("username", username);
        }

        return "profDashboard";
    }

    /**
     * Restituisce la vista della pagina di accesso negato.
     *
     * @return nome della view "accessDenied"
     */
    @GetMapping("/accessDenied")
    public String accessDenied() {
        return "accessDenied";
    }

}
