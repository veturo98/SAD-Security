package com.sad_security.sase.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sad_security.sase.service.UtenteServices;

@Controller
public class StudentController {
    // TBD
    // Schermate di gestione dell'utente
    @Autowired
    private UtenteServices utenteServices;

    @PostMapping("/check")
    public String controllaLogin(@RequestParam String username, @RequestParam String password, Model Utente) {

        boolean success = utenteServices.autenticaUtente(username, password);
        if (success) {
            Utente.addAttribute("username", username);
            return "dashboard";

        } else {
            Utente.addAttribute("errorMessage", "Username o password errate.");
            return "login";
        }

    }

    @PostMapping("/add-user")
    public String registraUtente(@RequestParam String username, @RequestParam String mail,
            @RequestParam String password, Model Utente) {

        boolean exists = utenteServices.aggiungiUtente(username, password, mail);

        // Se l'utente esiste allora procedo verso la pagina di login
        if (exists) {
            Utente.addAttribute("errorMessage", "Username o mail già usate.");
            return "registration";

        } else {
            Utente.addAttribute("successMessage", "Registrazione avvenuta con successo! Accedi.");
            return "login";
        }

    }

}
